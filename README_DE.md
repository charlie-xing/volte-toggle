# Pixel IMS & APN Tuner

**Language / Мова / Язык / Idioma / 语言:** [English](./README.md) | [Беларуская](./README_BY.md) | [Українська](./README_UA.md) | [Bahasa Indonesia](./README_ID.md) | [Bahasa Melayu](./README_MS.md) | [**Deutsch**](./README_DE.md) | [Español](./README_ES.md) | [Filipino](./README_FIL.md) | [Français](./README_FR.md) | [Português](./README_PT.md) | [日本語](./README_JA.md) | [简体中文](./README_CN.md) | [繁體中文](./README_TW.md) | [한국어](./README_KO.md) | [Русский](./README_RU.md)

**VoLTE, VoWiFi, VoNR (5G-Anrufe), 5G SA und automatische Anbieter-APN-Profile auf Google Pixel 6–11 aktivieren — ohne Root, ohne PC und ohne externe Shizuku-App.**

📦 Download: **[Releases → pixel-ims-apn-tuner.apk](https://github.com/4plashy-disprized/pixel-ims-apn-tuner/releases/latest)**

---

### Welches Problem löst diese App?

Google Pixel-Smartphones schalten VoLTE, WLAN-Anrufe (VoWiFi) und 5G ab Werk nur für Mobilfunkanbieter frei, die auf Googles Zertifizierungsliste stehen. Mit einer SIM-Karte eines nicht zertifizierten Anbieters oder im Ausland treten typischerweise folgende Probleme auf:

- **Anrufe fallen auf 3G/2G zurück** (oder schlagen fehl, wo 3G bereits abgeschaltet wurde), und mobile Daten brechen während eines Gesprächs ab.
- **Schalter für VoLTE, WLAN-Anrufe (VoWiFi) und 5G fehlen komplett** in den SIM-Einstellungen.
- Selbst mit einfachen CarrierConfig-Patches **fällt VoLTE nach kurzem 3G-Kontakt (z. B. im Aufzug oder in der U-Bahn) für 30 Minuten aus** (`invalid P-CSCF`-Sperre) oder Rufweiterleitung, MMS und VoWiFi funktionieren mangels passender `ims`-/`xcap`-/`mms`-APNs nicht.
- Einige Anbieter prüfen den SIP-`User-Agent` und **lehnen nicht zertifizierte Google-Pixel-Modelle ab**, obwohl das Shannon-Modem im Pixel technisch vollständig kompatibel ist.

**Pixel IMS & APN Tuner** behebt all das in einer einzigen, eigenständigen App (~800 KB):
1. **Integrierte `CARRIER_DB` für 461 Anbieter** (aus der Firmware des **Samsung Galaxy S26** mit Shannon-Modem): richtet dauerhafte **Internet-, IMS-, MMS- und XCAP-APNs** inklusive Schutz vor 3G-P-CSCF-Sperren ein.
2. **Schaltet VoLTE, VoWiFi, VoNR, 5G SA, ViLTE und Backup-Anrufe frei** und setzt bei Bedarf den **Samsung S26 Shannon IMS-User-Agent** (inklusive 74 anbieterspezifischer Profile).
3. **Kein Root, kein PC und kein Shizuku nötig** — Google Wallet, Banking-Apps und Android-Updates funktionieren uneingeschränkt weiter.

---

### Schnellstart (2 Minuten)

#### Schritt 1. Einmalige Kopplung

1. **Mit einem beliebigen WLAN verbinden** (Zuhause, Büro oder Hotspot eines zweiten Smartphones).
   > ⚠️ Android aktiviert das kabellose Debugging nur bei aktiver WLAN-Verbindung. Nach dem Anwenden funktionieren VoLTE und 5G unterwegs ganz normal über das Mobilfunknetz ohne WLAN.
2. **Entwickleroptionen aktivieren** *(falls noch nicht geschehen)*:
   **Einstellungen → Über das Telefon** öffnen, ganz nach unten scrollen und **7-mal auf „Build-Nummer" tippen**, bis *„Du bist jetzt Entwickler!"* erscheint.
3. **Kabelloses Debugging einschalten**:
   **Einstellungen → System → Entwickleroptionen → Kabelloses Debugging** aktivieren und *„In diesem Netzwerk immer zulassen"* bestätigen.
4. **App koppeln**:
   - **Pixel IMS & APN Tuner** öffnen und Berechtigungen für Telefon & Benachrichtigungen erteilen.
   - Auf **„Kopplung starten"** tippen — die App zeigt eine Benachrichtigung an und öffnet das Menü „Kabelloses Debugging".
   - Auf **„Gerät über Kopplungscode koppeln"** tippen — ein Dialog mit einem **6-stelligen Code** erscheint.
   - **Den Dialog geöffnet lassen**, die Benachrichtigungsleiste herunterziehen, in der Benachrichtigung den 6-stelligen Code eingeben und absenden.
5. Zurück in der App zeigt die ADB-Karte nun **Bereit** an. Diese Kopplung muss nicht wiederholt werden!

#### Schritt 2. Einstellungen für die SIM-Karte anwenden

1. In der SIM-Karte sind die empfohlenen Optionen für Ihren Anbieter **bereits vorausgewählt**.
2. Auf **„Einstellungen anwenden"** tippen (bzw. **„Auf alle SIMs anwenden"** bei Dual-SIM).
3. Nach wenigen Sekunden wechselt der Status auf **„IMS: registriert"** und darunter erscheint **„Im System aktiv: VoLTE, …"**.
4. **Wenn Sie VoWiFi aktiviert haben:** auf **„SIM-Einstellungen öffnen"** tippen → **WLAN-Anrufe** öffnen → Schalter einschalten und bevorzugten Modus wählen.

