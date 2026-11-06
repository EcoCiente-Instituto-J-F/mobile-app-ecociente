package com.example.ecociente.ui;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import com.example.ecociente.R;

public final class MenuPerfil {

    private MenuPerfil() {}

    public static void mostrar(
            @NonNull Context contexto,
            @NonNull View ancora,
            @NonNull Runnable aoGerenciarPerfil,
            @NonNull Runnable aoSair) {

        // A foto é um atalho direto. O logout permanece em Gerenciar perfil.
        aoGerenciarPerfil.run();
    }

    public static void confirmarSaida(@NonNull Context contexto, @NonNull Runnable aoConfirmar) {

        new DialogoEco.Builder(contexto)
                .titulo(R.string.perfil_sair_titulo)
                .mensagem(R.string.perfil_sair_mensagem)
                .botao(R.string.perfil_sair_confirmar, DialogoEco.Estilo.PERIGO, aoConfirmar)
                .botao(R.string.perfil_cancelar, DialogoEco.Estilo.PREENCHIDO, null)
                .mostrar();
    }
}
