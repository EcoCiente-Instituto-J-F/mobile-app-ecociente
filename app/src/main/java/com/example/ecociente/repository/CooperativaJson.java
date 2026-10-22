package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import com.example.ecociente.model.SindicoCooperativa;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

// Lê GET /api/cooperativas da ds-mobile-api: { total, pagina, tamanho, totalPaginas, cooperativas }.
final class CooperativaJson {

    private CooperativaJson() {}

    @NonNull
    static List<SindicoCooperativa> cooperativas(@NonNull JSONObject envelope) {

        List<SindicoCooperativa> lista = new ArrayList<>();

        JSONArray itens = envelope.optJSONArray("cooperativas");

        for (int i = 0; itens != null && i < itens.length(); i++) {
            JSONObject item = itens.optJSONObject(i);

            if (item == null || item.optInt("id", 0) <= 0) {
                continue;
            }

            String endereco = juntar(" ", texto(item, "logradouro"), texto(item, "numero"));

            lista.add(
                    new SindicoCooperativa(
                            item.optInt("id"),
                            texto(item, "nome"),
                            media(item),
                            item.optInt("totalAvaliacoes", 0),
                            endereco,
                            texto(item, "cidade"),
                            texto(item, "estado"),
                            texto(item, "cep"),
                            texto(item, "enderecoCompleto")));
        }

        return lista;
    }

    // "4,5" a partir de 4.5; vazio quando a cooperativa ainda não foi avaliada.
    @NonNull
    private static String media(@NonNull JSONObject item) {

        if (item.isNull("mediaAvaliacoes")) {
            return "";
        }

        return String.format(new Locale("pt", "BR"), "%.1f", item.optDouble("mediaAvaliacoes", 0));
    }

    @NonNull
    private static String texto(@NonNull JSONObject objeto, @NonNull String campo) {
        return objeto.isNull(campo) ? "" : objeto.optString(campo, "").trim();
    }

    @NonNull
    private static String juntar(@NonNull String separador, @NonNull String... partes) {

        StringBuilder texto = new StringBuilder();

        for (String parte : partes) {
            if (!parte.isEmpty()) {
                if (texto.length() > 0) {
                    texto.append(separador);
                }

                texto.append(parte);
            }
        }

        return texto.toString();
    }
}
