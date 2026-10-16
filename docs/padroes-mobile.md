# Padrões do app EcoCiente (mobile)

Regras para manter o app consistente. Valem para qualquer tela ou código novo.

## Estrutura

1. Pacotes por camada: `model`, `repository`, `viewmodels`, `views` e `ui` (utilitários de interface).
2. Chamada de API fica em `repository`. A tela nunca faz HTTP, e a rede roda fora da thread principal.
3. Toda API externa usa o JWT via `AutenticacaoExternaRepository.tokenParaUso`. Em 401 ou 403, renova com `renovarToken` e tenta de novo.
4. O timeout é de 180 s por causa do cold start do Render. A falha não pode derrubar a tela: devolva `null` ou vazio e a tela decide o que mostrar.

## Tela

5. Título de aba (Coletas, Mensagens, Histórico, Ranking e outras): rosa, 22sp, `nunito_bold`, a 22dp do topo. Telas com barra inferior **não** têm seta de voltar.
6. Telas sem barra inferior usam o estilo `BotaoVoltar`.
7. Cartões usam `CartaoComBorda`. Campos de busca usam `CampoBusca`.
8. Toda tela nova chama `Motion.entrar(this, ids...)` logo depois do `setContentView`, com os blocos na ordem de leitura.
9. Todo cartão ou linha clicável recebe `Motion.pressFeedback(view)`.
10. Navegação inferior: use `BarraNavegacaoView`, `ItensBarra` e `Navegacao`. Não crie rota nova à mão.

## Textos e diálogos

11. Nenhum texto fixo no código: use `strings.xml`. Os do síndico ficam em `strings_sindico.xml`.
12. Confirmação usa o `DialogoEco`. Aviso e erro curto usam `AvisoFlutuante.mostrar(this, ...)`. Não use `AlertDialog` nem `Toast` direto. Exceção: quando a tela fecha logo em seguida (`finish()`), o aviso precisa sobreviver a ela e continua `Toast`.
13. Imagem de perfil vai para o Cloudinary (preset `ecociente_perfil`), e a URL é salva no usuário.

## Segurança

14. Nada de senha, token, chave ou segredo no código, no chat ou nos commits. Credenciais ficam no `CredenciaisSeguras` (Keystore).
15. Dado de exemplo fica restrito a `DadosDemonstracao` e às classes de modelo e repositório. Quando existir API, o exemplo sai.

## Git

16. Nunca commitar ou dar push direto na `main`. Use branch própria e PR.
17. Commits pequenos, com mensagem `feat:` ou `fix:` em português, no nome de quem os fez.
18. Não versionar `bin/`, `build/`, `*.log` nem `.class`.
19. Antes do PR: `assembleDebug` e `lintDebug` limpos. Os builds locais precisam de `FACEBOOK_APP_ID=x FACEBOOK_CLIENT_TOKEN=x FACEBOOK_LOGIN_PROTOCOL_SCHEME=fbx`.

## Perfis

20. O perfil vem da API (`USUARIO_COMUM`, `MORADOR`, `SINDICO`, `COOPERATIVA`) e o roteamento é feito em `MainActivity.renderizar`. O Firestore (`tipoPerfil`) acompanha a API, não o contrário. E-mail e senha têm de ser iguais no Firebase e no Postgres.

## Boas práticas gerais (guia Android/Java/SOLID)

Complementam as regras acima. O que o app já cumpre está marcado com ✔; o que ainda falta, com ◻.

### Arquitetura

21. Fluxo de dados: Tela → ViewModel → Repository → API. A tela não acessa API nem banco. ✔
22. ViewModel não depende de `Activity`, `Fragment` ou `View`. Para `Context`, só `Application` (`AndroidViewModel`) ou `getApplicationContext()` recebido por parâmetro. ✔
23. Estado de tela (por exemplo, as perguntas do quiz) viaja na `Intent` ou vive no ViewModel, nunca em `static`. ✔
24. Uma responsabilidade por classe. Se a tela valida, calcula, acessa a API e desenha, separe.
25. Não crie interface ou camada só por criar (KISS e YAGNI). Aplique SOLID quando reduzir acoplamento ou facilitar teste.
26. Não duplique regra de negócio (DRY). Se duas telas calculam o mesmo valor, centralize.

### Android

27. Respeite o ciclo de vida: observe `LiveData` com o dono da tela, remova listeners e cancele animações em `onDestroy` (o `Motion.entrar` já faz isso). ✔
28. Não guarde `Activity`, `View` ou `Context` de tela em objeto que vive mais que a tela.
29. Rede, banco e arquivo nunca na thread principal. ✔
30. Dimensões em `dp`, texto em `sp`. Teste com fonte maior.
31. Toda imagem ou botão de ícone tem `contentDescription`. Não indique status só por cor.
32. Toda tela de dados tem os quatro estados: carregando, sucesso, lista vazia e erro. Lista vazia não é erro.
33. Imagens grandes sempre com Glide.

### Recursos (`res/`)

34. Nomes em minúsculas com `_`: `activity_x`, `item_x`, `dialog_x`, `view_x`, `ic_x`, `img_x`.
35. Cores, textos e dimensões que se repetem ficam em `colors.xml`, `strings*.xml` e `dimens.xml`.
36. Texto de exemplo em layout só com `tools:text`. Valor de demonstração visível vem de `DadosDemonstracao`, no código. ✔
37. Não colocar chave de API, senha ou token em `strings.xml`.

### Java

38. `PascalCase` em classe, `camelCase` em método e variável, `UPPER_SNAKE_CASE` em constante.
39. Campos `private`. Constante em vez de número ou texto mágico.
40. Capture exceção específica quando souber qual é (`IOException`, `JSONException`, `GeneralSecurityException`). `catch (Exception)` só dentro da tarefa de uma thread de rede, para o retorno à tela sempre acontecer, e sempre com `Log`. ✔ (15, todos nesse caso)
41. Valide entrada do usuário e resposta da API nas fronteiras. Não use `null` como erro genérico: diga o motivo.
42. Remova import, variável e método sem uso. Comente o porquê, não o quê.

### APIs, dados e segurança

43. Repositório de API: trate HTTP de erro, timeout e resposta inválida. Não presuma sucesso só porque não houve exceção.
44. Evite registro duplicado ao tentar de novo: reenvie só operação segura.
45. Não grave token, senha, CPF ou dado pessoal em log, notificação ou mensagem de erro. ✔
46. O backend valida e autoriza. O app não é prova de permissão.
47. A pontuação de reciclagem não pode depender só de valor calculado pelo app: o backend valida o evento e aplica as regras oficiais.
48. Permissão (câmera, galeria, notificação) só quando necessária, verificada antes do uso.

### Testes e qualidade

49. Regra de negócio, validação e parser de resposta de API têm teste de unidade, com caso de sucesso, erro e valor inválido. Parser de JSON fica em classe própria (`RankingJson`, `QuizJson`) para testar na JVM. ◻ (faltam os parsers do calendário e do cadastro)
50. Teste sem depender da API de produção (use dados falsos).
51. Antes de concluir: `assembleDebug` e `lintDebug` sem erro, e os testes relevantes passando. Não desative aviso para o build passar.

### Dependências

52. Versões estáveis e compatíveis. Remover dependência sem uso. Não atualizar tudo de uma vez.
