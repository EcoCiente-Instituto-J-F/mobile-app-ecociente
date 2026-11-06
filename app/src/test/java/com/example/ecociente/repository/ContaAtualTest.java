package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.model.PreferenciasNotificacao;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.model.ResultadoLogin;
import com.example.ecociente.model.UsuarioLogado;
import org.junit.After;
import org.junit.Test;

public class ContaAtualTest {

    private static final UsuarioLogado USUARIO = new UsuarioLogado("u", "m@x.y", "Nome");

    @After
    public void limpar() {
        ContaAtual.usar(null);
    }

    @Test
    public void usuario_comProvedor_devolveODoProvedor() {
        ContaAtual.usar(new ProvedorFalso());

        assertSame(USUARIO, ContaAtual.usuario());
        assertNotNull(ContaAtual.substituto());
    }

    @Test
    public void usar_null_removeOProvedor() {
        ContaAtual.usar(new ProvedorFalso());
        ContaAtual.usar(null);

        assertNull(ContaAtual.substituto());
    }

    @Test
    public void nomeExibicao_semNomeNoPerfil_usaODoUsuario() {
        assertEquals("Nome", PerfilUsuario.nomeExibicao(new PerfilUsuario(), USUARIO));
    }

    @Test
    public void emailExibicao_semEmailNoPerfil_usaODoUsuario() {
        assertEquals("m@x.y", PerfilUsuario.emailExibicao(new PerfilUsuario(), USUARIO));
    }

    private static final class ProvedorFalso implements ContaAtual.Provedor {
        @Nullable
        @Override
        public UsuarioLogado usuario() {
            return USUARIO;
        }

        @NonNull
        @Override
        public ResultadoLogin entrar(@NonNull String email, @NonNull String senha) {
            return ResultadoLogin.sucesso();
        }

        @Override
        public void sair() {}

        @Nullable
        @Override
        public PerfilUsuario perfil(@NonNull String uid) {
            return null;
        }

        @NonNull
        @Override
        public ResultadoApi atualizarCampo(
                @NonNull String uid, @NonNull String campo, @NonNull Object valor) {
            return ResultadoApi.sucesso();
        }

        @NonNull
        @Override
        public PreferenciasNotificacao preferencias(@NonNull String uid) {
            return new PreferenciasNotificacao();
        }

        @NonNull
        @Override
        public ResultadoApi atualizarPreferencia(
                @NonNull String uid, @NonNull String chave, boolean valor) {
            return ResultadoApi.sucesso();
        }

        @NonNull
        @Override
        public ResultadoApi alterarSenha(@NonNull String senhaAtual, @NonNull String novaSenha) {
            return ResultadoApi.sucesso();
        }

        @NonNull
        @Override
        public ResultadoApi excluirConta(@NonNull String senhaAtual) {
            return ResultadoApi.sucesso();
        }
    }
}
