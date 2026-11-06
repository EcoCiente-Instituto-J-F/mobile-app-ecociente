package com.example.ecociente.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.FiltroGuia;
import com.example.ecociente.model.CursoEducativo;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.model.QuizResumo;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Cursos, aulas e quizzes da ds-mobile-api (só leitura, exige o JWT da ds-mobile-api). O guia mostra as aulas; cada aula com quiz vira um quiz da lista.
public class EducacaoRepository {

    private static final String TAG = "EducacaoRepository";

    // Trocar quando o deploy do ds-mobile-api estiver no ar.
    private static final String URL_BASE = ConfiguracaoServidores.MOBILE_API;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final ExecutorService EXECUTOR_CONSULTAS = Executors.newFixedThreadPool(3);

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    private final AutenticacaoExternaRepository autenticacao = new AutenticacaoExternaRepository();
    private volatile String ultimoErro = "";

    @NonNull public String obterUltimoErro() { return ultimoErro; }

    private static final long VALIDADE_CACHE_MS = 5 * 60_000L;
    private static final Object CACHE_LOCK = new Object();
    private static JSONArray cacheCursos;
    private static long cacheEm;

    public interface Retorno<T> {
        void aoConcluir(boolean sucesso, @NonNull List<T> itens);
    }

    /** Cursos e aulas reais para a Biblioteca nativa. Compartilha o cache da equipe. */
    public void listarCursos(@NonNull Context contexto, @NonNull Retorno<CursoEducativo> retorno) {
        Context app = contexto.getApplicationContext();
        EXECUTOR.execute(() -> {
            List<CursoEducativo> saida = new ArrayList<>();
            boolean sucesso = false;
            try {
                JSONArray itens = carregarCursosComAulas(app);
                for (int i = 0; i < itens.length(); i++) {
                    JSONObject cursoJson = itens.getJSONObject(i);
                    JSONArray aulasJson = cursoJson.optJSONArray("aulas");
                    List<CursoEducativo.Aula> aulas = new ArrayList<>();
                    for (int j = 0; aulasJson != null && j < aulasJson.length(); j++) {
                        JSONObject aula = aulasJson.getJSONObject(j);
                        aulas.add(new CursoEducativo.Aula(
                                aula.getInt("id"), aula.optString("titulo"),
                                aula.optString("conteudo"), aula.optInt("ordem", j + 1)));
                    }
                    aulas.sort(Comparator.comparingInt(a -> a.ordem));
                    saida.add(new CursoEducativo(cursoJson.getInt("id"),
                            cursoJson.optString("titulo"), cursoJson.optString("descricao"), aulas));
                }
                sucesso = true;
                ultimoErro = "";
            } catch (Exception erro) {
                Log.w(TAG, "Não foi possível carregar a Biblioteca", erro);
                ultimoErro = "Não foi possível carregar os conteúdos. Tente novamente.";
            }
            boolean finalSucesso = sucesso;
            PRINCIPAL.post(() -> retorno.aoConcluir(finalSucesso, saida));
        });
    }

