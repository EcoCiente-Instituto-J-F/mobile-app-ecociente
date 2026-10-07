package com.example.ecociente.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class FormatoDataApi {

    private static final int TAMANHO_ISO_SEM_FRACAO = 19;
    private static final Locale PORTUGUES = new Locale("pt", "BR");

    private FormatoDataApi() {}

    @NonNull
    public static String data(@Nullable String iso) {
        return formatar(iso, "dd/MM/yyyy");
    }

    @NonNull
    public static String hora(@Nullable String iso) {
        return formatar(iso, "HH:mm");
    }

    @NonNull
    public static String dataExtensa(@Nullable String iso) {

        String texto = formatar(iso, "EEE, d 'de' MMM 'de' yyyy");

        return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    @NonNull
    public static String horaCurta(@Nullable String iso) {

        Calendar calendario = calendario(iso);

        if (calendario == null) {
            return "";
        }

        int minutos = calendario.get(Calendar.MINUTE);

        return minutos == 0
                ? calendario.get(Calendar.HOUR_OF_DAY) + "h"
                : String.format(
                        PORTUGUES, "%dh%02d", calendario.get(Calendar.HOUR_OF_DAY), minutos);
    }

    @Nullable
    public static Calendar calendario(@Nullable String iso) {

        if (iso == null || iso.length() < TAMANHO_ISO_SEM_FRACAO) {
            return null;
        }

        SimpleDateFormat entrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

        try {
            Date valor = entrada.parse(iso.substring(0, TAMANHO_ISO_SEM_FRACAO));

            if (valor == null) {
                return null;
            }

            Calendar calendario = Calendar.getInstance();

            calendario.setTime(valor);

            return calendario;

        } catch (ParseException erro) {
            return null;
        }
    }

    @NonNull
    private static String formatar(@Nullable String iso, @NonNull String padraoSaida) {

        Calendar calendario = calendario(iso);

        return calendario == null
                ? ""
                : new SimpleDateFormat(padraoSaida, PORTUGUES).format(calendario.getTime());
    }
}
