package com.example.ecociente;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.views.SplashScreen;

// Em tablet as telas, desenhadas para celular, ficavam esticadas na largura toda. Aqui o conteúdo de
// cada tela é limitado a R.dimen.largura_maxima_conteudo e centralizado. No celular o valor é 0 e
// nada muda. A splash fica de fora porque o vídeo ocupa a tela inteira.
public class EcoCienteApplication extends Application implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(this);
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
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle estado) {}

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
