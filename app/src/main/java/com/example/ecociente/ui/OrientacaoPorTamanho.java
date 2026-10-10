package com.example.ecociente.ui;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class OrientacaoPorTamanho implements Application.ActivityLifecycleCallbacks {

    private static final int LARGURA_MINIMA_TABLET_DP = 600;

    private static final boolean TABLET_PODE_GIRAR = false;

    @Override
    public void onActivityCreated(@NonNull Activity tela, @Nullable Bundle estadoSalvo) {

        boolean tablet =
                tela.getResources().getConfiguration().smallestScreenWidthDp >= LARGURA_MINIMA_TABLET_DP;

        tela.setRequestedOrientation(
                tablet && TABLET_PODE_GIRAR
                        ? ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onActivityStarted(@NonNull Activity tela) {}

    @Override
    public void onActivityResumed(@NonNull Activity tela) {}

    @Override
    public void onActivityPaused(@NonNull Activity tela) {}

    @Override
    public void onActivityStopped(@NonNull Activity tela) {}

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity tela, @NonNull Bundle estado) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity tela) {}
}
