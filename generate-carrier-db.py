#!/usr/bin/env python3
"""
generate-carrier-db.py
======================
Cross-references Samsung One UI (Exynos / Shannon modem) IMS & APN configuration
files against Google Pixel CarrierSettings protobufs to generate
`res/raw/carrier_db.json` for Pixel IMS & APN Tuner.

Required input files
--------------------
1. Samsung directory (`--samsung-dir`, default: `./firmware/samsung`):
   - `mnomap.json`      (from `/system/priv-app/imsservice/imsservice.apk` -> `res/raw/mnomap.json`)
   - `imsswitch.json`   (from `/system/priv-app/imsservice/imsservice.apk` -> `res/raw/imsswitch.json`)
   - `imsprofile.json`  (from `/system/priv-app/imsservice/imsservice.apk` -> `res/raw/imsprofile.json`)
   - `apns-conf.xml`    (from `/system/etc/apns-conf.xml`)
   - `build.prop`       (optional, from `/system/build.prop` — reads model, 4-char PDA build,
                         and Android release version for Samsung IMS User-Agent templates;
                         defaults to `SM-S942B` / `BZK1` / `17` when absent)

2. Google Pixel directory (`--pixel-dir`, default: `./firmware/pixel/CarrierSettings`):
   - `carrier_list.pb`  (from `/product/etc/CarrierSettings/carrier_list.pb`)
   - `others.pb`        (from `/product/etc/CarrierSettings/others.pb`)
   - `*.pb`             (from `/product/etc/CarrierSettings/*.pb`)

Note: Never commit proprietary firmware dumps (`./firmware/`) to Git.

Usage examples
--------------
  ./generate-carrier-db.py                                   # Print audit table
  ./generate-carrier-db.py --update                          # Write res/raw/carrier_db.json
  ./generate-carrier-db.py --mccs 250,255,257 --update       # Restrict to specific 3-digit MCCs
  ./generate-carrier-db.py --samsung-dir /path/to/samsung \\
                           --pixel-dir /path/to/CarrierSettings --update
"""

from collections import Counter
import argparse
import json
import os
import re
import sys
import xml.etree.ElementTree as ET

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DEFAULT_SAMSUNG_DIR = os.path.join(BASE_DIR, "firmware", "samsung")
DEFAULT_PIXEL_DIR = os.path.join(BASE_DIR, "firmware", "pixel", "CarrierSettings")
DEFAULT_OUTPUT_PATH = os.path.join(BASE_DIR, "res", "raw", "carrier_db.json")

# Non-geographic, lab, or invalid test MCC prefixes excluded from global scans
EXCLUDED_TEST_MCCS = ("001", "258", "777", "901", "966", "999")

# Default Samsung OMC sales codes used when expanding `[OMCCODE]` in `imsprofile.json`
SAMSUNG_OMC_CODES = {
    "TMobile_AT": "MAX",
    "SKT_KR": "SKC",
    "LGU+_KR": "LUC",
    "KT_KR": "KTC",
    "Orange_MA": "MWD",
}

# Live-network verified overrides where an operator's production core or brand differs
# from Samsung's legacy template (bypass with --pure-samsung):
LIVE_OVERRIDES = {
    "25701": {
        # Velcom rebranded to A1 Belarus; verified PAP/CHAP (3) + IPv4 internet APN on live network
        "mno": "A1_BY",
        "inet_auth": "3",
        "inet_ip": "IP",
    },
    "25702": {
        # Verified on live MTS BY (25702): MTS BY has VoLTE only (no production VoWiFi ePDG)
        "rat_mode": "LTE_ONLY",
    },
}


