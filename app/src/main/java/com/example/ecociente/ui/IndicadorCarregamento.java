package com.example.ecociente.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.example.ecociente.R;

// O círculo verde girando, com uma frase embaixo, para as listas enquanto os dados chegam.
public final class IndicadorCarregamento {

    private static final int TAMANHO_DP = 42;

    private IndicadorCarregamento() {}

    // Põe o indicador no fim de `lista` e devolve a view, caso quem chama queira tirá-la depois.
    @NonNull
    public static View mostrar(@NonNull ViewGroup lista, @StringRes int mensagem) {

        Context contexto = lista.getContext();

        float densidade = contexto.getResources().getDisplayMetrics().density;

        LinearLayout bloco = new LinearLayout(contexto);
        bloco.setOrientation(LinearLayout.VERTICAL);
        bloco.setGravity(Gravity.CENTER);
        bloco.setPadding(48, 96, 48, 0);

        ProgressBar indicador = new ProgressBar(contexto);
        indicador.setIndeterminateTintList(
                ColorStateList.valueOf(contexto.getColor(R.color.verde_escuro_principal)));
        bloco.addView(
                indicador,
                new LinearLayout.LayoutParams(
                        Math.round(TAMANHO_DP * densidade), Math.round(TAMANHO_DP * densidade)));

        TextView texto = new TextView(contexto);
        texto.setText(mensagem);
        texto.setTextColor(contexto.getColor(R.color.cinza_texto_auxiliar));
        texto.setTextSize(14);
        texto.setGravity(Gravity.CENTER);
        texto.setPadding(0, 24, 0, 0);
        bloco.addView(texto);

        lista.addView(bloco);

        return bloco;
    }
}
