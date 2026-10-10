package com.example.ecociente.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class Solicitacao implements Serializable {

    private final int id;
    private final int condominioId;
    private final int cooperativaId;
    private final String dataInicio;
    private final String dataFim;
    private final StatusAgendamento status;
    private final boolean possuiRecorrencia;
    private final String nomeCondominio;
    private final DetalheSolicitacao detalhe;

    public Solicitacao(
            int id,
            int condominioId,
            int cooperativaId,
            @Nullable String dataInicio,
            @Nullable String dataFim,
            @NonNull StatusAgendamento status,
            boolean possuiRecorrencia) {
        this(id, condominioId, cooperativaId, dataInicio, dataFim, status, possuiRecorrencia, null, null);
    }

    public Solicitacao(
            int id,
            int condominioId,
            int cooperativaId,
            @Nullable String dataInicio,
            @Nullable String dataFim,
            @NonNull StatusAgendamento status,
            boolean possuiRecorrencia,
            @Nullable String nomeCondominio,
            @Nullable DetalheSolicitacao detalhe) {
        this.nomeCondominio = nomeCondominio;
        this.detalhe = detalhe;
        this.id = id;
        this.condominioId = condominioId;
        this.cooperativaId = cooperativaId;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = status;
        this.possuiRecorrencia = possuiRecorrencia;
    }

    @Nullable
    public DetalheSolicitacao getDetalhe() {
        return detalhe;
    }

    @Nullable
    public String getNomeCondominio() {
        return nomeCondominio;
    }

    @NonNull
    public static List<Solicitacao> exemplos() {
        return Arrays.asList(
                exemplo(1, "Condomínio Raio de Luz", "2026-10-12T09:00:00", StatusAgendamento.AGENDADO, true),
                exemplo(2, "Residencial Jardim das Flores", "2026-10-13T14:30:00", StatusAgendamento.AGENDADO, false),
                exemplo(3, "Edifício Solar", "2026-10-15T08:00:00", StatusAgendamento.AGENDADO, false),
                exemplo(4, "Condomínio Vila Verde", "2026-10-09T10:00:00", StatusAgendamento.CONFIRMADO, true),
                exemplo(5, "Residencial Aurora", "2026-10-10T15:00:00", StatusAgendamento.CONFIRMADO, false),
                exemplo(6, "Condomínio Parque das Árvores", "2026-10-07T11:00:00", StatusAgendamento.RECUSADO, false),
                exemplo(7, "Condomínio Bela Vista", "2026-10-06T16:00:00", StatusAgendamento.CANCELADO, false),
                exemplo(8, "Residencial Primavera", "2026-10-02T09:30:00", StatusAgendamento.REALIZADO, true),
                exemplo(9, "Condomínio Raio de Luz", "2026-09-28T10:00:00", StatusAgendamento.REALIZADO, false),
                exemplo(10, "Edifício Solar", "2026-09-21T14:00:00", StatusAgendamento.REALIZADO, false),
                exemplo(11, "Condomínio Vila Verde", "2026-08-14T09:00:00", StatusAgendamento.REALIZADO, true),
                exemplo(12, "Residencial Jardim das Flores", "2026-06-30T13:30:00", StatusAgendamento.REALIZADO, false));
    }

    private static Solicitacao exemplo(
            int id,
            String nome,
            String inicio,
            StatusAgendamento status,
            boolean recorrente) {

        String[][] materiais = {
            {"Papelão", "Plástico"}, {"Vidro", "Metal"}, {"Papelão"}, {"Plástico", "Vidro", "Metal"}
        };

        String[] volumes = {"150kg", "80kg", "230kg", "120kg"};

        DetalheSolicitacao detalhe =
                new DetalheSolicitacao(
                        materiais[id % materiais.length],
                        volumes[id % volumes.length],
                        "Material limpo e separado por tipo. Preferência por coleta no período da manhã.",
                        "Condomínio Residencial",
                        "Jardim das Flores - SP",
                        "2026-10-05T10:24:00",
                        "2026-10-06T14:32:00");

        return new Solicitacao(id, id, 1, inicio, inicio, status, recorrente, nome, detalhe);
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
                && Objects.equals(nomeCondominio, outraSolicitacao.nomeCondominio)
                && Objects.equals(detalhe, outraSolicitacao.detalhe)
                && Objects.equals(dataInicio, outraSolicitacao.dataInicio)
                && Objects.equals(dataFim, outraSolicitacao.dataFim);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, condominioId, cooperativaId, dataInicio, dataFim, status, possuiRecorrencia, nomeCondominio, detalhe);
    }
}
