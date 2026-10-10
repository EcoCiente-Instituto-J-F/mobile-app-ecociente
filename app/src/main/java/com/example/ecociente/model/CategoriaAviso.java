package com.example.ecociente.model;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import com.example.ecociente.R;

public enum CategoriaAviso {
    COLETA(R.string.categoria_coleta, R.color.chip_coleta_fundo, R.color.chip_coleta_texto),
    ORIENTACAO(R.string.categoria_orientacao, R.color.chip_orientacao_fundo, R.color.chip_orientacao_texto),
    MANUTENCAO(R.string.categoria_manutencao, R.color.chip_manutencao_fundo, R.color.chip_manutencao_texto),
    CAMPANHA(R.string.categoria_campanha, R.color.chip_campanha_fundo, R.color.chip_campanha_texto),
    AVISO_GERAL(R.string.categoria_aviso_geral, R.color.chip_geral_fundo, R.color.chip_geral_texto),
    SEGURANCA(R.string.categoria_seguranca, R.color.chip_seguranca_fundo, R.color.chip_seguranca_texto);

    @StringRes public final int rotulo;
    @ColorRes public final int fundo;
    @ColorRes public final int texto;

    CategoriaAviso(@StringRes int rotulo, @ColorRes int fundo, @ColorRes int texto) {
        this.rotulo = rotulo;
        this.fundo = fundo;
        this.texto = texto;
    }
}
