package com.example.ecociente.model;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.example.ecociente.R;

public enum StatusAgendamento {
    AGENDADO(
            R.string.status_agendado,
            R.string.etiqueta_pendente,
            R.color.etiqueta_pendente_fundo,
            R.color.etiqueta_pendente_texto),
    CONFIRMADO(
            R.string.status_confirmado,
            R.string.etiqueta_aceito,
            R.color.etiqueta_aceito_fundo,
            R.color.etiqueta_aceito_texto),
    RECUSADO(
            R.string.status_recusado,
            R.string.etiqueta_recusado,
            R.color.etiqueta_recusado_fundo,
            R.color.etiqueta_recusado_texto),
    CANCELADO(
            R.string.status_cancelado,
            R.string.etiqueta_cancelado,
            R.color.chip_geral_fundo,
            R.color.chip_geral_texto),
    REALIZADO(
            R.string.status_realizado,
            R.string.etiqueta_concluida,
            R.color.etiqueta_aceito_fundo,
            R.color.etiqueta_aceito_texto);

    @StringRes private final int rotulo;
    @StringRes private final int etiqueta;
    @ColorRes private final int fundoEtiqueta;
    @ColorRes private final int textoEtiqueta;

    StatusAgendamento(
            @StringRes int rotulo,
            @StringRes int etiqueta,
            @ColorRes int fundoEtiqueta,
            @ColorRes int textoEtiqueta) {
        this.rotulo = rotulo;
        this.etiqueta = etiqueta;
        this.fundoEtiqueta = fundoEtiqueta;
        this.textoEtiqueta = textoEtiqueta;
    }

    @StringRes
    public int getEtiqueta() {
        return etiqueta;
    }

    @ColorRes
    public int getFundoEtiqueta() {
        return fundoEtiqueta;
    }

    @ColorRes
    public int getTextoEtiqueta() {
        return textoEtiqueta;
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
