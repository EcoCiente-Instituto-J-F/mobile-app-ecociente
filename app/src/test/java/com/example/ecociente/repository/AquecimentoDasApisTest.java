package com.example.ecociente.repository;

import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class AquecimentoDasApisTest {

    @Test
    public void todoPerfilAcordaAAutenticacaoEOCadastro() {
        for (String perfil : new String[] {null, "SINDICO", "COOPERATIVA", "MORADOR", "USUARIO"}) {
            List<String> servidores = AquecimentoDasApis.servidoresDo(perfil);

            assertTrue(perfil, servidores.contains("https://ds-autenticacao-api-1.onrender.com/"));
            assertTrue(perfil, servidores.contains("https://ds-cadastro-api.onrender.com/"));
        }
    }
}
