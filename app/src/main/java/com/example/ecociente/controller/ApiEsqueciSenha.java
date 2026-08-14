package com.example.ecociente.controller;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;

// Cliente HTTP simples pro backend de "esqueci a senha", hospedado no Vercel
// (não é Firebase - por isso é HTTP puro em vez do SDK do Firebase Functions).
final class ApiEsqueciSenha {
    // TODO: trocar pela URL real depois do deploy em vercel.com (ex: https://seu-projeto.vercel.app/api/)
    private static final String URL_BASE = "https://SEU-PROJETO.vercel.app/api/";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    interface Retorno {
        void aoConcluir(boolean sucesso, @NonNull String mensagemErro);
    }

    private ApiEsqueciSenha() {}

    // Faz a chamada numa thread separada e devolve o resultado na thread principal.
    static void chamar(@NonNull String endpoint, @NonNull JSONObject corpo, @NonNull Retorno retorno) {
        EXECUTOR.execute(() -> {
            boolean sucesso = false;
            String mensagemErro = "Não foi possível conectar ao servidor";

            try {
                JSONObject resposta = enviar(endpoint, corpo);
                sucesso = resposta.optBoolean("sucesso", false);

                if (!sucesso) {
                    mensagemErro = resposta.optString("erro", mensagemErro);
                }
            } catch (Exception erro) {
                // Falha de rede/timeout/JSON inválido - mensagem genérica já cobre esses casos.
            }

            boolean sucessoFinal = sucesso;
            String mensagemFinal = mensagemErro;

            PRINCIPAL.post(() -> retorno.aoConcluir(sucessoFinal, mensagemFinal));
        });
    }

    private static JSONObject enviar(String endpoint, JSONObject corpo) throws IOException, JSONException {
        HttpURLConnection conexao = (HttpURLConnection) new URL(URL_BASE + endpoint).openConnection();

        try {
            conexao.setRequestMethod("POST");
            conexao.setRequestProperty("Content-Type", "application/json");
            conexao.setDoOutput(true);
            conexao.setConnectTimeout(15000);
            conexao.setReadTimeout(15000);

            try (OutputStream saida = conexao.getOutputStream()) {
                saida.write(corpo.toString().getBytes(StandardCharsets.UTF_8));
            }

            int codigoResposta = conexao.getResponseCode();
            boolean deuCerto = codigoResposta >= 200 && codigoResposta < 300;
            InputStream fluxo = deuCerto ? conexao.getInputStream() : conexao.getErrorStream();

            return new JSONObject(lerFluxo(fluxo));
        } finally {
            conexao.disconnect();
        }
    }

    private static String lerFluxo(InputStream fluxo) throws IOException {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int lidos;

        while ((lidos = fluxo.read(buffer)) != -1) {
            saida.write(buffer, 0, lidos);
        }

        return saida.toString(StandardCharsets.UTF_8.name());
    }
}
