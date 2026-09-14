package com.example.ecociente.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ResultadoCadastro {
    private final boolean sucesso;
    private final String uid;
    private final String mensagemErro;
    private final String aviso;

    private ResultadoCadastro(
            boolean sucesso, @Nullable String uid, @NonNull String mensagemErro, @Nullable String aviso) {
        this.sucesso = sucesso;
        this.uid = uid;
        this.mensagemErro = mensagemErro;
        this.aviso = aviso;
    }

    public static ResultadoCadastro sucesso(@NonNull String uid) {
        return new ResultadoCadastro(true, uid, "", null);
    }

    public static ResultadoCadastro sucessoComAviso(@NonNull String uid, @NonNull String aviso) {
        return new ResultadoCadastro(true, uid, "", aviso);
    }

    public static ResultadoCadastro erro(@NonNull String mensagem) {
        return new ResultadoCadastro(false, null, mensagem, null);
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

    @Nullable
    public String getAviso() {
        return aviso;
    }
}
