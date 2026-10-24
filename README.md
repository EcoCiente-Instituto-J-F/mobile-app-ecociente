<p align="center">
  <img src="assets/card-readme.png" alt="Card do repositório" width="100%">
</p>

# mobile-app-ecociente

![Android](https://img.shields.io/badge/Android-API_24+-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-Kotlin_DSL-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-Auth_%7C_Firestore-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Node.js](https://img.shields.io/badge/Node.js-24-5FA04E?style=for-the-badge&logo=nodedotjs&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-backend-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![GitHub repo size](https://img.shields.io/github/repo-size/EcoCiente-Instituto-J-F/mobile-app-ecociente?style=for-the-badge)
![GitHub last commit](https://img.shields.io/github/last-commit/EcoCiente-Instituto-J-F/mobile-app-ecociente?style=for-the-badge)
![License](https://img.shields.io/badge/license-MIT-green?style=for-the-badge)

Repositório do aplicativo mobile do projeto EcoCiente

## Área do síndico (demonstração)

As telas `SindicoHomeActivity` e `SindicoAvisosActivity` estão separadas do login atual. O app usa Firebase Authentication para entrar, mas ainda não possui um papel de síndico validado nem uma API de autorização desse perfil. Na compilação `debug`, há um ícone separado **EcoCiente Síndico (demo)** para abrir a home e revisar o fluxo.

Os dados do calendário, indicadores, gráfico, ranking e avisos vêm de `SindicoRepository`. A demonstração inclui cinco blocos no ranking e oito avisos. Essa é a camada a substituir por uma API ou consulta autorizada ao banco. O formulário de notificação valida título e mensagem e salva apenas um rascunho em memória, que aparece na lista durante a sessão. Ele não envia avisos a moradores nem grava no servidor.

## Instalando

```bash
git clone https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente.git
cd mobile-app-ecociente
```

Pré-requisitos: Android Studio (compileSdk 36, minSdk 24) e JDK 17. O build exige `FACEBOOK_APP_ID`, `FACEBOOK_LOGIN_PROTOCOL_SCHEME` e `FACEBOOK_CLIENT_TOKEN`, lidas de variáveis de ambiente ou de um `.env` na raiz. O `.env.example` da raiz documenta tanto as variáveis do build Android quanto as do **backend** (`backend/`).

## Usando

Abra a pasta no Android Studio e execute o módulo `app`, ou compile pela linha de comando:

```bash
./gradlew assembleDebug      # Windows: .\gradlew.bat assembleDebug
```

## Testes

O CI executa testes unitários, lint e build:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

## Estrutura do projeto

```
.
├── app/                   # aplicativo Android (Java, MVVM)
│   └── src/main/java/com/example/ecociente/
│       ├── model/  repository/  viewmodels/  views/  ui/  notifications/
├── backend/               # funções serverless (Node.js 24) para a Vercel
│   ├── api/               # notificacaoMotivacional.js
│   └── lib/               # firebaseAdmin.js, postgres.js
├── docs/padroes-mobile.md # padrões do app
├── PADROES.md             # resumo dos padrões
├── firestore.rules        # regras do Firestore
├── k8s/                   # job de build do app
├── Dockerfile
└── scripts/pr-bot/        # geração automática da descrição de PR
```

## Contribuindo

Leia o [CONTRIBUTING.md](CONTRIBUTING.md) antes de abrir uma Pull Request. Ao participar, você concorda com o [Código de Conduta](CODE_OF_CONDUCT.md).

## Segurança

Encontrou uma vulnerabilidade? Siga o [SECURITY.md](SECURITY.md). **Não abra issue pública.**

## Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE).
