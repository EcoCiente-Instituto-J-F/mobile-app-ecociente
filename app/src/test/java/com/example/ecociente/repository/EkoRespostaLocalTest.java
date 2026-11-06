package com.example.ecociente.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EkoRespostaLocalTest {

    @Test
    public void identificaModoOfflineEOrientaReciclagem() {
        String resposta = EkoRespostaLocal.responder("Como reciclar plástico?");
        assertTrue(resposta.contains("modo offline"));
        assertTrue(resposta.contains("coleta seletiva"));
        assertTrue(resposta.contains("não gerada pela IA"));
    }

    @Test
    public void naoInventaEnderecoDeCooperativa() {
        String resposta = EkoRespostaLocal.responder("Qual cooperativa próxima?");
        assertTrue(resposta.contains("Não consigo consultar endereços"));
        assertFalse(resposta.contains("Rua "));
    }

    @Test
    public void orientaDescarteDePilhaSemInventarColeta() {
        String resposta = EkoRespostaLocal.responder("Onde descartar pilhas?");
        assertTrue(resposta.contains("pontos específicos de recebimento"));
    }
}
