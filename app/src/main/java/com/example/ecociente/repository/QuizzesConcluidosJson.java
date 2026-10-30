package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import java.util.LinkedHashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;

// Array de GET /api/tentativas-quiz/quizzes-concluidos: ids dos quizzes que o usuário já concluiu.
final class QuizzesConcluidosJson {

    private QuizzesConcluidosJson() {}

    @NonNull
    static Set<Integer> ids(@NonNull JSONArray array) throws JSONException {

        Set<Integer> ids = new LinkedHashSet<>();

        for (int i = 0; i < array.length(); i++) {
            int id = array.getInt(i);

            if (id <= 0) {
                throw new JSONException("Id de quiz inválido na posição " + i);
            }

            ids.add(id);
        }

        return ids;
    }
}
