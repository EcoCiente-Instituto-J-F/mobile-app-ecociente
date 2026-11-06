# Demo de morador mockada — plano de implementação

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** APK `demo` do EcoCiente que mostra o perfil morador de ponta a ponta, sem internet e sem servidor, com dados fictícios e ações que têm efeito.

**Architecture:** Build type `demo` com API falsa no processo, abaixo de `HttpURLConnection`: telas, ViewModels, repositórios e parsers rodam o código real. Firebase é desviado por uma costura `ContaAtual` em `main`. O mock vive só em `app/src/demo`.

**Tech Stack:** Java 11 (`sourceCompatibility`), Android minSdk 24, `org.json`, JUnit 4, OkHttp 4.12 (já é dependência).

**Spec:** `docs/superpowers/specs/2026-11-06-demo-morador-mockado-design.md`

## Global Constraints

- Leia `PADROES.md` e `docs/padroes-mobile.md`. Mensagens de commit `feat:`/`fix:`/`refactor:`/`test:` em português, **sem** `Co-Authored-By`. Nunca commite na `main`.
- Nenhuma dependência nova. Não altere `.github/workflows`, `Dockerfile` nem `k8s/`.
- `assembleDebug`, `lintDebug` e `testDebugUnitTest` têm de continuar válidos. Fora do desvio com provedor registrado, produção não muda de comportamento.
- Todo código da API falsa em `app/src/demo/java/com/example/ecociente/demo/`, sem classes Android, exceto `EcoCienteDemoApplication`, `ConexaoImagemCloudinary` e os pontos marcados.
- Só use a API de `org.json` que existe também no Android (`opt*`, `get*`, `put`, `isNull`, `length`, `toString`).
- Textos de tela em `strings.xml`. Conteúdo fictício servido pela API fica em `SementeDemo`. Nomes e dados 100% fictícios.
- **Este ambiente não tem Android SDK nem Gradle funcional** (Google e Maven bloqueados). Não tente instalar nem baixar de mirror. Verificação local só pelo harness da Task 1. Relate no report o que ficou sem verificação.
- Valores fixos: login `morador@ecociente.demo`; senha `demo1234`; `usuarioId` 42; `condominioId` 7; morador Marina Alves, Torre B, apto 42, Condomínio Jardim das Palmeiras; perfil API `MORADOR`; ranking 480, 410, 350, 320 (Marina), 180; descarte aprovado rende +50 e leva cerca de 20 s (`APROVACAO_MS = 20_000`); atraso de rede `ATRASO_MS = 300`.

## Review Focus

- O app é fechado com descarte em análise: ao reabrir, depois dos 20 s, ele já consta aprovado e os pontos entram uma vez só.
- "Sair" durante uma aprovação pendente: nada do estado antigo volta depois do reset, incluindo favoritos, progresso de leitura e lembretes.
- `demo_estado.json` corrompido ou ausente: o app sobe com a semente, sem crash.
- Datas da semente em domingo, fim de mês e virada de ano: sempre existe coleta na semana atual e uma próxima coleta futura.
- `getResponseCode()` e `getInputStream()` chamados em qualquer ordem e mais de uma vez, de threads diferentes: um POST é processado uma única vez.

---

### Task 1: Harness JVM local (fora do repositório)

**Files:** criar em `/tmp/claude-0/-home-claude/56481b70-b6d7-536d-a9b8-a3e11eb2da9e/scratchpad/harness-demo/`. Nada entra no git.

**Interfaces:**
- Produces: `rodar-testes.sh <classe de teste>...` e `checar-tipos.sh`, usados por todas as tasks.

