package com.example.ecociente.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ResultadoCadastro {
    private final boolean sucesso;
    private final String uid;
    private final String mensagemErro;

    private ResultadoCadastro(boolean sucesso, @Nullable String uid, @NonNull String mensagemErro) {
        this.sucesso = sucesso;
        this.uid = uid;
        this.mensagemErro = mensagemErro;
    }

    public static ResultadoCadastro sucesso(@NonNull String uid) {
        return new ResultadoCadastro(true, uid, "");
    }

    public static ResultadoCadastro erro(@NonNull String mensagem) {
        return new ResultadoCadastro(false, null, mensagem);
    }

    public boolean isSucesso() {
        return sucesso;
    }

    @Nullable
    public String getUid() {
        return uid;
    }

    @NonNull
    public String getMensagemErro() {
        return mensagemErro;
    }
}
