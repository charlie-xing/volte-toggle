# Pixel IMS & APN Tuner

**Language / Мова / Язык / Idioma / 语言:** [English](./README.md) | [Беларуская](./README_BY.md) | [Українська](./README_UA.md) | [Bahasa Indonesia](./README_ID.md) | [Bahasa Melayu](./README_MS.md) | [Deutsch](./README_DE.md) | [Español](./README_ES.md) | [Filipino](./README_FIL.md) | [Français](./README_FR.md) | [Português](./README_PT.md) | [日本語](./README_JA.md) | [**简体中文**](./README_CN.md) | [繁體中文](./README_TW.md) | [한국어](./README_KO.md) | [Русский](./README_RU.md)

**在 Google Pixel 6–11 上一键开启 VoLTE、VoWiFi、VoNR（5G 通话）、5G SA 并自动写入运营商 APN 配置 —— 无需 Root、无需电脑、无需安装第三方 Shizuku。**

📦 下载：**[Releases → pixel-ims-apn-tuner.apk](https://github.com/4plashy-disprized/pixel-ims-apn-tuner/releases/latest)**

---

### 这个应用解决什么问题？

Google Pixel 手机出厂时仅对 Google 认证白名单内的运营商默认开启 VoLTE、Wi‑Fi 通话（VoWiFi）和 5G。当插入中国移动、中国联通、中国电信、中国广电，或港澳及海外非认证地区运营商的 SIM 卡时，通常会遇到以下问题：

- **打电话时断网并回落到 3G / 2G**（在已退网 3G/2G 的地区甚至完全无法拨打或接听电话）。
- 系统 SIM 卡设置里**根本没有「VoLTE 高清通话」「Wi‑Fi 通话」和「5G」开关**。
- 即使使用了普通的 CarrierConfig 补丁，**进出电梯、地下车库或地铁短暂掉到 3G 后，VoLTE 会被锁死 30 分钟**（触发 `invalid P-CSCF` 锁定），或者因为缺少正确的 `ims` / `xcap` / `mms` APN 导致呼叫转移、彩信、VoWiFi 无法使用。
- 部分运营商还会校验手机上报的 SIP `User-Agent` 标识，**拒绝未认证的 Google Pixel 注册 IMS**（尽管 Pixel 搭载的三星 Shannon 基带在硬件上完全兼容）。

**Pixel IMS & APN Tuner** 用一个仅约 800 KB 的独立应用彻底解决以上问题：
1. **内置覆盖 461 家运营商的 `CARRIER_DB` 数据库**（提取自同样采用 Shannon 基带的旗舰 **Samsung Galaxy S26** 固件）：自动写入永久生效的 **Internet、IMS、MMS、XCAP APN**，并通过精确的承载网络掩码防止 3G 下 30 分钟锁死。
2. **一键解锁 VoLTE、VoWiFi、VoNR、5G SA、ViLTE 视频通话与跨卡备份通话**，并支持启用 **Samsung S26 (Shannon) IMS User-Agent**（含 74 家严格运营商的专属模板）。
3. **免 Root、免电脑、免 Shizuku** —— 手机银行、微信/支付宝支付、Google Wallet 以及系统 OTA 升级均不受任何影响。

---

### 快速上手（仅需 2 分钟）

#### 第一步：首次配对（只需做一次）

1. **连接任意 Wi‑Fi 网络**（家里、公司或另一部手机开启的热点均可）。
   > ⚠️ 只有在配置和应用的瞬间需要 Wi‑Fi；开启成功后，出门只用移动数据也能正常使用 VoLTE 和 5G。
2. **开启「开发者选项」** *(若尚未开启)*：
   进入 **设置 → 关于手机**，滑到最底部，**连续点击 7 次「版本号」**，直到提示 *“您现在处于开发者模式！”*。
3. **开启「无线调试」**：
   进入 **设置 → 系统 → 开发者选项 → 无线调试**，打开开关，并在弹窗中勾选 *“始终允许在此网络上调试”*。
4. **在应用内完成配对**：
   - 打开 **Pixel IMS & APN Tuner**，授予电话与通知权限。
   - 点击顶部卡片中的 **「开始配对」** —— 应用会弹出一条通知并自动跳转到「无线调试」页面。
   - 点击 **「使用配对码配对设备」**，屏幕上会显示 **6 位数字配对码**。
   - **保持该弹窗不要关闭**，下拉通知栏，点击应用通知上的 **「输入配对码」**，填入这 6 位数字并发送。
5. 返回应用，顶部 ADB 通道卡片显示 **「已就绪」** 即表示配对完成，以后无需再次配对！

#### 第二步：为 SIM 卡应用设置

1. 在对应的 SIM 卡卡片中，应用**已经根据你的运营商自动勾选了推荐项**。
2. 点击 **「应用设置」**（双卡用户可直接点击底部 **「应用到所有 SIM 卡」**）。
3. 等待 3–5 秒，卡片状态变为 **「IMS：已注册」**，下方会显示实时读取到的 **「系统当前生效：VoLTE, …」**。
4. **如果你勾选了 VoWiFi（Wi‑Fi 通话）：** 点击卡片上的 **「打开 SIM 卡设置」** → 进入 **「WLAN 通话 / Wi‑Fi 通话」**，确认开关已打开即可。

---

### 全部选项与按钮详细说明

#### SIM 卡卡片上的状态说明

- **`CARRIER_DB: ...`** —— 已在内置的 461 家运营商数据库中匹配到你的 SIM 卡（`LTE_WIFI` 表示该运营商同时支持 VoLTE 与 VoWiFi；`LTE_ONLY` 表示仅支持 VoLTE）。
- **`不在 CARRIER_DB 中（保留 Pixel 默认 APN）`** —— 数据库未收录或该卡已获 Google 原生认证；原有 APN 不会被改动，你依然可以自由勾选开启各项 IMS / 5G 功能。
- **`IMS：已注册` / `未注册`** —— 最核心的成功标志。显示 **「已注册」** 代表手机已连上运营商语音核心网，可正常进行 4G/5G/Wi‑Fi 高清通话。
- **`系统当前生效：...`** —— 直接从 Android 系统 `CarrierConfigLoader` 读取当前真实生效的功能项。
- **灰色锁定的 `(系统配置)` 复选框** —— 当某项功能在 Google 原厂配置中原本就已开启时，应用会自动保护它不被误关，同时允许你补齐缺失的功能（如 5G 或 VoWiFi）。

#### 主要开关说明

| 选项 | 通俗解释与开启建议 |
| :--- | :--- |
| **VoLTE（4G 通话）** | 通过 4G LTE 网络进行高清语音通话，接通快、不断网、不回落 2G/3G。**强烈建议始终开启。** |
| **VoWiFi（Wi‑Fi 通话 + 状态栏图标）** | 在地下室、室内弱信号区或境外漫游时，只要连上 Wi‑Fi 就能按普通电话拨打/接听语音和收发短信。*仅在运营商支持 VoWiFi 时开启（数据库内运营商已自动按需勾选）。* |
| **VoNR（5G 通话）** | 通话时直接驻留在 5G 网络，无需回落到 4G。 |
| **5G SA + NR 信号阈值** | 在 Android 侧同时启用 **5G NSA 与 5G SA**（`[1, 2]`），防止未开启 VoNR 时 IMS 协议栈自动关闭 5G SA（`ims.sa_disable_policy_int = 0`），并将状态栏 **5G 信号格数显示门限（`SS-RSRP`）** 对齐到标准 4G LTE 刻度（由 `-111` dBm 即显示 0 格的原厂 `[-110, -90, -80, -65]` dBm 调整为 `[-128, -118, -108, -98]` dBm）。*不会改变基带硬件的物理接收灵敏度。* |
| **来自 CARRIER_DB 的 APN 配置文件（Internet、IMS、MMS、XCAP）** | 自动写入源自三星 S26 的权威运营商 APN（含移动上网 `Internet`、彩信 `MMS`、防止 3G 下锁死 30 分钟的 `IMS` 以及补充业务 `XCAP`）。**永久保存在系统中，重启手机也不会丢失。** |

#### 「高级选项 ▸」（点击展开）

| 高级选项 | 作用说明 |
| :--- | :--- |
| **Samsung S26 (Shannon) IMS User-Agent** | 将基带向运营商上报的 SIP `User-Agent` 伪装为经过认证的 **三星 Galaxy S26**（含 74 家严格运营商的专属格式）。可解决部分运营商拦截非认证 Pixel 手机注册 IMS 的问题。*非 Google 原生认证运营商默认开启。* |
| **ViLTE（4G/5G 视频通话）** | 开启系统自带拨号盘的运营商原生视频通话功能（需运营商支持）。 |
| **漫游时启用 VoWiFi** | 允许在境外漫游时使用 Wi‑Fi 通话，并解锁漫游网络偏好选项。 |
| **补充业务 (UT / XCAP)** | 允许在手机通话设置里直接通过 4G/Wi‑Fi 设置**呼叫转移、呼叫等待、呼叫限制**，避免因回落 2G/3G 失败而报错。 |
| **跨卡通话 / 备份通话** | 双卡实用功能（Backup Calling）：当主卡无信号（例如在境外关闭漫游的国内卡）时，**直接借用副卡（如境外流量卡/eSIM）的移动数据流量来接打电话和收发短信**，效果等同于连着 Wi‑Fi！ |
| **显示 LTE+ / 4G+ 图标（不限聚合带宽）** | 移除 Pixel 隐藏的带宽门槛，只要基带开启了载波聚合（CA），状态栏就如实显示 `LTE+` / `4G+` 图标（纯显示优化，不影响网速与耗电）。 |
| **允许在系统设置中添加 APN** | 解除部分运营商配置对手动新建、修改 APN 的锁定。 |

#### 操作按钮与底部「选项」

- **「应用设置」** —— 将当前勾选的设置写入该 SIM 卡并立即触发 IMS 重新注册。
- **「重置」** —— 清除该 SIM 卡的所有覆盖项，并恢复出厂默认 APN 与 Google 原厂配置。
- **「打开 SIM 卡设置」** —— 一键直达该 SIM 卡的 Android 系统设置页面。
- **「应用到所有 SIM 卡」** *(双卡激活时显示)* —— 一键为两张 SIM 卡同时应用设置。
- **「开机后自动重新应用」** *(推荐在 Android 16 QPR2+ 与 Android 17 上开启)* —— 基础 **VoLTE 与所有 APN 配置本身就是永久生效的**；而受无 Root 系统限制在关机时清空的 `CarrierConfig` 扩展项（**VoWiFi、5G / VoNR、S26 User-Agent、跨卡通话**等），会在开机连上已信任的 Wi‑Fi 后由本功能自动唤醒无线调试并在后台静默恢复。
- **右上角「语言」按钮** —— 随时在 15 种界面语言之间切换。

---

### 常见问题排查 (FAQ)

<details>
<summary><b>1. 点击「应用设置」后依然显示「IMS：未注册」，该检查什么？</b></summary>

1. **确认手机号码已在运营商侧开通 VoLTE 高清通话业务**（移动/联通/电信均可在官方 App 或发送短信免费开通）。
2. 确认手机当前信号处于 **4G (LTE) 或 5G**，未锁定在仅 3G/2G。
3. 若运营商不支持 VoWiFi（如中国大陆三大运营商目前多数地区仅提供 VoLTE），请取消勾选 *VoWiFi* 后再点一次 *应用设置*。
4. 开关一次 **飞行模式**（等待 5–10 秒后关闭）让基带重新附着基站。
</details>

<details>
<summary><b>2. 手机重启后设置会失效吗？（比如在外面没有 Wi‑Fi 时重启）</b></summary>

- **基础 VoLTE（4G 高清通话）和所有写入的 APN（`Internet`、`IMS`、`MMS`、`XCAP`）在重启后不会失效！** 即使在最新的 Android 16 QPR2 / Android 17 上、即使在户外没有任何 Wi‑Fi，因为它们直接写入了系统永久数据库 `telephony.db`，绝大多数运营商开机即可正常使用 4G VoLTE 通话和上网。
- **在 Android 12–15 及 Android 16 (QPR2 之前)** 系统上，所有配置均为完全永久生效（日志显示 `[persistent]`）。
- **在 Android 16 QPR2+ 与 Android 17** 上，仅纯 `CarrierConfig` 扩展功能（**VoWiFi、5G / VoNR、S26 User-Agent、跨卡通话、ViLTE、LTE+ 图标**）会在重启时暂时还原；只要勾选了 **「开机后自动重新应用」**，手机一连上熟悉的 Wi‑Fi（在外也可临时连一下热点）就会在几秒内自动静默恢复。
</details>

<details>
<summary><b>3. 之前已经输过 6 位配对码了，为什么 ADB 卡片显示「未连接」？需要重新配对吗？</b></summary>

**不需要重新配对！** 配对密钥早已保存在系统中。只需连上之前允许过的 Wi‑Fi 网络并点击 **「重新检测」** —— 如果系统在重启后自动关掉了无线调试开关，本应用会自动将它重新唤醒并连接。
</details>

<details>
<summary><b>4. 显示「IMS：已注册」，VoLTE 正常，但 Wi‑Fi 通话（VoWiFi）没有生效？</b></summary>

点击卡片上的 **「打开 SIM 卡设置」** → **WLAN 通话 / Wi‑Fi 通话** → 打开开关并将偏好设置为 **「优先使用 WLAN 网络通话」**。测试方法：打开飞行模式，再单独打开 Wi‑Fi 拨打一通电话即可验证。
</details>

<details>
<summary><b>5. 安全吗？会影响银行 App、微信/支付宝、Google Wallet 或系统更新吗？</b></summary>

**完全安全。** 无需解锁 Bootloader，无需 Root，不修改任何系统分区，且应用完全不联网（仅通过手机内部回环地址 `127.0.0.1` 通信）。所有银行 App、移动支付、Google Wallet 和官方 OTA 升级均正常使用。
</details>

<details>
<summary><b>6. 如何彻底恢复到出厂默认状态？</b></summary>

在 ADB 通道显示「已就绪」时点击 SIM 卡卡片上的 **「重置」** 即可一键还原；或者直接卸载应用并重启手机，然后在系统 *接入点名称 (APN)* 菜单右上角选择 *重置为默认设置*。
</details>

---

### 如何通过 `ims` APN 位掩码解决回落 3G 后锁死 30 分钟的问题

在未认证运营商下，Pixel 原厂缺少专用 `ims` APN（或在系统界面手动新建时隐藏网络掩码保持为 `0` = 允许包含 2G/3G 在内的所有网络）。当手机进出电梯、地铁或地库短暂掉到 **3G** 时，Android 尝试在 3G 上建立/维持 `IMS PDN`，收到空地址 `P-CSCF = []`，从而触发 `ShannonImsService` 的 `BLOCK_PDN_BY_INVALID_PCSCF = 1` 惩罚机制（**封锁 30 分钟活跃 CPU 时间，息屏下可达 30–90 分钟**），即使回到满格 4G/5G 或连上 Wi‑Fi 也无法恢复。本应用的 `ApnProvisioner` 在写入 `ims` APN 时同步设置彻底排除 2G/3G 的位掩码（`LTE_WIFI`：`network_type_bitmask` 与 `lingering_network_type_bitmask = 659456`、`bearer_bitmask = 663552`；`LTE_ONLY`：`528384`、`532480`），使手机掉入 3G 时干净挂起 IMS，重回 4G/5G 后 1–2 秒即刻恢复注册。

---

### 从源码构建

```sh
./build.sh    # 生成 pixel-ims-apn-tuner.apk
```

#### 更新运营商数据库 (`generate-carrier-db.py`)

仓库中已内置生成好的 `res/raw/carrier_db.json`，普通编译 APK 无需任何固件文件。如果你想针对新版固件使用 [`generate-carrier-db.py`](./generate-carrier-db.py)（纯标准 Python 3，零第三方依赖）自行重新生成数据库：

<details>
<summary><b>脚本所需的输入文件与获取方式</b></summary>

1. **Google Pixel 文件**（`./firmware/pixel/CarrierSettings/`）：`carrier_list.pb`、`others.pb` 及所有 `*.pb` —— 可免 Root 直接通过 USB ADB 从任意 Pixel 手机提取（`adb pull /product/etc/CarrierSettings firmware/pixel/CarrierSettings`），或从 Google 官方工厂镜像（[developers.google.com/android/images](https://developers.google.com/android/images)）解包 `product.img` 获取。
2. **Samsung One UI（猎户座 Exynos / Shannon 基带机型）文件**（`./firmware/samsung/`）：`/system/etc/apns-conf.xml`、可选的 `/system/build.prop`，以及 `/system/priv-app/imsservice/imsservice.apk` 内 `res/raw/` 目录下的 `mnomap.json`、`imsswitch.json`、`imsprofile.json` —— 可免 Root 直接从猎户座版三星 Galaxy（如 S24/S25/S26 `B` 版）执行 `adb pull` 并 `unzip` 提取，或使用 `Bifrost` / `samloader` 下载官方固件（如 `SM-S942B`）解包 `system.img` 提取。
3. **运行生成并重新打包：** `./generate-carrier-db.py --update && ./build.sh` *（`./firmware/` 已加入 `.gitignore`，切勿将原始固件文件提交到 Git 仓库）。*
</details>

---

### 致谢与思路来源项目

**本项目原创核心设计：** 自动写入面向 IMS 的永久 **APN 配置文件**（`Internet`、带 3G `invalid P-CSCF` 防锁死保护的 `IMS`、`MMS`、`XCAP`），以及将 Google Pixel 原厂运营商库与同属 Shannon 基带家族的 Samsung Galaxy S26 固件运营商库进行交叉比对生成 `CARRIER_DB` 数据库，均属于**我们自己的原创思路与独立实现**。

在本地 ADB 通道与 `CarrierConfig` 覆盖机制方面，本项目也借鉴并融合了以下开源项目的思路：

- **[charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle)** —— 免 Shizuku 的本机回环无线调试 + `UiAutomation` 独立架构与免 Gradle 的 `build.sh` 构建流程。
- **[kyujin-cho/pixel-volte-patch](https://github.com/kyujin-cho/pixel-volte-patch)** —— Pixel `CarrierConfig` 补丁先驱、`ims.ims_user_agent_string` 覆盖思路、不中断无障碍服务的 `UiAutomation` 标志以及双卡 IMS 状态处理。
- **[vvb2060/Ims](https://github.com/vvb2060/Ims)** —— 特权 `Instrumentation`（`--no-restart`）、Android 14–17 MMTEL/Entitlement 鉴权绕过键值与运行时权限自动授予。
- **[TakaiSaisei/pixel_ims](https://github.com/TakaiSaisei/pixel_ims)** —— 休眠无线调试自动唤醒（`WRITE_SECURE_SETTINGS` + `adb_wifi_enabled`）、绑定 Wi‑Fi 的 `JobScheduler` 开机自动重放、Android 12–13 分离式执行及 `ICarrierConfigLoader` 实时状态读取。
- **[VinujaHerath/PixelIMS](https://github.com/VinujaHerath/PixelIMS)** —— `--no-hidden-api-checks` 参数、独立 ViLTE 开关、SIM 卡系统设置快捷跳转及多卡一键应用。
- **[flyfishxu/kadb](https://github.com/flyfishxu/kadb)** 与 **[LSPosed/HiddenApiBypass](https://github.com/LSPosed/HiddenApiBypass)** —— 内置 ADB over TLS / SPAKE2 客户端与 Android 隐藏 API 调用支持。

---

## 开源许可

[MIT](./LICENSE) —— 基于 [charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle) (© charlie-xing) 构建。
