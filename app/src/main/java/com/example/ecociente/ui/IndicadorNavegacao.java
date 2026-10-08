package com.example.ecociente.ui;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

public final class IndicadorNavegacao {

    private IndicadorNavegacao() {}

    public static void posicionarSobre(@NonNull View indicador, @NonNull View itemAtivo) {

        indicador.post(() -> indicador.setTranslationX(centroDe(indicador, itemAtivo)));
    }

    public static void deslizar(
            @NonNull View indicador, @NonNull View itemOrigem, @NonNull View itemDestino) {

        indicador.post(
                () -> {
                    indicador.setTranslationX(centroDe(indicador, itemOrigem));

                    indicador
                            .animate()
                            .translationX(centroDe(indicador, itemDestino))
                            .setDuration(Motion.ENTER_MS)
                            .setInterpolator(new FastOutSlowInInterpolator())
                            .start();
                });
    }

    private static float centroDe(@NonNull View indicador, @NonNull View item) {

        return item.getX() + (item.getWidth() / 2f) - (indicador.getWidth() / 2f);
    }
}
