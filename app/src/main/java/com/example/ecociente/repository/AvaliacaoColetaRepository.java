package com.example.ecociente.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.ResultadoApi;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// O síndico avalia uma coleta realizada (ds-mobile-api); a nota entra na média da cooperativa.
public class AvaliacaoColetaRepository {

    private static final String TAG = "AvaliacaoColeta";

    private static final String URL_BASE = "https://ds-mobile-api.onrender.com";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    public interface Retorno {
        void aoConcluir(@NonNull ResultadoApi resultado);
    }

    public void avaliar(
            @NonNull Context contexto,
            int agendamentoId,
            int nota,
            @Nullable String comentario,
            @NonNull Retorno retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    ResultadoApi resultado;

                    try {
                        ApiAutenticada.Resposta resposta =
                                ApiAutenticada.enviarComCodigo(
                                        contextoApp,
                                        URL_BASE + "/api/agendamentos/" + agendamentoId + "/avaliacao",
                                        AvaliacaoColetaJson.corpo(nota, comentario));

                        if (resposta != null && resposta.sucesso()) {
                            resultado = ResultadoApi.sucesso();
                        } else {
                            resultado =
                                    ResultadoApi.erro(
                                            AvaliacaoColetaJson.mensagemDoErro(
                                                    resposta == null ? 401 : resposta.codigo,
                                                    resposta == null ? null : resposta.corpo));
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível enviar a avaliação", erro);

                        resultado = ResultadoApi.erro(AvaliacaoColetaJson.mensagemDoErro(0, null));
                    }

                    ResultadoApi resultadoFinal = resultado;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resultadoFinal));
                });
    }
}