- [ ] **Step 1: Escrever os shims** em `shims/`: `org.json` (`JSONObject`, `JSONArray`, `JSONException`, semântica do Android, parser e serializador completos, `JSONException` checada), `androidx.annotation` (`NonNull`, `Nullable`, `StringRes`, `DrawableRes`, ...), `android.util.Log` (imprime nada), `android.content.Intent` vazio, `com.example.ecociente.R` mínimo para os modelos compilarem, `com.example.ecociente.BuildConfig`, e `com.google.firebase.auth.FirebaseAuth`/`FirebaseUser` com só `getInstance`, `getCurrentUser`, `getUid`, `getEmail`, `getDisplayName`.
- [ ] **Step 2: `rodar-testes.sh`**: `javac --release 11` com `-sourcepath` = shims, `app/src/main/java`, `app/src/demo/java`, `app/src/test/java`, `app/src/testDemo/java`; classpath `/opt/gradle-8.14.3/lib/junit-4.13.2.jar:/opt/gradle-8.14.3/lib/hamcrest-core-1.3.jar`; saída em diretório temporário; roda `org.junit.runner.JUnitCore` com as classes passadas. Não use o Gradle de `/opt`: ele não resolve o AGP aqui.
- [ ] **Step 3: `checar-tipos.sh [arquivo...]`**: `javac -proc:none -XDshould-stop.ifError=FLOW -Xmaxerrs 100000 -d <tmp>` nos arquivos dados, com `-sourcepath` igual, imprimindo só erros cujo texto **não** seja de pacote/símbolo Android/AndroidX/Firebase ausente (filtre `package .* does not exist` e `cannot find symbol` cujo símbolo seja classe desses pacotes). Serve para achar `incompatible types` e símbolo errado sem SDK.
- [ ] **Step 4: Validar** com testes existentes: `rodar-testes.sh com.example.ecociente.repository.RankingJsonTest com.example.ecociente.repository.PontuacaoJsonTest com.example.ecociente.repository.PostagemJsonTest com.example.ecociente.repository.TentativaQuizJsonTest com.example.ecociente.repository.QuizJsonTest com.example.ecociente.repository.CalendarioJsonTest com.example.ecociente.repository.PerfilApiJsonTest`. Esperado: OK em todos. Falha por semântica do shim se corrige no shim, nunca no repo.
- [ ] **Step 5:** Sem commit. Documente no report os comandos.

### Task 2: Costura `ContaAtual` em `main`

**Files:**
- Create: `app/src/main/java/com/example/ecociente/model/UsuarioLogado.java`, `.../repository/ContaAtual.java`
- Modify: `LoginRepository`, `PerfilRepository`, `SenhaUsuarioRepository`, `AutenticacaoExternaRepository` (linhas de `getCurrentUser` em `garantirPerfil`, `persistirToken` e `obterTokenSalvo`; **não** em `renovarPeloFirebase`), `FavoritosRepository`, `model/FluxoEducativo`, `model/PerfilUsuario`, `viewmodels/PerfilViewModel`, `viewmodels/HomeViewModel`, `notifications/EcoCienteFirebaseMessagingService`, e as telas `Biblioteca`, `CondominioHome`, `CooperativaHome`, `GerenciarPerfil`, `GerenciarSenha`, `InformacoesPessoais`, `InicioQuiz`, `Main`, `Notificacoes`, `Quiz`, `SindicoHome`, `SplashScreen`
- Test: `app/src/test/java/com/example/ecociente/repository/ContaAtualTest.java`

**Interfaces:**
- Produces:
```java
public final class UsuarioLogado { // getters com os nomes do FirebaseUser, para as chamadas não mudarem
    public UsuarioLogado(@NonNull String uid, @Nullable String email, @Nullable String nome);
    @NonNull public String getUid(); @Nullable public String getEmail(); @Nullable public String getDisplayName();
}
public final class ContaAtual {
    public interface Provedor {
        @Nullable UsuarioLogado usuario();
        @NonNull ResultadoLogin entrar(@NonNull String email, @NonNull String senha);
        void sair();
        @Nullable PerfilUsuario perfil(@NonNull String uid);
        @NonNull ResultadoApi atualizarCampo(@NonNull String uid, @NonNull String campo, @NonNull Object valor);
        @NonNull PreferenciasNotificacao preferencias(@NonNull String uid);
        @NonNull ResultadoApi atualizarPreferencia(@NonNull String uid, @NonNull String chave, boolean valor);
        @NonNull ResultadoApi alterarSenha(@NonNull String senhaAtual, @NonNull String novaSenha);
        @NonNull ResultadoApi excluirConta(@NonNull String senhaAtual);
    }
    public static void usar(@Nullable Provedor provedor);
    @Nullable public static Provedor substituto(); // null em produção
    @Nullable public static UsuarioLogado usuario(); // substituto, ou FirebaseAuth convertido
}
```

