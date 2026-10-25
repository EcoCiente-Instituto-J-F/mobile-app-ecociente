package com.example.ecociente.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.R;
import com.example.ecociente.model.CursoEducativo;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.model.QuizConteudo;
import com.example.ecociente.model.QuizResumo;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import org.json.JSONArray;
import org.json.JSONObject;

/** Fonte única da ds-mobile-api. Não simula dados se a API estiver inacessível. */
public class EducacaoRepository {
    private static final String TAG = "EducacaoRepository";
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);
    // Evita centenas de chamadas em série durante a listagem de aulas e quizzes.
    private static final ExecutorService CONSULTAS = Executors.newFixedThreadPool(4);
    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());
    private final AutenticacaoExternaRepository autenticacao = new AutenticacaoExternaRepository();
    private volatile String ultimoErro = "";

    public interface Retorno<T> { void aoConcluir(boolean sucesso, @NonNull List<T> itens); }
    public interface RetornoTentativa { void aoConcluir(boolean sucesso, int tentativaId, @NonNull String mensagem); }
    public interface RetornoResultado { void aoConcluir(boolean sucesso, @NonNull String mensagem); }

    @NonNull public String obterUltimoErro() { return ultimoErro; }

    /** Carregamento paralelo controlado. O app não precisa de um endpoint inexistente /api/quizzes. */
    @NonNull private List<CursoEducativo> consultarCursosSincronamente(@NonNull Context app) throws Exception {
        JSONArray cursos = obterArray(app, "/api/cursos");
        List<Future<CursoEducativo>> tarefas = new ArrayList<>();
        for (int i = 0; i < cursos.length(); i++) {
            JSONObject curso = cursos.getJSONObject(i);
            int id = curso.getInt("id");
            String titulo = curso.optString("titulo");
            String descricao = curso.optString("descricao");
            tarefas.add(CONSULTAS.submit(() -> {
                JSONArray aulasJson = obterArray(app, "/api/cursos/" + id + "/aulas");
                List<CursoEducativo.Aula> aulas = new ArrayList<>();
                for (int j = 0; j < aulasJson.length(); j++) {
                    JSONObject aula = aulasJson.getJSONObject(j);
                    aulas.add(new CursoEducativo.Aula(
                            aula.getInt("id"), aula.optString("titulo"),
                            aula.optString("conteudo"), aula.optInt("ordem", j + 1)));
                }
                aulas.sort(Comparator.comparingInt(a -> a.ordem));
                return new CursoEducativo(id, titulo, descricao, aulas);
            }));
        }
        List<CursoEducativo> lista = new ArrayList<>();
        for (Future<CursoEducativo> tarefa : tarefas) lista.add(tarefa.get());
        return lista;
    }

    public void listarCursos(@NonNull Context contexto, @NonNull Retorno<CursoEducativo> retorno) {
        Context app = contexto.getApplicationContext();
        EXECUTOR.execute(() -> {
            List<CursoEducativo> lista = new ArrayList<>();
            boolean sucesso = false;
            try {
                lista.addAll(consultarCursosSincronamente(app));
                sucesso = true;
                ultimoErro = "";
            } catch (Exception erro) { registrarFalha("cursos e aulas", falhaReal(erro)); }
            boolean resultado = sucesso;
            PRINCIPAL.post(() -> retorno.aoConcluir(resultado, lista));
        });
    }

    public void listarGuia(@NonNull Context contexto, @NonNull Retorno<ItemLista> retorno) {
        listarCursos(contexto, (sucesso, cursos) -> {
            List<ItemLista> itens = new ArrayList<>();
            for (CursoEducativo curso : cursos) {
                for (CursoEducativo.Aula aula : curso.aulas) {
                    itens.add(new ItemLista(aula.titulo, curso.titulo, "",
                            new String[][]{{"Curso", curso.titulo}, {"Conteúdo", aula.conteudo}}));
                }
            }
            retorno.aoConcluir(sucesso, itens);
        });
    }

    public void listarQuizzes(@NonNull Context contexto, @NonNull Retorno<QuizResumo> retorno) {
        Context app = contexto.getApplicationContext();
        EXECUTOR.execute(() -> {
            List<QuizResumo> quizzes = new ArrayList<>();
            boolean sucesso = false;
            try {
                List<CursoEducativo> cursos = consultarCursosSincronamente(app);
                List<Future<QuizResumo>> tarefas = new ArrayList<>();
                for (CursoEducativo curso : cursos) {
                    for (CursoEducativo.Aula aula : curso.aulas) {
                        tarefas.add(CONSULTAS.submit(() -> {
                            String resposta = obter(app, "/api/aulas/" + aula.id + "/quiz", true);
                            if (resposta == null) return null; // 404 = aula sem quiz.
                            return montarQuiz(curso.titulo, aula.titulo, new JSONObject(resposta));
                        }));
                    }
                }
                for (Future<QuizResumo> tarefa : tarefas) {
                    QuizResumo quiz = tarefa.get();
                    if (quiz != null && !quiz.getPerguntas().isEmpty()) quizzes.add(quiz);
                }
                ultimoErro = "";
                sucesso = true;
            } catch (Exception erro) { registrarFalha("quizzes", falhaReal(erro)); }
            boolean resultado = sucesso;
            PRINCIPAL.post(() -> retorno.aoConcluir(resultado, quizzes));
        });
    }

    @NonNull private QuizResumo montarQuiz(String categoria, String tituloAula, JSONObject quiz)
            throws Exception {
        List<QuizConteudo.Pergunta> perguntas = new ArrayList<>();
        JSONArray lista = quiz.optJSONArray("perguntas");
        for (int i = 0; lista != null && i < lista.length(); i++) {
            JSONObject p = lista.getJSONObject(i);
            JSONArray alternativas = p.optJSONArray("alternativas");
            if (alternativas == null || alternativas.length() < 2) continue;
            String[] opcoes = new String[alternativas.length()];
            int[] ids = new int[alternativas.length()];
            for (int j = 0; j < alternativas.length(); j++) {
                JSONObject a = alternativas.getJSONObject(j);
                opcoes[j] = a.optString("texto");
                ids[j] = a.getInt("id");
            }
            perguntas.add(new QuizConteudo.Pergunta(p.getInt("id"), p.optString("enunciado"),
                    opcoes, ids, -1, ""));
        }
        String titulo = quiz.optString("titulo");
        return new QuizResumo(quiz.getInt("id"), titulo.isEmpty() ? tituloAula : titulo,
                categoria, quiz.optInt("pontosRecompensa"), perguntas);
    }

    public void iniciarTentativa(@NonNull Context contexto, int quizId,
                                 @NonNull RetornoTentativa retorno) {
        Context app = contexto.getApplicationContext();
        EXECUTOR.execute(() -> {
            int id = -1;
            String mensagem = "";
            try {
                if (quizId <= 0) throw new IOException("Quiz sem ID válido na API.");
                String json = requisicao(app, "POST", "/api/quizzes/" + quizId + "/tentativas", null, false);
                id = new JSONObject(json).getInt("tentativaId");
                if (id <= 0) throw new IOException("A API não retornou tentativaId válido.");
                ultimoErro = "";
            } catch (Exception erro) { registrarFalha("iniciar quiz", erro); mensagem = ultimoErro; }
            int resultado = id;
            String aviso = mensagem;
            PRINCIPAL.post(() -> retorno.aoConcluir(resultado > 0, resultado, aviso));
        });
    }

    public void finalizarTentativa(@NonNull Context contexto, int tentativaId,
            @NonNull List<QuizConteudo.Pergunta> perguntas, @NonNull int[] respostas,
            @NonNull RetornoResultado retorno) {
        Context app = contexto.getApplicationContext();
        EXECUTOR.execute(() -> {
            boolean sucesso = false;
            String mensagem = "";
            try {
                if (tentativaId <= 0 || respostas.length != perguntas.size()) {
                    throw new IOException("Tentativa não iniciada na API.");
                }
                JSONObject corpo = new JSONObject();
                JSONArray itens = new JSONArray();
                for (int i = 0; i < respostas.length; i++) {
                    if (respostas[i] < 0 || respostas[i] >= perguntas.get(i).getOpcoes().length) {
                        throw new IOException("Responda todas as perguntas antes de finalizar.");
                    }
                    JSONObject resposta = new JSONObject();
                    resposta.put("perguntaId", perguntas.get(i).getId());
                    resposta.put("alternativaId", perguntas.get(i).getAlternativaId(respostas[i]));
                    itens.put(resposta);
                }
                corpo.put("respostas", itens);
                String base = "/api/tentativas-quiz/" + tentativaId;
                JSONObject resultado = new JSONObject(requisicao(app, "POST", base + "/finalizar", corpo, false));
                QuizConteudo.definirResultado(resultado.getInt("acertos"), resultado.optDouble("nota", -1));
                sucesso = true;
                ultimoErro = "";
                // Revisão só é exposta pelo backend depois de finalizar a tentativa.
                try {
                    JSONObject revisao = new JSONObject(obter(app, base + "/revisao", false));
                    JSONArray listagem = revisao.optJSONArray("perguntas");
                    Map<Integer, Integer> corretas = new HashMap<>();
                    for (int i = 0; listagem != null && i < listagem.length(); i++) {
                        JSONObject p = listagem.getJSONObject(i);
                        JSONObject correta = p.optJSONObject("correta");
                        if (correta != null) corretas.put(p.getInt("perguntaId"), correta.getInt("id"));
                    }
                    QuizConteudo.aplicarRevisao(corretas);
                } catch (Exception erroRevisao) {
                    Log.w(TAG, "Tentativa finalizada; revisão indisponível", erroRevisao);
                }
            } catch (Exception erro) { registrarFalha("finalizar quiz", erro); mensagem = ultimoErro; }
            boolean finalizado = sucesso;
            String aviso = mensagem;
            PRINCIPAL.post(() -> retorno.aoConcluir(finalizado, aviso));
        });
    }

    @NonNull private JSONArray obterArray(@NonNull Context app, @NonNull String caminho) throws Exception {
        String resposta = obter(app, caminho, false);
        return new JSONArray(resposta);
    }

    @Nullable private String obter(@NonNull Context app, @NonNull String caminho, boolean permitir404)
            throws IOException {
        return requisicao(app, "GET", caminho, null, permitir404);
    }

    @Nullable private String requisicao(@NonNull Context app, @NonNull String metodo,
              @NonNull String caminho, @Nullable JSONObject corpo, boolean permitir404) throws IOException {
        String token = autenticacao.tokenParaUso(app);
        if (token == null || token.trim().isEmpty()) {
            throw new IOException("Sem sessão na ds-autenticacao-api. Entre com e-mail e senha para sincronizar o acesso aos cursos e quizzes.");
        }
        Resposta resposta = enviar(app, metodo, caminho, token, corpo);
        if (resposta.codigo == 401 || resposta.codigo == 403) {
            String novo = autenticacao.renovarToken(app);
            if (novo != null) resposta = enviar(app, metodo, caminho, novo, corpo);
        }
        if (resposta.codigo == 404 && permitir404) return null;
        if (resposta.codigo == 401 || resposta.codigo == 403)
            throw new IOException("HTTP " + resposta.codigo + " — token rejeitado pela ds-mobile-api. Verifique a sessão e a chave JWT compartilhada pelas duas APIs.");
        if (resposta.codigo == 404)
            throw new IOException("HTTP 404 em " + caminho + " — confirme o deploy e as rotas da ds-mobile-api.");
        if (resposta.codigo < 200 || resposta.codigo >= 300)
            throw new IOException("HTTP " + resposta.codigo + " em " + caminho
                    + (resposta.codigo >= 500 ? " — servidor indisponível." : " — solicitação não aceita."));
        if (resposta.corpo == null || resposta.corpo.trim().isEmpty())
            throw new IOException("Resposta vazia da API em " + caminho);
        return resposta.corpo;
    }

    @NonNull private Resposta enviar(@NonNull Context app, @NonNull String metodo,
            @NonNull String caminho, @NonNull String token, @Nullable JSONObject corpo) throws IOException {
        String base = app.getString(R.string.educacao_api_base_url).trim();
        if (base.isEmpty()) throw new IOException("URL da ds-mobile-api não configurada.");
        HttpURLConnection conexao = (HttpURLConnection) new URL(base + caminho).openConnection();
        try {
            conexao.setRequestMethod(metodo);
            conexao.setRequestProperty("Accept", "application/json");
            conexao.setRequestProperty("Authorization", "Bearer " + token);
            conexao.setUseCaches(false);
            conexao.setConnectTimeout(150_000);
            conexao.setReadTimeout(150_000);
            if (corpo != null) {
                conexao.setDoOutput(true);
                conexao.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                try (OutputStream saida = conexao.getOutputStream()) {
                    saida.write(corpo.toString().getBytes(StandardCharsets.UTF_8));
                }
            }
            int codigo = conexao.getResponseCode();
            InputStream fluxo = codigo >= 400 ? conexao.getErrorStream() : conexao.getInputStream();
            return new Resposta(codigo, ler(fluxo));
        } finally { conexao.disconnect(); }
    }

    @NonNull private static String ler(@Nullable InputStream fluxo) throws IOException {
        if (fluxo == null) return "";
        try (InputStream entrada = fluxo; ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[2048];
            int lidos;
            while ((lidos = entrada.read(buffer)) != -1) saida.write(buffer, 0, lidos);
            return saida.toString(StandardCharsets.UTF_8.name());
        }
    }

    private static Exception falhaReal(Exception erro) {
        if (erro instanceof ExecutionException && erro.getCause() instanceof Exception) {
            return (Exception) erro.getCause();
        }
        if (erro instanceof InterruptedException) Thread.currentThread().interrupt();
        return erro;
    }

    private void registrarFalha(String contexto, Exception erro) {
        if (erro instanceof SocketTimeoutException) {
            ultimoErro = "A API demorou a responder. Aguarde o servidor iniciar e tente novamente.";
        } else if (erro instanceof java.net.UnknownHostException
                || erro instanceof java.net.ConnectException) {
            ultimoErro = "Sem conexão com a ds-mobile-api. Confira internet, URL e deploy.";
        } else if (erro instanceof org.json.JSONException) {
            ultimoErro = "A resposta da API não corresponde ao formato esperado. Confira o contrato do backend.";
        } else {
            ultimoErro = erro.getMessage() == null ? "Não foi possível concluir a consulta." : erro.getMessage();
        }
        Log.w(TAG, "Falha ao carregar " + contexto + ": " + ultimoErro, erro);
    }

    private static final class Resposta {
        final int codigo;
        final String corpo;
        Resposta(int codigo, String corpo) { this.codigo = codigo; this.corpo = corpo; }
    }
}
