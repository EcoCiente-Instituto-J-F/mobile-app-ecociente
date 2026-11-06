package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.LongSupplier;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// ds-mobile-api, agendamentos: GET /api/v1/agendamentos (com `status`, `dataInicio`, `dataFim`, `page`, `size`) e GET /api/v1/agendamentos/proxima.
final class RotasCalendario implements RotasDemo {

    private static final String HOST = "ds-mobile-api.onrender.com";
    private static final String CAMINHO = "/api/v1/agendamentos";

    // Datas são "yyyy-MM-dd'T'HH:mm:ss" (hora local): na mesma forma, comparar o texto é comparar o tempo.
    private static final int TAMANHO_DATA = 19;

    private final EstadoDemo estado;
    private final LongSupplier relogio;

    RotasCalendario(@NonNull EstadoDemo estado, @NonNull LongSupplier relogio) {
        this.estado = estado;
        this.relogio = relogio;
    }

    @Nullable
    @Override
    public RespostaDemo tratar(@NonNull RequisicaoDemo r) {
        if (!HOST.equals(r.host) || !(r.e("GET", CAMINHO) || r.e("GET", CAMINHO + "/proxima"))) {
            return null;
        }

        if (!ContaDemo.tokenConfere(estado.raiz(), r.autorizacao)) {
            return RespostaDemo.erro(401, "NAO_AUTORIZADO");
        }

        return r.caminho.equals(CAMINHO) ? listar(r) : proxima();
    }

    // Mais recente primeiro, como a API real.
    @NonNull
    private RespostaDemo listar(@NonNull RequisicaoDemo r) {
        String status = r.query.get("status");
        String de = recortar(r.query.get("dataInicio"));
        String ate = recortar(r.query.get("dataFim"));

        List<JSONObject> itens = new ArrayList<>();

        for (JSONObject item : agendamentos()) {
            String inicio = item.optString("dataInicio", "");

            if ((status != null && !status.isEmpty() && !status.equalsIgnoreCase(item.optString("statusAgendamento")))
                    || (de != null && inicio.compareTo(de) < 0)
                    || (ate != null && inicio.compareTo(ate) > 0)) {
                continue;
            }

            itens.add(item);
        }

        int pagina = Math.max(0, inteiro(r.query.get("page"), 0));
        int tamanho = inteiro(r.query.get("size"), 10);

        if (tamanho <= 0) {
            tamanho = 10;
        }

        JSONArray conteudo = new JSONArray();

        for (int i = pagina * tamanho; i < Math.min(itens.size(), (pagina + 1) * tamanho); i++) {
            conteudo.put(itens.get(i));
        }

        try {
            return new RespostaDemo(
                    200,
                    new JSONObject()
                            .put("content", conteudo)
                            .put("last", (long) (pagina + 1) * tamanho >= itens.size())
                            .put("totalElements", itens.size())
                            .put("totalPages", Math.max(1, (itens.size() + tamanho - 1) / tamanho))
                            .toString());

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    // O primeiro agendamento ainda por vir, AGENDADO ou CONFIRMADO. O repositório trata o 404 como "nenhum".
    @NonNull
    private RespostaDemo proxima() {
        String agora = SementeDemo.formatarData(relogio.getAsLong());
        JSONObject proximo = null;

        for (JSONObject item : agendamentos()) {
            String status = item.optString("statusAgendamento");

            if (!("AGENDADO".equals(status) || "CONFIRMADO".equals(status))
                    || item.optString("dataInicio", "").compareTo(agora) <= 0) {
                continue;
            }

            if (proximo == null || item.optString("dataInicio").compareTo(proximo.optString("dataInicio")) < 0) {
                proximo = item;
            }
        }

        return proximo == null
                ? RespostaDemo.erro(404, "AGENDAMENTO_NAO_ENCONTRADO")
                : new RespostaDemo(200, proximo.toString());
    }

    @NonNull
    private List<JSONObject> agendamentos() {
        JSONArray lista = estado.raiz().optJSONArray("agendamentos");
        List<JSONObject> itens = new ArrayList<>();

        for (int i = 0; lista != null && i < lista.length(); i++) {
            JSONObject item = lista.optJSONObject(i);

            if (item != null) {
                itens.add(item);
            }
        }

        Collections.sort(
                itens,
                new Comparator<JSONObject>() {
                    @Override
                    public int compare(JSONObject a, JSONObject b) {
                        return b.optString("dataInicio", "").compareTo(a.optString("dataInicio", ""));
                    }
                });

        return itens;
    }

    @Nullable
    private static String recortar(@Nullable String data) {
        if (data == null || data.length() < TAMANHO_DATA) {
            return null;
        }

        return data.substring(0, TAMANHO_DATA);
    }

    private static int inteiro(@Nullable String texto, int padrao) {
        try {
            return texto == null ? padrao : Integer.parseInt(texto.trim());

        } catch (NumberFormatException erro) {
            return padrao;
        }
    }
}
