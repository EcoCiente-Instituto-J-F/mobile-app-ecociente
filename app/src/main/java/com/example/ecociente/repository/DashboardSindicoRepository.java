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

// Não existe API de dashboard: os números saem das últimas postagens do condomínio
// (GET /api/postagens) e das coletas (GET /api/v1/agendamentos).
public class DashboardSindicoRepository {

    private static final String TAG = "DashboardSindico";

    private static final String URL_POSTAGENS = "https://ds-mobile-api.onrender.com/api/postagens?condominioId=";

    private static final int POR_PAGINA = 100;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    public interface Retorno {
        // null quando a API não respondeu.
        void aoConcluir(@Nullable ResumoDashboard resumo);
    }

    private JSONObject postagens;
    private List<Solicitacao> coletas;
    private Map<Integer, String> cooperativas = new HashMap<>();
    private int pendentes;

    public void carregar(@NonNull Context contexto, int condominioId, @NonNull Retorno retorno) {

        Context contextoApp = contexto.getApplicationContext();

        postagens = null;
        coletas = null;
        pendentes = 3;

        EXECUTOR.execute(
                () -> {
                    JSONObject envelope = null;

                    try {
                        String corpo = ApiAutenticada.obter(contextoApp, URL_POSTAGENS + condominioId + "&size=" + POR_PAGINA);

                        if (corpo != null) {
                            envelope = new JSONObject(corpo);
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível carregar as postagens", erro);
                    }

                    JSONObject resultado = envelope;

                    PRINCIPAL.post(() -> {
                        postagens = resultado;
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
                postagens == null || coletas == null
                        ? null
                        : DashboardSindicoJson.resumir(postagens, coletas, cooperativas, System.currentTimeMillis()));
    }
}
