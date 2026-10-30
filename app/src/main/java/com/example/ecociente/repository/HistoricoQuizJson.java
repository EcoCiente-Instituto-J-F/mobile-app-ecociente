package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import com.example.ecociente.model.DesempenhoQuiz;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Array de GET /api/tentativas-quiz: da tentativa mais recente para a mais antiga. Item inválido
// derruba a leitura toda, para uma resposta quebrada não virar um histórico "vazio" ou com nota zero.
final class HistoricoQuizJson {

    private HistoricoQuizJson() {}

    @NonNull
    static List<DesempenhoQuiz> desempenhos(@NonNull JSONArray array) throws JSONException {

        List<DesempenhoQuiz> resultado = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);

            int tentativaId = item.getInt("tentativaId");
            double nota = item.isNull("nota") ? Double.NaN : item.getDouble("nota");
            String concluidoEm = item.isNull("concluidoEm") ? "" : item.getString("concluidoEm");

            if (tentativaId <= 0 || Double.isNaN(nota) || nota < 0 || nota > 100 || concluidoEm.trim().isEmpty()) {
                throw new JSONException("Tentativa de quiz inválida na posição " + i);
            }

            resultado.add(new DesempenhoQuiz(tentativaId, item.optString("titulo", "").trim(), nota, concluidoEm));
        }

        return resultado;
    }
}
