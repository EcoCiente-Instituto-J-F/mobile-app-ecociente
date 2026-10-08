package com.example.ecociente.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class PerfilAcesso {

    public enum DestinoHome {
        USUARIO_COMUM,
        CONDOMINIO,
        COOPERATIVA,
        SEM_ACESSO
    }

    private PerfilAcesso() {}

    public static boolean ehUsuarioComum(
            @Nullable String tipoPerfil,
            @Nullable Boolean possuiCodigoCondominio,
            @Nullable String endereco) {
        return TipoPerfil.USUARIO.equalsIgnoreCase(textoSeguro(tipoPerfil))
                && !Boolean.TRUE.equals(possuiCodigoCondominio)
                && !textoSeguro(endereco).isEmpty();
    }

    @NonNull
    public static DestinoHome destinoDaHome(@NonNull PerfilUsuario perfil) {

        if (ehCooperativa(perfil.getTipoPerfil())) {
            return DestinoHome.COOPERATIVA;
        }

        if (ehUsuarioComum(
                perfil.getTipoPerfil(), perfil.isPossuiCodigoCondominio(), perfil.getEndereco())) {
            return DestinoHome.USUARIO_COMUM;
        }

        return TipoPerfil.USUARIO.equalsIgnoreCase(textoSeguro(perfil.getTipoPerfil()))
                        && perfil.isPossuiCodigoCondominio()
                ? DestinoHome.CONDOMINIO
                : DestinoHome.SEM_ACESSO;
    }

    public static boolean ehCooperativa(@Nullable String tipoPerfil) {
        return TipoPerfil.COOPERATIVA.equalsIgnoreCase(textoSeguro(tipoPerfil));
    }

    private static String textoSeguro(@Nullable String valor) {
        return valor == null ? "" : valor.trim();
    }
}
