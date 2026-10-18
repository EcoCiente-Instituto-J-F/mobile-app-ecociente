package com.example.ecociente.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

// GET /api/usuarios/me da ds-mobile-api: de onde o app descobre o condomínio do usuário.
// O resultado fica guardado para as telas lerem na hora; enquanto a API não responder
// (ou o usuário não tiver vínculo), vale o condomínio de demonstração.
public class PerfilApiRepository {

    private static final String TAG = "PerfilApi";

    private static final String URL_PERFIL = "https://ds-mobile-api.onrender.com/api/usuarios/me";

    // Condomínio do síndico de teste, usado enquanto a API não devolver o do usuário.
    public static final int CONDOMINIO_DE_DEMONSTRACAO = 1;

    private static final String PREFERENCIAS = "perfil_api";
    private static final String CHAVE_CONDOMINIO_ID = "condominioId";
    private static final String CHAVE_CONDOMINIO_NOME = "condominioNome";
    private static final String CHAVE_ATUALIZADO_EM = "atualizadoEm";
    private static final long VALIDADE_MS = 6 * 60 * 60_000L;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    // Id do condomínio do usuário; o de demonstração enquanto não houver o real.
    public static int condominioId(@NonNull Context contexto) {
        int salvo = preferencias(contexto).getInt(CHAVE_CONDOMINIO_ID, 0);

        return salvo > 0 ? salvo : CONDOMINIO_DE_DEMONSTRACAO;
    }

    // Nome do condomínio vindo da API; null se ainda não veio.
    @Nullable
    public static String condominioNome(@NonNull Context contexto) {
        String nome = preferencias(contexto).getString(CHAVE_CONDOMINIO_NOME, null);

        return nome == null || nome.trim().isEmpty() ? null : nome;
    }

    // Esquece o que era do usuário anterior (troca de conta).
    static void limpar(@NonNull Context contexto) {
        preferencias(contexto).edit().clear().apply();
    }

    // Atualiza em segundo plano se o que está guardado já passou da validade.
    public void atualizarSeNecessario(@NonNull Context contexto) {
        atualizarSeNecessario(contexto, null);
    }

    // `aoConcluir` roda na thread principal quando a consulta termina, com sucesso ou não,
    // e na hora se o que está guardado ainda vale.
    public void atualizarSeNecessario(@NonNull Context contexto, @Nullable Runnable aoConcluir) {

        Context contextoApp = contexto.getApplicationContext();

        long idade = System.currentTimeMillis() - preferencias(contextoApp).getLong(CHAVE_ATUALIZADO_EM, 0);

        if (idade < VALIDADE_MS) {
            if (aoConcluir != null) {
                PRINCIPAL.post(aoConcluir);
            }
            return;
        }

        EXECUTOR.execute(
                () -> {
                    buscarEGuardar(contextoApp);

                    if (aoConcluir != null) {
                        PRINCIPAL.post(aoConcluir);
                    }
                });
    }

    private void buscarEGuardar(@NonNull Context contexto) {

        try {
            String corpo = ApiAutenticada.obter(contexto, URL_PERFIL);

            if (corpo == null) {
                return;
            }

            PerfilApiJson.Condominio condominio = PerfilApiJson.condominio(new JSONObject(corpo));

            SharedPreferences.Editor editor = preferencias(contexto).edit();

            if (condominio != null) {
                editor.putInt(CHAVE_CONDOMINIO_ID, condominio.id);
                editor.putString(CHAVE_CONDOMINIO_NOME, condominio.nome);
            }

            editor.putLong(CHAVE_ATUALIZADO_EM, System.currentTimeMillis()).apply();

        } catch (Exception erro) {
            Log.w(TAG, "Não foi possível carregar o perfil da API", erro);
        }
    }

    @NonNull
    private static SharedPreferences preferencias(@NonNull Context contexto) {
        return contexto.getApplicationContext().getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
    }
}