- [ ] **Step 1: Teste que falha.** `ContaAtualTest`: `usuario_comProvedor_devolveODoProvedor`; `usar_null_removeOProvedor` (`assertNull(ContaAtual.substituto())`); `nomeExibicao_semNomeNoPerfil_usaODoUsuario` e `emailExibicao_semEmailNoPerfil_usaODoUsuario` em `PerfilUsuario` com `new UsuarioLogado("u","m@x.y","Nome")`. Limpe com `ContaAtual.usar(null)` em `@After`.
- [ ] **Step 2: Rode e veja falhar:** `rodar-testes.sh com.example.ecociente.repository.ContaAtualTest`.
- [ ] **Step 3: Implemente `UsuarioLogado`, `ContaAtual`** e troque `FirebaseUser` por `UsuarioLogado` em `PerfilUsuario`, `PerfilRepository.usuarioAtual()`, `PerfilViewModel`, `HomeViewModel` e nas telas e serviço listados. `FirebaseAuth.getInstance().getCurrentUser()` nesses arquivos vira `ContaAtual.usuario()`. `SindicoUnidadesRepository`, `CadastroRepository`, `PushRegistroRepository` e `renovarPeloFirebase` continuam no Firebase direto.
- [ ] **Step 4: Desvios.** No início, quando `ContaAtual.substituto() != null`, entregando o resultado no mesmo `MutableLiveData` com `setValue`:
  - `LoginRepository.signInWithEmailAndPassword` → `entrar`; `LoginRepository.sair` → `sair`.
  - `PerfilRepository`: `buscarPerfil`, `carregarPerfil` (`perfil == null` → `ResultadoPerfil.inexistente()`), `atualizarCampo`, `buscarPreferencias`, `atualizarPreferencia`. `encerrarSessao` → `sair` e `AutenticacaoExternaRepository.encerrarSessao`, sem `PushRegistroRepository` nem `signOut`. `excluirConta` → `excluirConta` e, em sucesso, `AutenticacaoExternaRepository.encerrarSessao`.
  - `SenhaUsuarioRepository.alterarSenha` → `alterarSenha` e, em sucesso, `AutenticacaoExternaRepository.atualizarSenhaSalva(contexto, usuario.getEmail(), novaSenha)`.
- [ ] **Step 5: `model/Aviso`:** adicione `public static void definirFonte(@Nullable java.util.function.Supplier<List<Aviso>> fonte)` e a fábrica `public static Aviso de(int id, String titulo, String mensagem, CategoriaAviso categoria, boolean deCooperativa, String remetente, long horasAtras, boolean lida)`. `lista()` usa a fonte, ou a lista vazia sem fonte.
- [ ] **Step 6: Verifique.** `rodar-testes.sh com.example.ecociente.repository.ContaAtualTest com.example.ecociente.PerfilAcessoTest`: OK. `checar-tipos.sh` em todos os arquivos modificados: nenhum erro envolvendo `UsuarioLogado`, `ContaAtual`, `FirebaseUser` ou `incompatible types`. `grep -rn 'FirebaseUser' app/src/main/java` só pode restar em `ContaAtual`, `CadastroRepository`, `PerfilRepository` (privado de exclusão), `SenhaUsuarioRepository`, `AutenticacaoExternaRepository.renovarPeloFirebase`, `SindicoUnidadesRepository` e `PushRegistroRepository`. Imports sem uso removidos.
- [ ] **Step 7: Commit** `refactor: isola a sessao do usuario em ContaAtual`.

### Task 3: Build `demo`, transporte, conta e login

