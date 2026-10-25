package com.example.ecociente.model;

import androidx.annotation.NonNull;
import java.util.Collections;
import java.util.List;

/** Identificadores reais do backend, necessários para iniciar tentativas no servidor. */
public final class QuizResumo {
    private final int quizId;
    private final String titulo;
    private final String categoria;
    private final int pontos;
    private final List<QuizConteudo.Pergunta> perguntas;

    public QuizResumo(int quizId, @NonNull String titulo, @NonNull String categoria,
                      int pontos, @NonNull List<QuizConteudo.Pergunta> perguntas) {
        this.quizId = quizId;
        this.titulo = titulo;
        this.categoria = categoria;
        this.pontos = pontos;
        this.perguntas = Collections.unmodifiableList(perguntas);
    }

    public int getQuizId() { return quizId; }
    @NonNull public String getTitulo() { return titulo; }
    @NonNull public String getCategoria() { return categoria; }
    public int getPontos() { return pontos; }
    @NonNull public List<QuizConteudo.Pergunta> getPerguntas() { return perguntas; }
}
