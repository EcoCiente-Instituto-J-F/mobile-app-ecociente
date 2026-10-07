package com.example.ecociente.ui;

import android.content.Context;
import android.view.View;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.example.ecociente.R;

public final class MenuPerfil {

    private MenuPerfil() {}

    public static void mostrar(
            @NonNull Context contexto,
            @NonNull View ancora,
            @NonNull Runnable aoGerenciarPerfil,
            @NonNull Runnable aoSair) {

        PopupMenu menu = new PopupMenu(contexto, ancora);

        menu.getMenuInflater().inflate(R.menu.menu_perfil_home, menu.getMenu());

        menu.setOnMenuItemClickListener(
                item -> {
                    if (item.getItemId() == R.id.menuGerenciarPerfil) {
                        aoGerenciarPerfil.run();

                    } else if (item.getItemId() == R.id.menuSair) {
                        aoSair.run();
                    }

                    return true;
                });

        menu.show();
    }

    public static void confirmarSaida(@NonNull Context contexto, @NonNull Runnable aoConfirmar) {

        new AlertDialog.Builder(contexto)
                .setTitle(R.string.perfil_sair_titulo)
                .setMessage(R.string.perfil_sair_mensagem)
                .setPositiveButton(R.string.perfil_sair_confirmar, (dialogo, botao) -> aoConfirmar.run())
                .setNegativeButton(R.string.perfil_cancelar, null)
                .show();
    }
}
