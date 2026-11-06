package com.example.ecociente.model;

import androidx.annotation.NonNull;

/**
 * Números do dashboard do síndico. Materiais, moradores, pontos e quizzes vêm de
 * GET /api/condominios/{id}/dashboard; as coletas por cooperativa, do calendário.
 */
public final class ResumoDashboard {
    private final long reciclados;
    private final long moradores;
    private final long pontosDoMes;
    private final int coletasRealizadas;
    private final String[] quizzes;
    private final int[] notasDosQuizzes;
    private final String[] materiais;
    private final int[] percentuais;
    private final String[] cooperativas;
    private final int[] coletasPorCooperativa;

    public ResumoDashboard(long reciclados, long moradores, long pontosDoMes, int coletasRealizadas,
                           @NonNull String[] quizzes, @NonNull int[] notasDosQuizzes,
                           @NonNull String[] materiais, @NonNull int[] percentuais,
                           @NonNull String[] cooperativas, @NonNull int[] coletasPorCooperativa) {
        this.reciclados = reciclados;
        this.moradores = moradores;
        this.pontosDoMes = pontosDoMes;
        this.coletasRealizadas = coletasRealizadas;
        this.quizzes = quizzes;
        this.notasDosQuizzes = notasDosQuizzes;
        this.materiais = materiais;
        this.percentuais = percentuais;
        this.cooperativas = cooperativas;
        this.coletasPorCooperativa = coletasPorCooperativa;
    }

    // Postagens aprovadas e já resolvidas.
    public long getReciclados() { return reciclados; }
    public long getMoradores() { return moradores; }
    public long getPontosDoMes() { return pontosDoMes; }
    public int getColetasRealizadas() { return coletasRealizadas; }
    public String[] getQuizzes() { return quizzes; }
    // Nota média de cada quiz, de 0 a 100, na mesma ordem de `quizzes`.
    public int[] getNotasDosQuizzes() { return notasDosQuizzes; }
    public String[] getMateriais() { return materiais; }
    // Mesma ordem de `materiais`; somam 100 quando há algum material.
    public int[] getPercentuais() { return percentuais; }
    public String[] getCooperativas() { return cooperativas; }
    public int[] getColetasPorCooperativa() { return coletasPorCooperativa; }
}
