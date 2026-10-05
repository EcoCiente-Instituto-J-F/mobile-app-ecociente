package com.example.ecociente.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ResultadoPerfil {
    private final PerfilUsuario perfil;
    private final boolean falhou;

    private ResultadoPerfil(@Nullable PerfilUsuario perfil, boolean falhou) {
        this.perfil = perfil;
        this.falhou = falhou;
    }

    public static ResultadoPerfil encontrado(@NonNull PerfilUsuario perfil) {
        return new ResultadoPerfil(perfil, false);
    }

    public static ResultadoPerfil inexistente() {
        return new ResultadoPerfil(null, false);
    }

    public static ResultadoPerfil falha() {
        return new ResultadoPerfil(null, true);
    }

    public boolean isFalha() {
        return falhou;
    }

    public boolean isInexistente() {
        return !falhou && perfil == null;
    }

    @Nullable
    public PerfilUsuario getPerfil() {
        return perfil;
    }
}
