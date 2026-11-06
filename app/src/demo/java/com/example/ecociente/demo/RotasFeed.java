package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.LongSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// ds-mobile-api, descartes: GET /api/postagens (feed), GET /api/postagens/minhas, GET /api/categorias-residuos, POST /api/postagens (registrar) e POST /api/postagens/{id}/votos. A decisão do síndico (/decisao) e a fila de análise do síndico ficam de fora: nenhuma tela do morador as usa.
final class RotasFeed implements RotasDemo {

    private static final String HOST = "ds-mobile-api.onrender.com";
    private static final String POSTAGENS = "/api/postagens";
    private static final Pattern VOTOS = Pattern.compile("^/api/postagens/(\\d{1,9})/votos$");

    private final EstadoDemo estado;
    private final LongSupplier relogio;

    RotasFeed(@NonNull EstadoDemo estado, @NonNull LongSupplier relogio) {
        this.estado = estado;
        this.relogio = relogio;
    }

    @Nullable
    @Override
    public RespostaDemo tratar(@NonNull RequisicaoDemo r) {
        if (!HOST.equals(r.host)) {
            return null;
        }

        Matcher voto = VOTOS.matcher(r.caminho);
        boolean votar = "POST".equals(r.metodo) && voto.matches();

        if (!(r.e("GET", POSTAGENS)
                || r.e("GET", POSTAGENS + "/minhas")
                || r.e("GET", "/api/categorias-residuos")
                || r.e("POST", POSTAGENS)
                || votar)) {
            return null;
        }

        if (!ContaDemo.tokenConfere(estado.raiz(), r.autorizacao)) {
            return RespostaDemo.erro(401, "NAO_AUTORIZADO");
        }

        if (r.e("GET", "/api/categorias-residuos")) {
            return new RespostaDemo(200, FeedDemo.categorias(estado.raiz()).toString());
        }

        if (votar) {
            return votar(Integer.parseInt(voto.group(1)), r);
        }

        if (r.e("POST", POSTAGENS)) {
            return registrar(r);
        }

        return r.caminho.equals(POSTAGENS) ? feed(r) : minhas(r);
    }

    // Aprovadas e, de outros moradores, as em análise (as que dá para votar). O descarte em análise do próprio morador só aparece em "minhas".
    @NonNull
    private RespostaDemo feed(@NonNull RequisicaoDemo r) {
        if (!String.valueOf(SementeDemo.CONDOMINIO_ID).equals(r.query.get("condominioId"))) {
            return erro(403, "SEM_VINCULO_APROVADO");
        }

        List<JSONObject> itens = new ArrayList<>();

        for (JSONObject item : FeedDemo.postagens(estado.raiz())) {
            boolean aprovada = FeedDemo.APROVADA.equals(item.optString("statusValidacao"));
            boolean votavel = FeedDemo.EM_ANALISE.equals(item.optString("statusValidacao")) && !FeedDemo.doUsuario(item);

            if (aprovada || votavel) {
                itens.add(item);
            }
        }

        return pagina(itens, r);
    }

    @NonNull
    private RespostaDemo minhas(@NonNull RequisicaoDemo r) {
        List<JSONObject> itens = new ArrayList<>();

        for (JSONObject item : FeedDemo.postagens(estado.raiz())) {
            if (FeedDemo.doUsuario(item)) {
                itens.add(item);
            }
        }

        return pagina(itens, r);
    }

