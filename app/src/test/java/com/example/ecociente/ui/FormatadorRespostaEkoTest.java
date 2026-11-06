package com.example.ecociente.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.json.JSONObject;
import org.junit.Test;

public class FormatadorRespostaEkoTest {

    @Test
    public void exibeTextoNaturalSemDescartarJsonOriginal() throws Exception {
        String original = "{\"dominio\":\"educador\",\"resposta\":\"Não encontrei na base.\","
                + "\"recomendacao\":\"Procure a cooperativa.\","
                + "\"esclarecer\":\"Qual material?\","
                + "\"categoria\":\"nao_confirmada\",\"curso_id\":null,"
                + "\"evidencias\":[\"FAQ EcoCiente | chunk 8\"]}";

        assertEquals("Não encontrei na base.\n\nProcure a cooperativa.\n\nQual material?",
                FormatadorRespostaEko.paraExibicao(original));
        assertEquals("nao_confirmada", new JSONObject(original).getString("categoria"));
        assertFalse(FormatadorRespostaEko.paraExibicao(original).contains("evidencias"));
    }

    @Test
    public void preservaRespostaSimplesEJsonMalformado() {
        String texto = "Olá! Como posso ajudar?";
        String invalido = "{\"resposta\":nao_valido}";
        assertEquals(texto, FormatadorRespostaEko.paraExibicao(texto));
        assertEquals(invalido, FormatadorRespostaEko.paraExibicao(invalido));
        assertEquals("", FormatadorRespostaEko.paraExibicao(null));
    }

    @Test
    public void naoAlteraDominiosDesconhecidosOuCamposInvalidos() {
        String analytics = "{\"dominio\":\"analytics\",\"resposta\":\"Dados internos\"}";
        String semTexto = "{\"dominio\":\"faq\",\"resposta\":10}";
        assertEquals(analytics, FormatadorRespostaEko.paraExibicao(analytics));
        assertEquals(semTexto, FormatadorRespostaEko.paraExibicao(semTexto));
    }

    @Test
    public void faqApresentaRespostaSemRecomendacaoVazia() {
        String json = "{\"dominio\":\"faq\",\"resposta\":\"Separe o vidro.\",\"recomendacao\":\"\"}";
        assertEquals("Separe o vidro.", FormatadorRespostaEko.paraExibicao(json));
    }
}
