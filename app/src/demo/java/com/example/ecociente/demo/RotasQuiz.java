package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// ds-mobile-api, tentativas de quiz: abrir (POST /api/quizzes/{id}/tentativas), finalizar (POST /api/tentativas-quiz/{id}/finalizar), revisão, histórico e quizzes concluídos. As tentativas ficam no estado (`educacao`). A correção e os pontos são da demo: a recompensa do quiz sai uma vez só, na primeira tentativa aprovada.
final class RotasQuiz implements RotasDemo {

    private static final String TENTATIVAS = "/api/tentativas-quiz";
    private static final Pattern ABRIR = Pattern.compile("^/api/quizzes/(\\d{1,9})/tentativas$");
    private static final Pattern FINALIZAR = Pattern.compile("^/api/tentativas-quiz/(\\d{1,9})/finalizar$");
    private static final Pattern REVISAO = Pattern.compile("^/api/tentativas-quiz/(\\d{1,9})/revisao$");

    private final EstadoDemo estado;
    private final LongSupplier relogio;

    RotasQuiz(@NonNull EstadoDemo estado, @NonNull LongSupplier relogio) {
        this.estado = estado;
        this.relogio = relogio;
    }

    // Bloco inicial do estado: sem tentativas.
    @NonNull
    static JSONObject semente() throws JSONException {
        return new JSONObject().put("proximoId", 1).put("tentativas", new JSONArray());
    }

