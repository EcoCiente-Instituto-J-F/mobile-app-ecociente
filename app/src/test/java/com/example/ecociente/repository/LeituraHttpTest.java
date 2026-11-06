package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class LeituraHttpTest {

    @Test
    public void fluxoNuloViraTextoVazio() throws IOException {
        assertEquals("", LeituraHttp.texto(null));
    }

    @Test
    public void leTextoUtf8ComAcentos() throws IOException {
        byte[] bytes = "{\"mensagem\":\"coleta já confirmada\"}".getBytes(StandardCharsets.UTF_8);

        assertEquals("{\"mensagem\":\"coleta já confirmada\"}", LeituraHttp.texto(new ByteArrayInputStream(bytes)));
    }

    @Test
    public void leCorpoMaiorQueOBuffer() throws IOException {
        StringBuilder grande = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            grande.append("ç").append(i % 10);
        }

        String lido = LeituraHttp.texto(new ByteArrayInputStream(grande.toString().getBytes(StandardCharsets.UTF_8)));

        assertEquals(grande.toString(), lido);
    }

    @Test
    public void fechaOFluxoDepoisDeLer() throws IOException {
        boolean[] fechado = {false};
        InputStream fluxo =
                new ByteArrayInputStream("ok".getBytes(StandardCharsets.UTF_8)) {
                    @Override
                    public void close() throws IOException {
                        fechado[0] = true;
                        super.close();
                    }
                };

        LeituraHttp.texto(fluxo);

        assertTrue(fechado[0]);
    }
}
