package com.example.ecociente.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class AquecimentoDasApisTest {

    @Test
    public void todoPerfilAcordaOServicoUnificado() {
        for (String perfil : new String[] {null, "SINDICO", "COOPERATIVA", "MORADOR", "USUARIO"}) {
            List<String> servidores = AquecimentoDasApis.servidoresDo(perfil);

            assertTrue(perfil, servidores.contains(ConfiguracaoServidores.MOBILE_API + "/"));
        }
    }

    @Test
    public void cooperativaNaoAcordaAPontuacao() {
        assertFalse(AquecimentoDasApis.servidoresDo("COOPERATIVA").contains(ConfiguracaoServidores.PONTUACAO_API + "/"));
        assertTrue(AquecimentoDasApis.servidoresDo("MORADOR").contains(ConfiguracaoServidores.PONTUACAO_API + "/"));
    }
}
