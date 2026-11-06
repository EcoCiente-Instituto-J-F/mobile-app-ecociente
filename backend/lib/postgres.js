const { Pool } = require("pg");

function obterUrlBanco() {
  const urlInformada =
    process.env.DATABASE_URL ||
    process.env.DB_URL ||
    "";

  const urlNormalizada = urlInformada
    .trim()
    .replace(/^jdbc:/, "");

  if (!urlNormalizada) {
    throw new Error(
      "Configure DATABASE_URL ou DB_URL com a conexão do PostgreSQL",
    );
  }

  return new URL(urlNormalizada);
}

function criarConfiguracao() {
  const url = obterUrlBanco();

  const usuario =
    decodeURIComponent(url.username || "") ||
    process.env.DB_USER ||
    "";

  const senha =
    decodeURIComponent(url.password || "") ||
    process.env.DB_PASSWORD ||
    "";

  if (!usuario || !senha) {
    throw new Error(
      "Usuário e senha do PostgreSQL não foram configurados",
    );
  }

  const hostLocal =
    url.hostname === "localhost" ||
    url.hostname === "127.0.0.1";

  return {
    host: url.hostname,
    port: Number(url.port || 5432),
    database: url.pathname.replace(/^\//, ""),
    user: usuario,
    password: senha,

    ssl: hostLocal
      ? false
      : {
          rejectUnauthorized: false,
        },

    /*
     * Mantemos um pool pequeno porque o endpoint
     * roda como função serverless no Vercel.
     */
    max: 3,

    idleTimeoutMillis: 10_000,

    connectionTimeoutMillis: 10_000,
  };
}

/*
 * Reutiliza o mesmo pool enquanto a instância
 * serverless estiver ativa.
 */
const chavePool = Symbol.for(
  "ecociente.postgres.pool",
);

const escopoGlobal = globalThis;

function obterPool() {
  if (!escopoGlobal[chavePool]) {
    escopoGlobal[chavePool] = new Pool(
      criarConfiguracao(),
    );
  }

  return escopoGlobal[chavePool];
}

module.exports = {
  obterPool,
};