package com.example.ecociente.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.example.ecociente.R;

public enum StatusAgendamento {
    AGENDADO(R.string.status_agendado),
    CONFIRMADO(R.string.status_confirmado),
    RECUSADO(R.string.status_recusado),
    CANCELADO(R.string.status_cancelado),
    REALIZADO(R.string.status_realizado);

    @StringRes private final int rotulo;

    StatusAgendamento(@StringRes int rotulo) {
        this.rotulo = rotulo;
    }

    @StringRes
    public int getRotulo() {
        return rotulo;
    }

    @NonNull
    public static StatusAgendamento daApi(@Nullable String valor) {

        for (StatusAgendamento status : values()) {
            if (status.name().equalsIgnoreCase(valor)) {
                return status;
            }
        }

        return AGENDADO;
    }
}
