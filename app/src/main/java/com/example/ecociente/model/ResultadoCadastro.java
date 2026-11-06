package com.example.ecociente.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ResultadoCadastro {
    private final boolean sucesso;
    private final String uid;
    private final String mensagemErro;
    private final boolean contaJaExistente;

    private ResultadoCadastro(
            boolean sucesso,
            @Nullable String uid,
            @NonNull String mensagemErro,
            boolean contaJaExistente) {
        this.sucesso = sucesso;
        this.uid = uid;
        this.mensagemErro = mensagemErro;
        this.contaJaExistente = contaJaExistente;
    }

    public static ResultadoCadastro sucesso(@NonNull String uid) {
        return new ResultadoCadastro(true, uid, "", false);
    }

    public static ResultadoCadastro erro(@NonNull String mensagem) {
        return new ResultadoCadastro(false, null, mensagem, false);
    }

    public static ResultadoCadastro contaJaExistente(@NonNull String mensagem) {
        return new ResultadoCadastro(false, null, mensagem, true);
    }

    public boolean isContaJaExistente() {
        return contaJaExistente;
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
