package com.example.ecociente.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import androidx.annotation.NonNull;
import com.example.ecociente.R;
import com.google.android.material.chip.Chip;

// Chip de seleção no visual do app: verde-claro quando marcado, branco com borda quando não.
public final class ChipEco {

    private ChipEco() {}

    @NonNull
    public static Chip criar(@NonNull Context contexto, @NonNull String texto, boolean marcado) {

        Chip chip = new Chip(contexto);

        chip.setText(texto);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(false);
        chip.setChecked(marcado);
        chip.setTextColor(contexto.getColor(R.color.verde_escuro_principal));
        chip.setChipBackgroundColor(
                new ColorStateList(
                        new int[][] {{android.R.attr.state_checked}, {}},
                        new int[] {
                            contexto.getColor(R.color.verde_claro_ecociente), contexto.getColor(R.color.branco)
                        }));
        chip.setChipStrokeColor(ColorStateList.valueOf(contexto.getColor(R.color.cinza_borda_clara_home)));
        chip.setChipStrokeWidth(contexto.getResources().getDisplayMetrics().density);

        return chip;
    }
}
