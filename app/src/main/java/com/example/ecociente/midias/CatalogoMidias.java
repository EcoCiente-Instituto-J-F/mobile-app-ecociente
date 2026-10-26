package com.example.ecociente.midias;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Manifesto publico pequeno. Nao varre pastas remotas e nao usa credenciais administrativas. */
public final class CatalogoMidias {
    public final List<Midia> itens = new ArrayList<>();
    public final Map<String, List<String>> sinonimos = new LinkedHashMap<>();
    public final Map<String, String> associacoes = new LinkedHashMap<>();
    public String revisao = "local";
    private static final Set<String> TIPOS = new HashSet<>(Arrays.asList(
            Midia.ICONE_CONTEUDO, Midia.FOTO_CONTEUDO, Midia.DECORACAO_CONTEUDO,
            Midia.EKO_LEITURA, Midia.ICONE_QUIZ, Midia.CAPA_QUIZ, Midia.DECORACAO_QUIZ));

    public static CatalogoMidias carregar(Context contexto) {
        CatalogoMidias catalogo = new CatalogoMidias();
        try (InputStream entrada = contexto.getAssets().open("catalogo_midias.json");
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] bloco = new byte[4096];
            int lidos;
            while ((lidos = entrada.read(bloco)) != -1) {
                if (buffer.size() + lidos > 4 * 1024 * 1024) throw new IllegalArgumentException("Catalogo muito grande");
                buffer.write(bloco, 0, lidos);
            }
            JSONObject raiz = new JSONObject(buffer.toString(StandardCharsets.UTF_8.name()));
            if (raiz.optInt("versao_schema") != 1) throw new IllegalArgumentException("Schema de midias incompativel");
            catalogo.revisao = raiz.optString("revisao", "local");
            JSONObject sin = raiz.optJSONObject("sinonimos");
            if (sin != null) {
                List<String> chaves = new ArrayList<>();
                Iterator<String> it = sin.keys();
                while (it.hasNext()) chaves.add(it.next());
                java.util.Collections.sort(chaves);
                for (String chave : chaves) catalogo.sinonimos.put(chave, strings(sin.optJSONArray(chave)));
            }
            JSONArray midias = raiz.optJSONArray("midias");
            Set<String> ids = new HashSet<>();
            for (int i = 0; midias != null && i < midias.length(); i++) {
                JSONObject j = midias.optJSONObject(i);
                if (j == null || !j.optBoolean("ativo", true)) continue;
                try {
                    Midia m = lerItem(j);
                    if (TIPOS.contains(m.tipo) && ids.add(m.id) && disponivel(contexto, m)) catalogo.itens.add(m);
                } catch (Exception e) { Log.w("CatalogoMidias", "Item de midia ignorado: formato invalido"); }
            }
            JSONObject assoc = raiz.optJSONObject("associacoes");
            if (assoc != null) {
                Iterator<String> it = assoc.keys();
                while (it.hasNext()) {
                    String chave = it.next();
                    catalogo.associacoes.put(chave, assoc.optString(chave));
                }
            }
        } catch (Exception erro) {
            Log.w("CatalogoMidias", "Catalogo indisponivel: usando artes locais de seguranca");
        }
        // Garantem mascotinho e decoracao generica mesmo com JSON ausente/invalido.
        adicionarPadrao(catalogo, "interno_eko", Midia.EKO_LEITURA, ConfiguracaoMidias.EKO_PADRAO, true);
        adicionarPadrao(catalogo, "interno_quiz", Midia.DECORACAO_QUIZ, ConfiguracaoMidias.DECORACAO_QUIZ_PADRAO, false);
        return catalogo;
    }

    private static void adicionarPadrao(CatalogoMidias c, String id, String tipo, String recurso, boolean eko) {
        for (Midia m : c.itens) if (id.equals(m.id)) return;
        c.itens.add(new Midia(id, tipo, "geral", "drawable", recurso, "png", recurso,
                eko ? "Eko" : "Planeta ilustrativo", "", 1, false, eko, true, new ArrayList<>()));
    }

    private static Midia lerItem(JSONObject j) throws Exception {
        String id = j.getString("id").trim(), caminho = j.getString("caminho").trim();
        if (id.isEmpty() || caminho.isEmpty()) throw new IllegalArgumentException("ID/caminho ausente");
        return new Midia(id, j.getString("tipo"), j.optString("tema", "geral"),
                j.getString("origem"), caminho, j.optString("formato", "png"),
                j.optString("grupo_imagem", ""), j.optString("descricao", ""),
                j.optString("cloud_name", ConfiguracaoMidias.CLOUD_NAME), j.optLong("versao", 1),
                j.optBoolean("animada", false), j.optBoolean("eko", false),
                j.optBoolean("neutra", false), strings(j.optJSONArray("tags")));
    }

    private static List<String> strings(JSONArray array) {
        List<String> lista = new ArrayList<>();
        for (int i = 0; array != null && i < array.length(); i++) lista.add(array.optString(i));
        return lista;
    }

    private static boolean disponivel(Context contexto, Midia m) {
        if ("drawable".equals(m.origem)) {
            return m.caminho.matches("[a-z][a-z0-9_]*") && contexto.getResources()
                    .getIdentifier(m.caminho, "drawable", contexto.getPackageName()) != 0;
        }
        if ("asset".equals(m.origem)) {
            if (!m.caminho.startsWith("midias/") || m.caminho.contains("..") || m.caminho.contains(":")) return false;
            // Nao abre milhares de arquivos na thread da tela; o carregador valida ao usar.
            return true;
        }
        if ("cloudinary".equals(m.origem)) {
            return m.cloudName.matches("[A-Za-z0-9_-]+") && !m.caminho.startsWith("/")
                    && !m.caminho.contains("..") && !m.caminho.contains(":");
        }
        return false;
    }
}
