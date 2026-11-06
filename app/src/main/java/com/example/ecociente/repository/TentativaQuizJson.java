package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import com.example.ecociente.model.QuizConteudo;
import com.example.ecociente.model.ResultadoQuiz;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Corpo e respostas dos endpoints de tentativa de quiz da ds-mobile-api.
final class TentativaQuizJson {

    private TentativaQuizJson() {}

    static int tentativaId(@NonNull JSONObject iniciada) {
        return iniciada.optInt("tentativaId", 0);
    }

    // Só vão as perguntas respondidas (respostas[i] >= 0): a API exige ao menos uma e corrige as demais como erro.
    @NonNull
    static String corpoFinalizar(@NonNull List<QuizConteudo.Pergunta> perguntas, @NonNull int[] respostas)
            throws JSONException {

        JSONArray lista = new JSONArray();

        for (int i = 0; i < perguntas.size() && i < respostas.length; i++) {
            if (respostas[i] < 0) {
                continue;
            }

            QuizConteudo.Pergunta pergunta = perguntas.get(i);
            int alternativaId = pergunta.getIdAlternativa(respostas[i]);

            if (pergunta.getId() <= 0 || alternativaId <= 0) {
                continue;
            }

            lista.put(new JSONObject().put("perguntaId", pergunta.getId()).put("alternativaId", alternativaId));
        }

        return new JSONObject().put("respostas", lista).toString();
    }

    @NonNull
    static ResultadoQuiz resultado(@NonNull JSONObject resposta) {

        JSONObject pontuacao = resposta.optJSONObject("pontuacao");

        return new ResultadoQuiz(
                resposta.optInt("tentativaId", 0),
                resposta.optDouble("nota", 0),
                resposta.optInt("acertos", 0),
                resposta.optInt("totalPerguntas", 0),
                resposta.optBoolean("aprovado", false),
                pontuacao != null ? pontuacao.optInt("pontos", 0) : 0);
    }

    // Aplica a alternativa correta e a explicação da revisão às perguntas que o app já tem.
    @NonNull
    static List<QuizConteudo.Pergunta> comGabarito(
            @NonNull JSONObject revisao, @NonNull List<QuizConteudo.Pergunta> originais) {

        List<QuizConteudo.Pergunta> resultado = new ArrayList<>(originais);

        JSONArray perguntas = revisao.optJSONArray("perguntas");

        for (int i = 0; perguntas != null && i < perguntas.length(); i++) {
            JSONObject item = perguntas.optJSONObject(i);

            if (item == null) {
                continue;
            }

            JSONObject correta = item.optJSONObject("respostaCorreta");
            int perguntaId = item.optInt("perguntaId", 0);

            for (int j = 0; correta != null && j < resultado.size(); j++) {
                QuizConteudo.Pergunta original = resultado.get(j);

                if (original.getId() != perguntaId) {
                    continue;
                }

                int indice = original.indiceDaAlternativa(correta.optInt("id", 0));

                if (indice >= 0) {
                    resultado.set(j, original.comGabarito(indice, item.optString("explicacao", "")));
                }
            }
        }

        return resultado;
    }
}