**Files:**
- Modify: `app/build.gradle.kts`, `app/src/main/res/layout/fragment_login.xml`, `app/src/main/res/values/strings.xml`, `views/LoginFragment.java`, `views/Login.java`
- Create em `app/src/demo/`: `AndroidManifest.xml`, `google-services.json`, `res/values/strings_demo.xml`, e em `java/.../demo/`: `EcoCienteDemoApplication`, `FabricaConexoesDemo`, `ConexaoDemo`, `ConexaoImagemCloudinary`, `ApiDemo`, `RespostaDemo`, `EstadoDemo`, `SementeDemo`, `ArmazemDemo`, `ArmazemDemoArquivos`, `RotasConta`, `ProvedorContaDemo`
- Test em `app/src/testDemo/java/com/example/ecociente/`: `demo/ConexaoDemoTest`, `demo/EstadoDemoTest`, `demo/ProvedorContaDemoTest`, `repository/DemoContaContratoTest`

**Interfaces:**
- Consumes: `ContaAtual.Provedor`, `UsuarioLogado`.
- Produces:
```java
// Todas as classes demo usadas em teste são public: os testes de contrato ficam no pacote repository.
public final class RespostaDemo { public final int codigo; public final byte[] corpo; public RespostaDemo(int codigo, String json); public String texto(); public static RespostaDemo erro(int codigo, String codigoNegocio); }
public interface TratadorDemo { RespostaDemo tratar(String metodo, String url, @Nullable String autorizacao, byte[] corpo); }
public interface ArmazemDemo { @Nullable String ler(); void salvar(String json); void apagarTudo(); String gravarFoto(byte[] jpeg); } // gravarFoto devolve URL file://
public final class ApiDemo implements TratadorDemo {
    public ApiDemo(EstadoDemo estado, java.util.function.LongSupplier relogio);
    public RespostaDemo tratar(String metodo, String url, @Nullable String autorizacao, byte[] corpo); // synchronized; 404 com log da rota se não houver handler
    public void processarPendentes();           // aprovações vencidas (Task 5)
    public void definirOuvinte(@Nullable OuvinteDemo ouvinte);
}
public final class EstadoDemo { // JSON único; synchronized
    public EstadoDemo(ArmazemDemo armazem, java.util.function.LongSupplier relogio); // carrega do armazém; JSON inválido ou ausente → semente
    public JSONObject raiz(); public void salvar(); public void resetar();
}
```

- [ ] **Step 1: Testes que falham.**
  - `ConexaoDemoTest` (com `TratadorDemo` falso contando chamadas, atraso 0): `post_processaUmaVezSo` chamando `getResponseCode()`, `getInputStream()` e `getResponseCode()` → 1 chamada; `getInputStream_primeiro_funciona`; `erro400_getInputStreamLanca_getErrorStreamTemCorpo`; `corpoEnviado_chegaIgual` com `setFixedLengthStreamingMode`; `hostDesconhecido_lancaIOException`; `duasThreads_umaChamadaSo`; `leituraCloudinary_naoPassaPelaApi` (`https://res.cloudinary.com/ncwfxyfi/image/upload/...` abre pela função de imagem injetada, em GET, e o tratador não é chamado); `uploadCloudinary_passaPelaApi` (`https://api.cloudinary.com/v1_1/x/image/upload` chega ao tratador).
  - `EstadoDemoTest`: `jsonCorrompido_voltaParaSemente`; `alteracao_sobreviveARecarregar`; `resetar_apagaArquivoEFotos_eRecriaSemente`.
  - `DemoContaContratoTest` (pacote `repository`, para usar os parsers package-private): login correto → 200 com `token`, `usuarioId` 42, `perfil` `MORADOR`; senha errada → `PoliticaErroLogin.credenciaisRecusadas(codigo, corpo)` é `true`; `GET /api/usuarios/me` sem token → 401; com token, `PerfilApiJson.condominio` dá id 7 e nome da semente, e `PerfilApiJson.perfilDaApi` traz telefone e endereço; health check `GET /` em `ds-pontuacao-api` → 200.
  - `ProvedorContaDemoTest`: `entrar` errado → erro e `usuario()` null; `entrar` certo → `usuario()` com uid; `PerfilAcesso.destinoDaHome(perfil)` é `CONDOMINIO`; `atualizarCampo("nome")` reflete em `perfil` e em `GET /me`; `alterarSenha` com senha atual errada → erro, certa → novo login vale; `sair` limpa a sessão e reseta; `excluirConta` com senha certa reseta; sessão sobrevive a recriar `EstadoDemo` no mesmo armazém.
