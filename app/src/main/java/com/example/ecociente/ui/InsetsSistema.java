package com.example.ecociente.ui;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.function.Consumer;

public final class InsetsSistema {

    private InsetsSistema() {}

    public static void aplicarComoPadding(@NonNull View raiz) {

        aplicarComoPadding(raiz, null);
    }

    public static void aplicarComoPadding(@NonNull View raiz, @Nullable Consumer<Insets> aposAplicar) {

        ViewCompat.setOnApplyWindowInsetsListener(
                raiz,
                (view, insets) -> {
                    Insets sistema =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                                            | WindowInsetsCompat.Type.displayCutout());

                    view.setPadding(sistema.left, sistema.top, sistema.right, sistema.bottom);

                    if (aposAplicar != null) {
                        aposAplicar.accept(sistema);
                    }

                    return insets;
                });

        ViewCompat.requestApplyInsets(raiz);
    }
}
