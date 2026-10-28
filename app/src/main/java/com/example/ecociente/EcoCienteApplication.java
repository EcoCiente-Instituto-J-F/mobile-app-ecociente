package com.example.ecociente;

import android.app.Activity;
import android.app.Application;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.views.SplashScreen;

// As telas foram desenhadas para celular. Em tablet (largura mínima de 600 dp ou mais) duas coisas
// mantêm o app proporcional: a escala de tudo (textos, botões, margens) cresce até a tela parecer
// um celular grande, e o conteúdo de cada tela é limitado a R.dimen.largura_maxima_conteudo e
// centralizado. No celular nada muda. A splash fica de fora do limite de largura porque o vídeo
// ocupa a tela inteira.
public class EcoCienteApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private static final int MENOR_LARGURA_TABLET_DP = 600;

    // A escala leva a menor largura da tela para perto deste valor, que é o de um celular grande.
    private static final float LARGURA_ALVO_DP = 480f;

    private static final float ESCALA_MAXIMA = 1.6f;

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(this);
    }

    // Roda antes de a tela montar o layout. Parte sempre das medidas reais do sistema, para não
    // acumular a escala quando uma tela é recriada (como ao girar o aparelho).
    @SuppressWarnings("deprecation") // scaledDensity ainda é lida por versões antigas do Android
    private static void aplicarEscalaDeTablet(Activity activity) {

        Resources sistema = Resources.getSystem();

        int menorLargura = sistema.getConfiguration().smallestScreenWidthDp;

        if (menorLargura < MENOR_LARGURA_TABLET_DP) {
            return;
        }

        float escala = Math.min(ESCALA_MAXIMA, menorLargura / LARGURA_ALVO_DP);

        DisplayMetrics real = sistema.getDisplayMetrics();

        DisplayMetrics metricas = activity.getResources().getDisplayMetrics();

        metricas.density = real.density * escala;
        metricas.scaledDensity = real.scaledDensity * escala;
        metricas.densityDpi = Math.round(real.densityDpi * escala);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle estado) {
        aplicarEscalaDeTablet(activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

        if (activity instanceof SplashScreen) {
            return;
        }

        int larguraMaxima =
                activity.getResources().getDimensionPixelSize(R.dimen.largura_maxima_conteudo);

        if (larguraMaxima <= 0) {
            return;
        }

        // Com a escala de tablet, os dp do limite crescem junto: o conteúdo nunca passa da largura real
        // da tela, senão a raiz fica maior que o aparelho e é cortada dos dois lados.
        larguraMaxima = Math.min(larguraMaxima, activity.getResources().getDisplayMetrics().widthPixels);

        ViewGroup conteudo = activity.findViewById(android.R.id.content);

        if (conteudo == null || conteudo.getChildCount() == 0) {
            return;
        }

        View raiz = conteudo.getChildAt(0);

        if (!(raiz.getLayoutParams() instanceof FrameLayout.LayoutParams)) {
            return;
        }

        FrameLayout.LayoutParams parametros = (FrameLayout.LayoutParams) raiz.getLayoutParams();

        int vertical =
                parametros.gravity == FrameLayout.LayoutParams.UNSPECIFIED_GRAVITY
                        ? Gravity.TOP
                        : parametros.gravity & Gravity.VERTICAL_GRAVITY_MASK;

        int gravidade = vertical | Gravity.CENTER_HORIZONTAL;

        if (parametros.width == larguraMaxima && parametros.gravity == gravidade) {
            return;
        }

        parametros.width = larguraMaxima;

        parametros.gravity = gravidade;

        raiz.setLayoutParams(parametros);
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {}

    @Override
    public void onActivityPaused(@NonNull Activity activity) {}

    @Override
    public void onActivityStopped(@NonNull Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle estado) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {}
}
