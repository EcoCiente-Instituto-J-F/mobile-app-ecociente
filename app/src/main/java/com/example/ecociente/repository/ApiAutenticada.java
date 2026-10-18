package com.example.ecociente.repository;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

        String token = AUTENTICACAO.tokenParaUso(contexto);

        if (token == null) {
            return null;
        }

        int[] codigo = new int[1];
        String corpo = requisitar(url, token, codigo);

        // O JWT dura minutos: o salvo costuma estar vencido.
        if (codigo[0] == 401 || codigo[0] == 403) {
            String renovado = AUTENTICACAO.renovarToken(contexto);

            if (renovado != null) {
                corpo = requisitar(url, renovado, codigo);
            }
        }

        if (codigo[0] < 200 || codigo[0] >= 300) {
            Log.w(TAG, "HTTP " + codigo[0] + " em " + url);
            return null;
        }

        return corpo;
    }

    @NonNull
    private static String requisitar(@NonNull String url, @NonNull String token, @NonNull int[] codigo)
            throws IOException {

        HttpURLConnection conexao = (HttpURLConnection) new URL(url).openConnection();

        try {
            conexao.setRequestMethod("GET");
            conexao.setRequestProperty("Accept", "application/json");
            conexao.setRequestProperty("Authorization", "Bearer " + token);
            conexao.setConnectTimeout(TIMEOUT_MS);
            conexao.setReadTimeout(TIMEOUT_MS);

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
