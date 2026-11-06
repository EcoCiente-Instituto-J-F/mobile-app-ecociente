package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.demo.ApiDemo;
import com.example.ecociente.demo.ArmazemEmMemoria;
import com.example.ecociente.demo.EstadoDemo;
import com.example.ecociente.demo.RespostaDemo;
import com.example.ecociente.demo.SementeDemo;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.model.MovimentacaoPontos;
import com.example.ecociente.model.PosicaoNoRanking;
import com.example.ecociente.model.SindicoPosicaoRanking;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

// Pontuação e ranking da ds-pontuacao-api falsa, lidos pelos parsers reais.
public class DemoPontuacaoContratoTest {

    private static final String MOBILE = "https://ds-mobile-api.onrender.com";
    private static final String PONTOS = "https://ds-pontuacao-api.onrender.com";
    private static final long AGORA = 1_700_000_000_000L;

    private EstadoDemo estado;
    private ApiDemo api;
    private String token;

    @Before
    public void preparar() throws Exception {
        estado = new EstadoDemo(new ArmazemEmMemoria(), () -> AGORA);
        api = new ApiDemo(estado, () -> AGORA);

        String corpo = new JSONObject().put("email", SementeDemo.EMAIL).put("senha", SementeDemo.SENHA).toString();
        RespostaDemo login = api.tratar("POST", MOBILE + "/auth/login", null, corpo.getBytes(StandardCharsets.UTF_8));

        token = "Bearer " + new JSONObject(login.texto()).getString("token");
    }

    private RespostaDemo obter(String caminho) {
        return api.tratar("GET", PONTOS + caminho, token, new byte[0]);
    }

    private PosicaoNoRanking posicao() throws Exception {
        RespostaDemo r = obter("/api/v1/rankings/moradores/42/posicao?condominioId=7");

        assertEquals(200, r.codigo);

        return PontuacaoJson.posicao(new JSONObject(r.texto()));
    }

    private List<ItemLista> moradores(String query) throws Exception {
        RespostaDemo r = obter("/api/v1/rankings/moradores?condominioId=7" + query);

        assertEquals(200, r.codigo);

        return RankingJson.moradores(new JSONObject(r.texto()));
    }

    @Test
    public void saldo_e320() throws Exception {
        RespostaDemo r = obter("/api/v1/pontuacoes/usuarios/42/saldo");

        assertEquals(200, r.codigo);
        assertEquals(320L, PontuacaoJson.saldo(new JSONObject(r.texto())));
    }

    @Test
    public void movimentacoes_somamOSaldo_eVemDoMaisRecente() throws Exception {
        RespostaDemo r = obter("/api/v1/pontuacoes/usuarios/42/movimentacoes?pagina=0&tamanho=10");

        assertEquals(200, r.codigo);

        JSONObject json = new JSONObject(r.texto());
        List<MovimentacaoPontos> itens = PontuacaoJson.movimentacoes(json);

        assertTrue(itens.size() >= 2);
        assertTrue(PontuacaoJson.ultimaPagina(json));

        int soma = 0;
        String anterior = null;

        for (MovimentacaoPontos m : itens) {
            soma += m.getPontos();

            assertFalse(m.getOcorridoEm().isEmpty());
            assertTrue(anterior == null || anterior.compareTo(m.getOcorridoEm()) >= 0);

            anterior = m.getOcorridoEm();
        }

        assertEquals(320, soma);
    }

    @Test
    public void movimentacoes_paginam() throws Exception {
        JSONObject primeira =
                new JSONObject(obter("/api/v1/pontuacoes/usuarios/42/movimentacoes?pagina=0&tamanho=1").texto());
        JSONObject ultima =
                new JSONObject(obter("/api/v1/pontuacoes/usuarios/42/movimentacoes?pagina=2&tamanho=1").texto());

        assertEquals(1, PontuacaoJson.movimentacoes(primeira).size());
        assertFalse(PontuacaoJson.ultimaPagina(primeira));
        assertTrue(PontuacaoJson.ultimaPagina(ultima));
    }

