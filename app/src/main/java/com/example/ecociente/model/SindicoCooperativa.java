package com.example.ecociente.model;

/** Dados ilustrativos de uma cooperativa; ainda não representam resultados de uma API. */
public final class SindicoCooperativa {
    private final int id;
    private final String nome;
    private final String distancia;
    private final String avaliacao;
    private final String endereco;
    private final String cidade;
    private final String estado;
    private final String cep;

    public SindicoCooperativa(int id, String nome, String distancia, String avaliacao,
                              String endereco, String cidade, String estado, String cep) {
        this.id = id;
        this.nome = nome;
        this.distancia = distancia;
        this.avaliacao = avaliacao;
        this.endereco = endereco;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getDistancia() { return distancia; }
    public String getAvaliacao() { return avaliacao; }
    public String getEndereco() { return endereco; }
    public String getCidade() { return cidade; }
    public String getEstado() { return estado; }
    public String getCep() { return cep; }
}
