# Pixel IMS & APN Tuner

**Language / Мова / Язык / Idioma / 语言:** [English](./README.md) | [Беларуская](./README_BY.md) | [Українська](./README_UA.md) | [**Bahasa Indonesia**](./README_ID.md) | [Bahasa Melayu](./README_MS.md) | [Deutsch](./README_DE.md) | [Español](./README_ES.md) | [Filipino](./README_FIL.md) | [Français](./README_FR.md) | [Português](./README_PT.md) | [日本語](./README_JA.md) | [简体中文](./README_CN.md) | [繁體中文](./README_TW.md) | [한국어](./README_KO.md) | [Русский](./README_RU.md)

**Aktifkan VoLTE, VoWiFi, VoNR (panggilan 5G), 5G SA, dan profil APN operator otomatis di Google Pixel 6–11 — tanpa Root, tanpa PC, dan tanpa aplikasi tambahan (Shizuku).**

📦 Unduh: **[Releases → pixel-ims-apn-tuner.apk](https://github.com/4plashy-disprized/pixel-ims-apn-tuner/releases/latest)**

---

### Masalah apa yang diselesaikan aplikasi ini?

Ponsel Google Pixel secara bawaan hanya mengaktifkan VoLTE, Panggilan Wi‑Fi (VoWiFi), dan 5G untuk operator resmi yang masuk daftar putih Google. Saat memakai kartu SIM operator Indonesia (Telkomsel, Indosat, XL, Tri, Smartfren, by.U) atau operator negara lain yang belum didukung, biasanya muncul kendala berikut:

- **Saat menelepon, internet mati** dan sinyal turun ke **3G/2G** (atau gagal menelepon di area yang 3G-nya sudah dimatikan).
- **Menu VoLTE, Panggilan Wi‑Fi, dan 5G tidak muncul sama sekali** di setelan SIM.
- Meski memakai patch biasa, **VoLTE sering terkunci selama 30 menit setelah masuk lift atau basement** (`invalid P-CSCF` saat sempat turun ke 3G), atau pengalihan panggilan, MMS, dan VoWiFi tidak berjalan karena tidak ada APN `ims` / `xcap` / `mms` yang tepat.
- Sebagian operator memeriksa identitas ponsel dan **menolak Google Pixel yang tidak tersertifikasi**, padahal modem Samsung Shannon di Pixel sepenuhnya kompatibel.

**Pixel IMS & APN Tuner** mengatasi semuanya dalam satu aplikasi ringan (~800 KB):
1. **Basis data bawaan `CARRIER_DB` untuk 461 operator** (dari firmware **Samsung Galaxy S26** bermodem Shannon): menulis otomatis APN permanen **Internet, IMS, MMS, dan XCAP** dengan perlindungan anti-kunci 3G.
2. **Membuka kunci VoLTE, VoWiFi, VoNR, 5G SA, ViLTE, dan Panggilan cadangan**, serta dapat memakai **IMS User-Agent Samsung S26** (termasuk 74 profil khusus operator).
3. **Tanpa Root, tanpa PC, dan tanpa Shizuku** — aplikasi perbankan, QRIS/Google Wallet, dan update OTA Android tetap aman dan normal.

---

### Panduan Cepat (2 menit)

#### Langkah 1. Pemasangan sekali saja

1. **Hubungkan ke jaringan Wi‑Fi apa saja** (Wi‑Fi rumah, kantor, atau hotspot dari HP lain).
   > ⚠️ Wi‑Fi hanya diperlukan saat menyetel; setelah diterapkan, VoLTE dan 5G tetap berjalan dengan data seluler tanpa Wi‑Fi.
2. **Aktifkan «Opsi developer»** *(jika belum aktif)*:
   Buka **Setelan → Tentang ponsel**, gulir ke paling bawah, lalu **ketuk «Nomor versi» 7 kali** hingga muncul *«Anda sekarang adalah developer!»*.
3. **Aktifkan «Proses debug nirkabel»**:
   Buka **Setelan → Sistem → Opsi developer → Proses debug nirkabel**, nyalakan tombolnya dan centang *«Selalu izinkan di jaringan ini»*.
4. **Pasangkan aplikasi**:
   - Buka **Pixel IMS & APN Tuner** dan izinkan akses Telepon & Notifikasi.
   - Ketuk **«Mulai pemasangan»** — notifikasi akan muncul dan layar Proses debug nirkabel terbuka.
   - Ketuk **«Pasangkan perangkat dengan kode pemasangan»** — muncul **kode 6 digit**.
   - **Jangan tutup kotak kode tersebut**, tarik panel notifikasi ke bawah, ketuk notifikasi aplikasi, ketik 6 angka tersebut, lalu kirim.
5. Kembali ke aplikasi — status berubah menjadi **Siap**. Anda tidak perlu memasangkan ulang lagi!

#### Langkah 2. Terapkan setelan untuk SIM Anda

1. Pada kartu SIM Anda, opsi yang disarankan untuk operator Anda **sudah dicentang otomatis**.
2. Ketuk **«Terapkan setelan»** (atau **«Terapkan ke semua SIM»** untuk Dual-SIM).
3. Dalam beberapa detik, status akan menjadi **«IMS: terdaftar»** beserta daftar fitur yang aktif di sistem.
4. **Jika Anda mengaktifkan VoWiFi:** ketuk **«Buka setelan SIM»** → masuk ke **Panggilan Wi‑Fi** dan pastikan tombolnya menyala.

---

### Penjelasan Lengkap Semua Opsi & Tombol

#### Indikator pada kartu SIM

- **`CARRIER_DB: ...`** — SIM Anda dikenali dalam database 461 operator (`LTE_WIFI` = mendukung VoLTE & VoWiFi; `LTE_ONLY` = hanya VoLTE).
- **`IMS: terdaftar` / `tidak terdaftar`** — konfirmasi langsung bahwa ponsel sudah terhubung ke server suara operator untuk panggilan 4G/5G/Wi‑Fi.
- **`Aktif di sistem: ...`** — menampilkan fitur yang benar-benar sedang aktif di sistem Android.
- **Kotak centang abu-abu `(profil sistem)`** — melindungi fitur yang sudah aktif dari pabrik agar tidak sengaja dimatikan.

#### Opsi utama SIM

| Opsi | Penjelasan mudah |
| :--- | :--- |
| **VoLTE (panggilan 4G)** | Panggilan suara jernih (HD) lewat 4G LTE tanpa turun ke 3G/2G dan internet tetap jalan saat menelepon. **Disarankan selalu aktif.** |
| **VoWiFi (Panggilan Wi‑Fi + ikon bilah status)** | Menelepon dan menerima SMS lewat Wi‑Fi apa pun saat sinyal seluler lemah/hilang (gedung dalam, basement, atau di luar negeri). *Aktifkan jika operator mendukung Wi‑Fi Calling.* |
| **VoNR (panggilan 5G)** | Panggilan suara langsung di jaringan 5G tanpa turun kembali ke 4G. |
| **5G SA + ambang sinyal NR** | Mengaktifkan **5G NSA & 5G SA** di Android (`[1, 2]`), mencegah stack IMS mematikan 5G SA saat tanpa VoNR (`ims.sa_disable_policy_int = 0`), dan menyelaraskan **skala batang sinyal 5G (`SS-RSRP`)** di bilah status ke skala standar 4G LTE (`[-128, -118, -108, -98]` dBm alih-alih `[-110, -90, -80, -65]` dBm yang menampilkan 0 batang di `-111` dBm). *Tidak mengubah sensitivitas fisik modem.* |
| **Profil APN (Internet, IMS, MMS, XCAP) dari CARRIER_DB** | Menulis APN teruji dari Samsung S26 (`Internet`, `MMS`, `IMS` bebas kunci 30 menit di 3G, serta `XCAP`). **Tersimpan permanen walau HP di-restart.** |

#### Bagian «Lanjutan ▸»

| Opsi lanjutan | Kegunaan |
| :--- | :--- |
| **IMS User-Agent Samsung S26 (Shannon)** | Mengenalkan modem ke jaringan operator sebagai **Samsung Galaxy S26** resmi (termasuk 74 profil khusus operator). Mengatasi penolakan VoLTE/VoWiFi pada Pixel. |
| **ViLTE (panggilan video 4G/5G)** | Mengaktifkan panggilan video bawaan langsung dari aplikasi Telepon lewat jaringan seluler. |
| **VoWiFi saat roaming** | Mengizinkan Panggilan Wi‑Fi bekerja saat berada di luar negeri. |
| **Layanan tambahan (UT / XCAP)** | Mengatur pengalihan panggilan & panggilan tunggu langsung lewat 4G/Wi‑Fi tanpa turun ke 2G/3G. |
| **Cross-SIM / Panggilan cadangan** | Pada Dual-SIM, memungkinkan SIM pertama menelepon & menerima SMS **memakai internet data SIM kedua** saat SIM pertama kehilangan sinyal. |
| **Tampilkan ikon LTE+ / 4G+ di semua agregasi** | Menghapus batas tersembunyi Pixel agar ikon `LTE+` / `4G+` tampil jujur setiap kali Carrier Aggregation aktif. |
| **Izinkan menambah APN di pengaturan sistem** | Membuka kunci penambahan dan pengeditan APN manual di setelan Android. |

#### Tombol aksi & «Opsi»

- **«Terapkan setelan»** — menyimpan dan mengaktifkan pilihan untuk SIM tersebut.
- **«Reset»** — menghapus perubahan SIM dan mengembalikan APN bawaan pabrik.
- **«Buka setelan SIM»** — langsung membuka halaman setelan Android untuk SIM tersebut.
- **«Terapkan ke semua SIM»** *(saat 2 SIM aktif)* — mengatur kedua SIM sekaligus.
- **«Terapkan ulang setelah boot»** *(disarankan di Android 16 QPR2+ dan Android 17)* — **VoLTE dasar & semua APN** bersifat permanen; opsi ini menunggu Wi‑Fi setelah HP menyala, membangunkan Proses debug nirkabel secara otomatis bila mati, dan memulihkan fitur `CarrierConfig` (**VoWiFi**, **5G / VoNR**, **User-Agent S26**, **Cross-SIM**) di latar belakang.

---

### Tanya Jawab & Solusi Masalah (FAQ)

<details>
<summary><b>1. Sudah ketuk «Terapkan setelan», tetapi masih tertulis «IMS: tidak terdaftar». Apa yang harus dicek?</b></summary>

1. Pastikan **layanan VoLTE sudah aktif pada nomor Anda dari pihak operator** (beberapa kartu perlu aktivasi VoLTE gratis lewat aplikasi operator atau CS).
2. Pastikan sinyal berada di **4G (LTE) atau 5G**.
3. Jika operator tidak menyediakan VoWiFi, hapus centang *VoWiFi* lalu ketuk *Terapkan setelan* lagi.
4. Nyalakan **Mode pesawat** selama 5–10 detik lalu matikan kembali.
</details>

<details>
<summary><b>2. Apakah setelan hilang setelah HP di-restart (termasuk saat di luar rumah tanpa Wi‑Fi)?</b></summary>

- **VoLTE dasar (panggilan 4G) dan semua APN (`Internet`, `IMS`, `MMS`, `XCAP`) TIDAK hilang saat restart** — bahkan di Android 16 QPR2 / Android 17 dan tanpa Wi‑Fi sekalipun, karena tersimpan permanen di `telephony.db`.
- **Di Android 12–15 dan Android 16 sebelum QPR2**, seluruh konfigurasi tersimpan permanen (`[persistent]`).
- **Di Android 16 QPR2+ dan Android 17**, hanya fitur khusus `CarrierConfig` (**VoWiFi**, **5G / VoNR**, **User-Agent S26**, **Cross-SIM**, **ViLTE**, **ikon LTE+**) yang di-reset saat HP mati dan otomatis pulih dalam hitungan detik begitu terhubung ke Wi‑Fi dikenal (bila *«Terapkan ulang setelah boot»* dicentang).
</details>

<details>
<summary><b>3. Kartu ADB tertulis «Tidak terhubung» padahal dulu sudah memasukkan kode 6 digit. Perlu pasang ulang?</b></summary>

**Tidak perlu!** Cukup sambungkan ke Wi‑Fi yang sudah diizinkan lalu ketuk **«Periksa ulang»** — aplikasi akan otomatis menyalakan kembali Proses debug nirkabel jika sempat mati setelah restart.
</details>

<details>
<summary><b>4. Tertulis «IMS: terdaftar», tetapi Panggilan Wi‑Fi (VoWiFi) belum aktif.</b></summary>

Ketuk **«Buka setelan SIM»** → **Panggilan Wi‑Fi** → aktifkan tombolnya dan pilih preferensi **«Panggil melalui Wi‑Fi»**. Untuk mengetes, nyalakan Mode pesawat, aktifkan Wi‑Fi, lalu coba menelepon.
</details>

<details>
<summary><b>5. Apakah aman untuk m-Banking, Google Wallet, dan update sistem?</b></summary>

**100% aman.** Tanpa Root, tanpa unlock bootloader, dan tanpa koneksi internet keluar (semua berjalan lokal di `127.0.0.1`). Aplikasi bank, dompet digital, dan update OTA tetap berjalan normal.
</details>

<details>
<summary><b>6. Bagaimana cara mengembalikan semuanya ke bawaan pabrik?</b></summary>

Ketuk **«Reset»** pada kartu SIM saat saluran ADB siap, atau hapus aplikasi, restart HP, dan pilih *Setel ulang ke default* pada menu APN Android.
</details>

---

### Solusi Masalah Jatuh ke 3G (Kunci VoLTE 30 Menit) via Bitmask APN `ims`

Tanpa APN `ims` khusus (atau APN manual yang bitmask-nya bernilai `0` = semua jaringan termasuk 2G/3G), saat sinyal sempat turun ke **3G** (lift, basement), Android mencoba menahan `IMS PDN` di 3G, menerima `P-CSCF = []` kosong, lalu `ShannonImsService` memicu `BLOCK_PDN_BY_INVALID_PCSCF = 1` yang mengunci VoLTE & VoWiFi selama **30–90 menit**. `ApnProvisioner` menulis APN `ims` dengan bitmask yang mengecualikan 2G/3G (`network_type_bitmask` & `lingering_network_type_bitmask` = `659456` untuk `LTE|IWLAN|NR` atau `528384` untuk `LTE|NR`, `bearer_bitmask` = `663552` atau `532480`), sehingga IMS berhenti rapi di 3G dan langsung tersambung kembali dalam 1–2 detik saat kembali ke 4G/5G.

---

### Membangun dari Kode Sumber

```sh
./build.sh    # menghasilkan pixel-ims-apn-tuner.apk
```

#### Memperbarui `CARRIER_DB` (`generate-carrier-db.py`)

`res/raw/carrier_db.json` sudah tersedia di repositori. Jika ingin membuatnya ulang dengan [`generate-carrier-db.py`](./generate-carrier-db.py) (Python 3 murni):

<details>
<summary><b>File yang dibutuhkan &amp; cara mendapatkannya</b></summary>

1. **Google Pixel** (`./firmware/pixel/CarrierSettings/`): `carrier_list.pb`, `others.pb`, dan `*.pb` — ambil via ADB tanpa Root dari Pixel (`adb pull /product/etc/CarrierSettings firmware/pixel/CarrierSettings`) atau ekstrak `product.img` dari Factory Image resmi Pixel ([developers.google.com/android/images](https://developers.google.com/android/images)).
2. **Samsung One UI bermodem Exynos/Shannon** (`./firmware/samsung/`): `apns-conf.xml`, `build.prop` (opsional), serta `mnomap.json`, `imsswitch.json`, `imsprofile.json` (di dalam `res/raw/` dari `/system/priv-app/imsservice/imsservice.apk`) — via `adb pull` tanpa Root dari Galaxy Exynos (S24/S25/S26 seri `B`) atau dari arsip firmware resmi (`SM-S942B` menggunakan `Bifrost` / `samloader`).
3. **Jalankan:** `./generate-carrier-db.py --update && ./build.sh` *(folder `./firmware/` diabaikan oleh `.gitignore` — jangan pernah mengunggah file firmware ke Git).*
</details>

---

### Ucapan Terima Kasih & Proyek Rujukan

**Inovasi asli proyek ini:** gagasan untuk mengonfigurasi otomatis **profil APN permanen khusus IMS** (`Internet`, `IMS` dengan perlindungan anti-kunci `invalid P-CSCF` di 3G, `MMS`, `XCAP`) serta membangun database `CARRIER_DB` melalui pencocokan silang antara database operator bawaan Google Pixel dan database firmware Samsung Galaxy S26 (yang memakai keluarga modem Shannon yang sama) adalah **ide dan implementasi orisinal kami sendiri**.

Untuk mekanisme saluran ADB lokal dan `CarrierConfig`, proyek ini juga mengadopsi ide dari komunitas open-source:

- **[charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle)** — arsitektur mandiri loopback Proses debug nirkabel + `UiAutomation` (tanpa Shizuku) dan skrip build `build.sh` tanpa Gradle.
- **[kyujin-cho/pixel-volte-patch](https://github.com/kyujin-cho/pixel-volte-patch)** — pelopor patch `CarrierConfig` Pixel, konsep penggantian `ims.ims_user_agent_string`, flag `UiAutomation` ramah layanan Aksesibilitas, dan penanganan Dual-SIM.
- **[vvb2060/Ims](https://github.com/vvb2060/Ims)** — `Instrumentation` berhak istimewa (`--no-restart`), kunci bypass MMTEL/Entitlement Android 14–17, serta pemberian izin otomatis.
- **[TakaiSaisei/pixel_ims](https://github.com/TakaiSaisei/pixel_ims)** — pemulihan otomatis Proses debug nirkabel (`WRITE_SECURE_SETTINGS` + `adb_wifi_enabled`), terapkan ulang saat boot terikat Wi‑Fi (`JobScheduler`), mode terpisah Android 12–13, dan pembacaan langsung via `ICarrierConfigLoader`.
- **[VinujaHerath/PixelIMS](https://github.com/VinujaHerath/PixelIMS)** — flag `--no-hidden-api-checks`, opsi ViLTE, pintasan setelan SIM, dan penerapan serentak ke semua SIM.
- **[flyfishxu/kadb](https://github.com/flyfishxu/kadb)** & **[LSPosed/HiddenApiBypass](https://github.com/LSPosed/HiddenApiBypass)** — klien ADB over TLS / SPAKE2 tertanam dan akses API tersembunyi Android.

---

## Lisensi

[MIT](./LICENSE) — berbasis [charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle) (© charlie-xing).
