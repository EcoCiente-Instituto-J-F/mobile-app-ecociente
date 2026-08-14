package com.example.ecociente.esquecisenha;

import androidx.annotation.NonNull;

// Resultado de uma chamada ao backend: ou deu certo, ou veio com uma mensagem de erro.
public final class ResultadoApi {
    private final boolean sucesso;
    private final String mensagemErro;

    private ResultadoApi(boolean sucesso, @NonNull String mensagemErro) {
        this.sucesso = sucesso;
        this.mensagemErro = mensagemErro;
    }

    static ResultadoApi sucesso() {
        return new ResultadoApi(true, "");
    }

    static ResultadoApi erro(@NonNull String mensagem) {
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