- [ ] **Step 2: Rode e veja falhar.**
- [ ] **Step 3: `ConexaoDemo`:** subclasse de `HttpURLConnection` que acumula o corpo em `ByteArrayOutputStream`, processa uma única vez sob trava, dorme `ATRASO_MS`, e implementa `connect`, `disconnect`, `usingProxy`, `getResponseCode`, `getResponseMessage`, `getInputStream`, `getErrorStream`, `getOutputStream`, `getHeaderField("Content-Type"/"Content-Length")`. `getRequestProperty("Authorization")` guardado pela própria classe. Em status ≥ 400, `getInputStream` lança `IOException`.
- [ ] **Step 4: `FabricaConexoesDemo implements URLStreamHandlerFactory`** (construtor `(TratadorDemo, long atrasoMs, Function<URL, HttpURLConnection> abrirImagem)`; `abrirImagem` é `ConexaoImagemCloudinary::new` no app, e falso nos testes): só `http`/`https`; demais protocolos → `null`. Hosts para `ApiDemo`: `ds-mobile-api.onrender.com`, `ds-pontuacao-api.onrender.com`, `ia-eko.onrender.com`, `api.52.71.117.143.sslip.io`, `api.cloudinary.com`, `viacep.com.br`. `res.cloudinary.com` → `ConexaoImagemCloudinary` (GET com OkHttp, expõe código, stream e `Content-Length`): as mídias do catálogo continuam vindo do Cloudinary real sempre que há internet, e sem internet o `CarregadorMidias` usa o fallback. Só a leitura é real; upload nunca sai do aparelho. Outro host → conexão que lança `IOException` em `connect`/`getInputStream`.
- [ ] **Step 5: `EstadoDemo`, `SementeDemo` (só conta, perfil e condomínio por ora; o restante em cada task), `ArmazemDemoArquivos` (grava em temporário e renomeia), `RotasConta`, `ProvedorContaDemo`.** Forma do `/api/usuarios/me`: leia `PerfilApiJson`, incluindo `condominios[].vinculo.aprovado`, `telefones[]`, `endereco`. Saída do `perfil`: `tipoPerfil` `usuario`, `tipoUsuario` `morador`, `possuiCodigoCondominio` `true`. `ProvedorContaDemo` recebe um `Runnable aoResetar`.
- [ ] **Step 6: `EcoCienteDemoApplication extends EcoCienteApplication`**, no `onCreate` antes de `super`: cria armazém em `filesDir`, `EstadoDemo`, `ApiDemo`; `URL.setURLStreamHandlerFactory`; `ContaAtual.usar`; agenda `api.processarPendentes()` a cada 5 s em `ScheduledExecutorService`; `aoResetar`: para cada arquivo em `shared_prefs/`, `getSharedPreferences(nome).edit().clear().commit()` (não use `deleteSharedPreferences`: o Android mantém a instância em cache) antes disso, para cada agendamento da semente, `LembretesColeta.ativado` → `desativar`, porque o cancelamento lê essas preferências.
- [ ] **Step 7: Build.** Em `build.gradle.kts`: `buildConfig = true`; `defaultConfig` com `buildConfigField` `boolean MODO_DEMO=false`, `String DEMO_EMAIL=""`, `String DEMO_SENHA=""`; `buildTypes { create("demo") { initWith(getByName("debug")); applicationIdSuffix = ".demo"; versionNameSuffix = "-demo"; buildConfigField com true e os valores fixos } }`. `AndroidManifest.xml` do demo com `tools:replace="android:name,android:label"`, `android:name=".demo.EcoCienteDemoApplication"`, label `@string/app_name_demo` ("EcoCiente Demo"). `google-services.json` com `package_name` `com.example.ecociente.demo`, projeto `ecociente-demo` e chave fictícia no formato aceito pelo Firebase (39 caracteres iniciando com `AIzaSy`, ex.: `AIzaSyDEMO0000000000000000000000000000000`) e app id `1:000000000000:android:0000000000000000`. Esse arquivo é barrado pelo `.gitignore`: use `git add -f`.
- [ ] **Step 8: Login.** `fragment_login.xml`: `TextView` `textoAvisoDemo`, `gone`. `strings.xml`: `demo_aviso_login` ("Versão de demonstração, dados fictícios"), `demo_indisponivel` ("Indisponível na demonstração"). Em `LoginFragment`, se `BuildConfig.MODO_DEMO`: preencher campos, mostrar aviso; Google, Facebook e "esqueci a senha" mostram `AvisoFlutuante`. Em `Login`, o mesmo para `abaCadastro`.
- [ ] **Step 9: Verifique.** Os quatro testes passam; `checar-tipos.sh` limpo nos arquivos desta task e em `src/demo`.
- [ ] **Step 10: Commit** `feat: adiciona build demo com transporte falso e conta de morador`.

