const admin = require("firebase-admin");

// O Vercel não faz parte do projeto Firebase, então precisa de uma chave de
// conta de serviço explícita pra poder usar o Admin SDK (Auth + Firestore).
//
// A chave vem em base64 (FIREBASE_SERVICE_ACCOUNT_KEY_BASE64) em vez de JSON
// cru: colar o JSON direto no campo de variável de ambiente da Vercel quebra
// por causa das quebras de linha dentro da private_key. Base64 evita isso.
function lerCredencial() {
  const valorBase64 = process.env.FIREBASE_SERVICE_ACCOUNT_KEY_BASE64;

  if (valorBase64) {
    const json = Buffer.from(valorBase64, "base64").toString("utf8");
    return JSON.parse(json);
  }

  // Mantido por compatibilidade, caso alguém prefira colar o JSON direto.
  return JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT_KEY);
}

if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert(lerCredencial()),
  });

  // Em serverless (Vercel/Lambda), a função "congela" entre execuções e a
  // conexão gRPC do Firestore pode ficar quebrada ao "descongelar", causando
  // erros intermitentes tipo "5 NOT_FOUND". Forçar REST evita esse problema
  // (recomendação oficial do Google pra ambientes serverless).
  admin.firestore().settings({ preferRest: true });
}

module.exports = admin;
