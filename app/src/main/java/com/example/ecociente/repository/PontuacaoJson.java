package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import com.example.ecociente.model.MovimentacaoPontos;
import com.example.ecociente.model.PosicaoNoRanking;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

// Converte as respostas de /api/v1/pontuacoes e /api/v1/rankings/moradores/{id}/posicao.
final class PontuacaoJson {

    private PontuacaoJson() {}

    static long saldo(@NonNull JSONObject resposta) {
        return resposta.optLong("saldo", resposta.optLong("pontos", 0));
    }

    @NonNull
    static List<MovimentacaoPontos> movimentacoes(@NonNull JSONObject pagina) {

        List<MovimentacaoPontos> lista = new ArrayList<>();

        JSONArray conteudo = pagina.optJSONArray("conteudo");

        for (int i = 0; conteudo != null && i < conteudo.length(); i++) {
            JSONObject item = conteudo.optJSONObject(i);

            if (item == null) {
                continue;
            }

            lista.add(
                    new MovimentacaoPontos(
                            origem(item.optString("origemTipo", "")),
                            tipo(item.optString("tipoMovimentacao", "")),
                            item.optInt("pontos", 0),
                            item.optString("ocorridoEm", "")));
        }

        return lista;
    }

    static boolean ultimaPagina(@NonNull JSONObject pagina) {
        return pagina.optInt("pagina", 0) + 1 >= pagina.optInt("totalPaginas", 1);
    }

    @NonNull
    static PosicaoNoRanking posicao(@NonNull JSONObject resposta) {
        return new PosicaoNoRanking(
                resposta.optLong("posicao", 0),
                resposta.optLong("pontos", 0),
                resposta.optLong("totalParticipantes", 0));
    }

    @NonNull
    private static MovimentacaoPontos.Origem origem(@NonNull String valor) {
        switch (valor) {
            case "POSTAGEM":
                return MovimentacaoPontos.Origem.POSTAGEM;
            case "QUIZ":
                return MovimentacaoPontos.Origem.QUIZ;
            default:
                return MovimentacaoPontos.Origem.OUTRA;
        }
    }

    @NonNull
    private static MovimentacaoPontos.Tipo tipo(@NonNull String valor) {
        switch (valor) {
            case "ESTORNO":
                return MovimentacaoPontos.Tipo.ESTORNO;
            case "RESTAURACAO":
                return MovimentacaoPontos.Tipo.RESTAURACAO;
            default:
                return MovimentacaoPontos.Tipo.CREDITO;
        }
    }
}
