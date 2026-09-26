# VoLTE Toggle (com.xcl.volte)

[English](#english) | [中文](#中文)

**Enable VoLTE / VoNR / 5G SA for mainland China carrier SIMs (China Mobile, China Unicom, China Telecom) on Google Pixel. No root, no PC.**

**在 Google Pixel 上一键为中国大陆运营商卡（移动 / 联通 / 电信）开启 VoLTE / VoNR / 5G SA。不需要 root，不需要电脑。**

📦 Download / 下载：[Releases → volte-toggle.apk](https://github.com/charlie-xing/volte-toggle/releases/latest)

---

## English

### The problem

Google Pixel phones usually ship without VoLTE / VoNR enabled in the carrier config for China Mobile, China Unicom and China Telecom. Symptoms:
- Calls drop to 3G/2G, or fail on 4G/5G.
- There's no VoLTE / HD icon.
- You can't make calls on 5G.

These switches live in the system's **CarrierConfig**. Normally only the system or the carrier can change them. This app uses a local debugging channel from **the phone to itself** to override them with limited, debug-level privileges.

### How it works

- Android doesn't let regular third-party apps write carrier config. That needs system-level permission such as `MODIFY_PHONE_STATE`.
- The app bundles an ADB client (kadb). It connects to the phone itself over the **Wireless debugging loopback port** and launches the app's own instrumentation with shell privileges.
- The instrumentation runs **in the app's own process**. It uses `UiAutomation` to temporarily adopt shell permissions, then calls `CarrierConfigManager.overrideConfig(subId, bundle)`.
- The override is **non-persistent**, because that's the only mode Android allows for non-system callers. So it's **lost on reboot** and must be re-applied. That's what the "Re-apply after boot" option is for.
- Afterwards it calls `TelephonyManager.resetIms(slot)`, so IMS re-registers immediately and the VoLTE icon appears.

**Keys written**
- VoLTE switches: `carrier_volte_available_bool`, `enhanced_4g_lte_on_by_default_bool`, `carrier_volte_provisioned_bool` and related keys.
- Optional:
  - `vonr_enabled_bool` for VoNR (5G calling)
  - `carrier_nr_availabilities_int_array` for 5G SA
  - the four VoWiFi keys

Turning a SIM off clears the override and restores the carrier defaults.

### Usage

#### ① First-time setup (once only)

1. Turn on **Wi-Fi and connect to a network**.
   > ⚠️ Wireless debugging only works while Wi-Fi is connected. This step is required.
2. Enable Developer options: **Settings → About phone → tap "Build number" 7 times** until you see "You are now a developer".
3. Enable Wireless debugging: **Settings → System → Developer options → Wireless debugging**. Turn it on and keep it on.
4. Back in the app, tap **Start pairing** in the ADB channel card. Enter the **6-digit pairing code** shown by the system into the notification and send it.
   Once it shows **Ready**, pairing is done. The key is remembered, so you don't need to pair again.

#### ② Daily use

1. Make sure the ADB channel shows **Ready**. If it's unavailable, Wi-Fi or Wireless debugging is usually off.
2. Flip the **VoLTE** switch of a SIM to turn it on or off.
3. To also enable VoNR / 5G SA / VoWiFi, tick them under **Options**, then flip that SIM's switch again to write them.
4. The override is lost after a reboot. Flip the switch again, or tick **Re-apply after boot** to replay it automatically.

> The "How to use" card at the top of the app is collapsed by default. Tap it to see these steps.

> The UI defaults to English. Use the language button at the top right to switch between English / 简体中文 / 繁體中文 / 日本語 / 한국어 / Русский / Español / Français / Deutsch / Português. The choice is remembered.

### Build

```sh
./build.sh          # produces volte-toggle.apk
```

The build doesn't use Gradle. It runs aapt2 → javac → d8 → package jni → zipalign → apksigner (debug key).

Dependencies live in `libs/` as jars: kadb, okio, kotlin-stdlib, kotlinx-coroutines, BouncyCastle, spake2, hiddenapibypass. UI strings are in `res/values*/strings.xml`.

### Known limitations

- Wireless debugging doesn't start when Wi-Fi is off, so the app can't reach its own ADB channel.
- The override must be replayed after every reboot (a system limitation). Use "Re-apply after boot" together with Wireless debugging.
- Re-apply after a system OTA update.
- Results depend on carrier and device model. Tested on **Pixel 10 + China Unicom**. Reports for other combinations are welcome via Issues.
- `PairReceiver` is exported so it's easy to trigger from adb while debugging. Set it to `false` in the manifest if that concerns you.

---

## 中文

### 解决什么问题

Google Pixel 出厂的运营商配置里，往往没有为国内三大运营商打开 VoLTE / VoNR。常见表现：
- 打电话要掉到 3G/2G，或者 4G/5G 下打不出去。
- 没有 VoLTE 图标。
- 5G 下无法通话。

这些开关由系统的「运营商配置（CarrierConfig）」控制，正常只有系统或运营商能改。本应用用一条**手机连自己**的本地调试通道，以受限的调试身份把这些开关覆盖打开。

### 原理（简述）

- Android 不允许普通第三方应用写运营商配置，写入需要 `MODIFY_PHONE_STATE` 这类系统级授权。
- 本应用内置一个 ADB 客户端（kadb），通过**无线调试的本机回环端口**连上手机自己，以 shell 调试身份启动应用自带的 instrumentation。
- instrumentation 跑在**本应用自己的进程**里，借 `UiAutomation` 临时取得 shell 的授权，再调用系统的 `CarrierConfigManager.overrideConfig(subId, bundle)` 覆盖运营商配置。
- 系统对第三方只放开**非持久化**的覆盖方式，所以**重启后覆盖会丢失**，需要重新应用一次。界面上「开机后自动重新应用」选项就是为此准备的。
- 写完后调用 `TelephonyManager.resetIms(slot)` 让 IMS 立即重新注册，VoLTE 图标随即出现。

**写入的键**
- VoLTE 开关：`carrier_volte_available_bool`、`enhanced_4g_lte_on_by_default_bool`、`carrier_volte_provisioned_bool` 等一组相关的键。
- 可选：
  - `vonr_enabled_bool`（VoNR / 5G 通话）
  - `carrier_nr_availabilities_int_array`（5G SA）
  - VoWiFi 相关的四个键

关闭时会清空覆盖，回到运营商默认配置。

### 使用步骤

#### ① 首次准备（只需做一次）

1. 打开 **Wi-Fi 并连上网络**。
   > ⚠️ 无线调试只有在 Wi-Fi 连着时才工作，这一步必须做。
2. 打开「开发者选项」：**设置 → 关于手机 → 连续点 7 下「版本号」**，看到「已处于开发者模式」即可。
3. 打开「无线调试」：**设置 → 系统 → 开发者选项 → 无线调试**，打开并保持打开。
4. 回到应用，在「ADB 通道」卡片点「**开始配对**」，按提示把系统弹出的 **6 位配对码**填进通知里发送。
   看到「已就绪」就配对完成，密钥会被系统记住，以后不用再配。

#### ② 日常使用

1. 确认「ADB 通道」显示「**已就绪**」（若不可用，多半是 Wi-Fi 或无线调试没开）。
2. 拨动对应 SIM 卡的「**VoLTE**」开关，即可开启或关闭。
3. 想同时开 VoNR / 5G SA / VoWiFi：先在「**选项**」里勾选，再重新拨一次该 SIM 的开关才会写入。
4. 手机重启后覆盖会失效：回到应用重新拨一次开关，或勾选「**开机后自动重新应用**」让它自动重新应用。

> 应用主界面顶部的「使用步骤」卡片默认折叠，点一下即可展开查看以上步骤。

> 界面默认英文（English），点右上角的语言按钮可从列表切换：English / 简体中文 / 繁體中文 / 日本語 / 한국어 / Русский / Español / Français / Deutsch / Português，选择会被记住。

### 构建

```sh
./build.sh          # 产出 volte-toggle.apk
```

构建不用 Gradle，流程是 aapt2 → javac → d8 → 打包 jni → zipalign → apksigner（debug key）。

依赖以 jar 形式放在 `libs/`：kadb、okio、kotlin-stdlib、kotlinx-coroutines、BouncyCastle、spake2、hiddenapibypass。界面文案在 `res/values*/strings.xml`。

### 已知限制

- Wi-Fi 关掉时无线调试不启动，应用就连不上自己的 ADB 通道。
- 重启后必须重新应用（系统限制），可以靠「开机后自动重新应用」配合无线调试。
- 系统 OTA 升级后要重新应用。
- 能否成功取决于运营商与机型，目前在 **Pixel 10 + 中国联通**上测试通过。欢迎在 Issues 里反馈其他组合的结果。
- `PairReceiver` 设为 exported，是为了方便调试时用 adb 触发；介意的话可以在 manifest 里改成 `false`。

---

## License

[MIT](LICENSE) © Charlie
