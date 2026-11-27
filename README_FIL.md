# Pixel IMS & APN Tuner

**Language / Мова / Язык / Idioma / 语言:** [English](./README.md) | [Беларуская](./README_BY.md) | [Українська](./README_UA.md) | [Bahasa Indonesia](./README_ID.md) | [Bahasa Melayu](./README_MS.md) | [Deutsch](./README_DE.md) | [Español](./README_ES.md) | [**Filipino**](./README_FIL.md) | [Français](./README_FR.md) | [Português](./README_PT.md) | [日本語](./README_JA.md) | [简体中文](./README_CN.md) | [繁體中文](./README_TW.md) | [한국어](./README_KO.md) | [Русский](./README_RU.md)

**I-enable ang VoLTE, VoWiFi, VoNR (5G na tawag), 5G SA, at awtomatikong APN profile ng carrier sa Google Pixel 6–11 — walang Root, walang PC, at walang hiwalay na Shizuku app.**

📦 I-download: **[Releases → pixel-ims-apn-tuner.apk](https://github.com/4plashy-disprized/pixel-ims-apn-tuner/releases/latest)**

---

### Anong problema ang nilulutas ng app na ito?

Sa Google Pixel, naka-enable lang nang default ang VoLTE, Wi‑Fi Calling (VoWiFi), at 5G para sa mga carrier na nasa opisyal na listahan ng Google. Kapag gumamit ka ng SIM mula sa Pilipinas (Smart, Globe, DITO, GOMO, TNT, TM) o iba pang hindi suportadong bansa, madalas mong mararanasan ang mga ito:

- **Nawawala ang mobile data habang may kausap sa tawag**, at bumabagsak sa **3G/2G** ang signal (o hindi makatawag kung patay na ang 3G).
- **Walang lumalabas na switch para sa VoLTE, Wi‑Fi Calling, at 5G** sa settings ng SIM.
- Kahit gumamit ng simpleng patcher, **nawawala nang 30 minuto ang VoLTE matapos sumakay sa elevator o basement** (`invalid P-CSCF` lockout kapag panandaliang bumagsak sa 3G), o ayaw gumana ng call forwarding, MMS, at VoWiFi dahil kulang sa tamang `ims` / `xcap` / `mms` APN.
- Tinitingnan din ng ilang carrier ang `User-Agent` ng telepono at **tinatanggihan ang hindi sertipikadong Google Pixel**, kahit buong compatible naman ang Samsung Shannon modem nito.

Nilulutas ng **Pixel IMS & APN Tuner** ang lahat ng ito sa iisang magaan na app (~800 KB):
1. **May built-in na `CARRIER_DB` para sa 461 carrier** (mula sa firmware ng **Samsung Galaxy S26** na may Shannon modem): awtomatikong naglalagay ng permanenteng **Internet, IMS, MMS, at XCAP APN** na may proteksyon laban sa 3G lockout.
2. **Binubuksan ang VoLTE, VoWiFi, VoNR, 5G SA, ViLTE, at Backup calling**, at maaaring gumamit ng **Samsung S26 Shannon IMS User-Agent** (kasama ang 74 carrier-specific profile).
3. **Walang Root, walang PC, at walang Shizuku** — tuloy-tuloy na gagana nang normal ang GCash, Maya, banking apps, Google Wallet, at Android OTA updates.

---

### Mabilisang Simula (2 minuto)

#### Hakbang 1. Isang beses na pag-pair

1. **Kumonekta sa anumang Wi‑Fi network** (bahay, opisina, o hotspot mula sa ibang cellphone).
   > ⚠️ Kailangan lang ang Wi‑Fi habang nagse-setup; pagkatapos mailapat, tuloy-tuloy nang gagana ang VoLTE at 5G sa mobile data kahit walang Wi‑Fi.
2. **Buksan ang «Developer options»** *(kung hindi pa naka-on)*:
   Pumunta sa **Settings → About phone**, mag-scroll sa pinakababa, at **pindutin nang 7 beses ang «Build number»** hanggang lumabas ang *"You are now a developer!"*.
3. **I-on ang «Wireless debugging»**:
   Pumunta sa **Settings → System → Developer options → Wireless debugging**, i-on ito, at i-check ang *"Always allow on this network"*.
4. **I-pair ang app**:
   - Buksan ang **Pixel IMS & APN Tuner** at payagan ang Phone at Notification permissions.
   - Pindutin ang **«Simulan ang pag-pair»** — maglalabas ito ng notification at bubuksan ang Wireless debugging.
   - Pindutin ang **«Pair device with pairing code»** — may lalabas na **6-digit code**.
   - **Huwag isara ang dialog na iyon**, hilahin pababa ang notification bar, pindutin ang notification ng app, i-type ang 6 na numero, at ipadala.
5. Bumalik sa app — makikita nang **Handa na (Ready)** ang ADB card. Hindi mo na kailangang ulitin ang pag-pair!

#### Hakbang 2. Ilapat ang settings para sa iyong SIM

1. Sa card ng iyong SIM, **naka-check na nang kusa** ang mga inirerekomendang opsyon para sa iyong network.
2. Pindutin ang **«Ilapat ang mga setting»** (o **«Ilapat sa lahat ng SIM»** kung dalawa ang SIM mo).
3. Makalipas ang ilang segundo, magiging **«IMS: nakarehistro»** ang status kasama ang listahan ng mga aktibong feature.
4. **Kung in-enable mo ang VoWiFi:** pindutin ang **«Buksan ang mga setting ng SIM»** → buksan ang **Wi‑Fi calling** at siguraduhing naka-ON ang switch.

---

### Kumpletong Gabay sa Lahat ng Opsyon at Button

#### Mga indicator sa SIM card

- **`CARRIER_DB: ...`** — nahanap ang iyong SIM sa database ng 461 carrier (`LTE_WIFI` = may VoLTE at VoWiFi; `LTE_ONLY` = VoLTE lang).
- **`IMS: nakarehistro` / `hindi nakarehistro`** — kumpirmasyon na nakakonekta na ang iyong Pixel sa voice server ng carrier para sa 4G/5G/Wi‑Fi calls.
- **`Aktibo sa system: ...`** — ipinapakita ang mga aktwal na flag na gumagana sa Android ngayon.
- **Naka-grey na checkbox na may `(system profile)`** — pinoprotektahan ang mga feature na gumagana na mula sa pabrika habang hinahayaan kang i-on ang mga kulang.

#### Pangunahing mga opsyon sa SIM

| Opsyon | Ano ang ginagawa sa simpleng paliwanag |
| :--- | :--- |
| **VoLTE (4G na tawag)** | Malinaw na HD calls sa 4G LTE nang hindi bumabagsak sa 3G/2G at hindi napuputol ang internet habang tumatawag. **Inirerekomendang laging naka-ON.** |
| **VoWiFi (Wi‑Fi Calling + icon sa status bar)** | Nakakatawag at nakakatanggap ng regular na tawag at text sa anumang Wi‑Fi kahit walang signal sa loob ng bahay, basement, o nasa abroad. *I-on kung suportado ng carrier mo ang Wi‑Fi Calling.* |
| **VoNR (5G na tawag)** | Pinananatili ang tawag direkta sa 5G nang hindi bumabalik sa 4G. |
| **5G SA + mga threshold ng NR signal** | Ine-enable sa Android ang **5G NSA at 5G SA** (`[1, 2]`), pinipigilan ang IMS stack na patayin ang 5G SA kapag walang VoNR (`ims.sa_disable_policy_int = 0`), at itinutugma ang **5G signal bars (`SS-RSRP`)** sa status bar sa karaniwang 4G LTE scale (`[-128, -118, -108, -98]` dBm sa halip na `[-110, -90, -80, -65]` dBm kung saan 0 bar agad sa `-111` dBm). *Hindi binabago ang pisikal na lakas ng sagap ng modem.* |
| **Mga profile ng APN (Internet, IMS, MMS, XCAP) mula sa CARRIER_DB** | Awtomatikong isinusulat ang napatunayang Samsung S26 APN (`Internet`, `MMS`, `IMS` na ligtas sa 30-minutong 3G lockout, at `XCAP`). **Permanenteng naka-save kahit i-restart ang telepono.** |

#### Bahaging «Advanced ▸»

| Advanced na Opsyon | Gamit |
| :--- | :--- |
| **Samsung S26 (Shannon) IMS User-Agent** | Ipinapakilala ang modem sa network bilang sertipikadong **Samsung Galaxy S26** (kasama ang 74 carrier-specific profile). Inaayos ang pagtanggi ng network sa hindi sertipikadong Pixel. |
| **ViLTE (4G/5G video calling)** | Binubuksan ang built-in na video call direkta sa Phone dialer app gamit ang mobile network. |
| **VoWiFi habang naka-roaming** | Pinapayagang gumana ang Wi‑Fi Calling kapag nasa ibang bansa. |
| **Mga karagdagang serbisyo (UT / XCAP)** | Para ma-setup ang Call Forwarding at Call Waiting direkta sa 4G/Wi‑Fi nang hindi bumababa sa 2G/3G. |
| **Cross-SIM / Backup calling** | Sa Dual-SIM, hinahayaan ang unang SIM na tumawag at makatanggap ng SMS **gamit ang mobile data ng pangalawang SIM** kapag nawalan ng signal ang unang SIM. |
| **Ipakita ang LTE+ / 4G+ icon sa anumang aggregation** | Tinatanggal ang nakatagong bandwidth limit ng Pixel para laging lumabas ang `LTE+` / `4G+` kapag may Carrier Aggregation. |
| **Payagan ang pagdaragdag ng APN sa mga setting** | Binubuksan ang manu-manong pagdagdag at pag-edit ng APN sa Android settings. |

#### Mga button at «Options»

- **«Ilapat ang mga setting»** — sine-save at ina-activate ang mga piniling opsyon para sa SIM.
- **«I-reset»** — binubura ang mga pagbabago at ibinabalik ang factory APN.
- **«Buksan ang mga setting ng SIM»** — direktang binubuksan ang Android settings ng SIM na iyon.
- **«Ilapat sa lahat ng SIM»** *(kapag 2 SIM ang aktibo)* — sine-setup ang parehong SIM sa isang pindot.
- **«Ilapat muli pagkatapos mag-boot»** *(inirerekomenda sa Android 16 QPR2+ at Android 17)* — permanente na ang **basic VoLTE at mga APN**; hinihintay ng opsyong ito ang Wi‑Fi matapos mag-restart, kusang binubuhay ang Wireless debugging kung namatay ito, at tahimik na ibinabalik ang mga `CarrierConfig` feature (**VoWiFi**, **5G / VoNR**, **S26 User-Agent**, **Cross-SIM**).

---

### Madalas Itanong at Solusyon (FAQ)

<details>
<summary><b>1. Napindot ko na ang «Ilapat ang mga setting», pero «IMS: hindi nakarehistro» pa rin. Ano ang titingnan?</b></summary>

1. Siguraduhing **naka-activate ang VoLTE sa mismong account/SIM mo sa iyong carrier** (sa ilang SIM gaya ng lumang Smart/Globe prepaid, kailangang ipapa-activate muna sa carrier app o hotline).
2. Tiyaking nasa **4G (LTE) o 5G** ang signal ng telepono.
3. Kung walang VoWiFi ang carrier mo, alisin ang check sa *VoWiFi* at pindutin ulit ang *Ilapat ang mga setting*.
4. I-on ang **Airplane mode** nang 5–10 segundo at i-off ulit.
</details>

<details>
<summary><b>2. Nawawala ba ang settings pagka-restart ng cellphone (kahit nasa labas at walang Wi‑Fi)?</b></summary>

- **HINDI nawawala ang basic VoLTE (4G calls) at lahat ng APN (`Internet`, `IMS`, `MMS`, `XCAP`) pagka-restart** — kahit sa Android 16 QPR2 / Android 17 at kahit walang Wi‑Fi, dahil permanente itong nakasulat sa `telephony.db`.
- **Sa Android 12–15 at Android 16 bago ang QPR2**, lahat ng settings ay permanente (`[persistent]`).
- **Sa Android 16 QPR2+ at Android 17**, tanging mga `CarrierConfig`-only feature (**VoWiFi**, **5G / VoNR**, **S26 User-Agent**, **Cross-SIM**, **ViLTE**, **LTE+ icon**) lang ang nare-reset pagka-off ng telepono at kusang bumabalik sa loob ng ilang segundo pagkakonekta sa kilalang Wi‑Fi kapag naka-check ang *«Ilapat muli pagkatapos mag-boot»*.
</details>

<details>
<summary><b>3. «Not connected» ang nakalagay sa ADB card kahit nag-pair na ako dati. Kailangan bang mag-pair ulit?</b></summary>

**Hindi na!** Kumonekta lang sa pinagkakatiwalaang Wi‑Fi at pindutin ang **«Suriin muli»** — kusang bubuksan ulit ng app ang Wireless debugging kung pinatay ito ng Android pagkatapos mag-reboot.
</details>

<details>
<summary><b>4. «IMS: nakarehistro» na at gumagana ang VoLTE, pero ayaw gumana ng Wi‑Fi Calling (VoWiFi).</b></summary>

Pindutin ang **«Buksan ang mga setting ng SIM»** → **Wi‑Fi calling** → i-ON ang switch at piliin ang **«Call over Wi‑Fi»**. Para subukan, i-on ang Airplane mode, buksan ang Wi‑Fi, at sumubok tumawag.
</details>

<details>
<summary><b>5. Ligtas ba ito para sa GCash, Maya, banking apps, Google Wallet, at OTA updates?</b></summary>

**100% ligtas.** Walang Root, walang bootloader unlock, at walang koneksyon sa internet (lahat ay lokal sa `127.0.0.1` sa loob mismo ng telepono). Normal na gagana lahat ng bangko, e-wallet, at system updates.
</details>

<details>
<summary><b>6. Paano ibabalik sa dating factory settings ang lahat?</b></summary>

Pindutin ang **«I-reset»** sa card ng SIM habang Ready ang ADB, o i-uninstall ang app, i-restart ang telepono, at piliin ang *Reset to default* sa APN settings ng Android.
</details>

---

### Paano Nilulutas ang 30-Minutong Lockout Pagkabagsak sa 3G Gamit ang `ims` APN Bitmasks

Kung walang nakalaang `ims` APN (o manu-manong ginawa na may bitmask `0` = lahat ng network kasama ang 2G/3G), kapag panandaliang bumagsak sa **3G** (elevator, basement), sinusubukan ng Android na panatilihin ang `IMS PDN` sa 3G, nakakatanggap ng bakanteng `P-CSCF = []`, at pinapagana ng `ShannonImsService` ang `BLOCK_PDN_BY_INVALID_PCSCF = 1` na nagla-lock sa VoLTE at VoWiFi nang **30–90 minuto**. Isinusulat ng `ApnProvisioner` ang `ims` APN na may bitmask na nag-aalis ng 2G/3G (`network_type_bitmask` at `lingering_network_type_bitmask` = `659456` para sa `LTE|IWLAN|NR` o `528384` para sa `LTE|NR`, `bearer_bitmask` = `663552` o `532480`), kaya maayos itong humihinto sa 3G at agad kumokonekta ulit sa loob ng 1–2 segundo pagbalik ng 4G/5G.

---

### Pag-build mula sa Source Code

```sh
./build.sh    # gagawa ng pixel-ims-apn-tuner.apk
```

#### Pag-update ng `CARRIER_DB` (`generate-carrier-db.py`)

Kasama na ang `res/raw/carrier_db.json` sa repository. Kung nais mo itong i-regenerate gamit ang [`generate-carrier-db.py`](./generate-carrier-db.py) (pure Python 3):

<details>
<summary><b>Mga kailangang file at saan makukuha</b></summary>

1. **Google Pixel** (`./firmware/pixel/CarrierSettings/`): `carrier_list.pb`, `others.pb`, at `*.pb` — kunin via ADB nang walang Root mula sa Pixel (`adb pull /product/etc/CarrierSettings firmware/pixel/CarrierSettings`) o i-extract ang `product.img` mula sa opisyal na Pixel Factory Image ([developers.google.com/android/images](https://developers.google.com/android/images)).
2. **Samsung One UI na may Exynos/Shannon modem** (`./firmware/samsung/`): `apns-conf.xml`, `build.prop` (opsyonal), at `mnomap.json`, `imsswitch.json`, `imsprofile.json` (mula sa `res/raw/` ng `/system/priv-app/imsservice/imsservice.apk`) — via `adb pull` nang walang Root mula sa Exynos Galaxy (S24/S25/S26 `B` model) o mula sa opisyal na Samsung firmware (`SM-S942B` gamit ang `Bifrost` / `samloader`).
3. **Patakbuhin:** `./generate-carrier-db.py --update && ./build.sh` *(nasa `.gitignore` ang `./firmware/` — huwag i-commit sa Git ang firmware files).*
</details>

---

### Pasasalamat at mga Pinagkunan ng Ideya

**Orihinal na ideya ng proyektong ito:** ang konsepto ng awtomatikong paglalagay ng permanenteng **APN profiles para sa IMS** (`Internet`, `IMS` na may proteksyon laban sa `invalid P-CSCF` 3G lockout, `MMS`, `XCAP`) at ang pagbuo ng `CARRIER_DB` sa pamamagitan ng pag-cross-reference ng carrier database ng Google Pixel at ng Samsung Galaxy S26 firmware (na parehong gumagamit ng Shannon modem family) ay **sarili naming orihinal na ideya at implementasyon**.

Para sa local ADB channel at `CarrierConfig` mechanics, humango rin ang proyektong ito ng mga ideya mula sa open-source community:

- **[charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle)** — orihinal na standalone loopback Wireless debugging + `UiAutomation` architecture (walang Shizuku) at `build.sh` nang walang Gradle.
- **[kyujin-cho/pixel-volte-patch](https://github.com/kyujin-cho/pixel-volte-patch)** — nanguna sa Pixel `CarrierConfig` patching, `ims.ims_user_agent_string` override concept, ligtas na `UiAutomation` Accessibility flags, at Dual-SIM IMS handling.
- **[vvb2060/Ims](https://github.com/vvb2060/Ims)** — privileged `Instrumentation` (`--no-restart`), Android 14–17 MMTEL/Entitlement provisioning bypass keys, at auto-grant ng permissions.
- **[TakaiSaisei/pixel_ims](https://github.com/TakaiSaisei/pixel_ims)** — awtomatikong pagbuhay sa Wireless debugging (`WRITE_SECURE_SETTINGS` + `adb_wifi_enabled`), Wi‑Fi-bound `JobScheduler` pagka-boot, detached mode sa Android 12–13, at live `ICarrierConfigLoader` check.
- **[VinujaHerath/PixelIMS](https://github.com/VinujaHerath/PixelIMS)** — `--no-hidden-api-checks` flag, ViLTE option, shortcut sa SIM settings, at sabayang pag-apply sa lahat ng SIM.
- **[flyfishxu/kadb](https://github.com/flyfishxu/kadb)** & **[LSPosed/HiddenApiBypass](https://github.com/LSPosed/HiddenApiBypass)** — built-in ADB-over-TLS / SPAKE2 client at hidden Android API access.

---

## Lisensya

[MIT](./LICENSE) — hango sa [charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle) (© charlie-xing).
