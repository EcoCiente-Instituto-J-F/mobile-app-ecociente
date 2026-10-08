package com.example.ecociente.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.Serializable;

public final class ItemLista implements Serializable {

    public final String titulo;
    public final String subtitulo;
    public final String destaque;
    @Nullable public final String[][] detalhes;

    public ItemLista(
            @NonNull String titulo,
            @NonNull String subtitulo,
            @NonNull String destaque,
            @Nullable String[][] detalhes) {
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.destaque = destaque;
        this.detalhes = detalhes;
    }

    public ItemLista(@NonNull String titulo, @NonNull String subtitulo, @NonNull String destaque) {
        this(titulo, subtitulo, destaque, null);
    }
}
