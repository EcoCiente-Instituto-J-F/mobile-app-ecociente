package com.example.ecociente.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PoliticaErroLoginTest {
    @Test
    public void recusaSoQuandoAApiDizQueAContaNaoServe() {
        assertTrue(PoliticaErroLogin.credenciaisRecusadas(401, null));
        assertTrue(PoliticaErroLogin.credenciaisRecusadas(400, ""));
        assertTrue(PoliticaErroLogin.credenciaisRecusadas(403, ""));
        assertTrue(PoliticaErroLogin.credenciaisRecusadas(
                404, "{\"codigoError\":\"RECURSO_NAO_ENCONTRADO\"}"));
        assertFalse(PoliticaErroLogin.credenciaisRecusadas(404, "<html>Not Found</html>"));
        assertFalse(PoliticaErroLogin.credenciaisRecusadas(404, null));
        assertFalse(PoliticaErroLogin.credenciaisRecusadas(500, ""));
        assertFalse(PoliticaErroLogin.credenciaisRecusadas(503, ""));
    }
}