    // Mais recente primeiro; `page` começa em 0.
    @NonNull
    private static RespostaDemo pagina(@NonNull List<JSONObject> itens, @NonNull RequisicaoDemo r) {
        Collections.sort(
                itens,
                new Comparator<JSONObject>() {
                    @Override
                    public int compare(JSONObject a, JSONObject b) {
                        int porData = Long.compare(b.optLong("criadoEm", 0), a.optLong("criadoEm", 0));

                        return porData != 0 ? porData : Integer.compare(b.optInt("id", 0), a.optInt("id", 0));
                    }
                });

        int pagina = Math.max(0, inteiro(r.query.get("page"), 0));
        int tamanho = inteiro(r.query.get("size"), 20);

        if (tamanho <= 0) {
            tamanho = 20;
        }

        JSONArray postagens = new JSONArray();

        try {
            for (int i = pagina * tamanho; i < Math.min(itens.size(), (pagina + 1) * tamanho); i++) {
                postagens.put(visao(itens.get(i)));
            }

            return new RespostaDemo(
                    200,
                    new JSONObject()
                            .put("total", itens.size())
                            .put("pagina", pagina)
                            .put("tamanho", tamanho)
                            .put("totalPaginas", Math.max(1, (itens.size() + tamanho - 1) / tamanho))
                            .put("postagens", postagens)
                            .toString());

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    // O que a API devolve de uma postagem (sem o hash, o autor interno e os votos).
    @NonNull
    private static JSONObject visao(@NonNull JSONObject item) throws JSONException {
        return new JSONObject()
                .put("id", item.optInt("id"))
                .put("autor", item.optString("autor", ""))
                .put("urlFoto", item.optString("urlFoto", ""))
                .put("categoria", item.optString("categoria", ""))
                .put("statusValidacao", item.optString("statusValidacao", ""))
                .put("dataPostagem", item.optString("dataPostagem", ""));
    }

    // Em análise por FeedDemo.APROVACAO_MS; a aprovação sai na próxima requisição ou no temporizador.
    @NonNull
    private RespostaDemo registrar(@NonNull RequisicaoDemo r) {
        JSONObject corpo = r.corpoJson();

        if (corpo == null
                || corpo.optString("urlFoto", "").isEmpty()
                || corpo.optString("hashFoto", "").isEmpty()
                || !corpo.has("categoriaId")) {
            return erro(400, "REQUISICAO_INVALIDA");
        }

        JSONObject raiz = estado.raiz();

        if (corpo.optInt("condominioId", 0) != SementeDemo.CONDOMINIO_ID) {
            return erro(400, "CONDOMINIO_ID_INVALIDO");
        }

        JSONObject categoria = FeedDemo.categoria(raiz, corpo.optInt("categoriaId", 0));

        if (categoria == null) {
            return erro(404, "CATEGORIA_NAO_ENCONTRADA");
        }

        String hash = corpo.optString("hashFoto", "");

        if (FeedDemo.hashJaUsado(raiz, hash)) {
            return erro(409, "FOTO_JA_UTILIZADA");
        }

        try {
            JSONObject nova = FeedDemo.criar(raiz, categoria, corpo.optString("urlFoto", ""), hash, relogio.getAsLong());

            estado.salvar();

            return new RespostaDemo(201, visao(nova).toString());

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    // Um voto por morador, só em descarte de outro e só enquanto está em análise.
    @NonNull
    private RespostaDemo votar(int id, @NonNull RequisicaoDemo r) {
        JSONObject corpo = r.corpoJson();
        String tipo = corpo == null ? "" : corpo.optString("tipo", "");

        if (!"aprovar".equals(tipo) && !"denunciar".equals(tipo)) {
            return erro(400, "REQUISICAO_INVALIDA");
        }

        if ("denunciar".equals(tipo) && corpo.optInt("motivoDenunciaId", 0) <= 0) {
            return erro(400, "MOTIVO_DENUNCIA_OBRIGATORIO");
        }

        JSONObject postagem = FeedDemo.postagem(estado.raiz(), id);

        if (postagem == null) {
            return erro(404, "POSTAGEM_NAO_ENCONTRADA");
        }

        if (FeedDemo.doUsuario(postagem)) {
            return erro(403, "VOTO_NAO_PERMITIDO");
        }

        if (!FeedDemo.EM_ANALISE.equals(postagem.optString("statusValidacao"))) {
            return erro(409, "VOTACAO_ENCERRADA");
        }

        if (!postagem.optString("votoDoUsuario", "").isEmpty()) {
            return erro(409, "VOTO_JA_REGISTRADO");
        }

        try {
            postagem.put("votoDoUsuario", tipo);
            estado.salvar();

            return new RespostaDemo(201, new JSONObject().put("tipo", tipo).toString());

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    // Erro das rotas de postagem: { status, codigo, mensagem }, o que o PostagemJson lê.
    @NonNull
    private static RespostaDemo erro(int status, @NonNull String codigo) {
        try {
            return new RespostaDemo(
                    status,
                    new JSONObject()
                            .put("status", status)
                            .put("codigo", codigo)
                            .put("mensagem", codigo)
                            .toString());

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    private static int inteiro(@Nullable String texto, int padrao) {
        try {
            return texto == null ? padrao : Integer.parseInt(texto.trim());

        } catch (NumberFormatException erro) {
            return padrao;
        }
    }
}
