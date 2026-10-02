package com.example.ecociente.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.SessaoExterna;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

// Fala com a ds-autenticacao-api (Java/Postgres, repositório separado deste
// app), hoje só com POST /auth/login implementado lá. Chamada em paralelo ao
// login do Firebase, sem travar o app se essa API estiver fora do ar - ainda
// não existe conta nenhuma migrada pro Postgres.
public class AutenticacaoExternaRepository {

    private static final String TAG = "AuthExternaEcoCiente";

    // Emulador Android: 10.0.2.2 é o alias pro localhost da máquina que roda
    // a API (ex.: via docker-compose). Em device físico isso precisa virar o
    // IP da máquina na rede, ou a URL de onde a API estiver hospedada.
    private static final String URL_LOGIN = "http://10.0.2.2:9801/auth/login";

    private static final String PREFERENCIAS = "sessao_externa";
    private static final String CHAVE_TOKEN = "token";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    public interface Retorno {
        void aoConcluir(@Nullable SessaoExterna sessao);
    }

    public void autenticar(
            @NonNull Context contexto,
            @NonNull String email,
            @NonNull String senha,
            @NonNull Retorno retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    SessaoExterna sessao = null;

                    try {
                        sessao = login(email, senha);

                        persistirToken(contextoApp, sessao.getToken());

                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível autenticar na API externa", erro);
                    }

                    SessaoExterna sessaoFinal = sessao;

                    PRINCIPAL.post(() -> retorno.aoConcluir(sessaoFinal));
                });
    }

    @NonNull
    private SessaoExterna login(@NonNull String email, @NonNull String senha) throws Exception {

        HttpURLConnection conexao = (HttpURLConnection) new URL(URL_LOGIN).openConnection();

        try {
            conexao.setRequestMethod("POST");
            conexao.setRequestProperty("Content-Type", "application/json");
            conexao.setRequestProperty("Accept", "application/json");
            conexao.setDoOutput(true);
            conexao.setUseCaches(false);
            conexao.setConnectTimeout(8_000);
            conexao.setReadTimeout(8_000);

            JSONObject corpo = new JSONObject();
            corpo.put("email", email);
            corpo.put("senha", senha);

            try (OutputStream saida = conexao.getOutputStream()) {
                saida.write(corpo.toString().getBytes(StandardCharsets.UTF_8));
            }

            int codigoResposta = conexao.getResponseCode();

            if (codigoResposta < 200 || codigoResposta >= 300) {
                throw new IOException("Resposta HTTP " + codigoResposta);
            }

            JSONObject resposta = new JSONObject(lerFluxo(conexao.getInputStream()));

            return new SessaoExterna(
                    resposta.getString("token"),
                    resposta.optString("tipoToken", "Bearer"),
                    resposta.optLong("expiraEmSegundos", 0),
                    resposta.optInt("usuarioId", 0),
                    resposta.optString("nome", ""),
                    resposta.optString("email", email),
                    resposta.optString("perfil", ""));

        } finally {
            conexao.disconnect();
        }
    }

    private void persistirToken(@NonNull Context contextoApp, @NonNull String token) {

        SharedPreferences preferencias =
                contextoApp.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);

        preferencias.edit().putString(CHAVE_TOKEN, token).apply();
    }

    @Nullable
    public static String obterTokenSalvo(@NonNull Context contexto) {

        SharedPreferences preferencias =
                contexto
                        .getApplicationContext()
                        .getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);

        return preferencias.getString(CHAVE_TOKEN, null);
    }

    @NonNull
    private String lerFluxo(@NonNull InputStream fluxo) throws IOException {

        try (InputStream entrada = fluxo;
                ByteArrayOutputStream saida = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int lidos;

            while ((lidos = entrada.read(buffer)) != -1) {
                saida.write(buffer, 0, lidos);
            }

            return saida.toString(StandardCharsets.UTF_8.name());
        }
    }
}