### Task 4: Pontuação, ranking e calendário

**Files:** Create `demo/RotasPontuacao.java`, `demo/RotasCalendario.java`; Modify `SementeDemo`, `ApiDemo`. Test `testDemo/.../repository/DemoPontuacaoContratoTest`, `DemoCalendarioContratoTest`.

**Interfaces:** Produces, em `EstadoDemo`: `int saldo()`, `void creditar(String origemTipo, int pontos)` (POSTAGEM ou QUIZ, grava movimentação com `ocorridoEm` e atualiza o saldo), `int posicaoDoUsuario()`. Ranking ordena por pontos desc., depois por nome.

- [ ] **Step 1: Testes que falham, via parsers reais.** `PontuacaoJson.saldo` = 320; `movimentacoes` com ao menos 2 itens e `ultimaPagina` `true`; `posicao` = (4, 320, 5); `RankingJson.moradores` com `limite=3` dá 3 itens e o 1º `titulo` começa com `1º`, ordem 480/410/350. `creditar("POSTAGEM", 50)` → saldo 370, posição 3, e Marina no pódio. Empate: Marina com 350 fica antes de quem vier depois no nome, de forma determinística. Calendário com relógio injetado em domingo, em 31/dez e no último dia de fevereiro: `doPeriodo` da semana (segunda a domingo) tem ao menos 1 item por `CalendarioJson.solicitacao` com status válido em `StatusAgendamento.daApi`; `/proxima` é sempre futura; envelope `{content, last}` respeita `dataInicio`/`dataFim`.
- [ ] **Step 2: Rode e veja falhar.**
- [ ] **Step 3: Implemente.** Leia `PontuacaoRepository`, `RankingExternoRepository`, `CalendarioExternoRepository` para rotas e query. Sem `limite`, devolve todos. Semente de agendamentos relativa ao relógio: 4 passadas, uma em cada dia futuro da semana quando houver, e 3 nas semanas seguintes, datas em `yyyy-MM-dd'T'HH:mm:ss`, cooperativa fictícia.
- [ ] **Step 4: Testes passam.**
- [ ] **Step 5: Commit** `feat: adiciona pontuacao, ranking e calendario ao demo`.

### Task 5: Feed, descarte e aprovação

**Files:** Create `demo/RotasFeed.java`, `demo/RotasUpload.java`, `demo/OuvinteDemo.java`; Modify `ApiDemo`, `SementeDemo`, `EcoCienteDemoApplication`. Test `testDemo/.../repository/DemoFeedContratoTest`.

**Interfaces:**
- Consumes: `EstadoDemo.creditar`, `posicaoDoUsuario`, `ArmazemDemo.gravarFoto`.
- Produces: `public interface OuvinteDemo { void aoAprovarDescarte(boolean subiuNoRanking); }`

