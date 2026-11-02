package com.example.ecociente.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;

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
    private static final String PREFS = "push_ecociente";
    private static final String INSTALACAO = "instalacao";
    private static final String ULTIMO_UID = "ultimo_uid";
    private static final String PREFERENCIAS_UID = "preferencias_uid";
    private static final String PREFERENCIAS_JSON = "preferencias_json";
    private static final String ULTIMO_TOKEN = "ultimo_token";
    private static final String ULTIMA_SINCRONIZACAO = "ultima_sincronizacao";
    private static final long INTERVALO = 6L * 60L * 60L * 1000L;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private PushRegistroRepository() {}

    private static String base(Context context) {
        return context.getString(com.example.ecociente.R.string.educacao_api_base_url).replaceAll("/+$", "");
    }

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
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Firestore indisponivel para ler preferencias; registro suspenso",
                                task.getException());
                        return;
                    }
                    if (task.getResult() == null || !task.getResult().exists()) {
                        Log.w(TAG, "Perfil ausente no Firestore; registro suspenso");
                        return;
                    }
                    DocumentSnapshot doc = task.getResult();
                    Object valor = doc.get("preferenciasNotificacao");
                    java.util.Map<?, ?> mapa = valor instanceof java.util.Map
                            ? (java.util.Map<?, ?>) valor : null;
                    if (mapa == null && doc.contains("preferenciasNotificacao")) {
                        Log.w(TAG, "Preferencias remotas invalidas");
                        return;
                    }
                    JSONObject preferencias = new JSONObject();
                    for (String chave : new String[]{"notificacoesGerais", "lembretesAvisos",
                            "novasAtividades", "dicasSustentaveis"}) {
                        Object campo = mapa == null ? Boolean.TRUE : mapa.get(chave);
                        if (campo == null) campo = Boolean.TRUE;
                        if (!(campo instanceof Boolean)) {
                            Log.w(TAG, "Preferencia remota invalida: " + chave);
                            return;
                        }
                        try {
                            preferencias.put(chave, campo);
                        } catch (Exception erro) {
                            Log.w(TAG, "Nao foi possivel preparar preferencias", erro);
                            return;
                        }
                    }
                    p.edit().putString(PREFERENCIAS_UID, uid)
                            .putString(PREFERENCIAS_JSON, preferencias.toString()).apply();
                    EXECUTOR.execute(() -> {
                        if (FirebaseAuth.getInstance().getCurrentUser() == null
                                || !uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) return;
                        try {
                            JSONObject corpo = new JSONObject();
                            corpo.put("instalacaoId", instalacao(app));
                            corpo.put("token", token);
                            corpo.put("preferencias", preferencias);
                            ApiAutenticada.Resposta resposta = ApiAutenticada.enviarComCodigo(
                                    app, base(app) + "/api/notificacoes/dispositivos", corpo.toString());
                            if (resposta != null && resposta.sucesso()) {
                                p.edit().putString(ULTIMO_UID, uid).putString(ULTIMO_TOKEN, token)
                                        .putLong(ULTIMA_SINCRONIZACAO, System.currentTimeMillis()).apply();
                                Log.i(TAG, "Dispositivo FCM registrado na API (HTTP " + resposta.codigo + ")");
                            } else if (resposta == null) {
                                Log.w(TAG, "Sem JWT da API mobile para registrar dispositivo; verifique login na API");
                            } else {
                                Log.w(TAG, "Falha HTTP ao registrar dispositivo: " + resposta.codigo);
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

    public static boolean notificacaoPermitida(@NonNull Context contexto,
            @NonNull String uid, @NonNull String grupo) {
        SharedPreferences p = prefs(contexto);
        if (!uid.equals(p.getString(PREFERENCIAS_UID, ""))) return false;
        try {
            JSONObject preferencias = new JSONObject(p.getString(PREFERENCIAS_JSON, ""));
            return preferencias.optBoolean("notificacoesGerais", false)
                    && preferencias.optBoolean(grupo, false);
        } catch (Exception erro) {
            return false;
        }
    }

    public static void encerrarSessao(@NonNull Context contexto) {
        Context app = contexto.getApplicationContext();
        String jwt = AutenticacaoExternaRepository.obterTokenSalvo(app);
        String idInstalacao = instalacao(app);
        prefs(app).edit().remove(ULTIMO_UID).remove(ULTIMO_TOKEN)
                .remove(ULTIMA_SINCRONIZACAO).remove(PREFERENCIAS_UID)
                .remove(PREFERENCIAS_JSON).apply();
        if (jwt == null || jwt.isBlank()) {
            Log.w(TAG, "Sem JWT para desativar token no logout");
            return;
        }
        EXECUTOR.execute(() -> {
            HttpURLConnection conexao = null;
            try {
                conexao = (HttpURLConnection) new URL(base(app)
                        + "/api/notificacoes/dispositivos/" + idInstalacao).openConnection();
                conexao.setRequestMethod("DELETE");
                conexao.setRequestProperty("Authorization", "Bearer " + jwt);
                conexao.setConnectTimeout(10000);
                conexao.setReadTimeout(10000);
                int codigo = conexao.getResponseCode();
                if (codigo < 200 || codigo >= 300) {
                    Log.w(TAG, "Falha HTTP ao desativar dispositivo: " + codigo);
                }
            } catch (IOException erro) {
                Log.w(TAG, "Nao foi possivel desativar dispositivo", erro);
            } finally {
                if (conexao != null) conexao.disconnect();
            }
        });
    }
}
