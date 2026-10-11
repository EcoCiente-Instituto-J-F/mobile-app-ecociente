package com.example.ecociente.model;

import androidx.annotation.NonNull;
import java.util.Arrays;
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

    @NonNull
    public static List<Notificacao> exemplos() {
        return Arrays.asList(
                new Notificacao("Hoje", "Aviso de coleta", "Amanhã às 12h, a cooperativa EcoCiente passará.", "8:30"),
                new Notificacao("Hoje", "Ranking", "Você subiu no ranking! Saiu de 3 para 2 posição.", "8:30"),
                new Notificacao("Hoje", "Aviso de coleta", "Amanhã às 12h, a cooperativa EcoCiente passará.", "7:45"),
                new Notificacao("Ontem", "Ranking", "Você subiu no ranking! Saiu de 4 para 3 posição.", "18:10"),
                new Notificacao("Ontem", "Aviso de coleta", "Amanhã às 12h, a cooperativa EcoCiente passará.", "9:00"));
    }
}
