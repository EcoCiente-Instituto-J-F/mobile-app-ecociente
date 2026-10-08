package com.example.ecociente.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.example.ecociente.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec;
import com.google.android.material.progressindicator.IndeterminateDrawable;

public final class BotaoCarregando {

    private static final int ESPACO_ICONE_DP = 10;
    private static final float OPACIDADE_CARREGANDO = 0.85f;

    private BotaoCarregando() {}

    public static void definir(
            @NonNull MaterialButton botao,
            boolean carregando,
            @StringRes int textoNormal,
            @StringRes int textoCarregando) {

        botao.setEnabled(!carregando);

        if (!carregando) {
            botao.setAlpha(1f);
            botao.setIcon(null);
            botao.setText(textoNormal);
            return;
        }

        Context contexto = botao.getContext();

        CircularProgressIndicatorSpec especificacao =
                new CircularProgressIndicatorSpec(
                        contexto,
                        null,
                        0,
                        com.google.android.material.R.style
                                .Widget_Material3_CircularProgressIndicator_ExtraSmall);

        especificacao.indicatorColors = new int[] {contexto.getColor(R.color.branco)};

        botao.setAlpha(OPACIDADE_CARREGANDO);
        botao.setText(textoCarregando);
        botao.setIcon(IndeterminateDrawable.createCircularDrawable(contexto, especificacao));
        botao.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        botao.setIconPadding(Dimensoes.dpParaPx(contexto, ESPACO_ICONE_DP));
        botao.setIconTint(null);
    }
}
