package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.json.JSONObject;
import org.junit.Test;

public class PerfilApiJsonTest {

    private static JSONObject perfil(String condominios) throws Exception {
        return new JSONObject("{\"id\":9,\"nome\":\"Teste\",\"condominios\":" + condominios + "}");
    }

    @Test
    public void sindicoFicaComOCondominioEmQueEResponsavel() throws Exception {
        JSONObject json =
                perfil(
                        "[{\"id\":3,\"nome\":\"Outro\",\"ativo\":true,\"sindicoResponsavel\":false,"
                                + "\"vinculo\":{\"aprovado\":true}},"
                                + "{\"id\":1,\"nome\":\"Residencial Eco\",\"ativo\":true,\"sindicoResponsavel\":true}]");

        PerfilApiJson.Condominio condominio = PerfilApiJson.condominio(json);

        assertNotNull(condominio);
        assertEquals(1, condominio.id);
        assertEquals("Residencial Eco", condominio.nome);
    }

    @Test
    public void moradorFicaComOPrimeiroVinculoAprovado() throws Exception {
        JSONObject json =
                perfil(
                        "[{\"id\":5,\"nome\":\"Pendente\",\"ativo\":true,\"vinculo\":{\"aprovado\":false}},"
                                + "{\"id\":7,\"nome\":\"Aprovado\",\"ativo\":true,\"vinculo\":{\"aprovado\":true}}]");

        assertEquals(7, PerfilApiJson.condominio(json).id);
    }

    @Test
    public void vinculoNaoAprovadoNaoServe() throws Exception {
        JSONObject json = perfil("[{\"id\":5,\"nome\":\"Pendente\",\"ativo\":true,\"vinculo\":{\"aprovado\":false}}]");

        assertNull(PerfilApiJson.condominio(json));
    }

    @Test
    public void condominioInativoOuSemIdNaoServe() throws Exception {
        JSONObject json =
                perfil(
                        "[{\"id\":4,\"nome\":\"Inativo\",\"ativo\":false,\"sindicoResponsavel\":true},"
                                + "{\"nome\":\"Sem id\",\"ativo\":true,\"sindicoResponsavel\":true}]");

        assertNull(PerfilApiJson.condominio(json));
    }

    @Test
    public void semCondominiosDevolveNull() throws Exception {
        assertNull(PerfilApiJson.condominio(perfil("[]")));
        assertNull(PerfilApiJson.condominio(new JSONObject("{\"id\":9}")));
    }

    @Test
    public void nomeAusenteViraTextoVazio() throws Exception {
        JSONObject json = perfil("[{\"id\":2,\"ativo\":true,\"sindicoResponsavel\":true}]");

        assertEquals("", PerfilApiJson.condominio(json).nome);
    }

    @Test
    public void cooperativaIdVemDoObjetoCooperativa() throws Exception {
        assertEquals(5, PerfilApiJson.cooperativaId(new JSONObject("{\"cooperativa\":{\"id\":5,\"nome\":\"Eco\"}}")));
        assertEquals(0, PerfilApiJson.cooperativaId(new JSONObject("{\"cooperativa\":null}")));
        assertEquals(0, PerfilApiJson.cooperativaId(new JSONObject("{}")));
    }
}
