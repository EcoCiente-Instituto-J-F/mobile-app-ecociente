# Demo de morador com dados mockados — design

Data: 2026-11-06. Autor: Heitor Shiniti Miasato.

## 1. Objetivo

Ter um APK do EcoCiente para **apresentação em stand** que funciona sem internet e sem servidor, mostrando o
perfil **morador** de ponta a ponta com dados fictícios. "Funcional" significa que as ações têm efeito: registrar
um descarte aparece no feed, passar no quiz soma pontos, o ranking muda.

Critérios de sucesso:

1. Com o aparelho em modo avião, um visitante percorre todo o fluxo de morador sem ver erro de rede.
2. Nenhuma requisição do APK demo chega às APIs de produção, ao Firebase Auth ou ao Firestore.
3. "Sair" deixa o app no estado inicial para o próximo visitante.
4. `assembleDebug`, `lintDebug` e `testDebugUnitTest` continuam funcionando sem mudança no CI, no Dockerfile e no
   job de k8s, e os APKs `debug` e `release` não contêm dados de exemplo novos.

Fora de escopo: telas de síndico e cooperativa, simulação de falhas de rede, publicação em loja, sincronização
entre aparelhos.

## 2. Abordagem

Um **build type `demo`** com uma **API falsa dentro do processo**, no nível do transporte HTTP. Telas, ViewModels,
repositórios e parsers de JSON rodam o código real. Só o que está abaixo de `HttpURLConnection` e o que depende
do Firebase é substituído.

Alternativas descartadas: `if (modoDemo)` em cada repositório (espalha mock em `main` e não exercita os parsers) e
branch separada (fica defasada com os merges diários da equipe).

## 3. Arquitetura

```
Tela → ViewModel → Repository → HttpURLConnection ──► ConexaoDemo ──► ApiDemo ──► EstadoDemo (JSON em disco)
                       │                                                ▲
                       └──► ContaAtual ──► ProvedorContaDemo ───────────┘
                               (em produção: ProvedorContaFirebase)
```

### 3.1 Build

- `buildTypes { create("demo") { initWith(debug); applicationIdSuffix = ".demo"; versionNameSuffix = "-demo" } }`,
  gerando `app-demo.apk` com `./gradlew assembleDemo`.
- `app/src/demo/` contém `java/`, `res/`, `AndroidManifest.xml` e `google-services.json`.
- O manifesto do demo troca a `Application` por `EcoCienteDemoApplication` (`tools:replace="android:name"`) e o
  nome do app para "EcoCiente Demo".
- `google-services.json` do demo só registra o pacote `.demo`, porque o plugin exige um client para o
  `applicationId`. Não precisa de chave real: o Firebase não é chamado por nenhum fluxo do demo.
- `buildConfig = true` e o campo `BuildConfig.MODO_DEMO` (`false` no `defaultConfig`, `true` no `demo`), usado só
  no login. O CI gera `app/google-services.json`, mas o do demo é versionado com valores fictícios.

### 3.2 Transporte (`app/src/demo`)

- `EcoCienteDemoApplication extends EcoCienteApplication` instala, no `onCreate`, antes de qualquer tela:
  `URL.setURLStreamHandlerFactory` e `ContaAtual.usar(new ProvedorContaDemo(...))`.
- O handler devolve `ConexaoDemo extends HttpURLConnection`, que acumula o corpo enviado e, em
  `getResponseCode()`, chama `ApiDemo.tratar(metodo, url, corpo, token)` depois de cerca de 300 ms.
- Hosts tratados: `ds-mobile-api`, `ds-pontuacao-api`, `ia-eko`, o endereço do Eko em `eko_chat_url`,
  `api.cloudinary.com` (upload) e ViaCEP.
- `res.cloudinary.com` (leitura de imagem): a conexão faz o GET com OkHttp, que já é dependência e não passa pelo
  `URL`. Sem internet, dá `IOException` e o `CarregadorMidias` usa o drawable de fallback.
- Qualquer outro host recebe `IOException`. É isso que garante o critério 2.
- `CalendarioExternoRepository.alterarStatus` usa OkHttp direto, mas só é chamado por síndico e cooperativa.

### 3.3 API falsa (`ApiDemo`)

Java puro, sem classes de Android, para rodar em teste de JVM. Recebe método, caminho, query e corpo; devolve
código HTTP e JSON. Um roteador simples despacha para handlers por área (conta, feed, quiz, educação, pontuação e
ranking, calendário, chat, upload).

