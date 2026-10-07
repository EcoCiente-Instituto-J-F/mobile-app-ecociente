package com.example.ecociente.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.R;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.model.StatusAgendamento;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SemanaColetasView extends LinearLayout {

    private static final int DIAS_UTEIS = 5;
    private static final Locale PORTUGUES = new Locale("pt", "BR");

    private final LayoutInflater inflador;

    public SemanaColetasView(@NonNull Context contexto, @Nullable AttributeSet atributos) {
        super(contexto, atributos);

        setOrientation(HORIZONTAL);

        inflador = LayoutInflater.from(contexto);

        exibir(null);
    }

    @NonNull
    public static Calendar segundaDaSemana() {

        Calendar dia = Calendar.getInstance();

        dia.set(Calendar.HOUR_OF_DAY, 0);
        dia.set(Calendar.MINUTE, 0);
        dia.set(Calendar.SECOND, 0);
        dia.set(Calendar.MILLISECOND, 0);

        int desvio = (dia.get(Calendar.DAY_OF_WEEK) + 5) % 7;

        dia.add(Calendar.DAY_OF_MONTH, -desvio);

        return dia;
    }

    @NonNull
    public static Calendar fimDaSemana() {

        Calendar fim = segundaDaSemana();

        fim.add(Calendar.DAY_OF_MONTH, DIAS_UTEIS);
        fim.add(Calendar.SECOND, -1);

        return fim;
    }

    public void exibir(@Nullable List<Solicitacao> coletas) {

        removeAllViews();

        Calendar dia = segundaDaSemana();

        Calendar hoje = Calendar.getInstance();

        SimpleDateFormat nomeDoDia = new SimpleDateFormat("EEE", PORTUGUES);
        SimpleDateFormat dataCurta = new SimpleDateFormat("dd/MM", PORTUGUES);

        for (int i = 0; i < DIAS_UTEIS; i++) {

            boolean ehHoje = mesmoDia(dia, hoje);

            boolean temColeta = coletas != null && possuiColeta(coletas, dia);

            addView(criarDia(nomeDoDia.format(dia.getTime()), dataCurta.format(dia.getTime()), ehHoje, temColeta));

            dia.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    @NonNull
    private View criarDia(String nome, String data, boolean ehHoje, boolean temColeta) {

        View item = inflador.inflate(R.layout.item_dia_semana, this, false);

        ((TextView) item.findViewById(R.id.textoNomeDiaSemana)).setText(capitalizar(nome));
        ((TextView) item.findViewById(R.id.textoDataDiaSemana)).setText(data);

        item.findViewById(R.id.iconeColetaDiaSemana).setVisibility(temColeta ? VISIBLE : GONE);
        item.findViewById(R.id.pontoSemColetaDiaSemana).setVisibility(temColeta ? GONE : VISIBLE);

        ((TextView) item.findViewById(R.id.textoSituacaoDiaSemana))
                .setText(temColeta ? R.string.coleta : R.string.nao_ha_coleta);

        item.setBackgroundResource(
                ehHoje ? R.drawable.fundo_dia_semana_atual : R.drawable.fundo_dia_semana);

        LayoutParams parametros = (LayoutParams) item.getLayoutParams();

        parametros.setMarginEnd(Dimensoes.dpParaPx(getContext(), 6));

        item.setLayoutParams(parametros);

        return item;
    }

    private boolean possuiColeta(@NonNull List<Solicitacao> coletas, @NonNull Calendar dia) {

        for (Solicitacao coleta : coletas) {

            Calendar inicio = FormatoDataApi.calendario(coleta.getDataInicio());

            if (inicio != null
                    && coleta.getStatus() != StatusAgendamento.CANCELADO
                    && coleta.getStatus() != StatusAgendamento.RECUSADO
                    && mesmoDia(inicio, dia)) {
                return true;
            }
        }

        return false;
    }

    private boolean mesmoDia(@NonNull Calendar a, @NonNull Calendar b) {

        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    @NonNull
    private String capitalizar(@NonNull String texto) {

        String limpo = texto.replace(".", "");

        return limpo.isEmpty() ? limpo : Character.toUpperCase(limpo.charAt(0)) + limpo.substring(1);
    }
}
