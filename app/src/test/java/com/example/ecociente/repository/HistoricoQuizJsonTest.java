package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.DesempenhoQuiz;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;

public class HistoricoQuizJsonTest {

    private static final String TENTATIVA =
            "{\"tentativaId\":123,\"quizId\":45,\"titulo\":\" Reciclagem \",\"nota\":87.5,"
                    + "\"aprovado\":true,\"concluidoEm\":\"2026-10-30T12:34:56Z\"}";

    @Test
    public void preservaNotaDecimalDataETentativa() throws Exception {
        List<DesempenhoQuiz> lista = HistoricoQuizJson.desempenhos(new JSONArray("[" + TENTATIVA + "]"));

        assertEquals(1, lista.size());
        assertEquals(123, lista.get(0).tentativaId);
        assertEquals("Reciclagem", lista.get(0).titulo);
        assertEquals(87.5, lista.get(0).nota, 0.0);
        assertEquals("2026-10-30T12:34:56Z", lista.get(0).concluidoEm);
    }

    @Test
    public void listaVaziaNaoFabricaResultado() throws Exception {
        assertTrue(HistoricoQuizJson.desempenhos(new JSONArray("[]")).isEmpty());
    }

    @Test
    public void notaZeroValidaContinuaZero() throws Exception {
        List<DesempenhoQuiz> lista = HistoricoQuizJson.desempenhos(
                new JSONArray("[" + TENTATIVA.replace("87.5", "0") + "]"));

        assertEquals(0.0, lista.get(0).nota, 0.0);
    }

    @Test(expected = JSONException.class)
    public void notaAusenteEhRejeitada() throws Exception {
        HistoricoQuizJson.desempenhos(new JSONArray("[" + TENTATIVA.replace("\"nota\":87.5,", "") + "]"));
    }

    @Test(expected = JSONException.class)
    public void notaNulaEhRejeitada() throws Exception {
        HistoricoQuizJson.desempenhos(new JSONArray("[" + TENTATIVA.replace("87.5", "null") + "]"));
    }

    @Test(expected = JSONException.class)
    public void notaForaDaEscalaEhRejeitada() throws Exception {
        HistoricoQuizJson.desempenhos(new JSONArray("[" + TENTATIVA.replace("87.5", "100.5") + "]"));
    }

    @Test(expected = JSONException.class)
    public void notaNegativaEhRejeitada() throws Exception {
        HistoricoQuizJson.desempenhos(new JSONArray("[" + TENTATIVA.replace("87.5", "-1") + "]"));
    }

    @Test(expected = JSONException.class)
    public void dataEmBrancoEhRejeitada() throws Exception {
        HistoricoQuizJson.desempenhos(new JSONArray("[" + TENTATIVA.replace("2026-10-30T12:34:56Z", " ") + "]"));
    }

    @Test(expected = JSONException.class)
    public void tentativaComIdInvalidoEhRejeitada() throws Exception {
        HistoricoQuizJson.desempenhos(new JSONArray("[" + TENTATIVA.replace("\"tentativaId\":123", "\"tentativaId\":0") + "]"));
    }

    @Test(expected = JSONException.class)
    public void itemQueNaoEhObjetoEhRejeitado() throws Exception {
        HistoricoQuizJson.desempenhos(new JSONArray("[5]"));
    }
}
