package com.example.ecociente.model;

import androidx.annotation.NonNull;

public final class ResultadoApi {
    private final boolean sucesso;
    private final String mensagemErro;

    private ResultadoApi(boolean sucesso, @NonNull String mensagemErro) {
        this.sucesso = sucesso;
        this.mensagemErro = mensagemErro;
    }

    public static ResultadoApi sucesso() {
        return new ResultadoApi(true, "");
    }

    public static ResultadoApi erro(@NonNull String mensagem) {
        return new ResultadoApi(false, mensagem);
    }

    public boolean isSucesso() {
        return sucesso;
    }

    @NonNull
    public String getMensagemErro() {
        return mensagemErro;
    }
}
