package com.example.ecociente.repository;

/**
 * Enderecos dos servicos HTTP consumidos pelo app. Mudou um servidor? Mude so aqui.
 *
 * A ds-mobile-api e o servico unificado: login, cadastro, senha, perfil, calendario, feed, quiz,
 * chat de coleta, avaliacoes e dashboard. Pontuacao/ranking e o chatbot ainda sao servicos a parte.
 */
public final class ConfiguracaoServidores {

    public static final String MOBILE_API = "https://ds-mobile-api.onrender.com";
    public static final String PONTUACAO_API = "https://ds-pontuacao-api.onrender.com";
    public static final String CHAT_IA = "https://ia-eko-1.onrender.com/api/v1/chat";

    public static final String URL_LOGIN = MOBILE_API + "/auth/login";
    // Servico de login antigo: so responde se o unificado estiver fora do ar.
    public static final String URL_LOGIN_RENDER = "https://ds-autenticacao-api-1.onrender.com/auth/login";
    public static final String URL_CADASTRO = MOBILE_API + "/api/cadastro";
    public static final String URL_SENHAS = MOBILE_API + "/senhas/";
    public static final String URL_USUARIO = MOBILE_API + "/api/usuarios/me";
    public static final String URL_CONDOMINIOS = MOBILE_API + "/api/condominios/";
    public static final String URL_AGENDAMENTOS = MOBILE_API + "/api/v1/agendamentos";

    public static final int TIMEOUT_CONEXAO_MS = 10_000;
    public static final int TIMEOUT_RESPOSTA_LOGIN_MS = 25_000;
    public static final int TIMEOUT_RESPOSTA_CADASTRO_MS = 40_000;
    public static final int TIMEOUT_RESPOSTA_RENDER_MS = 20_000;

    private ConfiguracaoServidores() {}
}