- [ ] **Step 1: Testes que falham.** Via `PostagemJson`:
  - `feed_semente_temPostagensDeOutros`, com `statusValidacao` aprovada e `urlFoto` `android.resource://com.example.ecociente.demo/drawable/img_green*`.
  - `categorias_temIdsPositivos` por `PostagemJson.categorias`.
  - `upload_multipart_gravaSoOJpeg`: corpo multipart com `upload_preset` e `file` → `ArmazemDemo` em memória recebe exatamente os bytes do JPEG; resposta tem `secure_url`.
  - `registrar_criaEmAnalise`: aparece em `/minhas` como "Em análise" e fora do `feed`.
  - `mesmoHash_devolve409`: `PostagemJson.mensagemDoErro` traz a mensagem de `FOTO_JA_UTILIZADA`.
  - `aprovacao`: em `19_999` ms `processarPendentes` não muda nada; em `20_000` vira "Aprovada", entra no `feed`, saldo 370, ouvinte chamado com `subiuNoRanking=true`; segunda chamada não credita de novo.
  - `reabrir_depoisDoPrazo_aprovaUmaVez`: recria `EstadoDemo`/`ApiDemo` no mesmo armazém com relógio avançado; `GET /minhas` já mostra "Aprovada", saldo 370.
  - `resetar_comPendente_naoRessuscita`.
  - Voto: primeiro `aprovar` → sucesso; repetido → `VOTO_JA_REGISTRADO`; na própria → `VOTO_NAO_PERMITIDO`; id inexistente → `POSTAGEM_NAO_ENCONTRADA`; `denunciar` sem motivo → `MOTIVO_DENUNCIA_OBRIGATORIO`.
- [ ] **Step 2: Rode e veja falhar.**
- [ ] **Step 3: Implemente.** Rotas em `PostagemRepository`; `tratar` e `processarPendentes` verificam pendências vencidas antes de responder. Multipart: boundary pelo próprio corpo, extrai a parte `name="file"` em bytes, sem converter para `String`. Application: ouvinte chama `NotificacaoEcoCiente.exibir(ctx, ECOREGISTRO_APROVADO, Collections.emptyMap())` e, se subiu, `RANKING_SUBIU`.
- [ ] **Step 4: Testes passam.**
- [ ] **Step 5: Commit** `feat: adiciona feed e ciclo de descarte ao demo`.

### Task 6: Educação e quiz

**Files:** Create `demo/RotasEducacao.java`, `demo/RotasQuiz.java`, `demo/ConteudoEducativoDemo.java`; Modify `ApiDemo`. Test `testDemo/.../repository/DemoQuizContratoTest`.

**Interfaces:** Consumes `EstadoDemo.creditar`.

- [ ] **Step 1: Testes que falham.** `cursos`: `/api/cursos` e `/api/cursos/{id}/aulas` com IDs positivos e `ordem`, e ao menos 5 cursos. `quiz`: 3 aulas com quiz por `QuizJson.montar`, ao menos 3 perguntas cada, IDs de pergunta e alternativa > 0, `pontosRecompensa` > 0, `notaMinimaAprovacao` 70; outra aula → 404. `tentativa`: `tentativaId > 0`. `finalizar_tudoCerto`: via `TentativaQuizJson.corpoFinalizar` e `resultado`: nota 100, aprovado, pontos = recompensa, saldo sobe, `origemTipo` QUIZ. `finalizar_reprovado`: 0 pontos e saldo igual. `repetir_aprovado_naoPontuaDeNovo`. `revisao`: `TentativaQuizJson.comGabarito` marca a correta e traz explicação. `naoRespondidas_contamComoErro`. `historico`: `HistoricoQuizJson.desempenhos` com mais recente primeiro. `concluidos`: `QuizzesConcluidosJson.ids` só com aprovados. O quiz devolvido por `/api/aulas/{id}/quiz` não expõe `correta`.
- [ ] **Step 2: Rode e veja falhar.**
- [ ] **Step 3: Implemente.** Conteúdo adaptado de `model/DadosDemonstracaoEducacao` com IDs positivos. Leia `EducacaoRepository`, `TentativaQuizRepository`. Recompensas 40, 30 e 50.
- [ ] **Step 4: Testes passam.**
- [ ] **Step 5: Commit** `feat: adiciona educacao e quiz ao demo`.

### Task 7: Eko, CEP, avisos e edição de perfil

**Files:** Create `demo/RespostasEko.java`, `demo/RotasDiversas.java`, `demo/AvisosDemo.java`; Modify `RotasConta`, `ApiDemo`, `EcoCienteDemoApplication`. Test `testDemo/.../demo/RespostasEkoTest`, `testDemo/.../repository/DemoPerfilContratoTest`.

