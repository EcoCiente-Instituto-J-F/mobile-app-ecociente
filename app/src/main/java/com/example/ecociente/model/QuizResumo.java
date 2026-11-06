package com.example.ecociente.model;

import androidx.annotation.NonNull;
import java.util.List;

// Um quiz da ds-mobile-api com as perguntas já carregadas.
public final class QuizResumo {

    private final String titulo;
    private final String categoria;
    private final int pontos;
    private final List<QuizConteudo.Pergunta> perguntas;
    private final int id;
    private final double notaMinimaAprovacao;

    public QuizResumo(
            @NonNull String titulo,
            @NonNull String categoria,
            int pontos,
            @NonNull List<QuizConteudo.Pergunta> perguntas,
            int id,
            double notaMinimaAprovacao) {
        this.titulo = titulo;
        this.categoria = categoria;
        this.pontos = pontos;
        this.perguntas = perguntas;
        this.id = id;
        this.notaMinimaAprovacao = notaMinimaAprovacao;
    }

    // Mesmo construtor usado pelo catálogo demonstrativo local.
    public QuizResumo(int quizId, @NonNull String titulo, @NonNull String categoria,
                      int pontos, @NonNull List<QuizConteudo.Pergunta> perguntas) {
        this(titulo, categoria, pontos, perguntas, quizId, 0d);
    }

    public int getQuizId() {
        return id;
    }

    @NonNull
    public String getTitulo() {
        return titulo;
    }

    @NonNull
    public String getCategoria() {
        return categoria;
    }

    public int getPontos() {
        return pontos;
    }

    // Id do quiz na ds-mobile-api, para enviar as respostas quando a API tiver onde recebê-las.
    public int getId() {
        return id;
    }

    public double getNotaMinimaAprovacao() {
        return notaMinimaAprovacao;
    }

    @NonNull
    public List<QuizConteudo.Pergunta> getPerguntas() {
        return perguntas;
    }
}