O relógio é injetado (`LongSupplier`), para os testes controlarem o tempo.

| Área | Endpoints |
|---|---|
| Conta | `POST /auth/login`, `GET/PATCH /api/usuarios/me`, `PATCH /api/usuarios/me/endereco`, `DELETE /api/usuarios/me`, troca de senha em `/senhas/` |
| Pontuação | `GET /api/v1/pontuacoes/usuarios/{id}/saldo`, `.../movimentacoes`, `GET /api/v1/rankings/moradores`, `.../moradores/{id}/posicao` |
| Feed | `GET /api/postagens`, `GET /api/postagens/minhas`, `POST /api/postagens`, `POST /api/postagens/{id}/votos`, `GET /api/categorias-residuos` |
| Educação | `GET /api/cursos`, `/api/cursos/{id}/aulas`, `/api/aulas/{id}/quiz` |
| Quiz | `POST /api/quizzes/{id}/tentativas`, `POST /api/tentativas-quiz/{id}/finalizar`, `GET .../revisao`, `GET /api/tentativas-quiz`, `.../quizzes-concluidos` |
| Calendário | `GET /api/v1/agendamentos` (com período), `GET .../proxima` |
| Chat | `POST` no endereço do Eko, com `answer` e `session_id` |
| Upload | `POST /v1_1/{cloud}/image/upload`: grava a foto em `filesDir` e devolve `secure_url` com `file://` |
| Outros | `POST`/`DELETE /api/notificacoes/dispositivos` e `GET` dos health checks do `AquecimentoDasApis` respondem 200 |

Rotas sem handler devolvem 404, e isso gera log com a rota, para achar lacunas ao testar no aparelho.

### 3.4 Estado (`EstadoDemo`)

- Um objeto único, com acesso sincronizado, serializado como JSON em `filesDir/demo_estado.json` a cada alteração.
- Guarda: conta e senha, saldo e movimentações, moradores do ranking, postagens, votos, hashes de foto,
  tentativas de quiz, agendamentos e dados do perfil.
- `SementeDemo` monta o estado inicial, com datas calculadas a partir de "hoje".
- `resetar()` apaga o arquivo e as fotos e recria a semente.

### 3.5 Costura do Firebase (`main`)

Firestore usa gRPC e `FirebaseUser` não pode ser falsificado sem depender de classes internas da biblioteca, então
essa parte exige uma mudança em `main`, sem efeito no comportamento de produção.

- `model/UsuarioLogado`: `uid`, `email`, `nome`. Substitui `FirebaseUser` nas telas e ViewModels de morador e em
  `PerfilUsuario.nomeExibicao`/`emailExibicao`.
- `repository/ContaAtual`: fachada estática com `usuario()` e `usar(Provedor)`, e a interface `Provedor`.
- `repository/ProvedorContaFirebase`: implementação padrão, com o código que hoje está em `LoginRepository`,
  `PerfilRepository` e `SenhaUsuarioRepository`.

A interface cobre: usuário atual, login com e-mail e senha, sair, buscar e carregar perfil, atualizar campo,
preferências de notificação, alterar senha e excluir conta. Telas de síndico e cooperativa passam a usar
`UsuarioLogado` quando dependerem de método alterado, sem nenhuma outra mudança.

Essa costura vai em **PR próprio**, antes do demo, para a equipe revisar isoladamente.

`ProvedorContaDemo` (`src/demo`) grava o perfil em `EstadoDemo` e, ao sair ou excluir a conta, chama `resetar()`.

### 3.6 Avisos

`Aviso.lista()` e `Notificacao.lista()` hoje devolvem listas vazias fixas. Ganham uma fonte substituível
(`Aviso.definirFonte(...)`), que o demo preenche com avisos fictícios. Em produção continuam vazias.

### 3.7 Login

`LoginFragment`, se `BuildConfig.MODO_DEMO`: preenche e-mail e senha a partir de strings que só têm valor em
`src/demo/res`, e mostra "Versão de demonstração, dados fictícios". Google, Facebook, aba de cadastro e "esqueci a
senha" mostram "Indisponível na demonstração" com `AvisoFlutuante`.

Credenciais erradas passam pelo fluxo real e dão o erro normal de login.

## 4. Roteiro de dados

- **Morador:** Marina Alves, Torre B, apto 42, Condomínio Jardim das Palmeiras. Login `morador@ecociente.demo`,
  senha fictícia de demonstração, preenchida na tela.
