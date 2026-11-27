# Pixel IMS & APN Tuner

**Language / Мова / Язык / Idioma / 语言:** [**English**](./README.md) | [Беларуская](./README_BY.md) | [Українська](./README_UA.md) | [Bahasa Indonesia](./README_ID.md) | [Bahasa Melayu](./README_MS.md) | [Deutsch](./README_DE.md) | [Español](./README_ES.md) | [Filipino](./README_FIL.md) | [Français](./README_FR.md) | [Português](./README_PT.md) | [日本語](./README_JA.md) | [简体中文](./README_CN.md) | [繁體中文](./README_TW.md) | [한국어](./README_KO.md) | [Русский](./README_RU.md)

**Enable VoLTE, VoWiFi, VoNR (5G calling), 5G SA, and automatic carrier APN profiles on Google Pixel 6–11 — no Root, no PC, and no external Shizuku app required.**

📦 Download: **[Releases → pixel-ims-apn-tuner.apk](https://github.com/4plashy-disprized/pixel-ims-apn-tuner/releases/latest)**

---

### What problem does this solve?

Google Pixel phones only enable VoLTE, VoWiFi, and 5G out of the box for carriers on Google's certification allowlist. If you use a SIM from an uncertified carrier or region, you typically run into these issues:

- **Calls drop to 3G/2G (or fail completely where 3G is shut down)**, and mobile data stops working during phone calls.
- **VoLTE, Wi‑Fi Calling (VoWiFi), and 5G toggles are completely missing** from SIM settings.
- Even with basic CarrierConfig patchers, **VoLTE breaks for 30 minutes after briefly losing 4G in an elevator or subway** (due to 3G `invalid P-CSCF` lockouts on stock APNs), or MMS, call forwarding, and Wi‑Fi Calling refuse to work because Pixel lacks proper carrier `ims` / `xcap` / `mms` APNs.
- Some carriers inspect the SIP `User-Agent` header and **reject uncertified Google Pixel devices**, even though Pixel 6–11 uses a Samsung Shannon modem fully compatible with their IMS core.

**Pixel IMS & APN Tuner** solves all of this in a single standalone app (~800 KB):
1. **Built-in `CARRIER_DB` for 461 carriers** (sourced from **Samsung Galaxy S26** firmware running the same Shannon modem family): automatically provisions persistent **Internet, IMS, MMS, and XCAP APNs** with proper bearer bitmasks that prevent 3G P-CSCF lockouts.
2. **Unlocks VoLTE, VoWiFi, VoNR, 5G SA, ViLTE, and Backup Calling**, and optionally sets the **Samsung S26 Shannon IMS User-Agent** (including 74 carrier-specific templates).
3. **No Root, no PC, and no external Shizuku app needed** — Google Wallet, banking apps, and OTA updates keep working normally.

---

### Quick Start (2 minutes)

#### Step 1. One-time pairing

1. **Connect to any Wi‑Fi network** (home, office, or mobile hotspot from another phone).
   > ⚠️ Android only activates Wireless debugging while connected to Wi‑Fi. Once settings are applied, VoLTE/5G keep working on mobile data without Wi‑Fi.
2. **Enable Developer options** *(if not enabled yet)*:
   Go to **Settings → About phone**, scroll to the bottom, and tap **Build number** 7 times until you see *"You are now a developer!"*.
3. **Enable Wireless debugging**:
   Go to **Settings → System → Developer options → Wireless debugging**, turn it **ON**, check *"Always allow on this network"*, and tap *Allow*.
4. **Pair the app**:
   - Open **Pixel IMS & APN Tuner** and grant the Phone & Notification permissions.
   - Tap **Start pairing** in the top ADB card — the app posts a pairing notification and opens Wireless debugging.
   - Tap **Pair device with pairing code** — a dialog with a **6-digit code** appears.
   - **Keep that dialog open**, pull down the notification shade, tap **Enter code** on the *Pixel IMS & APN Tuner* notification, type the 6 digits, and send.
5. Return to the app — the ADB card now shows **Ready — you can proceed with configuration**. You never need to pair again.

#### Step 2. Apply settings for your SIM

1. Check your SIM card panel — recommended options are **already pre-selected** for your carrier.
2. Tap **Apply settings** (or **Apply to all SIMs** on Dual-SIM setups).
3. Within a few seconds, the status line changes to **IMS: registered**, followed by **Active in system: VoLTE, …** confirming the live system flags.
4. **If you enabled VoWiFi:** tap **Open SIM settings** right on the SIM card → open **Wi‑Fi calling** → make sure the toggle is turned ON and choose your preferred calling mode.

---

### Complete Guide to All Options & Buttons

#### Status indicators on each SIM card

- **`CARRIER_DB: ...`** — your SIM carrier matched the built-in 461-carrier database (`LTE_WIFI` = carrier supports both VoLTE and VoWiFi; `LTE_ONLY` = carrier supports VoLTE only).
- **`Not in CARRIER_DB (factory Pixel APN kept)`** — carrier is not in the database (or is already stock-certified by Google). Existing APNs are untouched, while all IMS / 5G switches remain available.
- **`IMS: registered` / `not registered`** — live confirmation from the telephony stack. **Registered** means your phone is actively connected to your carrier's voice server for 4G/5G/Wi‑Fi calls.
- **`Active in system: ...`** — reads back the actual effective flags from Android's `CarrierConfigLoader` so you can see exactly what is active.
- **Greyed-out checkboxes marked `(system profile)`** — shown when a capability is already enabled by Google's stock profile for that SIM, protecting working factory features from accidental disabling while letting you enable missing ones.

#### Main SIM options

| Option | What it does & when to use it |
| :--- | :--- |
| **VoLTE (4G calling)** | Enables HD voice calls over 4G LTE without dropping to 3G/2G, fast call setup, and uninterrupted mobile data during calls. **Recommended ON for almost all carriers.** |
| **VoWiFi (Wi‑Fi Calling + status bar icon)** | Lets you make and receive regular phone calls and SMS over any Wi‑Fi network when cellular coverage is weak or absent (indoors, basements, rural areas, or abroad). *Enable if your carrier supports Wi‑Fi Calling (pre-selected automatically for database carriers).* |
| **VoNR (5G calling)** | Keeps voice calls directly on 5G networks without falling back to 4G LTE during a call. |
| **5G SA + NR signal thresholds** | Enables both **5G NSA and 5G SA** in Android (`carrier_nr_availabilities_int_array = [1, 2]`), prevents the IMS stack from disabling 5G SA when VoNR is inactive (`ims.sa_disable_policy_int = 0`), and aligns the **status-bar 5G signal bars (`SS-RSRP`)** with the standard 4G LTE scale (`[-128, -118, -108, -98]` dBm instead of Google's steep `[-110, -90, -80, -65]` dBm where `-111` dBm shows 0 bars). *Does not alter physical RF modem sensitivity.* |
| **APN profiles (Internet, IMS, MMS, XCAP) from CARRIER_DB** | Writes verified Samsung S26 carrier APNs (`Internet`, `MMS`, `IMS` with 4G/5G/IWLAN bearer masks to prevent 30-minute 3G P-CSCF lockouts, and `XCAP` for call settings). **Persists across reboots.** |

#### Collapsible "Advanced ▸" options

| Advanced Option | Purpose |
| :--- | :--- |
| **Samsung S26 (Shannon) IMS User-Agent** | Sets the SIP `User-Agent` header to match a certified **Samsung Galaxy S26** running the Shannon modem (using carrier-specific strings for 74 strict carriers and S26's default string for others). Fixes IMS registration rejections on carriers that block uncertified Pixel models. *Enabled by default for uncertified carriers.* |
| **ViLTE (4G/5G video calling)** | Enables native carrier video calling directly in the stock Phone dialer (when supported by your carrier). |
| **VoWiFi in roaming** | Allows Wi‑Fi Calling to work while roaming abroad and unlocks roaming mode preferences in SIM settings. |
| **Supplementary services (UT / XCAP)** | Enables managing Call Forwarding, Call Waiting, and Call Barring directly over 4G/Wi‑Fi (`XCAP`) without falling back to 2G/3G. |
| **Cross-SIM / Backup calling** | On Dual-SIM phones, lets one SIM place and receive calls/SMS **over the other SIM's mobile data connection** when the first SIM has no cellular signal (e.g., using your home SIM over a travel data eSIM abroad). |
| **Show LTE+ / 4G+ icon on any aggregation** | Removes Pixel's hidden bandwidth threshold so the status bar honestly displays `LTE+` / `4G+` whenever Carrier Aggregation is active. Purely visual — does not alter modem RF behavior. |
| **Allow adding APNs in system settings** | Unlocks manual APN creation and editing in Android's SIM settings if locked by the carrier profile. |

#### Action buttons & global Options

- **Apply settings** — writes the selected settings for that SIM and triggers immediate IMS re-registration.
- **Reset** — clears all overrides for that SIM and restores factory APNs and Google's default profile.
- **Open SIM settings** — jumps straight to Android's system settings screen for that specific SIM.
- **Apply to all SIMs** *(shown when 2 SIMs are active)* — configures all active SIMs in one tap.
- **Re-apply after boot** *(in the bottom Options card)* — **recommended on Android 16 QPR2+ and Android 17**. Basic **VoLTE and all carrier APNs** are stored persistently in `telephony.db` and survive reboots on their own, while session-only `CarrierConfig` features (**VoWiFi**, **5G / VoNR**, **Samsung S26 User-Agent**, **Cross-SIM**, etc.) reset when the phone powers off. With this option checked, the app waits for Wi‑Fi after boot, automatically wakes Wireless debugging if Android turned it off, and silently restores the full `CarrierConfig` bundle in the background.
- **Language** *(top-right button)* — switches the app UI between 15 languages on the fly.

---

### Troubleshooting (FAQ)

<details>
<summary><b>1. Applied settings, but it still says "IMS: not registered". What should I check?</b></summary>

1. **Make sure VoLTE is activated on your plan/account by your carrier.** Many carriers offer VoLTE for free, but keep it disabled by default on the SIM until enabled in their app, via USSD code, or through customer support.
2. **Verify your phone is currently on 4G (LTE) or 5G** and not forced into "3G/2G only" mode (*tap "Open SIM settings" → Preferred network type*).
3. **If your carrier does not support Wi‑Fi Calling** (VoLTE only), uncheck *VoWiFi* and tap *Apply settings* again.
4. Turn **Airplane mode** ON for 5–10 seconds and OFF again to force a fresh network attach.
5. Very old SIM cards (7–10+ years old without USIM/ISIM applets) may physically lack IMS support — replacing the SIM card with your carrier solves it.
</details>

<details>
<summary><b>2. Do settings reset after rebooting the phone (including away from Wi‑Fi)?</b></summary>

- **Basic VoLTE (4G calling) and all carrier APNs (`Internet`, `IMS`, `MMS`, `XCAP`) DO NOT reset after a reboot** — even on Android 16 QPR2 / Android 17 and even without Wi‑Fi! They are written directly into Android's permanent `telephony.db` (`carriers` + `Persistent VoLTE` opt-in), so on the vast majority of carriers 4G calls and mobile data work immediately upon boot.
- **On Android 12–15 and pre-QPR2 Android 16**, nothing resets at all — the entire configuration is stored permanently (`[persistent]` in the log).
- **On Android 16 QPR2+ and Android 17**, only session-only `CarrierConfig` features (**VoWiFi**, **5G / VoNR**, **Samsung S26 IMS User-Agent** for strict carriers that block `Google Pixel`, **Cross-SIM**, **ViLTE**, and the **LTE+ icon**) temporarily reset on power-off until the phone connects to Wi‑Fi.
- With **Re-apply after boot** enabled, the app automatically restores all of those `CarrierConfig` features within seconds as soon as your phone connects to a trusted Wi‑Fi network (or a brief hotspot from another phone while traveling).
</details>

<details>
<summary><b>3. The ADB card says "Not connected", even though I already entered the 6-digit code earlier. Do I need to pair again?</b></summary>

**No — you almost never need to pair again!** Your pairing key is already saved on the phone. Usually it is one of three things:
1. **Wi‑Fi is off**, or you are connected to a brand-new Wi‑Fi network (e.g., hotel or café) where Wireless debugging hasn't been allowed yet. Connect to a trusted Wi‑Fi network (or allow the current network under *Wireless debugging*) and tap **Re-check**.
2. Android sometimes turns the Wireless debugging switch off after a reboot. Tapping **Re-check** while connected to a trusted Wi‑Fi network **automatically wakes Wireless debugging back up**.
3. Re-pairing with a 6-digit code is only needed if you manually tapped *"Forget all devices"* in Wireless debugging or uninstalled and reinstalled the app.
</details>

<details>
<summary><b>4. It says "IMS: registered" and VoLTE works, but Wi‑Fi Calling (VoWiFi) does not activate.</b></summary>

Tap **Open SIM settings** on the SIM card → open **Wi‑Fi calling** → turn the toggle **ON** and choose **Call over Wi‑Fi** as the preference (many profiles default to *Call over mobile network*, which only switches to Wi‑Fi when cellular signal drops near zero). To test VoWiFi right away, turn on **Airplane mode**, turn **Wi‑Fi** back on, and place a test call.
</details>

<details>
<summary><b>5. Is this safe? Will Google Wallet (contactless pay), banking apps, and OTA updates still work?</b></summary>

**Yes, 100% safe.** The app does not unlock the bootloader, does not root the device, does not modify the system partition, and makes zero internet connections (everything runs strictly over local loopback `127.0.0.1` on the phone itself). Google Play Integrity, Google Wallet, banking apps, and official Android OTA updates continue to work normally.

> 💡 *Tip:* after installing a monthly Android system OTA update, open the app once on Wi‑Fi and tap **Apply settings**.
</details>

<details>
<summary><b>6. How do I revert everything back to factory defaults?</b></summary>

Open the app while the ADB channel shows *Ready* and tap **Reset** on your SIM card — this clears all overrides and restores factory APNs. Alternatively, uninstall the app, reboot your phone, and tap *Reset to default* in *Settings → Network & internet → SIMs → Access Point Names (APNs)*.
</details>

---

### How the 3G Fallback Problem (30-Minute Lockout) Is Solved via `ims` APN Bitmasks

1. **Why VoLTE breaks after dropping to 3G on uncertified carriers:**
   - For uncertified carriers, Pixel's stock `CarrierSettings` (`others.pb`) contains **no `type="ims"` APN** (and manually created APNs in Android's UI leave hidden radio bitmasks at `0` = *all networks, including 2G & 3G*).
   - When VoLTE is forced on via `CarrierConfig` alone, Android's `DataProfileManager` synthesizes a fallback `DEFAULT IMS` profile with bitmask `0` (`UNKNOWN`), allowing **2G (`GPRS/EDGE`) and 3G (`UMTS/HSPA+`)**.
   - As soon as the phone briefly drops into **3G** (elevator, subway, underground garage), Android attempts to keep/establish the `IMS PDN` over 3G. Because 3G SGSN/GGSN nodes do not serve IMS voice, the network returns an empty voice proxy list: **`P-CSCF = []` (`invalid P-CSCF address`)**.
   - Pixel's Tensor modem service (`com.shannon.imsservice` / `ShannonImsService`) enforces **`BLOCK_PDN_BY_INVALID_PCSCF = 1`** with a **`1,800,000 ms` (30 minutes of active CPU uptime — often 30–90 minutes with screen off)** penalty timer, locking out both VoLTE and VoWiFi even after returning to strong 4G/5G or Wi‑Fi!

2. **How `ApnProvisioner` fixes it using hidden radio bitmasks:**
   When writing the persistent `ims` APN into `content://telephony/carriers`, the app sets three synchronized bitmask columns (`network_type_bitmask`, `lingering_network_type_bitmask`, and legacy RIL `bearer_bitmask`) that **strictly exclude all 2G and 3G radio types** according to the carrier's `rat_mode` in `CARRIER_DB`:
   - **`LTE_WIFI`** (carrier supports both VoLTE and VoWiFi):
     - `network_type_bitmask = 659456` (`LTE 4096 | IWLAN 131072 | NR 524288`)
     - `bearer_bitmask = 663552` (`RIL_LTE 8192 | IWLAN 131072 | NR 524288`)
   - **`LTE_ONLY`** (carrier supports VoLTE only, no VoWiFi `ePDG`):
     - `network_type_bitmask = 528384` (`LTE 4096 | NR 524288` — also excludes `IWLAN` to prevent battery drain from failed `ePDG` IKEv2 retries)
     - `bearer_bitmask = 532480` (`RIL_LTE 8192 | NR 524288`)
   - **`WIFI_ONLY`**:
     - `network_type_bitmask = 131072` / `bearer_bitmask = 131072` (`IWLAN` only)

   When the phone enters 3G/2G, Android sees that 3G is disallowed on the `ims` profile, cleanly tears down IMS without requesting a 3G `P-CSCF`, and **re-registers VoLTE within 1–2 seconds** as soon as 4G/5G returns.

---

### Technical Overview & Building from Source

- Bundles a pure-Java/Kotlin ADB over TLS + SPAKE2 client (`kadb` + `BouncyCastle`) connecting to `127.0.0.1` Wireless debugging and launching `VolteInstrumentation` (`--no-hidden-api-checks`, detached mode on Android 12–13, `--no-restart` on Android 14+).
- Adopts shell permissions via `UiAutomation` with `FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES | FLAG_DONT_USE_ACCESSIBILITY` (never kills user Accessibility services), self-grants `WRITE_SECURE_SETTINGS` for automatic `adb_wifi_enabled` recovery, provisions `content://telephony/carriers`, updates persistent `siminfo` opt-in columns, overrides `CarrierConfigManager`, and queries live state via `ImsQueryTool` (`ICarrierConfigLoader` + `ImsMmTelManager`).
- Built without Gradle via `./build.sh` (`aapt2 → javac → r8 → zipalign → apksigner`) and `.github/workflows/build.yml`:

```sh
./build.sh    # produces pixel-ims-apn-tuner.apk
```

#### Updating `CARRIER_DB` (`generate-carrier-db.py`)

`res/raw/carrier_db.json` is already pre-built in the repository, so **you do not need any firmware files to build the APK**. If you want to regenerate or audit `carrier_db.json` against newer firmware releases using [`generate-carrier-db.py`](./generate-carrier-db.py) (pure Python 3, no third-party pip packages required):

<details>
<summary><b>Required input files &amp; how to obtain them</b></summary>

1. **Google Pixel files** (`./firmware/pixel/CarrierSettings/`):
   - `carrier_list.pb`, `others.pb`, and all `*.pb` carrier protobufs.
   - **Option A — Pull directly from any Google Pixel via USB ADB (no Root required):**
     ```sh
     mkdir -p firmware/pixel
     adb pull /product/etc/CarrierSettings firmware/pixel/CarrierSettings
     ```
   - **Option B — Extract from an official Google Pixel Factory / OTA Image:**
     Download a Pixel image from [developers.google.com/android/images](https://developers.google.com/android/images), unpack `product.img` (using `payload-dumper-go` + `fsck.erofs --extract=...` or `7z`), and copy `etc/CarrierSettings/` into `firmware/pixel/CarrierSettings/`.

2. **Samsung One UI (Exynos / Shannon modem) files** (`./firmware/samsung/`):
   - `mnomap.json`, `imsswitch.json`, `imsprofile.json` (from Samsung's `com.sec.imsservice`), `apns-conf.xml`, and optional `build.prop`.
   - **Option A — Pull from an Exynos/Shannon Samsung Galaxy phone (e.g., Galaxy S24 / S25 / S26 `B` model, no Root required):**
     ```sh
     mkdir -p firmware/samsung
     adb pull /system/etc/apns-conf.xml firmware/samsung/
     adb pull /system/build.prop firmware/samsung/
     adb pull /system/priv-app/imsservice/imsservice.apk /tmp/imsservice.apk
     unzip -j /tmp/imsservice.apk "res/raw/mnomap.json" "res/raw/imsswitch.json" "res/raw/imsprofile.json" -d firmware/samsung/
     ```
   - **Option B — Extract from an official Samsung firmware archive (without a Samsung phone):**
     Download an Exynos/Shannon firmware (e.g., `SM-S942B` `EUX`) using open-source tools like `Bifrost` / `samloader`, unpack `AP_*.tar.md5` → `lz4 -d super.img.lz4 super.img` → `lpunpack` `system.img` → extract `system/etc/apns-conf.xml`, `system/build.prop`, and `res/raw/{mnomap,imsswitch,imsprofile}.json` from `system/priv-app/imsservice/imsservice.apk`.

3. **Run the generator & rebuild:**
   ```sh
   ./generate-carrier-db.py --update   # writes res/raw/carrier_db.json
   ./build.sh                          # rebuilds pixel-ims-apn-tuner.apk
   ```
   > ⚠️ Raw firmware dumps under `./firmware/` are proprietary to Google and Samsung — keep them local only (`./firmware/` is git-ignored; never commit them).
</details>

---

### Acknowledgments & Credits

**Original innovation of this project:** the core concept of automatically provisioning persistent carrier **APN profiles for IMS** (`Internet`, `IMS` with 3G `invalid P-CSCF` lockout protection, `MMS`, `XCAP`) and building the `CARRIER_DB` database by cross-referencing Google Pixel's stock carrier database against Samsung Galaxy S26 firmware (sharing the same Shannon modem family) — so uncertified Pixel carriers get verified profiles without touching carriers already supported by Google — is **our own original idea and implementation**.

For the local ADB channel and `CarrierConfig` override mechanics, this project also builds upon ideas from the open-source Pixel IMS community:

- **[charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle)** — original standalone loopback Wireless Debugging + `UiAutomation` architecture and Gradle-free `build.sh` pipeline.
- **[kyujin-cho/pixel-volte-patch](https://github.com/kyujin-cho/pixel-volte-patch)** — pioneering Pixel VoLTE CarrierConfig patching, `ims.ims_user_agent_string` override concept, non-disruptive `UiAutomation` Accessibility flags, and Dual-SIM IMS handling.
- **[vvb2060/Ims](https://github.com/vvb2060/Ims)** — privileged `Instrumentation` (`--no-restart`), Android 14–17 MMTEL/Entitlement provisioning bypass keys, and runtime permission self-granting.
- **[TakaiSaisei/pixel_ims](https://github.com/TakaiSaisei/pixel_ims)** — automatic Wireless Debugging recovery (`WRITE_SECURE_SETTINGS` + `adb_wifi_enabled`), Wi‑Fi-bound `JobScheduler` boot re-apply, Android 12–13 detached execution, and live `ICarrierConfigLoader` flag inspection.
- **[VinujaHerath/PixelIMS](https://github.com/VinujaHerath/PixelIMS)** — `--no-hidden-api-checks` execution, ViLTE toggle, direct per-SIM system settings shortcut, and batch multi-SIM apply workflow.
- **[flyfishxu/kadb](https://github.com/flyfishxu/kadb)** & **[LSPosed/HiddenApiBypass](https://github.com/LSPosed/HiddenApiBypass)** — embedded ADB-over-TLS / SPAKE2 client and hidden Android API access.

---

## License

[MIT](./LICENSE) — based on [charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle) (© charlie-xing).
