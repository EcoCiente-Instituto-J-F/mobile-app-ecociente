package com.example.ecociente.model;

import androidx.annotation.NonNull;

// Resultado de uma tentativa de login: ou deu certo, ou veio com uma mensagem de erro.
public final class ResultadoLogin {
    private final boolean sucesso;
    private final String mensagemErro;

    private ResultadoLogin(boolean sucesso, @NonNull String mensagemErro) {
        this.sucesso = sucesso;
        this.mensagemErro = mensagemErro;
    }

    public static ResultadoLogin sucesso() {
        return new ResultadoLogin(true, "");
    }

    public static ResultadoLogin erro(@NonNull String mensagem) {
        return new ResultadoLogin(false, mensagem);
    }

    public boolean isSucesso() {
        return sucesso;
    }

    @NonNull
    public String getMensagemErro() {
        return mensagemErro;
    }
}
