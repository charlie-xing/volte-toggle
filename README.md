# VoLTE 开关 (com.xcl.volte)

在 **Google Pixel** 手机上一键为**中国大陆运营商卡**开启 VoLTE / VoNR / 5G SA，
让Google Pixel 也能正常用移动/联通/电信的高清语音和 5G 通话。
不需要 root，不需要电脑。

## 解决什么问题

Google Pixel出厂的运营商配置里，往往没有把国内三大运营商的 VoLTE/VoNR 打开，
表现为：4G/5G 下打不了电话要掉到 3G/2G、没有 VoLTE 图标、5G 下无法通话等。
这些开关由系统的「运营商配置（CarrierConfig）」控制，正常只有系统或运营商能改。
本应用用一条**手机连自己**的本地调试通道，以受限的调试身份把这些开关覆盖打开。

## 原理（简述）

- Android 不允许普通第三方应用写运营商配置，写入需要 `MODIFY_PHONE_STATE` 这类系统级授权。
- 本应用内置一个 ADB 客户端（kadb），通过**无线调试的本机回环端口**连上手机自己，
  以 shell 调试身份启动应用自带的 instrumentation。
- instrumentation 跑在**本应用自己的进程**里，借 `UiAutomation` 临时取得 shell 的授权，
  再调用系统的 `CarrierConfigManager.overrideConfig(subId, bundle)` 覆盖运营商配置。
- 覆盖用的是**非持久化**方式（系统对第三方只放开这种），所以**重启后覆盖会丢失**，
  需要重新应用一次 —— 这就是界面上「开机后自动重新应用」选项的用途。
- 写完后调用 `TelephonyManager.resetIms(slot)` 让 IMS 立即重新注册，VoLTE 图标随即出现。

写入的键包括 `carrier_volte_available_bool` / `enhanced_4g_lte_on_by_default_bool` /
`carrier_volte_provisioned_bool` 等一组 VoLTE 相关开关，
可选叠加 `vonr_enabled_bool`（VoNR / 5G 通话）、`carrier_nr_availabilities_int_array`（5G SA）、VoWiFi 四件套。
关闭 = 覆盖清空，回到运营商默认。

## 使用步骤

### ① 首次准备（只需做一次）

1. 打开 **Wi-Fi 并连上网络**。
   > ⚠️ 无线调试只有在 Wi-Fi 连着时才工作，这一步必须做。
2. 打开「开发者选项」：**设置 → 关于手机 → 连续点 7 下「版本号」**，看到「已处于开发者模式」即可。
3. 打开「无线调试」：**设置 → 系统 → 开发者选项 → 无线调试**，打开并保持打开。
4. 回到应用，在「ADB 通道」卡片点「**开始配对**」，按提示把系统弹出的 **6 位配对码**填进通知里发送。
   看到「已就绪」就配对完成，密钥会被系统记住，以后不用再配。

### ② 日常使用

1. 确认「ADB 通道」显示「**已就绪**」（若不可用，多半是 Wi-Fi 或无线调试没开）。
2. 拨动对应 SIM 卡的「**VoLTE**」开关，即可开启 / 关闭。
3. 想同时开 VoNR / 5G SA / VoWiFi：先在「**选项**」里勾选，再重新拨一次该 SIM 的开关才会写入。
4. 手机重启后覆盖会失效 —— 回到本页重新拨一次开关，或勾选「**开机后自动重新应用**」让它自动重放。

> 应用主界面顶部的「使用步骤」卡片默认折叠，点一下即可展开查看以上步骤。

> 界面默认英文（English），点右上角的语言按钮可从列表切换：English / 简体中文 / 繁體中文 / 日本語 / 한국어 / Русский / Español / Français / Deutsch / Português，选择会被记住。

## 构建

```sh
./build.sh          # 产出 volte-toggle.apk
```

不用 Gradle：aapt2 → javac → d8 → 打包 jni → zipalign → apksigner（debug key）。
依赖以 jar 形式放在 `libs/`（kadb / okio / kotlin-stdlib / kotlinx-coroutines / BouncyCastle / spake2 / hiddenapibypass）。

## 已知限制

- Wi-Fi 关掉时无线调试不启动，应用就连不上自己的 ADB 通道。
- 重启后必须重放（系统限制），靠「开机后自动重新应用」+ 无线调试自启。
- 系统 OTA 升级后要重新应用。
- 能否成功取决于运营商与机型，pixel 10测试通过。
- `PairReceiver` 是 exported 的（方便用 adb 调试触发），介意的话把 manifest 里改成 `false`。

## License

[MIT](LICENSE) © Charlie
