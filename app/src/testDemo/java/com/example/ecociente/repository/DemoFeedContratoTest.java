package com.example.ecociente.repository;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.demo.ApiDemo;
import com.example.ecociente.demo.ArmazemEmMemoria;
import com.example.ecociente.demo.EstadoDemo;
import com.example.ecociente.demo.RespostaDemo;
import com.example.ecociente.demo.SementeDemo;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.model.PostagemFeed;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

// Feed, categorias, upload, registro, votos e a aprovação automática do descarte, lidos pelos parsers reais.
public class DemoFeedContratoTest {

    private static final String MOBILE = "https://ds-mobile-api.onrender.com";
    private static final String UPLOAD = "https://api.cloudinary.com/v1_1/ncwfxyfi/image/upload";
    private static final long AGORA = 1_700_000_000_000L;
    private static final String PREFIXO_FOTO = "android.resource://com.example.ecociente.demo/drawable/img_green";

    private final long[] relogio = {AGORA};
    private final List<Boolean> aprovacoes = new ArrayList<>();

    private ArmazemEmMemoria armazem;
    private EstadoDemo estado;
    private ApiDemo api;
    private String token;

    @Before
    public void preparar() throws Exception {
        armazem = new ArmazemEmMemoria();
        abrir();

        token = entrar();
    }

    // "Abre o app": estado e API novos sobre o mesmo armazém e o mesmo relógio.
    private void abrir() {
        estado = new EstadoDemo(armazem, () -> relogio[0]);
        api = new ApiDemo(estado, () -> relogio[0]);
        api.definirOuvinte(aprovacoes::add);
    }

    private String entrar() throws Exception {
        String corpo = new JSONObject().put("email", SementeDemo.EMAIL).put("senha", SementeDemo.SENHA).toString();
        RespostaDemo login = api.tratar("POST", MOBILE + "/auth/login", null, corpo.getBytes(StandardCharsets.UTF_8));

        return "Bearer " + new JSONObject(login.texto()).getString("token");
    }

    private RespostaDemo obter(String caminho) {
        return api.tratar("GET", MOBILE + caminho, token, new byte[0]);
    }

    private RespostaDemo enviar(String caminho, String json) {
        return api.tratar("POST", MOBILE + caminho, token, json.getBytes(StandardCharsets.UTF_8));
    }

    private List<PostagemFeed> postagens(String caminho) throws Exception {
        RespostaDemo r = obter(caminho);

        assertEquals(200, r.codigo);

        return PostagemJson.postagens(new JSONObject(r.texto()), relogio[0]);
    }

    private List<PostagemFeed> feed() throws Exception {
        return postagens("/api/postagens?condominioId=7&size=20");
    }

    private List<PostagemFeed> minhas() throws Exception {
        return postagens("/api/postagens/minhas?size=20");
    }

    private RespostaDemo registrar(String hash) throws Exception {
        String corpo = PostagemJson.corpoCriar(7, 1, "file:///memoria/foto.jpg", hash, PostagemJson.iso(relogio[0]));

        return enviar("/api/postagens", corpo);
    }

    private static String codigo(RespostaDemo r) throws Exception {
        return new JSONObject(r.texto()).getString("codigo");
    }

    private static byte[] multipart(String limite, String preset, byte[] jpeg) throws Exception {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();

        if (preset != null) {
            saida.write(
                    ("--" + limite + "\r\nContent-Disposition: form-data; name=\"upload_preset\"\r\n\r\n" + preset + "\r\n")
                            .getBytes(StandardCharsets.UTF_8));
        }

        if (jpeg != null) {
            saida.write(
                    ("--" + limite
                                    + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"foto.jpg\"\r\n"
                                    + "Content-Type: image/jpeg\r\n\r\n")
                            .getBytes(StandardCharsets.UTF_8));
            saida.write(jpeg);
            saida.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }

        saida.write(("--" + limite + "--\r\n").getBytes(StandardCharsets.UTF_8));

        return saida.toByteArray();
    }

    // Todos os 256 valores de byte, com CRLF no meio e algo parecido com um limite: não pode passar por String.
    private static byte[] jpegDeTeste() {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();

        for (int i = 0; i < 256; i++) {
            saida.write(i);
        }

        byte[] meio = "\r\n--NaoEOLimite\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        saida.write(meio, 0, meio.length);
        saida.write(0xFF);
        saida.write(0xD9);

        return saida.toByteArray();
    }

    // ---- Leitura ----

