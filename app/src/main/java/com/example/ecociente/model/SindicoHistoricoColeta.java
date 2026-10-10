package com.example.ecociente.model;

/** Registro ilustrativo de coleta concluída, sem origem em banco ou API. */
public final class SindicoHistoricoColeta {
    private final int id;
    private final String cooperativa;
    private final String data;
    private final String avaliacaoPublica;

    public SindicoHistoricoColeta(int id, String cooperativa, String data,
                                  String avaliacaoPublica) {
        this.id = id;
        this.cooperativa = cooperativa;
        this.data = data;
        this.avaliacaoPublica = avaliacaoPublica;
    }

    public int getId() { return id; }
    public String getCooperativa() { return cooperativa; }
    public String getData() { return data; }
    public String getAvaliacaoPublica() { return avaliacaoPublica; }
}
