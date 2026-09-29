const { Pool } = require("pg");

function criarConfiguracao() {
  const urlInformada = (process.env.DB_URL || "").replace(/^jdbc:/, "");

  if (!urlInformada || !process.env.DB_USER || !process.env.DB_PASSWORD) {
    throw new Error("DB_URL, DB_USER e DB_PASSWORD precisam estar configuradas");
  }

  const url = new URL(urlInformada);

  return {
    host: url.hostname,
    port: Number(url.port || 5432),
    database: url.pathname.replace(/^\//, ""),
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    ssl: { rejectUnauthorized: false },
    max: 3,
    idleTimeoutMillis: 10_000,
    connectionTimeoutMillis: 10_000,
  };
}

const chavePool = Symbol.for("ecociente.postgres.pool");
const escopoGlobal = globalThis;

function obterPool() {
  if (!escopoGlobal[chavePool]) {
    escopoGlobal[chavePool] = new Pool(criarConfiguracao());
  }

  return escopoGlobal[chavePool];
}

module.exports = { obterPool };
