package com.example.ecociente.model;

import androidx.annotation.Nullable;

public final class PerfilAcesso {
    private static final String TIPO_USUARIO = "usuario";

    private PerfilAcesso() {}

    public static boolean ehUsuarioComum(
            @Nullable String tipoPerfil,
            @Nullable Boolean possuiCodigoCondominio,
            @Nullable String endereco) {
        return TIPO_USUARIO.equalsIgnoreCase(textoSeguro(tipoPerfil))
                && !Boolean.TRUE.equals(possuiCodigoCondominio)
                && !textoSeguro(endereco).isEmpty();
    }

    private static String textoSeguro(@Nullable String valor) {
        return valor == null ? "" : valor.trim();
    }
}
