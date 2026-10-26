package com.example.ecociente.model;

import androidx.annotation.NonNull;
import java.util.Collections;
import java.util.List;

/** Dados reais de cursos e aulas retornados pela ds-mobile-api. */
public final class CursoEducativo {
    public final int id;
    @NonNull public final String titulo;
    @NonNull public final String descricao;
    @NonNull public final List<Aula> aulas;

    public CursoEducativo(int id, @NonNull String titulo, @NonNull String descricao,
                         @NonNull List<Aula> aulas) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.aulas = Collections.unmodifiableList(aulas);
    }

    public static final class Aula {
        public final int id;
        @NonNull public final String titulo;
        @NonNull public final String conteudo;
        public final int ordem;

        public Aula(int id, @NonNull String titulo, @NonNull String conteudo, int ordem) {
            this.id = id;
            this.titulo = titulo;
            this.conteudo = conteudo;
            this.ordem = ordem;
        }
    }
}