**Interfaces:** Produces `public final class RespostasEko { public static String responder(String mensagem, EstadoDemo estado); }`. `AvisosDemo` é Android-free mas usa `model/Aviso` e `CategoriaAviso`.

- [ ] **Step 1: Testes que falham.** `RespostasEkoTest`: "onde jogo pilha?" menciona pilha; "ÓLEO de cozinha" (acento e caixa) menciona óleo; "quantos pontos eu tenho" contém o saldo atual, inclusive depois de `creditar`; "qual a próxima coleta" contém a data do próximo agendamento; "posição no ranking" contém a posição atual; texto sem palavra-chave devolve resposta padrão não vazia. `DemoPerfilContratoTest`: chat em `ia-eko.onrender.com/api/v1/chat` e `api.52.71.117.143.sslip.io/api/v1/chat` → `answer` e `session_id` não vazios, mantendo o `session_id` recebido; `PATCH /api/usuarios/me` com `nome`, `urlAvatar`, `telefone` reflete em `PerfilApiJson.perfilDaApi`; `PATCH .../endereco` também; `DELETE /api/usuarios/me` → 2xx; `POST /api/notificacoes/dispositivos` → 2xx; `viacep.com.br/ws/01001000/json/` → JSON no formato de `ConsultaCepRepository`; CEP inválido → `{"erro":true}`; troca de senha do repositório em `/senhas/` → 2xx.
- [ ] **Step 2: Rode e veja falhar.**
- [ ] **Step 3: Implemente.** Palavras-chave da spec: pilha, óleo, vidro, próxima coleta, pontos, ranking. Normalize com `java.text.Normalizer`. Leia `AtualizacaoExternaRepository`, `ConsultaCepRepository`, `AlterarSenhaExternaRepository`. No Application, `Aviso.definirFonte(AvisosDemo::lista)` com 3 avisos fictícios do síndico, mantendo a mesma lista para `marcarComoLida` valer.
- [ ] **Step 4: Testes passam.**
- [ ] **Step 5: Commit** `feat: adiciona eko, avisos e edicao de perfil ao demo`.

### Task 8: Fluxo completo e documentação

**Files:** Create `testDemo/.../repository/DemoFluxoCompletoTest.java`, `docs/demo-morador.md`; Modify `README.md`, `docs/padroes-mobile.md` (nota na regra 15).

- [ ] **Step 1: Teste do roteiro.** `DemoFluxoCompletoTest.roteiroDoStand`: login → saldo 320 e posição 4 → registrar descarte → 20 s → saldo 370 e posição 3 → quiz aprovado de 40 → saldo 410 → `sair` (`resetar`) → login → saldo 320, posição 4, sem postagem própria nem quiz concluído. `todaRotaDoMorador_naoDevolve404`: percorre a lista de rotas da tabela 3.3 da spec.
- [ ] **Step 2: Rode todos os testes demo e os de parser:** `rodar-testes.sh` com todas as classes `Demo*`, `ConexaoDemoTest`, `EstadoDemoTest`, `ProvedorContaDemoTest`, `RespostasEkoTest`, `ContaAtualTest` e os testes de parser da Task 1. Esperado: OK.
- [ ] **Step 3: Documente.** `docs/demo-morador.md`: como gerar (`./gradlew assembleDemo`, testes com `./gradlew testDemoUnitTest`), `app/build/outputs/apk/demo/app-demo.apk`, login, reset, e checklist manual em modo avião por tela: login, home/pódio, descarte com câmera e notificação após 20 s, feed e voto, quiz, guia e favoritos, calendário e lembrete, ranking, pontos, avisos, Eko, perfil, foto, senha, sair e reset, além de "sem erro de rede em nenhuma tela". README: seção curta com link. Regra 15: "O build `demo` é a exceção: dados fictícios em `app/src/demo`, fora dos APKs `debug` e `release`."
- [ ] **Step 4: Commit** `docs: documenta a demo de morador e o roteiro de validacao`.
