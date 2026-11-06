package com.example.ecociente.demo;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class ConteudoEducativoDemo {

    static final int NOTA_MINIMA = 70;

    private static volatile List<Curso> cursos = Collections.emptyList();

    private ConteudoEducativoDemo() {}

    static synchronized void inicializar(@NonNull Context contexto) {
        if (!cursos.isEmpty()) return;
        try {
            JSONArray originais = new JSONArray(ler(contexto, "mock/educacao/cursos.json"));
            JSONArray provas = new JSONArray(ler(contexto, "mock/educacao/quizzes.json"));
            java.util.Map<Integer, Quiz> porAula = new java.util.HashMap<>();
            for (int i = 0; i < provas.length(); i++) {
                JSONObject item = provas.getJSONObject(i);
                JSONArray questoes = item.getJSONArray("perguntas");
                List<Pergunta> perguntas = new ArrayList<>();
                for (int p = 0; p < questoes.length(); p++) {
                    JSONObject q = questoes.getJSONObject(p);
                    JSONArray respostas = q.getJSONArray("alternativas");
                    String[] opcoes = new String[respostas.length()];
                    int[] ids = new int[respostas.length()];
                    int correta = -1;
                    for (int a = 0; a < respostas.length(); a++) {
                        JSONObject alternativa = respostas.getJSONObject(a);
                        ids[a] = alternativa.getInt("id");
                        opcoes[a] = alternativa.getString("texto");
                        if (alternativa.optBoolean("correta", false)) {
                            if (correta >= 0) throw new IllegalStateException("Mais de uma alternativa correta na pergunta " + q.getInt("id"));
                            correta = a;
                        }
                    }
                    if (correta < 0) throw new IllegalStateException("Pergunta sem gabarito: " + q.getInt("id"));
                    perguntas.add(new Pergunta(q.getInt("id"), q.optInt("ordem", p + 1), q.getString("enunciado"), correta,
                            q.optString("explicacao", ""), ids, opcoes));
                }
                Quiz quiz = new Quiz(item.getInt("id"), item.getString("titulo"),
                        item.optInt("pontosRecompensa", 0), perguntas);
                if (porAula.put(item.getInt("aulaId"), quiz) != null) {
                    throw new IllegalStateException("Dois quizzes na mesma aula " + item.getInt("aulaId"));
                }
            }
            List<Curso> resultado = new ArrayList<>();
            for (int i = 0; i < originais.length(); i++) {
                JSONObject item = originais.getJSONObject(i);
                JSONArray itens = item.getJSONArray("aulas");
                List<Aula> aulas = new ArrayList<>();
                for (int j = 0; j < itens.length(); j++) {
                    JSONObject a = itens.getJSONObject(j);
                    aulas.add(new Aula(a.getInt("id"), a.optInt("ordem", j + 1), a.getString("titulo"),
                            a.optString("conteudo", ""), porAula.remove(a.getInt("id"))));
                }
                aulas.sort((a, b) -> Integer.compare(a.ordem, b.ordem));
                resultado.add(new Curso(item.getInt("id"), item.getString("titulo"),
                        item.optString("descricao", ""), aulas));
            }
            if (resultado.size() != 42 || !porAula.isEmpty()) {
                throw new IllegalStateException("Catalogo Neon incompleto ou quiz sem aula: " + resultado.size());
            }
            cursos = Collections.unmodifiableList(resultado);
        } catch (IOException | JSONException e) {
            throw new IllegalStateException("Nao foi possivel carregar catalogo offline EcoCiente", e);
        }
    }

    private static String ler(Context contexto, String caminho) throws IOException {
        try (InputStream entrada = contexto.getAssets().open(caminho);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] bloco = new byte[8192];
            int lidos;
            while ((lidos = entrada.read(bloco)) != -1) buffer.write(bloco, 0, lidos);
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    static final class Curso {
        final int id;
        final String titulo;
        final String descricao;
        final List<Aula> aulas;
        Curso(int id, String titulo, String descricao, List<Aula> aulas) {
            this.id = id; this.titulo = titulo; this.descricao = descricao;
            this.aulas = Collections.unmodifiableList(new ArrayList<>(aulas));
        }
    }

    static final class Aula {
        final int id;
        final int ordem;
        final String titulo;
        final String conteudo;
        @Nullable final Quiz quiz;
        Aula(int id, int ordem, String titulo, String conteudo, @Nullable Quiz quiz) {
            this.id = id; this.ordem = ordem; this.titulo = titulo;
            this.conteudo = conteudo; this.quiz = quiz;
        }
    }

    static final class Quiz {
        final int id;
        final String titulo;
        final int pontosRecompensa;
        final List<Pergunta> perguntas;
        Quiz(int id, String titulo, int pontosRecompensa, List<Pergunta> perguntas) {
            this.id = id; this.titulo = titulo; this.pontosRecompensa = pontosRecompensa;
            this.perguntas = Collections.unmodifiableList(new ArrayList<>(perguntas));
        }
        @Nullable Pergunta pergunta(int perguntaId) {
            for (Pergunta p : perguntas) if (p.id == perguntaId) return p;
            return null;
        }
    }

    static final class Pergunta {
        final int id;
        final int ordem;
        final String enunciado;
        final String explicacao;
        final int indiceCorreto;
        final List<String> alternativas;
        private final int[] idsAlternativas;
        Pergunta(int id, int ordem, String enunciado, int indiceCorreto, String explicacao,
                int[] idsAlternativas, String[] alternativas) {
            this.id = id; this.ordem = ordem; this.enunciado = enunciado;
            this.indiceCorreto = indiceCorreto; this.explicacao = explicacao;
            this.idsAlternativas = idsAlternativas.clone();
            this.alternativas = Collections.unmodifiableList(java.util.Arrays.asList(alternativas.clone()));
        }
        int idAlternativa(int indice) { return idsAlternativas[indice]; }
        int idCorreta() { return idAlternativa(indiceCorreto); }
        boolean temAlternativa(int id) {
            for (int item : idsAlternativas) if (item == id) return true;
            return false;
        }
    }

    @NonNull static List<Curso> cursos() { return cursos; }
    @Nullable static Curso curso(int id) {
        for (Curso c : cursos) if (c.id == id) return c;
        return null;
    }
    @Nullable static Aula aula(int id) {
        for (Curso c : cursos) for (Aula a : c.aulas) if (a.id == id) return a;
        return null;
    }
    @Nullable static Quiz quiz(int id) {
        for (Curso c : cursos) for (Aula a : c.aulas)
            if (a.quiz != null && a.quiz.id == id) return a.quiz;
        return null;
    }
}
