# Pixel IMS & APN Tuner

**Language / Мова / Язык / Idioma / 语言:** [English](./README.md) | [Беларуская](./README_BY.md) | [Українська](./README_UA.md) | [Bahasa Indonesia](./README_ID.md) | [Bahasa Melayu](./README_MS.md) | [Deutsch](./README_DE.md) | [Español](./README_ES.md) | [Filipino](./README_FIL.md) | [Français](./README_FR.md) | [Português](./README_PT.md) | [日本語](./README_JA.md) | [简体中文](./README_CN.md) | [繁體中文](./README_TW.md) | [**한국어**](./README_KO.md) | [Русский](./README_RU.md)

**Google Pixel 6–11에서 루팅 없이, PC 없이, 외부 앱(Shizuku) 없이 VoLTE, VoWiFi, VoNR(5G 통화), 5G SA 및 통신사 APN 프로필을 자동으로 설정합니다.**

📦 다운로드: **[Releases → pixel-ims-apn-tuner.apk](https://github.com/4plashy-disprized/pixel-ims-apn-tuner/releases/latest)**

---

### 어떤 문제를 해결하나요?

Google Pixel 스마트폰은 Google이 공식 인증한 국가/통신사에서만 기본적으로 VoLTE, Wi‑Fi 통화(VoWiFi), 5G를 활성화합니다. 한국 통신사(SKT, KT, LG U+, 알뜰폰)나 기타 미출시 국가 유심을 장착하면 다음과 같은 문제가 발생합니다.

- **통화 시 3G로 떨어지거나(또는 LG U+ 및 3G 종료 지역에서는 수발신 자체가 불가)** 통화 중 모바일 데이터가 끊깁니다.
- SIM 설정 메뉴에 **VoLTE, Wi‑Fi 통화, 5G 스위치가 아예 표시되지 않습니다**.
- 기본 패치만 적용하면 **엘리베이터·지하철 등에서 잠깐 3G로 전환된 뒤 30분 동안 VoLTE가 먹통이 되거나**(`invalid P-CSCF` 차단), 올바른 `ims` / `xcap` / `mms` APN이 없어 착신전환·MMS·VoWiFi가 작동하지 않습니다.
- 특히 한국 통신사(SKT/KT/LG U+) 등은 SIP `User-Agent` 헤더를 엄격하게 검사하여 **미인증 Google Pixel의 IMS 등록을 거부**할 수 있습니다.

**Pixel IMS & APN Tuner**는 가벼운 단독 앱 하나(약 800KB)로 이 모든 문제를 해결합니다:
1. **461개 통신사 내장 `CARRIER_DB`**(동일한 Shannon 모뎀 계열을 쓰는 **Samsung Galaxy S26** 펌웨어에서 추출): 3G 차단 방지 마스크가 포함된 영구 APN(`Internet`, `IMS`, `MMS`, `XCAP`)을 자동으로 입력합니다.
2. **VoLTE, VoWiFi, VoNR, 5G SA, ViLTE, 백업 통화를 활성화**하고, **Samsung S26 (Shannon) IMS User-Agent**(한국 SKT/KT/LGU+용 `TTA-VoLTE/3.0 SM-S942B/...` 포함 총 74개 통신사 전용 규격 지원)를 적용합니다.
3. **루팅·PC·Shizuku 불필요** — 금융/은행 앱, 삼성/구글 월렛(NFC 결제), Android 시스템 OTA 업데이트가 모두 정상 작동합니다.

---

### 빠른 시작 가이드 (2분 완성)

#### 1단계. 최초 1회 페어링

1. **아무 Wi‑Fi 네트워크에나 연결합니다** (집, 사무실 또는 다른 휴대폰 핫스팟).
   > ⚠️ Wi‑Fi는 설정을 적용하는 순간에만 필요합니다. 한 번 적용한 뒤에는 외출 시 모바일 데이터만으로도 VoLTE와 5G가 정상 작동합니다.
2. **「개발자 옵션」을 켭니다** *(아직 켜지 않은 경우)*:
   **설정 → 휴대전화 정보**로 이동해 맨 아래 **「빌드 번호」를 연속 7번 탭**하여 개발자 모드를 활성화합니다.
3. **「무선 디버깅」을 켭니다**:
   **설정 → 시스템 → 개발자 옵션 → 무선 디버깅**을 켜고 *「이 네트워크에서 항상 허용」*에 체크합니다.
4. **앱과 기기를 페어링합니다**:
   - **Pixel IMS & APN Tuner**를 실행하고 전화 및 알림 권한을 허용합니다.
   - 상단 카드의 **「페어링 시작」**을 누르면 상단바에 알림이 뜨고 무선 디버깅 화면이 열립니다.
   - **「페어링 코드로 기기 페어링」**을 눌러 화면에 **6자리 숫자**를 띄웁니다.
   - **코드 창을 닫지 않은 채로** 상단 알림창을 내려 앱 알림에 6자리 숫자를 입력하고 전송합니다.
5. 앱으로 돌아오면 ADB 채널이 **준비 완료**로 표시됩니다. 이후에는 다시 페어링할 필요가 없습니다!

#### 2단계. SIM 설정 적용하기

1. SIM 카드 패널에 이미 해당 통신사에 맞는 권장 항목이 **자동으로 체크**되어 있습니다.
2. **「설정 적용」**(듀얼 SIM인 경우 **「모든 SIM에 적용」**)을 누릅니다.
3. 몇 초 뒤 **「IMS: 등록됨」**과 함께 **「시스템 활성 항목: VoLTE, …」**이 표시되면 완료입니다!
4. **VoWiFi(Wi‑Fi 통화)를 사용하는 경우:** 카드 안의 **「SIM 설정 열기」** → **Wi‑Fi 통화**로 들어가 스위치가 켜져 있는지 확인하세요.

---

### 모든 옵션 및 버튼 상세 설명

#### SIM 카드 상태 표시

- **`CARRIER_DB: ...`** — 내장된 461개 통신사 DB에서 유심을 인식했음을 뜻합니다(`LTE_WIFI` = VoLTE·VoWiFi 모두 지원, `LTE_ONLY` = VoLTE만 지원).
- **`IMS: 등록됨` / `미등록`** — 통신사 음성 서버(IMS)에 정상 연결되었는지 실시간으로 보여줍니다.
- **`시스템 활성 항목: ...`** — Android 시스템(`CarrierConfigLoader`)에 실제로 적용된 항목을 직접 읽어 표시합니다.
- **회색으로 잠긴 `(시스템 프로필)` 항목** — 순정 프로필에서 이미 지원되는 기능이 실수로 꺼지지 않도록 보호하면서 부족한 기능만 켤 수 있게 해줍니다.

#### 기본 SIM 옵션

| 옵션 | 쉬운 설명 |
| :--- | :--- |
| **VoLTE (4G 통화)** | 통화 중 3G/2G로 떨어지지 않고 선명한 HD 보이스 통화와 끊김 없는 모바일 데이터를 제공합니다. **항상 켜두는 것을 권장합니다.** |
| **VoWiFi (Wi‑Fi 통화 + 상태 표시줄 아이콘)** | 지하실, 실내 음영지역, 해외 여행 중에도 Wi‑Fi만 있으면 일반 전화 및 문자를 주고받을 수 있습니다. *통신사가 Wi‑Fi 통화를 지원할 때 켜세요.* |
| **VoNR (5G 통화)** | 통화할 때 4G로 전환하지 않고 5G 망에서 직접 음성 통화를 유지합니다. |
| **5G SA + NR 신호 임계값** | Android에서 **5G NSA 및 5G SA**를 활성화하고(`[1, 2]`), VoNR 미사용 시 IMS 스택이 5G SA를 강제로 끄지 못하게 막으며(`ims.sa_disable_policy_int = 0`), 상단바의 **5G 안테나 막대 표시 기준(`SS-RSRP`)**을 표준 4G LTE 눈금(`-111`dBm에서 0칸이 뜨는 기본 `[-110, -90, -80, -65]`dBm 대신 `[-128, -118, -108, -98]`dBm)에 맞춥니다. *모뎀의 물리적 전파 수신 감도를 바꾸지는 않습니다.* |
| **CARRIER_DB의 APN 프로필 (Internet, IMS, MMS, XCAP)** | 검증된 Samsung S26 통신사 APN(`Internet`, `MMS`, 3G 전환 시 30분 먹통을 막아주는 `IMS`, 부가서비스용 `XCAP`)을 기록합니다. **재부팅해도 지워지지 않고 영구 유지됩니다.** |

#### 「고급 설정 ▸」 옵션

| 고급 옵션 | 용도 |
| :--- | :--- |
| **Samsung S26 (Shannon) IMS User-Agent** | 통신사 서버에 모뎀을 인증된 **Samsung Galaxy S26**으로 인식시킵니다(한국 3사 `TTA-VoLTE/3.0` 규격 포함 74개 통신사 전용 문자열 자동 적용). 미인증 Pixel 기기 차단을 우회할 때 핵심적인 옵션입니다. |
| **ViLTE (4G/5G 영상 통화)** | 기본 전화 앱을 통한 통신사 직통 영상 통화를 활성화합니다. |
| **로밍 시 VoWiFi** | 해외 로밍 중에도 Wi‑Fi 통화를 사용할 수 있게 해줍니다. |
| **부가 서비스 (UT / XCAP)** | 착신전환, 통화중 대기 등의 설정을 2G/3G 전환 없이 4G/Wi‑Fi로 바로 변경할 수 있게 합니다. |
| **Cross-SIM / 백업 통화** | 듀얼 SIM 사용 시 한쪽 유심이 통신 불능일 때 **다른 유심의 모바일 데이터를 빌려 통화·문자를 수발신**합니다(해외 여행용 데이터 eSIM과 한국 유심 조합에 매우 유용). |
| **LTE+ / 4G+ 아이콘 표시 (대역폭 제한 해제)** | Pixel의 숨겨진 대역폭 기준을 해제하여 주파수 묶음(CA) 작동 시 상단바에 `LTE+` / `4G+` 아이콘을 정직하게 표시합니다. |
| **시스템 설정에서 APN 추가 허용** | 통신사 프로필로 잠겨 있는 수동 APN 추가·편집 메뉴를 잠금 해제합니다. |

#### 실행 버튼 및 「옵션」

- **「설정 적용」** — 선택한 옵션을 SIM에 적용하고 즉시 IMS 재등록을 수행합니다.
- **「재설정」** — 모든 오버라이드를 지우고 순정 APN 및 기본 프로필로 되돌립니다.
- **「SIM 설정 열기」** — 해당 SIM의 Android 시스템 설정 화면으로 바로 이동합니다.
- **「모든 SIM에 적용」** *(SIM 2개 활성 시)* — 한 번의 터치로 두 SIM 모두에 적용합니다.
- **「부팅 후 자동으로 다시 적용」** *(Android 16 QPR2+ 및 Android 17에서 권장)* — 기본 **VoLTE와 APN은 영구 유지**되지만, 전원을 끄면 초기화되는 `CarrierConfig` 전용 기능(**VoWiFi, 5G / VoNR, S26 User-Agent, Cross-SIM** 등)을 재부팅 후 Wi‑Fi 연결 시 무선 디버깅 자동 깨우기와 함께 백그라운드에서 자동 복구해 줍니다.

---

### 자주 묻는 질문 및 문제 해결 (FAQ)

<details>
<summary><b>1. 「설정 적용」을 눌렀는데도 계속 「IMS: 미등록」으로 나옵니다.</b></summary>

1. 통신사에 **VoLTE 부가서비스(HD 보이스) 및 OMD/자급제 단말 등록**이 되어 있는지 확인하세요.
2. 휴대폰이 현재 **4G(LTE) 또는 5G**에 연결되어 있는지 확인하세요.
3. 통신사가 VoWiFi를 지원하지 않는다면 *VoWiFi* 체크를 끄고 다시 *설정 적용*을 눌러보세요.
4. **비행기 모드**를 5~10초간 켰다가 꺼보세요(특히 한국 통신사처럼 `S26 User-Agent`가 필수인 곳에서는 재부팅 직후 한 번 적용되어야 등록됩니다).
</details>

<details>
<summary><b>2. 휴대폰을 재부팅하면 설정이 풀리나요? (밖에서 Wi‑Fi가 없을 때)</b></summary>

- **기본 VoLTE(4G 통화)와 모든 APN(`Internet`, `IMS`, `MMS`, `XCAP`)은 재부팅해도 풀리지 않습니다**(Android 16 QPR2 / Android 17에서도 `telephony.db`에 영구 저장됨). 단, 한국 통신사처럼 **전용 `S26 User-Agent`(`CarrierConfig`)가 반드시 함께 들어가야만 IMS 등록을 받아주는 통신사**는 재부팅 후 Wi‑Fi에 잠깐 연결해 자동 재적용이 실행되어야 VoLTE가 붙습니다.
- **Android 12~15 및 Android 16 (QPR2 이전)** 버전에서는 `User-Agent`와 `5G`까지 모든 항목이 영구 유지됩니다(`[persistent]`).
- **Android 16 QPR2+ 및 Android 17**에서는 *「부팅 후 자동으로 다시 적용」*을 켜두면 등록된 Wi‑Fi(또는 다른 폰 핫스팟)에 연결되는 즉시 몇 초 만에 모든 기능이 자동으로 복구됩니다.
</details>

<details>
<summary><b>3. 예전에 6자리 코드로 페어링했는데 ADB 카드에 「연결 안 됨」이라고 뜹니다. 다시 페어링해야 하나요?</b></summary>

**아니요!** 허용된 Wi‑Fi에 연결한 뒤 **「다시 확인」**만 누르면 재부팅 후 꺼진 무선 디버깅도 앱이 자동으로 다시 켜서 연결합니다.
</details>

<details>
<summary><b>4. 「IMS: 등록됨」인데 Wi‑Fi 통화(VoWiFi)가 작동하지 않습니다.</b></summary>

**「SIM 설정 열기」** → **Wi‑Fi 통화**로 들어가 스위치를 켜고 통화 기본 설정을 **「Wi‑Fi로 통화」**로 변경하세요. 비행기 모드를 켠 뒤 Wi‑Fi만 켜고 전화를 걸어보면 바로 확인할 수 있습니다.
</details>

<details>
<summary><b>5. 은행/금융 앱, Google 월렛, OTA 업데이트에 안전한가요?</b></summary>

**100% 안전합니다.** 부트로더 언락이나 루팅을 하지 않으며 외부 인터넷 통신도 전혀 없습니다(기기 내부 `127.0.0.1` 루프백만 사용). 국내 금융/은행 앱, 간편결제, Android OTA 업데이트 모두 정상 작동합니다.
</details>

<details>
<summary><b>6. 순정 초기 상태로 완전히 되돌리려면 어떻게 하나요?</b></summary>

ADB가 준비된 상태에서 SIM 카드의 **「재설정」** 버튼을 누르거나, 앱을 삭제하고 재부팅한 뒤 Android APN 설정에서 *기본 설정으로 초기화*를 누르면 됩니다.
</details>

---

### 3G 전환 시 30분간 VoLTE가 먹통이 되는 문제와 `ims` APN 비트마스크 구현

미인증 통신사에서는 순정 `ims` APN이 없거나(수동 추가 시 숨겨진 비트마스크가 `0`=2G/3G 포함 전체 망 허용), 엘리베이터·지하주차장 등에서 잠깐 **3G**로 떨어질 때 Android가 3G에서 `IMS PDN`을 유지하려다 빈 `P-CSCF = []` 응답을 받아 `ShannonImsService`의 `BLOCK_PDN_BY_INVALID_PCSCF = 1` 보호 로직이 작동하며 **30~90분 동안 VoLTE/VoWiFi가 차단**됩니다. `ApnProvisioner`는 `ims` APN에 2G/3G를 완전히 제외한 비트마스크(`LTE|IWLAN|NR`: `network_type_bitmask` / `lingering_network_type_bitmask = 659456`, `bearer_bitmask = 663552` / `LTE|NR`: `528384`, `532480`)를 기록하여, 3G에서는 깔끔하게 일시 해제하고 4G/5G 복귀 후 1~2초 만에 즉시 재등록되도록 해결합니다.

---

### 소스 코드에서 빌드하기

```sh
./build.sh    # pixel-ims-apn-tuner.apk 생성
```

#### `CARRIER_DB` 직접 갱신하기 (`generate-carrier-db.py`)

`res/raw/carrier_db.json`은 저장소에 이미 포함되어 있으므로 일반 APK 빌드 시에는 펌웨어 파일이 필요하지 않습니다. [`generate-carrier-db.py`](./generate-carrier-db.py)(표준 Python 3)로 최신 통신사 DB를 직접 재생성하려면 다음 파일이 필요합니다:

<details>
<summary><b>필요한 파일 목록 및 추출 방법</b></summary>

1. **Google Pixel 파일** (`./firmware/pixel/CarrierSettings/`): `carrier_list.pb`, `others.pb`, `*.pb` — 루팅 없이 Pixel 기기에서 `adb pull /product/etc/CarrierSettings firmware/pixel/CarrierSettings`로 가져오거나, 공식 Pixel 팩토리 이미지([developers.google.com/android/images](https://developers.google.com/android/images))의 `product.img`에서 추출합니다.
2. **Samsung One UI (Exynos / Shannon 모뎀 기기) 파일** (`./firmware/samsung/`): `/system/etc/apns-conf.xml`, `/system/build.prop`(선택), 그리고 `/system/priv-app/imsservice/imsservice.apk` 내부 `res/raw/`의 `mnomap.json`, `imsswitch.json`, `imsprofile.json` — 엑시노스 탑재 갤럭시(S24/S25/S26 `B` 모델 등)에서 루팅 없이 `adb pull` + `unzip`으로 추출하거나, `Bifrost` / `samloader`로 받은 공식 펌웨어(`SM-S942B` 등)의 `system.img`에서 추출합니다.
3. **실행:** `./generate-carrier-db.py --update && ./build.sh` *(라이선스 보호를 위해 `./firmware/` 폴더는 `.gitignore`에 등록되어 있으며 Git에 커밋하면 안 됩니다).*
</details>

---

### 감사의 글 및 아이디어를 참고한 프로젝트

**이 프로젝트만의 독창적인 아이디어:** IMS 작동을 위한 영구 **APN 프로필**(`Internet`, 3G `invalid P-CSCF` 차단을 방지하는 `IMS`, `MMS`, `XCAP`)을 자동으로 설정하는 방식과, Google Pixel 순정 통신사 DB를 동일한 Shannon 모뎀 계열의 Samsung Galaxy S26 통신사 DB와 교차 대조해 `CARRIER_DB`를 구축한 것은 **저희의 독자적인 아이디어이자 직접 구현한 기능**입니다.

로컬 ADB 채널 및 `CarrierConfig` 제어 구조에서는 아래 오픈소스 프로젝트들의 아이디어도 참고했습니다:

- **[charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle)** — Shizuku 없이 동작하는 로컬 루프백 무선 디버깅 + `UiAutomation` 아키텍처 및 Gradle 없는 `build.sh` 빌드 파이프라인.
- **[kyujin-cho/pixel-volte-patch](https://github.com/kyujin-cho/pixel-volte-patch)** — Pixel `CarrierConfig` 패치의 선구적 구현, `ims.ims_user_agent_string` 오버라이드 개념, 접근성 서비스를 유지하는 `UiAutomation` 플래그 및 듀얼 SIM IMS 처리.
- **[vvb2060/Ims](https://github.com/vvb2060/Ims)** — 특권 `Instrumentation`(`--no-restart`), Android 14–17 MMTEL/Entitlement 프로비저닝 우회 키 및 런타임 권한 자동 부여.
- **[TakaiSaisei/pixel_ims](https://github.com/TakaiSaisei/pixel_ims)** — 무선 디버깅 자동 깨우기(`WRITE_SECURE_SETTINGS` + `adb_wifi_enabled`), Wi‑Fi 연결 연동 `JobScheduler` 부팅 재적용, Android 12–13 분리(detached) 실행 및 `ICarrierConfigLoader` 실시간 조회.
- **[VinujaHerath/PixelIMS](https://github.com/VinujaHerath/PixelIMS)** — `--no-hidden-api-checks` 플래그, ViLTE 옵션, SIM 설정 바로가기 및 전체 SIM 일괄 적용.
- **[flyfishxu/kadb](https://github.com/flyfishxu/kadb)** & **[LSPosed/HiddenApiBypass](https://github.com/LSPosed/HiddenApiBypass)** — 내장 ADB over TLS / SPAKE2 클라이언트 및 숨김 API 접근.

---

## 라이선스

[MIT](./LICENSE) — [charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle) (© charlie-xing) 기반.
