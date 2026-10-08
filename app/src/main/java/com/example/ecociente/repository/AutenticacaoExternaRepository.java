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
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

// Fala com a ds-autenticacao-api (Java/Postgres, repositório separado deste
// app), hoje só com POST /auth/login implementado lá. Chamada em paralelo ao
// login do Firebase, sem travar o app se essa API estiver fora do ar. Contas
// criadas antes do cadastro integrado não existem no Postgres e falham aqui.
public class AutenticacaoExternaRepository {

    private static final String TAG = "AuthExternaEcoCiente";

    private static final String URL_LOGIN = "https://ds-autenticacao-api-1.onrender.com/auth/login";

    private static final String PREFERENCIAS = "sessao_externa";
    private static final String CHAVE_TOKEN = "token";

    private static final Object TRAVA = new Object();

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

                        CredenciaisSeguras.salvar(contextoApp, email, senha);

                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível autenticar na API externa", erro);
                    }

                    SessaoExterna sessaoFinal = sessao;

                    PRINCIPAL.post(() -> retorno.aoConcluir(sessaoFinal));
                });
    }

    @Nullable
    public String tokenParaUso(@NonNull Context contexto) {

        String salvo = obterTokenSalvo(contexto);

        return salvo != null ? salvo : renovarToken(contexto);
    }

    @Nullable
    public String renovarToken(@NonNull Context contexto) {

        Context contextoApp = contexto.getApplicationContext();

        synchronized (TRAVA) {
            String[] credenciais = CredenciaisSeguras.ler(contextoApp);

            if (credenciais == null || credenciais.length < 2) {
                return null;
            }

            try {
                SessaoExterna sessao = login(credenciais[0], credenciais[1]);

                persistirToken(contextoApp, sessao.getToken());

                return sessao.getToken();

            } catch (Exception erro) {
                Log.w(TAG, "Não foi possível renovar o token", erro);

                return null;
            }
        }
    }

    public static void atualizarSenhaSalva(
            @NonNull Context contexto, @NonNull String email, @NonNull String novaSenha) {
        CredenciaisSeguras.salvar(contexto, email, novaSenha);
    }

    public static void encerrarSessao(@NonNull Context contexto) {

        CredenciaisSeguras.limpar(contexto);

        contexto.getApplicationContext()
                .getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
                .edit()
                .remove(CHAVE_TOKEN)
                .apply();
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
            // Render free tier "dorme" a API após inatividade (cold start de
            // 70s a 2min medido); roda em segundo plano, não trava o login.
            conexao.setConnectTimeout(150_000);
            conexao.setReadTimeout(150_000);

            JSONObject corpo = new JSONObject();
            corpo.put("email", email.trim().toLowerCase(Locale.ROOT));
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
