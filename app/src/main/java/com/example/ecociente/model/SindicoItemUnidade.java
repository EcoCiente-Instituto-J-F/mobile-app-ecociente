package com.example.ecociente.model;

/** Bloco/torre ou área de descarte exibido na demonstração local. */
public final class SindicoItemUnidade {
    private final int id;
    private final String nome;
    private final String detalhe;

    public SindicoItemUnidade(int id, String nome, String detalhe) {
        this.id = id;
        this.nome = nome;
        this.detalhe = detalhe;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getDetalhe() { return detalhe; }
}
