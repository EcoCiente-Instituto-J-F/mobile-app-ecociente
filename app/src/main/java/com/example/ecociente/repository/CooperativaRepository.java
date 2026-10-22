package com.example.ecociente.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.SindicoCooperativa;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

// GET /api/cooperativas da ds-mobile-api: cooperativas da mesma cidade do condomínio do síndico.
public class CooperativaRepository {

    private static final String TAG = "CooperativaRepository";

    private static final String URL_COOPERATIVAS = "https://ds-mobile-api.onrender.com/api/cooperativas?size=100";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    public interface Retorno {
        // null quando a API não respondeu (sem credenciais, não é síndico, fora do ar).
        void aoConcluir(@Nullable List<SindicoCooperativa> cooperativas);
    }

    public void listar(@NonNull Context contexto, @NonNull Retorno retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    List<SindicoCooperativa> cooperativas = null;

                    try {
                        String corpo = ApiAutenticada.obter(contextoApp, URL_COOPERATIVAS);

                        if (corpo != null) {
                            cooperativas = CooperativaJson.cooperativas(new JSONObject(corpo));
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível carregar as cooperativas", erro);
                    }

                    List<SindicoCooperativa> resultado = cooperativas;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resultado));
                });
    }
}
