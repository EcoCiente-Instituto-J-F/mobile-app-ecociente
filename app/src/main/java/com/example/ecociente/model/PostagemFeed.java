package com.example.ecociente.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// Uma foto de descarte no feed do condomínio.
public final class PostagemFeed {

    private final int id;
    private final String autor;
    private final String fotoUrl;
    private final String categoria;
    private final String status;
    private final String quando;

    public PostagemFeed(
            int id,
            @NonNull String autor,
            @NonNull String fotoUrl,
            @Nullable String categoria,
            @Nullable String status,
            @NonNull String quando) {
        this.id = id;
        this.autor = autor;
        this.fotoUrl = fotoUrl;
        this.categoria = categoria == null ? "" : categoria;
        this.status = status == null ? "" : status;
        this.quando = quando;
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getAutor() {
        return autor;
    }

    @NonNull
    public String getFotoUrl() {
        return fotoUrl;
    }

    @NonNull
    public String getCategoria() {
        return categoria;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    @NonNull
    public String getQuando() {
        return quando;
    }
}
