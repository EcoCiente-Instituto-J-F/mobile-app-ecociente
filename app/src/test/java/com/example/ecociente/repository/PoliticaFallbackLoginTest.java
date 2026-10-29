package com.example.ecociente.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLHandshakeException;
import org.junit.Test;

public class PoliticaFallbackLoginTest {
    @Test
    public void fazFallbackSomenteEmIndisponibilidadeHttp() {
        assertTrue(PoliticaFallbackLogin.tentarRenderAposHttp(502));
        assertTrue(PoliticaFallbackLogin.tentarRenderAposHttp(503));
        assertTrue(PoliticaFallbackLogin.tentarRenderAposHttp(504));
        assertFalse(PoliticaFallbackLogin.tentarRenderAposHttp(400));
        assertFalse(PoliticaFallbackLogin.tentarRenderAposHttp(401));
        assertFalse(PoliticaFallbackLogin.tentarRenderAposHttp(403));
        assertFalse(PoliticaFallbackLogin.tentarRenderAposHttp(404));
        assertFalse(PoliticaFallbackLogin.tentarRenderAposHttp(500));
    }

    @Test
    public void recusaSoQuandoAApiDizQueAContaNaoServe() {
        assertTrue(PoliticaFallbackLogin.credenciaisRecusadas(401, null));
        assertTrue(PoliticaFallbackLogin.credenciaisRecusadas(400, ""));
        assertTrue(PoliticaFallbackLogin.credenciaisRecusadas(403, ""));
        assertTrue(PoliticaFallbackLogin.credenciaisRecusadas(
                404, "{\"codigoError\":\"RECURSO_NAO_ENCONTRADO\"}"));
        assertFalse(PoliticaFallbackLogin.credenciaisRecusadas(404, "<html>Not Found</html>"));
        assertFalse(PoliticaFallbackLogin.credenciaisRecusadas(404, null));
        assertFalse(PoliticaFallbackLogin.credenciaisRecusadas(500, ""));
        assertFalse(PoliticaFallbackLogin.credenciaisRecusadas(503, ""));
    }

    @Test
    public void fazFallbackEmFalhaDeRedeMasNaoEmErroDeCertificado() {
        assertTrue(PoliticaFallbackLogin.tentarRenderAposErro(new ConnectException()));
        assertTrue(PoliticaFallbackLogin.tentarRenderAposErro(new SocketTimeoutException()));
        assertTrue(PoliticaFallbackLogin.tentarRenderAposErro(new UnknownHostException()));
        assertFalse(PoliticaFallbackLogin.tentarRenderAposErro(new SSLHandshakeException("certificado")));
        assertFalse(PoliticaFallbackLogin.tentarRenderAposErro(new IOException("erro generico")));
    }
}
