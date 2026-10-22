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
// (ou o usuário não tiver vínculo), o id fica 0.
public class PerfilApiRepository {

    private static final String TAG = "PerfilApi";

    private static final String URL_PERFIL = "https://ds-mobile-api.onrender.com/api/usuarios/me";

    private static final String PREFERENCIAS = "perfil_api";
    private static final String CHAVE_CONDOMINIO_ID = "condominioId";
    private static final String CHAVE_CONDOMINIO_NOME = "condominioNome";
    private static final String CHAVE_COOPERATIVA_ID = "cooperativaId";
    private static final String CHAVE_ATUALIZADO_EM = "atualizadoEm";
    private static final long VALIDADE_MS = 6 * 60 * 60_000L;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    // Id do condomínio do usuário; 0 enquanto a API não informar um (sem vínculo ou sem resposta).
    public static int condominioId(@NonNull Context contexto) {
        return Math.max(0, preferencias(contexto).getInt(CHAVE_CONDOMINIO_ID, 0));
    }

    // Id da cooperativa do usuário (perfil COOPERATIVA); 0 se a API ainda não informou.
    public static int cooperativaId(@NonNull Context contexto) {
        return preferencias(contexto).getInt(CHAVE_COOPERATIVA_ID, 0);
    }

    // Nome do condomínio vindo da API; null se ainda não veio.
    @Nullable
    public static String condominioNome(@NonNull Context contexto) {
        String nome = preferencias(contexto).getString(CHAVE_CONDOMINIO_NOME, null);

        return nome == null || nome.trim().isEmpty() ? null : nome;
    }

    // Para quem precisa de um texto e prefere vazio a inventar um nome.
    @NonNull
    public static String condominioNomeOuVazio(@NonNull Context contexto) {
        String nome = condominioNome(contexto);

        return nome != null ? nome : "";
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

        executar(contextoApp, aoConcluir);
    }

    // Consulta a API mesmo que o que está guardado ainda valha (por exemplo, quando falta um dado).
    public void atualizar(@NonNull Context contexto, @NonNull Runnable aoConcluir) {
        executar(contexto.getApplicationContext(), aoConcluir);
    }

    private void executar(@NonNull Context contextoApp, @Nullable Runnable aoConcluir) {

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

            JSONObject perfil = new JSONObject(corpo);

            PerfilApiJson.Condominio condominio = PerfilApiJson.condominio(perfil);

            SharedPreferences.Editor editor = preferencias(contexto).edit();

            if (condominio != null) {
                editor.putInt(CHAVE_CONDOMINIO_ID, condominio.id);
                editor.putString(CHAVE_CONDOMINIO_NOME, condominio.nome);
            }

            int cooperativaId = PerfilApiJson.cooperativaId(perfil);

            if (cooperativaId > 0) {
                editor.putInt(CHAVE_COOPERATIVA_ID, cooperativaId);
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
