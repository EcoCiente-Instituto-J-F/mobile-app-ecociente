package com.example.ecociente.model;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.Serializable;

// Resultado da correção feita pela ds-mobile-api ao finalizar uma tentativa de quiz.
public final class ResultadoQuiz implements Serializable {

    private static final String EXTRA = "quizResultado";

    private final int tentativaId;
    private final double nota;
    private final int acertos;
    private final int totalPerguntas;
    private final boolean aprovado;
    private final int pontosConcedidos;

    public ResultadoQuiz(
            int tentativaId, double nota, int acertos, int totalPerguntas, boolean aprovado, int pontosConcedidos) {
        this.tentativaId = tentativaId;
        this.nota = nota;
        this.acertos = acertos;
        this.totalPerguntas = totalPerguntas;
        this.aprovado = aprovado;
        this.pontosConcedidos = pontosConcedidos;
    }

    public int getTentativaId() {
        return tentativaId;
    }

    public double getNota() {
        return nota;
    }

    public int getAcertos() {
        return acertos;
    }

    public int getTotalPerguntas() {
        return totalPerguntas;
    }

    public boolean isAprovado() {
        return aprovado;
    }

    // Pontos que a ds-pontuacao registrou; 0 quando a pontuação não foi concedida.
    public int getPontosConcedidos() {
        return pontosConcedidos;
    }

    public static void anexar(@NonNull Intent rota, @NonNull ResultadoQuiz resultado) {
        rota.putExtra(EXTRA, resultado);
    }

    @Nullable
    public static ResultadoQuiz doIntent(@NonNull Intent rota) {
        Serializable valor = rota.getSerializableExtra(EXTRA);

        return valor instanceof ResultadoQuiz ? (ResultadoQuiz) valor : null;
    }
}
