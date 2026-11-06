package com.example.ecociente.model;

import androidx.annotation.NonNull;

public final class MensagemConversa {

    public final String texto;
    public final String hora;
    public final boolean enviada;

    public MensagemConversa(@NonNull String texto, @NonNull String hora, boolean enviada) {
        this.texto = texto;
        this.hora = hora;
        this.enviada = enviada;
    }
}
