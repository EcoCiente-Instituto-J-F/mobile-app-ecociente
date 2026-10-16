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

function obterApp() {
  return getApps().length
    ? getApp()
    : initializeApp({
        credential: cert(lerCredencial())
      });
}

const tipos = {
  dica: {},
  retorno: {},
  quiz: {},
  revisao: {},
  motivacional: {},
  conteudos: {},
  aulas: {},
  coleta_amanha: {},
  coleta_hoje: {},
  coleta_alterada: {},
  ecoregistro_aprovado: {},
  ranking_subiu: {},
  aviso_condominio: {},
  mensagem_nova: {},
  solicitacao_nova: {},
  solicitacao_aceita: {},
  solicitacao_recusada: {},
  calendario: {},
  sazonal: {}
};

function exibirUso() {
  console.error(
    "Uso: node backend/scripts/enviarPushTeste.js <TOKEN_FCM> <TIPO> [--titulo TEXTO] [--mensagem TEXTO] [--destino DESTINO] [--acao TEXTO]"
  );
  console.error("Tipos aceitos:");
  console.error(Object.keys(tipos).join("\n"));
}

if (process.argv[2] === "--listar") {
  console.log(Object.keys(tipos).join("\n"));
  process.exit(0);
}

const token = process.argv[2];
const tipo = process.argv[3];

if (!token || !tipo) {
  exibirUso();
  process.exit(1);
}

if (!Object.prototype.hasOwnProperty.call(tipos, tipo)) {
  console.error(`Tipo inválido: ${tipo}`);
  exibirUso();
  process.exit(1);
}

function lerSobrescritas(argumentos) {
  const chaves = {
    "--titulo": "titulo",
    "--mensagem": "mensagem",
    "--destino": "destino",
    "--acao": "acao"
  };
  const sobrescritas = {};

  for (let indice = 4; indice < argumentos.length; indice += 2) {
    const chave = chaves[argumentos[indice]];
    const valor = argumentos[indice + 1];

    if (!chave || !valor) {
      throw new Error("Sobrescrita inválida. Use pares como --titulo \"Novo título\".");
    }

    sobrescritas[chave] = valor;
  }

  return sobrescritas;
}

let sobrescritas;

try {
  sobrescritas = lerSobrescritas(process.argv);
} catch (erro) {
  console.error(erro.message);
  exibirUso();
  process.exit(1);
}

async function enviar() {
  const mensagem = {
    token,

    data: {
      tipo,
      ...sobrescritas
    },

    android: {
      priority: "high",
      ttl: 60 * 60 * 1000
    }
  };

  const resposta =
    await getMessaging(obterApp()).send(mensagem);

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
