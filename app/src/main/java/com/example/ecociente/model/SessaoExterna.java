package com.example.ecociente.model;

import androidx.annotation.NonNull;

// Resposta de POST /auth/login da API de autenticação (ds-autenticacao-api).
public final class SessaoExterna {
    private final String token;
    private final String tipoToken;
    private final long expiraEmSegundos;
    private final int usuarioId;
    private final String nome;
    private final String email;
    private final String perfil;

    public SessaoExterna(
            @NonNull String token,
            @NonNull String tipoToken,
            long expiraEmSegundos,
            int usuarioId,
            @NonNull String nome,
            @NonNull String email,
            @NonNull String perfil) {
        this.token = token;
        this.tipoToken = tipoToken;
        this.expiraEmSegundos = expiraEmSegundos;
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
    }

    @NonNull
    public String getToken() {
        return token;
    }

    @NonNull
    public String getTipoToken() {
        return tipoToken;
    }

    public long getExpiraEmSegundos() {
        return expiraEmSegundos;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    @NonNull
    public String getNome() {
        return nome;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    @NonNull
    public String getPerfil() {
        return perfil;
    }
}
