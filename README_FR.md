# Pixel IMS & APN Tuner

**Language / Мова / Язык / Idioma / 语言:** [English](./README.md) | [Беларуская](./README_BY.md) | [Українська](./README_UA.md) | [Bahasa Indonesia](./README_ID.md) | [Bahasa Melayu](./README_MS.md) | [Deutsch](./README_DE.md) | [Español](./README_ES.md) | [Filipino](./README_FIL.md) | [**Français**](./README_FR.md) | [Português](./README_PT.md) | [日本語](./README_JA.md) | [简体中文](./README_CN.md) | [繁體中文](./README_TW.md) | [한국어](./README_KO.md) | [Русский](./README_RU.md)

**Activez VoLTE, VoWiFi, VoNR (appels 5G), 5G SA et les profils APN opérateur automatiques sur Google Pixel 6–11 — sans Root, sans PC et sans application externe (Shizuku).**

📦 Télécharger : **[Releases → pixel-ims-apn-tuner.apk](https://github.com/4plashy-disprized/pixel-ims-apn-tuner/releases/latest)**

---

### Quel problème cette application résout-elle ?

Les smartphones Google Pixel n'activent par défaut la VoLTE, les appels Wi‑Fi (VoWiFi) et la 5G que pour les opérateurs figurant sur la liste blanche de Google. Avec une carte SIM d'un opérateur ou d'un pays non certifié, on rencontre généralement ces problèmes :

- **Les appels basculent en 3G/2G** (ou échouent là où la 3G est coupée) et la connexion internet mobile s'interrompt pendant les appels.
- **Les options VoLTE, Appels Wi‑Fi et 5G sont totalement absentes** des paramètres SIM.
- Même avec des patchs classiques, **la VoLTE peut se bloquer pendant 30 minutes après un passage en 3G** (ascenseur, métro, parking — erreur `invalid P-CSCF`), ou les MMS, le renvoi d'appel et la VoWiFi ne fonctionnent pas faute d'APN `ims` / `xcap` / `mms` adaptés.
- Certains opérateurs vérifient l'identifiant SIP `User-Agent` et **rejettent les Google Pixel non certifiés**, bien que leur modem Samsung Shannon soit pleinement compatible.

**Pixel IMS & APN Tuner** règle tout cela dans une seule application autonome (~800 Ko) :
1. **Base `CARRIER_DB` intégrée de 461 opérateurs** (extraite du firmware du **Samsung Galaxy S26** équipé du modem Shannon) : configure automatiquement les APN permanents **Internet, IMS, MMS et XCAP** protégés contre les blocages 3G.
2. **Débloque VoLTE, VoWiFi, VoNR, 5G SA, ViLTE et les appels de secours (Cross-SIM)**, et applique si besoin le **User-Agent IMS du Samsung S26** (avec 74 profils opérateurs dédiés).
3. **Sans Root, sans PC et sans Shizuku** — Google Wallet, les applications bancaires et les mises à jour Android fonctionnent normalement.

---

### Démarrage rapide (2 minutes)

#### Étape 1. Association unique (une seule fois)

1. **Connectez-vous à n'importe quel réseau Wi‑Fi** (domicile, bureau ou partage de connexion d'un autre téléphone).
   > ⚠️ Le Wi‑Fi n'est requis qu'au moment de la configuration ; ensuite, la VoLTE et la 5G fonctionnent sur le réseau mobile sans Wi‑Fi.
