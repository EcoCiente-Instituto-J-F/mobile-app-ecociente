const { randomInt } = require("crypto");
const { obterPool } = require("../lib/postgres");

module.exports = async (req, res) => {
  /*
   * Este endpoint é somente consulta.
   */
  if (req.method !== "GET") {
    res.setHeader("Allow", "GET");

    return res.status(405).json({
      erro: "Método não permitido",
    });
  }

  /*
   * Não queremos que navegador, Vercel ou proxy
   * devolvam uma frase antiga em cache.
   *
   * Cada atualização da Home deve realmente
   * consultar o endpoint.
   */
  res.setHeader(
    "Cache-Control",
    "no-store, no-cache, must-revalidate, proxy-revalidate",
  );

  res.setHeader(
    "Pragma",
    "no-cache",
  );

  res.setHeader(
    "Expires",
    "0",
  );

  try {
    /*
     * Busca somente as notificações motivacionais
     * que possuem uma mensagem válida.
     */
    const resultado = await obterPool().query(
      `
        SELECT
          id_notificacao,
          corpo_mensagem
        FROM tb_notificacoes
        WHERE LOWER(TRIM(tipo_notificacao)) = LOWER($1)
          AND corpo_mensagem IS NOT NULL
          AND BTRIM(corpo_mensagem) <> ''
      `,
      [
        "motivacional",
      ],
    );

    /*
     * Nenhuma mensagem cadastrada.
     */
    if (resultado.rows.length === 0) {
      return res.status(404).json({
        erro:
          "Nenhuma mensagem motivacional encontrada",
      });
    }

    /*
     * O Android envia o ID da frase que está
     * aparecendo atualmente.
     *
     * Exemplo:
     *
     * ?excluirId=5
     *
     * Assim evitamos sortear a mesma mensagem
     * duas vezes seguidas.
     */
    const excluirId =
      req.query &&
      req.query.excluirId
        ? String(
            req.query.excluirId,
          ).trim()
        : "";

    let mensagensDisponiveis =
      resultado.rows;

    /*
     * Se houver mais de uma frase,
     * removemos temporariamente a atual
     * do sorteio.
     */
    if (
      excluirId !== "" &&
      resultado.rows.length > 1
    ) {
      mensagensDisponiveis =
        resultado.rows.filter(
          notificacao =>
            String(
              notificacao.id_notificacao,
            ) !== excluirId,
        );
    }

    /*
     * Segurança adicional.
     *
     * Caso algo inesperado faça o filtro
     * resultar em uma lista vazia,
     * voltamos a utilizar todas as mensagens.
     */
    if (
      mensagensDisponiveis.length === 0
    ) {
      mensagensDisponiveis =
        resultado.rows;
    }

    /*
     * Escolhe aleatoriamente uma frase.
     */
    const indice =
      randomInt(
        mensagensDisponiveis.length,
      );

    const notificacao =
      mensagensDisponiveis[indice];

    /*
     * Retornamos somente aquilo que o Mobile
     * realmente precisa:
     *
     * - ID para impedir repetição imediata
     * - corpo da mensagem para mostrar na Home
     */
    const resposta = {
      id:
        notificacao.id_notificacao,

      mensagem:
        String(
          notificacao.corpo_mensagem,
        ).trim(),
    };

    /*
     * Debug temporário.
     *
     * Você pode abrir:
     *
     * /api/notificacaoMotivacional?debug=1
     *
     * para confirmar se o banco está realmente
     * sendo consultado.
     */
    if (
      req.query &&
      req.query.debug === "1"
    ) {
      resposta.diagnostico = {
        bancoConectado: true,

        quantidadeMotivacionais:
          resultado.rows.length,

        quantidadeDisponivelParaSorteio:
          mensagensDisponiveis.length,

        idAnteriorRecebido:
          excluirId || null,

        idSelecionado:
          notificacao.id_notificacao,

        horarioServidor:
          new Date().toISOString(),
      };
    }

    console.log(
      "[motivacional]",
      "banco OK",
      `total=${resultado.rows.length}`,
      `anterior=${excluirId || "nenhum"}`,
      `selecionada=${notificacao.id_notificacao}`,
    );

    return res
      .status(200)
      .json(resposta);

  } catch (erro) {
    console.error(
      "Erro ao buscar notificação motivacional:",
      erro,
    );

    return res.status(500).json({
      erro:
        "Não foi possível buscar a mensagem motivacional",
    });
  }
};