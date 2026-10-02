package com.example.ecociente.model;

/** Dados locais ilustrativos da aba Unidades. */
public final class SindicoDadosCondominio {
    private final boolean residencial;
    private final String nome;
    private final String endereco;
    private final String cidade;
    private final String estado;
    private final String cep;

    public SindicoDadosCondominio(boolean residencial, String nome, String endereco,
                                   String cidade, String estado, String cep) {
        this.residencial = residencial;
        this.nome = nome;
        this.endereco = endereco;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
    }

    public boolean isResidencial() { return residencial; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public String getCidade() { return cidade; }
    public String getEstado() { return estado; }
    public String getCep() { return cep; }
}
