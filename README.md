# mobile-app-ecociente
Repositório do aplicativo mobile do projeto EcoCiente

## Área do síndico (demonstração)

As telas `SindicoHomeActivity` e `SindicoAvisosActivity` estão separadas do login atual. O app usa Firebase Authentication para entrar, mas ainda não possui um papel de síndico validado nem uma API de autorização desse perfil. Na compilação `debug`, há um ícone separado **EcoCiente Síndico (demo)** para abrir a home e revisar o fluxo.

Os dados do calendário, indicadores, gráfico, ranking e avisos vêm de `SindicoRepository`. A demonstração inclui cinco blocos no ranking e oito avisos. Essa é a camada a substituir por uma API ou consulta autorizada ao banco. O formulário de notificação valida título e mensagem e salva apenas um rascunho em memória, que aparece na lista durante a sessão. Ele não envia avisos a moradores nem grava no servidor.
