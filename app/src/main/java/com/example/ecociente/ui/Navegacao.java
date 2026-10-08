package com.example.ecociente.ui;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.example.ecociente.views.CooperativaHomeActivity;
import com.example.ecociente.views.Login;
import com.example.ecociente.views.MensagensActivity;
import com.example.ecociente.views.SolicitacoesActivity;

public final class Navegacao {

    public static final String EXTRA_ORIGEM_BARRA = "origemBarra";

    private Navegacao() {}

    public static void abrirPelaBarra(@NonNull Activity de, @NonNull Intent rota, int indiceOrigem) {

        de.startActivity(rota.putExtra(EXTRA_ORIGEM_BARRA, indiceOrigem));

        de.overridePendingTransition(0, 0);
    }

    public static void abrirHomePelaBarra(
            @NonNull Activity de, @NonNull Class<?> home, int indiceOrigem) {

        abrirPelaBarra(
                de,
                new Intent(de, home)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                indiceOrigem);
    }

    public static void irParaItemDaCooperativa(@NonNull Activity de, int indice, int indiceAtual) {

        if (indice == indiceAtual) {
            return;
        }

        if (indice == ItensBarra.HOME) {
            abrirHomePelaBarra(de, CooperativaHomeActivity.class, indiceAtual);
            return;
        }

        Intent rota =
                indice == ItensBarra.TERCEIRO
                        ? new Intent(de, MensagensActivity.class)
                        : new Intent(de, SolicitacoesActivity.class)
                                .putExtra(SolicitacoesActivity.EXTRA_HISTORICO, indice == ItensBarra.QUARTO);

        abrirPelaBarra(de, rota, indiceAtual);

        if (indiceAtual != ItensBarra.HOME) {
            de.finish();
        }
    }

    public static void marcarNaBarra(
            @NonNull Activity tela, @NonNull BarraNavegacaoView barra, int indice) {

        barra.selecionar(indice, tela.getIntent().getIntExtra(EXTRA_ORIGEM_BARRA, ItensBarra.NENHUM));
    }

    public static void abrirLoginLimpandoPilha(@NonNull Activity origem) {

        Intent rota = new Intent(origem, Login.class);

        rota.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        origem.startActivity(rota);

        origem.finish();
    }
}