    public void listarQuizzes(@NonNull Context contexto, @NonNull Retorno<QuizResumo> retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    List<QuizResumo> quizzes = new ArrayList<>();
                    boolean sucesso = false;

                    try {
                        JSONArray cursos = carregarCursosComAulas(contextoApp);

                        List<Future<QuizResumo>> pendentes = new ArrayList<>();

                        try {
                            for (int i = 0; i < cursos.length(); i++) {
                                JSONObject curso = cursos.getJSONObject(i);
                                JSONArray aulas = curso.getJSONArray("aulas");
                                String tituloCurso = curso.optString("titulo");

                                for (int j = 0; j < aulas.length(); j++) {
                                    JSONObject aula = aulas.getJSONObject(j);
                                    int aulaId = aula.getInt("id");
                                    String tituloAula = aula.optString("titulo");

                                    pendentes.add(EXECUTOR_CONSULTAS.submit(() -> {
                                        String corpo = obter(contextoApp, "/api/aulas/" + aulaId + "/quiz");
                                        return corpo == null ? null : QuizJson.montar(
                                                tituloCurso, tituloAula, new JSONObject(corpo));
                                    }));
                                }
                            }

                            for (Future<QuizResumo> pendente : pendentes) {
                                QuizResumo quiz = aguardar(pendente);
                                if (quiz != null) {
                                    quizzes.add(quiz);
                                }
                            }
                        } finally {
                            cancelarPendentes(pendentes);
                        }

                        sucesso = true;

                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível carregar os quizzes", erro);
                    }

                    boolean sucessoFinal = sucesso;

                    PRINCIPAL.post(() -> retorno.aoConcluir(sucessoFinal, quizzes));
                });
    }

    public void listarGuia(@NonNull Context contexto, @NonNull Retorno<ItemLista> retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    List<ItemLista> itens = new ArrayList<>();
                    boolean sucesso = false;

                    try {
                        JSONArray cursos = carregarCursosComAulas(contextoApp);

                        for (int i = 0; i < cursos.length(); i++) {
                            JSONObject curso = cursos.getJSONObject(i);

                            JSONArray aulas = curso.getJSONArray("aulas");

                            for (int j = 0; j < aulas.length(); j++) {
                                JSONObject aula = aulas.getJSONObject(j);

                                String tituloCurso = curso.optString("titulo");

                                itens.add(
                                        new ItemLista(
                                                "aula:" + aula.optInt("id"),
                                                aula.optString("titulo"),
                                                tituloCurso,
                                                "",
                                                new String[][] {
                                                    {"Curso", tituloCurso},
                                                    {FiltroGuia.ROTULO_CONTEUDO, aula.optString("conteudo")}
                                                }));
                            }
                        }

                        sucesso = true;

                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível carregar o guia", erro);
                    }

                    boolean sucessoFinal = sucesso;

                    PRINCIPAL.post(() -> retorno.aoConcluir(sucessoFinal, itens));
                });
    }

    @NonNull
    private JSONArray carregarCursosComAulas(@NonNull Context contexto) throws IOException, JSONException {

        synchronized (CACHE_LOCK) {
            if (cacheCursos != null && System.currentTimeMillis() - cacheEm < VALIDADE_CACHE_MS) {
                return cacheCursos;
            }
        }

        JSONArray cursos = obterArray(contexto, "/api/cursos");

        List<Future<JSONArray>> pendentes = new ArrayList<>();

        try {
            for (int i = 0; i < cursos.length(); i++) {
                int cursoId = cursos.getJSONObject(i).getInt("id");
                pendentes.add(EXECUTOR_CONSULTAS.submit(
                        () -> obterArray(contexto, "/api/cursos/" + cursoId + "/aulas")));
            }

            for (int i = 0; i < cursos.length(); i++) {
                cursos.getJSONObject(i).put("aulas", QuizJson.ordenarPorOrdem(aguardar(pendentes.get(i))));
            }
        } finally {
            cancelarPendentes(pendentes);
        }

        synchronized (CACHE_LOCK) {
            cacheCursos = cursos;
            cacheEm = System.currentTimeMillis();
        }

        return cursos;
    }

    private static <T> T aguardar(@NonNull Future<T> pendente) throws IOException {
        try {
            return pendente.get();
        } catch (InterruptedException erro) {
            Thread.currentThread().interrupt();
            throw new IOException("Consulta interrompida", erro);
        } catch (ExecutionException erro) {
            throw new IOException("Falha na consulta", erro.getCause());
        }
    }

    private static void cancelarPendentes(@NonNull List<? extends Future<?>> pendentes) {
        for (Future<?> pendente : pendentes) {
            if (!pendente.isDone()) {
                pendente.cancel(true);
            }
        }
    }

    @NonNull
    private JSONArray obterArray(@NonNull Context contexto, @NonNull String caminho) throws IOException, JSONException {

        String corpo = obter(contexto, caminho);

        return corpo == null ? new JSONArray() : new JSONArray(corpo);
    }

    // Devolve null em 404 (aula sem quiz, por exemplo); outros erros viram exceção.
    @Nullable
    private String obter(@NonNull Context contexto, @NonNull String caminho) throws IOException {

        String token = autenticacao.tokenParaUso(contexto);

        if (token == null) {
            throw new IOException("Sem credenciais para a API");
        }

        Resposta resposta = requisitar(caminho, token);

        // O JWT dura minutos: o salvo costuma estar vencido.
        if (resposta.codigo == 401 || resposta.codigo == 403) {
            String renovado = autenticacao.renovarToken(contexto);

            if (renovado != null) {
                resposta = requisitar(caminho, renovado);
            }
        }

        if (resposta.codigo == 404) {
            return null;
        }

        if (resposta.codigo < 200 || resposta.codigo >= 300) {
            throw new IOException("HTTP " + resposta.codigo + " em " + caminho);
        }

        return resposta.corpo;
    }

    @NonNull
    private Resposta requisitar(@NonNull String caminho, @NonNull String token) throws IOException {

        HttpURLConnection conexao = (HttpURLConnection) new URL(URL_BASE + caminho).openConnection();

        try {
            conexao.setRequestMethod("GET");
            conexao.setRequestProperty("Accept", "application/json");
            conexao.setRequestProperty("Authorization", "Bearer " + token);
            conexao.setUseCaches(false);
            conexao.setConnectTimeout(180_000);
            conexao.setReadTimeout(180_000);

            int codigo = conexao.getResponseCode();

            InputStream fluxo = codigo >= 400 ? conexao.getErrorStream() : conexao.getInputStream();

            return new Resposta(codigo, LeituraHttp.texto(fluxo));

        } finally {
            conexao.disconnect();
        }
    }

    private static final class Resposta {
        final int codigo;
        final String corpo;

        Resposta(int codigo, String corpo) {
            this.codigo = codigo;
            this.corpo = corpo;
        }
    }
}
