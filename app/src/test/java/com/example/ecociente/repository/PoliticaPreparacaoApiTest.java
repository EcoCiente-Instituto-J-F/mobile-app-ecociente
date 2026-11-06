package com.example.ecociente.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PoliticaPreparacaoApiTest {
    @Test
    public void credenciaisSoEstaoProntasComTokenEIdValido() {
        assertFalse(PoliticaPreparacaoApi.credenciaisProntas(null, 11));
        assertFalse(PoliticaPreparacaoApi.credenciaisProntas("", 11));
        assertFalse(PoliticaPreparacaoApi.credenciaisProntas("token", 0));
        assertTrue(PoliticaPreparacaoApi.credenciaisProntas("token", 11));
    }

    @Test
    public void reintentosSaoLimitados() {
        assertFalse(PoliticaPreparacaoApi.deveRepetir(0, 3));
        assertTrue(PoliticaPreparacaoApi.deveRepetir(1, 3));
        assertTrue(PoliticaPreparacaoApi.deveRepetir(2, 3));
        assertFalse(PoliticaPreparacaoApi.deveRepetir(3, 3));
        assertFalse(PoliticaPreparacaoApi.deveRepetir(4, 3));
    }
}
