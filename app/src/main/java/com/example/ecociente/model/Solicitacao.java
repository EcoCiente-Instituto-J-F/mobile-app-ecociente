package com.example.ecociente.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;

public final class Solicitacao implements Serializable {

    private final int id;
    private final int condominioId;
    private final int cooperativaId;
    private final String dataInicio;
    private final String dataFim;
    private final StatusAgendamento status;
    private final boolean possuiRecorrencia;

    public Solicitacao(
            int id,
            int condominioId,
            int cooperativaId,
            @Nullable String dataInicio,
            @Nullable String dataFim,
            @NonNull StatusAgendamento status,
            boolean possuiRecorrencia) {
        this.id = id;
        this.condominioId = condominioId;
        this.cooperativaId = cooperativaId;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = status;
        this.possuiRecorrencia = possuiRecorrencia;
    }

    public int getId() {
        return id;
    }

    public int getCondominioId() {
        return condominioId;
    }

    public int getCooperativaId() {
        return cooperativaId;
    }

    @Nullable
    public String getDataInicio() {
        return dataInicio;
    }

    @Nullable
    public String getDataFim() {
        return dataFim;
    }

    @NonNull
    public StatusAgendamento getStatus() {
        return status;
    }

    public boolean possuiRecorrencia() {
        return possuiRecorrencia;
    }

    @Override
    public boolean equals(Object outro) {

        if (this == outro) {
            return true;
        }

        if (!(outro instanceof Solicitacao)) {
            return false;
        }

        Solicitacao outraSolicitacao = (Solicitacao) outro;

        return id == outraSolicitacao.id
                && condominioId == outraSolicitacao.condominioId
                && cooperativaId == outraSolicitacao.cooperativaId
                && possuiRecorrencia == outraSolicitacao.possuiRecorrencia
                && status == outraSolicitacao.status
                && Objects.equals(dataInicio, outraSolicitacao.dataInicio)
                && Objects.equals(dataFim, outraSolicitacao.dataFim);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, condominioId, cooperativaId, dataInicio, dataFim, status, possuiRecorrencia);
    }
}
