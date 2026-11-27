# Pixel IMS & APN Tuner

**Language / Мова / Язык / Idioma / 语言:** [English](./README.md) | [Беларуская](./README_BY.md) | [Українська](./README_UA.md) | [Bahasa Indonesia](./README_ID.md) | [Bahasa Melayu](./README_MS.md) | [Deutsch](./README_DE.md) | [Español](./README_ES.md) | [Filipino](./README_FIL.md) | [Français](./README_FR.md) | [**Português**](./README_PT.md) | [日本語](./README_JA.md) | [简体中文](./README_CN.md) | [繁體中文](./README_TW.md) | [한국어](./README_KO.md) | [Русский](./README_RU.md)

**Ative VoLTE, VoWiFi, VoNR (chamadas 5G), 5G SA e perfis automáticos de APN de operadora no Google Pixel 6–11 — sem Root, sem PC e sem aplicativos externos (Shizuku).**

📦 Baixar: **[Releases → pixel-ims-apn-tuner.apk](https://github.com/4plashy-disprized/pixel-ims-apn-tuner/releases/latest)**

---

### Qual problema este aplicativo resolve?

Os celulares Google Pixel só trazem VoLTE, Chamadas Wi‑Fi (VoWiFi) e 5G liberados de fábrica para operadoras homologadas pelo Google. Ao usar um chip (SIM/eSIM) em países ou operadoras não homologados (como Vivo, Claro, TIM no Brasil ou operadoras de outros países), costumam ocorrer estes problemas:

- **A internet móvel cai durante as ligações**, e o celular volta para **3G ou 2G** (ou a chamada falha onde o 3G já foi desligado).
- **Os botões de VoLTE, Chamadas Wi‑Fi e 5G simplesmente não aparecem** nas configurações do SIM.
- Mesmo usando patches básicos, **o VoLTE trava por 30 minutos após passar por elevadores ou subsolos** (erro `invalid P-CSCF` ao cair rapidamente para 3G), ou MMS, desvio de chamadas e VoWiFi não funcionam por falta dos APNs `ims` / `xcap` / `mms`.
- Algumas operadoras verificam o cabeçalho `User-Agent` e **recusam aparelhos Google Pixel não homologados**, embora o modem Samsung Shannon do Pixel seja totalmente compatível.

O **Pixel IMS & APN Tuner** resolve tudo isso em um único aplicativo leve (~800 KB):
1. **Base integrada `CARRIER_DB` com 461 operadoras** (extraída do firmware do **Samsung Galaxy S26** com modem Shannon): grava automaticamente os APNs permanentes **Internet, IMS, MMS e XCAP** com proteção contra bloqueios em 3G.
2. **Desbloqueia VoLTE, VoWiFi, VoNR, 5G SA, ViLTE e Chamadas de reserva**, e permite usar o **IMS User-Agent do Samsung S26** (incluindo 74 perfis específicos de operadoras).
3. **Sem Root, sem PC e sem Shizuku** — Carteira do Google (NFC), aplicativos de banco e atualizações do Android continuam funcionando normalmente.

---

### Início rápido (2 minutos)

#### Passo 1. Pareamento único (apenas na primeira vez)

1. **Conecte-se a qualquer rede Wi‑Fi** (casa, trabalho ou roteador de outro celular).
   > ⚠️ O Wi‑Fi só é necessário no momento da configuração; depois disso, o VoLTE e o 5G funcionam normalmente nos dados móveis sem Wi‑Fi.
2. **Ative as «Opções do desenvolvedor»** *(se ainda não estiverem ativas)*:
   Vá em **Configurações → Sobre o telefone**, role até o fim e **toque 7 vezes em «Número da versão»** até aparecer *«Você agora é um desenvolvedor!»*.
3. **Ative a «Depuração por Wi‑Fi»**:
   Vá em **Configurações → Sistema → Opções do desenvolvedor → Depuração por Wi‑Fi**, ative a chave e marque *«Sempre permitir nesta rede»*.
4. **Faça o pareamento no aplicativo**:
   - Abra o **Pixel IMS & APN Tuner** e conceda as permissões de Telefone e Notificações.
   - Toque em **«Iniciar pareamento»** — o app exibirá uma notificação e abrirá a tela de Depuração por Wi‑Fi.
   - Toque em **«Parear dispositivo com código de pareamento»** — aparecerá um código de **6 dígitos**.
   - **Sem fechar essa janela**, puxe a barra de notificações, toque na notificação do app, digite os 6 dígitos e envie.
5. Volte ao aplicativo — o canal ADB mostrará **Pronto**. Você nunca mais precisará repetir o pareamento!

#### Passo 2. Aplicar configurações ao seu SIM

1. No cartão do seu SIM, as opções recomendadas para sua operadora **já vêm marcadas automaticamente**.
2. Toque em **«Aplicar configurações»** (ou **«Aplicar a todos os SIMs»** se usar dois chips).
3. Em poucos segundos aparecerá **«IMS: registrado»** e abaixo **«Ativo no sistema: VoLTE, …»**.
4. **Se você ativou o VoWiFi:** toque em **«Abrir configurações do SIM»** → entre em **Chamadas por Wi‑Fi** e verifique se a chave está ligada.

---

### Guia completo de todas as opções e botões

#### Indicadores no cartão do SIM

- **`CARRIER_DB: ...`** — sua operadora foi encontrada na base de 461 operadoras (`LTE_WIFI` = suporta VoLTE e VoWiFi; `LTE_ONLY` = apenas VoLTE).
- **`IMS: registrado` / `não registrado`** — confirma em tempo real que o aparelho está conectado ao servidor de voz da operadora.
- **`Ativo no sistema: ...`** — mostra quais recursos estão realmente ativos no Android naquele momento.
- **Caixas cinzas `(perfil do sistema)`** — protegem funções que já funcionam de fábrica para esse chip, permitindo ativar as demais.

#### Opções principais do SIM

| Opção | O que faz de forma simples |
| :--- | :--- |
| **VoLTE (chamadas 4G)** | Ligações em voz HD pelo 4G LTE sem cair para 3G/2G e sem cortar a internet móvel durante a chamada. **Sempre recomendado.** |
| **VoWiFi (chamadas Wi‑Fi + ícone)** | Permite fazer e receber ligações e SMS por qualquer Wi‑Fi onde não há sinal de celular (subsolos, áreas internas ou viagens internacionais). *Ative se sua operadora oferecer Chamadas Wi‑Fi.* |
| **VoNR (chamadas 5G)** | Mantém as chamadas de voz diretamente na rede 5G sem voltar para o 4G. |
| **5G SA + limites de sinal NR** | Habilita no Android **5G NSA e 5G SA** (`[1, 2]`), impede que a pilha IMS desative o 5G SA sem VoNR (`ims.sa_disable_policy_int = 0`) e alinha a **escala de barras de sinal 5G (`SS-RSRP`)** da barra de status com a escala padrão do 4G LTE (`[-128, -118, -108, -98]` dBm em vez de `[-110, -90, -80, -65]` dBm, que exibe 0 barras em `-111` dBm). *Não altera a sensibilidade física do modem.* |
| **Perfis de APN (Internet, IMS, MMS, XCAP) do CARRIER_DB** | Grava os APNs verificados do Samsung S26 (`Internet`, `MMS`, `IMS` protegido contra travamentos de 30 min no 3G e `XCAP`). **Ficam salvos permanentemente mesmo após reiniciar.** |

#### Seção «Avançado ▸»

| Opção avançada | Para que serve |
| :--- | :--- |
| **IMS User-Agent do Samsung S26 (Shannon)** | Identifica o modem na rede da operadora como um **Samsung Galaxy S26** homologado (incluindo 74 perfis específicos, como Claro/AMX). Resolve bloqueios de VoLTE/VoWiFi em Pixels não homologados. |
| **ViLTE (videochamadas 4G/5G)** | Ativa videochamadas nativas pelo discador padrão via rede móvel. |
| **VoWiFi em roaming** | Permite usar Chamadas Wi‑Fi no exterior e libera a preferência em roaming. |
| **Serviços suplementares (UT / XCAP)** | Permite configurar encaminhamento de chamadas e chamada em espera direto pelo 4G/Wi‑Fi sem cair para 2G/3G. |
| **Cross-SIM / Chamadas de reserva** | Com dois chips, permite que um SIM sem sinal faça ligações e receba SMS **usando a internet móvel do segundo SIM** (ótimo com eSIM de viagem). |
| **Ícone LTE+ / 4G+ em qualquer agregação** | Remove o limite oculto de largura de banda do Pixel para exibir `LTE+` / `4G+` sempre que houver agregação de frequências. |
| **Permitir adicionar APNs nas configurações** | Desbloqueia a criação e edição manual de APNs nas configurações do Android. |

#### Botões de ação e «Opções»

- **«Aplicar configurações»** — grava e ativa as opções escolhidas para o SIM.
- **«Redefinir»** — limpa as alterações do SIM e restaura os APNs de fábrica.
- **«Abrir configurações do SIM»** — abre diretamente as configurações do Android para esse SIM.
- **«Aplicar a todos os SIMs»** *(com 2 SIMs ativos)* — aplica aos dois chips com um toque.
- **«Reaplicar após iniciar»** *(recomendado no Android 16 QPR2+ e Android 17)* — o **VoLTE básico e os APNs** são permanentes; esta opção aguarda o Wi‑Fi após reiniciar, reativa sozinha a Depuração por Wi‑Fi se necessário e restaura em segundo plano os recursos do `CarrierConfig` (**VoWiFi**, **5G / VoNR**, **User-Agent S26**, **Cross-SIM**).

---

### Perguntas frequentes e solução de problemas (FAQ)

<details>
<summary><b>1. Toquei em «Aplicar configurações», mas continua dizendo «IMS: não registrado». O que verificar?</b></summary>

1. Confirme se o **VoLTE está habilitado na sua linha junto à operadora**.
2. Verifique se o aparelho está conectado em **4G (LTE) ou 5G**.
3. Se sua operadora não suporta VoWiFi, desmarque *VoWiFi* e toque novamente em *Aplicar configurações*.
4. Ative o **Modo avião** por 5–10 segundos e desative.
</details>

<details>
<summary><b>2. As configurações somem ao reiniciar o celular (inclusive na rua sem Wi‑Fi)?</b></summary>

- **O VoLTE básico (chamadas 4G) e todos os APNs (`Internet`, `IMS`, `MMS`, `XCAP`) NÃO somem ao reiniciar** — nem no Android 16 QPR2 / Android 17 e nem sem Wi‑Fi, pois ficam gravados permanentemente no `telephony.db`.
- **No Android 12–15 e Android 16 antes do QPR2**, tudo fica salvo permanentemente (`[persistent]`).
- **No Android 16 QPR2+ e Android 17**, apenas os recursos exclusivos de `CarrierConfig` (**VoWiFi**, **5G / VoNR**, **User-Agent S26**, **Cross-SIM**, **ViLTE**, **ícone LTE+**) reiniciam ao desligar o aparelho e voltam sozinhos em segundos assim que o celular conecta a um Wi‑Fi conhecido com *«Reaplicar após iniciar»* marcado.
</details>

<details>
<summary><b>3. O cartão ADB mostra «Não conectado», mesmo eu já tendo pareado antes. Preciso parear de novo?</b></summary>

**Não!** Basta conectar-se a um Wi‑Fi confiável e tocar em **«Verificar novamente»** — o app religa automaticamente a Depuração por Wi‑Fi caso o Android a tenha desligado após reiniciar.
</details>

<details>
<summary><b>4. Aparece «IMS: registrado», mas as chamadas Wi‑Fi (VoWiFi) não entram.</b></summary>

Toque em **«Abrir configurações do SIM»** → **Chamadas por Wi‑Fi** → ative a chave e escolha **«Ligar por Wi‑Fi»** como preferência. Para testar, ligue o Modo avião, ative o Wi‑Fi e faça uma ligação.
</details>

<details>
<summary><b>5. É seguro para Carteira do Google (NFC), bancos e atualizações do sistema?</b></summary>

**100% seguro.** Não exige Root nem desbloqueio de bootloader, e não faz conexões externas à internet (tudo roda localmente em `127.0.0.1`). Carteira do Google, apps de banco e atualizações OTA funcionam normalmente.
</details>

<details>
<summary><b>6. Como voltar tudo para o padrão de fábrica?</b></summary>

Toque em **«Redefinir»** no cartão do SIM com o canal ADB pronto, ou desinstale o aplicativo, reinicie o celular e redefina os APNs padrão nas configurações do Android.
</details>

---

### Como o bloqueio de 30 minutos ao cair para 3G é resolvido via máscaras de bits do APN `ims`

Sem um APN `ims` dedicado (ou com APN criado manualmente com máscara `0` = todas as redes, incluindo 2G/3G), ao cair brevemente para **3G** (elevador, metrô, subsolo) o Android tenta manter o `IMS PDN` em 3G, recebe `P-CSCF = []` vazio e o `ShannonImsService` ativa `BLOCK_PDN_BY_INVALID_PCSCF = 1`, bloqueando VoLTE e VoWiFi por **30–90 minutos**. O `ApnProvisioner` grava o APN `ims` com máscaras de bits que excluem 2G/3G (`network_type_bitmask` e `lingering_network_type_bitmask` = `659456` para `LTE|IWLAN|NR` ou `528384` para `LTE|NR`, `bearer_bitmask` = `663552` ou `532480`), pausando o IMS em 3G sem bloqueio e reconectando em 1–2 s ao voltar ao 4G/5G.

---

### Compilação a partir do código-fonte

```sh
./build.sh    # gera pixel-ims-apn-tuner.apk
```

#### Atualizar `CARRIER_DB` (`generate-carrier-db.py`)

`res/raw/carrier_db.json` já vem pronto no repositório. Para recriá-lo usando [`generate-carrier-db.py`](./generate-carrier-db.py) (Python 3 puro):

<details>
<summary><b>Arquivos necessários e onde obtê-los</b></summary>

1. **Google Pixel** (`./firmware/pixel/CarrierSettings/`): `carrier_list.pb`, `others.pb` e `*.pb` — extraídos via ADB sem Root de qualquer Pixel (`adb pull /product/etc/CarrierSettings firmware/pixel/CarrierSettings`) ou da imagem oficial do Pixel (`product.img` em [developers.google.com/android/images](https://developers.google.com/android/images)).
2. **Samsung One UI com modem Exynos/Shannon** (`./firmware/samsung/`): `apns-conf.xml`, `build.prop` (opcional) e `mnomap.json`, `imsswitch.json`, `imsprofile.json` (dentro de `res/raw/` em `/system/priv-app/imsservice/imsservice.apk`) — via `adb pull` sem Root de um Galaxy Exynos (S24/S25/S26 modelo `B`) ou extraídos de `system.img` de um firmware oficial (`SM-S942B` via `Bifrost` / `samloader`).
3. **Executar:** `./generate-carrier-db.py --update && ./build.sh` *(a pasta `./firmware/` está no `.gitignore` — nunca envie arquivos de firmware ao Git).*
</details>

---

### Agradecimentos e projetos que inspiraram ideias

**Ideia original deste projeto:** o conceito de configurar automaticamente **perfis permanentes de APN para IMS** (`Internet`, `IMS` com proteção contra bloqueio `invalid P-CSCF` em 3G, `MMS`, `XCAP`) e construir a base `CARRIER_DB` cruzando a base de operadoras do Google Pixel com a base do firmware do Samsung Galaxy S26 (da mesma família de modems Shannon) é **nossa própria ideia e implementação autoral**.

Para o canal ADB local e os ajustes de `CarrierConfig`, o projeto também incorpora ideias da comunidade open-source:

- **[charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle)** — arquitetura autônoma via Depuração por Wi‑Fi local + `UiAutomation` (sem Shizuku) e compilação via `build.sh` sem Gradle.
- **[kyujin-cho/pixel-volte-patch](https://github.com/kyujin-cho/pixel-volte-patch)** — pioneiro no patch de `CarrierConfig` para Pixel, conceito de substituição de `ims.ims_user_agent_string`, flags `UiAutomation` que preservam serviços de Acessibilidade e tratamento Dual-SIM.
- **[vvb2060/Ims](https://github.com/vvb2060/Ims)** — `Instrumentation` privilegiada (`--no-restart`), chaves de desbloqueio MMTEL/Entitlement no Android 14–17 e autoconcessão de permissões.
- **[TakaiSaisei/pixel_ims](https://github.com/TakaiSaisei/pixel_ims)** — reativação automática da Depuração por Wi‑Fi (`WRITE_SECURE_SETTINGS` + `adb_wifi_enabled`), reaplicação no boot ao conectar no Wi‑Fi (`JobScheduler`), modo desacoplado no Android 12–13 e leitura em tempo real via `ICarrierConfigLoader`.
- **[VinujaHerath/PixelIMS](https://github.com/VinujaHerath/PixelIMS)** — flag `--no-hidden-api-checks`, opção ViLTE, atalho direto para configurações do SIM e aplicação em lote para todos os SIMs.
- **[flyfishxu/kadb](https://github.com/flyfishxu/kadb)** e **[LSPosed/HiddenApiBypass](https://github.com/LSPosed/HiddenApiBypass)** — cliente embutido ADB over TLS / SPAKE2 e acesso a APIs ocultas do Android.

---

## Licença

[MIT](./LICENSE) — baseado em [charlie-xing/volte-toggle](https://github.com/charlie-xing/volte-toggle) (© charlie-xing).
