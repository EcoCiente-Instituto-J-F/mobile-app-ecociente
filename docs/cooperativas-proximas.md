# Cooperativas da região do condomínio

## Implementado nesta branch

- Tela Android acessível pela Home do condomínio.
- Consulta autenticada à API real `GET /api/cooperativas?page=0&size=100`.
- Paginação automática: carrega todas as páginas informadas pela API.
- A API retorna o `condominioId` ativo associado ao usuário autenticado; o aplicativo não fixa nem infere o ID.
- Busca por nome, endereço, cidade e estado, sem diferenciar maiúsculas/minúsculas ou acentuação.
- Cartões com endereço, avaliação média, quantidade de avaliações e contato quando os dados são fornecidos pela API.
- Ações de telefone, e-mail e abertura do endereço no Google Maps, somente quando há dados para isso.
- Estados de carregamento, erro, tentativa novamente, lista vazia e busca sem resultados.
- Atualização da lista por gesto de puxar para baixo.
- Seleção de data e horário futuros.
- Confirmação antes do envio.
- Criação da solicitação pelo endpoint existente `POST /api/v1/agendamentos`, usando os IDs reais retornados pela API.
- A solicitação é criada como pendente (`AGENDADO)); a cooperativa continua responsável por aceitar (`CONFIRMADO`) ou recusar (`RECUSADO)) no fluxo existente.

## Dependência do backend

A branch da API `feat/condominio-id-cooperativas-proximas` adiciona `condominioId` à resposta de `GET /api/cooperativas`.

Exemplo de parte da resposta:

```json
{
  "total": 1,
  "pagina": 0,
  "tamanho": 100,
  "totalPaginas": 1,
  "condominioId": 6,
  "cooperativas": []
}
```

O `condominioId` é resolvido usando o usuário autenticado no JWT:

`tb_usuarios.id_usuario → tb_sindicos.usuario_id → tb_sindicos.id_sindico → tb_condominios.sindico_id → tb_condominios.id_condominio`.

Para o registro validado no banco durante esta tarefa, o usuário 61 está associado ao síndico 6 e ao condomínio ativo 6 (Residencial EcoCiente). O Android não contém esse ID como constante.

**Importante:** o backend precisa ser integrado e implantado a partir da branch acima para o aplicativo receber o novo campo. Enquanto a API implantada não retornar `condominioId`, o botão de solicitação fica desabilitado, em vez de enviar um ID incorreto.

## Limitações reais dos dados

- A API lista cooperativas da mesma cidade/estado do condomínio. Ela não calcula distância em quilômetros.
- O serviço ainda não entrega latitude/longitude; por isso a tela abre o endereço no Google Maps, mas não exibe distância ou mapa com pins próprios.
- O bairro só pode ser pesquisado quando estiver presente no endereço devolvido pela API.
- Contato e avaliação são exibidos somente quando existem na resposta; nenhum dado é fabricado.

## Validação pendente

As alterações foram gravadas nas branches indicadas. Não foi possível executar o build Gradle, testes de dispositivo ou chamadas autenticadas contra o Render nesta sessão. Esses testes precisam ocorrer após o backend ser integrado/implantado e com as variáveis locais de build disponíveis.
