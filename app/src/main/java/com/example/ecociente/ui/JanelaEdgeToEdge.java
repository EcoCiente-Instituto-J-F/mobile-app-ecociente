package com.example.ecociente.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public final class JanelaEdgeToEdge {

    private JanelaEdgeToEdge() {}

    public static void aplicar(@NonNull Activity tela) {

        Window janela = tela.getWindow();

        WindowCompat.setDecorFitsSystemWindows(janela, false);

        janela.setStatusBarColor(Color.TRANSPARENT);

        janela.setNavigationBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            janela.setNavigationBarContrastEnforced(false);
        }

        WindowInsetsControllerCompat controlador =
                WindowCompat.getInsetsController(janela, janela.getDecorView());

        controlador.setAppearanceLightStatusBars(true);

        controlador.setAppearanceLightNavigationBars(true);
    }
}
