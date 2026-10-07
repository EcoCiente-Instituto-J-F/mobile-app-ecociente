package com.example.ecociente.model;

import androidx.annotation.NonNull;

public final class EstadoHome {

    public enum Tipo {
        SEM_LOGIN,
        CARREGANDO,
        ERRO_REDE,
        CADASTRO_INCOMPLETO,
        SEM_ACESSO,
        COOPERATIVA,
        CONDOMINIO,
        PRONTA
    }

    private final Tipo tipo;
    private final String nome;

    private EstadoHome(@NonNull Tipo tipo, @NonNull String nome) {
        this.tipo = tipo;
        this.nome = nome;
    }

    public static EstadoHome de(@NonNull Tipo tipo) {
        return new EstadoHome(tipo, "");
    }

    public static EstadoHome pronta(@NonNull String nome) {
        return new EstadoHome(Tipo.PRONTA, nome);
    }

    @NonNull
    public Tipo getTipo() {
        return tipo;
    }

    @NonNull
    public String getNome() {
        return nome;
    }
}
