package com.example.ecociente.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.CondominioAtendido;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

/** Consulta os condomínios efetivamente atendidos pela cooperativa do JWT atual. */
public final class CondominiosAtendidosRepository {
    private static final String TAG = "CondominiosAtendidos";
    private static final int TAMANHO_PAGINA = 100;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    public interface Retorno {
        /** null indica falha de rede/autenticação; lista vazia é resposta válida sem atendimentos. */
        void aoConcluir(@Nullable List<CondominioAtendido> condominios);
    }

    public void listar(@NonNull Context contexto, int cooperativaId, @NonNull Retorno retorno) {
        Context app = contexto.getApplicationContext();
        if (cooperativaId <= 0) {
            PRINCIPAL.post(() -> retorno.aoConcluir(null));
            return;
        }
        EXECUTOR.execute(() -> {
            List<CondominioAtendido> todos = new ArrayList<>();
            boolean sucesso = true;
            int pagina = 0;
            try {
                while (true) {
                    String url = ConfiguracaoServidores.MOBILE_API
                            + "/api/cooperativas/" + cooperativaId
                            + "/condominios?page=" + pagina + "&size=" + TAMANHO_PAGINA;
                    String corpo = ApiAutenticada.obter(app, url);
                    if (corpo == null) {
                        sucesso = false;
                        break;
                    }
                    JSONObject resposta = new JSONObject(corpo);
                    todos.addAll(CondominiosAtendidosJson.ler(resposta));
                    long totalPaginas = resposta.optLong("totalPaginas", 0);
                    if (pagina + 1 >= totalPaginas || resposta.optJSONArray("condominios") == null) break;
                    pagina++;
                }
            } catch (Exception erro) {
                sucesso = false;
                Log.w(TAG, "Não foi possível carregar os condomínios atendidos", erro);
            }
            List<CondominioAtendido> resultado = sucesso ? todos : null;
            PRINCIPAL.post(() -> retorno.aoConcluir(resultado));
        });
    }
}
