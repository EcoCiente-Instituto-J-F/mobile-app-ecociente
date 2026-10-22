package com.example.ecociente.repository;

import android.content.Context;
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

// GET com o JWT da ds-autenticacao-api. Quem chama já está numa thread de rede.
final class ApiAutenticada {

    private static final String TAG = "ApiAutenticada";

    // Render dorme as APIs após inatividade (cold start de 70 a 150 s).
    private static final int TIMEOUT_MS = 180_000;

    private static final AutenticacaoExternaRepository AUTENTICACAO = new AutenticacaoExternaRepository();

    private ApiAutenticada() {}

    // Corpo da resposta, ou null quando não há credenciais ou a API não respondeu 2xx.
    @Nullable
    static String obter(@NonNull Context contexto, @NonNull String url) throws IOException {
        return chamar(contexto, "GET", url, null);
    }

    // POST com corpo JSON; mesma regra de retorno do `obter`.
    @Nullable
    static String enviar(@NonNull Context contexto, @NonNull String url, @NonNull String json)
            throws IOException {
        return chamar(contexto, "POST", url, json);
    }

    // Código HTTP e corpo da resposta, para quem precisa distinguir o erro (409, 403...).
    static final class Resposta {
        final int codigo;
        @NonNull final String corpo;

        Resposta(int codigo, @NonNull String corpo) {
            this.codigo = codigo;
            this.corpo = corpo;
        }

        boolean sucesso() {
            return codigo >= 200 && codigo < 300;
        }
    }

    // POST com corpo JSON; null só quando não há credenciais.
    @Nullable
    static Resposta enviarComCodigo(@NonNull Context contexto, @NonNull String url, @NonNull String json)
            throws IOException {
        return requisitarComRenovacao(contexto, "POST", url, json);
    }

    // PATCH com corpo JSON; null só quando não há credenciais.
    @Nullable
    static Resposta alterarComCodigo(@NonNull Context contexto, @NonNull String url, @NonNull String json)
            throws IOException {
        return requisitarComRenovacao(contexto, "PATCH", url, json);
    }

    @Nullable
    private static String chamar(
            @NonNull Context contexto, @NonNull String metodo, @NonNull String url, @Nullable String json)
            throws IOException {

        Resposta resposta = requisitarComRenovacao(contexto, metodo, url, json);

        if (resposta == null) {
            return null;
        }

        if (!resposta.sucesso()) {
            Log.w(TAG, "HTTP " + resposta.codigo + " em " + metodo + " " + url);
            return null;
        }

        return resposta.corpo;
    }

    @Nullable
    private static Resposta requisitarComRenovacao(
            @NonNull Context contexto, @NonNull String metodo, @NonNull String url, @Nullable String json)
            throws IOException {

        String token = AUTENTICACAO.tokenParaUso(contexto);

        if (token == null) {
            return null;
        }

        int[] codigo = new int[1];
        String corpo = requisitar(metodo, url, token, json, codigo);

        // O JWT dura minutos: o salvo costuma estar vencido.
        if (codigo[0] == 401 || codigo[0] == 403) {
            String renovado = AUTENTICACAO.renovarToken(contexto);

            if (renovado != null) {
                corpo = requisitar(metodo, url, renovado, json, codigo);
            }
        }

        return new Resposta(codigo[0], corpo);
    }

    @NonNull
    private static String requisitar(
            @NonNull String metodo,
            @NonNull String url,
            @NonNull String token,
            @Nullable String json,
            @NonNull int[] codigo)
            throws IOException {

        HttpURLConnection conexao = (HttpURLConnection) new URL(url).openConnection();

        try {
            conexao.setRequestMethod(metodo);
            conexao.setRequestProperty("Accept", "application/json");
            conexao.setRequestProperty("Authorization", "Bearer " + token);
            conexao.setConnectTimeout(TIMEOUT_MS);
            conexao.setReadTimeout(TIMEOUT_MS);

            if (json != null) {
                byte[] dados = json.getBytes(StandardCharsets.UTF_8);

                conexao.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conexao.setDoOutput(true);
                conexao.setFixedLengthStreamingMode(dados.length);

                try (OutputStream saida = conexao.getOutputStream()) {
                    saida.write(dados);
                }
            }

            codigo[0] = conexao.getResponseCode();

            InputStream fluxo = codigo[0] >= 400 ? conexao.getErrorStream() : conexao.getInputStream();

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
        } finally {
            conexao.disconnect();
        }
    }
}
