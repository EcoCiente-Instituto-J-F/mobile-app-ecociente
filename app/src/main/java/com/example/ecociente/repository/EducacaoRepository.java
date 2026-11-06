package com.example.ecociente.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.FiltroGuia;
import com.example.ecociente.model.DadosDemonstracaoEducacao;
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
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Cursos, aulas e quizzes da ds-mobile-api (só leitura, exige o JWT da ds-mobile-api). O guia mostra as aulas; cada aula com quiz vira um quiz da lista.
public class EducacaoRepository {

    private static final String TAG = "EducacaoRepository";

    // Trocar quando o deploy do ds-mobile-api estiver no ar.
    private static final String URL_BASE = ConfiguracaoServidores.MOBILE_API;

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final ExecutorService REDE = Executors.newFixedThreadPool(3);
    private static final long PRAZO_MS = 10_000L;

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

    private <T> List<T> consultarComPrazo(Callable<List<T>> tarefa, List<T> demonstracao) {
        Future<List<T>> consulta = REDE.submit(tarefa);
        try {
            List<T> encontrados = consulta.get(PRAZO_MS, TimeUnit.MILLISECONDS);
            return encontrados == null || encontrados.isEmpty() ? demonstracao : encontrados;
        } catch (TimeoutException erro) {
            Log.w(TAG, "Consulta ultrapassou 10 segundos; ativando demonstracao");
            consulta.cancel(true);
            return demonstracao;
        } catch (Exception erro) {
            Log.w(TAG, "Consulta falhou; ativando demonstracao", erro);
            consulta.cancel(true);
            return demonstracao;
        }
    }

    public void listarCursos(@NonNull Context contexto, @NonNull Retorno<CursoEducativo> retorno) {
        Context app = contexto.getApplicationContext();
        EXECUTOR.execute(() -> {
            List<CursoEducativo> itens = consultarComPrazo(() -> {
                JSONArray cursos = carregarCursosComAulas(app);
                List<CursoEducativo> encontrados = new ArrayList<>();
                for (int i = 0; i < cursos.length(); i++) {
                    JSONObject cursoJson = cursos.getJSONObject(i);
                    JSONArray aulasJson = cursoJson.optJSONArray("aulas");
                    List<CursoEducativo.Aula> aulas = new ArrayList<>();
                    for (int j = 0; aulasJson != null && j < aulasJson.length(); j++) {
                        JSONObject aula = aulasJson.getJSONObject(j);
                        aulas.add(new CursoEducativo.Aula(
                                aula.getInt("id"), aula.optString("titulo"),
                                aula.optString("conteudo"), aula.optInt("ordem", j + 1)));
                    }
                    aulas.sort(Comparator.comparingInt(a -> a.ordem));
                    encontrados.add(new CursoEducativo(cursoJson.getInt("id"),
                            cursoJson.optString("titulo"), cursoJson.optString("descricao"), aulas));
                }
                return encontrados;
            }, DadosDemonstracaoEducacao.cursos());
            boolean demonstracao = !itens.isEmpty() && itens.get(0).id < 0;
            ultimoErro = demonstracao ? "Conteudo demonstrativo: API indisponivel ou lenta." : "";
            PRINCIPAL.post(() -> retorno.aoConcluir(true, itens));
        });
    }

    public void listarQuizzes(@NonNull Context contexto, @NonNull Retorno<QuizResumo> retorno) {
        Context app = contexto.getApplicationContext();
        EXECUTOR.execute(() -> {
            List<QuizResumo> itens = consultarComPrazo(() -> {
                JSONArray cursos = carregarCursosComAulas(app);
                List<QuizResumo> quizzes = new ArrayList<>();
                for (int i = 0; i < cursos.length(); i++) {
                    JSONObject curso = cursos.getJSONObject(i);
                    JSONArray aulas = curso.optJSONArray("aulas");
                    for (int j = 0; aulas != null && j < aulas.length(); j++) {
                        JSONObject aula = aulas.getJSONObject(j);
                        String corpo = obter(app, "/api/aulas/" + aula.getInt("id") + "/quiz");
                        if (corpo != null) {
                            quizzes.add(QuizJson.montar(
                                    curso.optString("titulo"), aula.optString("titulo"),
                                    new JSONObject(corpo)));
                        }
                    }
                }
                return quizzes;
            }, DadosDemonstracaoEducacao.quizzes());
            PRINCIPAL.post(() -> retorno.aoConcluir(true, itens));
        });
    }

    public void listarGuia(@NonNull Context contexto, @NonNull Retorno<ItemLista> retorno) {
        Context app = contexto.getApplicationContext();
        EXECUTOR.execute(() -> {
            List<ItemLista> demonstracao = itensDoGuia(DadosDemonstracaoEducacao.cursos());
            List<ItemLista> itens = consultarComPrazo(() -> {
                JSONArray cursos = carregarCursosComAulas(app);
                List<ItemLista> guia = new ArrayList<>();
                for (int i = 0; i < cursos.length(); i++) {
                    JSONObject curso = cursos.getJSONObject(i);
                    JSONArray aulas = curso.optJSONArray("aulas");
                    for (int j = 0; aulas != null && j < aulas.length(); j++) {
                        JSONObject aula = aulas.getJSONObject(j);
                        String tituloCurso = curso.optString("titulo");
                        guia.add(new ItemLista(
                                "aula:" + aula.optInt("id"),
                                aula.optString("titulo"), tituloCurso, "",
                                new String[][] {
                                    {"Curso", tituloCurso},
                                    {FiltroGuia.ROTULO_CONTEUDO, aula.optString("conteudo")}
                                }));
                    }
                }
                return guia;
            }, demonstracao);
            PRINCIPAL.post(() -> retorno.aoConcluir(true, itens));
        });
    }

    private static List<ItemLista> itensDoGuia(List<CursoEducativo> cursos) {
        List<ItemLista> itens = new ArrayList<>();
        for (CursoEducativo curso : cursos) {
            for (CursoEducativo.Aula aula : curso.aulas) {
                itens.add(new ItemLista(
                        "aula:" + aula.id,
                        aula.titulo + " (Demonstração)", curso.titulo, "",
                        new String[][] {
                            {"Curso", curso.titulo},
                            {FiltroGuia.ROTULO_CONTEUDO, aula.conteudo}
                        }));
            }
        }
        return itens;
    }

    @NonNull
    private JSONArray carregarCursosComAulas(@NonNull Context contexto) throws IOException, JSONException {

        synchronized (CACHE_LOCK) {
            if (cacheCursos != null && System.currentTimeMillis() - cacheEm < VALIDADE_CACHE_MS) {
                return cacheCursos;
            }
        }

        JSONArray cursos = obterArray(contexto, "/api/cursos");

        for (int i = 0; i < cursos.length(); i++) {
            if (Thread.currentThread().isInterrupted()) throw new IOException("Consulta cancelada");
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
            conexao.setConnectTimeout(5_000);
            conexao.setReadTimeout(10_000);

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
