package com.example.ecociente.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.AvaliacoesCooperativa;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

// GET /api/cooperativas/{id}/avaliacoes da ds-mobile-api (exige o JWT da ds-mobile-api).
public class AvaliacaoRepository {

    private static final String TAG = "AvaliacaoRepository";

    private static final String URL_BASE = ConfiguracaoServidores.MOBILE_API;

    private static final int POR_PAGINA = 50;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    public interface Retorno {
        // null quando a API não respondeu; sem avaliações, a lista vem vazia.
        void aoConcluir(@Nullable AvaliacoesCooperativa avaliacoes);
    }

    public void listar(@NonNull Context contexto, int cooperativaId, @NonNull Retorno retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    AvaliacoesCooperativa avaliacoes = null;

                    try {
                        String corpo =
                                ApiAutenticada.obter(
                                        contextoApp,
                                        URL_BASE + "/api/cooperativas/" + cooperativaId
                                                + "/avaliacoes?page=0&size=" + POR_PAGINA);

                        if (corpo != null) {
                            avaliacoes = AvaliacaoJson.avaliacoes(new JSONObject(corpo));
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível carregar as avaliações", erro);
                    }

                    AvaliacoesCooperativa resultado = avaliacoes;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resultado));
                });
    }
}
