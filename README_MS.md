# Pixel IMS & APN Tuner

**Language / Мова / Язык / Idioma / 语言:** [English](./README.md) | [Беларуская](./README_BY.md) | [Українська](./README_UA.md) | [Bahasa Indonesia](./README_ID.md) | [**Bahasa Melayu**](./README_MS.md) | [Deutsch](./README_DE.md) | [Español](./README_ES.md) | [Filipino](./README_FIL.md) | [Français](./README_FR.md) | [Português](./README_PT.md) | [日本語](./README_JA.md) | [简体中文](./README_CN.md) | [繁體中文](./README_TW.md) | [한국어](./README_KO.md) | [Русский](./README_RU.md)

**Dayakan VoLTE, VoWiFi, VoNR (panggilan 5G), 5G SA dan profil APN pengendali automatik pada Google Pixel 6–11 — tanpa Root, tanpa PC dan tanpa aplikasi luaran (Shizuku).**

📦 Muat turun: **[Releases → pixel-ims-apn-tuner.apk](https://github.com/4plashy-disprized/pixel-ims-apn-tuner/releases/latest)**

---

### Apakah masalah yang diselesaikan oleh aplikasi ini?

Telefon Google Pixel hanya mendayakan VoLTE, Panggilan Wi‑Fi (VoWiFi) dan 5G secara lalai untuk pengendali yang disenaraikan secara rasmi oleh Google. Apabila menggunakan kad SIM pengendali yang belum diperakui (contohnya di Malaysia, Brunei atau rantau lain), masalah berikut sering berlaku:

- **Panggilan jatuh ke 3G/2G** (atau gagal sepenuhnya di kawasan yang telah menutup rangkaian 3G), dan data mudah alih terputus semasa panggilan.
- **Suis VoLTE, Panggilan Wi‑Fi dan 5G tiada langsung** dalam tetapan SIM.
- Walaupun menggunakan tampalan biasa, **VoLTE boleh terkunci selama 30 minit selepas melalui lif atau parkir bawah tanah** (isu `invalid P-CSCF` apabila jatuh seketika ke 3G), atau MMS, pemajuan panggilan dan VoWiFi gagal kerana tiada APN `ims` / `xcap` / `mms` yang betul.
- Sesetengah pengendali menyemak pengepala `User-Agent` dan **menolak peranti Google Pixel yang tidak diperakui**, walaupun modem Samsung Shannon di dalamnya serasi sepenuhnya.

**Pixel IMS & APN Tuner** menyelesaikan semuanya dalam satu aplikasi ringan (~800 KB):
1. **Pangkalan data terbina dalam `CARRIER_DB` untuk 461 pengendali** (daripada perisian tegar **Samsung Galaxy S26** dengan modem Shannon): menulis APN kekal **Internet, IMS, MMS dan XCAP** secara automatik tanpa sekatan 3G.
2. **Membuka kunci VoLTE, VoWiFi, VoNR, 5G SA, ViLTE dan Panggilan sandaran**, serta boleh menggunakan **IMS User-Agent Samsung S26** (termasuk 74 profil khusus pengendali).
3. **Tanpa Root, tanpa PC dan tanpa Shizuku** — Google Wallet, aplikasi perbankan dan kemas kini OTA Android terus berfungsi seperti biasa.

---

### Mula Pantas (2 minit)

#### Langkah 1. Penggandingan sekali sahaja

1. **Sambungkan ke mana-mana rangkaian Wi‑Fi** (rumah, pejabat atau hotspot telefon lain).
   > ⚠️ Wi‑Fi hanya diperlukan semasa membuat tetapan; selepas digunakan, VoLTE dan 5G terus berfungsi melalui data mudah alih tanpa Wi‑Fi.
2. **Dayakan «Pilihan pembangun»** *(jika belum aktif)*:
   Buka **Tetapan → Perihal telefon**, tatal ke bawah dan **ketik «Nombor binaan» 7 kali** sehingga muncul *«Anda kini seorang pembangun»*.
3. **Dayakan «Penyahpepijatan wayarles»**:
   Buka **Tetapan → Sistem → Pilihan pembangun → Penyahpepijatan wayarles**, hidupkan suis dan tandakan *«Sentiasa benarkan pada rangkaian ini»*.
4. **Gandakan aplikasi**:
   - Buka **Pixel IMS & APN Tuner** dan berikan kebenaran Telefon & Pemberitahuan.
   - Ketik **«Mulakan penggandingan»** — pemberitahuan akan dipaparkan dan skrin Penyahpepijatan wayarles dibuka.
   - Ketik **«Gandakan peranti dengan kod penggandingan»** — dialog dengan **kod 6 digit** akan muncul.
   - **Biarkan dialog itu terbuka**, tarik talam pemberitahuan ke bawah, ketik **«Masukkan kod»**, taip 6 digit tersebut dan hantar.
5. Kembali ke aplikasi — kad ADB kini memaparkan **Sedia**. Anda tidak perlu menggandakan lagi!

#### Langkah 2. Gunakan tetapan untuk SIM anda

1. Dalam kad SIM anda, pilihan yang disyorkan untuk pengendali anda **telah dipilih secara automatik**.
2. Ketik **«Gunakan tetapan»** (atau **«Gunakan pada semua SIM»** untuk Dwi-SIM).
3. Dalam beberapa saat, status bertukar kepada **«IMS: berdaftar»** berserta senarai **«Aktif dalam sistem: VoLTE, …»**.
4. **Jika anda mendayakan VoWiFi:** ketik **«Buka tetapan SIM»** → buka **Panggilan Wi‑Fi** dan pastikan suisnya dihidupkan.

---

### Panduan Lengkap Semua Pilihan & Butang

#### Penunjuk pada kad SIM

- **`CARRIER_DB: ...`** — SIM anda ditemui dalam pangkalan data 461 pengendali (`LTE_WIFI` = menyokong VoLTE & VoWiFi; `LTE_ONLY` = VoLTE sahaja).
- **`IMS: berdaftar` / `tidak berdaftar`** — pengesahan langsung bahawa telefon telah bersambung ke pelayan suara pengendali.
- **`Aktif dalam sistem: ...`** — memaparkan bendera sistem sebenar yang sedang aktif dalam Android.
- **Kotak semak kelabu `(profil sistem)`** — melindungi ciri yang sudah sedia aktif dari kilang supaya tidak dimatikan secara tidak sengaja.

#### Pilihan utama SIM

| Pilihan | Penerangan mudah |
| :--- | :--- |
| **VoLTE (panggilan 4G)** | Panggilan suara HD melalui 4G LTE tanpa jatuh ke 3G/2G dan tanpa gangguan internet semasa berbual. **Disyorkan sentiasa hidup.** |
| **VoWiFi (Panggilan Wi‑Fi + ikon bar status)** | Membuat dan menerima panggilan & SMS melalui mana-mana Wi‑Fi apabila liputan selular lemah atau tiada (bawah tanah, dalam bangunan atau di luar negara). *Dayakan jika pengendali menyokong Panggilan Wi‑Fi.* |
| **VoNR (panggilan 5G)** | Panggilan suara terus di rangkaian 5G tanpa kembali ke 4G. |
| **5G SA + ambang isyarat NR** | Mendayakan **5G NSA & 5G SA** dalam Android (`[1, 2]`), menghalang tindanan IMS daripada mematikan 5G SA tanpa VoNR (`ims.sa_disable_policy_int = 0`), dan menjajarkan **skala bar isyarat 5G (`SS-RSRP`)** pada bar status mengikut skala piawai 4G LTE (`[-128, -118, -108, -98]` dBm berbanding `[-110, -90, -80, -65]` dBm yang memaparkan 0 bar pada `-111` dBm). *Tidak mengubah kepekaan fizikal modem.* |
| **Profil APN (Internet, IMS, MMS, XCAP) daripada CARRIER_DB** | Menulis APN Samsung S26 yang disahkan (`Internet`, `MMS`, `IMS` bebas sekatan 30 minit dalam 3G dan `XCAP`). **Kekal disimpan walaupun selepas but semula.** |

#### Bahagian «Lanjutan ▸»

| Pilihan lanjutan | Tujuan |
| :--- | :--- |
| **IMS User-Agent Samsung S26 (Shannon)** | Memperkenalkan modem kepada rangkaian pengendali sebagai **Samsung Galaxy S26** yang diperakui (termasuk 74 profil khusus pengendali). Menyelesaikan penolakan VoLTE/VoWiFi pada Pixel. |
| **ViLTE (panggilan video 4G/5G)** | Mendayakan panggilan video asli terus dalam pendail Telefon melalui rangkaian mudah alih. |
| **VoWiFi semasa perayauan** | Membenarkan Panggilan Wi‑Fi berfungsi semasa berada di luar negara. |
| **Perkhidmatan tambahan (UT / XCAP)** | Mengurus pemajuan panggilan & panggilan menunggu terus melalui 4G/Wi‑Fi tanpa jatuh ke 2G/3G. |
| **Cross-SIM / Panggilan sandaran** | Pada telefon Dwi-SIM, membolehkan SIM pertama membuat panggilan & SMS **menggunakan data mudah alih SIM kedua** apabila SIM pertama tiada liputan. |
| **Paparkan ikon LTE+ / 4G+ pada sebarang agregasi** | Membuang had lebar jalur tersembunyi Pixel supaya ikon `LTE+` / `4G+` dipaparkan setiap kali Carrier Aggregation aktif. |
| **Benarkan penambahan APN dalam tetapan sistem** | Membuka kunci penambahan dan penyuntingan APN manual dalam tetapan Android. |

#### Butang tindakan & «Pilihan»

- **«Gunakan tetapan»** — menyimpan dan mengaktifkan pilihan untuk SIM tersebut.
- **«Tetapkan semula»** — memadam perubahan SIM dan memulihkan APN asal kilang.
- **«Buka tetapan SIM»** — terus membuka skrin tetapan Android bagi SIM tersebut.
- **«Gunakan pada semua SIM»** *(apabila 2 SIM aktif)* — menetapkan kedua-dua SIM dengan satu ketikan.
- **«Gunakan semula selepas but»** *(disyorkan pada Android 16 QPR2+ dan Android 17)* — **VoLTE asas & semua APN** adalah kekal; pilihan ini menunggu Wi‑Fi selepas telefon dihidupkan semula, menghidupkan semula Penyahpepijatan wayarles secara automatik jika perlu, dan memulihkan ciri `CarrierConfig` (**VoWiFi**, **5G / VoNR**, **User-Agent S26**, **Cross-SIM**) di latar belakang.

---

### Soalan Lazim & Penyelesaian Masalah (FAQ)

<details>
<summary><b>1. Sudah ketik «Gunakan tetapan», tetapi masih memaparkan «IMS: tidak berdaftar». Apa perlu disemak?</b></summary>

1. Pastikan **perkhidmatan VoLTE telah diaktifkan pada pelan/nombor anda oleh pengendali**.
2. Pastikan telefon sedang berada pada rangkaian **4G (LTE) atau 5G**.
3. Jika pengendali tidak menawarkan VoWiFi, nyahtanda *VoWiFi* dan ketik *Gunakan tetapan* semula.
4. Hidupkan **Mod pesawat** selama 5–10 saat kemudian matikan semula.
</details>

<details>
<summary><b>2. Adakah tetapan hilang selepas telefon dibut semula (termasuk di luar tanpa Wi‑Fi)?</b></summary>

- **VoLTE asas (panggilan 4G) dan semua APN (`Internet`, `IMS`, `MMS`, `XCAP`) TIDAK hilang selepas but semula** — malah pada Android 16 QPR2 / Android 17 dan tanpa Wi‑Fi, kerana ia ditulis secara kekal ke dalam `telephony.db`.
- **Pada Android 12–15 dan Android 16 sebelum QPR2**, keseluruhan konfigurasi kekal sepenuhnya (`[persistent]`).
- **Pada Android 16 QPR2+ dan Android 17**, hanya ciri khusus `CarrierConfig` (**VoWiFi**, **5G / VoNR**, **User-Agent S26**, **Cross-SIM**, **ViLTE**, **ikon LTE+**) ditetapkan semula apabila telefon dimatikan dan dipulihkan secara automatik sebaik sahaja bersambung ke Wi‑Fi yang dipercayai apabila *«Gunakan semula selepas but»* ditandakan.
</details>

<details>
<summary><b>3. Kad ADB memaparkan «Tidak disambungkan» walaupun sudah digandakan dahulu. Perlu gandakan semula?</b></summary>

**Tidak perlu!** Sambungkan sahaja ke Wi‑Fi yang dipercayai dan ketik **«Semak semula»** — aplikasi akan menghidupkan semula Penyahpepijatan wayarles secara automatik jika ia dimatikan selepas but semula.
</details>

<details>
<summary><b>4. Memaparkan «IMS: berdaftar», tetapi Panggilan Wi‑Fi (VoWiFi) tidak berfungsi.</b></summary>

Ketik **«Buka tetapan SIM»** → **Panggilan Wi‑Fi** → hidupkan suis dan pilih keutamaan **«Buat panggilan melalui Wi‑Fi»**. Untuk menguji, hidupkan Mod pesawat, hidupkan semula Wi‑Fi dan cuba buat panggilan.
</details>

<details>
<summary><b>5. Adakah ini selamat untuk Google Wallet, aplikasi bank dan kemas kini OTA?</b></summary>

**100% selamat.** Tiada Root, tiada nyahkunci bootloader dan tiada sambungan internet luar (semuanya berjalan secara tempatan pada `127.0.0.1`). Google Wallet, aplikasi perbankan dan kemas kini OTA berfungsi seperti biasa.
</details>

<details>
<summary><b>6. Bagaimana cara mengembalikan semuanya kepada asal kilang?</b></summary>

Ketik **«Tetapkan semula»** pada kad SIM semasa saluran ADB sedia, atau nyahpasang aplikasi, but semula telefon dan pilih *Tetapkan semula kepada lalai* dalam menu APN Android.
</details>

---

### Penyelesaian Sekatan 30 Minit Apabila Jatuh ke 3G Melalui Bitmask APN `ims`

Tanpa APN `ims` khusus (atau APN manual dengan bitmask `0` = semua rangkaian termasuk 2G/3G), apabila telefon jatuh seketika ke **3G** (lif, parkir bawah tanah), Android cuba mengekalkan `IMS PDN` pada 3G, menerima `P-CSCF = []` kosong dan `ShannonImsService` mengaktifkan `BLOCK_PDN_BY_INVALID_PCSCF = 1`, menyekat VoLTE & VoWiFi selama **30–90 minit**. `ApnProvisioner` menulis APN `ims` dengan bitmask yang mengecualikan 2G/3G (`network_type_bitmask` & `lingering_network_type_bitmask` = `659456` bagi `LTE|IWLAN|NR` atau `528384` bagi `LTE|NR`, `bearer_bitmask` = `663552` atau `532480`), supaya IMS dihentikan dengan kemas dalam 3G dan bersambung semula dalam 1–2 saat sebaik kembali ke 4G/5G.

---

### Membina daripada Kod Sumber

```sh
./build.sh    # menghasilkan pixel-ims-apn-tuner.apk
```

#### Mengemas kini `CARRIER_DB` (`generate-carrier-db.py`)

`res/raw/carrier_db.json` telah disertakan dalam repositori. Jika mahu menjana semula melalui [`generate-carrier-db.py`](./generate-carrier-db.py) (Python 3 tulen):

<details>
<summary><b>Fail yang diperlukan &amp; cara mendapatkannya</b></summary>

1. **Google Pixel** (`./firmware/pixel/CarrierSettings/`): `carrier_list.pb`, `others.pb` dan `*.pb` — salin melalui ADB tanpa Root dari Pixel (`adb pull /product/etc/CarrierSettings firmware/pixel/CarrierSettings`) atau ekstrak `product.img` daripada imej rasmi Pixel ([developers.google.com/android/images](https://developers.google.com/android/images)).
2. **Samsung One UI bermodem Exynos/Shannon** (`./firmware/samsung/`): `apns-conf.xml`, `build.prop` (pilihan) serta `mnomap.json`, `imsswitch.json`, `imsprofile.json` (di dalam `res/raw/` bagi `/system/priv-app/imsservice/imsservice.apk`) — melalui `adb pull` tanpa Root daripada Galaxy Exynos (S24/S25/S26 siri `B`) atau daripada arkib perisian tegar rasmi (`SM-S942B` melalui `Bifrost` / `samloader`).
3. **Jalankan:** `./generate-carrier-db.py --update && ./build.sh` *(folder `./firmware/` disenaraikan dalam `.gitignore` — jangan sesekali komit fail perisian tegar ke Git).*
</details>

---

### Penghargaan & Projek Rujukan

**Inovasi asal projek ini:** idea untuk mengkonfigurasi secara automatik **profil APN kekal bagi IMS** (`Internet`, `IMS` dengan perlindungan sekatan `invalid P-CSCF` dalam 3G, `MMS`, `XCAP`) serta membina pangkalan data `CARRIER_DB` melalui padanan silang antara pangkalan data pengendali Google Pixel dan perisian tegar Samsung Galaxy S26 (keluarga modem Shannon yang sama) adalah **idea dan pelaksanaan asli kami sendiri**.

Bagi mekanisme saluran ADB tempatan dan `CarrierConfig`, projek ini turut menggabungkan idea daripada komuniti sumber terbuka:

- **[charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle)** — seni bina kendiri gelung balik Penyahpepijatan wayarles + `UiAutomation` (tanpa Shizuku) dan binaan `build.sh` tanpa Gradle.
- **[kyujin-cho/pixel-volte-patch](https://github.com/kyujin-cho/pixel-volte-patch)** — perintis tampalan `CarrierConfig` Pixel, konsep penggantian `ims.ims_user_agent_string`, bendera `UiAutomation` mesra Kebolehaksesan, dan pengendalian Dwi-SIM.
- **[vvb2060/Ims](https://github.com/vvb2060/Ims)** — `Instrumentation` berkeistimewaan (`--no-restart`), kunci pintasan MMTEL/Entitlement Android 14–17, dan pemberian kebenaran automatik.
- **[TakaiSaisei/pixel_ims](https://github.com/TakaiSaisei/pixel_ims)** — pemulihan automatik Penyahpepijatan wayarles (`WRITE_SECURE_SETTINGS` + `adb_wifi_enabled`), guna semula selepas but terikat Wi‑Fi (`JobScheduler`), mod terasing Android 12–13, dan semakan langsung `ICarrierConfigLoader`.
- **[VinujaHerath/PixelIMS](https://github.com/VinujaHerath/PixelIMS)** — bendera `--no-hidden-api-checks`, pilihan ViLTE, pintasan terus ke tetapan SIM, dan penggunaan serentak pada semua SIM.
- **[flyfishxu/kadb](https://github.com/flyfishxu/kadb)** & **[LSPosed/HiddenApiBypass](https://github.com/LSPosed/HiddenApiBypass)** — klien ADB over TLS / SPAKE2 terbina dalam dan akses API tersembunyi Android.

---

## Lesen

[MIT](./LICENSE) — berasaskan [charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle) (© charlie-xing).
