package com.example.ecociente.ui;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.example.ecociente.R;

// Os servidores gratuitos dormem: na primeira chamada o login e o cadastro podem levar minutos.
// Se a espera passar de alguns segundos, avisa que o app não travou.
public final class AvisoServidorLento {

    private static final long ESPERA_MS = 8_000;

    private final Handler principal = new Handler(Looper.getMainLooper());
    private Runnable pendente;

    public void iniciar(@NonNull Activity tela) {

        parar();

        pendente = () -> AvisoFlutuante.mostrar(tela, R.string.aviso_servidor_lento);

        principal.postDelayed(pendente, ESPERA_MS);
    }

    public void parar() {

        if (pendente != null) {
            principal.removeCallbacks(pendente);
            pendente = null;
        }
    }
}
