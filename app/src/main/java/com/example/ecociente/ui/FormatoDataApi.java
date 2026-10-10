package com.example.ecociente.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class FormatoDataApi {

    private static final int TAMANHO_ISO_SEM_FRACAO = 19;
    private static final Locale PORTUGUES = new Locale("pt", "BR");

    private FormatoDataApi() {}

    @NonNull
    public static String data(@Nullable String iso) {
        return formatar(iso, "dd/MM/yyyy");
    }

    @NonNull
    public static String dataCompacta(@Nullable String iso) {
        return formatar(iso, "d 'de' MMM 'de' yyyy");
    }

    @NonNull
    public static String dataLonga(@Nullable String iso) {
        return formatar(iso, "d 'de' MMMM 'de' yyyy");
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

    @NonNull
    public static String relativa(@Nullable String iso, long agoraMs) {

        Calendar data = calendario(iso);

        if (data == null) {
            return "";
        }

        long minutos = TimeUnit.MILLISECONDS.toMinutes(agoraMs - data.getTimeInMillis());

        if (minutos < 1) {
            return "Agora";
        }

        if (minutos < 60) {
            return "Há " + minutos + " min";
        }

        long horas = minutos / 60;

        if (horas < 24) {
            return "Há " + horas + " h";
        }

        long dias = horas / 24;

        if (dias == 1) {
            return "Ontem";
        }

        return dias < 7 ? "Há " + dias + " dias" : formatar(iso, "d 'de' MMM");
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
