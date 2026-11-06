package com.example.ecociente.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
            "https://ds-mobile-api.onrender.com/api/cooperativas";
    private static final int TAMANHO_PAGINA = 100;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onResult(@NonNull Resultado resultado);
    }

    public static final class Resultado {
        public final List<CooperativaProxima> cooperativas;
        @Nullable public final Integer condominioId;
        @Nullable public final String erro;

        public Resultado(
                @NonNull List<CooperativaProxima> cooperativas,
                @Nullable Integer condominioId,
                @Nullable String erro) {
            this.cooperativas = cooperativas;
            this.condominioId = condominioId;
            this.erro = erro;
        }

        public boolean sucesso() {
            return erro == null;
        }
    }

    public void listar(@NonNull Context context, @NonNull Callback callback) {
        Context app = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            Resultado resultado;
            try {
                resultado = consultar(app);
            } catch (Exception e) {
                Log.e(TAG, "Falha ao consultar cooperativas", e);
                resultado = new Resultado(
                        new ArrayList<>(),
                        null,
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
            return erroSessao();
        }

        List<CooperativaProxima> cooperativas = new ArrayList<>();
        Integer condominioId = null;
        int totalPaginas = 1;

        for (int pagina = 0; pagina < totalPaginas; pagina++) {
            RespostaHttp resposta = requisitarPagina(context, auth, token, pagina);
            token = resposta.token;

            if (resposta.codigo < 200 || resposta.codigo >= 300) {
                Log.w(TAG, "GET cooperativas respondeu HTTP " + resposta.codigo);
                String mensagem = resposta.codigo == 401 || resposta.codigo == 403
                        ? "Sua sessão expirou ou não possui acesso às cooperativas."
                        : "Não foi possível carregar as cooperativas (HTTP " + resposta.codigo + ").";
                return new Resultado(new ArrayList<>(), null, mensagem);
            }

            JSONObject json = new JSONObject(resposta.corpo == null ? "{}" : resposta.corpo);
            if (pagina == 0) {
                if (!json.isNull("condominioId")) {
                    int id = json.optInt("condominioId", 0);
                    condominioId = id > 0 ? id : null;
                }
                totalPaginas = Math.max(1, json.optInt("totalPaginas", 1));
            }

            JSONArray array = json.optJSONArray("cooperativas");
            if (array == null) {
                continue;
            }

            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) {
                    continue;
                }

                int id = item.optInt("id", 0);
                String nome = item.optString("nome", "").trim();
                if (id <= 0 || nome.isEmpty()) {
                    continue;
                }

                Double media = item.isNull("mediaAvaliacoes")
                        ? null
                        : item.optDouble("mediaAvaliacoes");

                cooperativas.add(new CooperativaProxima(
                        id,
                        nome,
                        item.optString("email", "").trim(),
                        item.optString("telefone", "").trim(),
                        item.optString("enderecoCompleto", "").trim(),
                        item.optString("cidade", "").trim(),
                        item.optString("estado", "").trim(),
                        media,
                        item.optLong("totalAvaliacoes", 0)));
            }
        }

        return new Resultado(cooperativas, condominioId, null);
    }

    @NonNull
    private RespostaHttp requisitarPagina(
            Context context,
            AutenticacaoExternaRepository auth,
            String tokenInicial,
            int pagina) throws Exception {

        String token = tokenInicial;
        int codigo = 0;
        String corpo = null;

        for (int tentativa = 0; tentativa < 2; tentativa++) {
            HttpURLConnection connection = (HttpURLConnection)
                    new URL(ENDPOINT + "?page=" + pagina + "&size=" + TAMANHO_PAGINA)
                            .openConnection();
            try {
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setConnectTimeout(30_000);
                connection.setReadTimeout(30_000);
                codigo = connection.getResponseCode();

                if ((codigo == HttpURLConnection.HTTP_UNAUTHORIZED
                        || codigo == HttpURLConnection.HTTP_FORBIDDEN)
                        && tentativa == 0) {
                    String renovado = auth.renovarToken(context);
                    if (renovado != null) {
                        token = renovado;
                        continue;
                    }
                }

                InputStream stream = codigo >= 200 && codigo < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();
                if (stream != null) {
                    corpo = ler(stream);
                }
                break;
            } finally {
                connection.disconnect();
            }
        }

        return new RespostaHttp(codigo, corpo, token);
    }

    @NonNull
    private Resultado erroSessao() {
        return new Resultado(
                new ArrayList<>(),
                null,
                "Sua sessão não está disponível. Entre novamente para continuar.");
    }

    @NonNull
    private String ler(InputStream input) throws Exception {
        try (InputStream in = input; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[2048];
            int lidos;
            while ((lidos = in.read(buffer)) != -1) {
                out.write(buffer, 0, lidos);
            }
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }

    private static final class RespostaHttp {
        final int codigo;
        @Nullable final String corpo;
        @NonNull final String token;

        RespostaHttp(int codigo, @Nullable String corpo, @NonNull String token) {
            this.codigo = codigo;
            this.corpo = corpo;
            this.token = token;
        }
    }
}
