const admin = require("firebase-admin");

// O Vercel não faz parte do projeto Firebase, então precisa de uma chave de
// conta de serviço explícita (variável de ambiente FIREBASE_SERVICE_ACCOUNT_KEY)
// pra poder usar o Admin SDK (Auth + Firestore).
if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert(
        JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT_KEY),
    ),
  });
}

module.exports = admin;
