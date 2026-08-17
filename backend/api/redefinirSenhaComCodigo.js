const { admin } = require("../lib/firebaseAdmin");
const { lerCodigoValido } = require("../lib/validarCodigo");

module.exports = async (req, res) => {
  if (req.method !== "POST") {
    return res.status(405).json({ erro: "Método não permitido" });
  }

  const email = (req.body?.email || "").trim().toLowerCase();
  const codigo = (req.body?.codigo || "").trim();
  const novaSenha = req.body?.novaSenha || "";

  if (!email || !codigo || !novaSenha) {
    return res.status(400).json({ erro: "Preencha todos os campos" });
  }

  if (novaSenha.length < 6) {
    return res.status(400).json({ erro: "A senha deve ter no mínimo 6 caracteres" });
  }

  try {
    const { referencia, dados } = await lerCodigoValido(email, codigo);

    await admin.auth().updateUser(dados.uid, { password: novaSenha });
    await referencia.delete();

    return res.status(200).json({ sucesso: true });
  } catch (erro) {
    return res.status(erro.status || 500).json({ erro: erro.message });
  }
};
