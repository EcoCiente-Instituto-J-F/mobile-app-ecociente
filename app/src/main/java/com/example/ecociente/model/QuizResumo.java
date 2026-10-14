package com.example.ecociente.model;

import androidx.annotation.NonNull;
import java.util.List;

// Um quiz da ds-mobile-api com as perguntas já carregadas.
public final class QuizResumo {

    private final String titulo;
    private final String categoria;
    private final int pontos;
    private final List<QuizConteudo.Pergunta> perguntas;

    public QuizResumo(
            @NonNull String titulo,
            @NonNull String categoria,
            int pontos,
            @NonNull List<QuizConteudo.Pergunta> perguntas) {
        this.titulo = titulo;
        this.categoria = categoria;
        this.pontos = pontos;
        this.perguntas = perguntas;
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

    @NonNull
    public List<QuizConteudo.Pergunta> getPerguntas() {
        return perguntas;
    }
}
