package com.example.ecociente;

import static org.junit.Assert.assertEquals;

import com.example.ecociente.ui.MascaraTexto;
import org.junit.Test;

public class MascaraTextoTest {

    @Test
    public void cpf() {
        MascaraTexto m = new MascaraTexto(null, "###.###.###-##");
        assertEquals("", m.formatar(""));
        assertEquals("123", m.formatar("123"));
        assertEquals("123.4", m.formatar("1234"));
        assertEquals("123.456.789-01", m.formatar("12345678901"));
        assertEquals("123.456.789-01", m.formatar("123456789012345"));
    }

    @Test
    public void cnpj() {
        MascaraTexto m = new MascaraTexto(null, "##.###.###/####-##");
        assertEquals("54.934.758/0001-26", m.formatar("54934758000126"));
        assertEquals("54.9", m.formatar("549"));
    }

    @Test
    public void telefone() {
        MascaraTexto m = new MascaraTexto(null, "(##) #####-####");
        assertEquals("(1", m.formatar("1"));
        assertEquals("(11) 9", m.formatar("119"));
        assertEquals("(11) 98765-4321", m.formatar("11987654321"));
    }

    @Test
    public void cepEData() {
        assertEquals("12345-678", new MascaraTexto(null, "#####-###").formatar("12345678"));
        assertEquals("12/03/2000", new MascaraTexto(null, "##/##/####").formatar("12032000"));
    }

    @Test
    public void reformatarTextoJaFormatadoEhEstavel() {
        MascaraTexto m = new MascaraTexto(null, "###.###.###-##");
        assertEquals("123.456.789-01", m.formatar("123.456.789-01"));
        assertEquals("123.456", m.formatar("123.456."));
    }
}
