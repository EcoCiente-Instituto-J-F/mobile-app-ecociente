package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.model.StatusAgendamento;
import org.json.JSONObject;
import org.junit.Test;

public class CalendarioJsonTest {

    @Test
    public void leAgendamentoDaListagem() throws Exception {
        Solicitacao solicitacao = CalendarioJson.solicitacao(new JSONObject(
                "{\"id\":42,\"condominioId\":3,\"cooperativaId\":4,\"dataInicio\":\"2026-10-23T12:30:00\","
                        + "\"dataFim\":null,\"statusAgendamento\":\"CONFIRMADO\",\"possuiRecorrencia\":true}"));

        assertEquals(42, solicitacao.getId());
        assertEquals(3, solicitacao.getCondominioId());
        assertEquals(4, solicitacao.getCooperativaId());
        assertEquals("2026-10-23T12:30:00", solicitacao.getDataInicio());
        assertNull(solicitacao.getDataFim());
        assertEquals(StatusAgendamento.CONFIRMADO, solicitacao.getStatus());
        assertTrue(solicitacao.possuiRecorrencia());
    }

    @Test
    public void leARespostaDaCriacaoQueUsaStatus() throws Exception {
        Solicitacao solicitacao = CalendarioJson.solicitacao(new JSONObject(
                "{\"id\":7,\"condominioId\":3,\"cooperativaId\":4,\"status\":\"AGENDADO\","
                        + "\"dataInicio\":\"2026-10-23T12:30:00-03:00\",\"possuiRecorrencia\":false}"));

        assertEquals(7, solicitacao.getId());
        assertEquals(StatusAgendamento.AGENDADO, solicitacao.getStatus());
        assertFalse(solicitacao.possuiRecorrencia());
    }

    @Test
    public void comNomeCondominioMantemOsDemaisDados() throws Exception {
        Solicitacao solicitacao = CalendarioJson.solicitacao(new JSONObject(
                "{\"id\":7,\"condominioId\":3,\"cooperativaId\":4,\"status\":\"AGENDADO\"}"))
                .comNomeCondominio("Residencial Eco");

        assertEquals("Residencial Eco", solicitacao.getNomeCondominio());
        assertEquals(7, solicitacao.getId());
        assertEquals(StatusAgendamento.AGENDADO, solicitacao.getStatus());
    }

    @Test
    public void leOrigemENomesQuandoAApiInforma() throws Exception {
        Solicitacao solicitacao = CalendarioJson.solicitacao(new JSONObject(
                "{\"id\":42,\"condominioId\":3,\"cooperativaId\":4,\"statusAgendamento\":\"AGENDADO\","
                        + "\"origemSolicitacao\":\"COOPERATIVA\",\"solicitadoPorUsuarioId\":20,"
                        + "\"cooperativaNome\":\"Coop Sol\",\"condominioNome\":\"Residencial Eco\"}"));

        assertEquals("COOPERATIVA", solicitacao.getOrigemSolicitacao());
        assertEquals("Coop Sol", solicitacao.getCooperativaNome());
        assertEquals("Residencial Eco", solicitacao.getNomeCondominio());
        assertFalse(solicitacao.pediuPeloCondominio());
    }

    @Test
    public void pedidoDoCondominioNaoPodeSerRespondidoPeloSindico() throws Exception {
        Solicitacao solicitacao = CalendarioJson.solicitacao(new JSONObject(
                "{\"id\":42,\"statusAgendamento\":\"AGENDADO\",\"origemSolicitacao\":\"CONDOMINIO\"}"));

        assertTrue(solicitacao.pediuPeloCondominio());
    }

    @Test
    public void semOrigemNaoSeSabeQuemPediu() throws Exception {
        Solicitacao solicitacao = CalendarioJson.solicitacao(new JSONObject(
                "{\"id\":42,\"statusAgendamento\":\"AGENDADO\",\"origemSolicitacao\":null,\"cooperativaNome\":null}"));

        assertNull(solicitacao.getOrigemSolicitacao());
        assertNull(solicitacao.getCooperativaNome());
        assertFalse(solicitacao.pediuPeloCondominio());
    }

    @Test
    public void origemEPedidoSobrevivemAMudancaDeStatusENome() throws Exception {
        Solicitacao solicitacao = CalendarioJson.solicitacao(new JSONObject(
                "{\"id\":42,\"statusAgendamento\":\"AGENDADO\",\"origemSolicitacao\":\"CONDOMINIO\","
                        + "\"cooperativaNome\":\"Coop Sol\"}"))
                .comStatus(StatusAgendamento.CANCELADO)
                .comNomeCondominio("Residencial Eco");

        assertEquals("CONDOMINIO", solicitacao.getOrigemSolicitacao());
        assertEquals("Coop Sol", solicitacao.getCooperativaNome());
    }
}
