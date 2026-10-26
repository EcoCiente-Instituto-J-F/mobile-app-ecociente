package com.example.ecociente.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.FiltroGuia;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.model.QuizConteudo;
import com.example.ecociente.model.QuizResumo;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Cursos, aulas e quizzes da ds-mobile-api (só leitura, exige o JWT da ds-autenticacao-api).
// O guia mostra as aulas; cada aula com quiz vira um quiz da lista.
public class EducacaoRepository {

    private static final String TAG = "EducacaoRepository";

    // Trocar quando o deploy do ds-mobile-api estiver no ar.
    private static final String URL_BASE = "https://ds-mobile-api.onrender.com";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    private final AutenticacaoExternaRepository autenticacao = new AutenticacaoExternaRepository();

    private static final long VALIDADE_CACHE_MS = 5 * 60_000L;
    private static final Object CACHE_LOCK = new Object();
    private static JSONArray cacheCursos;
    private static long cacheEm;

    public interface Retorno<T> {
        void aoConcluir(boolean sucesso, @NonNull List<T> itens);
    }

    public void listarQuizzes(@NonNull Context contexto, @NonNull Retorno<QuizResumo> retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    List<QuizResumo> quizzes = new ArrayList<>();
                    boolean sucesso = false;

                    try {
                        JSONArray cursos = carregarCursosComAulas(contextoApp);

                        for (int i = 0; i < cursos.length(); i++) {
                            JSONObject curso = cursos.getJSONObject(i);

                            JSONArray aulas = curso.getJSONArray("aulas");

                            for (int j = 0; j < aulas.length(); j++) {
                                JSONObject aula = aulas.getJSONObject(j);

                                String corpo =
                                        obter(contextoApp, "/api/aulas/" + aula.getInt("id") + "/quiz");

                                if (corpo == null) {
                                    continue;
                                }

                                quizzes.add(
                                        QuizJson.montar(
                                                curso.optString("titulo"),
                                                aula.optString("titulo"),
                                                new JSONObject(corpo)));
                            }
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

    // Cursos com as aulas ordenadas, buscados uma vez e reaproveitados por alguns minutos: o guia
    // e a lista de quizzes pedem a mesma árvore, e cada pedido pode acordar o Render.
    @NonNull
    private JSONArray carregarCursosComAulas(@NonNull Context contexto) throws IOException, JSONException {

        synchronized (CACHE_LOCK) {
            if (cacheCursos != null && System.currentTimeMillis() - cacheEm < VALIDADE_CACHE_MS) {
                return cacheCursos;
            }
        }

        JSONArray cursos = obterArray(contexto, "/api/cursos");

        for (int i = 0; i < cursos.length(); i++) {
            JSONObject curso = cursos.getJSONObject(i);

            curso.put(
                    "aulas",
                    QuizJson.ordenarPorOrdem(
                            obterArray(contexto, "/api/cursos/" + curso.getInt("id") + "/aulas")));
        }

        synchronized (CACHE_LOCK) {
            cacheCursos = cursos;
            cacheEm = System.currentTimeMillis();
        }

        return cursos;
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
            // Render dorme a API após inatividade (cold start de 70 a 150 s).
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