2. **Activez les « Options pour les développeurs »** *(si ce n'est pas déjà fait)* :
   Ouvrez **Paramètres → À propos du téléphone**, descendez tout en bas et **appuyez 7 fois sur « Numéro de build »** jusqu'au message confirmant le mode développeur.
3. **Activez le « Débogage sans fil »** :
   Allez dans **Paramètres → Système → Options pour les développeurs → Débogage sans fil**, activez-le et cochez *« Toujours autoriser sur ce réseau »*.
4. **Associez l'application** :
   - Ouvrez **Pixel IMS & APN Tuner** et accordez les autorisations Téléphone et Notifications.
   - Appuyez sur **« Démarrer l'association »** — une notification s'affiche et l'écran « Débogage sans fil » s'ouvre.
   - Appuyez sur **« Associer l'appareil avec un code d'association »** — un code à **6 chiffres** s'affiche.
   - **Laissez cette boîte ouverte**, déroulez le volet des notifications, appuyez sur la notification de l'application, saisissez les 6 chiffres et envoyez.
5. Revenez dans l'application — la carte ADB indique **Prêt**. Vous n'aurez plus jamais besoin de refaire cette association !

#### Étape 2. Appliquer les paramètres à votre SIM

1. Sur la carte de votre SIM, les options recommandées pour votre opérateur sont **déjà précochées**.
2. Appuyez sur **« Appliquer les paramètres »** (ou **« Appliquer à toutes les SIM »** en double SIM).
3. En quelques secondes, l'état passe à **« IMS : enregistré »** avec la liste des fonctions actives.
4. **Si vous avez activé la VoWiFi :** appuyez sur **« Ouvrir les paramètres SIM »** → **Appels Wi‑Fi** et vérifiez que l'interrupteur est activé.

---

### Guide complet de toutes les options et boutons

#### Indicateurs de la carte SIM

- **`CARRIER_DB: ...`** — votre SIM est reconnue dans la base de 461 opérateurs (`LTE_WIFI` = VoLTE + VoWiFi ; `LTE_ONLY` = VoLTE uniquement).
- **`IMS : enregistré` / `non enregistré`** — confirme en direct que le téléphone est connecté au serveur vocal de l'opérateur.
- **`Actif dans le système : ...`** — affiche les drapeaux réellement actifs dans Android.
- **Cases grisées `(profil système)`** — protègent les fonctions déjà prises en charge d'origine par Google tout en permettant d'activer celles qui manquent.

#### Options principales

| Option | Explication simple |
| :--- | :--- |
| **VoLTE (appels 4G)** | Appels voix HD en 4G LTE sans basculer en 3G/2G et sans coupure des données mobiles pendant l'appel. **Toujours recommandé.** |
| **VoWiFi (appels Wi‑Fi + icône)** | Permet d'appeler et d'envoyer des SMS via n'importe quel Wi‑Fi même sans réseau mobile (sous-sols, zones blanches ou à l'étranger au tarif local). *À activer si votre opérateur propose les Appels Wi‑Fi.* |
| **VoNR (appels 5G)** | Maintient les appels vocaux directement en 5G sans retomber en 4G. |
| **5G SA + seuils de signal NR** | Active dans Android la **5G NSA et 5G SA** (`[1, 2]`), empêche la pile IMS de couper la 5G SA en l'absence de VoNR (`ims.sa_disable_policy_int = 0`) et aligne l'**échelle des barres de réception 5G (`SS-RSRP`)** de la barre d'état sur celle de la 4G LTE (`[-128, -118, -108, -98]` dBm au lieu des seuils stricts `[-110, -90, -80, -65]` dBm affichant 0 barre dès `-111` dBm). *Ne modifie pas la sensibilité radio physique du modem.* |
| **Profils APN (Internet, IMS, MMS, XCAP) depuis CARRIER_DB** | Enregistre les APN vérifiés du Samsung S26 (`Internet`, `MMS`, `IMS` protégé contre les blocages 3G de 30 min et `XCAP`). **Conservés de façon permanente après redémarrage.** |

#### Section « Avancé ▸ »

| Option avancée | Utilité |
| :--- | :--- |
| **User-Agent IMS Samsung S26 (Shannon)** | Présente le modem au réseau opérateur comme un **Samsung Galaxy S26** certifié (avec 74 profils spécifiques). Résout les rejets VoLTE/VoWiFi sur les Pixel non certifiés. |
| **ViLTE (appels vidéo 4G/5G)** | Active les appels vidéo natifs via l'application Téléphone sur le réseau mobile. |
| **VoWiFi en itinérance** | Autorise les Appels Wi‑Fi à l'étranger et débloque le choix du mode d'itinérance. |
| **Services supplémentaires (UT / XCAP)** | Permet de gérer le renvoi d'appel et le double appel directement en 4G/Wi‑Fi sans repasser en 2G/3G. |
| **Cross-SIM / Appels de secours** | En double SIM, permet à une SIM sans réseau de passer des appels et SMS **via les données mobiles de la seconde SIM** (idéal avec une eSIM de voyage). |
| **Icône LTE+ / 4G+ pour toute agrégation** | Supprime le seuil caché de largeur de bande du Pixel afin d'afficher `LTE+` / `4G+` dès qu'il y a agrégation de fréquences. |
| **Autoriser l'ajout d'APN dans les paramètres** | Déverrouille l'ajout et la modification manuels des APN dans les paramètres Android. |

#### Boutons d'action & « Options »

