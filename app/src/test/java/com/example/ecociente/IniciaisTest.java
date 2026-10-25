package com.example.ecociente;

import static org.junit.Assert.assertEquals;

import com.example.ecociente.ui.Iniciais;
import org.junit.Test;

public class IniciaisTest {

    @Test
    public void usaPrimeiraEUltimaPalavra() {
        assertEquals("AS", Iniciais.de("Ana Paula Souza"));
    }

    @Test
    public void nomeUnicoTemUmaLetra() {
        assertEquals("R", Iniciais.de("rafael"));
    }

    @Test
    public void ignoraEspacosExtras() {
        assertEquals("MA", Iniciais.de("  maria    alves  "));
    }

    @Test
    public void textoVazioDevolveVazio() {
        assertEquals("", Iniciais.de(""));
        assertEquals("", Iniciais.de("   "));
    }
}
