package com.example.ecociente;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.TipoDocumento;
import org.junit.Test;

public class TipoDocumentoTest {

    @Test
    public void cnpjValidoComOuSemMascara() {
        assertTrue(TipoDocumento.CNPJ.valido("54.934.758/0001-26"));
        assertTrue(TipoDocumento.CNPJ.valido("54934758000126"));
    }

    @Test
    public void cnpjInvalido() {
        assertFalse(TipoDocumento.CNPJ.valido("54.934.758/0001-27"));
        assertFalse(TipoDocumento.CNPJ.valido("11111111111111"));
        assertFalse(TipoDocumento.CNPJ.valido("5493475800012"));
        assertFalse(TipoDocumento.CNPJ.valido(""));
        assertFalse(TipoDocumento.CNPJ.valido(null));
    }

    @Test
    public void cpfValido() {
        assertTrue(TipoDocumento.CPF.valido("529.982.247-25"));
        assertTrue(TipoDocumento.CPF.valido("52998224725"));
    }

    @Test
    public void cpfInvalido() {
        assertFalse(TipoDocumento.CPF.valido("529.982.247-24"));
        assertFalse(TipoDocumento.CPF.valido("111.111.111-11"));
        assertFalse(TipoDocumento.CPF.valido("5299822472"));
    }

    @Test
    public void apenasDigitos() {
        assertEquals("12345", TipoDocumento.apenasDigitos("1.2-3/4 5"));
        assertEquals("", TipoDocumento.apenasDigitos(null));
    }
}
