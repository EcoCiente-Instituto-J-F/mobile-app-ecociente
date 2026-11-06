package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Categorias de resíduo e postagens (descartes) dentro do JSON do estado (`feed`). Quem chama já está sob `synchronized (estado)`; `aprovarVencidas` salva sozinho.
final class FeedDemo {

    // Quanto o descarte do morador fica "Em análise" antes de ser aprovado sozinho.
    static final long APROVACAO_MS = 20_000;

    // Fixo, qualquer que seja a categoria (o `pontosBase` da categoria é só informativo).
    static final int PONTOS_POR_DESCARTE = 50;

    static final String EM_ANALISE = "em_analise";
    static final String APROVADA = "aprovada";

    // Drawables do app; o applicationId do build demo é com.example.ecociente.demo.
    private static final String FOTO_PLASTICO = "android.resource://com.example.ecociente.demo/drawable/img_greenplastic";
    private static final String FOTO_PAPEL = "android.resource://com.example.ecociente.demo/drawable/img_greenpapers";
    private static final String FOTO_CONTAINER =
            "android.resource://com.example.ecociente.demo/drawable/img_greenrecyclecontainer";

    private static final long MINUTO = 60_000L;
    private static final long HORA = 60 * MINUTO;

    private FeedDemo() {}

    @NonNull
    static JSONObject semente(long agora) throws JSONException {
        JSONArray categorias =
                new JSONArray()
                        .put(categoria(1, "Plástico", "Garrafas, embalagens, sacolas e potes.", 10))
                        .put(categoria(2, "Papel", "Jornais, caixas de papelão, cadernos e folhas.", 8))
                        .put(categoria(3, "Vidro", "Garrafas, potes e frascos de vidro.", 12))
                        .put(categoria(4, "Metal", "Latas de alumínio e de aço, tampas e arames.", 15))
                        .put(categoria(5, "Orgânico", "Restos de comida, cascas e borra de café.", 5));

        // Do mais antigo ao mais novo. Nenhum é do morador; os três últimos seguem em análise (a tela do feed só mostra Aprovar/Denunciar nesses) e nunca são aprovados sozinhos.
        JSONArray postagens =
                new JSONArray()
                        .put(postagem(1, "Letícia Prado", "Plástico", 1, FOTO_PLASTICO, APROVADA, agora - 50 * HORA))
                        .put(postagem(2, "Thiago Barros", "Metal", 4, FOTO_CONTAINER, APROVADA, agora - 26 * HORA))
                        .put(postagem(3, "Camila Duarte", "Papel", 2, FOTO_PAPEL, APROVADA, agora - 5 * HORA))
                        .put(postagem(4, "Rafael Mendes", "Plástico", 1, FOTO_PLASTICO, APROVADA, agora - 2 * HORA))
                        .put(postagem(5, "Camila Duarte", "Papel", 2, FOTO_PAPEL, EM_ANALISE, agora - 20 * MINUTO))
                        .put(postagem(6, "Thiago Barros", "Plástico", 1, FOTO_PLASTICO, EM_ANALISE, agora - 45 * MINUTO))
                        .put(postagem(7, "Rafael Mendes", "Metal", 4, FOTO_CONTAINER, EM_ANALISE, agora - 90 * MINUTO));

        return new JSONObject().put("categorias", categorias).put("postagens", postagens).put("proximoId", 8);
    }

    private static JSONObject categoria(int id, String nome, String descricao, int pontosBase) throws JSONException {
        return new JSONObject()
                .put("id", id)
                .put("nome", nome)
                .put("descricao", descricao)
                .put("pontosBase", pontosBase);
    }

    private static JSONObject postagem(
            int id, String autor, String categoria, int categoriaId, String urlFoto, String status, long criadoEm)
            throws JSONException {
        return new JSONObject()
                .put("id", id)
                .put("autorId", 0)
                .put("autor", autor)
                .put("urlFoto", urlFoto)
                .put("hashFoto", "semente-" + id)
                .put("categoria", categoria)
                .put("categoriaId", categoriaId)
                .put("statusValidacao", status)
                .put("criadoEm", criadoEm)
                .put("dataPostagem", SementeDemo.formatarData(criadoEm));
    }

