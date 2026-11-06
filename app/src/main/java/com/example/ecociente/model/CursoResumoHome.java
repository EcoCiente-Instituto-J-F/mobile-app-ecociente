package com.example.ecociente.model;

import androidx.annotation.NonNull;

/** Resumo de um curso ativo apresentado na tela inicial do condomínio. */
public final class CursoResumoHome {
    public final int id;
    @NonNull public final String titulo;
    @NonNull public final String descricao;

    public CursoResumoHome(int id, @NonNull String titulo, @NonNull String descricao) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
    }
}
