package com.example.ecociente.model;

import androidx.annotation.NonNull;

public final class ResultadoUploadFoto {
    private final boolean sucesso;
    private final String url;
    private final String mensagemErro;

    private ResultadoUploadFoto(boolean sucesso, @NonNull String url, @NonNull String mensagemErro) {
        this.sucesso = sucesso;
        this.url = url;
        this.mensagemErro = mensagemErro;
    }

    public static ResultadoUploadFoto sucesso(@NonNull String url) {
        return new ResultadoUploadFoto(true, url, "");
    }

    public static ResultadoUploadFoto erro(@NonNull String mensagem) {
        return new ResultadoUploadFoto(false, "", mensagem);
    }

    public boolean isSucesso() {
        return sucesso;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    @NonNull
    public String getMensagemErro() {
        return mensagemErro;
    }
}