    @Test
    public void feed_semente_temPostagensDeOutros() throws Exception {
        JSONObject bruto = new JSONObject(obter("/api/postagens?condominioId=7&size=20").texto());
        JSONArray itens = bruto.getJSONArray("postagens");
        int aprovadas = 0;

        for (int i = 0; i < itens.length(); i++) {
            JSONObject item = itens.getJSONObject(i);

            if ("aprovada".equals(item.getString("statusValidacao"))) {
                aprovadas++;

                assertTrue(item.getString("urlFoto").startsWith(PREFIXO_FOTO));
            }
        }

        assertTrue(aprovadas >= 4);

        List<PostagemFeed> lista = PostagemJson.postagens(bruto, AGORA);
        Set<String> categorias = new HashSet<>();

        for (PostagemFeed p : lista) {
            assertFalse(p.getAutor().isEmpty());
            assertFalse(p.getAutor().equals(SementeDemo.NOME));
            assertFalse(p.getQuando().isEmpty());
            assertTrue(p.getFotoUrl().startsWith(PREFIXO_FOTO));

            categorias.add(p.getCategoria());
        }

        assertTrue(categorias.size() >= 2);
        assertTrue(minhas().isEmpty());
    }

    @Test
    public void categorias_temIdsPositivos() throws Exception {
        RespostaDemo r = obter("/api/categorias-residuos");

        assertEquals(200, r.codigo);

        List<ItemLista> lista = PostagemJson.categorias(new JSONArray(r.texto()));

        assertTrue(lista.size() >= 4);

        Set<Integer> ids = new HashSet<>();

        for (ItemLista item : lista) {
            assertTrue(Integer.parseInt(item.chave) > 0);
            assertFalse(item.titulo.isEmpty());
            assertFalse(item.subtitulo.isEmpty());
            assertTrue(ids.add(Integer.parseInt(item.chave)));
        }
    }

    @Test
    public void semToken_devolve401() {
        assertEquals(401, api.tratar("GET", MOBILE + "/api/postagens?condominioId=7", null, new byte[0]).codigo);
        assertEquals(401, api.tratar("GET", MOBILE + "/api/postagens/minhas", null, new byte[0]).codigo);
        assertEquals(401, api.tratar("GET", MOBILE + "/api/categorias-residuos", null, new byte[0]).codigo);
        assertEquals(
                401, api.tratar("POST", MOBILE + "/api/postagens", null, "{}".getBytes(StandardCharsets.UTF_8)).codigo);
    }

    @Test
    public void feed_deOutroCondominio_devolve403() {
        assertEquals(403, obter("/api/postagens?condominioId=8").codigo);
    }

    // ---- Upload ----

    @Test
    public void upload_multipart_gravaSoOJpeg() throws Exception {
        byte[] jpeg = jpegDeTeste();

        RespostaDemo r =
                api.tratar("POST", UPLOAD, null, multipart("----EcoCiente1700000000000", "ecociente_feed", jpeg));

        assertEquals(200, r.codigo);
        assertEquals(1, armazem.fotos.size());
        assertArrayEquals(jpeg, armazem.fotos.get(0));
        assertEquals("file:///memoria/foto_1.jpg", new JSONObject(r.texto()).getString("secure_url"));
    }

    @Test
    public void upload_semArquivoOuSemPreset_devolve400() throws Exception {
        assertEquals(400, api.tratar("POST", UPLOAD, null, multipart("L", "ecociente_feed", null)).codigo);
        assertEquals(400, api.tratar("POST", UPLOAD, null, multipart("L", null, jpegDeTeste())).codigo);
        assertEquals(400, api.tratar("POST", UPLOAD, null, "lixo".getBytes(StandardCharsets.UTF_8)).codigo);
        assertTrue(armazem.fotos.isEmpty());
    }

    // ---- Registro ----

    @Test
    public void registrar_criaEmAnalise() throws Exception {
        int antes = feed().size();

        RespostaDemo r = registrar("hash-a");

        assertEquals(201, r.codigo);

        List<PostagemFeed> minhas = minhas();

        assertEquals(1, minhas.size());
        assertEquals("Em análise", minhas.get(0).getStatus());
        assertEquals("file:///memoria/foto.jpg", minhas.get(0).getFotoUrl());
        assertEquals("Plástico", minhas.get(0).getCategoria());
        assertEquals(antes, feed().size());
        assertEquals(320, estado.saldo());
    }

    @Test
    public void mesmoHash_devolve409() throws Exception {
        assertEquals(201, registrar("hash-a").codigo);

        RespostaDemo repetida = registrar("hash-a");

        assertEquals(409, repetida.codigo);
        assertEquals(
                "Esta foto já foi usada em outro descarte. Tire uma foto nova.",
                PostagemJson.mensagemDoErro(repetida.codigo, repetida.texto()));
        assertEquals(1, minhas().size());
    }

