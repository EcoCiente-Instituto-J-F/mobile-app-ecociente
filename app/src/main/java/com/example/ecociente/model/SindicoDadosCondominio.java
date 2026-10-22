package com.example.ecociente.model;

/** Dados do condomínio do síndico: vêm de /api/usuarios/me e o endereço volta por PATCH. */
public final class SindicoDadosCondominio {
    private final boolean residencial;
    private final String nome;
    private final String endereco;
    private final String numero;
    private final String complemento;
    private final String cidade;
    private final String estado;
    private final String cep;

    public SindicoDadosCondominio(boolean residencial, String nome, String endereco, String numero,
                                   String complemento, String cidade, String estado, String cep) {
        this.residencial = residencial;
        this.nome = nome;
        this.endereco = endereco;
        this.numero = numero;
        this.complemento = complemento;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
    }

    // Preenche com os valores da API os campos que ela trouxe; os vazios ficam como estavam.
    public SindicoDadosCondominio comDadosDaApi(SindicoDadosCondominio api) {
        return new SindicoDadosCondominio(
                api.residencial,
                preferir(api.nome, nome),
                preferir(api.endereco, endereco),
                preferir(api.numero, numero),
                preferir(api.complemento, complemento),
                preferir(api.cidade, cidade),
                preferir(api.estado, estado),
                preferir(api.cep, cep));
    }

    private static String preferir(String novo, String atual) {
        return novo == null || novo.trim().isEmpty() ? atual : novo;
    }

    public boolean isResidencial() { return residencial; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public String getNumero() { return numero; }
    public String getComplemento() { return complemento; }
    public String getCidade() { return cidade; }
    public String getEstado() { return estado; }
    public String getCep() { return cep; }
}
