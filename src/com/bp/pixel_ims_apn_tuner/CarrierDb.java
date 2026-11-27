package com.bp.pixel_ims_apn_tuner;

import android.content.Context;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Loads the Samsung One UI 9.0 vs Google Pixel CarrierSettings problematic-operator
 * JSON database bundled in res/raw/carrier_db.json (461 PLMNs across 154 countries).
 */
public final class CarrierDb {

    // Mobile Internet (2G / 3G / 4G LTE / 5G NR):
    public static final int INET_BEARER_BITMASK  = 550663;
    public static final int INET_NETTYPE_BITMASK = 545671;

    // IMS bitmasks (excludes 2G/3G to prevent 30-min invalid P-CSCF lockouts):
    public static final int IMS_LTE_WIFI_NETMASK     = 659456; // LTE|IWLAN|LTE_CA|NR
    public static final int IMS_LTE_WIFI_BEARERMASK  = 663552;
    public static final int IMS_LTE_ONLY_NETMASK     = 528384; // LTE|LTE_CA|NR
    public static final int IMS_LTE_ONLY_BEARERMASK  = 532480;
    public static final int IMS_WIFI_ONLY_NETMASK    = 131072; // IWLAN only
    public static final int IMS_WIFI_ONLY_BEARERMASK = 131072;

    // Default VoLTE/VoWiFi SIP User-Agent from Samsung Galaxy S26 (SM-S942B, Exynos 2600 + Shannon 5165, PDA S942BXXS6BZK1)
    public static final String SAMSUNG_S26_IMS_UA = "SM-S942B-BZK1 Samsung IMS 6.0";

    public static final class ApnSpec {
        public final String name;
        public final String apn;
        public final String type;
        public final String user;
        public final String password;
        public final int authtype;
        public final String protocol;
        public final String roamingProtocol;
        public final String mmsc;
        public final String mmsproxy;
        public final String mmsport;
        public final String proxy;
        public final String port;
        public final int mtu;

        ApnSpec(String name, String apn, String type, String user, String password,
                int authtype, String protocol, String roamingProtocol,
                String mmsc, String mmsproxy, String mmsport,
                String proxy, String port, int mtu) {
            this.name = name == null ? "" : name;
            this.apn = apn == null ? "" : apn;
            this.type = (type == null || type.isEmpty()) ? "default,supl" : type;
            this.user = user == null ? "" : user;
            this.password = password == null ? "" : password;
            this.authtype = authtype;
            this.protocol = (protocol == null || protocol.isEmpty()) ? "IP" : protocol;
            this.roamingProtocol = (roamingProtocol == null || roamingProtocol.isEmpty())
                    ? ("IPV6".equals(this.protocol) ? "IPV4V6" : this.protocol)
                    : roamingProtocol;
            this.mmsc = mmsc == null ? "" : mmsc;
            this.mmsproxy = mmsproxy == null ? "" : mmsproxy;
            this.mmsport = mmsport == null ? "" : mmsport;
            this.proxy = proxy == null ? "" : proxy;
            this.port = port == null ? "" : port;
            this.mtu = mtu;
        }

        public boolean isIms() {
            return hasType("ims") || "ims".equalsIgnoreCase(apn);
        }

        public boolean isDefault() {
            return hasType("default") && !isIms();
        }

        public boolean hasType(String target) {
            if (target == null || type == null) return false;
            for (String t : type.split(",")) {
                if (target.equalsIgnoreCase(t.trim())) return true;
            }
            return false;
        }
    }

    public static final class Entry {
        public final String numeric;
        public final String mcc;
        public final String mnc;
        public final String mnoName;
        public final String imsName;
        public final String imsApn;
        public final String imsIp;
        public final String ratMode;
        public final String inetName;
        public final String inetApn;
        public final String inetUser;
        public final String inetPass;
        public final int inetAuth;
        public final String inetIp;
        public final String imsUserAgent;
        public final List<ApnSpec> apns;

