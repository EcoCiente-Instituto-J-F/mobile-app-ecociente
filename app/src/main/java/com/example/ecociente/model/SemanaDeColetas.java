package com.example.ecociente.model;

import androidx.annotation.NonNull;
import java.util.Calendar;

// A semana de coletas vai de segunda a sexta. Datas calculadas a partir de um dia qualquer.
public final class SemanaDeColetas {

    public static final int DIAS_UTEIS = 5;

    private SemanaDeColetas() {}

    @NonNull
    public static Calendar segunda() {
        return segundaDe(Calendar.getInstance());
    }

    @NonNull
    public static Calendar fim() {
        return fimDe(Calendar.getInstance());
    }

    // Segunda-feira, 00:00, da semana de `dia`. Sábado e domingo contam para a semana que termina.
    @NonNull
    public static Calendar segundaDe(@NonNull Calendar dia) {

        Calendar segunda = (Calendar) dia.clone();

        segunda.set(Calendar.HOUR_OF_DAY, 0);
        segunda.set(Calendar.MINUTE, 0);
        segunda.set(Calendar.SECOND, 0);
        segunda.set(Calendar.MILLISECOND, 0);

        int desvio = (segunda.get(Calendar.DAY_OF_WEEK) + 5) % 7;

        segunda.add(Calendar.DAY_OF_MONTH, -desvio);

        return segunda;
    }

    // Sexta-feira, 23:59:59, da semana de `dia`.
    @NonNull
    public static Calendar fimDe(@NonNull Calendar dia) {

        Calendar fim = segundaDe(dia);

        fim.add(Calendar.DAY_OF_MONTH, DIAS_UTEIS);
        fim.add(Calendar.SECOND, -1);

        return fim;
    }
}
