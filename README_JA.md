# Pixel IMS & APN Tuner

**Language / Мова / Язык / Idioma / 语言:** [English](./README.md) | [Беларуская](./README_BY.md) | [Українська](./README_UA.md) | [Bahasa Indonesia](./README_ID.md) | [Bahasa Melayu](./README_MS.md) | [Deutsch](./README_DE.md) | [Español](./README_ES.md) | [Filipino](./README_FIL.md) | [Français](./README_FR.md) | [Português](./README_PT.md) | [**日本語**](./README_JA.md) | [简体中文](./README_CN.md) | [繁體中文](./README_TW.md) | [한국어](./README_KO.md) | [Русский](./README_RU.md)

**Google Pixel 6〜11 で VoLTE・VoWiFi・VoNR（5G 通話）・5G SA の有効化とキャリア APN プロファイルの自動設定を行うスタンドアロンアプリ — Root 不要・PC 不要・外部ツール（Shizuku）不要。**

📦 ダウンロード：**[Releases → pixel-ims-apn-tuner.apk](https://github.com/4plashy-disprized/pixel-ims-apn-tuner/releases/latest)**

---

### このアプリで解決できる問題

Google Pixel は、Google の認証リストに登録されている通信事業者の SIM でしか標準で VoLTE・Wi‑Fi 通話（VoWiFi）・5G が有効になりません。海外キャリアの SIM / eSIM や未認証キャリアを利用すると、次のような問題が起こります。

- **通話中にモバイルデータが切れ、3G / 2G にフォールバックする**（3G 停波エリアでは発着信自体ができなくなる）。
- SIM 設定画面に **「VoLTE」「Wi‑Fi 通話」「5G」のスイッチが表示されない**。
- 一般的な CarrierConfig パッチだけでは、**エレベーターや地下鉄で一瞬 3G に入った後に 30 分間 VoLTE がロックされる**（`invalid P-CSCF` ブロック）、または `ims` / `xcap` / `mms` APN が無いために着信転送・MMS・VoWiFi が動作しない。
- 一部の通信事業者は SIP `User-Agent` を判定して **未認証の Google Pixel を拒否する**（ハードウェア上の Shannon モデム自体は完全対応しているにもかかわらず）。

**Pixel IMS & APN Tuner** は、これらを単体の軽量アプリ（約 800 KB）だけで解決します。
1. **461 キャリア収録の内蔵 `CARRIER_DB`**（同じ Shannon モデム系統を搭載する **Samsung Galaxy S26** ファームウェア由来）：3G ロックを防止する適切なベアラーマスク付きの永続 APN（`Internet` / `IMS` / `MMS` / `XCAP`）を自動書き込みします。
2. **VoLTE・VoWiFi・VoNR・5G SA・ViLTE・バックアップ通話を解放**し、必要に応じて **Samsung S26 (Shannon) の IMS User-Agent**（厳格な 74 キャリア向けの専用プロファイルを含む）を設定します。
3. **Root・PC・Shizuku すべて不要** — Google ウォレット（おサイフケータイ / タッチ決済）、銀行アプリ、Android のシステムアップデートもそのまま通常通り利用できます。

---

### かんたん初期設定（約 2 分）

#### ステップ 1：初回のペア設定（最初の 1 回だけ）

1. **任意の Wi‑Fi ネットワークに接続します**（自宅・職場・別スマホのテザリングでも可）。
   > ⚠️ Wi‑Fi が必要なのは設定時のみです。一度適用すれば、外出先のモバイル回線だけでも VoLTE / 5G はそのまま動作します。
2. **「開発者向けオプション」を有効化します**（未有効の場合）：
   **設定 → デバイス情報** を開き、一番下の **「ビルド番号」を 7 回連続でタップ**して *「デベロッパーになりました！」* と表示させます。
3. **「ワイヤレス デバッグ」をオンにします**：
   **設定 → システム → 開発者向けオプション → ワイヤレス デバッグ** を開き、スイッチをオンにして *「このネットワークで常に許可する」* にチェックを入れます。
4. **アプリとペア設定します**：
   - **Pixel IMS & APN Tuner** を開き、電話と通知の権限を許可します。
   - 上部カードの **「ペア設定を開始」** をタップすると、通知が表示され「ワイヤレス デバッグ」画面が開きます。
   - **「ペア設定コードによるデバイスのペア設定」** をタップすると、**6 桁の数字**が表示されます。
   - **そのダイアログを閉じずに**通知シェードを下ろし、アプリの通知から **「コードを入力」** を押して 6 桁の数字を入力・送信します。
5. アプリに戻ると ADB チャネルが **「準備完了」** になります。今後ペア設定をやり直す必要はありません！

#### ステップ 2：SIM に設定を適用する

1. SIM カードの枠には、そのキャリアに最適なチェック項目が**最初から自動選択**されています。
2. **「設定を適用」**（2 枚運用時は **「すべての SIM に適用」**）をタップします。
3. 数秒後に **「IMS: 登録済み」** と表示され、その下に **「システム有効設定: VoLTE, …」** が表示されれば完了です。
4. **VoWiFi（Wi‑Fi 通話）を使う場合：** カード内の **「SIM 設定を開く」** をタップ → **「Wi‑Fi 通話」** を開いてスイッチがオンになっているか確認してください。

---

### すべての設定項目とボタンの解説

#### SIM カード内のステータス表示

- **`CARRIER_DB: ...`** — 内蔵の 461 キャリア DB に一致したことを示します（`LTE_WIFI` = VoLTE・VoWiFi 両対応、`LTE_ONLY` = VoLTE のみ対応）。
- **`IMS: 登録済み` / `未登録`** — キャリアの音声サーバー（IMS）に正常に接続されているかどうかのリアルタイム表示です。
- **`システム有効設定: ...`** — Android の `CarrierConfigLoader` から実際に有効になっているフラグを読み取って表示します。
- **グレー表示の `(システムプロファイル)`** — Google 標準プロファイルで最初から有効な機能を誤操作でオフにしないよう保護しつつ、不足機能だけを追加できるようにしています。

#### メイン設定項目

| 項目 | わかりやすい説明 |
| :--- | :--- |
| **VoLTE（4G 通話）** | 3G / 2G に落ちずに 4G LTE で高音質（HD）通話を行い、通話中も高速データ通信を維持します。**原則つねにオン推奨です。** |
| **VoWiFi（Wi‑Fi 通話 + アイコン）** | 地下や屋内、山間部、海外滞在時など携帯電波が届かない場所でも Wi‑Fi 経由で通常の音声通話・SMS を利用できます。*契約キャリアが Wi‑Fi 通話に対応している場合にオンにします。* |
| **VoNR（5G 通話）** | 通話時に 4G へ戻らず、5G ネットワーク上で直接音声通話を行います。 |
| **5G SA + NR 電波しきい値** | Android 側で **5G NSA / 5G SA** の両方を有効化し（`[1, 2]`）、VoNR 非利用時に IMS スタックが 5G SA を無効化するのを防ぎ（`ims.sa_disable_policy_int = 0`）、ステータスバーの **5G アンテナ本数表示（`SS-RSRP`）** を標準の 4G LTE スケール（`-111` dBm で 0 本になる標準 `[-110, -90, -80, -65]` dBm ではなく `[-128, -118, -108, -98]` dBm）に揃えます。*モデムの物理的な受信感度は変更しません。* |
| **CARRIER_DB の APN プロファイル（Internet / IMS / MMS / XCAP）** | Samsung S26 由来の検証済みキャリア APN（3G での 30 分ロックを防ぐ `IMS`、`Internet`、`MMS`、通話設定用の `XCAP`）を書き込みます。**再起動しても消えずに永続保持されます。** |

#### 「詳細設定 ▸」内の項目

| 詳細項目 | 役割 |
| :--- | :--- |
| **Samsung S26 (Shannon) IMS User-Agent** | キャリア網に対する SIP `User-Agent` を認証済みの **Samsung Galaxy S26** に変更します（74 キャリアの個別フォーマットにも対応）。未認証 Pixel を弾くキャリアでの登録失敗を解消します。 |
| **ViLTE (4G/5G ビデオ通話)** | 標準の「電話」アプリによるキャリア網ビデオ通話を有効化します（キャリア対応時）。 |
| **ローミング時の VoWiFi** | 海外ローミング中の Wi‑Fi 通話と優先モード選択を有効化します。 |
| **付加サービス (UT / XCAP)** | 着信転送やキャッチホンなどの設定を、2G/3G に落とさず 4G / Wi‑Fi 経由で直接変更できるようにします。 |
| **Cross-SIM / バックアップ通話** | デュアル SIM 運用時、片方の SIM が圏外でも**もう片方の SIM のモバイルデータ通信を使って通話・SMS を行える**ようにします（海外旅行用データ eSIM との併用に最適）。 |
| **LTE+ / 4G+ アイコン表示（帯域幅制限なし）** | Pixel 内部の帯域幅しきい値を解除し、キャリアアグリゲーション動作中は常にステータスバーへ `LTE+` / `4G+` を表示します。 |
| **システム設定での APN 追加を許可** | キャリアによってロックされている手動 APN 追加・編集メニューを解放します。 |

#### 操作ボタンと「オプション」

- **「設定を適用」** — 選択した項目を SIM に書き込み、即座に IMS 再登録を行います。
- **「リセット」** — 上書き設定をすべて解除し、標準 APN と初期プロファイルに戻します。
- **「SIM 設定を開く」** — その SIM の Android システム設定画面を直接開きます。
- **「すべての SIM に適用」** *(2 枚有効時)* — ワンタップで両方の SIM に適用します。
- **「起動後に自動で再適用」** *(Android 16 QPR2 以降・Android 17 で推奨)* — 基本の **VoLTE と APN は永続保存**されますが、電源オフでリセットされる `CarrierConfig` 側の追加機能（**VoWiFi・5G / VoNR・S26 User-Agent・Cross-SIM** 等）を、起動後に Wi‑Fi へ接続したタイミングでワイヤレスデバッグの自動復帰とともにバックグラウンドで自動復元します。

---

### よくある質問・トラブルシューティング (FAQ)

<details>
<summary><b>1.「設定を適用」を押しても「IMS: 未登録」のままになります。</b></summary>

1. **回線契約側で VoLTE オプションが有効になっているか**確認してください。
2. 端末が現在 **4G (LTE) または 5G** を掴んでいるか確認してください。
3. キャリアが VoWiFi 非対応の場合は、*VoWiFi* のチェックを外して再度 *設定を適用* を押してください。
4. **機内モード**をオンにして 5〜10 秒待ってからオフにしてください。
</details>

<details>
<summary><b>2. 再起動すると設定は消えますか？（外出先で Wi‑Fi がない場合など）</b></summary>

- **基本の VoLTE（4G 通話）とすべての APN（`Internet` / `IMS` / `MMS` / `XCAP`）は再起動しても消えません**（Android 16 QPR2 / Android 17 でも、Wi‑Fi がなくても `telephony.db` に永続保存されているため起動直後から 4G 通話・通信が可能です）。
- **Android 12〜15 および Android 16 (QPR2 未満)** では全項目が完全に永続化されます（`[persistent]`）。
- **Android 16 QPR2 以降・Android 17** では、`CarrierConfig` 専用機能（**VoWiFi・5G / VoNR・S26 User-Agent・Cross-SIM・ViLTE・LTE+ アイコン**）のみ再起動時に一旦リセットされますが、*「起動後に自動で再適用」* をオンにしておけば登録済み Wi‑Fi に繋がった瞬間に数秒で自動復元されます。
</details>

<details>
<summary><b>3. 以前に 6 桁コードでペア設定したのに「未接続」と表示されます。再ペアリングが必要ですか？</b></summary>

**不要です！** 許可済みの Wi‑Fi に接続して **「再確認」** をタップするだけで、再起動後にオフになったワイヤレスデバッグもアプリが自動で再有効化します。
</details>

<details>
<summary><b>4.「IMS: 登録済み」なのに Wi‑Fi 通話（VoWiFi）になりません。</b></summary>

**「SIM 設定を開く」** → **「Wi‑Fi 通話」** を開き、スイッチをオンにして優先設定を **「Wi‑Fi 経由で通話」** に変更してください。機内モードをオンにした状態で Wi‑Fi だけオンにして発信するとすぐに確認できます。
</details>

<details>
<summary><b>5. Google ウォレットや銀行アプリ、システムアップデートへの影響はありますか？</b></summary>

**まったく影響ありません（安全です）。** ブートローダー解錠や Root 化は一切行わず、外部インターネット通信もゼロ（端末内部の `127.0.0.1` のみで完結）です。タッチ決済、銀行アプリ、OTA アップデートもそのまま利用できます。
</details>

<details>
<summary><b>6. 工場出荷時の状態に完全に戻すには？</b></summary>

ADB が準備完了の状態で SIM カードの **「リセット」** をタップするか、アプリをアンインストールして再起動し、Android の APN 設定メニューから *初期設定にリセット* を実行してください。
</details>

---

### 3G フォールバック時の 30 分間ロック問題と `ims` APN ビットマスク実装

未認証キャリアでは標準の `ims` APN が無く（手動追加 APN もビットマスクが `0`＝2G/3G を含む全回線許可になるため）、エレベーターや地下で一瞬 **3G** に落ちた際に Android が 3G 上で `IMS PDN` を維持しようとして空の `P-CSCF = []` を受け取り、`ShannonImsService` の `BLOCK_PDN_BY_INVALID_PCSCF = 1` によって **30〜90 分間 VoLTE / VoWiFi がロック**されます。本アプリの `ApnProvisioner` は `ims` APN に 2G/3G を除外したビットマスク（`LTE|IWLAN|NR` では `network_type_bitmask` / `lingering_network_type_bitmask = 659456`・`bearer_bitmask = 663552`、`LTE|NR` では `528384`・`532480`）を設定するため、3G では正常に一時切断され、4G/5G 復帰後 1〜2 秒で即座に再登録されます。

---

### ソースからのビルド

```sh
./build.sh    # pixel-ims-apn-tuner.apk を生成します
```

#### `CARRIER_DB` の再生成（`generate-carrier-db.py`）

`res/raw/carrier_db.json` はリポジトリに含まれているため、通常のビルドにファームウェアは不要です。[`generate-carrier-db.py`](./generate-carrier-db.py)（標準 Python 3 のみで動作）で最新のキャリア DB を再生成する場合：

<details>
<summary><b>必要なファイルと入手方法</b></summary>

1. **Google Pixel 側ファイル**（`./firmware/pixel/CarrierSettings/`）：`carrier_list.pb`、`others.pb`、`*.pb` — Root 不要で実機から `adb pull /product/etc/CarrierSettings firmware/pixel/CarrierSettings` で取得するか、公式 Pixel ファクトリーイメージ（[developers.google.com/android/images](https://developers.google.com/android/images)）の `product.img` から展開します。
2. **Samsung One UI（Exynos / Shannon モデム機）側ファイル**（`./firmware/samsung/`）：`/system/etc/apns-conf.xml`、`/system/build.prop`（任意）、および `/system/priv-app/imsservice/imsservice.apk` 内の `res/raw/{mnomap,imsswitch,imsprofile}.json` — Exynos 版 Galaxy（S24/S25/S26 の `B` 型番など）から Root 不要で `adb pull` するか、`Bifrost` / `samloader` 等で取得した公式ファームウェア（`SM-S942B` など）の `system.img` から展開します。
3. **実行：** `./generate-carrier-db.py --update && ./build.sh`（`./firmware/` は `.gitignore` 済みです。ライセンス保護のため生ファームウェアは Git にコミットしないでください）。
</details>

---

### 謝辞・アイデアの参考プロジェクト

**本プロジェクト独自の考案・実装：** IMS 用の永続 **APN プロファイル**（3G での `invalid P-CSCF` ロックを防止する `IMS`、`Internet`、`MMS`、`XCAP`）を自動構成する仕組みと、Google Pixel 標準キャリア DB と同じ Shannon モデム系統を搭載する Samsung Galaxy S26 のキャリア DB を突き合わせて `CARRIER_DB` を構築する手法は、**私たち自身のオリジナルアイデアおよび独自実装**です。

また、ローカル ADB チャネルや `CarrierConfig` 制御の仕組みについては、以下のオープンソースプロジェクトの知見も参考にしています。

- **[charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle)** — Shizuku 不要のローカルループバック・ワイヤレスデバッグ + `UiAutomation` 構成および Gradle 不要の `build.sh` ビルド基盤。
- **[kyujin-cho/pixel-volte-patch](https://github.com/kyujin-cho/pixel-volte-patch)** — Pixel 向け `CarrierConfig` パッチの先駆け、`ims.ims_user_agent_string` 上書き手法、ユーザー補助サービスを停止させない `UiAutomation` フラグ、デュアル SIM 処理。
- **[vvb2060/Ims](https://github.com/vvb2060/Ims)** — 特権 `Instrumentation`（`--no-restart`）、Android 14〜17 の MMTEL / Entitlement プロビジョニング回避キー、権限の自動付与。
- **[TakaiSaisei/pixel_ims](https://github.com/TakaiSaisei/pixel_ims)** — スリープしたワイヤレスデバッグの自動復帰（`WRITE_SECURE_SETTINGS` + `adb_wifi_enabled`）、Wi‑Fi 接続連動の起動時 `JobScheduler` 再適用、Android 12〜13 向けデタッチ実行、`ICarrierConfigLoader` のライブ状態取得。
- **[VinujaHerath/PixelIMS](https://github.com/VinujaHerath/PixelIMS)** — `--no-hidden-api-checks` フラグ、ViLTE オプション、SIM 設定画面への直接ショートカット、全 SIM 一括適用。
- **[flyfishxu/kadb](https://github.com/flyfishxu/kadb)** & **[LSPosed/HiddenApiBypass](https://github.com/LSPosed/HiddenApiBypass)** — 内蔵 ADB over TLS / SPAKE2 クライアントおよび非公開 Android API アクセス。

---

## ライセンス

[MIT](./LICENSE) — [charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle) (© charlie-xing) をベースに開発。
