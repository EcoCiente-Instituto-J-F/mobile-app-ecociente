package com.example.ecociente.model;

import androidx.annotation.NonNull;

// Conversa entre um condomínio e uma cooperativa (ds-mobile-api, /api/conversas). `nome` é o do
// outro lado; a API não devolve o texto da última mensagem, só quando ela foi enviada.
public final class Conversa {

    public final int id;
    public final int condominioId;
    public final int cooperativaId;
    public final String nome;
    public final String ultimaMensagemEm;
    public final int naoLidas;
    public final boolean arquivada;

    public Conversa(
            int id,
            int condominioId,
            int cooperativaId,
            @NonNull String nome,
            @NonNull String ultimaMensagemEm,
            int naoLidas,
            boolean arquivada) {
        this.id = id;
        this.condominioId = condominioId;
        this.cooperativaId = cooperativaId;
        this.nome = nome;
        this.ultimaMensagemEm = ultimaMensagemEm;
        this.naoLidas = naoLidas;
        this.arquivada = arquivada;
    }

    public boolean temNaoLidas() {
        return naoLidas > 0;
    }
}
