package com.example.ecociente.model;

import java.text.Normalizer;
import java.util.Locale;

/** Cooperativa da mesma cidade do condomínio do síndico (GET /api/cooperativas da ds-mobile-api). */
public final class SindicoCooperativa {
    private final int id;
    private final String nome;
    private final String avaliacao;
    private final int totalAvaliacoes;
    private final String endereco;
    private final String cidade;
    private final String estado;
    private final String cep;
    private final String enderecoCompleto;

    // `avaliacao` é a média já formatada ("4,5"), ou vazio quando ninguém avaliou ainda.
    public SindicoCooperativa(int id, String nome, String avaliacao, int totalAvaliacoes,
                              String endereco, String cidade, String estado, String cep,
                              String enderecoCompleto) {
        this.id = id;
        this.nome = nome;
        this.avaliacao = avaliacao;
        this.totalAvaliacoes = totalAvaliacoes;
        this.endereco = endereco;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
        this.enderecoCompleto = enderecoCompleto;
    }

    // Busca pelo nome, sem diferenciar maiúsculas nem acentos; termo vazio casa com todas.
    public boolean corresponde(String termo) {
        return normalizar(nome).contains(normalizar(termo));
    }

    private static String normalizar(String valor) {
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getAvaliacao() { return avaliacao; }
    public int getTotalAvaliacoes() { return totalAvaliacoes; }
    public String getEndereco() { return endereco; }
    public String getCidade() { return cidade; }
    public String getEstado() { return estado; }
    public String getCep() { return cep; }
    // Endereço em uma linha, pronto para ser o destino no Google Maps.
    public String getEnderecoCompleto() { return enderecoCompleto; }
}
