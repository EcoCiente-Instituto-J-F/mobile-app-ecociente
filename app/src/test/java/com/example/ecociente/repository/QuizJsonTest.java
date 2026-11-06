package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.QuizConteudo;
import com.example.ecociente.model.QuizResumo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class QuizJsonTest {

    private static final String QUIZ_SEM_GABARITO =
            "{\"id\":7,\"titulo\":\"Vidro\",\"notaMinimaAprovacao\":70.5,\"pontosRecompensa\":30,"
                    + "\"perguntas\":["
                    + "{\"id\":2,\"enunciado\":\"Segunda\",\"ordem\":2,\"alternativas\":[{\"id\":21,\"texto\":\"A\"},{\"id\":22,\"texto\":\"B\"}]},"
                    + "{\"id\":1,\"enunciado\":\"Primeira\",\"ordem\":1,\"alternativas\":[{\"id\":11,\"texto\":\"X\"},{\"id\":12,\"texto\":\"Y\"}]}"
                    + "]}";

    @Test
    public void ordenaPerguntasPeloCampoOrdem() throws Exception {
        QuizResumo quiz = QuizJson.montar("Reciclagem", "Aula 1", new JSONObject(QUIZ_SEM_GABARITO));

        assertEquals("Primeira", quiz.getPerguntas().get(0).getEnunciado());
        assertEquals("Segunda", quiz.getPerguntas().get(1).getEnunciado());
    }

    @Test
    public void guardaIdsEDadosDoQuiz() throws Exception {
        QuizResumo quiz = QuizJson.montar("Reciclagem", "Aula 1", new JSONObject(QUIZ_SEM_GABARITO));

        assertEquals(7, quiz.getId());
        assertEquals(30, quiz.getPontos());
        assertEquals(70.5, quiz.getNotaMinimaAprovacao(), 0.0001);
        assertEquals("Reciclagem", quiz.getCategoria());

        QuizConteudo.Pergunta primeira = quiz.getPerguntas().get(0);

        assertEquals(1, primeira.getId());
        assertEquals(12, primeira.getIdAlternativa(1));
        assertEquals(0, primeira.getIdAlternativa(5));
        assertEquals(0, primeira.getIdAlternativa(-1));
    }

    @Test
    public void semCampoCorretaOQuizFicaSemGabarito() throws Exception {
        QuizResumo quiz = QuizJson.montar("c", "a", new JSONObject(QUIZ_SEM_GABARITO));

        assertEquals(-1, quiz.getPerguntas().get(0).getIndiceCorreto());
        assertFalse(QuizConteudo.temGabarito(quiz.getPerguntas()));
    }

    @Test
    public void leAlternativaCorretaEAExplicacaoQuandoVierem() throws Exception {
        JSONObject json =
                new JSONObject(
                        "{\"perguntas\":[{\"enunciado\":\"P\",\"explicacao\":\"Porque sim\","
                                + "\"alternativas\":[{\"texto\":\"A\"},{\"texto\":\"B\",\"correta\":true}]}]}");

        QuizResumo quiz = QuizJson.montar("c", "a", json);

        assertEquals(1, quiz.getPerguntas().get(0).getIndiceCorreto());
        assertEquals("Porque sim", quiz.getPerguntas().get(0).getExplicacao());
        assertTrue(QuizConteudo.temGabarito(quiz.getPerguntas()));
    }

    @Test
    public void semTituloUsaOTituloDaAula() throws Exception {
        QuizResumo quiz = QuizJson.montar("c", "Aula de Papel", new JSONObject("{\"perguntas\":[]}"));

        assertEquals("Aula de Papel", quiz.getTitulo());
        assertTrue(quiz.getPerguntas().isEmpty());
    }

    @Test
    public void quizSemPerguntasNaoTemGabarito() {
        assertFalse(QuizConteudo.temGabarito(java.util.Collections.emptyList()));
    }

    @Test
    public void ordenarPorOrdemMantemAOrdemQuandoNaoHaCampo() throws Exception {
        JSONArray itens = new JSONArray("[{\"n\":\"a\"},{\"n\":\"b\"},{\"n\":\"c\"}]");

        JSONArray ordenado = QuizJson.ordenarPorOrdem(itens);

        assertEquals("a", ordenado.getJSONObject(0).getString("n"));
        assertEquals("c", ordenado.getJSONObject(2).getString("n"));
    }

    @Test
    public void ordenarPorOrdemColocaOMenorPrimeiro() throws Exception {
        JSONArray itens = new JSONArray("[{\"ordem\":3,\"n\":\"c\"},{\"ordem\":1,\"n\":\"a\"},{\"ordem\":2,\"n\":\"b\"}]");

        JSONArray ordenado = QuizJson.ordenarPorOrdem(itens);

        assertEquals("a", ordenado.getJSONObject(0).getString("n"));
        assertEquals("b", ordenado.getJSONObject(1).getString("n"));
        assertEquals("c", ordenado.getJSONObject(2).getString("n"));
    }
}
