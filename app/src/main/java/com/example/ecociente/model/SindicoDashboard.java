package com.example.ecociente.model;

public final class SindicoDashboard {
    private final String nome;
    private final String condominio;
    private final int participacao;
    private final int descartes;
    private final int adesao;
    private final int variacaoParticipacao;
    private final int variacaoDescartes;
    private final int variacaoAdesao;
    private final int[] evolucao;
    private final String[] datasEvolucao;
    private final String[] blocosRanking;
    private final String[] iniciaisRanking;
    private final int[] pontosRanking;

    public SindicoDashboard(String nome, String condominio, int participacao,
                            int descartes, int adesao, int variacaoParticipacao,
                            int variacaoDescartes, int variacaoAdesao, int[] evolucao,
                            String[] datasEvolucao,
                            String[] blocosRanking, String[] iniciaisRanking,
                            int[] pontosRanking) {
        if (evolucao.length != datasEvolucao.length
                || blocosRanking.length != iniciaisRanking.length
                || blocosRanking.length != pontosRanking.length) {
            throw new IllegalArgumentException("Dados de dashboard inconsistentes");
        }
        this.nome = nome;
        this.condominio = condominio;
        this.participacao = participacao;
        this.descartes = descartes;
        this.adesao = adesao;
        this.variacaoParticipacao = variacaoParticipacao;
        this.variacaoDescartes = variacaoDescartes;
        this.variacaoAdesao = variacaoAdesao;
        this.evolucao = evolucao.clone();
        this.datasEvolucao = datasEvolucao.clone();
        this.blocosRanking = blocosRanking.clone();
        this.iniciaisRanking = iniciaisRanking.clone();
        this.pontosRanking = pontosRanking.clone();
    }

    // Sem API de dashboard ainda: tudo zerado e sem listas.
    public static SindicoDashboard vazio() {
        return new SindicoDashboard("", "", 0, 0, 0, 0, 0, 0,
                new int[0], new String[0], new String[0], new String[0], new int[0]);
    }

    public boolean semDados() {
        return participacao == 0 && descartes == 0 && adesao == 0
                && evolucao.length == 0 && blocosRanking.length == 0;
    }

    public String getNome() { return nome; }
    public String getCondominio() { return condominio; }
    public int getParticipacao() { return participacao; }
    public int getDescartes() { return descartes; }
    public int getAdesao() { return adesao; }
    public int getVariacaoParticipacao() { return variacaoParticipacao; }
    public int getVariacaoDescartes() { return variacaoDescartes; }
    public int getVariacaoAdesao() { return variacaoAdesao; }
    public int[] getEvolucao() { return evolucao.clone(); }
    public String[] getDatasEvolucao() { return datasEvolucao.clone(); }
    public String[] getBlocosRanking() { return blocosRanking.clone(); }
    public String[] getIniciaisRanking() { return iniciaisRanking.clone(); }
    public int[] getPontosRanking() { return pontosRanking.clone(); }
}
