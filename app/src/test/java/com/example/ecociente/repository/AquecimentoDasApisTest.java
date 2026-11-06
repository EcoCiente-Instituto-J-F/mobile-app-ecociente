package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
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
    public void aquecimentoEvitaDuplicacaoQuandoPontuacaoUsaMobile() {
        assertEquals(1, AquecimentoDasApis.servidoresDo("COOPERATIVA").size());
        int esperado = ConfiguracaoServidores.MOBILE_API.equals(ConfiguracaoServidores.PONTUACAO_CONSULTAS_API) ? 1 : 2;
        assertEquals(esperado, AquecimentoDasApis.servidoresDo("MORADOR").size());
    }
}