    @Test
    public void registrar_comDadosInvalidos_devolveOCodigoDaApi() throws Exception {
        RespostaDemo categoria =
                enviar("/api/postagens", PostagemJson.corpoCriar(7, 999, "u", "h", PostagemJson.iso(AGORA)));
        RespostaDemo condominio =
                enviar("/api/postagens", PostagemJson.corpoCriar(8, 1, "u", "h", PostagemJson.iso(AGORA)));

        assertEquals("CATEGORIA_NAO_ENCONTRADA", codigo(categoria));
        assertEquals("CONDOMINIO_ID_INVALIDO", codigo(condominio));
        assertEquals(400, enviar("/api/postagens", "nao e json").codigo);
        assertTrue(minhas().isEmpty());
    }

    // ---- Aprovação ----

    @Test
    public void aprovacao() throws Exception {
        registrar("hash-a");

        relogio[0] = AGORA + 19_999;
        api.processarPendentes();

        assertEquals("Em análise", minhas().get(0).getStatus());
        assertEquals(320, estado.saldo());
        assertTrue(aprovacoes.isEmpty());

        relogio[0] = AGORA + 20_000;
        api.processarPendentes();

        assertEquals("Aprovada", minhas().get(0).getStatus());
        assertEquals(370, estado.saldo());
        assertEquals(1, feed().stream().filter(p -> "file:///memoria/foto.jpg".equals(p.getFotoUrl())).count());
        assertEquals(1, aprovacoes.size());
        assertTrue(aprovacoes.get(0));

        api.processarPendentes();
        obter("/api/postagens/minhas");

        assertEquals(370, estado.saldo());
        assertEquals(1, aprovacoes.size());
    }

    @Test
    public void aprovacao_viraPontosNaMovimentacaoENoRanking() throws Exception {
        registrar("hash-a");

        relogio[0] = AGORA + 20_000;
        api.processarPendentes();

        assertEquals(3, estado.posicaoDoUsuario());
        assertEquals(
                370L,
                PontuacaoJson.saldo(
                        new JSONObject(
                                api.tratar(
                                                "GET",
                                                "https://ds-pontuacao-api.onrender.com/api/v1/pontuacoes/usuarios/42/saldo",
                                                token,
                                                new byte[0])
                                        .texto())));
    }

    @Test
    public void aprovacao_acontecePorCimaDeQualquerRequisicao() throws Exception {
        registrar("hash-a");

        relogio[0] = AGORA + 25_000;

        // Sem ninguém chamar processarPendentes: o GET já responde com a aprovação feita.
        assertEquals("Aprovada", minhas().get(0).getStatus());
        assertEquals(370, estado.saldo());
        assertEquals(1, aprovacoes.size());
    }

    @Test
    public void aprovacoes_sucessivas_avisamSeSubiuNoRanking() throws Exception {
        for (int i = 0; i < 4; i++) {
            registrar("hash-" + i);

            relogio[0] += 20_000;
            api.processarPendentes();
        }

        // 370 (3º), 420 (2º), 470 (continua 2º, Rafael tem 480), 520 (1º).
        assertEquals(java.util.Arrays.asList(true, true, false, true), aprovacoes);
        assertEquals(520, estado.saldo());
        assertEquals(1, estado.posicaoDoUsuario());
    }

    @Test
    public void reabrir_depoisDoPrazo_aprovaUmaVez() throws Exception {
        registrar("hash-a");

        relogio[0] = AGORA + 60_000;
        aprovacoes.clear();
        abrir();
        token = entrar();

        List<PostagemFeed> minhas = minhas();

        assertEquals("Aprovada", minhas.get(0).getStatus());
        assertEquals(370, estado.saldo());
        assertEquals(1, aprovacoes.size());

        api.processarPendentes();
        abrir();

        assertEquals(370, estado.saldo());
        assertEquals(1, aprovacoes.size());
    }

    @Test
    public void reabrir_antesDoPrazo_mantemPendenteEAprovaDepois() throws Exception {
        registrar("hash-a");

        relogio[0] = AGORA + 5_000;
        abrir();

        assertEquals("Em análise", minhas().get(0).getStatus());
        assertEquals(320, estado.saldo());

        relogio[0] = AGORA + 20_000;
        api.processarPendentes();

        assertEquals("Aprovada", minhas().get(0).getStatus());
        assertEquals(370, estado.saldo());
        assertEquals(1, aprovacoes.size());
    }

    @Test
    public void resetar_comPendente_naoRessuscita() throws Exception {
        registrar("hash-a");

        estado.resetar();
        relogio[0] = AGORA + 60_000;
        api.processarPendentes();
        token = entrar();

        assertTrue(minhas().isEmpty());
        assertEquals(320, estado.saldo());
        assertTrue(aprovacoes.isEmpty());

        abrir();
        api.processarPendentes();

        assertEquals(320, estado.saldo());
        assertTrue(aprovacoes.isEmpty());
    }

