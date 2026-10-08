package com.example.ecociente.model;

import androidx.annotation.NonNull;
import com.example.ecociente.ui.FormatoDataApi;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Coletas {

    private Coletas() {}

    public static boolean estaAtiva(@NonNull Solicitacao coleta) {
        return coleta.getStatus() != StatusAgendamento.CANCELADO
                && coleta.getStatus() != StatusAgendamento.RECUSADO;
    }

    @NonNull
    public static List<Solicitacao> ativasDoMes(
            @NonNull List<Solicitacao> coletas, int ano, int mes) {

        List<Solicitacao> doMes = new ArrayList<>();

        for (Solicitacao coleta : coletas) {

            Calendar inicio = FormatoDataApi.calendario(coleta.getDataInicio());

            if (inicio != null
                    && estaAtiva(coleta)
                    && inicio.get(Calendar.YEAR) == ano
                    && inicio.get(Calendar.MONTH) == mes) {
                doMes.add(coleta);
            }
        }

        doMes.sort(Comparator.comparing(coleta -> coleta.getDataInicio()));

        return doMes;
    }

    @NonNull
    public static Set<Integer> diasDoMes(@NonNull List<Solicitacao> coletas, int ano, int mes) {

        Set<Integer> dias = new HashSet<>();

        for (Solicitacao coleta : ativasDoMes(coletas, ano, mes)) {
            dias.add(FormatoDataApi.calendario(coleta.getDataInicio()).get(Calendar.DAY_OF_MONTH));
        }

        return dias;
    }
}
