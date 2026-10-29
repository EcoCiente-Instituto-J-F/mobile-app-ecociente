# Entregas de Desenvolvimento de Aplicativos Móveis

Projeto Interdisciplinar 2026, 2º ano. Cada requisito abaixo traz o que foi feito, onde está o código e como demonstrar. Os links apontam para a branch `main`.

Pré-requisitos para rodar o app: veja o [README](../README.md) (arquivos `google-services.json`, `.env` e `local.properties`).

## 1. Controle de acesso

**O que foi feito:** cadastro e login com Firebase Authentication, validação dos campos, sessão que continua aberta ao reabrir o app, e uma tela inicial diferente para cada perfil (usuário comum, morador, síndico e cooperativa). Depois do login, o app também obtém o token (JWT) das APIs do projeto e o renova sozinho quando vence.

- Login e cadastro: [Login.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/views/Login.java), [LoginFragment.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/views/LoginFragment.java), [CadastroEtapa1Fragment.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/views/CadastroEtapa1Fragment.java), [CadastroEtapa2Fragment.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/views/CadastroEtapa2Fragment.java)
- Acesso por perfil e roteamento das telas: [PerfilAcesso.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/model/PerfilAcesso.java), [MainActivity.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/views/MainActivity.java)
- Sessão ao abrir o app: [SplashScreen.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/views/SplashScreen.java)
- Token das APIs e credenciais cifradas no Keystore: [AutenticacaoExternaRepository.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/AutenticacaoExternaRepository.java), [CredenciaisSeguras.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/CredenciaisSeguras.java)
- Teste da regra de acesso por perfil: [PerfilAcessoTest.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/test/java/com/example/ecociente/PerfilAcessoTest.java)

**Como demonstrar:** entrar com cada perfil e ver que cada um cai na sua home; fechar e reabrir o app sem pedir login de novo.

## 2. Recurso de hardware: câmera

**O que foi feito:** a câmera registra o descarte (foto do material) e atualiza o avatar do perfil. O app usa o aplicativo de câmera do sistema (`TakePicture`), por isso não precisa da permissão `CAMERA`. Se o aparelho não tiver aplicativo de câmera, o app avisa em vez de fechar.

- Registro de descarte com foto: [RegistroDescarteActivity.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/views/RegistroDescarteActivity.java)
- Avatar pela câmera ou galeria: [GerenciarPerfilActivity.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/views/GerenciarPerfilActivity.java)

## 3. Consumo de API

**O que foi feito:** o app consome as APIs do projeto (autenticação, cadastro, calendário, pontuação, mobile e o chatbot), com estados de carregamento e de erro com "Tentar novamente". As chamadas rodam fora da thread principal.

- Chamada autenticada, com renovação do token: [ApiAutenticada.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/ApiAutenticada.java)
- Leitura do corpo das respostas: [LeituraHttp.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/LeituraHttp.java)
- Calendário e coletas: [CalendarioExternoRepository.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/CalendarioExternoRepository.java)
- Postagens (feed e descartes): [PostagemRepository.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/PostagemRepository.java)
- Cooperativas e avaliações: [CooperativaRepository.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/CooperativaRepository.java), [AvaliacaoColetaRepository.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/AvaliacaoColetaRepository.java)
- Dashboard do síndico: [DashboardSindicoRepository.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/DashboardSindicoRepository.java)
- Educação (cursos, aulas e quizzes): [EducacaoRepository.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/EducacaoRepository.java)
- Conversas entre condomínio e cooperativa: [ConversaRepository.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/ConversaRepository.java)
- Pontuação e ranking: [PontuacaoRepository.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/PontuacaoRepository.java), [RankingExternoRepository.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/RankingExternoRepository.java)

## 4. Persistência de dados: Firebase

**O que foi feito:** o Firebase Authentication guarda a conta, e o Firestore guarda o perfil de cada usuário (coleção `usuarios`) e as preferências de notificação, e os favoritos de conteúdo ficam no aparelho, separados por usuário. Os dados continuam depois de fechar e reabrir o app.

- Perfil no Firestore: [PerfilRepository.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/PerfilRepository.java)
- Gravação no cadastro: [CadastroRepository.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/CadastroRepository.java)
- Favoritos (no aparelho): [FavoritosRepository.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/FavoritosRepository.java)
- Regras do Firestore: [firestore.rules](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/firestore.rules)

## 5. Chatbot operacional

**O que foi feito:** a tela do assistente (Eko) envia a mensagem do usuário ao serviço de IA (`ia-eko`, `POST /api/v1/chat`) e mostra a resposta. Há tratamento de falha (o serviço fora do ar mostra uma mensagem) e a sessão da conversa é mantida.

- Tela: [ChatActivity.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/views/ChatActivity.java)
- Estado da conversa: [ChatViewModel.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/viewmodels/ChatViewModel.java)
- Chamada ao serviço: [ChatRepository.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/ChatRepository.java)

**Limite conhecido:** o chatbot depende de o serviço `ia-eko` estar no ar. O servidor gratuito do Render pode levar alguns minutos para acordar na primeira mensagem.