def load_samsung_json(path: str) -> dict:
    """Load Samsung lenient JSON (strips /* */ and // comments and fixes trailing/missing commas)."""
    with open(path, "r", encoding="utf-8") as f:
        s = f.read()
    s = re.sub(r"/\*.*?\*/", "", s, flags=re.S)
    s = re.sub(r"(?m)(?<!:)//.*$", "", s)
    s = re.sub(r'(true|false|"[^"]*"|\d+)\s*\n(\s*"[^"]+"\s*:)', r"\1,\n\2", s)
    s = re.sub(r",\s*([}\]])", r"\1", s)
    return json.loads(s)


def load_samsung_build_meta(samsung_dir: str) -> tuple:
    """Read (model, 4-char build version, Android release) from Samsung build.prop if present."""
    model = "SM-S942B"
    build_ver = "BZK1"
    os_ver = "17"
    prop_path = os.path.join(samsung_dir, "build.prop")
    if os.path.isfile(prop_path):
        with open(prop_path, "r", encoding="utf-8", errors="ignore") as f:
            for line in f:
                line = line.strip()
                if line.startswith("ro.product.system.model="):
                    val = line.split("=", 1)[1].strip()
                    if val:
                        model = val
                elif line.startswith("ro.build.PDA="):
                    val = line.split("=", 1)[1].strip()
                    if len(val) >= 4:
                        build_ver = val[-4:]
                elif line.startswith("ro.build.version.release="):
                    val = line.split("=", 1)[1].strip()
                    if val:
                        os_ver = val
    return model, build_ver, os_ver


def parse_proto(data: bytes) -> list:
    """Minimal pure-Python Protobuf wire-format parser (no external protobuf package required)."""
    pos = 0
    fields = []
    n = len(data)
    while pos < n:
        tag = 0
        shift = 0
        while True:
            b = data[pos]
            pos += 1
            tag |= (b & 0x7F) << shift
            if not (b & 0x80):
                break
            shift += 7
        fnum = tag >> 3
        wtype = tag & 7
        if wtype == 0:  # varint
            val = 0
            shift = 0
            while True:
                b = data[pos]
                pos += 1
                val |= (b & 0x7F) << shift
                if not (b & 0x80):
                    break
                shift += 7
            fields.append((fnum, wtype, val))
        elif wtype == 2:  # length-delimited
            ln = 0
            shift = 0
            while True:
                b = data[pos]
                pos += 1
                ln |= (b & 0x7F) << shift
                if not (b & 0x80):
                    break
                shift += 7
            fields.append((fnum, wtype, data[pos : pos + ln]))
            pos += ln
        elif wtype == 1:  # 64-bit
            fields.append((fnum, wtype, data[pos : pos + 8]))
            pos += 8
        elif wtype == 5:  # 32-bit
            fields.append((fnum, wtype, data[pos : pos + 4]))
            pos += 4
        else:
            break
    return fields


def parse_pixel_carrier_pb(raw_bytes: bytes) -> dict:
    """Extract IMS APN presence, bearer bitmask, and VoLTE/VoWiFi flags from a Pixel CarrierSettings protobuf."""
    info = {
        "volte": False,
        "wfc": False,
        "ims_apn": False,
        "ims_iwlan": False,
        "ims_bearers": "",
    }
    for fn, wt, val in parse_proto(raw_bytes):
        if fn == 3 and wt == 2:  # Apns message
            for afn, awt, aval in parse_proto(val):
                if afn == 2 and awt == 2:  # ApnItem
                    apn_val = ""
                    types = []
                    bearer_mask = ""
                    for ifn, iwt, ival in parse_proto(aval):
                        if ifn == 2 and iwt == 2:
                            apn_val = ival.decode("utf-8", errors="ignore")
                        elif ifn == 3 and iwt == 0:
                            types.append(ival)
                        elif ifn == 4 and iwt == 2:
                            bearer_mask = ival.decode("utf-8", errors="ignore")
                    if 7 in types or apn_val.lower() == "ims":
                        info["ims_apn"] = True
                        info["ims_bearers"] = bearer_mask
                        if "18" in bearer_mask.split("|") or bearer_mask in ("", "0"):
                            info["ims_iwlan"] = True
        elif fn == 4 and wt == 2:  # Configs message
            for cfn, cwt, cval in parse_proto(val):
                if cfn == 2 and cwt == 2:
                    key = ""
                    bval = False
                    for kfn, kwt, kval in parse_proto(cval):
                        if kfn == 1 and kwt == 2:
                            key = kval.decode("utf-8", errors="ignore")
                        elif kfn == 5 and kwt == 0:
                            bval = bool(kval)
                    if key == "carrier_volte_available_bool":
                        info["volte"] = bval
                    elif key == "carrier_wfc_ims_available_bool":
                        info["wfc"] = bval
    return info


