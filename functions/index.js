const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");

admin.initializeApp();

const db = admin.firestore();

// Credenciais do Gmail, configuradas via `firebase functions:secrets:set` (nunca em código).
const GMAIL_USER = defineSecret("GMAIL_USER");
const GMAIL_APP_PASSWORD = defineSecret("GMAIL_APP_PASSWORD");

const VALIDADE_CODIGO_MS = 10 * 60 * 1000; // 10 minutos
const MAXIMO_TENTATIVAS = 5;

function gerarCodigo() {
  return String(Math.floor(1000 + Math.random() * 9000));
}

function criarTransportador() {
  return nodemailer.createTransport({
    service: "gmail",
    auth: {
      user: GMAIL_USER.value(),
      pass: GMAIL_APP_PASSWORD.value(),
    },
  });
}

// Lê o código salvo e valida existência/expiração/tentativas.
// Não altera nada no banco (isso fica a cargo de cada function chamadora).
async function lerCodigoValido(email, codigo) {
  const referencia = db.collection("codigosRecuperacao").doc(email);
  const documento = await referencia.get();

  if (!documento.exists) {
    throw new HttpsError("not-found", "Código inválido ou expirado");
  }

  const dados = documento.data();

  if (dados.tentativas >= MAXIMO_TENTATIVAS) {
    throw new HttpsError("resource-exhausted", "Muitas tentativas. Solicite um novo código");
  }

  const expirado = Date.now() - dados.criadoEm.toMillis() > VALIDADE_CODIGO_MS;

  if (expirado) {
    await referencia.delete();
    throw new HttpsError("deadline-exceeded", "Código expirado. Solicite um novo");
  }

  if (dados.codigo !== codigo) {
    await referencia.update({ tentativas: admin.firestore.FieldValue.increment(1) });
    throw new HttpsError("invalid-argument", "Código incorreto");
  }

  return referencia;
}

// Passo 1: gera o código e envia por email.
exports.enviarCodigoRecuperacao = onCall(
    { secrets: [GMAIL_USER, GMAIL_APP_PASSWORD] },
    async (requisicao) => {
      const email = (requisicao.data && requisicao.data.email || "").trim().toLowerCase();

      if (!email) {
        throw new HttpsError("invalid-argument", "Informe um email");
      }

      // Mensagem de sucesso é sempre a mesma, exista ou não o usuário,
      // pra não permitir descobrir emails cadastrados por tentativa e erro.
      try {
        const usuario = await admin.auth().getUserByEmail(email);

        const codigo = gerarCodigo();

        await db.collection("codigosRecuperacao").doc(email).set({
          codigo,
          criadoEm: admin.firestore.Timestamp.now(),
          tentativas: 0,
          uid: usuario.uid,
        });

        await criarTransportador().sendMail({
          from: `EcoCiente <${GMAIL_USER.value()}>`,
          to: email,
          subject: "Código de recuperação de senha - EcoCiente",
          text: `Seu código de recuperação é: ${codigo}\n\nEle expira em 10 minutos.`,
        });
      } catch (erro) {
        logger.error("Erro ao enviar código de recuperação", erro);
        // Erros de "usuário não existe" (auth/user-not-found) caem aqui e são
        // silenciados de propósito - o retorno pro app é sempre de sucesso.
      }

      return { sucesso: true };
    },
);

// Passo 2: só valida o código, sem alterar nada (feedback pra tela "Verificar").
exports.verificarCodigoRecuperacao = onCall(async (requisicao) => {
  const email = (requisicao.data && requisicao.data.email || "").trim().toLowerCase();
  const codigo = (requisicao.data && requisicao.data.codigo || "").trim();

  if (!email || !codigo) {
    throw new HttpsError("invalid-argument", "Informe email e código");
  }

  await lerCodigoValido(email, codigo);

  return { sucesso: true };
});

// Passo 3: revalida o código (nunca confiar só na checagem do passo 2) e troca a senha.
exports.redefinirSenhaComCodigo = onCall(async (requisicao) => {
  const email = (requisicao.data && requisicao.data.email || "").trim().toLowerCase();
  const codigo = (requisicao.data && requisicao.data.codigo || "").trim();
  const novaSenha = requisicao.data && requisicao.data.novaSenha || "";

  if (!email || !codigo || !novaSenha) {
    throw new HttpsError("invalid-argument", "Preencha todos os campos");
  }

  if (novaSenha.length < 6) {
    throw new HttpsError("invalid-argument", "A senha deve ter no mínimo 6 caracteres");
  }

  const referencia = await lerCodigoValido(email, codigo);
  const dados = (await referencia.get()).data();

  await admin.auth().updateUser(dados.uid, { password: novaSenha });
  await referencia.delete();

  return { sucesso: true };
});
