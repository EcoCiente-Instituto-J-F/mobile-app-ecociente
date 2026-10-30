package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.model.PostagemFeed;
import com.example.ecociente.util.FormatoDataApi;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Lê e monta o JSON de /api/postagens e /api/categorias-residuos da ds-mobile-api.
final class PostagemJson {

    private PostagemJson() {}

    // Envelope { total, pagina, tamanho, totalPaginas, postagens: [...] } do feed e de "minhas".
    @NonNull
    static List<PostagemFeed> postagens(@NonNull JSONObject envelope, long agoraMs) {

        List<PostagemFeed> lista = new ArrayList<>();

        JSONArray postagens = envelope.optJSONArray("postagens");

        for (int i = 0; postagens != null && i < postagens.length(); i++) {
            JSONObject item = postagens.optJSONObject(i);

            if (item == null) {
                continue;
            }

            lista.add(
                    new PostagemFeed(
                            item.optInt("id", i),
                            item.optString("autor", ""),
                            item.optString("urlFoto", ""),
                            item.optString("categoria", ""),
                            statusLegivel(item.optString("statusValidacao", "")),
                            FormatoDataApi.relativa(item.optString("dataPostagem", null), agoraMs)));
        }

        return lista;
    }

    // Categorias como itens de lista: título = nome, subtítulo = descrição, chave = id.
    @NonNull
    static List<ItemLista> categorias(@NonNull JSONArray categorias) {

        List<ItemLista> lista = new ArrayList<>();

        for (int i = 0; i < categorias.length(); i++) {
            JSONObject item = categorias.optJSONObject(i);

            if (item == null || item.optInt("id", 0) <= 0) {
                continue;
            }

            String descricao = item.isNull("descricao") ? "" : item.optString("descricao", "");

            lista.add(
                    new ItemLista(
                            String.valueOf(item.optInt("id")),
                            item.optString("nome", ""),
                            descricao,
                            String.valueOf(item.optInt("pontosBase", 0)),
                            null));
        }

        return lista;
    }

    @NonNull
    static String statusLegivel(@NonNull String status) {

        switch (status) {
            case "em_analise":
                return "Em análise";

            case "aprovada":
                return "Aprovada";

            case "reprovada":
                return "Reprovada";

            default:
                return "";
        }
    }

    // Corpo de PATCH /api/v1/postagens/{id}/decisao da ds-pontuacao-api.
    @NonNull
    static String corpoDecisao(boolean aprovar) {
        return "{\"aprovar\":" + aprovar + "}";
    }

    @NonNull
    static String mensagemDaDecisao(int codigoHttp) {

        switch (codigoHttp) {
            case 401:
                return "Sua sessão expirou. Entre novamente.";

            case 403:
                return "Você não pode decidir os descartes deste condomínio.";

            case 404:
                return "Este descarte não foi encontrado.";

            default:
                return "Não foi possível registrar a decisão. Tente de novo.";
        }
    }

    // Corpo de POST /api/postagens/{id}/votos: "aprovar", ou "denunciar" com o id do motivo (tb_lkp_motivos_denuncia).
    @NonNull
    static String corpoVoto(boolean aprovar, int motivoDenunciaId) {
        return aprovar
                ? "{\"tipo\":\"aprovar\"}"
                : "{\"tipo\":\"denunciar\",\"motivoDenunciaId\":" + motivoDenunciaId + "}";
    }

    // Mensagem para o morador a partir do código de erro da API ({ status, codigo, mensagem }).
    @NonNull
    static String mensagemDoVoto(int codigoHttp, @Nullable String corpo) {

        String codigo = "";

        try {
            if (corpo != null && !corpo.isEmpty()) {
                codigo = new JSONObject(corpo).optString("codigo", "");
            }
        } catch (JSONException erro) {
            // Resposta fora do formato esperado: cai na mensagem pelo código HTTP.
        }

        switch (codigo) {
            case "VOTO_NAO_PERMITIDO":
                return "Você não pode votar nesta postagem, por exemplo na sua própria.";

            case "VOTO_JA_REGISTRADO":
                return "Você já votou nesta postagem.";

            case "VOTACAO_ENCERRADA":
                return "A votação desta postagem já foi encerrada.";

            case "SEM_VINCULO_APROVADO":
                return "Você ainda não tem vínculo aprovado com o condomínio.";

            case "MOTIVO_DENUNCIA_OBRIGATORIO":
                return "Escolha o motivo da denúncia.";

            case "POSTAGEM_NAO_ENCONTRADA":
                return "Este descarte não existe mais.";

            default:
                return codigoHttp == 401
                        ? "Sua sessão expirou. Entre novamente."
                        : "Não foi possível registrar seu voto agora. Tente de novo.";
        }
    }

    @NonNull
    static String corpoCriar(
            int condominioId,
            int categoriaId,
            @NonNull String urlFoto,
            @NonNull String hashFoto,
            @NonNull String capturadaEmIso)
            throws JSONException {
        return new JSONObject()
                .put("urlFoto", urlFoto)
                .put("hashFoto", hashFoto)
                .put("capturadaEm", capturadaEmIso)
                .put("categoriaId", categoriaId)
                .put("condominioId", condominioId)
                .toString();
    }

    // Instante em UTC no formato ISO que a API lê como OffsetDateTime ("2026-10-22T13:00:00Z").
    @NonNull
    static String iso(long milissegundos) {

        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

        formato.setTimeZone(TimeZone.getTimeZone("UTC"));

        return formato.format(new Date(milissegundos));
    }

    // SHA-256 em hexadecimal: a API usa o hash para não aceitar a mesma foto duas vezes.
    @NonNull
    static String sha256(@NonNull byte[] dados) {

        try {
            byte[] resumo = MessageDigest.getInstance("SHA-256").digest(dados);

            StringBuilder hex = new StringBuilder(resumo.length * 2);

            for (byte b : resumo) {
                hex.append(String.format(Locale.ROOT, "%02x", b));
            }

            return hex.toString();

        } catch (NoSuchAlgorithmException erro) {
            throw new IllegalStateException(erro);
        }
    }

    // Mensagem para o morador a partir do código de erro da API ({ status, codigo, mensagem }).
    @NonNull
    static String mensagemDoErro(int codigoHttp, @Nullable String corpo) {

        String codigo = "";

        try {
            if (corpo != null && !corpo.isEmpty()) {
                codigo = new JSONObject(corpo).optString("codigo", "");
            }
        } catch (JSONException erro) {
            // Resposta fora do formato esperado: cai na mensagem pelo código HTTP.
        }

        switch (codigo) {
            case "FOTO_JA_UTILIZADA":
                return "Esta foto já foi usada em outro descarte. Tire uma foto nova.";

            case "SEM_VINCULO_APROVADO":
                return "Você ainda não tem vínculo aprovado com o condomínio.";

            case "TORRE_CONDOMINIO_INCONSISTENTE":
                return "A torre do seu cadastro não pertence ao condomínio. Fale com o síndico.";

            case "CATEGORIA_NAO_ENCONTRADA":
                return "Esse tipo de resíduo não está disponível. Escolha outro.";

            case "CONDOMINIO_ID_INVALIDO":
                return "Não encontramos o seu condomínio. Atualize o perfil e tente de novo.";

            case "CAPTURA_INVALIDA":
                return "A data da foto é inválida. Confira o horário do seu aparelho.";

            default:
                return codigoHttp == 401 || codigoHttp == 403
                        ? "Faça login com e-mail e senha para registrar o descarte."
                        : "Não foi possível registrar o descarte agora. Tente de novo.";
        }
    }
}
