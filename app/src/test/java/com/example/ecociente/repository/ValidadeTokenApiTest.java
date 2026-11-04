package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ValidadeTokenApiTest {

    private static final long AGORA = 1_800_000_000_000L;

    @Test
    public void sessaoAntigaSemVencimentoPrecisaSerRenovada() {
        assertTrue(ValidadeTokenApi.deveRenovar(0L, AGORA));
    }

    @Test
    public void tokenVencidoOuPertoDoVencimentoPrecisaSerRenovado() {
        assertTrue(ValidadeTokenApi.deveRenovar(AGORA - 1L, AGORA));
        assertTrue(ValidadeTokenApi.deveRenovar(AGORA + 60_000L, AGORA));
        assertTrue(ValidadeTokenApi.deveRenovar(AGORA, AGORA));
    }

    @Test
    public void tokenVigentePodeSerUtilizado() {
        assertFalse(ValidadeTokenApi.deveRenovar(AGORA + 61_000L, AGORA));
        assertFalse(ValidadeTokenApi.deveRenovar(AGORA + 300_000L, AGORA));
    }

    @Test
    public void vencimentoDaApiUsaDuracaoEmSegundos() {
        assertEquals(AGORA + 300_000L, ValidadeTokenApi.calcularVencimento(AGORA, 300L));
    }

    @Test
    public void vencimentoAusenteOuInvalidoExigeRenovacao() {
        assertEquals(0L, ValidadeTokenApi.calcularVencimento(AGORA, 0L));
        assertEquals(0L, ValidadeTokenApi.calcularVencimento(AGORA, -10L));
        assertEquals(0L, ValidadeTokenApi.calcularVencimento(AGORA, Long.MAX_VALUE));
        assertEquals(0L, ValidadeTokenApi.calcularVencimento(-1L, 300L));
    }
}
