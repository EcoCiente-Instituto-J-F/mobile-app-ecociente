# Contribuindo com o mobile-app-ecociente

Ao participar, você concorda em seguir o [Código de Conduta](CODE_OF_CONDUCT.md). Para reportar um problema de segurança, **não** abra uma issue — siga o [SECURITY.md](SECURITY.md).

## Idioma

Documentação, issues, Pull Requests e mensagens ao usuário em português do Brasil.

## Configurando o ambiente

1. Instale o Android Studio (SDK 36) e o JDK 17.
2. Clone o repositório:
   ```bash
   git clone https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente.git\n   cd mobile-app-ecociente\n   ```\n3. Crie um `.env` na raiz com `FACEBOOK_APP_ID`, `FACEBOOK_LOGIN_PROTOCOL_SCHEME` e `FACEBOOK_CLIENT_TOKEN` (ou exporte as três variáveis). Para compilar sem login real, use valores fictícios, como o CI faz.\n4. Abra a pasta no Android Studio e execute o módulo `app`.\n5. O backend (`backend/`, Node.js 24 na Vercel) tem variáveis próprias, descritas no mesmo `.env.example`.

## Testando sua mudança

O CI roda, nesta ordem:

```bash
./gradlew testDebugUnitTest --no-daemon --stacktrace
./gradlew lintDebug --no-daemon --stacktrace
./gradlew assembleDebug --no-daemon --stacktrace
```

Sem `.env`, defina as variáveis do Facebook na linha de comando (por exemplo, `FACEBOOK_APP_ID=x FACEBOOK_CLIENT_TOKEN=x FACEBOOK_LOGIN_PROTOCOL_SCHEME=fbx`).

Não desative avisos do lint para passar no CI.

## Regras do projeto

- Nenhum segredo (chaves, tokens, senhas) no repositório.
- Nenhum dado pessoal real em testes, fixtures ou exemplos.
- Nenhuma dependência nova sem discussão prévia.
- Leia `PADROES.md` e `docs/padroes-mobile.md` (fonte da verdade dos padrões do app) antes de alterar código.
- Java, MVVM, pacotes por camada (`model`, `repository`, `viewmodels`, `views`, `ui`); a tela nunca faz HTTP e a rede roda fora da thread principal.
- Nenhum texto fixo no código: use `strings.xml` (`strings_sindico.xml` para o síndico).
- Confirmações com `DialogoEco` e avisos curtos com `AvisoFlutuante`; não use `AlertDialog` nem `Toast` direto.
- Todo ícone ou imagem tem `contentDescription`; dimensões em dp e texto em sp.
- Dado de exemplo só em `DadosDemonstracao` e nas classes de modelo/repositório.
- Nunca commite nem dê push direto na `main`; use branch própria e PR.
- Não versionar `bin/`, `build/`, `*.log` nem `.class`.
- As regras do Firestore (`firestore.rules`) acompanham a API: o perfil vem da API.

## Pull requests

1. Crie uma branch a partir da `main` (ex.: `feat/nome-da-mudanca`).
2. Faça commits focados com prefixos `feat:`, `fix:`, `chore:`, `docs:`, `test:` ou `refactor:`.
3. Abra a PR para a `main`. O bot do projeto preenche a descrição automaticamente (PRs de forks não recebem esse recurso).
4. O CI precisa passar (quando houver) e um mantenedor revisa antes do merge.

## Reportando bugs e propondo funcionalidades

Abra uma issue com o comportamento esperado, o observado, os passos para reproduzir e a versão. Use dados fictícios. Para vulnerabilidades, siga o [SECURITY.md](SECURITY.md).
