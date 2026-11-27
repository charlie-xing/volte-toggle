# Pixel IMS & APN Tuner

**Language / Мова / Язык / Idioma / 语言:** [English](./README.md) | [Беларуская](./README_BY.md) | [Українська](./README_UA.md) | [Bahasa Indonesia](./README_ID.md) | [Bahasa Melayu](./README_MS.md) | [Deutsch](./README_DE.md) | [Español](./README_ES.md) | [Filipino](./README_FIL.md) | [Français](./README_FR.md) | [Português](./README_PT.md) | [日本語](./README_JA.md) | [简体中文](./README_CN.md) | [**繁體中文**](./README_TW.md) | [한국어](./README_KO.md) | [Русский](./README_RU.md)

**在 Google Pixel 6–11 上一鍵開啟 VoLTE、VoWiFi、VoNR（5G 通話）、5G SA 並自動寫入電信業者 APN 設定檔 —— 免 Root、免電腦、免安裝外部 Shizuku。**

📦 下載：**[Releases → pixel-ims-apn-tuner.apk](https://github.com/4plashy-disprized/pixel-ims-apn-tuner/releases/latest)**

---

### 這個應用程式解決什麼問題？

Google Pixel 手機出廠時，僅針對 Google 認證白名單內的電信業者預設開啟 VoLTE、Wi‑Fi 通話（VoWiFi）與 5G。當使用香港（CSL、1O1O、SmarTone、3HK、CMHK、Club SIM、SoSIM）、澳門、台灣漫遊卡或海外未認證地區的 SIM / eSIM 時，常會遇到以下問題：

- **通話時行動網路斷線並降回 3G / 2G**（在已關閉 3G 網路的地區甚至無法撥打或接聽電話）。
- SIM 卡設定裡**完全找不到「VoLTE」「Wi‑Fi 通話」與「5G」開關**。
- 即使使用一般的 CarrierConfig 修補工具，**進出電梯、地鐵或地下室短暫跳到 3G 後，VoLTE 會被鎖死 30 分鐘**（觸發 `invalid P-CSCF` 封鎖），或因缺少正確的 `ims` / `xcap` / `mms` APN 導致來電轉接、MMS、VoWiFi 無法運作。
- 部分電信業者還會檢查 SIP `User-Agent` 標頭，**拒絕未認證的 Google Pixel 註冊 IMS**（儘管 Pixel 內建的三星 Shannon 數據機硬體完全相容）。

**Pixel IMS & APN Tuner** 透過單一輕量應用程式（約 800 KB）一次解決所有問題：
1. **內建涵蓋 461 家電信業者的 `CARRIER_DB` 資料庫**（擷取自同樣搭載 Shannon 數據機的 **Samsung Galaxy S26** 韌體）：自動寫入永久保存的 **Internet、IMS、MMS、XCAP APN**，並設定正確的網路遮罩防止 3G 鎖死。
2. **解鎖 VoLTE、VoWiFi、VoNR、5G SA、ViLTE 視訊通話與跨卡備援通話**，並支援套用 **Samsung S26 (Shannon) IMS User-Agent**（包含 CSL HK 等 74 家嚴格業者的專屬格式）。
3. **免 Root、免電腦、免 Shizuku** —— 網銀 App、Google 錢包（感應支付）與 Android OTA 系統更新皆正常運作。

---

### 快速入門（只需 2 分鐘）

#### 步驟 1：首次配對（只需做一次）

1. **連上任意 Wi‑Fi 網路**（住家、公司或另一支手機分享的熱點皆可）。
   > ⚠️ 只有在設定與套用的當下需要 Wi‑Fi；套用完成後，外出只用行動數據也能正常使用 VoLTE 與 5G。
2. **開啟「開發人員選項」** *(若尚未開啟)*：
   前往 **設定 → 關於手機**，滑到最下方，**連續點擊 7 下「版本號碼」**，直到出現 *「你現在已成為開發人員！」*。
3. **開啟「無線偵錯」**：
   前往 **設定 → 系統 → 開發人員選項 → 無線偵錯**，打開開關並勾選 *「一律允許透過這個網路進行偵錯」*。
4. **在應用程式中完成配對**：
   - 開啟 **Pixel IMS & APN Tuner**，授予電話與通知權限。
   - 點擊上方卡片的 **「開始配對」** —— 系統會跳出一則通知並開啟「無線偵錯」畫面。
   - 點擊 **「使用配對碼配對裝置」**，畫面上會顯示 **6 位數配對碼**。
   - **保持該對話框開啟**，下拉通知欄，點擊通知上的 **「輸入配對碼」**，填入 6 位數字並傳送。
5. 回到應用程式，ADB 通道顯示 **「已就緒」** 即代表完成，之後再也不必重新配對！

#### 步驟 2：為 SIM 卡套用設定

1. 在 SIM 卡卡片中，應用程式**已經依據你的電信業者自動勾選好建議選項**。
2. 點擊 **「套用設定」**（雙卡使用者可直接點 **「套用到所有 SIM 卡」**）。
3. 等待 3–5 秒，狀態會變為 **「IMS：已註冊」**，下方同時顯示 **「系統目前生效：VoLTE, …」**。
4. **若已啟用 VoWiFi（Wi‑Fi 通話）：** 點擊卡片上的 **「開啟 SIM 卡設定」** → 進入 **「Wi‑Fi 通話」**，確認開關已開啟並選擇偏好模式。

---

### 所有選項與按鈕完整說明

#### SIM 卡卡片狀態說明

- **`CARRIER_DB: ...`** —— 已在內建 461 家電信業者資料庫中找到你的 SIM 卡（`LTE_WIFI` = 同時支援 VoLTE 與 VoWiFi；`LTE_ONLY` = 僅支援 VoLTE）。
- **`不在 CARRIER_DB 中（保留 Pixel 預設 APN）`** —— 資料庫未收錄或該業者已受 Google 原生支援；既有 APN 保持不變，仍可自由開啟各項 IMS / 5G 選項。
- **`IMS：已註冊` / `未註冊`** —— 最重要的成功指標。顯示 **「已註冊」** 代表手機已連上電信業者的語音伺服器，可進行 4G/5G/Wi‑Fi 通話。
- **`系統目前生效：...`** —— 直接從 Android `CarrierConfigLoader` 讀取目前實際生效的系統旗標。
- **灰色鎖定的 `(系統設定檔)` 核取方塊** —— 保護原廠已支援的功能不被誤關，同時讓你補齊缺少的功能（例如 5G 或 VoWiFi）。

#### 主要選項

| 選項 | 白話說明與建議 |
| :--- | :--- |
| **VoLTE（4G 通話）** | 透過 4G LTE 進行高音質（HD Voice）通話，秒撥接通、通話不斷網、不降回 3G/2G。**建議永遠開啟。** |
| **VoWiFi（Wi‑Fi 通話 + 狀態列圖示）** | 在室內死角、地下室或出國旅遊時，只要連上 Wi‑Fi 就能撥接一般電話與收發簡訊。*電信業者支援 Wi‑Fi 通話時建議開啟。* |
| **VoNR（5G 通話）** | 通話時直接維持在 5G 網路，不降回 4G。 |
| **5G SA + NR 訊號閾值** | 在 Android 端同時啟用 **5G NSA 與 5G SA**（`[1, 2]`），防止未啟用 VoNR 時 IMS 協定堆疊自動關閉 5G SA（`ims.sa_disable_policy_int = 0`），並將狀態列 **5G 訊號格數顯示門檻（`SS-RSRP`）** 對齊至標準 4G LTE 刻度（由 `-111` dBm 就顯示 0 格的原廠 `[-110, -90, -80, -65]` dBm 調整為 `[-128, -118, -108, -98]` dBm）。*不會改變數據機硬體的物理接收靈敏度。* |
| **來自 CARRIER_DB 的 APN 設定檔（Internet、IMS、MMS、XCAP）** | 自動寫入來自三星 S26 的驗證 APN（含上網 `Internet`、`MMS`、防止 3G 鎖死 30 分鐘的 `IMS` 與通話設定用的 `XCAP`）。**永久儲存於系統，重新開機也不會消失。** |

#### 「進階選項 ▸」（點擊展開）

| 進階選項 | 用途說明 |
| :--- | :--- |
| **Samsung S26 (Shannon) IMS User-Agent** | 將數據機回報給電信業者的 SIP `User-Agent` 改為認證過的 **Samsung Galaxy S26**（含 74 家業者的專屬格式）。解決電信業者阻擋未認證 Pixel 註冊的問題。*非 Google 原廠認證業者預設開啟。* |
| **ViLTE（4G/5G 視訊通話）** | 啟用內建「電話」撥號程式的電信原生視訊通話（需業者支援）。 |
| **漫遊時啟用 VoWiFi** | 允許在國外漫遊時使用 Wi‑Fi 通話，並開放漫遊偏好設定。 |
| **補充服務 (UT / XCAP)** | 可直接透過 4G/Wi‑Fi 在手機選單設定**來電轉接、插播（通話等待）與通話限制**，不必降回 2G/3G。 |
| **跨卡通話 / 備援通話** | 雙卡實用功能（Backup Calling）：當主卡沒有訊號（例如在國外關閉漫遊的原門號）時，可**直接借用副卡（旅遊上網 eSIM）的行動數據來撥接電話與收發簡訊**！ |
| **顯示 LTE+ / 4G+ 圖示（不限聚合頻寬）** | 移除 Pixel 隱藏的頻寬門檻，只要載波聚合（CA）運作中就如實顯示 `LTE+` / `4G+` 圖示（純視覺顯示，不影響網速與耗電）。 |
| **允許在系統設定中新增 APN** | 解除部分電信業者對手動新增、編輯 APN 的鎖定。 |

#### 操作按鈕與底部「選項」

- **「套用設定」** —— 儲存並寫入該 SIM 卡的設定，立即重新註冊 IMS。
- **「重設」** —— 清除該 SIM 卡的所有覆寫，並還原原廠預設 APN 與 Google 設定檔。
- **「開啟 SIM 卡設定」** —— 直接跳轉至該 SIM 卡的 Android 系統設定頁。
- **「套用到所有 SIM 卡」** *(雙卡啟用時顯示)* —— 一鍵套用到兩張 SIM 卡。
- **「開機後自動重新套用」** *(建議於 Android 16 QPR2+ 與 Android 17 開啟)* —— 基礎 **VoLTE 與所有 APN 皆為永久保存**；而關機時會重設的 `CarrierConfig` 進階項目（**VoWiFi、5G / VoNR、S26 User-Agent、跨卡通話**等），會在重開機連上熟識的 Wi‑Fi 時自動喚醒無線偵錯並在背景靜默還原。
- **右上角「語言」按鈕** —— 隨時切換 15 種介面語言。

---

### 常見問題與疑難排解 (FAQ)

<details>
<summary><b>1. 點了「套用設定」卻還是顯示「IMS：未註冊」，該檢查什麼？</b></summary>

1. **確認你的門號已在電信業者端開通 VoLTE 服務**（部分門號或儲值卡需先透過電信 App 或客服免費開通）。
2. 確認手機目前處於 **4G (LTE) 或 5G** 訊號下。
3. 若電信業者不支援 VoWiFi，請取消勾選 *VoWiFi* 後再點一次 *套用設定*。
4. 開關一次 **飛航模式**（等待 5–10 秒再關閉）。
</details>

<details>
<summary><b>2. 重新開機後設定會消失嗎？（例如在戶外沒有 Wi‑Fi 時重開機）</b></summary>

- **基礎 VoLTE（4G 通話）與所有 APN（`Internet`、`IMS`、`MMS`、`XCAP`）在重新開機後不會消失！** 即使在最新的 Android 16 QPR2 / Android 17 上、即使在戶外沒有 Wi‑Fi，因為它們直接寫入永久資料庫 `telephony.db`，絕大多數電信業者開機後即可直接使用 4G 通話與上網。
- **在 Android 12–15 及 Android 16 (QPR2 之前)** 上，所有設定皆完全永久保存（記錄顯示 `[persistent]`）。
- **在 Android 16 QPR2+ 與 Android 17** 上，僅純 `CarrierConfig` 項目（**VoWiFi、5G / VoNR、S26 User-Agent、跨卡通話、ViLTE、LTE+ 圖示**）會在關機時暫時重設；只要勾選 **「開機後自動重新套用」**，連上信任的 Wi‑Fi 後數秒內就會自動恢復。
</details>

<details>
<summary><b>3. 之前已經配對過 6 位數密碼，但 ADB 卡片顯示「未連線」，需要重新配對嗎？</b></summary>

**不需要！** 只要連上先前允許過的 Wi‑Fi 網路並點擊 **「重新偵測」** —— 若系統在重開機後自動關閉了無線偵錯，應用程式會自動將它重新開啟並連線。
</details>

<details>
<summary><b>4. 顯示「IMS：已註冊」，但 Wi‑Fi 通話（VoWiFi）沒有運作？</b></summary>

點擊 **「開啟 SIM 卡設定」** → **Wi‑Fi 通話** → 開啟開關並將偏好設為 **「透過 Wi‑Fi 通話」**。測試方式：開啟飛航模式後再單獨打開 Wi‑Fi 撥打電話即可驗證。
</details>

<details>
<summary><b>5. 對 Google 錢包（NFC 感應支付）、網銀 App 與系統更新安全嗎？</b></summary>

**100% 安全。** 不解鎖 Bootloader、不 Root、不修改系統磁區，且完全不連外網（僅透過本機 `127.0.0.1` 迴路運作）。Google 錢包、銀行 App 與官方 OTA 更新皆正常運作。
</details>

<details>
<summary><b>6. 如何完整還原回原廠狀態？</b></summary>

在 ADB 顯示「已就緒」時點擊 SIM 卡上的 **「重設」**，或直接解除安裝應用程式並重新開機，再到 Android 的 *存取點名稱 (APN)* 選單點選 *重設為預設值*。
</details>

---

### 如何透過 `ims` APN 位元遮罩解決掉回 3G 後鎖死 30 分鐘的問題

在未認證電信業者下，Pixel 原廠缺少專用 `ims` APN（或在系統選單手動新增時隱藏網路遮罩維持為 `0` = 允許包含 2G/3G 的所有網路）。當手機進出電梯、地鐵或地下室短暫降回 **3G** 時，Android 會嘗試在 3G 上維持 `IMS PDN`，收到空位址 `P-CSCF = []`，進而觸發 `ShannonImsService` 的 `BLOCK_PDN_BY_INVALID_PCSCF = 1` 懲罰機制（**封鎖 30 分鐘活躍 CPU 時間，待機下可達 30–90 分鐘**），即使回到滿格 4G/5G 或連上 Wi‑Fi 也無法恢復。本程式的 `ApnProvisioner` 在寫入 `ims` APN 時同步設定完全排除 2G/3G 的位元遮罩（`LTE_WIFI`：`network_type_bitmask` 與 `lingering_network_type_bitmask = 659456`、`bearer_bitmask = 663552`；`LTE_ONLY`：`528384`、`532480`），讓手機掉入 3G 時正常暫停 IMS，重回 4G/5G 後 1–2 秒立即恢復註冊。

---

### 從原始碼建置

```sh
./build.sh    # 產生 pixel-ims-apn-tuner.apk
```

#### 更新電信業者資料庫 (`generate-carrier-db.py`)

儲存庫已內建產生好的 `res/raw/carrier_db.json`，一般建置 APK 不需要任何韌體檔案。若你想透過 [`generate-carrier-db.py`](./generate-carrier-db.py)（純標準 Python 3，免安裝第三方套件）自行重新產生資料庫：

<details>
<summary><b>腳本所需的檔案與取得方式</b></summary>

1. **Google Pixel 檔案**（`./firmware/pixel/CarrierSettings/`）：`carrier_list.pb`、`others.pb` 與所有 `*.pb` —— 可免 Root 透過 ADB 直接從 Pixel 手機擷取（`adb pull /product/etc/CarrierSettings firmware/pixel/CarrierSettings`），或從 Google 官方韌體映像檔（[developers.google.com/android/images](https://developers.google.com/android/images)）解開 `product.img` 取得。
2. **Samsung One UI（Exynos / Shannon 數據機機型）檔案**（`./firmware/samsung/`）：`/system/etc/apns-conf.xml`、選用的 `/system/build.prop`，以及 `/system/priv-app/imsservice/imsservice.apk` 內 `res/raw/` 中的 `mnomap.json`、`imsswitch.json`、`imsprofile.json` —— 可免 Root 從 Exynos 版三星 Galaxy（如 S24/S25/S26 `B` 型號）透過 `adb pull` + `unzip` 擷取，或用 `Bifrost` / `samloader` 下載官方韌體（如 `SM-S942B`）解開 `system.img` 取得。
3. **執行產生並重新建置：** `./generate-carrier-db.py --update && ./build.sh` *（`./firmware/` 已列入 `.gitignore`，請勿將原始韌體檔案提交至 Git）。*
</details>

---

### 致謝與參考專案

**本專案原創核心設計：** 自動寫入專為 IMS 最佳化的永久 **APN 設定檔**（`Internet`、防止 3G `invalid P-CSCF` 鎖死的 `IMS`、`MMS`、`XCAP`），以及將 Google Pixel 原廠電信業者資料庫與同屬 Shannon 數據機家族的 Samsung Galaxy S26 韌體資料庫交叉比對建構出 `CARRIER_DB`，皆為**我們自己的原創構想與獨立實作**。

在本機 ADB 通道與 `CarrierConfig` 覆寫機制方面，本專案亦借鑑了以下開源專案的設計：

- **[charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle)** —— 免 Shizuku 的本機迴路無線偵錯 + `UiAutomation` 獨立架構與免 Gradle 的 `build.sh` 建置流程。
- **[kyujin-cho/pixel-volte-patch](https://github.com/kyujin-cho/pixel-volte-patch)** —— Pixel `CarrierConfig` 修補先驅、`ims.ims_user_agent_string` 覆寫概念、不中斷無障礙服務的 `UiAutomation` 旗標以及雙卡 IMS 處理。
- **[vvb2060/Ims](https://github.com/vvb2060/Ims)** —— 特權 `Instrumentation`（`--no-restart`）、Android 14–17 MMTEL/Entitlement 驗證繞過鍵值與自動授予執行期權限。
- **[TakaiSaisei/pixel_ims](https://github.com/TakaiSaisei/pixel_ims)** —— 無線偵錯自動喚醒（`WRITE_SECURE_SETTINGS` + `adb_wifi_enabled`）、綁定 Wi‑Fi 的 `JobScheduler` 開機自動套用、Android 12–13 分離式執行及 `ICarrierConfigLoader` 即時狀態讀取。
- **[VinujaHerath/PixelIMS](https://github.com/VinujaHerath/PixelIMS)** —— `--no-hidden-api-checks` 參數、獨立 ViLTE 開關、SIM 卡系統設定捷徑與多卡一鍵套用。
- **[flyfishxu/kadb](https://github.com/flyfishxu/kadb)** 與 **[LSPosed/HiddenApiBypass](https://github.com/LSPosed/HiddenApiBypass)** —— 內建 ADB over TLS / SPAKE2 用戶端與 Android 隱藏 API 存取支援。

---

## 授權條款

[MIT](./LICENSE) —— 基於 [charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle) (© charlie-xing) 開發。
