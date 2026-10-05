package com.example.ecociente.ui;

import androidx.annotation.NonNull;
import com.example.ecociente.R;

public final class ItensBarra {

    public static final int HOME = 0;
    public static final int GUIA_OU_SOLICITACOES = 1;
    public static final int QUIZ_OU_CONDOMINIOS = 2;
    public static final int PERFIL = 3;

    private ItensBarra() {}

    @NonNull
    public static ItemBarra[] usuario() {
        return new ItemBarra[] {
            new ItemBarra(R.string.home, R.drawable.icon_home_verde, R.drawable.icon_home_cinza),
            new ItemBarra(R.string.guia, R.drawable.icon_guia_verde, R.drawable.icon_guia_cinza),
            new ItemBarra(R.string.quiz, R.drawable.icon_quiz_verde, R.drawable.icon_quiz_cinza),
            new ItemBarra(R.string.perfil, R.drawable.icon_perfil_verde, R.drawable.icon_perfil_cinza)
        };
    }

    @NonNull
    public static ItemBarra[] cooperativa() {
        return new ItemBarra[] {
            new ItemBarra(R.string.home, R.drawable.icon_home_verde, R.drawable.icon_home_cinza),
            new ItemBarra(
                    R.string.solicitacoes, R.drawable.icon_guia_verde, R.drawable.icon_guia_cinza),
            new ItemBarra(
                    R.string.condominios, R.drawable.icon_quiz_verde, R.drawable.icon_quiz_cinza),
            new ItemBarra(R.string.perfil, R.drawable.icon_perfil_verde, R.drawable.icon_perfil_cinza)
        };
    }
}
