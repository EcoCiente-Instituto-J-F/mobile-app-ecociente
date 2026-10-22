package com.example.ecociente.model;

import androidx.annotation.NonNull;
import java.util.Collections;
import java.util.List;

public final class Notificacao {

    public final String grupo;
    public final String titulo;
    public final String mensagem;
    public final String hora;

    private Notificacao(String grupo, String titulo, String mensagem, String hora) {
        this.grupo = grupo;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.hora = hora;
    }

    // Ainda não há API de notificações: a lista é vazia.
    @NonNull
    public static List<Notificacao> lista() {
        return Collections.emptyList();
    }
}
