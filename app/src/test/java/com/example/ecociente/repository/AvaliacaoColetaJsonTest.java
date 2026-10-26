package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;

public class AvaliacaoColetaJsonTest {

    @Test
    public void corpoLevaANotaEOComentarioSemEspacosSobrando() throws Exception {
        JSONObject corpo = new JSONObject(AvaliacaoColetaJson.corpo(5, "  Equipe pontual  "));

        assertEquals(5, corpo.getInt("nota"));
        assertEquals("Equipe pontual", corpo.getString("comentario"));
    }

    @Test
    public void comentarioVazioOuEmBrancoNaoVai() throws Exception {
        assertFalse(new JSONObject(AvaliacaoColetaJson.corpo(4, "")).has("comentario"));
        assertFalse(new JSONObject(AvaliacaoColetaJson.corpo(4, "   ")).has("comentario"));
        assertFalse(new JSONObject(AvaliacaoColetaJson.corpo(4, null)).has("comentario"));
    }

    @Test
    public void mensagemDaApiTemPrioridade() {
        assertEquals(
                "Esta coleta já foi avaliada",
                AvaliacaoColetaJson.mensagemDoErro(
                        409, "{\"status\":409,\"codigo\":\"AVALIACAO_JA_REGISTRADA\",\"mensagem\":\"Esta coleta já foi avaliada\"}"));
    }

    @Test
    public void semCorpoLegivelValeOCodigoHttp() {
        assertTrue(AvaliacaoColetaJson.mensagemDoErro(403, "").contains("síndico"));
        assertTrue(AvaliacaoColetaJson.mensagemDoErro(404, "não é json").contains("não foi encontrada"));
        assertTrue(AvaliacaoColetaJson.mensagemDoErro(401, null).contains("sessão expirou"));
        assertTrue(AvaliacaoColetaJson.mensagemDoErro(0, null).contains("Tente de novo"));
    }
}
