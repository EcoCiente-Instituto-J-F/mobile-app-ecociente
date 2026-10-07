package com.example.ecociente.ui;

import androidx.annotation.NonNull;
import com.example.ecociente.R;

public final class ItensBarra {

    public static final int NENHUM = -1;
    public static final int HOME = 0;
    public static final int SEGUNDO = 1;
    public static final int TERCEIRO = 2;
    public static final int QUARTO = 3;

    private ItensBarra() {}

    @NonNull
    public static ItemBarra[] usuario() {
        return new ItemBarra[] {
            home(),
            new ItemBarra(R.string.guia, R.drawable.icon_guia_verde, R.drawable.icon_guia_cinza),
            new ItemBarra(R.string.quiz, R.drawable.icon_quiz_verde, R.drawable.icon_quiz_cinza),
            new ItemBarra(R.string.perfil, R.drawable.icon_perfil_verde, R.drawable.icon_perfil_cinza)
        };
    }

    @NonNull
    public static ItemBarra[] morador() {
        return new ItemBarra[] {
            usuario()[HOME],
            usuario()[SEGUNDO],
            usuario()[TERCEIRO],
            new ItemBarra(R.string.feed, R.drawable.ic_nav_feed)
        };
    }

    @NonNull
    public static ItemBarra[] sindico() {
        return new ItemBarra[] {
            home(),
            new ItemBarra(R.string.solicitacao, R.drawable.ic_nav_coletas),
            new ItemBarra(R.string.historico, R.drawable.ic_nav_historico),
            new ItemBarra(R.string.unidades, R.drawable.ic_nav_unidades)
        };
    }

    @NonNull
    public static ItemBarra[] cooperativa() {
        return new ItemBarra[] {
            home(),
            new ItemBarra(R.string.coletas, R.drawable.ic_nav_coletas),
            new ItemBarra(R.string.mensagens, R.drawable.ic_nav_mensagens),
            new ItemBarra(R.string.historico, R.drawable.ic_nav_historico)
        };
    }

    private static ItemBarra home() {
        return new ItemBarra(R.string.home, R.drawable.icon_home_verde, R.drawable.icon_home_cinza);
    }
}