- **« Appliquer les paramètres »** — enregistre et applique les réglages pour cette SIM.
- **« Réinitialiser »** — supprime les modifications et restaure les APN d'usine.
- **« Ouvrir les paramètres SIM »** — ouvre directement les paramètres système de cette SIM.
- **« Appliquer à toutes les SIM »** *(si 2 SIM actives)* — configure les deux SIM en un seul geste.
- **« Réappliquer au démarrage »** *(recommandé sur Android 16 QPR2+ et Android 17)* — la **VoLTE de base et les APN** restent permanents ; cette option attend le Wi‑Fi après un redémarrage, réveille automatiquement le Débogage sans fil si nécessaire et restaure en arrière-plan les fonctions `CarrierConfig` (**VoWiFi**, **5G / VoNR**, **User-Agent S26**, **Cross-SIM**).

---

### Questions fréquentes et dépannage (FAQ)

<details>
<summary><b>1. J'ai appuyé sur « Appliquer les paramètres », mais il est toujours écrit « IMS : non enregistré ». Que vérifier ?</b></summary>

1. Vérifiez que **l'option VoLTE est bien activée sur votre forfait** auprès de votre opérateur.
2. Assurez-vous que le téléphone capte la **4G (LTE) ou la 5G**.
3. Si votre opérateur ne propose pas la VoWiFi, décochez *VoWiFi* et appuyez à nouveau sur *Appliquer les paramètres*.
4. Activez le **Mode avion** pendant 5 à 10 secondes puis désactivez-le.
</details>

<details>
<summary><b>2. Les réglages disparaissent-ils après un redémarrage (même sans Wi‑Fi) ?</b></summary>

- **La VoLTE de base (appels 4G) et tous les APN (`Internet`, `IMS`, `MMS`, `XCAP`) NE disparaissent PAS au redémarrage** — même sous Android 16 QPR2 / Android 17 et sans Wi‑Fi, car ils sont inscrits durablement dans `telephony.db`.
- **Sous Android 12–15 et Android 16 avant QPR2**, tout est conservé de façon permanente (`[persistent]`).
- **Sous Android 16 QPR2+ et Android 17**, seules les fonctions propres à `CarrierConfig` (**VoWiFi**, **5G / VoNR**, **User-Agent S26**, **Cross-SIM**, **ViLTE**, **icône LTE+**) sont réinitialisées à l'extinction et restaurées automatiquement dès la connexion à un Wi‑Fi connu si *« Réappliquer au démarrage »* est coché.
</details>

<details>
<summary><b>3. La carte ADB indique « Non connecté » alors que j'ai déjà fait l'association. Faut-il réassocier ?</b></summary>

**Non !** Connectez-vous simplement à un réseau Wi‑Fi approuvé et appuyez sur **« Revérifier »** — l'application réactive automatiquement le Débogage sans fil s'il s'est désactivé après un redémarrage.
</details>

<details>
<summary><b>4. Il est écrit « IMS : enregistré », mais les appels Wi‑Fi (VoWiFi) ne passent pas.</b></summary>

Appuyez sur **« Ouvrir les paramètres SIM »** → **Appels Wi‑Fi** → activez l'interrupteur et choisissez **« Appeler via le Wi‑Fi »**. Pour tester, activez le Mode avion, rallumez le Wi‑Fi et passez un appel.
</details>

<details>
<summary><b>5. Est-ce sans risque pour Google Wallet, les banques et les mises à jour Android ?</b></summary>

**Totalement sûr.** Aucun Root ni déverrouillage du bootloader, aucune connexion internet externe (tout passe en local sur `127.0.0.1`). Google Wallet, les applications bancaires et les mises à jour OTA fonctionnent normalement.
</details>

<details>
<summary><b>6. Comment tout remettre à l'état d'usine ?</b></summary>

Appuyez sur **« Réinitialiser »** sur la carte SIM lorsque le canal ADB est prêt, ou désinstallez l'application, redémarrez le téléphone et rétablissez les APN par défaut dans Android.
</details>

---

### Résolution du blocage de 30 minutes après passage en 3G via les masques binaires de l'APN `ims`

