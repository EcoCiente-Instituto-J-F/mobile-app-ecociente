package com.example.ecociente.model;

import androidx.annotation.NonNull;

// Uma linha do histórico de pontos da ds-pontuacao-api.
public final class MovimentacaoPontos {

    public enum Origem { POSTAGEM, QUIZ, OUTRA }

    public enum Tipo { CREDITO, ESTORNO, RESTAURACAO }

    private final Origem origem;
    private final Tipo tipo;
    private final int pontos;
    private final String ocorridoEm;

    public MovimentacaoPontos(
            @NonNull Origem origem, @NonNull Tipo tipo, int pontos, @NonNull String ocorridoEm) {
        this.origem = origem;
        this.tipo = tipo;
        this.pontos = pontos;
        this.ocorridoEm = ocorridoEm;
    }

    @NonNull
    public Origem getOrigem() {
        return origem;
    }

    @NonNull
    public Tipo getTipo() {
        return tipo;
    }

    public int getPontos() {
        return pontos;
    }

    // Data e hora em ISO-8601, como a API devolve.
    @NonNull
    public String getOcorridoEm() {
        return ocorridoEm;
    }

    // Estorno tira pontos; crédito e restauração somam.
    public int getPontosComSinal() {
        return tipo == Tipo.ESTORNO ? -pontos : pontos;
    }
}
