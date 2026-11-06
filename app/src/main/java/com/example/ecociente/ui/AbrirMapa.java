package com.example.ecociente.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import com.example.ecociente.R;

// Abre a rota até um endereço no Google Maps (ou no navegador, se o app não existir). Só precisa
// do endereço em texto: não há latitude nem longitude no banco.
public final class AbrirMapa {

    private AbrirMapa() {}

    @NonNull
    public static Uri uriDaRota(@NonNull String endereco) {
        return Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + Uri.encode(endereco));
    }

    public static void rota(@NonNull Activity tela, @NonNull String endereco) {

        if (endereco.trim().isEmpty()) {
            AvisoFlutuante.mostrar(tela, R.string.mapa_sem_endereco);
            return;
        }

        try {
            tela.startActivity(new Intent(Intent.ACTION_VIEW, uriDaRota(endereco.trim())));

        } catch (ActivityNotFoundException erro) {
            AvisoFlutuante.mostrar(tela, R.string.mapa_erro);
        }
    }
}
