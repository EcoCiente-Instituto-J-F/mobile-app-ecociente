const { randomInt } = require("crypto");
const { obterPool } = require("../lib/postgres");

module.exports = async (req, res) => {
  if (req.method !== "GET") {
    res.setHeader("Allow", "GET");

    return res.status(405).json({
      erro: "Método não permitido",
    });
  }

  try {
    /*
     * Esta consulta também serve como teste real
     * da conexão API -> PostgreSQL.
     *
     * Se ela falhar, caímos no catch e retornamos 500.
     */
    const resultado = await obterPool().query(
      `
        SELECT *
        FROM tb_notificacoes
        WHERE tipo_notificacao = $1
      `,
      ["motivacional"],
    );

    if (resultado.rows.length === 0) {
      return res.status(404).json({
        erro: "Nenhuma mensagem motivacional encontrada",
      });
    }

    /*
     * ID da mensagem que o aplicativo já está mostrando.
     *
     * Exemplo:
     *
     * ?excluirId=4
     *
     * Se houver mais de uma mensagem no banco,
     * não sorteamos novamente a mesma.
     */
    const excluirId =
      req.query && req.query.excluirId
        ? String(req.query.excluirId).trim()
        : "";

    let mensagensDisponiveis = resultado.rows;

    if (
      excluirId !== "" &&
      resultado.rows.length > 1
    ) {
      mensagensDisponiveis =
        resultado.rows.filter(
          notificacao =>
            String(notificacao.id_notificacao) !==
            excluirId,
        );
    }

    /*
     * Segurança extra.
     */
    if (mensagensDisponiveis.length === 0) {
      mensagensDisponiveis = resultado.rows;
    }

    const indice = randomInt(
      mensagensDisponiveis.length,
    );

    const notificacao =
      mensagensDisponiveis[indice];

    /*
     * Não queremos nenhuma camada de cache
     * devolvendo uma resposta antiga.
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

    const resposta = {
      id: notificacao.id_notificacao,
      titulo: notificacao.titulo_mensagem,
      mensagem: notificacao.corpo_mensagem,
    };

    /*
     * Modo temporário de diagnóstico.
     *
     * Ao acessar:
     *
     * /api/notificacaoMotivacional?debug=1
     *
     * conseguimos confirmar se a consulta ao banco
     * realmente aconteceu e quantas mensagens existem.
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
      `[motivacional] banco OK | total=${resultado.rows.length} | anterior=${excluirId || "nenhum"} | selecionada=${notificacao.id_notificacao}`,
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

      diagnostico: {
        bancoConectado: false,
      },
    });
  }
};