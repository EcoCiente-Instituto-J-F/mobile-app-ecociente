const { lerCodigoValido } = require("../lib/validarCodigo");

module.exports = async (req, res) => {
  if (req.method !== "POST") {
    return res.status(405).json({ erro: "Método não permitido" });
  }

  const email = (req.body?.email || "").trim().toLowerCase();
  const codigo = (req.body?.codigo || "").trim();

  if (!email || !codigo) {
    return res.status(400).json({ erro: "Informe email e código" });
  }

  try {
    await lerCodigoValido(email, codigo);
    return res.status(200).json({ sucesso: true });
  } catch (erro) {
    return res.status(erro.status || 500).json({ erro: erro.message });
  }
};
