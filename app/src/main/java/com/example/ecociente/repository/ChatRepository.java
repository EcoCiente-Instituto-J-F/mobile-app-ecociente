package com.example.ecociente.repository;

import android.content.Context;
import com.example.ecociente.model.RespostaChat;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ChatRepository {

    private static final String URL_CHAT = ConfiguracaoServidores.CHAT_IA;
    private static final int TIMEOUT_CONEXAO_MS = 180_000;
    private static final int TIMEOUT_RESPOSTA_MS = 180_000;

    private final AutenticacaoExternaRepository autenticacao = new AutenticacaoExternaRepository();

    // A identidade vem do JWT da ds-mobile-api: o servidor valida o token e descobre o usuário e o perfil.
    public RespostaChat enviarMensagem(Context contexto, String mensagem, String sessionId) throws IOException, JSONException {
        String token = autenticacao.tokenParaUso(contexto);

        if (token == null) {
            throw new IllegalStateException("Sem credenciais para falar com a Eko.");
        }

        JSONObject corpo = new JSONObject().put("mensagem", mensagem);

        if (sessionId != null && !sessionId.isEmpty()) {
            corpo.put("session_id", sessionId);
        }

        int[] codigo = new int[1];
        String respostaJson = enviar(corpo, token, codigo);

        // O JWT dura minutos: se o servidor recusar, renova e tenta uma vez.
        if (codigo[0] == 401) {
            String renovado = autenticacao.renovarToken(contexto);

            if (renovado != null) {
                respostaJson = enviar(corpo, renovado, codigo);
            }
        }

        if (codigo[0] < 200 || codigo[0] >= 300) {
            throw new IllegalStateException("API respondeu HTTP " + codigo[0]);
        }

        JSONObject objeto = new JSONObject(respostaJson);
        String resposta = objeto.optString("answer", "").trim();
        String novaSessao = objeto.optString("session_id", "").trim();

        if (resposta.isEmpty()) {
            throw new IllegalStateException("A API não retornou o campo answer.");
        }

        return new RespostaChat(resposta, novaSessao);
    }

    private String enviar(JSONObject corpo, String token, int[] codigo) throws IOException {
        HttpURLConnection conexao = null;

        try {
            conexao = (HttpURLConnection) new URL(URL_CHAT).openConnection();
            conexao.setRequestMethod("POST");
            conexao.setConnectTimeout(TIMEOUT_CONEXAO_MS);
            conexao.setReadTimeout(TIMEOUT_RESPOSTA_MS);
            conexao.setDoOutput(true);
            conexao.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conexao.setRequestProperty("Accept", "application/json");
            conexao.setRequestProperty("Authorization", "Bearer " + token);

            byte[] dados = corpo.toString().getBytes(StandardCharsets.UTF_8);
            conexao.setFixedLengthStreamingMode(dados.length);

            try (OutputStream saida = conexao.getOutputStream()) {
                saida.write(dados);
            }

            codigo[0] = conexao.getResponseCode();

            return lerUtf8(
                    codigo[0] >= 200 && codigo[0] < 300
                            ? conexao.getInputStream()
                            : conexao.getErrorStream());
        } finally {
            if (conexao != null) {
                conexao.disconnect();
            }
        }
    }

    private static String lerUtf8(InputStream fluxo) throws IOException {
        if (fluxo == null) {
            return "";
        }

        StringBuilder conteudo = new StringBuilder();
        try (BufferedReader leitor = new BufferedReader(
                new InputStreamReader(fluxo, StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                conteudo.append(linha);
            }
        }
        return conteudo.toString();
    }

}
