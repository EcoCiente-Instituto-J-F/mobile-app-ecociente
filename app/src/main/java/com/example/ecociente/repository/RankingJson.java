package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.model.SindicoPosicaoRanking;
import com.example.ecociente.util.Iniciais;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

// Converte as respostas de /api/v1/rankings da ds-pontuacao-api nas listas que as telas exibem.
final class RankingJson {

    private RankingJson() {}

    @NonNull
    static List<SindicoPosicaoRanking> torres(@NonNull JSONObject resposta) {
        return posicoes(resposta);
    }

    // Torres e moradores vêm no mesmo formato: "ranking": [{ nome, pontos, ... }].
    @NonNull
    static List<SindicoPosicaoRanking> posicoes(@NonNull JSONObject resposta) {

        List<SindicoPosicaoRanking> posicoes = new ArrayList<>();

        JSONArray ranking = resposta.optJSONArray("ranking");

        for (int i = 0; ranking != null && i < ranking.length(); i++) {
            JSONObject item = ranking.optJSONObject(i);

            if (item == null) {
                continue;
            }

            String nome = item.optString("nome", "");

            posicoes.add(
                    new SindicoPosicaoRanking(
                            Iniciais.de(nome), nome, (int) item.optLong("pontos", 0), false));
        }

        return posicoes;
    }

    // O ranking só traz as torres com pontos no limite pedido; a torre que ficou de fora é identificada pelo nome.
    static boolean contem(@NonNull List<SindicoPosicaoRanking> ranking, @NonNull String nome) {

        for (SindicoPosicaoRanking posicao : ranking) {
            if (posicao.getNome().trim().equalsIgnoreCase(nome.trim())) {
                return true;
            }
        }

        return false;
    }

    // Resposta de GET /rankings/torres/{id}/posicao como entrada do ranking.
    @NonNull
    static SindicoPosicaoRanking torreDaPosicao(@NonNull JSONObject resposta, @NonNull String nome) {
        return new SindicoPosicaoRanking(Iniciais.de(nome), nome, (int) resposta.optLong("pontos", 0), false);
    }

    @NonNull
    static List<ItemLista> moradores(@NonNull JSONObject resposta) {

        List<ItemLista> moradores = new ArrayList<>();

        JSONArray ranking = resposta.optJSONArray("ranking");

        for (int i = 0; ranking != null && i < ranking.length(); i++) {
            JSONObject item = ranking.optJSONObject(i);

            if (item == null) {
                continue;
            }

            moradores.add(
                    new ItemLista(
                            item.optInt("posicao", i + 1) + "º  " + item.optString("nome", ""),
                            "",
                            NumberFormat.getIntegerInstance(new Locale("pt", "BR"))
                                            .format(item.optLong("pontos", 0))
                                    + " pts"));
        }

        return moradores;
    }
}