    @NonNull
    private static JSONObject bloco(@NonNull JSONObject raiz) {
        JSONObject bloco = raiz.optJSONObject("feed");

        if (bloco == null) {
            throw new IllegalStateException("Estado da demo sem feed");
        }

        return bloco;
    }

    @NonNull
    static JSONArray categorias(@NonNull JSONObject raiz) {
        JSONArray lista = bloco(raiz).optJSONArray("categorias");

        return lista == null ? new JSONArray() : lista;
    }

    @Nullable
    static JSONObject categoria(@NonNull JSONObject raiz, int id) {
        JSONArray lista = categorias(raiz);

        for (int i = 0; i < lista.length(); i++) {
            JSONObject item = lista.optJSONObject(i);

            if (item != null && item.optInt("id", 0) == id) {
                return item;
            }
        }

        return null;
    }

    // Em ordem de criação (a posição na lista).
    @NonNull
    static List<JSONObject> postagens(@NonNull JSONObject raiz) {
        JSONArray lista = bloco(raiz).optJSONArray("postagens");
        List<JSONObject> saida = new ArrayList<>();

        for (int i = 0; lista != null && i < lista.length(); i++) {
            JSONObject item = lista.optJSONObject(i);

            if (item != null) {
                saida.add(item);
            }
        }

        return saida;
    }

    @Nullable
    static JSONObject postagem(@NonNull JSONObject raiz, int id) {
        for (JSONObject item : postagens(raiz)) {
            if (item.optInt("id", 0) == id) {
                return item;
            }
        }

        return null;
    }

    static boolean doUsuario(@NonNull JSONObject raiz, @NonNull JSONObject postagem) {
        return postagem.optInt("autorId", 0) == UsuariosMultiperfilDemo.usuarioId(raiz);
    }

    static boolean hashJaUsado(@NonNull JSONObject raiz, @NonNull String hash) {
        for (JSONObject item : postagens(raiz)) {
            if (hash.equals(item.optString("hashFoto", null))) {
                return true;
            }
        }

        return false;
    }

    // Novo descarte do morador, em análise. Quem chama salva o estado.
    @NonNull
    static JSONObject criar(
            @NonNull JSONObject raiz,
            @NonNull JSONObject categoria,
            @NonNull String urlFoto,
            @NonNull String hash,
            long agora) {
        try {
            JSONObject bloco = bloco(raiz);
            int id = bloco.optInt("proximoId", 1);

            JSONObject nova =
                    postagem(
                                    id,
                                    ContaDemo.me(raiz).optString("nome", SementeDemo.NOME),
                                    categoria.optString("nome", ""),
                                    categoria.optInt("id", 0),
                                    urlFoto,
                                    EM_ANALISE,
                                    agora)
                            .put("autorId", UsuariosMultiperfilDemo.usuarioId(raiz))
                            .put("hashFoto", hash);

            bloco.getJSONArray("postagens").put(nova);
            bloco.put("proximoId", id + 1);

            return nova;

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    // Aprova os descartes do morador em análise há APROVACAO_MS ou mais, na ordem em que foram feitos: cada um credita os pontos e vira "aprovada", com uma gravação do estado para cada passo. Só mexe nos do morador (autorId igual ao da conta); os de outros ficam em análise. Devolve, para cada aprovação, se o morador subiu no ranking.
    @NonNull
    static List<Boolean> aprovarVencidas(@NonNull EstadoDemo estado, long agora) {
        List<Boolean> subidas = new ArrayList<>();

        try {
            for (JSONObject item : postagens(estado.raiz())) {
                if (!doUsuario(estado.raiz(), item)
                        || !EM_ANALISE.equals(item.optString("statusValidacao"))
                        || agora - item.optLong("criadoEm", agora) < APROVACAO_MS) {
                    continue;
                }

                int antes = estado.posicaoDoUsuario();

                // Primeiro os pontos: se falhar, o descarte continua em análise em vez de ficar aprovado sem pontos.
                estado.creditar("POSTAGEM", PONTOS_POR_DESCARTE);
                item.put("statusValidacao", APROVADA);
                estado.salvar();

                int depois = estado.posicaoDoUsuario();

                subidas.add(antes > 0 && depois > 0 && depois < antes);
            }

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }

        return subidas;
    }
}
