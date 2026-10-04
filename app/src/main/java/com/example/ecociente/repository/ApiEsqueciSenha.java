package com.example.ecociente.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;

// Cliente HTTP da ds-esqueceusenha-api (Spring/Postgres, repositório separado deste app).
final class ApiEsqueciSenha {
    private static final String TAG = "EsqueciSenhaApi";
    private static final String URL_BASE = "https://ds-esqueceusenha-api-1.onrender.com/senhas/";
    private static final String MENSAGEM_PADRAO = "Não foi possível conectar ao servidor";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    interface Retorno {
        void aoConcluir(boolean sucesso, @NonNull String mensagemErro);
    }

    private ApiEsqueciSenha() {}

    static void chamar(@NonNull String endpoint, @NonNull JSONObject corpo, @NonNull Retorno retorno) {
        chamar(endpoint, corpo, null, retorno);
    }

    static void chamar(
            @NonNull String endpoint,
            @NonNull JSONObject corpo,
            @Nullable String token,
            @NonNull Retorno retorno) {
        Log.d(TAG, "-> " + endpoint);

        EXECUTOR.execute(() -> {
            boolean sucesso = false;
            String mensagemErro = MENSAGEM_PADRAO;

            try {
                String erro = enviar(endpoint, corpo, token);

                sucesso = erro == null;

                if (erro != null) {
                    mensagemErro = erro;
                }

                Log.d(TAG, "<- " + endpoint + (sucesso ? " ok" : " " + mensagemErro));
            } catch (Exception erro) {
                Log.e(TAG, "<- " + endpoint + " falhou", erro);
            }

            boolean sucessoFinal = sucesso;
            String mensagemFinal = mensagemErro;

            PRINCIPAL.post(() -> retorno.aoConcluir(sucessoFinal, mensagemFinal));
        });
    }

    // Devolve null quando deu certo, ou a mensagem de erro que a API mandou.
    @Nullable
    private static String enviar(String endpoint, JSONObject corpo, @Nullable String token)
            throws IOException {
        HttpURLConnection conexao = (HttpURLConnection) new URL(URL_BASE + endpoint).openConnection();

        try {
            conexao.setRequestMethod("POST");
            conexao.setRequestProperty("Content-Type", "application/json");
            conexao.setRequestProperty("Accept", "application/json");

            if (token != null) {
                conexao.setRequestProperty("Authorization", "Bearer " + token);
            }

            conexao.setDoOutput(true);
            // O Render dorme a API após inatividade; o primeiro pedido pode levar mais de 1 minuto.
            conexao.setConnectTimeout(120_000);
            conexao.setReadTimeout(120_000);

            try (OutputStream saida = conexao.getOutputStream()) {
                saida.write(corpo.toString().getBytes(StandardCharsets.UTF_8));
            }

            int codigoResposta = conexao.getResponseCode();

            if (codigoResposta >= 200 && codigoResposta < 300) {
                return null;
            }

            return extrairMensagemErro(lerFluxo(conexao.getErrorStream()));
        } finally {
            conexao.disconnect();
        }
    }

    // A API devolve { "status", "codigoError", "details": [{ "field", "message" }] }.
    @NonNull
    private static String extrairMensagemErro(@NonNull String corpoErro) {
        try {
            JSONArray detalhes = new JSONObject(corpoErro).optJSONArray("details");

            if (detalhes != null && detalhes.length() > 0) {
                String mensagem = detalhes.getJSONObject(0).optString("message", "");

                if (!mensagem.isEmpty()) {
                    return mensagem;
                }
            }
        } catch (Exception erro) {
            Log.w(TAG, "Resposta de erro fora do formato esperado", erro);
        }

        return MENSAGEM_PADRAO;
    }

    @NonNull
    private static String lerFluxo(@Nullable InputStream fluxo) throws IOException {
        if (fluxo == null) {
            return "";
        }

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