    // O ouvinte pode mostrar notificação e tocar no estado: roda fora da trava da API.
    @Test
    public void ouvinte_rodaForaDaTrava() throws Exception {
        boolean[] outraThreadPassou = {false};

        api.definirOuvinte(
                subiu -> {
                    Thread outra = new Thread(() -> {
                        estado.saldo();
                        outraThreadPassou[0] = true;
                    });

                    outra.start();

                    try {
                        outra.join(3_000);

                    } catch (InterruptedException erro) {
                        Thread.currentThread().interrupt();
                    }
                });

        registrar("hash-a");
        relogio[0] = AGORA + 20_000;
        api.processarPendentes();

        assertTrue(outraThreadPassou[0]);
    }

    @Test
    public void ouvinteQueFalha_naoDesfazAAprovacao() throws Exception {
        api.definirOuvinte(
                subiu -> {
                    throw new IllegalStateException("sem permissão");
                });

        registrar("hash-a");
        relogio[0] = AGORA + 20_000;
        api.processarPendentes();

        assertEquals(370, estado.saldo());
        assertEquals("Aprovada", minhas().get(0).getStatus());
    }

    // ---- Votos ----

    private int idDeUmaEmAnaliseDeOutro() throws Exception {
        for (PostagemFeed p : feed()) {
            if (PostagemJson.statusLegivel("em_analise").equals(p.getStatus())) {
                return p.getId();
            }
        }

        throw new AssertionError("A semente deveria ter um descarte de outro morador em análise");
    }

    private RespostaDemo votar(int id, String corpo) {
        return enviar("/api/postagens/" + id + "/votos", corpo);
    }

    @Test
    public void voto_primeiroAprovar_repetidoRecusa() throws Exception {
        int id = idDeUmaEmAnaliseDeOutro();

        RespostaDemo primeiro = votar(id, PostagemJson.corpoVoto(true, 0));

        assertTrue(primeiro.codigo >= 200 && primeiro.codigo < 300);

        RespostaDemo repetido = votar(id, PostagemJson.corpoVoto(true, 0));

        assertEquals(409, repetido.codigo);
        assertEquals("VOTO_JA_REGISTRADO", codigo(repetido));
        assertEquals(
                "Você já votou nesta postagem.", PostagemJson.mensagemDoVoto(repetido.codigo, repetido.texto()));
        assertEquals("VOTO_JA_REGISTRADO", codigo(votar(id, PostagemJson.corpoVoto(false, 1))));
    }

    @Test
    public void voto_denunciarComMotivo_funciona() throws Exception {
        RespostaDemo r = votar(idDeUmaEmAnaliseDeOutro(), PostagemJson.corpoVoto(false, 2));

        assertTrue(r.codigo >= 200 && r.codigo < 300);
    }

    @Test
    public void voto_naPropria_naoPermitido() throws Exception {
        registrar("hash-a");

        RespostaDemo r = votar(minhas().get(0).getId(), PostagemJson.corpoVoto(true, 0));

        assertEquals(403, r.codigo);
        assertEquals("VOTO_NAO_PERMITIDO", codigo(r));
    }

    @Test
    public void voto_idInexistente_naoEncontrada() throws Exception {
        RespostaDemo r = votar(9999, PostagemJson.corpoVoto(true, 0));

        assertEquals(404, r.codigo);
        assertEquals("POSTAGEM_NAO_ENCONTRADA", codigo(r));
    }

    @Test
    public void voto_denunciarSemMotivo_exigeMotivo() throws Exception {
        RespostaDemo r = votar(idDeUmaEmAnaliseDeOutro(), PostagemJson.corpoVoto(false, 0));

        assertEquals(400, r.codigo);
        assertEquals("MOTIVO_DENUNCIA_OBRIGATORIO", codigo(r));
    }

    @Test
    public void voto_emPostagemJaDecidida_votacaoEncerrada() throws Exception {
        int aprovada = -1;

        for (PostagemFeed p : feed()) {
            if (PostagemJson.statusLegivel("aprovada").equals(p.getStatus())) {
                aprovada = p.getId();
            }
        }

        assertTrue(aprovada > 0);
        assertEquals("VOTACAO_ENCERRADA", codigo(votar(aprovada, PostagemJson.corpoVoto(true, 0))));
    }

    @Test
    public void voto_sobrevivePraReabrir() throws Exception {
        int id = idDeUmaEmAnaliseDeOutro();

        votar(id, PostagemJson.corpoVoto(true, 0));
        abrir();

        assertEquals("VOTO_JA_REGISTRADO", codigo(votar(id, PostagemJson.corpoVoto(true, 0))));
    }
}
