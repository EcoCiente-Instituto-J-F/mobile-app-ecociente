package com.example.ecociente.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.example.ecociente.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

public class CalendarioMensalView extends LinearLayout {

    public interface OnNavegacaoListener {
        void aoMesAnterior();

        void aoProximoMes();
    }

    private static final int DIAS_NA_SEMANA = 7;
    private static final int ALTURA_LINHA_DP = 42;

    private final TextView textoMes;
    private final LinearLayout grade;
    private final LayoutInflater inflador;

    public CalendarioMensalView(@NonNull Context contexto, @Nullable AttributeSet atributos) {
        super(contexto, atributos);

        setOrientation(VERTICAL);

        inflador = LayoutInflater.from(contexto);

        inflador.inflate(R.layout.view_calendario_mensal, this, true);

        textoMes = findViewById(R.id.textoMesCalendario);
        grade = findViewById(R.id.gradeDiasCalendario);

        montarDiasDaSemana(findViewById(R.id.linhaDiasSemana));
    }

    public void setOnNavegacaoListener(@Nullable OnNavegacaoListener ouvinte) {

        findViewById(R.id.botaoMesAnterior)
                .setOnClickListener(view -> {
                    if (ouvinte != null) {
                        ouvinte.aoMesAnterior();
                    }
                });

        findViewById(R.id.botaoProximoMes)
                .setOnClickListener(view -> {
                    if (ouvinte != null) {
                        ouvinte.aoProximoMes();
                    }
                });
    }

    public void exibir(int ano, int mes, @NonNull Set<Integer> diasComColeta) {

        Calendar primeiro = Calendar.getInstance();

        primeiro.clear();
        primeiro.set(ano, mes, 1);

        textoMes.setText(tituloDoMes(primeiro));

        int diasNoMes = primeiro.getActualMaximum(Calendar.DAY_OF_MONTH);

        int deslocamento = (primeiro.get(Calendar.DAY_OF_WEEK) + 5) % DIAS_NA_SEMANA;

        Calendar anterior = (Calendar) primeiro.clone();

        anterior.add(Calendar.MONTH, -1);

        int diasNoMesAnterior = anterior.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar hoje = Calendar.getInstance();

        int diaAtual =
                hoje.get(Calendar.YEAR) == ano && hoje.get(Calendar.MONTH) == mes
                        ? hoje.get(Calendar.DAY_OF_MONTH)
                        : -1;

        int linhas = (deslocamento + diasNoMes + DIAS_NA_SEMANA - 1) / DIAS_NA_SEMANA;

        grade.removeAllViews();

        for (int linha = 0; linha < linhas; linha++) {

            LinearLayout linhaView = new LinearLayout(getContext());

            linhaView.setOrientation(HORIZONTAL);

            grade.addView(
                    linhaView,
                    new LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            Dimensoes.dpParaPx(getContext(), ALTURA_LINHA_DP)));

            for (int coluna = 0; coluna < DIAS_NA_SEMANA; coluna++) {

                int posicao = linha * DIAS_NA_SEMANA + coluna;

                int dia = posicao - deslocamento + 1;

                boolean doMes = dia >= 1 && dia <= diasNoMes;

                int numero = doMes ? dia : dia < 1 ? diasNoMesAnterior + dia : dia - diasNoMes;

                linhaView.addView(
                        criarDia(numero, doMes, doMes && dia == diaAtual, doMes && diasComColeta.contains(dia)));
            }
        }
    }

    @NonNull
    private View criarDia(int numero, boolean doMes, boolean hoje, boolean temColeta) {

        View item = inflador.inflate(R.layout.item_dia_calendario, grade, false);

        TextView texto = item.findViewById(R.id.textoDiaCalendario);

        texto.setText(String.valueOf(numero));

        if (!doMes) {
            texto.setTextColor(ContextCompat.getColor(getContext(), R.color.cinza_escuro_textos));
        }

        if (hoje) {
            texto.setBackgroundResource(R.drawable.fundo_dia_atual);
        }

        item.findViewById(R.id.marcaColetaCalendario)
                .setVisibility(temColeta ? View.VISIBLE : View.INVISIBLE);

        return item;
    }

    private void montarDiasDaSemana(@NonNull LinearLayout linha) {

        String[] dias = getResources().getStringArray(R.array.dias_semana_abreviados);

        for (String dia : dias) {

            TextView texto = new TextView(getContext());

            texto.setText(dia);
            texto.setGravity(android.view.Gravity.CENTER);
            texto.setTextSize(11);
            texto.setTextColor(ContextCompat.getColor(getContext(), R.color.cinza_texto_home));

            linha.addView(texto, new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
        }
    }

    @NonNull
    private String tituloDoMes(@NonNull Calendar primeiro) {

        String titulo =
                new SimpleDateFormat("MMMM 'de' yyyy", new Locale("pt", "BR"))
                        .format(primeiro.getTime());

        return Character.toUpperCase(titulo.charAt(0)) + titulo.substring(1);
    }
}
