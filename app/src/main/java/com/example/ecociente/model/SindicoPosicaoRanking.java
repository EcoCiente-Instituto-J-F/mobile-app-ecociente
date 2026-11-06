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

    public static String[] nomes(java.util.List<SindicoPosicaoRanking> posicoes) {
        String[] nomes = new String[posicoes.size()];
        for (int i = 0; i < nomes.length; i++) nomes[i] = posicoes.get(i).getNome();
        return nomes;
    }

    public static String[] iniciais(java.util.List<SindicoPosicaoRanking> posicoes) {
        String[] iniciais = new String[posicoes.size()];
        for (int i = 0; i < iniciais.length; i++) iniciais[i] = posicoes.get(i).getIniciais();
        return iniciais;
    }

    public static int[] pontos(java.util.List<SindicoPosicaoRanking> posicoes) {
        int[] pontos = new int[posicoes.size()];
        for (int i = 0; i < pontos.length; i++) pontos[i] = posicoes.get(i).getPontos();
        return pontos;
    }

    public String getIniciais() { return iniciais; }
    public String getNome() { return nome; }
    public int getPontos() { return pontos; }
    public boolean isUsuarioAtual() { return usuarioAtual; }
}
