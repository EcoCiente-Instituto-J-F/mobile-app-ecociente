package com.example.ecociente.model;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Estado transitório do quiz enquanto as Activities compartilham a mesma tentativa. */
public final class QuizConteudo {
    private static List<Pergunta> perguntasAtuais = Collections.emptyList();
    private static int quizId = -1;
    private static int tentativaId = -1;
    private static int acertosServidor = -1;
    private static double notaServidor = -1;

    private QuizConteudo() { }

    public static void definir(@NonNull List<Pergunta> perguntas) {
        definirQuiz(-1, perguntas);
    }

    public static synchronized void definirQuiz(int id, @NonNull List<Pergunta> perguntas) {
        quizId = id;
        tentativaId = -1;
        acertosServidor = -1;
        notaServidor = -1;
        perguntasAtuais = Collections.unmodifiableList(new ArrayList<>(perguntas));
    }

    public static synchronized void definirTentativa(int id) {
        tentativaId = id;
        acertosServidor = -1;
        notaServidor = -1;
        // Nova tentativa não pode herdar respostas corretas da revisão anterior.
        List<Pergunta> atualizadas = new ArrayList<>();
        for (Pergunta p : perguntasAtuais) {
            atualizadas.add(new Pergunta(p.id, p.enunciado,
                    p.opcoes, p.alternativaIds, -1, ""));
        }
        perguntasAtuais = Collections.unmodifiableList(atualizadas);
    }
    public static synchronized int getTentativaId() { return tentativaId; }
    public static synchronized int getQuizId() { return quizId; }

    public static synchronized void definirResultado(int acertos, double nota) {
        acertosServidor = acertos;
        notaServidor = nota;
    }

    public static synchronized int getAcertosServidor() { return acertosServidor; }
    public static synchronized double getNotaServidor() { return notaServidor; }

    @NonNull
    public static synchronized List<Pergunta> obterPerguntas() { return perguntasAtuais; }

    public static synchronized boolean temGabarito() {
        if (perguntasAtuais.isEmpty()) return false;
        for (Pergunta pergunta : perguntasAtuais) {
            if (pergunta.getIndiceCorreto() < 0) return false;
        }
        return true;
    }

    /** O backend só fornece a alternativa correta APÓS a finalização. */
    public static synchronized void aplicarRevisao(@NonNull java.util.Map<Integer, Integer> corretas) {
        List<Pergunta> atualizadas = new ArrayList<>();
        for (Pergunta p : perguntasAtuais) {
            int indice = -1;
            Integer idCorreta = corretas.get(p.id);
            if (idCorreta != null) {
                for (int j = 0; j < p.alternativaIds.length; j++) {
                    if (p.alternativaIds[j] == idCorreta) { indice = j; break; }
                }
            }
            atualizadas.add(new Pergunta(p.id, p.enunciado, p.opcoes, p.alternativaIds,
                    indice, p.explicacao));
        }
        perguntasAtuais = Collections.unmodifiableList(atualizadas);
    }

    public static final class Pergunta {
        private final int id;
        private final String enunciado;
        private final String[] opcoes;
        private final int[] alternativaIds;
        private final int indiceCorreto;
        private final String explicacao;

        public Pergunta(int id, @NonNull String enunciado, @NonNull String[] opcoes,
                        @NonNull int[] alternativaIds, int indiceCorreto, @NonNull String explicacao) {
            this.id = id;
            this.enunciado = enunciado;
            this.opcoes = opcoes.clone();
            this.alternativaIds = alternativaIds.clone();
            this.indiceCorreto = indiceCorreto;
            this.explicacao = explicacao;
        }

        public Pergunta(@NonNull String enunciado, @NonNull String[] opcoes,
                        int indiceCorreto, @NonNull String explicacao) {
            this(-1, enunciado, opcoes, new int[opcoes.length], indiceCorreto, explicacao);
        }

        public int getId() { return id; }
        public int getAlternativaId(int indice) { return alternativaIds[indice]; }
        @NonNull public String getEnunciado() { return enunciado; }
        @NonNull public String[] getOpcoes() { return opcoes.clone(); }
        public int getIndiceCorreto() { return indiceCorreto; }
        @NonNull public String getExplicacao() { return explicacao; }
    }
}
