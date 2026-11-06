# EcoCiente Demo — Aplicativo mockado offline

Os build types **debug** (Run padrao do Android Studio) e **demo** na branch `feat/demo-morador-mockado`. Toda identidade visual,
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

Windows PowerShell na raiz do projeto (agora o Run padrao em **debug** e tambem e 100% local):

```powershell
git fetch origin
git switch feat/demo-morador-mockado
git pull --ff-only
.\gradlew.bat assembleDebug
```

**Instale o APK debug mockado** em `app/build/outputs/apk/debug/app-debug.apk`.
Se preferir `assembleDemo`, o APK fica em `app/build/outputs/apk/demo/app-demo.apk`.
Ambos `assembleDebug` e `assembleDemo` produzem a simulacao. A build `release` e a unica que continua sendo o app normal conectado a APIs reais.

O app demo/debug tem id `com.example.ecociente.demo`; pode ser instalado ao lado
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
- Recuperacao de senha demo: codigo local de quatro digitos **2026**, sem envio de email real.
- Botoes Google/Facebook simulam login do email digitado, sem OAuth real.
- Imagens remotas substituidas por recursos locais existentes no aplicativo.
- Requisicoes simuladas sem atraso artificial, sem dependencia AWS/Render/Firebase/Neon.

O estado persiste no dispositivo. Para reiniciar completamente a demonstracao,
limpe os dados do aplicativo EcoCiente Demo nas configuracoes do Android.

## Limites

Esta versao nao usa OAuth real, nao faz cadastro de novas contas, nao envia notificacao push real,
sincronizacao entre dispositivos nem mapas/rotas reais. Respostas sem rota
ainda devem ser identificadas no log `EcoCienteDemo`. Nao usar credenciais
de producao nesta versao. O APK precisa ser validado em um aparelho.

## Verificacao rapida

Ao abrir o aplicativo no celular, Android > Configuracoes > Apps deve mostrar o pacote `com.example.ecociente.demo` (rotulo **EcoCiente Demo**). Se aparecer `com.example.ecociente`, voce ainda esta rodando o build real.

No perfil Morador, os dados iniciais incluem 320 pontos, cinco participantes no ranking e coletas agendadas, todos predefinidos. O acesso por email e senha dos quatro perfis e local; nao e necessario cadastrar usuario. Para ver o estado inicial novamente, limpe os dados do pacote `.demo` nas configuracoes do Android.

Para conferir a ausencia de rede, ative modo aviao antes do login. O build debug/demo remove `android.permission.INTERNET` e nao deve tentar autenticar no Firebase ou AWS.
