const admin = require("firebase-admin");

// Chave em base64 pra não quebrar com as quebras de linha da private_key
// ao colar direto num campo de env var.
function lerCredencial() {
  const valorBase64 = process.env.FIREBASE_SERVICE_ACCOUNT_KEY_BASE64;

  if (valorBase64) {
    const json = Buffer.from(valorBase64, "base64").toString("utf8");
    return JSON.parse(json);
  }

  return JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT_KEY);
}

if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert(lerCredencial()),
  });

  // Evita erro intermitente "5 NOT_FOUND" causado pela conexão gRPC
  // congelando/descongelando entre execuções serverless.
  admin.firestore().settings({ preferRest: true });
}

module.exports = admin;
