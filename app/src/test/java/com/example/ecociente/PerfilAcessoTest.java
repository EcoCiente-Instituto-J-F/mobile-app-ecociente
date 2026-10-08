package com.example.ecociente;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.junit.Assert.assertEquals;

import com.example.ecociente.model.PerfilAcesso;
import com.example.ecociente.model.PerfilUsuario;
import org.junit.Test;

public class PerfilAcessoTest {
    @Test
    public void usuarioSemCondominioComEnderecoPodeEntrar() {
        assertTrue(PerfilAcesso.ehUsuarioComum("usuario", false, "Rua das Flores"));
    }

    @Test
    public void usuarioComCondominioNaoPodeEntrar() {
        assertFalse(PerfilAcesso.ehUsuarioComum("usuario", true, "Rua das Flores"));
    }

    @Test
    public void usuarioSemEnderecoNaoPodeEntrar() {
        assertFalse(PerfilAcesso.ehUsuarioComum("usuario", false, "  "));
    }

    @Test
    public void cooperativaNaoPodeEntrar() {
        assertFalse(PerfilAcesso.ehUsuarioComum("cooperativa", false, "Rua das Flores"));
    }

    private PerfilUsuario perfil(String tipo, boolean possuiCondominio, String endereco) {
        PerfilUsuario perfil = new PerfilUsuario();
        perfil.setTipoPerfil(tipo);
        perfil.setPossuiCodigoCondominio(possuiCondominio);
        perfil.setEndereco(endereco);
        return perfil;
    }

    @Test
    public void usuarioComCondominioVaiParaHomeDoCondominio() {
        assertEquals(
                PerfilAcesso.DestinoHome.CONDOMINIO,
                PerfilAcesso.destinoDaHome(perfil("usuario", true, "")));
    }

    @Test
    public void cooperativaVaiParaHomeDaCooperativa() {
        assertEquals(
                PerfilAcesso.DestinoHome.COOPERATIVA,
                PerfilAcesso.destinoDaHome(perfil("cooperativa", false, "Rua das Flores")));
    }

    @Test
    public void usuarioSemCondominioVaiParaHomeComum() {
        assertEquals(
                PerfilAcesso.DestinoHome.USUARIO_COMUM,
                PerfilAcesso.destinoDaHome(perfil("usuario", false, "Rua das Flores")));
    }

    @Test
    public void perfilSemTipoNaoTemAcesso() {
        assertEquals(
                PerfilAcesso.DestinoHome.SEM_ACESSO,
                PerfilAcesso.destinoDaHome(perfil("", false, "")));
    }
}
