package com.example.ecociente.ui;

import android.content.Context;
import androidx.annotation.NonNull;

public final class Dimensoes {

    private Dimensoes() {}

    public static int dpParaPx(@NonNull Context contexto, int dp) {

        return Math.round(dp * contexto.getResources().getDisplayMetrics().density);
    }
}
