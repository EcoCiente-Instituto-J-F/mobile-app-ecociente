package com.example.ecociente.ui;

import androidx.annotation.NonNull;
import java.util.Locale;

public final class Iniciais {

    private Iniciais() {}

    @NonNull
    public static String de(@NonNull String nome) {

        String[] palavras = nome.trim().split("\\s+");

        if (palavras[0].isEmpty()) {
            return "";
        }

        String primeira = palavras[0].substring(0, 1);

        String ultima = palavras.length > 1 ? palavras[palavras.length - 1].substring(0, 1) : "";

        return (primeira + ultima).toUpperCase(Locale.ROOT);
    }
}
