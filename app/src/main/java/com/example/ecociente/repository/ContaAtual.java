package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.model.PreferenciasNotificacao;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.model.ResultadoLogin;
import com.example.ecociente.model.UsuarioLogado;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// Ponto único para saber quem está logado. Em produção não há provedor e tudo segue no Firebase; uma build que registra um provedor assume a sessão no lugar dele.
public final class ContaAtual {

    public interface Provedor {
        @Nullable
        UsuarioLogado usuario();

        @NonNull
        ResultadoLogin entrar(@NonNull String email, @NonNull String senha);

        void sair();

        @Nullable
        PerfilUsuario perfil(@NonNull String uid);

        @NonNull
        ResultadoApi atualizarCampo(@NonNull String uid, @NonNull String campo, @NonNull Object valor);

        @NonNull
        PreferenciasNotificacao preferencias(@NonNull String uid);

        @NonNull
        ResultadoApi atualizarPreferencia(@NonNull String uid, @NonNull String chave, boolean valor);

        @NonNull
        ResultadoApi alterarSenha(@NonNull String senhaAtual, @NonNull String novaSenha);

        @NonNull
        ResultadoApi excluirConta(@NonNull String senhaAtual);
    }

    private static volatile Provedor provedor;

    private ContaAtual() {}

    public static void usar(@Nullable Provedor novoProvedor) {
        provedor = novoProvedor;
    }

    @Nullable
    public static Provedor substituto() {
        return provedor;
    }

    @Nullable
    public static UsuarioLogado usuario() {
        Provedor substituto = provedor;

        if (substituto != null) {
            return substituto.usuario();
        }

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            return null;
        }

        return new UsuarioLogado(usuario.getUid(), usuario.getEmail(), usuario.getDisplayName());
    }
}