        Entry(String numeric, String mnoName, String imsName, String imsApn,
              String imsIp, String ratMode, String inetName, String inetApn,
              String inetUser, String inetPass, int inetAuth, String inetIp,
              String imsUserAgent, List<ApnSpec> apns) {
            this.numeric = numeric;
            this.mcc = numeric.length() >= 3 ? numeric.substring(0, 3) : "";
            this.mnc = numeric.length() > 3 ? numeric.substring(3) : "";
            this.mnoName = mnoName;
            this.imsName = (imsName == null || imsName.isEmpty()) ? (mnoName + " IMS") : imsName;
            this.imsApn = (imsApn == null || imsApn.isEmpty()) ? "ims" : imsApn;
            this.imsIp = (imsIp == null || imsIp.isEmpty()) ? "IPV4V6" : imsIp;
            this.ratMode = (ratMode == null || ratMode.isEmpty()) ? "LTE_ONLY" : ratMode;
            this.inetName = (inetName == null || inetName.isEmpty()) ? (mnoName + " Internet") : inetName;
            this.inetApn = (inetApn == null || inetApn.isEmpty()) ? "internet" : inetApn;
            this.inetUser = inetUser == null ? "" : inetUser;
            this.inetPass = inetPass == null ? "" : inetPass;
            this.inetAuth = inetAuth;
            this.inetIp = (inetIp == null || inetIp.isEmpty()) ? "IP" : inetIp;
            this.imsUserAgent = (imsUserAgent == null || imsUserAgent.isEmpty())
                    ? SAMSUNG_S26_IMS_UA : imsUserAgent;
            this.apns = Collections.unmodifiableList(apns);
        }

        public int imsNetmask() {
            if ("LTE_WIFI".equals(ratMode)) return IMS_LTE_WIFI_NETMASK;
            if ("WIFI_ONLY".equals(ratMode)) return IMS_WIFI_ONLY_NETMASK;
            return IMS_LTE_ONLY_NETMASK;
        }

        public int imsBearermask() {
            if ("LTE_WIFI".equals(ratMode)) return IMS_LTE_WIFI_BEARERMASK;
            if ("WIFI_ONLY".equals(ratMode)) return IMS_WIFI_ONLY_BEARERMASK;
            return IMS_LTE_ONLY_BEARERMASK;
        }

        public String ratDescription() {
            if ("LTE_WIFI".equals(ratMode)) return "LTE|IWLAN|LTE_CA|NR";
            if ("WIFI_ONLY".equals(ratMode)) return "IWLAN";
            return "LTE|LTE_CA|NR";
        }

        public boolean supportsWifiCalling() {
            return !"LTE_ONLY".equals(ratMode);
        }

