package com.example.ecociente.model;

/** Unidade ilustrativa vinculada a um bloco; mantida apenas em memória. */
public final class SindicoApartamento {
    private final int id;
    private final int blocoId;
    private final String numero;
    private final String morador;
    private final String telefone;

    public SindicoApartamento(int id, int blocoId, String numero, String morador,
                              String telefone) {
        this.id = id;
        this.blocoId = blocoId;
        this.numero = numero;
        this.morador = morador;
        this.telefone = telefone;
    }

    public int getId() { return id; }
    public int getBlocoId() { return blocoId; }
    public String getNumero() { return numero; }
    public String getMorador() { return morador; }
    public String getTelefone() { return telefone; }
    public boolean isVinculado() { return !morador.isEmpty(); }
}
