package com.example.ecociente.model;

import androidx.annotation.NonNull;
import java.util.Collections;
import java.util.List;

public final class Conversa {

    public final String nome;
    public final String ultimaMensagem;
    public final String hora;
    public final int naoLidas;
    public final boolean arquivada;

    private Conversa(String nome, String ultimaMensagem, String hora, int naoLidas, boolean arquivada) {
        this.nome = nome;
        this.ultimaMensagem = ultimaMensagem;
        this.hora = hora;
        this.naoLidas = naoLidas;
        this.arquivada = arquivada;
    }

    public boolean temNaoLidas() {

        return naoLidas > 0;
    }

    @NonNull
    public static List<Conversa> listaDoSindico() {
        return Collections.emptyList();
    }

    @NonNull
    public static List<Conversa> lista() {
        return Collections.emptyList();
    }
}
