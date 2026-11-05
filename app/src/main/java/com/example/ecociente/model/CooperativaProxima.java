package com.example.ecociente.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class CooperativaProxima {
    private final int id;
    private final String nome;
    private final String endereco;
    private final String cidade;
    private final String estado;
    @Nullable private final Double mediaAvaliacoes;
    private final long totalAvaliacoes;

    public CooperativaProxima(int id, @NonNull String nome, @NonNull String endereco,
            @NonNull String cidade, @NonNull String estado,
            @Nullable Double mediaAvaliacoes, long totalAvaliacoes) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.cidade = cidade;
        this.estado = estado;
        this.mediaAvaliacoes = mediaAvaliacoes;
        this.totalAvaliacoes = totalAvaliacoes;
    }

    public int getId() { return id; }
    @NonNull public String getNome() { return nome; }
    @NonNull public String getEndereco() { return endereco; }
    @NonNull public String getCidade() { return cidade; }
    @NonNull public String getEstado() { return estado; }
    @Nullable public Double getMediaAvaliacoes() { return mediaAvaliacoes; }
    public long getTotalAvaliacoes() { return totalAvaliacoes; }

    @NonNull public String getTextoLocalizacao() {
        String local = cidade;
        if (!estado.isEmpty()) local += " - " + estado;
        if (!endereco.isEmpty()) local = endereco + " · " + local;
        return local;
    }
}