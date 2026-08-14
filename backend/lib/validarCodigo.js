const admin = require("./firebaseAdmin");

const VALIDADE_CODIGO_MS = 10 * 60 * 1000; // 10 minutos
const MAXIMO_TENTATIVAS = 5;

function erroHttp(status, mensagem) {
  const erro = new Error(mensagem);
  erro.status = status;
  return erro;
}

// Lê o código salvo e valida existência/expiração/tentativas.
// Não altera nada além do contador de tentativas em caso de erro
// (a escrita/exclusão do código em si fica a cargo de quem chama).
async function lerCodigoValido(email, codigo) {
  const referencia = admin.firestore().collection("codigosRecuperacao").doc(email);
  const documento = await referencia.get();

  if (!documento.exists) {
    throw erroHttp(404, "Código inválido ou expirado");
  }

  const dados = documento.data();

  if (dados.tentativas >= MAXIMO_TENTATIVAS) {
    throw erroHttp(429, "Muitas tentativas. Solicite um novo código");
  }

  const expirado = Date.now() - dados.criadoEm.toMillis() > VALIDADE_CODIGO_MS;

  if (expirado) {
    await referencia.delete();
    throw erroHttp(410, "Código expirado. Solicite um novo");
  }

  if (dados.codigo !== codigo) {
    await referencia.update({ tentativas: admin.firestore.FieldValue.increment(1) });
    throw erroHttp(400, "Código incorreto");
  }

  return { referencia, dados };
}

module.exports = { lerCodigoValido, erroHttp, VALIDADE_CODIGO_MS, MAXIMO_TENTATIVAS };
