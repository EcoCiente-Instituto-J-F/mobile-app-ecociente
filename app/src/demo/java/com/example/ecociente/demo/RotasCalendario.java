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
        if (!HOST.equals(r.host)) return null;
        boolean listar = r.e("GET", CAMINHO);
        boolean proxima = r.e("GET", CAMINHO + "/proxima");
        boolean criar = r.e("POST", CAMINHO);
        java.util.regex.Matcher status = java.util.regex.Pattern
                .compile("^/api/v1/agendamentos/(\\d+)/status$").matcher(r.caminho);
        boolean alterar = "PATCH".equals(r.metodo) && status.matches();
        if (!listar && !proxima && !criar && !alterar) return null;

        if (!ContaDemo.tokenConfere(estado.raiz(), r.autorizacao)) {
            return RespostaDemo.erro(401, "NAO_AUTORIZADO");
        }

        try {
            if (criar) return criar(r);
            if (alterar) return alterar(Integer.parseInt(status.group(1)), r);
        } catch (JSONException erro) { throw new IllegalStateException(erro); }
        return listar ? listar(r) : proxima();
    }


    @NonNull
    private RespostaDemo criar(@NonNull RequisicaoDemo r) throws JSONException {
        String perfil = ContaDemo.me(estado.raiz()).optString("perfil", "");
        if (!"SINDICO".equals(perfil)) return RespostaDemo.erro(403, "PERFIL_NAO_PERMITIDO");
        JSONObject entrada = r.corpoJson();
        if (entrada == null) return RespostaDemo.erro(400, "REQUISICAO_INVALIDA");
        int condominio = entrada.optInt("condominioId", 0);
        int cooperativa = entrada.optInt("cooperativaId", 0);
        String data = entrada.optString("dataInicio", "");
        if (data.length() < 19 || condominio != 7 || cooperativa <= 0)
            return RespostaDemo.erro(400, "AGENDAMENTO_INVALIDO");
        String inicio = data.substring(0, 19);
        if (inicio.compareTo(SementeDemo.formatarData(relogio.getAsLong())) <= 0)
            return RespostaDemo.erro(400, "DATA_NO_PASSADO");
        JSONArray todos = estado.raiz().getJSONArray("agendamentos");
        int id = 1;
        for (int i = 0; i < todos.length(); i++) id = Math.max(id, todos.getJSONObject(i).optInt("id", 0) + 1);
        JSONObject item = new JSONObject().put("id", id).put("condominioId", condominio)
                .put("condominioNome", "Condominio Jardim das Palmeiras")
                .put("cooperativaId", cooperativa)
                .put("cooperativaNome", "Cooperativa EcoCiente")
                .put("dataInicio", inicio)
                .put("dataFim", inicio).put("statusAgendamento", "AGENDADO")
                .put("status", "AGENDADO").put("possuiRecorrencia", false)
                .put("origemSolicitacao", "CONDOMINIO");
        todos.put(item);
        estado.salvar();
        return new RespostaDemo(201, item.toString());
    }

    @NonNull
    private RespostaDemo alterar(int id, @NonNull RequisicaoDemo r) throws JSONException {
        String perfil = ContaDemo.me(estado.raiz()).optString("perfil", "");
        if (!"COOPERATIVA".equals(perfil) && !"SINDICO".equals(perfil))
            return RespostaDemo.erro(403, "PERFIL_NAO_PERMITIDO");
        JSONObject pedido = r.corpoJson();
        String novo = pedido == null ? "" : pedido.optString("status", "").toUpperCase(java.util.Locale.ROOT);
        if (!("CONFIRMADO".equals(novo) || "RECUSADO".equals(novo) || "CANCELADO".equals(novo) || "REALIZADO".equals(novo)))
            return RespostaDemo.erro(400, "STATUS_INVALIDO");
        JSONArray todos = estado.raiz().getJSONArray("agendamentos");
        for (int i = 0; i < todos.length(); i++) {
            JSONObject item = todos.getJSONObject(i);
            if (item.optInt("id", 0) != id) continue;
            String atual = item.optString("statusAgendamento", "AGENDADO");
            if (atual.equals(novo)) return new RespostaDemo(204, "");
            if ("RECUSADO".equals(atual) || "CANCELADO".equals(atual))
                return RespostaDemo.erro(409, "SOLICITACAO_FINALIZADA");
            item.put("statusAgendamento", novo).put("status", novo);
            estado.salvar();
            return new RespostaDemo(204, "");
        }
        return RespostaDemo.erro(404, "AGENDAMENTO_NAO_ENCONTRADO");
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