def load_pixel_database(pixel_dir: str):
    """Load Pixel carrier_list.pb, others.pb, and individual <carrier>.pb files."""
    with open(os.path.join(pixel_dir, "carrier_list.pb"), "rb") as f:
        cl_data = f.read()

    mccmnc_to_cname = {}
    qual_counts = {}
    for fnum, wtype, val in parse_proto(cl_data):
        if fnum == 1 and wtype == 2:
            cname = ""
            cids = []
            qual_cids = []
            for sf, sw, sv in parse_proto(val):
                if sf == 1 and sw == 2:
                    cname = sv.decode("utf-8", errors="ignore")
                elif sf == 2 and sw == 2:
                    mccmnc = ""
                    extra = []
                    for cf, cw, cv in parse_proto(sv):
                        if cf == 1 and cw == 2:
                            mccmnc = cv.decode("utf-8", errors="ignore")
                        else:
                            extra.append(cf)
                    if mccmnc:
                        if not extra:
                            cids.append(mccmnc)
                        else:
                            qual_cids.append(mccmnc)
            for m in cids:
                mccmnc_to_cname[m] = cname
            if cname and os.path.exists(os.path.join(pixel_dir, f"{cname}.pb")):
                for m in qual_cids:
                    qual_counts.setdefault(m, Counter())[cname] += 1

    for m, cnt in qual_counts.items():
        if m not in mccmnc_to_cname:
            mccmnc_to_cname[m] = cnt.most_common(1)[0][0]

    others_map = {}
    with open(os.path.join(pixel_dir, "others.pb"), "rb") as f:
        for fn, wt, val in parse_proto(f.read()):
            if fn == 2 and wt == 2:
                cname = ""
                for sfn, swt, sval in parse_proto(val):
                    if sfn == 1 and swt == 2:
                        cname = sval.decode("utf-8", errors="ignore")
                if cname:
                    others_map[cname] = parse_pixel_carrier_pb(val)

    pb_cache = {}

    def get_status(mccmnc: str) -> dict:
        cname = mccmnc_to_cname.get(mccmnc)
        if not cname:
            return {
                "source": "unlisted",
                "volte": False,
                "wfc": False,
                "ims_apn": False,
                "ims_iwlan": False,
                "ims_bearers": "",
            }
        pb_path = os.path.join(pixel_dir, f"{cname}.pb")
        if os.path.exists(pb_path):
            if cname not in pb_cache:
                with open(pb_path, "rb") as f:
                    pb_cache[cname] = parse_pixel_carrier_pb(f.read())
            res = dict(pb_cache[cname])
            res["source"] = f"{cname}.pb"
            return res
        res = dict(
            others_map.get(
                cname,
                {
                    "volte": False,
                    "wfc": False,
                    "ims_apn": False,
                    "ims_iwlan": False,
                    "ims_bearers": "",
                },
            )
        )
        res["source"] = f"others.pb[{cname}]"
        return res

    return get_status


