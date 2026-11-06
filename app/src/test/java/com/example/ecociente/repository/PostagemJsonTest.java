package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.ItemLista;
import com.example.ecociente.model.PostagemFeed;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class PostagemJsonTest {

    @Test
    public void votoDeAprovacaoNaoLevaMotivo() throws Exception {
        JSONObject corpo = new JSONObject(PostagemJson.corpoVoto(true, 3));

        assertEquals("aprovar", corpo.getString("tipo"));
        assertFalse(corpo.has("motivoDenunciaId"));
    }

    @Test
    public void denunciaLevaOIdDoMotivo() throws Exception {
        JSONObject corpo = new JSONObject(PostagemJson.corpoVoto(false, 3));

        assertEquals("denunciar", corpo.getString("tipo"));
        assertEquals(3, corpo.getInt("motivoDenunciaId"));
    }

    @Test
    public void mensagemDoVotoDependeDoCodigoDaApi() {
        assertTrue(PostagemJson.mensagemDoVoto(409, "{\"codigo\":\"VOTO_JA_REGISTRADO\"}").contains("já votou"));
        assertTrue(PostagemJson.mensagemDoVoto(409, "{\"codigo\":\"VOTACAO_ENCERRADA\"}").contains("encerrada"));
        assertTrue(PostagemJson.mensagemDoVoto(403, "{\"codigo\":\"VOTO_NAO_PERMITIDO\"}").contains("própria"));
        assertTrue(PostagemJson.mensagemDoVoto(401, null).contains("sessão"));
        assertTrue(PostagemJson.mensagemDoVoto(0, "nao e json").contains("Tente de novo"));
    }

    @Test    public void leOEnvelopeDoFeed() throws Exception {
        JSONObject envelope =
                new JSONObject(
                        "{\"total\":2,\"pagina\":0,\"tamanho\":20,\"totalPaginas\":1,\"postagens\":["
                                + "{\"id\":7,\"autor\":\"Ana\",\"urlFoto\":\"https://x/a.jpg\","
                                + "\"categoria\":\"Plástico\",\"statusValidacao\":\"em_analise\","
                                + "\"dataPostagem\":\"2026-10-22T13:00:00Z\"},"
                                + "{\"id\":6,\"autor\":\"Bia\",\"urlFoto\":\"https://x/b.jpg\","
                                + "\"categoria\":\"Papel\",\"statusValidacao\":\"aprovada\"}]}");

        List<PostagemFeed> lista = PostagemJson.postagens(envelope, 0L);

        assertEquals(2, lista.size());
        assertEquals(7, lista.get(0).getId());
        assertEquals("Ana", lista.get(0).getAutor());
        assertEquals("https://x/a.jpg", lista.get(0).getFotoUrl());
        assertEquals("Plástico", lista.get(0).getCategoria());
        assertEquals("Em análise", lista.get(0).getStatus());
        assertEquals("Aprovada", lista.get(1).getStatus());
    }

    @Test
    public void envelopeSemPostagensDevolveListaVazia() throws Exception {
        assertTrue(PostagemJson.postagens(new JSONObject("{\"total\":0}"), 0L).isEmpty());
    }

    @Test
    public void statusDesconhecidoFicaEmBranco() {
        assertEquals("", PostagemJson.statusLegivel("outro"));
        assertEquals("Reprovada", PostagemJson.statusLegivel("reprovada"));
    }

    @Test
    public void categoriasViramItensComOIdNaChave() throws Exception {
        List<ItemLista> lista =
                PostagemJson.categorias(
                        new JSONArray(
                                "[{\"id\":2,\"nome\":\"Papel\",\"descricao\":null,\"pontosBase\":10},"
                                        + "{\"id\":1,\"nome\":\"Plástico\",\"descricao\":\"Garrafas\",\"pontosBase\":15},"
                                        + "{\"id\":0,\"nome\":\"Inválida\"}]"));

        assertEquals(2, lista.size());
        assertEquals("2", lista.get(0).chave);
        assertEquals("Papel", lista.get(0).titulo);
        assertEquals("", lista.get(0).subtitulo);
        assertEquals("Garrafas", lista.get(1).subtitulo);
        assertEquals("15", lista.get(1).destaque);
    }

    @Test
    public void corpoDeCriarTemTodosOsCamposDaApi() throws Exception {
        JSONObject corpo =
                new JSONObject(PostagemJson.corpoCriar(5, 2, "https://x/f.jpg", "abc", "2026-10-22T13:00:00Z"));

        assertEquals("https://x/f.jpg", corpo.getString("urlFoto"));
        assertEquals("abc", corpo.getString("hashFoto"));
        assertEquals("2026-10-22T13:00:00Z", corpo.getString("capturadaEm"));
        assertEquals(2, corpo.getInt("categoriaId"));
        assertEquals(5, corpo.getInt("condominioId"));
    }

    @Test
    public void isoSaiEmUtcNoFormatoQueAApiLe() {
        assertEquals("1970-01-01T00:00:00Z", PostagemJson.iso(0L));
        assertEquals("2026-10-22T13:00:00Z", PostagemJson.iso(1792674000000L));
    }

    @Test
    public void sha256DaMesmaFotoEhSempreIgual() {
        assertEquals(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                PostagemJson.sha256("abc".getBytes()));
        assertEquals(PostagemJson.sha256("x".getBytes()), PostagemJson.sha256("x".getBytes()));
    }

    @Test
    public void mensagemDoErroUsaOCodigoDaApi() {
        assertTrue(PostagemJson.mensagemDoErro(409, "{\"codigo\":\"FOTO_JA_UTILIZADA\"}").contains("já foi usada"));
        assertTrue(PostagemJson.mensagemDoErro(403, "{\"codigo\":\"SEM_VINCULO_APROVADO\"}").contains("vínculo"));
        assertTrue(PostagemJson.mensagemDoErro(400, "{\"codigo\":\"CAPTURA_INVALIDA\"}").contains("horário"));
        assertTrue(PostagemJson.mensagemDoErro(400, "{\"codigo\":\"CONDOMINIO_ID_INVALIDO\"}").contains("condomínio"));
    }

    @Test
    public void semCodigoUsaMensagemPeloStatusHttp() {
        assertTrue(PostagemJson.mensagemDoErro(401, null).contains("login"));
        assertTrue(PostagemJson.mensagemDoErro(500, "<html>").contains("Tente de novo"));
        assertTrue(PostagemJson.mensagemDoErro(0, "").contains("Tente de novo"));
    }

    @Test
    public void corpoDaDecisaoTrazSoOCampoAprovar() {
        assertEquals("{\"aprovar\":true}", PostagemJson.corpoDecisao(true));
        assertEquals("{\"aprovar\":false}", PostagemJson.corpoDecisao(false));
    }

    @Test
    public void mensagemDaDecisaoExplicaOMotivo() {
        assertEquals("Você não pode decidir os descartes deste condomínio.", PostagemJson.mensagemDaDecisao(403));
        assertEquals("Este descarte não foi encontrado.", PostagemJson.mensagemDaDecisao(404));
        assertEquals("Não foi possível registrar a decisão. Tente de novo.", PostagemJson.mensagemDaDecisao(500));
    }
}
