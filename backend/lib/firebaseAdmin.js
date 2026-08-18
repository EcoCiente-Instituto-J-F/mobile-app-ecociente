const admin = require("firebase-admin");
const { getFirestore } = require("firebase-admin/firestore");

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

const app = admin.apps.length
  ? admin.app()
  : admin.initializeApp({ credential: admin.credential.cert(lerCredencial()) });

// O banco desse projeto não é o "(default)" especial, é um banco nomeado
// "default" - por isso precisa apontar pro ID explicitamente.
const db = getFirestore(app, "default");
db.settings({ preferRest: true });

module.exports = { admin, db };
