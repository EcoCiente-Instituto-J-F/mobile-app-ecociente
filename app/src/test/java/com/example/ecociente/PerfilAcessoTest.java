package com.example.ecociente;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.PerfilAcesso;
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
}
