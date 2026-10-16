package com.example.ecociente.model;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

public final class MensagemConversa {

    public final String texto;
    public final String hora;
    public final boolean enviada;

    public MensagemConversa(@NonNull String texto, @NonNull String hora, boolean enviada) {
        this.texto = texto;
        this.hora = hora;
        this.enviada = enviada;
    }

    @NonNull
    public static List<MensagemConversa> exemplos(@NonNull String ultimaMensagem, @NonNull String hora) {

        List<MensagemConversa> mensagens = new ArrayList<>();

        mensagens.add(new MensagemConversa("Bom dia! Podemos agendar a coleta desta semana?", "08:02", true));
        mensagens.add(new MensagemConversa("Bom dia! Claro, qual dia fica melhor para vocês?", "08:10", false));
        mensagens.add(new MensagemConversa("Quinta-feira pela manhã, se possível.", "08:12", true));
        mensagens.add(new MensagemConversa("Combinado. Nossa equipe passa por volta das 9h.", "08:15", true));
        mensagens.add(new MensagemConversa(ultimaMensagem, hora, false));

        return mensagens;
    }
}
