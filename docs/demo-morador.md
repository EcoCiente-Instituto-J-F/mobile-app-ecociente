# EcoCiente Demo — Aplicativo mockado offline

Build type **demo** na branch `feat/demo-morador-mockado`. Toda identidade visual,
telas e animacoes sao as originais do aplicativo EcoCiente. As APIs sao simuladas
localmente; nenhum banco de dados ou servidor externo e necessario para os fluxos mockados.

## Contas ficticias de demonstracao

| Perfil | E-mail | Senha |
|---|---|---|
| Comum | comum@ecociente.com.br | Ecociente2026. |
| Sindico | sindico@ecociente.com.br | Ecociente2026. |
| Cooperativa | cooperativa@ecociente.com.br | Ecociente2026. |
| Morador | morador@ecociente.com.br | Ecociente2026. |

## Compilar

Windows PowerShell na raiz do projeto:

```powershell
git fetch origin
git switch feat/demo-morador-mockado
git pull --ff-only
.\gradlew.bat assembleDemo
```

**Instale apenas o APK demo** em `app/build/outputs/apk/demo/app-demo.apk`.
O `assembleDebug` produz o aplicativo normal, e NAO a simulacao.

O app demo tem id `com.example.ecociente.demo`; pode ser instalado ao lado
do aplicativo original. No primeiro login, o estado demonstrativo e criado
no armazenamento privado do Android.

## Funcionamento

- Login de quatro perfis, senha incorreta recusada, sessao local.
- Curso, aula, quiz, gabarito, historico e pontuacao offline.
- Feed de postagens, descarte com foto, votos e avaliacao do sindico simulados.
- Ranking de moradores e torres, extrato e mudancas de saldo.
- Solicitar e confirmar coletas e consultar calendario.
- Cadastro de cooperativas ficticias, dashboard e avaliacoes.
- Conversas ficticias locais entre condominio e cooperativa.
- Eko offline consultando base educacional e FAQ; nao e uma IA generativa.
- Imagens remotas substituidas por recursos locais existentes no aplicativo.
- Requisicoes simuladas sem atraso artificial, sem dependencia AWS/Render/Firebase/Neon.

O estado persiste no dispositivo. Para reiniciar completamente a demonstracao,
limpe os dados do aplicativo EcoCiente Demo nas configuracoes do Android.

## Limites

Esta versao nao simula Google/Facebook, envio real de notificacao push,
sincronizacao entre dispositivos nem mapas/rotas reais. Respostas sem rota
ainda devem ser identificadas no log `EcoCienteDemo`. Nao usar credenciais
de producao nesta versao. O APK precisa ser validado em um aparelho.
