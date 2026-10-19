package com.example.ecociente.model;

import android.content.Intent;
import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Perguntas do quiz em andamento. Elas viajam na Intent de uma tela para a outra (introdução,
 * perguntas, resultado e revisão), então sobrevivem à recriação do processo e não há estado global.
 */
public final class QuizConteudo {

    private static final String EXTRA_PERGUNTAS = "quizPerguntas";
    private static final String EXTRA_QUIZ_ID = "quizId";

    private QuizConteudo() {
    }

    public static void anexar(@NonNull Intent rota, @NonNull List<Pergunta> perguntas) {
        rota.putExtra(EXTRA_PERGUNTAS, new ArrayList<>(perguntas));
    }

    // Id do quiz na ds-mobile-api, para abrir e finalizar a tentativa. 0 quando não se sabe.
    public static void anexarQuizId(@NonNull Intent rota, int quizId) {
        rota.putExtra(EXTRA_QUIZ_ID, quizId);
    }

    public static int quizIdDoIntent(@NonNull Intent rota) {
        return rota.getIntExtra(EXTRA_QUIZ_ID, 0);
    }

    // Leva o id do quiz e o resultado (se já houver) de uma tela para a seguinte.
    public static void anexarContexto(@NonNull Intent destino, @NonNull Intent origem) {
        destino.putExtra(EXTRA_QUIZ_ID, quizIdDoIntent(origem));

        ResultadoQuiz resultado = ResultadoQuiz.doIntent(origem);

        if (resultado != null) {
            ResultadoQuiz.anexar(destino, resultado);
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static List<Pergunta> doIntent(@NonNull Intent rota) {
        Serializable perguntas = rota.getSerializableExtra(EXTRA_PERGUNTAS);

        return perguntas instanceof ArrayList
                ? (ArrayList<Pergunta>) perguntas
                : Collections.<Pergunta>emptyList();
    }

    // Sem gabarito em alguma pergunta não dá para corrigir o quiz no aparelho.
    public static boolean temGabarito(@NonNull List<Pergunta> perguntas) {
        for (Pergunta pergunta : perguntas) {
            if (pergunta.getIndiceCorreto() < 0) {
                return false;
            }
        }

        return !perguntas.isEmpty();
    }

    public static final class Pergunta implements Serializable {
        private final String enunciado;
        private final String[] opcoes;
        private final int indiceCorreto;
        private final String explicacao;
        private final int id;
        private final int[] idsAlternativas;

        public Pergunta(
                @NonNull String enunciado,
                @NonNull String[] opcoes,
                int indiceCorreto,
                @NonNull String explicacao,
                int id,
                @NonNull int[] idsAlternativas
        ) {
            this.enunciado = enunciado;
            this.opcoes = opcoes;
            this.indiceCorreto = indiceCorreto;
            this.explicacao = explicacao;
            this.id = id;
            this.idsAlternativas = idsAlternativas;
        }

        @NonNull
        public String getEnunciado() {
            return enunciado;
        }

        @NonNull
        public String[] getOpcoes() {
            return opcoes.clone();
        }

        // -1 quando a API não informou a alternativa correta.
        public int getIndiceCorreto() {
            return indiceCorreto;
        }

        // Mesma pergunta, agora com a alternativa certa e a explicação que a API devolveu na revisão.
        @NonNull
        public Pergunta comGabarito(int novoIndiceCorreto, @NonNull String novaExplicacao) {
            return new Pergunta(enunciado, opcoes, novoIndiceCorreto, novaExplicacao, id, idsAlternativas);
        }

        // Posição da alternativa com esse id, ou -1.
        public int indiceDaAlternativa(int alternativaId) {
            for (int i = 0; i < idsAlternativas.length; i++) {
                if (idsAlternativas[i] == alternativaId) {
                    return i;
                }
            }

            return -1;
        }

        // Ids da pergunta e das alternativas na API: é o que o envio das respostas vai precisar.
        public int getId() {
            return id;
        }

        public int getIdAlternativa(int indice) {
            return indice >= 0 && indice < idsAlternativas.length ? idsAlternativas[indice] : 0;
        }

        @NonNull
        public String getExplicacao() {
            return explicacao;
        }
    }
}