Sans APN `ims` dédié (ou avec un APN créé manuellement dont le masque vaut `0` = tous réseaux y compris 2G/3G), lors d'une brève chute en **3G** (ascenseur, métro, parking), Android tente de maintenir l'`IMS PDN` en 3G, reçoit `P-CSCF = []` vide et `ShannonImsService` déclenche `BLOCK_PDN_BY_INVALID_PCSCF = 1`, bloquant VoLTE et VoWiFi pendant **30 à 90 minutes**. `ApnProvisioner` configure l'APN `ims` avec des masques binaires excluant 2G/3G (`network_type_bitmask` et `lingering_network_type_bitmask` = `659456` pour `LTE|IWLAN|NR` ou `528384` pour `LTE|NR`, `bearer_bitmask` = `663552` ou `532480`), suspendant proprement l'IMS en 3G avec reconnexion immédiate au retour de la 4G/5G.

---

### Compilation depuis les sources

```sh
./build.sh    # génère pixel-ims-apn-tuner.apk
```

#### Mettre à jour `CARRIER_DB` (`generate-carrier-db.py`)

`res/raw/carrier_db.json` est déjà fourni dans le dépôt. Pour le régénérer vous-même via [`generate-carrier-db.py`](./generate-carrier-db.py) (Python 3 standard) :

<details>
<summary><b>Fichiers requis et comment les obtenir</b></summary>

1. **Google Pixel** (`./firmware/pixel/CarrierSettings/`) : `carrier_list.pb`, `others.pb` et `*.pb` — récupérables via ADB sans Root depuis un Pixel (`adb pull /product/etc/CarrierSettings firmware/pixel/CarrierSettings`) ou depuis une image officielle Pixel (`product.img` sur [developers.google.com/android/images](https://developers.google.com/android/images)).
2. **Samsung One UI avec modem Exynos/Shannon** (`./firmware/samsung/`) : `apns-conf.xml`, `build.prop` (optionnel) ainsi que `mnomap.json`, `imsswitch.json`, `imsprofile.json` (dans `res/raw/` de `/system/priv-app/imsservice/imsservice.apk`) — via `adb pull` sans Root depuis un Galaxy Exynos (S24/S25/S26 modèle `B`) ou extraits de `system.img` d'un firmware officiel (`SM-S942B` via `Bifrost` / `samloader`).
3. **Exécution :** `./generate-carrier-db.py --update && ./build.sh` *(le dossier `./firmware/` est ignoré par Git et ne doit jamais être commité).*
</details>

---

### Remerciements et projets ayant inspiré des idées

**Innovation propre à ce projet :** l'idée de configurer automatiquement des **profils APN permanents pour l'IMS** (`Internet`, `IMS` protégé contre le blocage `invalid P-CSCF` en 3G, `MMS`, `XCAP`) et de constituer la base `CARRIER_DB` en croisant la base opérateurs de Google Pixel avec celle du firmware du Samsung Galaxy S26 (équipé de la même famille de modems Shannon) est **notre propre idée et implémentation originale**.

Pour le canal ADB local et la surcharge `CarrierConfig`, ce projet s'appuie également sur des idées de la communauté open-source :

- **[charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle)** — architecture autonome par boucle locale Débogage sans fil + `UiAutomation` (sans Shizuku) et compilation via `build.sh` sans Gradle.
- **[kyujin-cho/pixel-volte-patch](https://github.com/kyujin-cho/pixel-volte-patch)** — pionnier du patch `CarrierConfig` sur Pixel, concept de surcharge `ims.ims_user_agent_string`, drapeaux `UiAutomation` préservant les services d'Accessibilité et gestion double SIM.
- **[vvb2060/Ims](https://github.com/vvb2060/Ims)** — `Instrumentation` privilégiée (`--no-restart`), clés de contournement MMTEL/Entitlement pour Android 14–17 et auto-attribution des autorisations.
- **[TakaiSaisei/pixel_ims](https://github.com/TakaiSaisei/pixel_ims)** — réveil automatique du Débogage sans fil (`WRITE_SECURE_SETTINGS` + `adb_wifi_enabled`), réapplication au démarrage liée au Wi‑Fi (`JobScheduler`), mode détaché sur Android 12–13 et lecture en direct via `ICarrierConfigLoader`.
- **[VinujaHerath/PixelIMS](https://github.com/VinujaHerath/PixelIMS)** — option `--no-hidden-api-checks`, bascule ViLTE, raccourci vers les paramètres SIM et application groupée à toutes les SIM.
- **[flyfishxu/kadb](https://github.com/flyfishxu/kadb)** & **[LSPosed/HiddenApiBypass](https://github.com/LSPosed/HiddenApiBypass)** — client embarqué ADB over TLS / SPAKE2 et accès aux API cachées d'Android.

---

## Licence

[MIT](./LICENSE) — basé sur [charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle) (© charlie-xing).