        public String remainingApnNames() {
            if (apns == null || apns.size() <= 1) return imsName;
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < apns.size(); i++) {
                if (sb.length() > 0) sb.append(" / ");
                sb.append(apns.get(i).name);
            }
            return sb.toString();
        }
    }

    private static volatile Map<String, Entry> cache;

    private static Map<String, Entry> load(Context ctx) {
        Map<String, Entry> existing = cache;
        if (existing != null) return existing;

        Map<String, Entry> map = new HashMap<>(512);
        try (InputStream is = ctx.getResources().openRawResource(R.raw.carrier_db);
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder(65536);
            char[] buf = new char[8192];
            int n;
            while ((n = br.read(buf)) > 0) {
                sb.append(buf, 0, n);
            }
            JSONObject root = new JSONObject(sb.toString());
            Iterator<String> keys = root.keys();
            while (keys.hasNext()) {
                String numeric = keys.next().trim();
                if (numeric.length() < 5) continue;
                JSONObject obj = root.optJSONObject(numeric);
                if (obj == null) continue;

                String mnoName = obj.optString("mno_name", "").trim();
                String ratMode = obj.optString("rat_mode", "LTE_ONLY").trim();
                JSONArray apnsArr = obj.optJSONArray("apns");

                List<ApnSpec> specs = new ArrayList<>();
                ApnSpec inetSpec = null;
                ApnSpec imsSpec = null;

                if (apnsArr != null) {
                    for (int i = 0; i < apnsArr.length(); i++) {
                        JSONObject a = apnsArr.optJSONObject(i);
                        if (a == null) continue;
                        String type = a.optString("type", "default,supl").trim();
                        int defaultAuth = "ims".equalsIgnoreCase(type) ? -1 : 0;
                        String proto = a.optString("protocol", "IP").trim();
                        String roamProto = a.optString("roaming_protocol", "").trim();
                        ApnSpec spec = new ApnSpec(
                                a.optString("name", "").trim(),
                                a.optString("apn", "").trim(),
                                type,
                                a.optString("user", "").trim(),
                                a.optString("password", "").trim(),
                                a.optInt("authtype", defaultAuth),
                                proto,
                                roamProto,
                                a.optString("mmsc", "").trim(),
                                a.optString("mmsproxy", "").trim(),
                                a.optString("mmsport", "").trim(),
                                a.optString("proxy", "").trim(),
                                a.optString("port", "").trim(),
                                a.optInt("mtu", 0)
                        );
                        specs.add(spec);
                        if (imsSpec == null && spec.isIms()) {
                            imsSpec = spec;
                        } else if (inetSpec == null && spec.isDefault()) {
                            inetSpec = spec;
                        }
                    }
                }

                String inetName = inetSpec != null ? inetSpec.name : (mnoName + " Internet");
                String inetApn  = inetSpec != null ? inetSpec.apn : "internet";
                String inetUser = inetSpec != null ? inetSpec.user : "";
                String inetPass = inetSpec != null ? inetSpec.password : "";
                int inetAuth    = inetSpec != null ? inetSpec.authtype : 0;
                String inetIp   = inetSpec != null ? inetSpec.protocol : "IP";

                String imsName  = imsSpec != null ? imsSpec.name : (mnoName + " IMS");
                String imsApn   = imsSpec != null ? imsSpec.apn : "ims";
                String imsIp    = imsSpec != null ? imsSpec.protocol : "IPV4V6";
                String imsUa    = obj.optString("ims_ua", SAMSUNG_S26_IMS_UA).trim();

                Entry entry = new Entry(
                        numeric, mnoName, imsName, imsApn, imsIp, ratMode,
                        inetName, inetApn, inetUser, inetPass, inetAuth, inetIp, imsUa, specs);
                if (!map.containsKey(numeric)) {
                    map.put(numeric, entry);
                }
            }
        } catch (Throwable t) {
            Log.e(VolteInstrumentation.TAG, "Failed to load CARRIER_DB JSON", t);
        }
        cache = map;
        return map;
    }

    public static String normalizePlmn(String mcc, String mnc) {
        if (mcc == null || mnc == null) return "";
        String c = mcc.trim();
        String n = mnc.trim();
        if (c.isEmpty() || n.isEmpty()) return "";
        if (n.length() == 1) n = "0" + n;
        return c + n;
    }

    /**
     * Resolves PLMN for a subscription. If SubscriptionInfo's MCC+MNC is absent from CARRIER_DB
     * (e.g. multi-IMSI eSIMs like Roamless where SubscriptionInfo reports 20601 while active
     * IMSI / simOperator is 45400), falls back to TelephonyManager.getSimOperator().
     */
    public static String resolvePlmn(Context ctx, int subId, String mcc, String mnc) {
        String primary = normalizePlmn(mcc, mnc);
        if (!primary.isEmpty() && lookup(ctx, primary) != null) {
            return primary;
        }
        try {
            TelephonyManager tm = ctx.getSystemService(TelephonyManager.class);
            if (tm != null && subId >= 0) {
                String simOp = tm.createForSubscriptionId(subId).getSimOperator();
                if (simOp != null) {
                    simOp = simOp.trim();
                    if (simOp.length() >= 5 && (lookup(ctx, simOp) != null || primary.isEmpty())) {
                        return simOp;
                    }
                }
            }
        } catch (Throwable ignored) {}
        return primary;
    }

    public static String resolvePlmn(Context ctx, SubscriptionInfo si) {
        if (si == null) return "";
        return resolvePlmn(ctx, si.getSubscriptionId(), si.getMccString(), si.getMncString());
    }

    public static Entry lookup(Context ctx, String plmn) {
        if (plmn == null || plmn.isEmpty()) return null;
        return load(ctx).get(plmn.trim());
    }

    public static Entry lookup(Context ctx, String mcc, String mnc) {
        return lookup(ctx, normalizePlmn(mcc, mnc));
    }

    public static int count(Context ctx) {
        return load(ctx).size();
    }

    private CarrierDb() {}
}