---

### Alle Optionen und Schaltflächen im Überblick

#### Anzeigen auf der SIM-Karte

- **`CARRIER_DB: ...`** — Ihre SIM wurde in der Datenbank (461 Anbieter) erkannt (`LTE_WIFI` = VoLTE + VoWiFi; `LTE_ONLY` = nur VoLTE).
- **`IMS: registriert` / `nicht registriert`** — zeigt live an, ob das Telefon erfolgreich beim IMS-Sprachserver des Anbieters angemeldet ist.
- **`Im System aktiv: ...`** — liest die tatsächlich aktiven System-Flags direkt aus Androids `CarrierConfigLoader` aus.
- **Ausgegraute Optionen mit `(Systemprofil)`** — bereits ab Werk von Google aktivierte Funktionen werden vor versehentlichem Ausschalten geschützt, während fehlende Optionen ergänzt werden können.

#### Hauptoptionen

| Option | Bedeutung in einfachen Worten |
| :--- | :--- |
| **VoLTE (4G-Anrufe)** | Sprachanrufe in HD-Qualität über 4G LTE ohne Rückfall auf 3G/2G und ohne Unterbrechung der mobilen Daten. **Immer empfohlen.** |
| **VoWiFi (WLAN-Anrufe + Symbol)** | Telefonieren und SMS über jedes WLAN, auch ohne Mobilfunkempfang (Keller, dicke Wände, ländliche Gebiete oder im Ausland zum Heimattarif). *Aktivieren, wenn Ihr Anbieter WLAN-Anrufe unterstützt.* |
| **VoNR (5G-Anrufe)** | Sprachanrufe direkt über 5G ohne Wechsel zurück zu 4G während des Telefonats. |
| **5G SA + NR-Signalschwellen** | Aktiviert in Android **5G NSA & 5G SA** (`[1, 2]`), verhindert das automatische Abschalten von 5G SA durch den IMS-Stack ohne VoNR (`ims.sa_disable_policy_int = 0`) und gleicht die **5G-Signalbalken-Skala (`SS-RSRP`)** in der Statusleiste an die gewohnte 4G-LTE-Skala an (`[-128, -118, -108, -98]` dBm statt der strengen Standardwerte `[-110, -90, -80, -65]` dBm mit 0 Balken bei `-111` dBm). *Ändert nicht die physische Modem-Empfindlichkeit.* |
| **APN-Profile (Internet, IMS, MMS, XCAP) aus CARRIER_DB** | Schreibt geprüfte Anbieter-APNs vom Samsung S26 (`Internet`, `MMS`, `IMS` mit Schutz vor 30-minütigen 3G-Sperren sowie `XCAP`). **Bleibt dauerhaft über Neustarts hinweg gespeichert.** |