def resolve_operator_ims_ua(
    mccmnc: str,
    base_mno: str,
    exact_ims_profiles: list,
    model: str,
    build_ver: str,
    os_ver: str,
) -> str:
    """
    Resolve an operator-specific Samsung VoLTE SIP User-Agent from `imsprofile.json` if one
    is customized and differs from the default `[PRODUCT_MODEL]-[BUILD_VERSION] Samsung IMS 6.0`.
    """
    default_ua = f"{model}-{build_ver} Samsung IMS 6.0"
    omc = SAMSUNG_OMC_CODES.get(base_mno, "")
    for p in exact_ims_profiles:
        has_mmtel = any(
            "mmtel" in net.get("services", [])
            for net in p.get("network", [])
            if net.get("enabled") is not False
        )
        if not has_mmtel and p.get("network"):
            continue
        ua = (p.get("useragent") or "").strip()
        if (
            not ua
            or "IM-client/OMA" in ua
            or "[IMEISV]" in ua
            or "[IMS_PROFILE_VOICE]" in ua
            or "[RCS_PROFILE]" in ua
        ):
            continue
        prof_omc = omc
        for m in p.get("mnoname", "").split(","):
            m = m.strip()
            if m.split(":")[0].strip() == base_mno and ":" in m:
                prof_omc = m.split(":")[1].split("@")[0].strip()
                break
        resolved = (
            ua.replace("[PRODUCT_MODEL]", model)
            .replace("[BUILD_VERSION]", build_ver)
            .replace("[OS_VERSION]", os_ver)
            .replace("[PRODUCT_TYPE]", "Phone")
            .replace("[SUPPORT]", "VoLTE")
            .replace("[UICC_VERSION]", "3.0")
            .replace("[MCCMNC]", mccmnc)
            .replace("[MNO_CUSTOM]", f"mno-custom/{mccmnc[:3]}_{mccmnc[3:]}")
            .replace("[OMCCODE]", prof_omc)
            .strip()
        )
        if resolved and resolved != default_ua and "[" not in resolved:
            return resolved
    return ""


def validate_inputs(samsung_dir: str, pixel_dir: str):
    """Verify all required input files are present and print actionable guidance if missing."""
    missing = []
    for fname in ("mnomap.json", "imsswitch.json", "imsprofile.json", "apns-conf.xml"):
        fpath = os.path.join(samsung_dir, fname)
        if not os.path.isfile(fpath):
            missing.append(fpath)
    for fname in ("carrier_list.pb", "others.pb"):
        fpath = os.path.join(pixel_dir, fname)
        if not os.path.isfile(fpath):
            missing.append(fpath)
    if missing:
        sys.stderr.write("ERROR: Missing required firmware input files:\n")
        for m in missing:
            sys.stderr.write(f"  - {m}\n")
        sys.stderr.write(
            "\nRun `./generate-carrier-db.py --help` or see README.md ('Regenerating CARRIER_DB') "
            "for instructions on extracting these files via `adb pull` or firmware images.\n"
        )
        sys.exit(1)


