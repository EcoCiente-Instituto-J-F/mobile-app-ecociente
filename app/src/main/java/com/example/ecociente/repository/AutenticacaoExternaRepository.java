package com.example.ecociente.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
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
    private static final String CHAVE_PERFIL = "perfil";

    private static final Object TRAVA = new Object();

    // Quem precisa do JWT espera o login em andamento terminar: no primeiro acesso da sessão a Home
    // pede a agenda antes de a API responder (cold start do Render leva de 70 a 150 s).
    private static final Object ESPERA = new Object();
    private static final long ESPERA_MAXIMA_MS = 180_000;
    private static int loginsEmAndamento = 0;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final ExecutorService EXECUTOR_ESPERA = Executors.newSingleThreadExecutor();

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

        synchronized (ESPERA) {
            loginsEmAndamento++;
        }

        EXECUTOR.execute(
                () -> {
                    SessaoExterna sessao = null;

                    try {
                        sessao = login(email, senha);

                        persistirToken(contextoApp, sessao.getToken());
                        persistirPerfil(contextoApp, sessao.getPerfil());

                        CredenciaisSeguras.salvar(contextoApp, email, senha);

                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível autenticar na API externa", erro);
                    }

                    synchronized (ESPERA) {
                        loginsEmAndamento--;
                        ESPERA.notifyAll();
                    }

                    SessaoExterna sessaoFinal = sessao;

                    PRINCIPAL.post(() -> retorno.aoConcluir(sessaoFinal));
                });
    }

    // Só chamar fora da thread principal: pode esperar o login terminar.
    @Nullable
    public String tokenParaUso(@NonNull Context contexto) {

        aguardarLoginEmAndamento();

        String salvo = obterTokenSalvo(contexto);

        return salvo != null ? salvo : renovarToken(contexto);
    }

    // Login por e-mail e senha ainda falando com a API (o Render pode levar mais de 1 min a acordar).
    public static boolean loginEmAndamento() {

        synchronized (ESPERA) {
            return loginsEmAndamento > 0;
        }
    }

    // Espera, fora da thread principal, o login terminar (ou o limite) e avisa na principal.
    public static void aguardarLogin(long limiteMs, @NonNull Runnable aoConcluir) {

        EXECUTOR_ESPERA.execute(
                () -> {
                    aguardarLoginEmAndamento(limiteMs);

                    PRINCIPAL.post(aoConcluir);
                });
    }

    private void aguardarLoginEmAndamento() {
        aguardarLoginEmAndamento(ESPERA_MAXIMA_MS);
    }

    private static void aguardarLoginEmAndamento(long limiteMs) {

        long limite = SystemClock.elapsedRealtime() + limiteMs;

        synchronized (ESPERA) {
            while (loginsEmAndamento > 0) {
                long restante = limite - SystemClock.elapsedRealtime();

                if (restante <= 0) {
                    return;
                }

                try {
                    ESPERA.wait(restante);
                } catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
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
                persistirPerfil(contextoApp, sessao.getPerfil());

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
                .remove(CHAVE_PERFIL)
                .apply();
    }

    // Perfil que a API devolveu no último login (SINDICO, COOPERATIVA...): é o dado do Postgres,
    // usado para escolher a home quando o Firestore está com outro tipoPerfil.
    @Nullable
    public static String perfilSalvo(@NonNull Context contexto) {

        return contexto.getApplicationContext()
                .getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
                .getString(CHAVE_PERFIL, null);
    }

    private void persistirPerfil(@NonNull Context contextoApp, @NonNull String perfil) {

        contextoApp.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
                .edit()
                .putString(CHAVE_PERFIL, perfil)
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
