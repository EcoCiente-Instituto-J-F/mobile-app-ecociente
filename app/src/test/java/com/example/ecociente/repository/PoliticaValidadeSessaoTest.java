package com.example.ecociente.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PoliticaValidadeSessaoTest {
    @Test
    public void tokenSemRegistroDeEmissaoDeveSerRenovado() {
        assertTrue(PoliticaValidadeSessao.deveRenovar(0, 1_000_000));
    }

    @Test
    public void tokenRecemEmitidoPodeSerReutilizado() {
        assertFalse(PoliticaValidadeSessao.deveRenovar(1_000_000, 1_000_001));
    }

    @Test
    public void tokenRenovaAosQuatroMinutos() {
        long inicio = 1_000_000L;
        assertFalse(PoliticaValidadeSessao.deveRenovar(inicio, inicio + 239_999));
        assertTrue(PoliticaValidadeSessao.deveRenovar(inicio, inicio + 240_000));
    }

    @Test
    public void horarioDoDispositivoRetrocedeuExigeRenovacao() {
        assertTrue(PoliticaValidadeSessao.deveRenovar(1_100_000, 1_000_000));
    }
}