#### Bereich „Erweitert ▸"

| Erweiterte Option | Zweck |
| :--- | :--- |
| **Samsung S26 (Shannon) IMS-User-Agent** | Meldet das Modem gegenüber dem Netz als zertifiziertes **Samsung Galaxy S26** (inkl. spezifischer Profile für 74 strenge Anbieter). Löst Registrierungsfehler bei Anbietern, die unbekannte Pixel-Geräte blockieren. |
| **ViLTE (4G/5G-Videoanrufe)** | Aktiviert native Mobilfunk-Videoanrufe direkt in der Telefon-App (sofern vom Anbieter unterstützt). |
| **VoWiFi im Roaming** | Erlaubt WLAN-Anrufe im Ausland und schaltet die Roaming-Auswahl frei. |
| **Zusatzdienste (UT / XCAP)** | Ermöglicht Rufweiterleitung, Anklopfen und Anrufsperren direkt über 4G/WLAN statt 2G/3G. |
| **Cross-SIM / Backup-Anrufe** | Ermöglicht bei Dual-SIM Anrufe und SMS der ersten SIM **über die mobilen Daten der zweiten SIM**, wenn die erste SIM kein Netz hat (ideal mit Reise-eSIM im Ausland). |
| **LTE+- / 4G+-Symbol bei jeder Kanalbündelung** | Entfernt Pixels versteckte Bandbreitenschwelle, sodass `LTE+` / `4G+` bei jeder Carrier Aggregation angezeigt wird (rein optisch). |
| **Hinzufügen von APNs in den Einstellungen erlauben** | Entsperrt das manuelle Anlegen und Bearbeiten von APNs in den Systemeinstellungen. |

#### Schaltflächen & „Optionen"

- **„Einstellungen anwenden"** — speichert und aktiviert die Auswahl für diese SIM.
- **„Zurücksetzen"** — entfernt alle Überschreibungen und stellt die Werks-APNs wieder her.
- **„SIM-Einstellungen öffnen"** — öffnet direkt das Android-Einstellungsmenü dieser SIM.
- **„Auf alle SIMs anwenden"** *(bei 2 aktiven SIMs)* — konfiguriert beide SIMs mit einem Tippen.
- **„Nach dem Start erneut anwenden"** *(empfohlen ab Android 16 QPR2 / Android 17)* — Basis-**VoLTE und APNs** bleiben dauerhaft erhalten; diese Option wartet nach einem Neustart auf WLAN, weckt bei Bedarf das kabellose Debugging automatisch auf und stellt auch `CarrierConfig`-Funktionen (**VoWiFi**, **5G / VoNR**, **S26 User-Agent**, **Cross-SIM**) im Hintergrund wieder her.

---

### Häufige Fragen & Fehlerbehebung (FAQ)

<details>
<summary><b>1. Einstellungen angewendet, aber es steht weiterhin „IMS: nicht registriert". Was prüfen?</b></summary>

1. Prüfen Sie, ob **VoLTE bei Ihrem Mobilfunkvertrag freigeschaltet** ist (oft kostenlos in der Anbieter-App oder beim Support aktivierbar).
2. Stellen Sie sicher, dass das Telefon mit **4G (LTE) oder 5G** verbunden ist.
3. Unterstützt Ihr Anbieter kein VoWiFi, deaktivieren Sie *VoWiFi* und tippen Sie erneut auf *Einstellungen anwenden*.
4. Schalten Sie den **Flugmodus** für 5–10 Sekunden ein und wieder aus.
</details>

<details>
<summary><b>2. Gehen die Einstellungen nach einem Neustart verloren (auch unterwegs ohne WLAN)?</b></summary>

