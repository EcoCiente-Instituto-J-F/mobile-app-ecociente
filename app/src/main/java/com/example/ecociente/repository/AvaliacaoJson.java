package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import com.example.ecociente.model.AvaliacoesCooperativa;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.ui.FormatoDataApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

// Lê GET /api/cooperativas/{id}/avaliacoes da ds-mobile-api.
final class AvaliacaoJson {

    private static final Locale PT_BR = new Locale("pt", "BR");

    private AvaliacaoJson() {}

    @NonNull
    static AvaliacoesCooperativa avaliacoes(@NonNull JSONObject resposta) {

        List<ItemLista> itens = new ArrayList<>();

        JSONArray avaliacoes = resposta.optJSONArray("avaliacoes");

        for (int i = 0; avaliacoes != null && i < avaliacoes.length(); i++) {
            JSONObject avaliacao = avaliacoes.optJSONObject(i);

            if (avaliacao == null) {
                continue;
            }

            String condominio = avaliacao.optString("nomeCondominio", "");
            String comentario = avaliacao.optString("comentario", "").trim();
            String nota = nota(avaliacao.optInt("nota", 0));
            String data = FormatoDataApi.data(avaliacao.optString("avaliadoEm", null));

            itens.add(
                    new ItemLista(
                            "avaliacao:" + avaliacao.optInt("id", i),
                            condominio,
                            comentario.isEmpty() ? "" : "“" + comentario + "”",
                            nota,
                            new String[][] {
                                {"Condomínio", condominio},
                                {"Nota", nota},
                                {"Comentário", comentario.isEmpty() ? "—" : comentario},
                                {"Data", data}
                            }));
        }

        return new AvaliacoesCooperativa(resposta.optDouble("media", 0), resposta.optLong("total", itens.size()), itens);
    }

    // "5,0": a nota inteira da API no formato da lista.
    @NonNull
    static String nota(double nota) {
        return String.format(PT_BR, "%.1f", nota);
    }
}
