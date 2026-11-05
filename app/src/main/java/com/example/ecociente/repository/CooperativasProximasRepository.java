package com.example.ecociente.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.ecociente.model.CooperativaProxima;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CooperativasProximasRepository {
    private static final String TAG = "CooperativasProximas";
    private static final String ENDPOINT =
            "https://ds-mobile-api.onrender.com/api/cooperativas?page=0&size=50";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onResult(@NonNull Resultado resultado);
    }

    public static final class Resultado {
        public final List<CooperativaProxima> cooperativas;
        public final String erro;
        public Resultado(@NonNull List<CooperativaProxima> cooperativas, String erro) {
            this.cooperativas = cooperativas;
            this.erro = erro;
        }
        public boolean sucesso() { return erro == null; }
    }

    public void listar(@NonNull Context context, @NonNull Callback callback) {
        Context app = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            Resultado resultado;
            try {
                resultado = consultar(app);
            } catch (Exception e) {
                Log.e(TAG, "Falha ao consultar cooperativas", e);
                resultado = new Resultado(new ArrayList<>(),
                        "Não foi possível carregar as cooperativas. Verifique sua conexão e tente novamente.");
            }
            Resultado finalResultado = resultado;
            PRINCIPAL.post(() -> callback.onResult(finalResultado));
        });
    }

    @NonNull
    private Resultado consultar(Context context) throws Exception {
        AutenticacaoExternaRepository auth = new AutenticacaoExternaRepository();
        String token = auth.tokenParaUso(context);
        if (token == null) {
            return new Resultado(new ArrayList<>(),
                    "Sua sessão não está disponível. Entre novamente para continuar.");
        }

        int status = 0;
        String body = null;
        for (int tentativa = 0; tentativa < 2; tentativa++) {
            HttpURLConnection connection = (HttpURLConnection) new URL(ENDPOINT).openConnection();
            try {
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                status = connection.getResponseCode();
                if ((status == HttpURLConnection.HTTP_UNAUTHORIZED
                        || status == HttpURLConnection.HTTP_FORBIDDEN) && tentativa == 0) {
                    String renovado = auth.renovarToken(context);
                    if (renovado != null) {
                        token = renovado;
                        continue;
                    }
                }
                InputStream stream = status >= 200 && status < 300
                        ? connection.getInputStream() : connection.getErrorStream();
                if (stream != null) body = ler(stream);
                break;
            } finally {
                connection.disconnect();
            }
        }

        if (status < 200 || status >= 300) {
            Log.w(TAG, "GET cooperativas respondeu HTTP " + status);
            String mensagem = status == 401 || status == 403
                    ? "Sua sessão expirou ou não possui acesso a cooperativas."
                    : "Não foi possível carregar as cooperativas (HTTP " + status + ").";
            return new Resultado(new ArrayList<>(), mensagem);
        }

        JSONObject json = new JSONObject(body == null ? "{}" : body);
        JSONArray array = json.optJSONArray("cooperativas");
        List<CooperativaProxima> lista = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) continue;
                int id = item.optInt("id", 0);
                String nome = item.optString("nome", "").trim();
                if (id <= 0 || nome.isEmpty()) continue;
                Double media = item.isNull("mediaAvaliacoes")
                        ? null : item.optDouble("mediaAvaliacoes");
                lista.add(new CooperativaProxima(
                        id, nome,
                        item.optString("enderecoCompleto", ""),
                        item.optString("cidade", ""),
                        item.optString("estado", ""),
                        media, item.optLong("totalAvaliacoes", 0)));
            }
        }
        return new Resultado(lista, null);
    }

    @NonNull
    private String ler(InputStream input) throws Exception {
        try (InputStream in = input; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[2048];
            int lidos;
            while ((lidos = in.read(buffer)) != -1) out.write(buffer, 0, lidos);
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }
}