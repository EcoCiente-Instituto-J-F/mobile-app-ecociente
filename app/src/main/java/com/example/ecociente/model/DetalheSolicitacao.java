package com.example.ecociente.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public final class DetalheSolicitacao implements Serializable {

    private final String[] materiais;
    private final String volume;
    private final String observacoes;
    private final String tipoCondominio;
    private final String cidade;
    private final String recebidaEm;
    private final String atualizadaEm;

    public DetalheSolicitacao(
            @NonNull String[] materiais,
            @Nullable String volume,
            @Nullable String observacoes,
            @Nullable String tipoCondominio,
            @Nullable String cidade,
            @Nullable String recebidaEm,
            @Nullable String atualizadaEm) {
        this.materiais = materiais;
        this.volume = volume;
        this.observacoes = observacoes;
        this.tipoCondominio = tipoCondominio;
        this.cidade = cidade;
        this.recebidaEm = recebidaEm;
        this.atualizadaEm = atualizadaEm;
    }

    @NonNull
    public String[] getMateriais() {
        return materiais;
    }

    @Nullable
    public String getVolume() {
        return volume;
    }

    @Nullable
    public String getObservacoes() {
        return observacoes;
    }

    @Nullable
    public String getTipoCondominio() {
        return tipoCondominio;
    }

    @Nullable
    public String getCidade() {
        return cidade;
    }

    @Nullable
    public String getRecebidaEm() {
        return recebidaEm;
    }

    @Nullable
    public String getAtualizadaEm() {
        return atualizadaEm;
    }

    @Override
    public boolean equals(Object outro) {

        if (this == outro) {
            return true;
        }

        if (!(outro instanceof DetalheSolicitacao)) {
            return false;
        }

        DetalheSolicitacao outroDetalhe = (DetalheSolicitacao) outro;

        return Arrays.equals(materiais, outroDetalhe.materiais)
                && Objects.equals(volume, outroDetalhe.volume)
                && Objects.equals(observacoes, outroDetalhe.observacoes)
                && Objects.equals(tipoCondominio, outroDetalhe.tipoCondominio)
                && Objects.equals(cidade, outroDetalhe.cidade)
                && Objects.equals(recebidaEm, outroDetalhe.recebidaEm)
                && Objects.equals(atualizadaEm, outroDetalhe.atualizadaEm);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(materiais)
                + Objects.hash(volume, observacoes, tipoCondominio, cidade, recebidaEm, atualizadaEm);
    }
}
