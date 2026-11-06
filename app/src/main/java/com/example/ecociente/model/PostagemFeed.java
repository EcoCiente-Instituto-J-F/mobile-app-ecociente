package com.example.ecociente.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// Uma foto de descarte no feed do condomínio.
public final class PostagemFeed {

    private final int id;
    private final int usuarioId;
    private final long dataLimiteAnaliseMs;
    private final boolean resolvida;
    private final boolean prazoConhecido;
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
        this(id, 0, autor, fotoUrl, categoria, status, quando, 0L, false, false);
    }

    public PostagemFeed(
            int id,
            int usuarioId,
            @NonNull String autor,
            @NonNull String fotoUrl,
            @Nullable String categoria,
            @Nullable String status,
            @NonNull String quando,
            long dataLimiteAnaliseMs,
            boolean resolvida,
            boolean prazoConhecido) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.dataLimiteAnaliseMs = dataLimiteAnaliseMs;
        this.resolvida = resolvida;
        this.prazoConhecido = prazoConhecido;
        this.autor = autor;
        this.fotoUrl = fotoUrl;
        this.categoria = categoria == null ? "" : categoria;
        this.status = status == null ? "" : status;
        this.quando = quando;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public boolean possuiPrazo() {
        return prazoConhecido;
    }

    public boolean estaResolvida() {
        return resolvida;
    }

    public boolean votacaoAberta(long agoraMs) {
        return prazoConhecido && !resolvida && dataLimiteAnaliseMs > agoraMs;
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
