package com.example.ecociente.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.ResumoDashboard;
import com.example.ecociente.model.ResultadoSolicitacoes;
import com.example.ecociente.model.SindicoCooperativa;
import com.example.ecociente.model.Solicitacao;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

// Materiais, pontos e quizzes vêm de GET /api/condominios/{id}/dashboard (ds-mobile-api, view
// vw_dashboard_sindico); as coletas por cooperativa, do calendário (GET /api/v1/agendamentos).
public class DashboardSindicoRepository {

    private static final String TAG = "DashboardSindico";

    private static final String URL_BASE = "https://ds-mobile-api.onrender.com/api/condominios/";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    public interface Retorno {
        // null quando a API não respondeu.
        void aoConcluir(@Nullable ResumoDashboard resumo);
    }

    private JSONObject dashboard;
    private List<Solicitacao> coletas;
    private Map<Integer, String> cooperativas = new HashMap<>();
    private int pendentes;

    public void carregar(@NonNull Context contexto, int condominioId, @NonNull Retorno retorno) {

        Context contextoApp = contexto.getApplicationContext();

        dashboard = null;
        coletas = null;
        pendentes = 3;

        EXECUTOR.execute(
                () -> {
                    JSONObject envelope = null;

                    try {
                        String corpo = ApiAutenticada.obter(contextoApp, URL_BASE + condominioId + "/dashboard");

                        if (corpo != null) {
                            envelope = new JSONObject(corpo);
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível carregar o dashboard", erro);
                    }

                    JSONObject resultado = envelope;

                    PRINCIPAL.post(() -> {
                        dashboard = resultado;
                        concluir(retorno);
                    });
                });

        new CalendarioExternoRepository()
                .listarTodos(
                        contextoApp,
                        resultado -> {
                            if (resultado.getTipo() == ResultadoSolicitacoes.Tipo.SUCESSO) {
                                coletas = resultado.getItens();
                            }
                            concluir(retorno);
                        });

        new CooperativaRepository()
                .listar(
                        contextoApp,
                        lista -> {
                            Map<Integer, String> nomes = new HashMap<>();

                            for (SindicoCooperativa cooperativa : lista == null ? new ArrayList<SindicoCooperativa>() : lista) {
                                nomes.put(cooperativa.getId(), cooperativa.getNome());
                            }

                            cooperativas = nomes;
                            concluir(retorno);
                        });
    }

    // As três consultas respondem na thread principal; a última a chegar monta o resumo.
    private void concluir(@NonNull Retorno retorno) {

        if (--pendentes > 0) {
            return;
        }

        retorno.aoConcluir(
                dashboard == null || coletas == null
                        ? null
                        : DashboardSindicoJson.resumir(dashboard, coletas, cooperativas));
    }
}
