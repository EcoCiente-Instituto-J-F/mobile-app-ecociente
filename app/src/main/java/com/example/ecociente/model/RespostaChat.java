package com.example.ecociente.model;

public class RespostaChat {
    private final String resposta;
    private final String sessionId;

    public RespostaChat(String resposta, String sessionId) {
        this.resposta = resposta;
        this.sessionId = sessionId;
    }

    public String getResposta() {
        return resposta;
    }

    public String getSessionId() {
        return sessionId;
    }
}