O app tem também a conversa entre síndico e cooperativa, que usa a API de conversas (lista, envio de mensagens e marcação de lidas): [MensagensActivity.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/views/MensagensActivity.java), [ConversaActivity.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/views/ConversaActivity.java).

## 6. Perfil com avatar

**O que foi feito:** o usuário vê e edita nome, telefone e endereço, escolhe o avatar pela câmera ou pela galeria (a foto sobe ao Cloudinary e o link é gravado no Firestore e na API) e troca a senha.

- [GerenciarPerfilActivity.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/views/GerenciarPerfilActivity.java), [InformacoesPessoaisActivity.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/views/InformacoesPessoaisActivity.java), [GerenciarSenhaActivity.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/views/GerenciarSenhaActivity.java)
- Upload e sincronização da foto: [FotoPerfilRepository.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/FotoPerfilRepository.java), [AtualizacaoExternaRepository.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/AtualizacaoExternaRepository.java)

## 7. Notificações

**O que foi feito:** o app recebe notificações push do Firebase e as exibe com o visual do Eko (19 tipos, cada um com título, texto, botão e tela de destino), pede a permissão no Android 13 ou mais, e agenda **lembretes locais** de coleta (na véspera, às 18h), que funcionam sem servidor. A tela de configurações liga e desliga os grupos de aviso.

- Recebimento e exibição: [EcoCienteFirebaseMessagingService.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/notifications/EcoCienteFirebaseMessagingService.java), [NotificacaoEcoCiente.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/notifications/NotificacaoEcoCiente.java), [TipoNotificacaoEcoCiente.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/notifications/TipoNotificacaoEcoCiente.java)
- Permissão: [PermissaoNotificacoes.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/notifications/PermissaoNotificacoes.java)
- Lembretes locais de coleta: [LembretesColeta.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/LembretesColeta.java), ativados em [CalendarioColetasActivity.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/views/CalendarioColetasActivity.java)
- Preferências: [NotificacoesActivity.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/views/NotificacoesActivity.java)

**Limite conhecido:** o envio dos pushes depende do servidor de notificações, que está em desenvolvimento. Para demonstrar, ative um lembrete de coleta no calendário, ou envie uma mensagem de teste pelo console do Firebase com o campo `tipo`.

## 8. Celular e tablet

**O que foi feito:** a partir de 600 dp de largura mínima, o app aumenta a escala de tudo (textos, botões e margens) até a tela parecer um celular grande, e limita o conteúdo a uma coluna central. O login em tablet tem um layout próprio, com o cartão centralizado. No celular nada muda.

- Escala e largura máxima: [EcoCienteApplication.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/EcoCienteApplication.java)
- Login em tablet: [activity_login.xml](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/res/layout-sw600dp/activity_login.xml), [dimens.xml](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/res/values-sw600dp/dimens.xml)

## 9. GPS

**O que foi feito:** na tela "Cooperativas próximas", o síndico toca em "Ordenar por distância". O app pede a permissão de localização no momento do toque, obtém a posição (GPS, ou a rede se o GPS estiver desligado) e ordena as cooperativas da mais próxima para a mais distante, calculando a distância de carro pela Routes API do Google (ou pelo `Geocoder` do aparelho, se a chave não estiver configurada). Se a permissão for negada, a localização estiver desligada ou os endereços não forem encontrados, o app avisa e mantém a lista.

- Tela e permissão: [SindicoCooperativasActivity.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/views/SindicoCooperativasActivity.java)
- Distância pela Routes API: [RotasGoogle.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/java/com/example/ecociente/repository/RotasGoogle.java)
- Teste da leitura da Routes API: [RotasGoogleTest.java](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/test/java/com/example/ecociente/repository/RotasGoogleTest.java)

## Qualidade técnica

- **Arquitetura:** MVVM em pacotes `model`, `repository`, `viewmodels`, `views` e `ui`. As APIs seguem arquitetura hexagonal (veja o repositório de cada API).
- **Testes:** 185 testes unitários, em sua maioria da leitura e montagem de JSON, das regras de acesso e da ordenação. Rodam no CI (`./gradlew testDebugUnitTest`). Pasta: [app/src/test](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/test).
- **Segurança:** token e credenciais cifrados no Keystore do Android, backup do app desligado ([data_extraction_rules.xml](https://github.com/EcoCiente-Instituto-J-F/mobile-app-ecociente/blob/main/app/src/main/res/xml/data_extraction_rules.xml)), chaves fora do repositório (`.gitignore`).
- **Tratamento de falha:** carregamento e "Tentar novamente" nas telas que dependem de API; falha de conexão ao renovar o token não derruba a sessão.

## Limitações conhecidas

- O servidor gratuito do Render dorme quando fica sem uso: a primeira chamada pode levar de 1 a 3 minutos.
- A central de avisos ainda não tem API: ela aparece vazia.
- O envio de notificações push depende do servidor de notificações, em desenvolvimento.
