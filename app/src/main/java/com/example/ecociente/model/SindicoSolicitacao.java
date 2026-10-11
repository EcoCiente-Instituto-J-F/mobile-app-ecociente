package com.example.ecociente.model;

import androidx.annotation.NonNull;

public final class SindicoSolicitacao {

    private final int id;
    private final int cooperativaId;
    private final String cooperativa;
    private final String inicio;
    private final boolean recorrente;
    private final StatusAgendamento status;

    public SindicoSolicitacao(
            int id,
            int cooperativaId,
            @NonNull String cooperativa,
            @NonNull String inicio,
            boolean recorrente,
            @NonNull StatusAgendamento status) {
        this.id = id;
        this.cooperativaId = cooperativaId;
        this.cooperativa = cooperativa;
        this.inicio = inicio;
        this.recorrente = recorrente;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getCooperativaId() {
        return cooperativaId;
    }

    @NonNull
    public String getCooperativa() {
        return cooperativa;
    }

    @NonNull
    public String getInicio() {
        return inicio;
    }

    public boolean isRecorrente() {
        return recorrente;
    }

    @NonNull
    public StatusAgendamento getStatus() {
        return status;
    }

    @NonNull
    public SindicoSolicitacao comStatus(@NonNull StatusAgendamento novoStatus) {
        return new SindicoSolicitacao(id, cooperativaId, cooperativa, inicio, recorrente, novoStatus);
    }

    @NonNull
    public Solicitacao paraSolicitacao(@NonNull String nomeCondominio) {
        DetalheSolicitacao detalhe =
                new DetalheSolicitacao(
                        new String[] {"Papelão", "Plástico"},
                        "150kg",
                        "Material limpo e separado por tipo. Preferência por coleta no período da manhã.",
                        "Cooperativa de reciclagem",
                        "Jardim das Flores - SP",
                        null,
                        null);

        return new Solicitacao(id, 0, cooperativaId, inicio, inicio, status, recorrente, nomeCondominio, detalhe);
    }
}
