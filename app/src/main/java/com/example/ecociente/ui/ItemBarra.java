package com.example.ecociente.ui;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

public final class ItemBarra {
    @StringRes final int rotulo;
    @DrawableRes final int iconeAtivo;
    @DrawableRes final int iconeInativo;

    public ItemBarra(@StringRes int rotulo, @DrawableRes int icone) {
        this(rotulo, icone, icone);
    }

    public ItemBarra(
            @StringRes int rotulo, @DrawableRes int iconeAtivo, @DrawableRes int iconeInativo) {
        this.rotulo = rotulo;
        this.iconeAtivo = iconeAtivo;
        this.iconeInativo = iconeInativo;
    }
}
