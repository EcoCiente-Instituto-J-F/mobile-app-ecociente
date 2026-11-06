package com.example.ecociente.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.DesempenhoQuiz;
import com.example.ecociente.model.QuizConteudo;
import com.example.ecociente.model.ResultadoQuiz;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;

// Tentativas de quiz da ds-mobile-api: abrir, finalizar (a API corrige e pontua) e buscar a revisão.
public class TentativaQuizRepository {

    private static final String TAG = "TentativaQuiz";

    private static final String URL_BASE = ConfiguracaoServidores.MOBILE_API;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    public interface Retorno<T> {
        // null quando a API não respondeu (sem login, fora do ar, quiz sem permissão).
        void aoConcluir(@Nullable T resultado);
    }

    public static final class Conclusao {
        @NonNull public final ResultadoQuiz resultado;
        @NonNull public final List<QuizConteudo.Pergunta> perguntasComGabarito;

        Conclusao(@NonNull ResultadoQuiz resultado, @NonNull List<QuizConteudo.Pergunta> perguntasComGabarito) {
            this.resultado = resultado;
            this.perguntasComGabarito = perguntasComGabarito;
        }
    }

    private interface Leitor<T> {
        T ler(String corpo) throws Exception;
    }

    // Últimas tentativas concluídas do usuário autenticado (GET /api/tentativas-quiz). Lista vazia = consulta feita sem resultados; null = falha (sem login, API fora do ar ou resposta inválida).
    public void historico(@NonNull Context contexto, @NonNull Retorno<List<DesempenhoQuiz>> retorno) {
        consultar(contexto, "/api/tentativas-quiz?limite=20",
                corpo -> HistoricoQuizJson.desempenhos(new JSONArray(corpo)), retorno);
    }

    // Ids de todos os quizzes que o usuário já concluiu, aprovado ou não. Mesma convenção do histórico.
    public void quizzesConcluidos(@NonNull Context contexto, @NonNull Retorno<Set<Integer>> retorno) {
        consultar(contexto, "/api/tentativas-quiz/quizzes-concluidos",
                corpo -> QuizzesConcluidosJson.ids(new JSONArray(corpo)), retorno);
    }

    private <T> void consultar(
            @NonNull Context contexto, @NonNull String caminho, @NonNull Leitor<T> leitor, @NonNull Retorno<T> retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    T resultado = null;

                    try {
                        String corpo = ApiAutenticada.obter(contextoApp, URL_BASE + caminho);

                        if (corpo != null) {
                            resultado = leitor.ler(corpo);
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível consultar " + caminho, erro);
                    }

                    T resposta = resultado;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resposta));
                });
    }

    // Abre a tentativa assim que a tela de perguntas aparece: acorda a API e marca a hora de início.
    public void iniciar(@NonNull Context contexto, int quizId, @NonNull Retorno<Integer> retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    Integer tentativaId = abrir(contextoApp, quizId);

                    PRINCIPAL.post(() -> retorno.aoConcluir(tentativaId));
                });
    }

    // Finaliza a tentativa (abrindo uma se a do início falhou) e já traz a revisão com o gabarito.
    public void concluir(
            @NonNull Context contexto,
            int quizId,
            @Nullable Integer tentativaAberta,
            @NonNull List<QuizConteudo.Pergunta> perguntas,
            @NonNull int[] respostas,
            @NonNull Retorno<Conclusao> retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    Conclusao conclusao = null;

                    try {
                        Integer tentativaId = tentativaAberta != null ? tentativaAberta : abrir(contextoApp, quizId);

                        if (tentativaId != null) {
                            conclusao = finalizar(contextoApp, tentativaId, perguntas, respostas);
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível finalizar o quiz", erro);
                    }

                    Conclusao resultado = conclusao;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resultado));
                });
    }

    @Nullable
    private Integer abrir(@NonNull Context contexto, int quizId) {

        try {
            String corpo = ApiAutenticada.enviar(contexto, URL_BASE + "/api/quizzes/" + quizId + "/tentativas", "{}");

            if (corpo == null) {
                return null;
            }

            int id = TentativaQuizJson.tentativaId(new JSONObject(corpo));

            return id > 0 ? id : null;

        } catch (Exception erro) {
            Log.w(TAG, "Não foi possível abrir a tentativa do quiz " + quizId, erro);

            return null;
        }
    }

    @Nullable
    private Conclusao finalizar(
            @NonNull Context contexto,
            int tentativaId,
            @NonNull List<QuizConteudo.Pergunta> perguntas,
            @NonNull int[] respostas)
            throws Exception {

        String corpo =
                ApiAutenticada.enviar(
                        contexto,
                        URL_BASE + "/api/tentativas-quiz/" + tentativaId + "/finalizar",
                        TentativaQuizJson.corpoFinalizar(perguntas, respostas));

        if (corpo == null) {
            return null;
        }

        ResultadoQuiz resultado = TentativaQuizJson.resultado(new JSONObject(corpo));

        // Sem a revisão o app não sabe qual era a certa, mas a nota continua valendo.
        String revisao = ApiAutenticada.obter(contexto, URL_BASE + "/api/tentativas-quiz/" + tentativaId + "/revisao");

        List<QuizConteudo.Pergunta> corrigidas =
                revisao != null
                        ? TentativaQuizJson.comGabarito(new JSONObject(revisao), perguntas)
                        : perguntas;

        return new Conclusao(resultado, corrigidas);
    }
}
