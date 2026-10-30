package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.Conversa;
import com.example.ecociente.model.MensagemConversa;
import com.example.ecociente.util.FormatoDataApi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Leitura e escrita do JSON das conversas entre condomínio e cooperativa (ds-mobile-api, /api/conversas).
final class ConversaJson {

    static final String LADO_CONDOMINIO = "CONDOMINIO";
    static final String LADO_COOPERATIVA = "COOPERATIVA";

    private ConversaJson() {}

    // Página de GET /api/conversas. O nome mostrado é o do outro lado: quem é do condomínio vê o nome
    // da cooperativa, e vice-versa.
    @NonNull
    static List<Conversa> conversas(@NonNull JSONObject pagina, boolean souCondominio) {

        List<Conversa> conversas = new ArrayList<>();

        JSONArray itens = pagina.optJSONArray("itens");

        for (int i = 0; itens != null && i < itens.length(); i++) {
            JSONObject item = itens.optJSONObject(i);

            if (item == null || item.optInt("id", 0) <= 0) {
                continue;
            }

            conversas.add(conversa(item, souCondominio));
        }

        return conversas;
    }

    // Resposta de POST /api/conversas (criar ou obter).
    @NonNull
    static Conversa conversa(@NonNull JSONObject item, boolean souCondominio) {

        String nome = texto(item, souCondominio ? "nomeCooperativa" : "nomeCondominio");

        return new Conversa(
                item.optInt("id"),
                item.optInt("condominioId"),
                item.optInt("cooperativaId"),
                nome,
                texto(item, "ultimaMensagemEm"),
                (int) item.optLong("naoLidas", 0),
                !item.optBoolean("ativa", true));
    }

    // Página de GET /api/conversas/{id}/mensagens. A API devolve da mais nova para a mais antiga; a
    // lista sai na ordem de leitura (a mais antiga primeiro). `meuLado` marca as mensagens enviadas.
    @NonNull
    static List<MensagemConversa> mensagens(@NonNull JSONObject pagina, @NonNull String meuLado) {

        List<MensagemConversa> mensagens = new ArrayList<>();

        JSONArray itens = pagina.optJSONArray("itens");

        for (int i = 0; itens != null && i < itens.length(); i++) {
            JSONObject item = itens.optJSONObject(i);

            if (item != null) {
                mensagens.add(mensagem(item, meuLado));
            }
        }

        Collections.reverse(mensagens);

        return mensagens;
    }

    // Resposta de POST /api/conversas/{id}/mensagens.
    @NonNull
    static MensagemConversa mensagem(@NonNull JSONObject item, @NonNull String meuLado) {

        return new MensagemConversa(
                texto(item, "conteudo"),
                FormatoDataApi.horaCurta(texto(item, "enviadaEm")),
                meuLado.equals(texto(item, "remetenteTipo")));
    }

    @NonNull
    static String corpoCriar(int condominioId, int cooperativaId) throws JSONException {
        return new JSONObject().put("condominioId", condominioId).put("cooperativaId", cooperativaId).toString();
    }

    @NonNull
    static String corpoEnviar(@NonNull String conteudo) throws JSONException {
        return new JSONObject().put("conteudo", conteudo).toString();
    }

    // Só a cooperativa pode abrir a conversa: enquanto não há mensagem, o condomínio não escreve.
    static boolean podeEscrever(@NonNull String meuLado, int quantidadeDeMensagens) {
        return !LADO_CONDOMINIO.equals(meuLado) || quantidadeDeMensagens > 0;
    }

    // Texto do campo, vazio quando ausente ou null (o optString devolveria a palavra "null").
    @NonNull
    private static String texto(@NonNull JSONObject objeto, @NonNull String campo) {
        return objeto.isNull(campo) ? "" : objeto.optString(campo, "");
    }

    // Mensagem de erro para quem tentou enviar, segundo o status que a API devolveu.
    @NonNull
    static String mensagemDoErroDeEnvio(int codigo, @Nullable String corpo) {

        if (codigo == 400) {
            return "A mensagem deve ter entre 1 e 2000 caracteres.";
        }

        if (codigo == 403) {
            return "Você não faz parte desta conversa.";
        }

        if (codigo == 409) {
            return "Esta conversa está arquivada. Desarquive para enviar.";
        }

        return "Não foi possível enviar a mensagem. Tente de novo.";
    }
}
