package com.example.ecociente.ui;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.example.ecociente.R;

public final class AvisoFlutuante {

    private static final long DURACAO_MS = 3000L;
    private static final long TRANSICAO_MS = 220L;
    private static final int MARGEM_TOPO_DP = 56;

    private AvisoFlutuante() {}

    public static void mostrar(@NonNull Activity tela, @StringRes int texto) {

        ViewGroup conteudo = tela.findViewById(android.R.id.content);

        View aviso = LayoutInflater.from(tela).inflate(R.layout.aviso_flutuante, conteudo, false);

        ((TextView) aviso.findViewById(R.id.textoAvisoFlutuante)).setText(texto);

        FrameLayout.LayoutParams parametros =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        parametros.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        parametros.topMargin = Dimensoes.dpParaPx(tela, MARGEM_TOPO_DP);

        aviso.setLayoutParams(parametros);
        aviso.setAlpha(0f);

        conteudo.addView(aviso);

        aviso.animate().alpha(1f).setDuration(TRANSICAO_MS).start();

        aviso.postDelayed(
                () ->
                        aviso.animate()
                                .alpha(0f)
                                .setDuration(TRANSICAO_MS)
                                .withEndAction(() -> conteudo.removeView(aviso))
                                .start(),
                DURACAO_MS);
    }
}
