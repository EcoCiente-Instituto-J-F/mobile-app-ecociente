package com.example.ecociente.repository;

/**
 * Enderecos dos servicos HTTP consumidos pelo app.
 *
 * Durante a fase de integracao, login e cadastro utilizam a AWS.
 * Os demais endpoints ainda em Render continuam em seus repositories.
 * A AWS Academy depende de uma sessao ativa do Learner Lab.
 */
public final class ConfiguracaoServidores {
    private static final String BASE_AWS = "https://api.52.71.117.143.sslip.io";

    public static final String URL_LOGIN = BASE_AWS + "/auth/login";
    public static final String URL_LOGIN_RENDER = "https://ds-autenticacao-api-1.onrender.com/auth/login";
    public static final String URL_CADASTRO = BASE_AWS + "/api/cadastro";

    public static final int TIMEOUT_CONEXAO_MS = 10_000;
    public static final int TIMEOUT_RESPOSTA_LOGIN_MS = 25_000;
    public static final int TIMEOUT_RESPOSTA_CADASTRO_MS = 40_000;
    public static final int TIMEOUT_RESPOSTA_RENDER_MS = 20_000;

    private ConfiguracaoServidores() {}
}
