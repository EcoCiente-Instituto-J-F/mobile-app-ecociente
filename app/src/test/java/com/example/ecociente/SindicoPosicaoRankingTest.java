package com.example.ecociente;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.example.ecociente.model.SindicoPosicaoRanking;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class SindicoPosicaoRankingTest {

    private static List<SindicoPosicaoRanking> posicoes() {
        return Arrays.asList(
                new SindicoPosicaoRanking("TA", "Torre A", 1980, false),
                new SindicoPosicaoRanking("TB", "Torre B", 1500, false));
    }

    @Test
    public void convertePosicoesEmVetoresNaMesmaOrdem() {
        assertArrayEquals(new String[] {"Torre A", "Torre B"}, SindicoPosicaoRanking.nomes(posicoes()));
        assertArrayEquals(new String[] {"TA", "TB"}, SindicoPosicaoRanking.iniciais(posicoes()));
        assertArrayEquals(new int[] {1980, 1500}, SindicoPosicaoRanking.pontos(posicoes()));
    }

    @Test
    public void listaVaziaGeraVetoresVazios() {
        assertEquals(0, SindicoPosicaoRanking.nomes(Collections.emptyList()).length);
        assertEquals(0, SindicoPosicaoRanking.pontos(Collections.emptyList()).length);
    }
}
