package com.example.ecociente.model;

/** Entrada ilustrativa do ranking, sem vínculo com dados reais. */
public final class SindicoPosicaoRanking {
    private final String iniciais;
    private final String nome;
    private final int pontos;
    private final boolean usuarioAtual;

    public SindicoPosicaoRanking(String iniciais, String nome, int pontos,
                                 boolean usuarioAtual) {
        this.iniciais = iniciais;
        this.nome = nome;
        this.pontos = pontos;
        this.usuarioAtual = usuarioAtual;
    }

    public String getIniciais() { return iniciais; }
    public String getNome() { return nome; }
    public int getPontos() { return pontos; }
    public boolean isUsuarioAtual() { return usuarioAtual; }
}
