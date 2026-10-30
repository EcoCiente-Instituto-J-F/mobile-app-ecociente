package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;

public class QuizzesConcluidosJsonTest {

    @Test
    public void leOsIdsDosQuizzes() throws Exception {
        Set<Integer> ids = QuizzesConcluidosJson.ids(new JSONArray("[12,18,25]"));

        assertEquals(new HashSet<>(Arrays.asList(12, 18, 25)), ids);
    }

    @Test
    public void idsRepetidosSaoDeduplicados() throws Exception {
        assertEquals(2, QuizzesConcluidosJson.ids(new JSONArray("[12,12,18,12]")).size());
    }

    @Test
    public void listaVaziaNaoFabricaConclusao() throws Exception {
        assertTrue(QuizzesConcluidosJson.ids(new JSONArray("[]")).isEmpty());
    }

    @Test(expected = JSONException.class)
    public void idZeroEhRejeitado() throws Exception {
        QuizzesConcluidosJson.ids(new JSONArray("[12,0]"));
    }

    @Test(expected = JSONException.class)
    public void idNegativoEhRejeitado() throws Exception {
        QuizzesConcluidosJson.ids(new JSONArray("[-3]"));
    }

    @Test(expected = JSONException.class)
    public void valorQueNaoEhNumeroEhRejeitado() throws Exception {
        QuizzesConcluidosJson.ids(new JSONArray("[\"abc\"]"));
    }
}
