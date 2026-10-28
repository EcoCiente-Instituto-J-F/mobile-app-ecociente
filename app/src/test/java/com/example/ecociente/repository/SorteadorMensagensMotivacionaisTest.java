package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

public class SorteadorMensagensMotivacionaisTest {

    @Test
    public void sorteiaAsVinteFrasesUnicas() {
        SorteadorMensagensMotivacionais sorteador =
                new SorteadorMensagensMotivacionais(new Random(20261028L));
        Set<String> frases = new HashSet<>();

        for (int tentativa = 0; tentativa < 2000; tentativa++) {
            String mensagem = sorteador.sortear();
            assertFalse(mensagem.isBlank());
            frases.add(mensagem);
        }

        assertEquals(20, frases.size());
    }

    @Test
    public void naoRepeteTextoEmSorteiosConsecutivos() {
        SorteadorMensagensMotivacionais sorteador =
                new SorteadorMensagensMotivacionais(new Random(42L));

        String anterior = sorteador.sortear();

        for (int tentativa = 0; tentativa < 2000; tentativa++) {
            String atual = sorteador.sortear();
            assertNotEquals(anterior, atual);
            anterior = atual;
        }
    }
}
