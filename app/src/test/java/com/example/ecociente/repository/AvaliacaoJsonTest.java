package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.AvaliacoesCooperativa;
import com.example.ecociente.model.ItemLista;
import org.json.JSONObject;
import org.junit.Test;

public class AvaliacaoJsonTest {

    @Test
    public void leMediaTotalEAvaliacoes() throws Exception {
        AvaliacoesCooperativa resultado =
                AvaliacaoJson.avaliacoes(
                        new JSONObject(
                                "{\"cooperativaId\":1,\"media\":4.5,\"total\":2,\"avaliacoes\":["
                                        + "{\"id\":10,\"nomeCondominio\":\"Raio de Luz\",\"nota\":5,"
                                        + "\"comentario\":\"Equipe pontual\",\"avaliadoEm\":\"2026-05-12T10:00:00\"},"
                                        + "{\"id\":11,\"nomeCondominio\":\"Vila Verde\",\"nota\":4,\"comentario\":\"\"}]}"));

        assertEquals(4.5, resultado.getMedia(), 0.001);
        assertEquals(2, resultado.getTotal());
        assertEquals(2, resultado.getItens().size());

        ItemLista primeira = resultado.getItens().get(0);

        assertEquals("Raio de Luz", primeira.titulo);
        assertEquals("“Equipe pontual”", primeira.subtitulo);
        assertEquals("5,0", primeira.destaque);
        assertEquals("avaliacao:10", primeira.chave);
    }

    @Test
    public void semComentarioOSubtituloFicaVazio() throws Exception {
        AvaliacoesCooperativa resultado =
                AvaliacaoJson.avaliacoes(
                        new JSONObject("{\"avaliacoes\":[{\"id\":1,\"nomeCondominio\":\"X\",\"nota\":3}]}"));

        assertEquals("", resultado.getItens().get(0).subtitulo);
        assertEquals("3,0", resultado.getItens().get(0).destaque);
    }

    @Test
    public void semAvaliacoesDevolveListaVazia() throws Exception {
        AvaliacoesCooperativa resultado = AvaliacaoJson.avaliacoes(new JSONObject("{\"media\":0,\"total\":0}"));

        assertTrue(resultado.getItens().isEmpty());
        assertEquals(0, resultado.getTotal());
    }
}
