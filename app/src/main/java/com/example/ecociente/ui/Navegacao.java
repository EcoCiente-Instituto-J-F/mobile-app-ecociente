package com.example.ecociente.ui;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.example.ecociente.views.Login;

public final class Navegacao {

    private Navegacao() {}

    public static void abrirLoginLimpandoPilha(@NonNull Activity origem) {

        Intent rota = new Intent(origem, Login.class);

        rota.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        origem.startActivity(rota);

        origem.finish();
    }
}