    @Nullable
    @Override
    public RespostaDemo tratar(@NonNull RequisicaoDemo r) {
        if (!RotasEducacao.HOST.equals(r.host)) {
            return null;
        }

        Matcher abrir = ABRIR.matcher(r.caminho);
        Matcher finalizar = FINALIZAR.matcher(r.caminho);
        Matcher revisao = REVISAO.matcher(r.caminho);

        boolean eAbrir = "POST".equals(r.metodo) && abrir.matches();
        boolean eFinalizar = "POST".equals(r.metodo) && finalizar.matches();
        boolean eRevisao = "GET".equals(r.metodo) && revisao.matches();
        boolean eHistorico = r.e("GET", TENTATIVAS);
        boolean eConcluidos = r.e("GET", TENTATIVAS + "/quizzes-concluidos");

        if (!(eAbrir || eFinalizar || eRevisao || eHistorico || eConcluidos)) {
            return null;
        }

        if (!ContaDemo.tokenConfere(estado.raiz(), r.autorizacao)) {
            return RespostaDemo.erro(401, "NAO_AUTORIZADO");
        }

        try {
            if (eAbrir) {
                return abrir(Integer.parseInt(abrir.group(1)));
            }

            if (eFinalizar) {
                return finalizar(Integer.parseInt(finalizar.group(1)), r);
            }

            if (eRevisao) {
                return revisao(Integer.parseInt(revisao.group(1)));
            }

            return eHistorico ? historico(r) : concluidos();

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    @NonNull
    private static JSONObject bloco(@NonNull JSONObject raiz) {
        JSONObject bloco = raiz.optJSONObject("educacao");

        if (bloco == null) {
            throw new IllegalStateException("Estado da demo sem educação");
        }

        return bloco;
    }

    @NonNull
    private JSONArray tentativas() throws JSONException {
        return bloco(estado.raiz()).getJSONArray("tentativas");
    }

    @Nullable
    private JSONObject tentativa(int id) throws JSONException {
        JSONArray lista = tentativas();

        for (int i = 0; i < lista.length(); i++) {
            if (lista.getJSONObject(i).optInt("id", 0) == id) {
                return lista.getJSONObject(i);
            }
        }

        return null;
    }

    @NonNull
    private RespostaDemo abrir(int quizId) throws JSONException {
        if (ConteudoEducativoDemo.quiz(quizId) == null) {
            return erro(404, "QUIZ_NAO_ENCONTRADO");
        }

        JSONObject bloco = bloco(estado.raiz());
        int id = bloco.optInt("proximoId", 1);
        long agora = relogio.getAsLong();

        bloco.put("proximoId", id + 1);
        tentativas()
                .put(
                        new JSONObject()
                                .put("id", id)
                                .put("quizId", quizId)
                                .put("iniciadaEm", agora)
                                .put("finalizada", false));
        estado.salvar();

        return new RespostaDemo(
                201,
                new JSONObject()
                        .put("tentativaId", id)
                        .put("quizId", quizId)
                        .put("iniciadaEm", SementeDemo.formatarData(agora))
                        .toString());
    }

    // Corrige, grava e, na primeira aprovação do quiz, credita a recompensa. Tentativa desconhecida é 404 e já finalizada é 409: nenhuma credita de novo.
    @NonNull
    private RespostaDemo finalizar(int id, @NonNull RequisicaoDemo r) throws JSONException {
        JSONObject tentativa = tentativa(id);

        if (tentativa == null) {
            return erro(404, "TENTATIVA_NAO_ENCONTRADA");
        }

        if (tentativa.optBoolean("finalizada", false)) {
            return erro(409, "TENTATIVA_JA_FINALIZADA");
        }

        JSONObject corpo = r.corpoJson();
        JSONArray enviadas = corpo == null ? null : corpo.optJSONArray("respostas");

        if (enviadas == null || enviadas.length() == 0) {
            return erro(400, "RESPOSTAS_OBRIGATORIAS");
        }

        ConteudoEducativoDemo.Quiz quiz = ConteudoEducativoDemo.quiz(tentativa.optInt("quizId", 0));

        if (quiz == null) {
            return erro(404, "QUIZ_NAO_ENCONTRADO");
        }

        Map<Integer, Integer> dadas = new HashMap<>();
        JSONArray gravadas = new JSONArray();

        for (int i = 0; i < enviadas.length(); i++) {
            JSONObject resposta = enviadas.optJSONObject(i);

            if (resposta == null) {
                return erro(400, "REQUISICAO_INVALIDA");
            }

            ConteudoEducativoDemo.Pergunta pergunta = quiz.pergunta(resposta.optInt("perguntaId", 0));
            int alternativaId = resposta.optInt("alternativaId", 0);

            if (pergunta == null || !pergunta.temAlternativa(alternativaId)) {
                return erro(400, "RESPOSTA_INVALIDA");
            }

            dadas.put(pergunta.id, alternativaId);
        }

        int acertos = 0;

        for (ConteudoEducativoDemo.Pergunta pergunta : quiz.perguntas) {
            Integer dada = dadas.get(pergunta.id);

            if (dada != null) {
                gravadas.put(new JSONObject().put("perguntaId", pergunta.id).put("alternativaId", dada));
            }

            if (dada != null && dada == pergunta.idCorreta()) {
                acertos++;
            }
        }

        int total = quiz.perguntas.size();
        double nota = Math.round(acertos * 10000.0 / total) / 100.0;
        boolean aprovado = nota >= ConteudoEducativoDemo.NOTA_MINIMA;
        boolean primeiraAprovacao = aprovado && !aprovadoAntes(quiz.id);

        tentativa
                .put("finalizada", true)
                .put("concluidoEm", relogio.getAsLong())
                .put("nota", nota)
                .put("aprovado", aprovado)
                .put("respostas", gravadas);

        int pontos = primeiraAprovacao ? quiz.pontosRecompensa : 0;

        if (pontos > 0) {
            // Grava as duas mudanças juntas.
            estado.creditar("QUIZ", pontos);
        } else {
            estado.salvar();
        }

        return new RespostaDemo(
                200,
                new JSONObject()
                        .put("tentativaId", id)
                        .put("quizId", quiz.id)
                        .put("nota", nota)
                        .put("acertos", acertos)
                        .put("totalPerguntas", total)
                        .put("aprovado", aprovado)
                        .put("pontuacao", new JSONObject().put("pontos", pontos))
                        .toString());
    }

    // Já existe tentativa aprovada (e finalizada) desse quiz? Chamado antes de gravar a atual.
    private boolean aprovadoAntes(int quizId) throws JSONException {
        JSONArray lista = tentativas();

        for (int i = 0; i < lista.length(); i++) {
            JSONObject item = lista.getJSONObject(i);

            if (item.optInt("quizId", 0) == quizId
                    && item.optBoolean("finalizada", false)
                    && item.optBoolean("aprovado", false)) {
                return true;
            }
        }

        return false;
    }

    // O gabarito só sai depois de finalizar, senão a revisão entregaria as respostas antes da prova.
    @NonNull
    private RespostaDemo revisao(int id) throws JSONException {
        JSONObject tentativa = tentativa(id);

        if (tentativa == null) {
            return erro(404, "TENTATIVA_NAO_ENCONTRADA");
        }

        if (!tentativa.optBoolean("finalizada", false)) {
            return erro(409, "TENTATIVA_EM_ANDAMENTO");
        }

        ConteudoEducativoDemo.Quiz quiz = ConteudoEducativoDemo.quiz(tentativa.optInt("quizId", 0));

        if (quiz == null) {
            return erro(404, "QUIZ_NAO_ENCONTRADO");
        }

        Map<Integer, Integer> dadas = new HashMap<>();
        JSONArray gravadas = tentativa.optJSONArray("respostas");

        for (int i = 0; gravadas != null && i < gravadas.length(); i++) {
            JSONObject resposta = gravadas.getJSONObject(i);

            dadas.put(resposta.getInt("perguntaId"), resposta.getInt("alternativaId"));
        }

        JSONArray perguntas = new JSONArray();

        for (ConteudoEducativoDemo.Pergunta pergunta : quiz.perguntas) {
            Integer dada = dadas.get(pergunta.id);
            JSONObject item =
                    new JSONObject()
                            .put("perguntaId", pergunta.id)
                            .put("enunciado", pergunta.enunciado)
                            .put("explicacao", pergunta.explicacao)
                            .put("respostaCorreta", new JSONObject().put("id", pergunta.idCorreta()))
                            .put("acertou", dada != null && dada == pergunta.idCorreta());

            if (dada != null) {
                item.put("respostaDada", new JSONObject().put("id", dada));
            }

            perguntas.put(item);
        }

        return new RespostaDemo(
                200,
                new JSONObject()
                        .put("tentativaId", id)
                        .put("quizId", quiz.id)
                        .put("nota", tentativa.optDouble("nota", 0))
                        .put("perguntas", perguntas)
                        .toString());
    }

    // Finalizadas, da mais recente para a mais antiga; `limite` corta a lista.
    @NonNull
    private RespostaDemo historico(@NonNull RequisicaoDemo r) throws JSONException {
        List<JSONObject> itens = new ArrayList<>();
        JSONArray lista = tentativas();

        for (int i = 0; i < lista.length(); i++) {
            if (lista.getJSONObject(i).optBoolean("finalizada", false)) {
                itens.add(lista.getJSONObject(i));
            }
        }

        Collections.sort(
                itens,
                new Comparator<JSONObject>() {
                    @Override
                    public int compare(JSONObject a, JSONObject b) {
                        int porData = Long.compare(b.optLong("concluidoEm", 0), a.optLong("concluidoEm", 0));

                        return porData != 0 ? porData : Integer.compare(b.optInt("id", 0), a.optInt("id", 0));
                    }
                });

        int limite = inteiro(r.query.get("limite"), 20);

        if (limite <= 0) {
            limite = 20;
        }

        JSONArray saida = new JSONArray();

        for (int i = 0; i < Math.min(limite, itens.size()); i++) {
            JSONObject item = itens.get(i);
            ConteudoEducativoDemo.Quiz quiz = ConteudoEducativoDemo.quiz(item.optInt("quizId", 0));

            saida.put(
                    new JSONObject()
                            .put("tentativaId", item.optInt("id"))
                            .put("quizId", item.optInt("quizId"))
                            .put("titulo", quiz == null ? "" : quiz.titulo)
                            .put("nota", item.optDouble("nota", 0))
                            .put("aprovado", item.optBoolean("aprovado", false))
                            .put("concluidoEm", SementeDemo.formatarData(item.optLong("concluidoEm", 0))));
        }

        return new RespostaDemo(200, saida.toString());
    }

    // Ids dos quizzes com ao menos uma tentativa aprovada.
    @NonNull
    private RespostaDemo concluidos() throws JSONException {
        JSONArray lista = tentativas();
        JSONArray ids = new JSONArray();
        List<Integer> vistos = new ArrayList<>();

        for (int i = 0; i < lista.length(); i++) {
            JSONObject item = lista.getJSONObject(i);
            int quizId = item.optInt("quizId", 0);

            if (item.optBoolean("finalizada", false) && item.optBoolean("aprovado", false) && !vistos.contains(quizId)) {
                vistos.add(quizId);
                ids.put(quizId);
            }
        }

        return new RespostaDemo(200, ids.toString());
    }

    @NonNull
    private static RespostaDemo erro(int status, @NonNull String codigo) {
        return RespostaDemo.erro(status, codigo);
    }

    private static int inteiro(@Nullable String texto, int padrao) {
        try {
            return texto == null ? padrao : Integer.parseInt(texto.trim());

        } catch (NumberFormatException erro) {
            return padrao;
        }
    }
}
