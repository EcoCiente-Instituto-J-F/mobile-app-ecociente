package com.example.ecociente.midias;

/** Nao coloque API key, API secret, token de servico ou credenciais nesta classe. */
public final class ConfiguracaoMidias {
    private ConfiguracaoMidias() { }
    // Nome publico, nao e segredo. Pode ficar vazio: cada item do JSON pode fornecer cloud_name.
    public static final String CLOUD_NAME = "";
    // Troque ao usar outro banco/ambiente com os mesmos IDs de cursos e quizzes.
    public static final String AMBIENTE = "ecociente_atual";
    public static final double CHANCE_ICONE_TITULO = 0.45;
    public static final double CHANCE_ICONE_TEXTO = 0.30;
    // Recursos existentes no ZIP. O GIF de comemoracao NAO e usado durante perguntas.
    public static final String EKO_PADRAO = "mascote_eko";
    public static final String DECORACAO_QUIZ_PADRAO = "quiz_planeta_pergunta";
}
