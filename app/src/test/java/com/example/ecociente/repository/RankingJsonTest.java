package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.ItemLista;
import com.example.ecociente.model.SindicoPosicaoRanking;
import java.util.List;
import org.json.JSONObject;
import org.junit.Test;

public class RankingJsonTest {

    @Test
    public void torresLeNomeEPontosEMontaIniciais() throws Exception {
        JSONObject resposta =
                new JSONObject(
                        "{\"ranking\":[{\"nome\":\"Torre Azul\",\"pontos\":1200},"
                                + "{\"nome\":\"Bloco B\",\"pontos\":800}]}");

        List<SindicoPosicaoRanking> torres = RankingJson.torres(resposta);

        assertEquals(2, torres.size());
        assertEquals("Torre Azul", torres.get(0).getNome());
        assertEquals("TA", torres.get(0).getIniciais());
        assertEquals(1200, torres.get(0).getPontos());
        assertFalse(torres.get(0).isUsuarioAtual());
    }

    @Test
    public void torresSemRankingOuVazioDevolveListaVazia() throws Exception {
        assertTrue(RankingJson.torres(new JSONObject("{}")).isEmpty());
        assertTrue(RankingJson.torres(new JSONObject("{\"ranking\":[]}")).isEmpty());
    }

    @Test
    public void torresIgnoraItensInvalidosETolerarCamposAusentes() throws Exception {
        JSONObject resposta = new JSONObject("{\"ranking\":[null,{\"pontos\":5}]}");

        List<SindicoPosicaoRanking> torres = RankingJson.torres(resposta);

        assertEquals(1, torres.size());
        assertEquals("", torres.get(0).getNome());
        assertEquals(5, torres.get(0).getPontos());
    }

    @Test
    public void moradoresFormataPosicaoENomeEPontosComSeparadorDeMilhar() throws Exception {
        JSONObject resposta =
                new JSONObject(
                        "{\"ranking\":[{\"posicao\":1,\"nome\":\"Ana Souza\",\"pontos\":1980},"
                                + "{\"posicao\":2,\"nome\":\"Rafael\",\"pontos\":950}]}");

        List<ItemLista> moradores = RankingJson.moradores(resposta);

        assertEquals(2, moradores.size());
        assertEquals("1º  Ana Souza", moradores.get(0).titulo);
        assertEquals("1.980 pts", moradores.get(0).destaque);
        assertEquals("", moradores.get(0).subtitulo);
        assertEquals("950 pts", moradores.get(1).destaque);
    }

    @Test
    public void moradoresSemPosicaoUsaOIndiceDaLista() throws Exception {
        JSONObject resposta = new JSONObject("{\"ranking\":[{\"nome\":\"A\",\"pontos\":1},{\"nome\":\"B\",\"pontos\":0}]}");

        List<ItemLista> moradores = RankingJson.moradores(resposta);

        assertEquals("1º  A", moradores.get(0).titulo);
        assertEquals("2º  B", moradores.get(1).titulo);
        assertEquals("0 pts", moradores.get(1).destaque);
    }

    @Test
    public void moradoresSemRankingDevolveListaVazia() throws Exception {
        assertTrue(RankingJson.moradores(new JSONObject("{\"ciclo\":{}}")).isEmpty());
    }

    @Test
    public void reconheceATorreQueJaEstaNoRankingPeloNome() throws Exception {
        List<SindicoPosicaoRanking> ranking = RankingJson.torres(
                new JSONObject("{\"ranking\":[{\"nome\":\"Torre A\",\"pontos\":40}]}"));

        assertTrue(RankingJson.contem(ranking, " torre a "));
        assertEquals(false, RankingJson.contem(ranking, "Torre B"));
    }

    @Test
    public void torreForaDoRankingEntraComOsPontosDaPosicao() throws Exception {
        SindicoPosicaoRanking torre = RankingJson.torreDaPosicao(
                new JSONObject("{\"torreId\":2,\"posicao\":5,\"pontos\":12,\"totalParticipantes\":9}"), "Torre B");

        assertEquals("Torre B", torre.getNome());
        assertEquals(12, torre.getPontos());
    }
}
