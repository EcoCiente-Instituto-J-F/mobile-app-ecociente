package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.QuizConteudo;
import com.example.ecociente.model.ResultadoQuiz;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class TentativaQuizJsonTest {

    // Duas perguntas: a 10 tem alternativas 101 e 102; a 20 tem 201 e 202. Sem gabarito ainda.
    private static List<QuizConteudo.Pergunta> perguntas() {
        List<QuizConteudo.Pergunta> lista = new ArrayList<>();

        lista.add(new QuizConteudo.Pergunta("Primeira", new String[] {"A", "B"}, -1, "", 10, new int[] {101, 102}));
        lista.add(new QuizConteudo.Pergunta("Segunda", new String[] {"C", "D"}, -1, "", 20, new int[] {201, 202}));

        return lista;
    }

    @Test
    public void tentativaIdVemDoCampoTentativaId() throws Exception {
        assertEquals(42, TentativaQuizJson.tentativaId(new JSONObject("{\"tentativaId\":42,\"quiz\":{}}")));
        assertEquals(0, TentativaQuizJson.tentativaId(new JSONObject("{}")));
    }

    @Test
    public void corpoDeFinalizarMandaOsIdsDasAlternativasEscolhidas() throws Exception {
        JSONObject corpo = new JSONObject(TentativaQuizJson.corpoFinalizar(perguntas(), new int[] {1, 0}));

        JSONArray respostas = corpo.getJSONArray("respostas");

        assertEquals(2, respostas.length());
        assertEquals(10, respostas.getJSONObject(0).getInt("perguntaId"));
        assertEquals(102, respostas.getJSONObject(0).getInt("alternativaId"));
        assertEquals(20, respostas.getJSONObject(1).getInt("perguntaId"));
        assertEquals(201, respostas.getJSONObject(1).getInt("alternativaId"));
    }

    @Test
    public void perguntasNaoRespondidasNaoVao() throws Exception {
        JSONObject corpo = new JSONObject(TentativaQuizJson.corpoFinalizar(perguntas(), new int[] {-1, 1}));

        JSONArray respostas = corpo.getJSONArray("respostas");

        assertEquals(1, respostas.length());
        assertEquals(20, respostas.getJSONObject(0).getInt("perguntaId"));
        assertEquals(202, respostas.getJSONObject(0).getInt("alternativaId"));
    }

    @Test
    public void semNenhumaRespostaAListaVaiVazia() throws Exception {
        JSONObject corpo = new JSONObject(TentativaQuizJson.corpoFinalizar(perguntas(), new int[] {-1, -1}));

        assertEquals(0, corpo.getJSONArray("respostas").length());
    }

    @Test
    public void perguntaSemIdDaApiNaoVai() throws Exception {
        List<QuizConteudo.Pergunta> lista = new ArrayList<>();
        lista.add(new QuizConteudo.Pergunta("Local", new String[] {"A"}, 0, "", 0, new int[] {0}));

        assertEquals(0, new JSONObject(TentativaQuizJson.corpoFinalizar(lista, new int[] {0})).getJSONArray("respostas").length());
    }

    @Test
    public void resultadoLeNotaAcertosAprovacaoEPontos() throws Exception {
        ResultadoQuiz resultado =
                TentativaQuizJson.resultado(
                        new JSONObject(
                                "{\"tentativaId\":42,\"quizId\":7,\"nota\":50.00,\"totalPerguntas\":2,\"acertos\":1,"
                                        + "\"aprovado\":true,\"pontuacao\":{\"status\":\"CONCEDIDA\",\"pontos\":30}}"));

        assertEquals(42, resultado.getTentativaId());
        assertEquals(50.0, resultado.getNota(), 0.001);
        assertEquals(1, resultado.getAcertos());
        assertEquals(2, resultado.getTotalPerguntas());
        assertTrue(resultado.isAprovado());
        assertEquals(30, resultado.getPontosConcedidos());
    }

    @Test
    public void pontuacaoAusenteOuNulaVale0Pontos() throws Exception {
        assertEquals(0, TentativaQuizJson.resultado(new JSONObject("{\"nota\":100,\"aprovado\":true}")).getPontosConcedidos());
        assertEquals(
                0,
                TentativaQuizJson.resultado(new JSONObject("{\"aprovado\":false,\"pontuacao\":{\"status\":\"NAO_ELEGIVEL\",\"pontos\":null}}"))
                        .getPontosConcedidos());
    }

    @Test
    public void respostaSemCamposNaoQuebra() throws Exception {
        ResultadoQuiz resultado = TentativaQuizJson.resultado(new JSONObject("{}"));

        assertEquals(0, resultado.getAcertos());
        assertFalse(resultado.isAprovado());
    }

    @Test
    public void revisaoAplicaAAlternativaCertaEAExplicacao() throws Exception {
        JSONObject revisao =
                new JSONObject(
                        "{\"perguntas\":["
                                + "{\"perguntaId\":10,\"respostaCorreta\":{\"id\":102,\"texto\":\"B\"},\"explicacao\":\"Porque sim\"},"
                                + "{\"perguntaId\":20,\"respostaCorreta\":{\"id\":201,\"texto\":\"C\"},\"explicacao\":\"Outra razão\"}]}");

        List<QuizConteudo.Pergunta> corrigidas = TentativaQuizJson.comGabarito(revisao, perguntas());

        assertEquals(1, corrigidas.get(0).getIndiceCorreto());
        assertEquals("Porque sim", corrigidas.get(0).getExplicacao());
        assertEquals(0, corrigidas.get(1).getIndiceCorreto());
        assertEquals("Outra razão", corrigidas.get(1).getExplicacao());
        assertTrue(QuizConteudo.temGabarito(corrigidas));
    }

    @Test
    public void revisaoMantemIdsEEnunciado() throws Exception {
        JSONObject revisao =
                new JSONObject("{\"perguntas\":[{\"perguntaId\":10,\"respostaCorreta\":{\"id\":101},\"explicacao\":\"x\"}]}");

        QuizConteudo.Pergunta corrigida = TentativaQuizJson.comGabarito(revisao, perguntas()).get(0);

        assertEquals("Primeira", corrigida.getEnunciado());
        assertEquals(10, corrigida.getId());
        assertEquals(102, corrigida.getIdAlternativa(1));
    }

    @Test
    public void perguntaDaRevisaoQueOAppNaoTemEIgnorada() throws Exception {
        JSONObject revisao =
                new JSONObject("{\"perguntas\":[{\"perguntaId\":99,\"respostaCorreta\":{\"id\":101},\"explicacao\":\"x\"}]}");

        List<QuizConteudo.Pergunta> corrigidas = TentativaQuizJson.comGabarito(revisao, perguntas());

        assertEquals(-1, corrigidas.get(0).getIndiceCorreto());
        assertFalse(QuizConteudo.temGabarito(corrigidas));
    }

    @Test
    public void alternativaCorretaDesconhecidaNaoMudaAPergunta() throws Exception {
        JSONObject revisao =
                new JSONObject("{\"perguntas\":[{\"perguntaId\":10,\"respostaCorreta\":{\"id\":999},\"explicacao\":\"x\"}]}");

        assertEquals(-1, TentativaQuizJson.comGabarito(revisao, perguntas()).get(0).getIndiceCorreto());
    }

    @Test
    public void revisaoSemPerguntasDevolveAsOriginais() throws Exception {
        List<QuizConteudo.Pergunta> corrigidas = TentativaQuizJson.comGabarito(new JSONObject("{}"), perguntas());

        assertEquals(2, corrigidas.size());
        assertEquals(-1, corrigidas.get(1).getIndiceCorreto());
    }
}
