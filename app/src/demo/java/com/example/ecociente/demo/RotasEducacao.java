package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// ds-mobile-api, conteúdo: GET /api/cursos, GET /api/cursos/{id}/aulas e GET /api/aulas/{id}/quiz. É só leitura e o conteúdo é fixo (ConteudoEducativoDemo). O quiz sai sem o gabarito; ele só aparece na revisão de uma tentativa finalizada (RotasQuiz).
final class RotasEducacao implements RotasDemo {

    static final String HOST = "ds-mobile-api.onrender.com";

    private static final Pattern AULAS = Pattern.compile("^/api/cursos/(\\d{1,9})/aulas$");
    private static final Pattern QUIZ = Pattern.compile("^/api/aulas/(\\d{1,9})/quiz$");

    private final EstadoDemo estado;

    RotasEducacao(@NonNull EstadoDemo estado) {
        this.estado = estado;
    }

    @Nullable
    @Override
    public RespostaDemo tratar(@NonNull RequisicaoDemo r) {
        if (!HOST.equals(r.host) || !"GET".equals(r.metodo)) {
            return null;
        }

        Matcher aulas = AULAS.matcher(r.caminho);
        Matcher quiz = QUIZ.matcher(r.caminho);
        boolean listaAulas = aulas.matches();
        boolean umQuiz = quiz.matches();

        if (!(r.caminho.equals("/api/cursos") || listaAulas || umQuiz)) {
            return null;
        }

        if (!ContaDemo.tokenConfere(estado.raiz(), r.autorizacao)) {
            return RespostaDemo.erro(401, "NAO_AUTORIZADO");
        }

        try {
            if (listaAulas) {
                return aulas(Integer.parseInt(aulas.group(1)));
            }

            if (umQuiz) {
                return quiz(Integer.parseInt(quiz.group(1)));
            }

            return cursos();

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    @NonNull
    private static RespostaDemo cursos() throws JSONException {
        JSONArray lista = new JSONArray();

        for (ConteudoEducativoDemo.Curso curso : ConteudoEducativoDemo.cursos()) {
            lista.put(
                    new JSONObject()
                            .put("id", curso.id)
                            .put("titulo", curso.titulo)
                            .put("descricao", curso.descricao));
        }

        return new RespostaDemo(200, lista.toString());
    }

    @NonNull
    private static RespostaDemo aulas(int cursoId) throws JSONException {
        ConteudoEducativoDemo.Curso curso = ConteudoEducativoDemo.curso(cursoId);

        if (curso == null) {
            return RespostaDemo.erro(404, "CURSO_NAO_ENCONTRADO");
        }

        JSONArray lista = new JSONArray();

        for (ConteudoEducativoDemo.Aula aula : curso.aulas) {
            lista.put(
                    new JSONObject()
                            .put("id", aula.id)
                            .put("cursoId", curso.id)
                            .put("ordem", aula.ordem)
                            .put("titulo", aula.titulo)
                            .put("conteudo", aula.conteudo));
        }

        return new RespostaDemo(200, lista.toString());
    }

    // Aula sem quiz (ou inexistente): 404, que o EducacaoRepository trata como "sem quiz".
    @NonNull
    private static RespostaDemo quiz(int aulaId) throws JSONException {
        ConteudoEducativoDemo.Aula aula = ConteudoEducativoDemo.aula(aulaId);

        if (aula == null || aula.quiz == null) {
            return RespostaDemo.erro(404, "QUIZ_NAO_ENCONTRADO");
        }

        ConteudoEducativoDemo.Quiz quiz = aula.quiz;
        JSONArray perguntas = new JSONArray();

        for (ConteudoEducativoDemo.Pergunta pergunta : quiz.perguntas) {
            JSONArray alternativas = new JSONArray();

            for (int i = 0; i < pergunta.alternativas.size(); i++) {
                alternativas.put(
                        new JSONObject().put("id", pergunta.idAlternativa(i)).put("texto", pergunta.alternativas.get(i)));
            }

            perguntas.put(
                    new JSONObject()
                            .put("id", pergunta.id)
                            .put("ordem", pergunta.ordem)
                            .put("enunciado", pergunta.enunciado)
                            .put("alternativas", alternativas));
        }

        return new RespostaDemo(
                200,
                new JSONObject()
                        .put("id", quiz.id)
                        .put("aulaId", aula.id)
                        .put("titulo", quiz.titulo)
                        .put("pontosRecompensa", quiz.pontosRecompensa)
                        .put("notaMinimaAprovacao", ConteudoEducativoDemo.NOTA_MINIMA)
                        .put("perguntas", perguntas)
                        .toString());
    }
}
