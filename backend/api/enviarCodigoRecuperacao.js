const admin = require("../lib/firebaseAdmin");
const nodemailer = require("nodemailer");

function gerarCodigo() {
  return String(Math.floor(1000 + Math.random() * 9000));
}

async function enviarEmail(email, codigo) {
  const transportador = nodemailer.createTransport({
    service: "gmail",
    auth: {
      user: process.env.GMAIL_USER,
      pass: process.env.GMAIL_APP_PASSWORD,
    },
  });

  await transportador.sendMail({
    from: `EcoCiente <${process.env.GMAIL_USER}>`,
    to: email,
    subject: "Código de recuperação de senha - EcoCiente",
    text: `Seu código de recuperação é: ${codigo}\n\nEle expira em 10 minutos.`,
  });
}

// Passo 1: gera o código de 4 dígitos e envia por email.
module.exports = async (req, res) => {
  if (req.method !== "POST") {
    return res.status(405).json({ erro: "Método não permitido" });
  }

  const email = (req.body?.email || "").trim().toLowerCase();

  if (!email) {
    return res.status(400).json({ erro: "Informe um email" });
  }

  // A resposta é sempre de sucesso, exista ou não o usuário, pra não
  // permitir descobrir emails cadastrados por tentativa e erro.
  try {
    const usuario = await admin.auth().getUserByEmail(email);
    const codigo = gerarCodigo();

    await admin.firestore().collection("codigosRecuperacao").doc(email).set({
      codigo,
      criadoEm: admin.firestore.Timestamp.now(),
      tentativas: 0,
      uid: usuario.uid,
    });

    await enviarEmail(email, codigo);
  } catch (erro) {
    console.error("Erro ao enviar código de recuperação", erro);
  }

  return res.status(200).json({ sucesso: true });
};
