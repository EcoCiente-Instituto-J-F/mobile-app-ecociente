const {
  initializeApp,
  cert,
  getApps,
  getApp
} = require("firebase-admin/app");

const {
  getMessaging
} = require("firebase-admin/messaging");

function lerCredencial() {
  const valorBase64 =
    process.env.FIREBASE_SERVICE_ACCOUNT_KEY_BASE64;

  if (!valorBase64) {
    throw new Error(
      "A variável FIREBASE_SERVICE_ACCOUNT_KEY_BASE64 não foi definida."
    );
  }

  const json = Buffer
    .from(valorBase64, "base64")
    .toString("utf8");

  return JSON.parse(json);
}

const app = getApps().length
  ? getApp()
  : initializeApp({
      credential: cert(lerCredencial())
    });

const token = process.argv[2];

if (!token) {
  console.error(
    "Uso: node backend/scripts/enviarPushTeste.js <TOKEN_FCM>"
  );

  process.exit(1);
}

async function enviar() {
  const mensagem = {
    token,

    data: {
      titulo: "Bora de desafio? 👀",
      mensagem:
        "Tem um quiz rapidinho esperando por você!",
      destino: "quiz",
      acao: "Fazer quiz"
    },

    android: {
      priority: "high",
      ttl: 60 * 60 * 1000
    }
  };

  const resposta =
    await getMessaging(app).send(mensagem);

  console.log(
    "Notificação enviada com sucesso:"
  );

  console.log(resposta);
}

enviar().catch((erro) => {
  console.error(
    "Erro ao enviar notificação:"
  );

  console.error(erro);

  process.exit(1);
});