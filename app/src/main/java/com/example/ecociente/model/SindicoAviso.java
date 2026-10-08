package com.example.ecociente.model;

public final class SindicoAviso {
    private final String titulo;
    private final String mensagem;
    private final String destinatario;
    private final String autor;
    private final String momento;
    private final String categoria;
    private final boolean rascunho;
    private final boolean novo;

    public SindicoAviso(String titulo, String mensagem, String destinatario,
                        String autor, String momento, String categoria,
                        boolean rascunho, boolean novo) {
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.destinatario = destinatario;
        this.autor = autor;
        this.momento = momento;
        this.categoria = categoria;
        this.rascunho = rascunho;
        this.novo = novo;
    }

    public String getTitulo() { return titulo; }
    public String getMensagem() { return mensagem; }
    public String getDestinatario() { return destinatario; }
    public String getAutor() { return autor; }
    public String getMomento() { return momento; }
    public String getCategoria() { return categoria; }
    public boolean isRascunho() { return rascunho; }
    public boolean isNovo() { return novo; }
}
