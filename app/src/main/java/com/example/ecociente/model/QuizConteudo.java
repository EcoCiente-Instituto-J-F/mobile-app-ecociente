package com.example.ecociente.model;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * Quiz em andamento. A lista de quizzes carrega as perguntas da ds-mobile-api e a tela escolhida
 * fica aqui para as telas de introdução, perguntas, resultado e revisão lerem a mesma fonte.
 */
public final class QuizConteudo {

    private static List<Pergunta> perguntasAtuais = Collections.emptyList();

    private QuizConteudo() {
    }

    public static void definir(@NonNull List<Pergunta> perguntas) {
        perguntasAtuais = perguntas;
    }

    @NonNull
    public static List<Pergunta> obterPerguntas() {
        return perguntasAtuais;
    }

    // Sem gabarito em alguma pergunta não dá para corrigir o quiz no aparelho.
    public static boolean temGabarito() {
        for (Pergunta pergunta : perguntasAtuais) {
            if (pergunta.getIndiceCorreto() < 0) {
                return false;
            }
        }

        return !perguntasAtuais.isEmpty();
    }

    public static final class Pergunta {
        private final String enunciado;
        private final String[] opcoes;
        private final int indiceCorreto;
        private final String explicacao;

        public Pergunta(
                @NonNull String enunciado,
                @NonNull String[] opcoes,
                int indiceCorreto,
                @NonNull String explicacao
        ) {
            this.enunciado = enunciado;
            this.opcoes = opcoes;
            this.indiceCorreto = indiceCorreto;
            this.explicacao = explicacao;
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

        @NonNull
        public String getExplicacao() {
            return explicacao;
        }
    }
}
