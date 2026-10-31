package com.example.ecociente.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PushRegistroRepository {

    private static final String TAG = "PushEcoCiente";
    private static final String BASE = "https://ds-mobile-api.onrender.com";
    private static final String PREFS = "push_ecociente";
    private static final String INSTALACAO = "instalacao";
    private static final String ULTIMO_UID = "ultimo_uid";
    private static final String ULTIMO_TOKEN = "ultimo_token";
    private static final String ULTIMA_SINCRONIZACAO = "ultima_sincronizacao";
    private static final long INTERVALO = 6L * 60L * 60L * 1000L;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private PushRegistroRepository() {}

    @NonNull
    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    @NonNull
    public static String instalacao(@NonNull Context context) {
        SharedPreferences p = prefs(context);
        String salvo = p.getString(INSTALACAO, null);
        if (salvo != null) return salvo;
        String novo = UUID.randomUUID().toString();
        p.edit().putString(INSTALACAO, novo).apply();
        return novo;
    }

    public static void sincronizar(@NonNull Context contexto) {
        Context app = contexto.getApplicationContext();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            if (token != null && !token.isBlank()) registrarToken(app, token);
        }).addOnFailureListener(e -> Log.w(TAG, "Token FCM indisponível", e));
    }

    public static void registrarToken(@NonNull Context contexto, @NonNull String token) {
        Context app = contexto.getApplicationContext();
        var usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) return;
        String uid = usuario.getUid();
        SharedPreferences p = prefs(app);
        long agora = System.currentTimeMillis();
        if (uid.equals(p.getString(ULTIMO_UID, ""))
                && token.equals(p.getString(ULTIMO_TOKEN, ""))
                && agora - p.getLong(ULTIMA_SINCRONIZACAO, 0) < INTERVALO) {
            return;
        }
        FirestoreProvider.obterInstancia().collection("usuarios").document(uid).get()
                .addOnCompleteListener(task -> {
                    JSONObject preferencias = new JSONObject();
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        Object valor = doc.get("preferenciasNotificacao");
                        if (valor instanceof java.util.Map) {
                            java.util.Map<?, ?> mapa = (java.util.Map<?, ?>) valor;
                            for (String chave : new String[]{"notificacoesGerais", "lembretesAvisos", "novasAtividades", "dicasSustentaveis"}) {
                                if (mapa.get(chave) instanceof Boolean) {
                                    try { preferencias.put(chave, mapa.get(chave)); } catch (Exception ignored) {}
                                }
                            }
                        }
                    }
                    EXECUTOR.execute(() -> {
                        if (FirebaseAuth.getInstance().getCurrentUser() == null
                                || !uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) return;
                        try {
                            JSONObject corpo = new JSONObject();
                            corpo.put("instalacaoId", instalacao(app));
                            corpo.put("token", token);
                            corpo.put("preferencias", preferencias);
                            ApiAutenticada.Resposta resposta = ApiAutenticada.enviarComCodigo(
                                    app, BASE + "/api/notificacoes/dispositivos", corpo.toString());
                            if (resposta != null && resposta.sucesso()) {
                                p.edit().putString(ULTIMO_UID, uid).putString(ULTIMO_TOKEN, token)
                                        .putLong(ULTIMA_SINCRONIZACAO, System.currentTimeMillis()).apply();
                            } else {
                                Log.w(TAG, "Registro de dispositivo ainda indisponível");
                            }
                        } catch (Exception erro) {
                            Log.w(TAG, "Falha ao registrar dispositivo", erro);
                        }
                    });
                });
    }

    public static void sincronizarPreferencias(@NonNull Context contexto) {
        prefs(contexto).edit().remove(ULTIMA_SINCRONIZACAO).apply();
        sincronizar(contexto);
    }

    public static void encerrarSessao(@NonNull Context contexto) {
        Context app = contexto.getApplicationContext();
        prefs(app).edit().remove(ULTIMO_UID).remove(ULTIMO_TOKEN).remove(ULTIMA_SINCRONIZACAO).apply();
        EXECUTOR.execute(() -> {
            try {
                ApiAutenticada.excluirComCodigo(app, BASE + "/api/notificacoes/dispositivos/" + instalacao(app));
            } catch (Exception erro) {
                Log.w(TAG, "Não foi possível desativar o dispositivo", erro);
            }
        });
    }
}
