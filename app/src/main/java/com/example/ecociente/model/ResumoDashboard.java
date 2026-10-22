package com.example.ecociente.model;

import androidx.annotation.NonNull;

/** Números do dashboard do síndico, calculados a partir das postagens e das coletas do condomínio. */
public final class ResumoDashboard {
    private final int descartes;
    private final int moradores;
    private final int coletasRealizadas;
    private final int[] descartesPorSemana;
    private final String[] materiais;
    private final int[] percentuais;
    private final String[] cooperativas;
    private final int[] coletasPorCooperativa;

    public ResumoDashboard(int descartes, int moradores, int coletasRealizadas,
                           @NonNull int[] descartesPorSemana, @NonNull String[] materiais,
                           @NonNull int[] percentuais, @NonNull String[] cooperativas,
                           @NonNull int[] coletasPorCooperativa) {
        this.descartes = descartes;
        this.moradores = moradores;
        this.coletasRealizadas = coletasRealizadas;
        this.descartesPorSemana = descartesPorSemana;
        this.materiais = materiais;
        this.percentuais = percentuais;
        this.cooperativas = cooperativas;
        this.coletasPorCooperativa = coletasPorCooperativa;
    }

    // Descartes dos últimos 30 dias que não foram reprovados.
    public int getDescartes() { return descartes; }
    // Moradores diferentes que descartaram nesse período.
    public int getMoradores() { return moradores; }
    public int getColetasRealizadas() { return coletasRealizadas; }
    // Quatro semanas, da mais antiga para a atual.
    public int[] getDescartesPorSemana() { return descartesPorSemana; }
    public String[] getMateriais() { return materiais; }
    // Mesma ordem de `materiais`; somam 100 quando há algum descarte.
    public int[] getPercentuais() { return percentuais; }
    public String[] getCooperativas() { return cooperativas; }
    public int[] getColetasPorCooperativa() { return coletasPorCooperativa; }
}
