package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.MovimentacaoPontos;
import com.example.ecociente.model.PosicaoNoRanking;
import java.util.List;
import org.json.JSONObject;
import org.junit.Test;

public class PontuacaoJsonTest {

    @Test
    public void saldoLeOCampoSaldo() throws Exception {
        assertEquals(1450L, PontuacaoJson.saldo(new JSONObject("{\"usuarioId\":4,\"saldo\":1450}")));
    }

    @Test
    public void saldoAusenteVale0() throws Exception {
        assertEquals(0L, PontuacaoJson.saldo(new JSONObject("{}")));
    }

    @Test
    public void movimentacoesLeOrigemTipoPontosEData() throws Exception {
        JSONObject pagina =
                new JSONObject(
                        "{\"conteudo\":["
                                + "{\"origemTipo\":\"POSTAGEM\",\"tipoMovimentacao\":\"CREDITO\",\"pontos\":500,"
                                + "\"ocorridoEm\":\"2026-10-17T10:00:00-03:00\"},"
                                + "{\"origemTipo\":\"QUIZ\",\"tipoMovimentacao\":\"ESTORNO\",\"pontos\":30,"
                                + "\"ocorridoEm\":\"2026-10-16T09:00:00-03:00\"}],"
                                + "\"pagina\":0,\"tamanho\":20,\"totalElementos\":2,\"totalPaginas\":1}");

        List<MovimentacaoPontos> lista = PontuacaoJson.movimentacoes(pagina);

        assertEquals(2, lista.size());
        assertEquals(MovimentacaoPontos.Origem.POSTAGEM, lista.get(0).getOrigem());
        assertEquals(MovimentacaoPontos.Tipo.CREDITO, lista.get(0).getTipo());
        assertEquals(500, lista.get(0).getPontosComSinal());
        assertEquals("2026-10-17T10:00:00-03:00", lista.get(0).getOcorridoEm());
        assertEquals(-30, lista.get(1).getPontosComSinal());
    }

    @Test
    public void restauracaoSomaPontos() throws Exception {
        JSONObject pagina =
                new JSONObject(
                        "{\"conteudo\":[{\"origemTipo\":\"POSTAGEM\",\"tipoMovimentacao\":\"RESTAURACAO\","
                                + "\"pontos\":10,\"ocorridoEm\":\"2026-10-17T10:00:00-03:00\"}]}");

        assertEquals(10, PontuacaoJson.movimentacoes(pagina).get(0).getPontosComSinal());
    }

    @Test
    public void valoresDesconhecidosViramOutraECredito() throws Exception {
        JSONObject pagina = new JSONObject("{\"conteudo\":[{\"origemTipo\":\"NOVA\",\"pontos\":5}]}");

        MovimentacaoPontos m = PontuacaoJson.movimentacoes(pagina).get(0);

        assertEquals(MovimentacaoPontos.Origem.OUTRA, m.getOrigem());
        assertEquals(MovimentacaoPontos.Tipo.CREDITO, m.getTipo());
        assertEquals("", m.getOcorridoEm());
    }

    @Test
    public void paginaSemConteudoOuComItemNuloDevolveVazia() throws Exception {
        assertTrue(PontuacaoJson.movimentacoes(new JSONObject("{}")).isEmpty());
        assertTrue(PontuacaoJson.movimentacoes(new JSONObject("{\"conteudo\":[null]}")).isEmpty());
    }

    @Test
    public void ultimaPaginaConsideraPaginaETotal() throws Exception {
        assertTrue(PontuacaoJson.ultimaPagina(new JSONObject("{\"pagina\":0,\"totalPaginas\":1}")));
        assertTrue(PontuacaoJson.ultimaPagina(new JSONObject("{\"pagina\":2,\"totalPaginas\":3}")));
        assertFalse(PontuacaoJson.ultimaPagina(new JSONObject("{\"pagina\":0,\"totalPaginas\":3}")));
    }

    @Test
    public void posicaoLePosicaoPontosETotal() throws Exception {
        PosicaoNoRanking posicao =
                PontuacaoJson.posicao(
                        new JSONObject("{\"usuarioId\":4,\"posicao\":3,\"pontos\":1720,\"totalParticipantes\":12}"));

        assertEquals(3L, posicao.getPosicao());
        assertEquals(1720L, posicao.getPontos());
        assertEquals(12L, posicao.getTotalParticipantes());
        assertTrue(posicao.estaNoRanking());
    }

    @Test
    public void quemNaoPontuouNaoEstaNoRanking() throws Exception {
        assertFalse(PontuacaoJson.posicao(new JSONObject("{\"posicao\":0,\"pontos\":0,\"totalParticipantes\":5}")).estaNoRanking());
        assertFalse(PontuacaoJson.posicao(new JSONObject("{}")).estaNoRanking());
    }
}
