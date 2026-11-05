package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import com.example.ecociente.model.CondominioAtendido;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/** Conversor da resposta paginada de GET /api/cooperativas/{id}/condominios. */
final class CondominiosAtendidosJson {
    private CondominiosAtendidosJson() {}

    @NonNull
    static List<CondominioAtendido> ler(@NonNull JSONObject resposta) {
        List<CondominioAtendido> lista = new ArrayList<>();
        JSONArray itens = resposta.optJSONArray("condominios");
        for (int i = 0; itens != null && i < itens.length(); i++) {
            JSONObject item = itens.optJSONObject(i);
            if (item == null || item.optInt("id", 0) <= 0) continue;

            JSONObject endereco = item.optJSONObject("endereco");
            String enderecoCompleto = enderecoCompleto(endereco);
            JSONArray diasJson = item.optJSONArray("diasColeta");
            List<String> dias = new ArrayList<>();
            for (int j = 0; diasJson != null && j < diasJson.length(); j++) {
                String dia = diasJson.optString(j, "").trim();
                if (!dia.isEmpty() && !dias.contains(dia)) dias.add(dia);
            }
            lista.add(new CondominioAtendido(
                    item.optInt("id"),
                    item.optString("nome", "").trim(),
                    enderecoCompleto,
                    item.optLong("totalTorres", 0),
                    dias));
        }
        return lista;
    }

    @NonNull
    private static String enderecoCompleto(JSONObject e) {
        if (e == null) return "";
        List<String> partes = new ArrayList<>();
        adicionar(partes, e.optString("logradouro", ""));
        adicionar(partes, e.optString("numero", ""));
        adicionar(partes, e.optString("complemento", ""));
        adicionar(partes, e.optString("cidade", ""));
        adicionar(partes, e.optString("estado", ""));
        adicionar(partes, e.optString("cep", ""));
        return String.join(", ", partes);
    }

    private static void adicionar(List<String> partes, String valor) {
        String limpo = valor == null ? "" : valor.trim();
        if (!limpo.isEmpty() && !limpo.equalsIgnoreCase("null")) partes.add(limpo);
    }
}
