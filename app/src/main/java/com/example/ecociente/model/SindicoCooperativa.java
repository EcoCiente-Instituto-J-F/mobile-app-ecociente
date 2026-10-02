package com.example.ecociente.model;

/** Dados ilustrativos de uma cooperativa; ainda não representam resultados de uma API. */
public final class SindicoCooperativa {
    private final int id;
    private final String nome;
    private final String distancia;
    private final String avaliacao;

    public SindicoCooperativa(int id, String nome, String distancia, String avaliacao) {
        this.id = id;
        this.nome = nome;
        this.distancia = distancia;
        this.avaliacao = avaliacao;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getDistancia() { return distancia; }
    public String getAvaliacao() { return avaliacao; }
}
