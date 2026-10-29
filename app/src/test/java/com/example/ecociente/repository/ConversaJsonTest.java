package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.Conversa;
import com.example.ecociente.model.MensagemConversa;
import java.util.List;
import org.json.JSONObject;
import org.junit.Test;

public class ConversaJsonTest {

    private static final String PAGINA_DE_CONVERSAS =
            "{\"total\":3,\"pagina\":0,\"tamanho\":20,\"totalPaginas\":1,\"itens\":["
                    + "{\"id\":7,\"condominioId\":100,\"cooperativaId\":200,"
                    + "\"nomeCondominio\":\"Residencial Eco\",\"nomeCooperativa\":\"Cooperativa Verde\","
                    + "\"criadaEm\":\"2026-10-01T10:00:00-03:00\",\"ultimaMensagemEm\":\"2026-10-20T14:30:00-03:00\","
                    + "\"ativa\":true,\"naoLidas\":3},"
                    + "{\"id\":0,\"nomeCondominio\":\"Sem id\"},"
                    + "{\"id\":9,\"condominioId\":101,\"cooperativaId\":200,"
                    + "\"nomeCondominio\":\"Condomínio Dois\",\"nomeCooperativa\":\"Cooperativa Verde\","
                    + "\"ultimaMensagemEm\":null,\"ativa\":false,\"naoLidas\":0}]}";

    @Test
    public void quemEDoCondominioVeOsNomesDasCooperativas() throws Exception {
        List<Conversa> conversas = ConversaJson.conversas(new JSONObject(PAGINA_DE_CONVERSAS), true);

        assertEquals(2, conversas.size());
        assertEquals("Cooperativa Verde", conversas.get(0).nome);
        assertEquals(7, conversas.get(0).id);
        assertEquals(100, conversas.get(0).condominioId);
        assertEquals(200, conversas.get(0).cooperativaId);
    }

    @Test
    public void quemEDaCooperativaVeOsNomesDosCondominios() throws Exception {
        List<Conversa> conversas = ConversaJson.conversas(new JSONObject(PAGINA_DE_CONVERSAS), false);

        assertEquals("Residencial Eco", conversas.get(0).nome);
        assertEquals("Condomínio Dois", conversas.get(1).nome);
    }

    @Test
    public void leNaoLidasArquivadaEDataDaUltimaMensagem() throws Exception {
        List<Conversa> conversas = ConversaJson.conversas(new JSONObject(PAGINA_DE_CONVERSAS), false);

        assertEquals(3, conversas.get(0).naoLidas);
        assertTrue(conversas.get(0).temNaoLidas());
        assertFalse(conversas.get(0).arquivada);
        assertEquals("2026-10-20T14:30:00-03:00", conversas.get(0).ultimaMensagemEm);

        assertTrue(conversas.get(1).arquivada);
        assertFalse(conversas.get(1).temNaoLidas());
        assertEquals("", conversas.get(1).ultimaMensagemEm);
    }

    @Test
    public void paginaSemItensOuSemIdsValidosDevolveListaVazia() throws Exception {
        assertTrue(ConversaJson.conversas(new JSONObject("{}"), true).isEmpty());
        assertTrue(ConversaJson.conversas(new JSONObject("{\"itens\":[null,{\"id\":0}]}"), true).isEmpty());
    }

    private static final String PAGINA_DE_MENSAGENS =
            "{\"total\":3,\"itens\":["
                    + "{\"id\":30,\"conversaId\":7,\"remetenteUsuarioId\":5,\"remetenteTipo\":\"CONDOMINIO\","
                    + "\"conteudo\":\"Combinado, obrigado.\",\"enviadaEm\":\"2026-10-20T14:30:00-03:00\"},"
                    + "{\"id\":20,\"conversaId\":7,\"remetenteUsuarioId\":4,\"remetenteTipo\":\"COOPERATIVA\","
                    + "\"conteudo\":\"Passamos amanhã às 10h.\",\"enviadaEm\":\"2026-10-20T09:05:00-03:00\"},"
                    + "{\"id\":10,\"conversaId\":7,\"remetenteUsuarioId\":4,\"remetenteTipo\":\"COOPERATIVA\","
                    + "\"conteudo\":\"Olá! Podemos agendar a coleta?\",\"enviadaEm\":\"2026-10-19T16:00:00-03:00\"}]}";

    @Test
    public void mensagensSaemDaMaisAntigaParaAMaisNova() throws Exception {
        List<MensagemConversa> mensagens =
                ConversaJson.mensagens(new JSONObject(PAGINA_DE_MENSAGENS), ConversaJson.LADO_CONDOMINIO);

        assertEquals(3, mensagens.size());
        assertEquals("Olá! Podemos agendar a coleta?", mensagens.get(0).texto);
        assertEquals("Passamos amanhã às 10h.", mensagens.get(1).texto);
        assertEquals("Combinado, obrigado.", mensagens.get(2).texto);
    }

    @Test
    public void mensagensDoMeuLadoSaoAsEnviadas() throws Exception {
        List<MensagemConversa> comoCondominio =
                ConversaJson.mensagens(new JSONObject(PAGINA_DE_MENSAGENS), ConversaJson.LADO_CONDOMINIO);
        List<MensagemConversa> comoCooperativa =
                ConversaJson.mensagens(new JSONObject(PAGINA_DE_MENSAGENS), ConversaJson.LADO_COOPERATIVA);

        assertFalse(comoCondominio.get(0).enviada);
        assertTrue(comoCondominio.get(2).enviada);

        assertTrue(comoCooperativa.get(0).enviada);
        assertFalse(comoCooperativa.get(2).enviada);
    }

    @Test
    public void mensagemMostraAHoraDoEnvio() throws Exception {
        List<MensagemConversa> mensagens =
                ConversaJson.mensagens(new JSONObject(PAGINA_DE_MENSAGENS), ConversaJson.LADO_CONDOMINIO);

        assertEquals("16h", mensagens.get(0).hora);
        assertEquals("9h05", mensagens.get(1).hora);
        assertEquals("14h30", mensagens.get(2).hora);
    }

    @Test
    public void corpoDeEnvioETextoDoCriarTemOsCamposDaApi() throws Exception {
        JSONObject envio = new JSONObject(ConversaJson.corpoEnviar("Oi, tudo bem?"));
        JSONObject criar = new JSONObject(ConversaJson.corpoCriar(100, 200));

        assertEquals("Oi, tudo bem?", envio.getString("conteudo"));
        assertEquals(100, criar.getInt("condominioId"));
        assertEquals(200, criar.getInt("cooperativaId"));
    }

    @Test
    public void soACooperativaAbreAConversa() {
        assertFalse(ConversaJson.podeEscrever(ConversaJson.LADO_CONDOMINIO, 0));
        assertTrue(ConversaJson.podeEscrever(ConversaJson.LADO_CONDOMINIO, 1));
        assertTrue(ConversaJson.podeEscrever(ConversaJson.LADO_COOPERATIVA, 0));
    }

    @Test
    public void mensagemDeErroDependeDoStatusDaApi() {
        assertTrue(ConversaJson.mensagemDoErroDeEnvio(400, null).contains("2000"));
        assertTrue(ConversaJson.mensagemDoErroDeEnvio(403, null).contains("não faz parte"));
        assertTrue(ConversaJson.mensagemDoErroDeEnvio(409, null).contains("arquivada"));
        assertTrue(ConversaJson.mensagemDoErroDeEnvio(0, null).contains("Tente de novo"));
    }
}