- **Basis-VoLTE (4G-Anrufe) und alle APN-Profile (`Internet`, `IMS`, `MMS`, `XCAP`) bleiben nach einem Neustart DAUERHAFT erhalten** — auch unter Android 16 QPR2 / Android 17 und komplett ohne WLAN!
- Unter **Android 12–15 und frühem Android 16 (vor QPR2)** bleibt die gesamte Konfiguration dauerhaft gespeichert (`[persistent]`).
- Unter **Android 16 QPR2+ und Android 17** werden beim Ausschalten nur reine `CarrierConfig`-Zusatzfunktionen (**VoWiFi**, **5G / VoNR**, **Samsung S26 User-Agent**, **Cross-SIM**, **ViLTE**, **LTE+-Symbol**) zurückgesetzt und automatisch wiederhergestellt, sobald sich das Telefon mit einem bekannten WLAN verbindet (bei aktiviertem *„Nach dem Start erneut anwenden"*).
</details>

<details>
<summary><b>3. ADB meldet „Nicht verbunden", obwohl ich bereits gekoppelt habe. Muss ich neu koppeln?</b></summary>

**Nein!** Verbinden Sie sich einfach mit einem vertrauenswürdigen WLAN und tippen Sie auf **„Erneut prüfen"** — die App schaltet das kabellose Debugging automatisch wieder ein, falls Android es nach einem Neustart deaktiviert hat.
</details>

<details>
<summary><b>4. „IMS: registriert" wird angezeigt, aber WLAN-Anrufe (VoWiFi) starten nicht.</b></summary>

Tippen Sie auf **„SIM-Einstellungen öffnen"** → **WLAN-Anrufe** → Schalter aktivieren und **„Über WLAN anrufen"** als Präferenz wählen. Zum Testen Flugmodus einschalten, WLAN wieder aktivieren und einen Testanruf tätigen.
</details>

<details>
<summary><b>5. Ist das sicher für Google Wallet, Banking-Apps und Systemupdates?</b></summary>

**Ja, zu 100 % sicher.** Kein Root, kein offener Bootloader, keine Internetverbindung (alles läuft lokal über `127.0.0.1`). Google Wallet, Banking-Apps und OTA-Updates funktionieren normal weiter.
</details>

<details>
<summary><b>6. Wie setze ich alles auf den Werkszustand zurück?</b></summary>

Tippen Sie bei bereitem ADB-Kanal auf **„Zurücksetzen"** auf der SIM-Karte oder deinstallieren Sie die App, starten Sie das Gerät neu und wählen Sie in den Android-APN-Einstellungen *Auf Standard zurücksetzen*.
</details>

---

### Lösung des 3G-Fallback-Problems (30-Minuten-Sperre) über `ims`-APN-Bitmasken

Ohne dedizierten `ims`-APN (oder bei manuell angelegtem APN mit Standardmaske `0` = alle Netze inkl. 2G/3G) versucht Android bei kurzem Rückfall ins **3G-Netz** (Aufzug, U-Bahn, Tiefgarage), den `IMS PDN` über 3G zu halten. Da 3G kein IMS-Sprachrouting liefert (`P-CSCF = []`), sperrt Pixels `ShannonImsService` (`BLOCK_PDN_BY_INVALID_PCSCF = 1`) VoLTE und VoWiFi für **30 Minuten aktiver CPU-Zeit (30–90 Min.)**. `ApnProvisioner` setzt daher für den `ims`-APN exakte Bitmasken ohne 2G/3G (`network_type_bitmask` & `lingering_network_type_bitmask` = `659456` für `LTE|IWLAN|NR` bzw. `528384` für `LTE|NR`, `bearer_bitmask` = `663552` bzw. `532480`), sodass IMS im 3G sauber pausiert und bei Rückkehr nach 4G/5G sofort wieder verbindet.

---

### Aus dem Quellcode bauen

```sh
./build.sh    # erzeugt pixel-ims-apn-tuner.apk
```

#### `CARRIER_DB` aktualisieren (`generate-carrier-db.py`)

`res/raw/carrier_db.json` ist bereits fertig im Repository enthalten. Wer die Datenbank mit [`generate-carrier-db.py`](./generate-carrier-db.py) (reines Python 3) selbst neu generieren möchte, benötigt folgende Dateien:

<details>
<summary><b>Benötigte Dateien &amp; Bezugsquellen</b></summary>

1. **Google Pixel** (`./firmware/pixel/CarrierSettings/`): `carrier_list.pb`, `others.pb`, `*.pb` — per ADB ohne Root direkt vom Pixel (`adb pull /product/etc/CarrierSettings firmware/pixel/CarrierSettings`) oder aus dem offiziellen Pixel-Factory-Image (`product.img` von [developers.google.com/android/images](https://developers.google.com/android/images)).
2. **Samsung One UI mit Exynos-/Shannon-Modem** (`./firmware/samsung/`): `apns-conf.xml`, optional `build.prop` sowie `mnomap.json`, `imsswitch.json`, `imsprofile.json` (aus `res/raw/` von `/system/priv-app/imsservice/imsservice.apk`) — per `adb pull` ohne Root von einem Exynos-Galaxy (z. B. S24/S25/S26 `B`-Modell) oder entpackt aus einem offiziellen Firmware-Archiv (`SM-S942B` via `Bifrost` / `samloader` → `AP_*.tar.md5` → `super.img.lz4` → `system.img`).
3. **Ausführen:** `./generate-carrier-db.py --update && ./build.sh` *(Roh-Firmware unter `./firmware/` steht in `.gitignore` und darf aus Lizenzgründen nicht committet werden).*
</details>

---

### Danksagung & inspirierende Projekte

**Eigene Entwicklung dieses Projekts:** Die Kernidee, dauerhafte **APN-Profile speziell für IMS** (`Internet`, `IMS` mit Schutz vor 30-minütigen `invalid P-CSCF`-Sperren im 3G-Netz, `MMS`, `XCAP`) automatisch einzurichten und die `CARRIER_DB` durch den Abgleich der Google-Pixel-Anbieterdatenbank mit der Samsung-Galaxy-S26-Datenbank (gleiche Shannon-Modemfamilie) zu erzeugen, ist **unsere eigene Idee und Implementierung**.

Für den lokalen ADB-Kanal und die `CarrierConfig`-Mechanismen greift dieses Projekt zudem Ideen aus der Open-Source-Community auf:

- **[charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle)** — eigenständige Loopback-Architektur über Kabelloses Debugging + `UiAutomation` (ohne Shizuku) sowie Gradle-freies `build.sh`-Skript.
- **[kyujin-cho/pixel-volte-patch](https://github.com/kyujin-cho/pixel-volte-patch)** — Vorreiter beim Pixel-`CarrierConfig`-Patching, `ims.ims_user_agent_string`-Konzept, schonende `UiAutomation`-Bedienungshilfen-Flags und Dual-SIM-IMS-Behandlung.
- **[vvb2060/Ims](https://github.com/vvb2060/Ims)** — privilegierte `Instrumentation` (`--no-restart`), MMTEL-/Entitlement-Bypass-Schlüssel für Android 14–17 und automatische Berechtigungsvergabe.
- **[TakaiSaisei/pixel_ims](https://github.com/TakaiSaisei/pixel_ims)** — automatisches Aufwecken des kabellosen Debuggings (`WRITE_SECURE_SETTINGS` + `adb_wifi_enabled`), WLAN-gebundener `JobScheduler` beim Neustart, Detached-Modus für Android 12–13 und Live-Auslesen über `ICarrierConfigLoader`.
- **[VinujaHerath/PixelIMS](https://github.com/VinujaHerath/PixelIMS)** — `--no-hidden-api-checks`-Aufruf, ViLTE-Option, Direktlink in die SIM-Einstellungen und Anwenden auf alle SIMs.
- **[flyfishxu/kadb](https://github.com/flyfishxu/kadb)** & **[LSPosed/HiddenApiBypass](https://github.com/LSPosed/HiddenApiBypass)** — integrierter ADB-over-TLS-/SPAKE2-Client und Zugriff auf versteckte Android-APIs.

---

## Lizenz

[MIT](./LICENSE) — basierend auf [charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle) (© charlie-xing).
