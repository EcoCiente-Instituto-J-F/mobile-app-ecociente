package com.example.ecociente.model;

// Onde o morador está no ranking do condomínio no ciclo atual.
public final class PosicaoNoRanking {

    private final long posicao;
    private final long pontos;
    private final long totalParticipantes;

    public PosicaoNoRanking(long posicao, long pontos, long totalParticipantes) {
        this.posicao = posicao;
        this.pontos = pontos;
        this.totalParticipantes = totalParticipantes;
    }

    public long getPosicao() {
        return posicao;
    }

    public long getPontos() {
        return pontos;
    }

    public long getTotalParticipantes() {
        return totalParticipantes;
    }

    // A API devolve posição 0 (ou vazia) para quem ainda não pontuou no ciclo.
    public boolean estaNoRanking() {
        return posicao > 0 && totalParticipantes > 0;
    }
}
