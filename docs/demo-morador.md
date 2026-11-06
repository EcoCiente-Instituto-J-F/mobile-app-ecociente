# Demo do morador

APK de apresentação do fluxo do morador, sem rede nem Firebase: uma API falsa roda dentro do app (`app/src/demo`). Os dados são fictícios. O comportamento ainda não foi conferido em aparelho: o checklist abaixo é para isso.

## Gerar e instalar

```bash
./gradlew assembleDemo       # Windows: .\gradlew.bat assembleDemo
./gradlew installDemo        # instala no aparelho conectado; Windows: .\gradlew.bat installDemo
./gradlew testDemoUnitTest   # testes da demo; Windows: .\gradlew.bat testDemoUnitTest
```

APK: `app/build/outputs/apk/demo/app-demo.apk`. O `applicationId` termina em `.demo`, então convive com o app normal.

## Login

- E-mail: `morador@ecociente.demo`
- Senha: `demo1234`

Conta fictícia. Começa com 320 pontos, 4º lugar no ranking, nenhum descarte seu e nenhum quiz concluído.

## O que é fictício e o que é real

- Tudo o que o app pede à rede é respondido por dentro do app, sem internet, exceto a leitura de imagens do Cloudinary (próximo item).
- O Eko responde por palavra-chave, não é IA. Ao apresentar, diga que está em **modo offline**.
- As imagens do Cloudinary (guia, feed) carregam se houver internet e, sem ela, caem para a imagem de reserva.
- A foto do descarte fica só no aparelho: o upload é local.
- A aprovação do descarte leva 20 s a 25 s depois do registro, enquanto o Android mantiver o app vivo; se o sistema o encerrar, a aprovação aparece ao reabrir.
- O aviso de aprovação precisa da permissão de notificações do Android 13 ou superior.
- Registrar descarte exige câmera (aparelho com câmera, e a permissão concedida).
- Pontos do quiz: 40, 30 e 50 na primeira aprovação de cada um; repetir não pontua de novo.

## Reset

O estado fica salvo no aparelho e continua de onde parou, mesmo fechando o app. Ele volta ao começo (320 pontos, 4º lugar, sem descarte e sem quiz) só com **Sair** ou **Excluir conta**, que também apagam favoritos, progresso de leitura, preferências e lembretes de coleta. Antes de cada apresentação: Sair e entrar de novo. Para zerar na marra: limpar os dados do app nas configurações do Android.

## Checklist manual (modo avião)

Ligue o modo avião antes de abrir o app. Em nenhuma tela pode aparecer erro de rede.

- [ ] Login com `morador@ecociente.demo` / `demo1234`; senha errada mostra erro.
- [ ] Home e pódio: saldo 320 e 4º lugar.
- [ ] Descarte: abre a câmera, registra a foto, aparece "Em análise" em Meus descartes; após 20 a 25 s (com o app aberto ou em segundo plano e vivo; se o Android o encerrar, ao reabrir) chega a notificação, o saldo vai a 370 e a posição a 3º.
- [ ] Feed e voto: lista de descartes de outros moradores; votar em um em análise; votar de novo é recusado.
- [ ] Quiz: abrir um quiz, ser reprovado (sem pontos), refazer e ser aprovado (recompensa); histórico e revisão com gabarito.
- [ ] Guia e favoritos: cursos e aulas abrem; favoritar e ver na lista de favoritos.
- [ ] Calendário e lembrete: próxima coleta e meses com coletas; ativar lembrete de uma coleta.
- [ ] Ranking: lista de moradores com o usuário destacado.
- [ ] Pontos: extrato com as movimentações, inclusive descarte e quiz.
- [ ] Avisos: lista abre; marcar como lido persiste até o reset.
- [ ] Eko: perguntar "onde jogo pilha", "óleo de cozinha", "vidro", "qual a próxima coleta", "quantos pontos eu tenho" e "minha posição no ranking". Outras perguntas (como "onde descartar") recebem a resposta genérica.
- [ ] Perfil: editar nome, telefone e endereço (CEP `01001000`); salva.
- [ ] Foto de perfil: trocar a foto e ver a nova no perfil.
- [ ] Senha: trocar com a senha atual `demo1234`, sair e entrar com a nova.
- [ ] Sair e reset: após Sair e novo login, volta a 320 pontos, 4º lugar, sem descarte e sem quiz concluído, e sem favoritos.
- [ ] Sem erro de rede em nenhuma das telas acima.

Se uma tela mostrar erro, o log (`adb logcat -s EcoCienteDemo`) registra "Demo sem rota" com o método e o caminho.