def build_carrier_db(samsung_dir: str, pixel_dir: str, target_mccs, pure_samsung: bool = False):
    validate_inputs(samsung_dir, pixel_dir)
    get_pixel_status = load_pixel_database(pixel_dir)
    s_model, s_build, s_os = load_samsung_build_meta(samsung_dir)

    mnomap = load_samsung_json(os.path.join(samsung_dir, "mnomap.json"))["mnomap"]
    imsswitch_raw = load_samsung_json(os.path.join(samsung_dir, "imsswitch.json"))
    imsprofile_raw = load_samsung_json(os.path.join(samsung_dir, "imsprofile.json"))
    apn_tree = ET.parse(os.path.join(samsung_dir, "apns-conf.xml"))

    s_apns = {}
    for apn in apn_tree.getroot().findall("apn"):
        key = f"{apn.attrib.get('mcc', '')}{apn.attrib.get('mnc', '')}"
        s_apns.setdefault(key, []).append(apn.attrib)

    switches = {}
    for item in imsswitch_raw.get("imsswitch", []):
        switches[item["mnoname"].split(":")[0]] = item
        switches[item["mnoname"]] = item

    profiles = {}
    exact_profiles = {}
    for p in imsprofile_raw.get("profile", []):
        for m in p.get("mnoname", "").split(","):
            m_clean = m.strip()
            profiles.setdefault(m_clean.split(":")[0].strip(), []).append(p)
            exact_profiles.setdefault(m_clean, []).append(p)

    seen_plmns = set()
    audit_rows = []
    db_rows = []

    for entry in mnomap:
        mccmnc = entry.get("mccmnc", "")
        if not mccmnc.isdigit() or mccmnc in seen_plmns:
            continue
        if mccmnc[:3] in EXCLUDED_TEST_MCCS:
            continue
        if target_mccs and mccmnc[:3] not in target_mccs:
            continue
        if entry.get("subset") or entry.get("gid1") or entry.get("spname"):
            continue

        seen_plmns.add(mccmnc)
        raw_mno = entry.get("mnoname", "")
        if ":Inbound" in raw_mno:
            continue
        base_mno = raw_mno.split(":")[0].split("@")[0]
        if base_mno in ("DEFAULT", "GoogleGC_ALL", "LABSIM", "Samsung_OPEN"):
            continue

        sw = switches.get(base_mno, {})
        s_volte = bool(sw.get("enableServiceVolte"))
        s_vowifi = bool(sw.get("enableServiceVowifi"))
        if not s_volte and not s_vowifi:
            continue

        ps = profiles.get(base_mno, [])
        ims_p = [
            p
            for p in ps
            if (p.get("pdn") or "ims") == "ims"
            and "emergency" not in p.get("name", "").lower()
            and "e911" not in p.get("name", "").lower()
        ]
        exact_ims_p = [
            p
            for p in (exact_profiles.get(base_mno) or ps)
            if (p.get("pdn") or "ims") == "ims"
            and "emergency" not in p.get("name", "").lower()
            and "e911" not in p.get("name", "").lower()
        ]

        ims_ip = "IPV4V6"
        nets = set()
        rep_plmn = ""
        prof_name = ""
        for p in ims_p:
            if not rep_plmn and p.get("representative_plmn"):
                rep_plmn = str(p.get("representative_plmn"))
            if not prof_name and p.get("name"):
                prof_name = str(p.get("name")).strip()
            if p.get("ipver") == "ipv4":
                ims_ip = "IP"
            elif p.get("ipver") == "ipv6":
                ims_ip = "IPV6"
            for net in p.get("network", []):
                if net.get("enabled") is not False and "mmtel" in net.get("services", []):
                    for nt in net.get("type", "").split(","):
                        nets.add(nt.strip())

        if s_volte and (s_vowifi and "wifi" in nets):
            rat_mode = "LTE_WIFI"
        elif not s_volte and s_vowifi:
            rat_mode = "WIFI_ONLY"
        else:
            rat_mode = "LTE_ONLY"
            s_vowifi = False

        raw_apn_list = s_apns.get(mccmnc, []) or (s_apns.get(rep_plmn, []) if rep_plmn else [])
        base_apn_list = [a for a in raw_apn_list if not a.get("mvno_type")]
        apn_list = base_apn_list if base_apn_list else raw_apn_list

        inet_apns = [
            a
            for a in apn_list
            if "default" in a.get("type", "").lower() and a.get("apn", "").lower() != "ims"
        ]
        ims_apns = [
            a
            for a in apn_list
            if "ims" in a.get("type", "").lower() or a.get("apn", "").lower() == "ims"
        ]
        if not ims_apns and rep_plmn:
            rep_raw = s_apns.get(rep_plmn, [])
            rep_apns = [a for a in rep_raw if not a.get("mvno_type")] or rep_raw
            ims_apns = [
                a
                for a in rep_apns
                if "ims" in a.get("type", "").lower() or a.get("apn", "").lower() == "ims"
            ]

        ia = inet_apns[0] if inet_apns else {}
        ima = ims_apns[0] if ims_apns else {}

        mno = base_mno
        if not pure_samsung and mccmnc in LIVE_OVERRIDES:
            ovr = LIVE_OVERRIDES[mccmnc]
            mno = ovr.get("mno", mno)
            rat_mode = ovr.get("rat_mode", rat_mode)
        else:
            ovr = {}

        def clean_token(val: str) -> str:
            return (val or "").replace("|", "/").replace(";", ",").replace("~", "-").strip()

        ims_name = clean_token(ovr.get("ims_name") or ima.get("carrier", "") or prof_name or f"{mno} IMS")
        inet_name = clean_token(ovr.get("inet_name") or ia.get("carrier", "") or f"{mno} Internet")

        ims_apn = clean_token(ima.get("apn", "ims").lower() or "ims")
        inet_apn = clean_token(ia.get("apn", "internet") or "internet")
        inet_user = clean_token(ia.get("user", ""))
        inet_pass = clean_token(ia.get("password", ""))
        inet_auth = ia.get("authtype", "")
        if inet_auth not in ("0", "1", "2", "3"):
            inet_auth = "1" if (inet_user or inet_pass) else "0"
        inet_ip = clean_token(ia.get("protocol", "IP") or "IP")
        inet_roam_ip = clean_token(
            ia.get("roaming_protocol", "") or ("IPV4V6" if inet_ip == "IPV6" else inet_ip)
        )

        if ovr:
            inet_auth = ovr.get("inet_auth", inet_auth)
            if "inet_ip" in ovr:
                inet_ip = ovr["inet_ip"]
                inet_roam_ip = ovr.get("inet_roam_ip", inet_ip)

        raw_inet_type = clean_token(ia.get("type", "default,supl").lower() or "default,supl")
        inet_type_parts = [t.strip() for t in raw_inet_type.split(",") if t.strip()]
        if "default" not in inet_type_parts:
            inet_type_parts.insert(0, "default")
        if "supl" not in inet_type_parts:
            inet_type_parts.append("supl")
        inet_type = ",".join(inet_type_parts)

        def make_apn_obj(
            name,
            apn_str,
            apn_type,
            user,
            password,
            authtype,
            proto,
            roam_proto,
            mmsc="",
            mmsproxy="",
            mmsport="",
            proxy="",
            port="",
            mtu="",
        ):
            obj = {
                "name": (name or "").strip(),
                "apn": (apn_str or "").strip(),
                "type": (apn_type or "").strip(),
            }
            if user:
                obj["user"] = user.strip()
            if password:
                obj["password"] = password.strip()
            try:
                obj["authtype"] = int(str(authtype).strip())
            except ValueError:
                obj["authtype"] = -1 if apn_type == "ims" else 0
            obj["protocol"] = (proto or "IP").strip()
            obj["roaming_protocol"] = (roam_proto or obj["protocol"]).strip()
            if mmsc:
                obj["mmsc"] = mmsc.strip()
            if mmsproxy:
                obj["mmsproxy"] = mmsproxy.strip()
            if mmsport:
                obj["mmsport"] = mmsport.strip()
            if proxy:
                obj["proxy"] = proxy.strip()
            if port:
                obj["port"] = port.strip()
            if mtu and str(mtu).strip().isdigit() and int(str(mtu).strip()) > 0:
                obj["mtu"] = int(str(mtu).strip())
            return obj

        apns_list_json = []
        seen_apn_keys = set()

        # 1. Primary Internet APN
        inet_mtu = ia.get("mtu") or ia.get("mtusize") or ""
        apns_list_json.append(
            make_apn_obj(
                inet_name,
                inet_apn,
                inet_type,
                inet_user,
                inet_pass,
                inet_auth,
                inet_ip,
                inet_roam_ip,
                ia.get("mmsc", ""),
                ia.get("mmsproxy", ""),
                ia.get("mmsport", ""),
                ia.get("proxy", ""),
                ia.get("port", ""),
                inet_mtu,
            )
        )
        seen_apn_keys.add((inet_apn.lower(), inet_type.lower()))
        if ia:
            seen_apn_keys.add((ia.get("apn", "").lower(), ia.get("type", "").lower()))

        # 2. IMS APN
        ims_mtu = ima.get("mtu") or ima.get("mtusize") or ""
        apns_list_json.append(
            make_apn_obj(
                ims_name,
                ims_apn,
                "ims",
                ima.get("user", ""),
                ima.get("password", ""),
                -1,
                ims_ip,
                ims_ip,
                "",
                "",
                "",
                "",
                "",
                ims_mtu,
            )
        )
        seen_apn_keys.add((ims_apn.lower(), "ims"))
        if ima:
            seen_apn_keys.add((ima.get("apn", "").lower(), ima.get("type", "").lower()))

        # 3. Additional operator APNs from Samsung apns-conf.xml (MMS, XCAP, DUN, etc.)
        for extra in apn_list:
            ex_apn = extra.get("apn", "").strip()
            ex_type = extra.get("type", "").strip().lower()
            if not ex_apn and ex_type != "ia":
                continue
            if "ims" in ex_type.split(",") or ex_apn.lower() == "ims":
                continue
            key = (ex_apn.lower(), ex_type)
            if key in seen_apn_keys:
                continue
            seen_apn_keys.add(key)

            ex_name = extra.get("carrier", "").strip() or f"{mno} {ex_type.upper()}"
            ex_user = extra.get("user", "")
            ex_pass = extra.get("password", "")
            ex_auth = extra.get("authtype", "")
            if ex_auth not in ("-1", "0", "1", "2", "3"):
                ex_auth = "1" if (ex_user or ex_pass) else "0"
            ex_proto = extra.get("protocol", "IP") or "IP"
            ex_roam = extra.get("roaming_protocol", "") or (
                "IPV4V6" if ex_proto == "IPV6" else ex_proto
            )
            ex_mtu = extra.get("mtu") or extra.get("mtusize") or ""

            apns_list_json.append(
                make_apn_obj(
                    ex_name,
                    ex_apn,
                    ex_type,
                    ex_user,
                    ex_pass,
                    ex_auth,
                    ex_proto,
                    ex_roam,
                    extra.get("mmsc", ""),
                    extra.get("mmsproxy", ""),
                    extra.get("mmsport", ""),
                    extra.get("proxy", ""),
                    extra.get("port", ""),
                    ex_mtu,
                )
            )

        px = get_pixel_status(mccmnc)
        reasons = []
        if not px["ims_apn"]:
            reasons.append("NO_IMS_APN")
        if s_volte and not px["volte"]:
            reasons.append("VOLTE_OFF_IN_PIXEL")
        if s_vowifi and (not px["wfc"] or not px["ims_iwlan"]):
            reasons.append("VOWIFI_MISSING_IN_PIXEL")

        included = len(reasons) > 0
        ims_ua = resolve_operator_ims_ua(mccmnc, base_mno, exact_ims_p, s_model, s_build, s_os)

        audit_rows.append(
            {
                "mccmnc": mccmnc,
                "mno": mno,
                "s_volte": s_volte,
                "s_vowifi": s_vowifi,
                "rat_mode": rat_mode,
                "ims_ua": ims_ua,
                "px_source": px["source"],
                "px_ims": px["ims_apn"],
                "px_bearers": px["ims_bearers"],
                "included": included,
                "reasons": reasons,
            }
        )

        if included:
            record = {
                "mno_name": mno,
                "rat_mode": rat_mode,
            }
            if ims_ua:
                record["ims_ua"] = ims_ua
            record["apns"] = apns_list_json
            db_rows.append((mccmnc, record))

    audit_rows.sort(key=lambda r: r["mccmnc"])
    db_rows.sort(key=lambda r: r[0])
    db_dict = {k: v for k, v in db_rows}
    return audit_rows, db_dict


