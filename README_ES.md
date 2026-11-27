# Pixel IMS & APN Tuner

**Language / Мова / Язык / Idioma / 语言:** [English](./README.md) | [Беларуская](./README_BY.md) | [Українська](./README_UA.md) | [Bahasa Indonesia](./README_ID.md) | [Bahasa Melayu](./README_MS.md) | [Deutsch](./README_DE.md) | [**Español**](./README_ES.md) | [Filipino](./README_FIL.md) | [Français](./README_FR.md) | [Português](./README_PT.md) | [日本語](./README_JA.md) | [简体中文](./README_CN.md) | [繁體中文](./README_TW.md) | [한국어](./README_KO.md) | [Русский](./README_RU.md)

**Activa VoLTE, VoWiFi, VoNR (llamadas 5G), 5G SA y perfiles APN automáticos de operador en Google Pixel 6–11 — sin Root, sin PC y sin aplicaciones externas (Shizuku).**

📦 Descargar: **[Releases → pixel-ims-apn-tuner.apk](https://github.com/4plashy-disprized/pixel-ims-apn-tuner/releases/latest)**

---

### ¿Qué problema resuelve esta aplicación?

Los teléfonos Google Pixel solo traen activados VoLTE, VoWiFi y 5G de fábrica para los operadores incluidos en la lista oficial de Google. Si usas una tarjeta SIM de un operador o país no certificado (Latinoamérica, Europa del Este, Asia, etc.), suelen aparecer estos problemas:

- **Durante una llamada se corta el internet móvil** y el teléfono baja a **3G o 2G** (o la llamada falla si ya apagaron el 3G).
- **No aparecen los interruptores de VoLTE, Llamadas Wi‑Fi (VoWiFi) ni 5G** en los ajustes de la SIM.
- Incluso con parches básicos, **VoLTE deja de funcionar durante 30 minutos tras pasar por un ascensor o metro** (bloqueo `invalid P-CSCF` al caer brevemente a 3G), o no funcionan el desvío de llamadas, los MMS ni VoWiFi por falta de los APN `ims` / `xcap` / `mms`.
- Algunos operadores —como Claro, Movistar, Telcel y otros— revisan el identificador del teléfono y **rechazan los Google Pixel no certificados**, aunque su módem Samsung Shannon es totalmente compatible.

**Pixel IMS & APN Tuner** lo soluciona todo en una sola app ligera (~800 KB):
1. **Base integrada `CARRIER_DB` con 461 operadores** (extraída del firmware del **Samsung Galaxy S26** con módem Shannon): configura automáticamente los APN permanentes **Internet, IMS, MMS y XCAP** evitando bloqueos en 3G.
2. **Desbloquea VoLTE, VoWiFi, VoNR, 5G SA, ViLTE y Llamadas de respaldo**, y permite usar el **IMS User-Agent del Samsung S26** (incluidos perfiles específicos para 74 operadores estrictos).
3. **Sin Root, sin PC y sin Shizuku** — Google Wallet, apps bancarias y actualizaciones OTA siguen funcionando con total normalidad.

---

### Inicio rápido (2 minutos)

#### Paso 1. Vinculación única (solo la primera vez)

1. **Conéctate a cualquier red Wi‑Fi** (casa, trabajo o zona Wi‑Fi compartida desde otro móvil).
   > ⚠️ Solo hace falta Wi‑Fi en el momento de configurar; después, VoLTE y 5G funcionan normalmente con datos móviles sin Wi‑Fi.
2. **Activa las «Opciones para desarrolladores»** *(si aún no están activas)*:
   Ve a **Ajustes → Información del teléfono**, baja hasta el final y **pulsa 7 veces seguidas en «Número de compilación»** hasta ver *«¡Ya eres desarrollador!»*.
3. **Activa la «Depuración inalámbrica»**:
   Ve a **Ajustes → Sistema → Opciones para desarrolladores → Depuración inalámbrica**, actívala y marca *«Permitir siempre en esta red»*.
4. **Vincula la aplicación**:
   - Abre **Pixel IMS & APN Tuner** y concede los permisos de Teléfono y Notificaciones.
   - Pulsa **«Iniciar vinculación»** — aparecerá una notificación y se abrirá «Depuración inalámbrica».
   - Pulsa **«Vincular dispositivo con código de vinculación»** — verás un código de **6 dígitos**.
   - **Sin cerrar ese cuadro**, desliza la barra de notificaciones, pulsa en la notificación de la app, escribe los 6 dígitos y envíalos.
5. Vuelve a la aplicación — verás el estado **Listo**. ¡No tendrás que volver a vincular!

#### Paso 2. Aplicar ajustes a tu SIM

1. En la tarjeta de tu SIM verás **preseleccionadas las opciones recomendadas** para tu operador.
2. Pulsa **«Aplicar ajustes»** (o **«Aplicar a todas las SIM»** si usas Dual-SIM).
3. En unos segundos verás **«IMS: registrado»** y debajo **«Activo en el sistema: VoLTE, …»**.
4. **Si activaste VoWiFi:** pulsa **«Abrir ajustes de SIM»** → entra en **Llamadas por Wi‑Fi** y asegúrate de que el interruptor esté activado.

---

### Guía completa de todas las opciones y botones

#### Indicadores en la tarjeta SIM

- **`CARRIER_DB: ...`** — tu SIM fue reconocida en la base de 461 operadores (`LTE_WIFI` = soporta VoLTE y VoWiFi; `LTE_ONLY` = solo VoLTE).
- **`IMS: registrado` / `no registrado`** — confirma en tiempo real que el teléfono está conectado al servidor de voz del operador para llamadas 4G/5G/Wi‑Fi.
- **`Activo en el sistema: ...`** — muestra las funciones realmente activas en Android en ese instante.
- **Casillas grises con `(perfil del sistema)`** — protegen funciones que ya vienen activas de fábrica para esa SIM, permitiéndote activar las que faltan.

#### Opciones principales de la SIM

| Opción | Qué hace en palabras sencillas |
| :--- | :--- |
| **VoLTE (llamadas 4G)** | Llamadas en voz HD sobre 4G LTE sin caer a 3G/2G y manteniendo los datos móviles activos durante la llamada. **Recomendado siempre.** |
| **VoWiFi (llamadas Wi‑Fi + icono)** | Permite hacer y recibir llamadas y SMS normales por cualquier red Wi‑Fi donde no hay cobertura móvil (sótanos, interiores o viajes al extranjero con tarifa local). *Actívalo si tu operador ofrece Llamadas Wi‑Fi.* |
| **VoNR (llamadas 5G)** | Mantiene las llamadas de voz directamente en 5G sin bajar a 4G. |
| **5G SA + umbrales de señal NR** | Habilita en Android **5G NSA y 5G SA** (`[1, 2]`), impide que la pila IMS desactive 5G SA cuando no hay VoNR (`ims.sa_disable_policy_int = 0`) y alinea las **barras de señal 5G (`SS-RSRP`)** de la barra de estado con la escala estándar de 4G LTE (`[-128, -118, -108, -98]` dBm en lugar de `[-110, -90, -80, -65]` dBm, que muestra 0 barras a `-111` dBm). *No altera la sensibilidad física del módem.* |
| **Perfiles APN (Internet, IMS, MMS, XCAP) desde CARRIER_DB** | Escribe los APN verificados del Samsung S26 (`Internet`, `MMS`, `IMS` protegido frente a bloqueos de 30 min en 3G y `XCAP`). **Se guardan de forma permanente aunque reinicies el teléfono.** |

#### Sección desplegable «Avanzado ▸»

| Opción avanzada | Para qué sirve |
| :--- | :--- |
| **IMS User-Agent de Samsung S26 (Shannon)** | Presenta el módem ante la red del operador como un **Samsung Galaxy S26** certificado (incluyendo formatos específicos para 74 operadores como Claro/AMX, etc.). Soluciona rechazos de VoLTE/VoWiFi en Pixel no certificados. |
| **ViLTE (videollamadas 4G/5G)** | Activa las videollamadas nativas desde la app Teléfono por red móvil (si el operador lo soporta). |
| **VoWiFi en roaming** | Permite usar Llamadas Wi‑Fi estando en el extranjero y habilita el selector de preferencia en roaming. |
| **Servicios suplementarios (UT / XCAP)** | Permite configurar desvío de llamadas y llamada en espera directamente sobre 4G/Wi‑Fi sin bajar a 2G/3G. |
| **Cross-SIM / Llamadas de respaldo** | En teléfonos con dos SIM, permite que una SIM sin cobertura llame y reciba SMS **usando los datos móviles de la otra SIM** (ideal con una eSIM de viaje). |
| **Icono LTE+ / 4G+ en cualquier agregación** | Elimina el umbral oculto de ancho de banda del Pixel para mostrar el icono `LTE+` / `4G+` siempre que haya agregación de frecuencias (solo visual). |
| **Permitir añadir APN en ajustes del sistema** | Desbloquea la creación y edición manual de APN si el perfil del operador lo bloquea. |

#### Botones de acción y tarjeta «Opciones»

- **«Aplicar ajustes»** — guarda y activa las opciones elegidas para esa SIM.
- **«Restablecer»** — elimina los cambios de esa SIM y restaura los APN de fábrica.
- **«Abrir ajustes de SIM»** — abre directamente la pantalla de ajustes de Android de esa SIM.
- **«Aplicar a todas las SIM»** *(con 2 SIM activas)* — aplica los ajustes a ambas SIM con un toque.
- **«Volver a aplicar al arrancar»** *(recomendado en Android 16 QPR2+ y Android 17)* — el **VoLTE básico y los APN** son permanentes; esta opción espera a tener Wi‑Fi tras reiniciar, reactiva automáticamente la Depuración inalámbrica si Android la apagó y restaura en segundo plano las funciones de `CarrierConfig` (**VoWiFi**, **5G / VoNR**, **User-Agent S26**, **Cross-SIM**).

---

### Preguntas frecuentes y solución de problemas (FAQ)

<details>
<summary><b>1. Pulsé «Aplicar ajustes», pero sigue diciendo «IMS: no registrado». ¿Qué reviso?</b></summary>

1. Asegúrate de que **VoLTE esté activo en tu línea con tu operador** (muchos operadores requieren activarlo gratis desde su app o atención al cliente).
2. Comprueba que el móvil esté conectado a **4G (LTE) o 5G**.
3. Si tu operador no ofrece Llamadas Wi‑Fi, desmarca *VoWiFi* y vuelve a pulsar *Aplicar ajustes*.
4. Activa el **Modo avión** durante 5–10 segundos y desactívalo.
</details>

<details>
<summary><b>2. ¿Se pierden los ajustes al reiniciar el teléfono (incluso fuera de casa sin Wi‑Fi)?</b></summary>

- **El VoLTE básico (llamadas 4G) y todos los APN (`Internet`, `IMS`, `MMS`, `XCAP`) NO se pierden al reiniciar** — ni siquiera en Android 16 QPR2 / Android 17 ni sin Wi‑Fi, ya que se guardan de forma permanente en `telephony.db`.
- **En Android 12–15 y Android 16 anterior a QPR2**, absolutamente todo es permanente (`[persistent]`).
- **En Android 16 QPR2+ y Android 17**, al apagar el móvil solo se reinician temporalmente las funciones exclusivas de `CarrierConfig` (**VoWiFi**, **5G / VoNR**, **User-Agent S26**, **Cross-SIM**, **ViLTE**, **icono LTE+**), que se restauran solas en segundos al conectarse a un Wi‑Fi conocido si tienes marcado *«Volver a aplicar al arrancar»*.
</details>

<details>
<summary><b>3. El canal ADB dice «No conectado», aunque ya introduje el código de 6 dígitos antes. ¿Debo vincular otra vez?</b></summary>

**¡No!** Simplemente conéctate a una red Wi‑Fi de confianza y pulsa **«Volver a comprobar»** — la app reactivará sola la Depuración inalámbrica si Android la desactivó tras reiniciar.
</details>

<details>
<summary><b>4. Dice «IMS: registrado», pero las llamadas por Wi‑Fi (VoWiFi) no funcionan.</b></summary>

Pulsa **«Abrir ajustes de SIM»** → **Llamadas por Wi‑Fi** → activa el interruptor y elige **«Llamar por Wi‑Fi»** como preferencia. Para probarlo, activa Modo avión, enciende el Wi‑Fi y haz una llamada de prueba.
</details>

<details>
<summary><b>5. ¿Es seguro para Google Wallet, apps bancarias y actualizaciones OTA?</b></summary>

**Totalmente seguro.** No requiere Root ni abrir el bootloader y no se conecta a internet (todo ocurre localmente en `127.0.0.1`). Google Wallet, bancos y actualizaciones OTA funcionan sin problemas.
</details>

<details>
<summary><b>6. ¿Cómo dejo todo como venía de fábrica?</b></summary>

Pulsa **«Restablecer»** en la tarjeta de tu SIM con el canal ADB listo, o desinstala la app, reinicia el móvil y restablece los APN predeterminados en los ajustes de Android.
</details>

---

### Solución al bloqueo de 30 minutos al caer a 3G mediante máscaras de bits del APN `ims`

Sin un APN `ims` dedicado (o con un APN creado a mano cuya máscara queda en `0` = todas las redes incluidas 2G/3G), al caer brevemente a **3G** (ascensor, metro, sótano) Android intenta mantener el `IMS PDN` en 3G, recibe `P-CSCF = []` vacío y `ShannonImsService` activa `BLOCK_PDN_BY_INVALID_PCSCF = 1`, bloqueando VoLTE y VoWiFi durante **30–90 minutos**. `ApnProvisioner` graba el APN `ims` con máscaras de bits que excluyen 2G/3G (`network_type_bitmask` y `lingering_network_type_bitmask` = `659456` para `LTE|IWLAN|NR` o `528384` para `LTE|NR`; `bearer_bitmask` = `663552` o `532480`), pausando IMS limpiamente en 3G y reconectando en 1–2 segundos al volver a 4G/5G.

---

### Compilación desde el código fuente

```sh
./build.sh    # genera pixel-ims-apn-tuner.apk
```

#### Actualizar `CARRIER_DB` (`generate-carrier-db.py`)

`res/raw/carrier_db.json` ya viene pregenerado en el repositorio. Si deseas regenerarlo con [`generate-carrier-db.py`](./generate-carrier-db.py) (Python 3 estándar):

<details>
<summary><b>Archivos necesarios y cómo obtenerlos</b></summary>

1. **Google Pixel** (`./firmware/pixel/CarrierSettings/`): `carrier_list.pb`, `others.pb` y `*.pb` — extráelos por ADB sin Root desde cualquier Pixel (`adb pull /product/etc/CarrierSettings firmware/pixel/CarrierSettings`) o desde la imagen oficial de Pixel (`product.img` en [developers.google.com/android/images](https://developers.google.com/android/images)).
2. **Samsung One UI con módem Exynos/Shannon** (`./firmware/samsung/`): `apns-conf.xml`, `build.prop` (opcional) y `mnomap.json`, `imsswitch.json`, `imsprofile.json` (dentro de `res/raw/` en `/system/priv-app/imsservice/imsservice.apk`) — mediante `adb pull` sin Root desde un Galaxy Exynos (S24/S25/S26 serie `B`) o extrayendo `system.img` del firmware oficial (`SM-S942B` con `Bifrost` / `samloader`).
3. **Ejecutar:** `./generate-carrier-db.py --update && ./build.sh` *(la carpeta `./firmware/` está en `.gitignore`; no subas volcados de firmware a Git).*
</details>

---

### Agradecimientos y proyectos de referencia

**Desarrollo propio de este proyecto:** la idea de configurar automáticamente **perfiles APN permanentes para IMS** (`Internet`, `IMS` con protección frente al bloqueo `invalid P-CSCF` en 3G, `MMS`, `XCAP`) y construir la base `CARRIER_DB` cruzando la base de operadores de Google Pixel con la base del firmware del Samsung Galaxy S26 (con la misma familia de módems Shannon) es **nuestra propia idea e implementación original**.

En cuanto al canal ADB local y los ajustes de `CarrierConfig`, el proyecto también incorpora ideas de la comunidad open-source:

- **[charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle)** — arquitectura autónoma mediante Depuración inalámbrica local + `UiAutomation` (sin Shizuku) y compilación con `build.sh` sin Gradle.
- **[kyujin-cho/pixel-volte-patch](https://github.com/kyujin-cho/pixel-volte-patch)** — pionero en parches `CarrierConfig` para Pixel, idea de reemplazo de `ims.ims_user_agent_string`, indicadores `UiAutomation` que respetan los servicios de Accesibilidad y soporte Dual-SIM.
- **[vvb2060/Ims](https://github.com/vvb2060/Ims)** — `Instrumentation` privilegiada (`--no-restart`), claves para omitir bloqueos MMTEL/Entitlement en Android 14–17 y autoconcesión de permisos.
- **[TakaiSaisei/pixel_ims](https://github.com/TakaiSaisei/pixel_ims)** — reactivación automática de Depuración inalámbrica (`WRITE_SECURE_SETTINGS` + `adb_wifi_enabled`), reaplicación tras el arranque al conectar Wi‑Fi (`JobScheduler`), modo desacoplado para Android 12–13 y lectura en vivo desde `ICarrierConfigLoader`.
- **[VinujaHerath/PixelIMS](https://github.com/VinujaHerath/PixelIMS)** — indicador `--no-hidden-api-checks`, opción ViLTE, acceso directo a ajustes de cada SIM y aplicación simultánea a todas las SIM.
- **[flyfishxu/kadb](https://github.com/flyfishxu/kadb)** y **[LSPosed/HiddenApiBypass](https://github.com/LSPosed/HiddenApiBypass)** — cliente integrado ADB over TLS / SPAKE2 y acceso a APIs ocultas de Android.

---

## Licencia

[MIT](./LICENSE) — basado en [charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle) (© charlie-xing).