- **Ranking:** 5 moradores fictícios com 480, 410, 350, **320 (Marina)** e 180 pontos. O primeiro descarte aprovado
  (+50) a leva ao 3º lugar, que aparece no pódio da home.
- **Descarte:** foto real → "Em análise" → aprovado cerca de 20 s depois, verificado por relógio a cada consulta e
  por um temporizador no processo → entra no feed, soma pontos e movimentação, e dispara
  `NotificacaoEcoCiente` do tipo `ecoregistro_aprovado`, além de `ranking_subiu` quando a posição melhora.
- **Feed:** postagens de outros moradores, com imagens dos drawables existentes por URI `android.resource://`.
  Regras reais: `VOTO_NAO_PERMITIDO`, `VOTO_JA_REGISTRADO`, `FOTO_JA_UTILIZADA`.
- **Educação:** cursos e aulas adaptados de `DadosDemonstracaoEducacao`, com IDs positivos. 3 quizzes com gabarito,
  `notaMinimaAprovacao` e `pontosRecompensa`.
- **Calendário:** coletas passadas e futuras relativas a hoje, com ao menos uma na semana atual. O lembrete local é
  o real.
- **Eko:** respostas por palavra-chave (descarte de pilha, óleo, vidro, próxima coleta, pontos, ranking), usando o
  estado atual, com resposta padrão para o resto. Não é a IA de verdade: quem apresenta deve dizer que, no stand, o Eko
  está em modo offline.
- **Perfil:** nome, telefone, endereço, foto, senha e preferências de notificação persistidos em `EstadoDemo`.
- **Reset:** sair ou excluir a conta. Fechar e reabrir o app mantém o estado e a sessão.

Todos os nomes e dados são fictícios, conforme o `CONTRIBUTING.md`.

## 5. Regras do repositório

- **Regra 15** ("quando existir API, o exemplo sai"): o mock fica em `src/demo`, fora dos APKs de produção. Vale
  citar a exceção na descrição da PR.
- **Regra 11** (textos em `strings.xml`): textos de tela vão para `strings`. Conteúdo fictício servido como resposta
  de API fica em `SementeDemo`, como dado.
- **Senha fictícia:** não é credencial de nada real, e só existe no `src/demo`.
- **Commits:** branch própria, sem `Co-Authored-By` de IA, conforme o `PADROES.md`.

## 6. Testes

- **JVM (`src/testDemo`, com `./gradlew testDemoUnitTest`):** o CI atual só roda `testDebugUnitTest`, então esses
  testes ficam fora dele até alguém adicionar o passo, decisão deixada para a PR do demo. Cada endpoint passa pelo parser real (`PostagemJson`, `PontuacaoJson`, `RankingJson`,
  `QuizJson`, `TentativaQuizJson`, `HistoricoQuizJson`, `CalendarioJson`, `PerfilApiJson`), com casos de sucesso,
  erro e regra de negócio. Como os parsers são package-private, os testes ficam no pacote `repository`.
- **Fluxos:** login → descarte → aprovação após 20 s → saldo e ranking; quiz → pontos; reset → estado inicial.
- **Regressão:** a costura mantém os testes existentes verdes, e `PerfilAcessoTest` continua valendo.
- **Manual (só possível no aparelho ou emulador):** checklist em modo avião cobrindo cada tela. Nada Android
  roda no ambiente em que o código foi escrito, por isso `assembleDemo`, `assembleDebug` e `lintDebug` precisam
  rodar na máquina do Heitor ou no CI.

## 7. Riscos

| Risco | Tratamento |
|---|---|
| `URL.setURLStreamHandlerFactory` só pode ser chamado uma vez, e o comportamento só é validado no aparelho | Primeira etapa do plano é um esqueleto com login e home; falha cedo, antes de escrever os outros endpoints |
| Endpoint esquecido | 404 com log da rota, e checklist manual |
| Costura altera produção sem querer | PR próprio, sem mudança de lógica, e `ProvedorContaFirebase` com o código movido como está |
| Conflito com merges da `main` | Costura pequena, por PR próprio e rápido; o demo só adiciona arquivos novos |
| Fotos do Cloudinary sem internet | Fallback já existente; visual um pouco mais simples |
| Firebase em segundo plano tenta rede (Installations, FCM) | Recebe `IOException` do handler e só registra log |