def write_carrier_db(db_dict: dict, output_path: str):
    """Write generated JSON database to output_path."""
    os.makedirs(os.path.dirname(os.path.abspath(output_path)), exist_ok=True)
    with open(output_path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(db_dict, f, ensure_ascii=False, indent=2)
        f.write("\n")
    ua_count = sum(1 for v in db_dict.values() if "ims_ua" in v)
    print(f"Updated {output_path} ({len(db_dict)} carriers, {ua_count} custom IMS User-Agents)")


def main():
    ap = argparse.ArgumentParser(
        description="Generate res/raw/carrier_db.json by cross-referencing Samsung One UI and Google Pixel CarrierSettings.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    ap.add_argument(
        "--samsung-dir",
        default=DEFAULT_SAMSUNG_DIR,
        help="Directory containing mnomap.json, imsswitch.json, imsprofile.json, apns-conf.xml, and optional build.prop (default: ./firmware/samsung)",
    )
    ap.add_argument(
        "--pixel-dir",
        default=DEFAULT_PIXEL_DIR,
        help="Directory containing Google Pixel CarrierSettings carrier_list.pb, others.pb, and *.pb (default: ./firmware/pixel/CarrierSettings)",
    )
    ap.add_argument(
        "--output",
        default=DEFAULT_OUTPUT_PATH,
        help="Output JSON path when --update is passed (default: ./res/raw/carrier_db.json)",
    )
    ap.add_argument(
        "--mccs",
        type=str,
        default="",
        help="Optional comma-separated list of 3-digit MCCs to scan (default: all global MCCs)",
    )
    ap.add_argument(
        "--pure-samsung",
        action="store_true",
        help="Do not apply live-network verified overrides",
    )
    ap.add_argument(
        "--db-only",
        action="store_true",
        help="Print only the generated JSON to stdout",
    )
    ap.add_argument(
        "--update",
        action="store_true",
        help="Write generated database to --output (default: ./res/raw/carrier_db.json)",
    )
    args = ap.parse_args()

    target_mccs = tuple(x.strip() for x in args.mccs.split(",") if x.strip()) if args.mccs else None
    audit_rows, db_dict = build_carrier_db(
        args.samsung_dir,
        args.pixel_dir,
        target_mccs,
        pure_samsung=args.pure_samsung,
    )

    if args.db_only:
        print(json.dumps(db_dict, ensure_ascii=False, indent=2))
        return

    print("=" * 118)
    print(
        f"{'MCCMNC':6s} | {'Samsung MNO':16s} | {'Samsung IMS':18s} | {'Pixel CarrierSettings':28s} | {'Decision & Reason'}"
    )
    print("-" * 118)
    for r in audit_rows:
        s_str = f"VoLTE={'Y' if r['s_volte'] else 'N'} VoWiFi={'Y' if r['s_vowifi'] else 'N'}"
        p_str = f"{r['px_source']} (ims={'Y' if r['px_ims'] else 'N'},{r['px_bearers'] or '-'})"
        dec = f"INCLUDE {r['reasons']}" if r["included"] else "SKIP (Already complete in Pixel)"
        print(f"{r['mccmnc']:6s} | {r['mno']:16s} | {s_str:18s} | {p_str:28s} | {dec}")
    print("=" * 118)
    print(
        f"Total audited: {len(audit_rows)} | Included (problematic in Pixel): {len(db_dict)} | "
        f"Skipped (already complete in Pixel): {len(audit_rows) - len(db_dict)}\n"
    )

    if args.update:
        write_carrier_db(db_dict, args.output)


if __name__ == "__main__":
    main()