    @Test
    public void posicao_e4De5Com320() throws Exception {
        PosicaoNoRanking p = posicao();

        assertEquals(4L, p.getPosicao());
        assertEquals(320L, p.getPontos());
        assertEquals(5L, p.getTotalParticipantes());
    }

    @Test
    public void ranking_comLimite3_devolvePodio() throws Exception {
        List<ItemLista> lista = moradores("&limit=3");

        assertEquals(3, lista.size());
        assertTrue(lista.get(0).titulo.startsWith("1º"));
        assertTrue(lista.get(0).destaque.startsWith("480"));
        assertTrue(lista.get(1).destaque.startsWith("410"));
        assertTrue(lista.get(2).destaque.startsWith("350"));
    }

    @Test
    public void ranking_semLimite_devolveTodos() throws Exception {
        assertEquals(5, moradores("").size());
    }

    @Test
    public void ranking_posicoesLidasComoSindico() throws Exception {
        RespostaDemo r = obter("/api/v1/rankings/moradores?condominioId=7&limit=5");
        List<SindicoPosicaoRanking> lista = RankingJson.posicoes(new JSONObject(r.texto()));

        assertEquals(5, lista.size());
        assertEquals(SementeDemo.NOME, lista.get(3).getNome());
    }

    @Test
    public void creditarPostagem_sobeSaldoEPosicao() throws Exception {
        estado.creditar("POSTAGEM", 50);

        assertEquals(370, estado.saldo());
        assertEquals(3, estado.posicaoDoUsuario());
        assertEquals(370L, PontuacaoJson.saldo(new JSONObject(obter("/api/v1/pontuacoes/usuarios/42/saldo").texto())));
        assertEquals(3L, posicao().getPosicao());

        List<ItemLista> podio = moradores("&limit=3");

        assertTrue(podio.get(2).titulo.contains(SementeDemo.NOME));

        List<MovimentacaoPontos> itens =
                PontuacaoJson.movimentacoes(
                        new JSONObject(
                                obter("/api/v1/pontuacoes/usuarios/42/movimentacoes?pagina=0&tamanho=10").texto()));

        assertEquals(MovimentacaoPontos.Origem.POSTAGEM, itens.get(0).getOrigem());
        assertEquals(50, itens.get(0).getPontos());
    }

    @Test
    public void creditarQuiz_persisteNoArmazem() throws Exception {
        ArmazemEmMemoria armazem = new ArmazemEmMemoria();
        EstadoDemo a = new EstadoDemo(armazem, () -> AGORA);

        a.creditar("QUIZ", 30);

        assertEquals(350, new EstadoDemo(armazem, () -> AGORA).saldo());
    }

    @Test(expected = IllegalArgumentException.class)
    public void creditarOrigemDesconhecida_recusa() {
        estado.creditar("OUTRA", 10);
    }

    @Test
    public void empate_desempataPorNome() throws Exception {
        estado.creditar("QUIZ", 30);

        assertEquals(350, estado.saldo());
        // Marina Alves vem antes de Thiago Barros, ambos com 350.
        assertEquals(3, estado.posicaoDoUsuario());
        assertTrue(moradores("&limit=3").get(2).titulo.contains(SementeDemo.NOME));
        assertEquals(3L, posicao().getPosicao());
    }

    @Test
    public void semToken_devolve401() {
        assertEquals(401, api.tratar("GET", PONTOS + "/api/v1/pontuacoes/usuarios/42/saldo", null, new byte[0]).codigo);
    }

    @Test
    public void outroUsuario_devolve403() {
        assertEquals(403, obter("/api/v1/pontuacoes/usuarios/43/saldo").codigo);
    }

    @Test
    public void reiniciar_voltaA320() throws Exception {
        estado.creditar("QUIZ", 30);
        estado.resetar();

        assertEquals(320, estado.saldo());
    }
}
