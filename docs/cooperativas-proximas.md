# Tela de cooperativas próximas — diagnóstico e limites da integração

## Implementado nesta branch

- Tela Android `CooperativasProximasActivity`, acessível pela Home do condomínio.
- Consulta autenticada ao endpoint real `GET https://ds-mobile-api.onrender.com/api/cooperativas?page=0&size=50`.
- Parsing dos campos retornados pela API: `id`, `nome`, `enderecoCompleto`, `cidade`, `estado`, `mediaAvaliacoes` e `totalAvaliacoes`.
- Busca local por nome, endereço, cidade e estado.
- Estados de carregamento, erro, lista vazia e busca sem resultados.
- Cartões com avaliação real quando disponível.
- Distância explicitamente marcada como indisponível quando não há coordenadas.
- Nenhum dado de cooperativa ou avaliação é mockado.

## Contratos verificados no backend ds-mobile-api

### Listagem

`GET /api/cooperativas?page=0&size=50`

A resposta é `CooperativasResponseDto`, com:
- `total`
- `pagina`
- `tamanho`
- `totalPaginas`
- `cooperativas[]`

Cada cooperativa contém:
- `id`
- `nome`
- `email`
- `telefone`
- `cep`
- `logradouro`
- `numero`
- `complemento`
- `cidade`
- `estado`
- `enderecoCompleto`
- `mediaAvaliacoes`
- `totalAvaliacoes`

O endpoint exige autenticação e o backend restringe a consulta a síndico com condomínio ativo.

### Criação de coleta

O backend possui `POST /api/v1/agendamentos`, com contrato `CriarColetaRequestDto`:
- `condominioId` (obrigatório)
- `cooperativaId` (obrigatório)
- `dataInicio` (obrigatório, OffsetDateTime futuro)
- `dataFim` (opcional, OffsetDateTime não anterior ao início)

A tela ainda não envia solicitações porque, no fluxo atual do aplicativo inspecionado, não foi identificada uma fonte confiável para obter o `condominioId` associado ao usuário autenticado. Enviar um ID inferido ou fixo poderia criar coleta para o condomínio errado. Por segurança, o CTA aparece desabilitado com explicação; nenhuma solicitação é enviada.

## Funcionalidades bloqueadas por falta de dados/integração

- **Distância real:** a resposta de listagem não contém latitude/longitude. A tela não exibe valores inventados e mostra “Distância indisponível”.
- **Mapa:** não há coordenadas para posicionar pins com precisão. A tela não abre mapa vazio nem cria coordenadas artificiais.
- **Bairro separado:** o DTO não fornece campo `bairro`; a busca procura bairro somente se o texto estiver incluído em `enderecoCompleto`.

## Próximos passos para concluir o fluxo

1. Disponibilizar no app, por endpoint ou perfil autenticado, o identificador real do condomínio ativo do síndico.
2. Integrar formulário de data/horário e enviar o contrato real de `POST /api/v1/agendamentos`.
3. Confirmar no backend o endpoint/consulta para acompanhar o status por cooperativa e condomínio.
4. Se a distância e o mapa forem requisitos, fornecer coordenadas oficiais da cooperativa ou aprovar formalmente o uso de geocodificação de endereços, sempre identificando coordenadas estimadas.
5. Testar em dispositivo/emulador e validar o build Gradle.

## Observação de validação

As alterações foram gravadas na branch `feat/correcoes-sindico-cooperativa`. O build Android e os testes instrumentados ainda precisam ser executados em ambiente Gradle com as variáveis e arquivos locais exigidos pelo projeto.
