package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import com.example.ecociente.model.QuizConteudo;
import com.example.ecociente.model.QuizResumo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

// Converte as respostas da ds-mobile-api (cursos, aulas e quiz) nos modelos do app.
final class QuizJson {

    private QuizJson() {}

    // Devolve os itens ordenados pelo campo "ordem"; sem o campo, mantém a ordem recebida.
    @NonNull
    static JSONArray ordenarPorOrdem(@NonNull JSONArray itens) throws Exception {

        List<JSONObject> lista = new ArrayList<>();

        for (int i = 0; i < itens.length(); i++) {
            lista.add(itens.getJSONObject(i));
        }

        Collections.sort(lista, (a, b) -> Integer.compare(a.optInt("ordem", 0), b.optInt("ordem", 0)));

        return new JSONArray(lista);
    }

    // A API ainda não manda o gabarito: lê "correta" (alternativa) e "explicacao" (pergunta) se vierem.
    @NonNull
    static QuizResumo montar(
            @NonNull String categoria, @NonNull String tituloAula, @NonNull JSONObject quiz)
            throws Exception {

        List<QuizConteudo.Pergunta> perguntas = new ArrayList<>();

        JSONArray listaPerguntas =
                ordenarPorOrdem(
                        quiz.optJSONArray("perguntas") != null
                                ? quiz.optJSONArray("perguntas")
                                : new JSONArray());

        for (int i = 0; i < listaPerguntas.length(); i++) {
            JSONObject pergunta = listaPerguntas.getJSONObject(i);

            JSONArray alternativas = pergunta.optJSONArray("alternativas");

            List<String> opcoes = new ArrayList<>();
            int[] idsAlternativas = new int[alternativas != null ? alternativas.length() : 0];
            int correta = -1;

            for (int j = 0; alternativas != null && j < alternativas.length(); j++) {
                JSONObject alternativa = alternativas.getJSONObject(j);

                opcoes.add(alternativa.optString("texto"));
                idsAlternativas[j] = alternativa.optInt("id", 0);

                if (alternativa.optBoolean("correta", false)) {
                    correta = j;
                }
            }

            perguntas.add(
                    new QuizConteudo.Pergunta(
                            pergunta.optString("enunciado"),
                            opcoes.toArray(new String[0]),
                            correta,
                            pergunta.optString("explicacao", ""),
                            pergunta.optInt("id", 0),
                            idsAlternativas));
        }

        String titulo = quiz.optString("titulo", "");

        return new QuizResumo(
                titulo.isEmpty() ? tituloAula : titulo,
                categoria,
                quiz.optInt("pontosRecompensa", 0),
                perguntas,
                quiz.optInt("id", 0),
                quiz.optDouble("notaMinimaAprovacao", 0));
    }
}
