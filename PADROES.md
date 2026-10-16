# EcoCiente (mobile) — padrões para quem (ou qual IA) mexer no código

Antes de criar ou alterar qualquer código, leia `docs/padroes-mobile.md`. Ele é a fonte da verdade
dos padrões do app (52 regras). Siga-o em toda tela, classe e recurso novos. Resumo do que mais importa:

## Estrutura
- Java, MVVM, pacotes por camada: `model`, `repository`, `viewmodels`, `views`, `ui`.
- Fluxo: Tela -> ViewModel -> Repository -> API. A tela nunca faz HTTP. Rede fora da thread principal.
- APIs externas usam o JWT de `AutenticacaoExternaRepository` (`tokenParaUso` / `renovarToken`).
  Timeout de 180 s (cold start do Render). Falha devolve `null`/vazio, nunca derruba a tela.

## Interface
- Títulos de aba: rosa, 22sp, `nunito_bold`. Telas com barra inferior não têm seta de voltar.
- Tela nova chama `Motion.entrar(this, ids...)` depois do `setContentView`.
- Cartão ou linha clicável recebe `Motion.pressFeedback(view)`.
- Navegação inferior: `BarraNavegacaoView`, `ItensBarra`, `Navegacao`.
- Confirmação: `DialogoEco`. Aviso/erro curto: `AvisoFlutuante.mostrar(...)`. Nunca `AlertDialog` ou `Toast`
  direto (exceção: a tela fecha em seguida com `finish()`).
- Nenhum texto fixo no código: use `strings.xml` (`strings_sindico.xml` para o síndico).
- Todo ícone ou imagem tem `contentDescription`. Dimensões em dp, texto em sp.

## Segurança e dados
- Nunca coloque senha, token, chave ou segredo no código, em logs ou em commits.
- Dado de exemplo só em `DadosDemonstracao` e nas classes de modelo/repositório. Se existir API, o exemplo sai.
- O perfil vem da API (`USUARIO_COMUM`, `MORADOR`, `SINDICO`, `COOPERATIVA`); o Firestore acompanha a API.

## Git
- Nunca commitar nem dar push na `main`. Use branch própria e PR.
- Mensagens `feat:` / `fix:` / `refactor:` / `test:` em português. Não adicione `Co-Authored-By` de IA:
  o commit é feito no nome da pessoa.
- Não versionar `bin/`, `build/`, `*.log`, `.class`.

## Antes de dizer que terminou
Rode `assembleDebug` e `lintDebug` (builds locais precisam de
`FACEBOOK_APP_ID=x FACEBOOK_CLIENT_TOKEN=x FACEBOOK_LOGIN_PROTOCOL_SCHEME=fbx`). Não desative avisos para passar.
Se uma regra do `docs/padroes-mobile.md` conflitar com o pedido, avise em vez de ignorá-la.
