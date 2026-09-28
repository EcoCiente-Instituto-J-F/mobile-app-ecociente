package com.example.ecociente.model;

public class MensagemChat {

    public enum Autor {
        USUARIO,
        EKO
    }

    public enum Tipo {
        TEXTO,
        ERRO_CONEXAO
    }

    private final Autor autor;
    private final Tipo tipo;
    private final String texto;

    private MensagemChat(Autor autor, Tipo tipo, String texto) {
        this.autor = autor;
        this.tipo = tipo;
        this.texto = texto;
    }

    public static MensagemChat doUsuario(String texto) {
        return new MensagemChat(Autor.USUARIO, Tipo.TEXTO, texto);
    }

    public static MensagemChat doEko(String texto) {
        return new MensagemChat(Autor.EKO, Tipo.TEXTO, texto);
    }

    public static MensagemChat erroDeConexao() {
        return new MensagemChat(Autor.EKO, Tipo.ERRO_CONEXAO, "");
    }

    public Autor getAutor() {
        return autor;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getTexto() {
        return texto;
    }
}
