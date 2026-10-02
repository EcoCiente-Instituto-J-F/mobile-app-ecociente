package com.example.ecociente.repository;

import com.example.ecociente.model.RespostaChat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ChatRepository {

    private static final String URL_CHAT_LOCAL = "htvtp://127.0.0.1:8000/api/v1/chat";
    private static final int USUARIO_TESTE_ID = 1;
    private static final String PERFIL_TESTE = "MORADOR_RESIDENCIAL";
    private static final int TIMEOUT_CONEXAO_MS = 10_000;
    private static final int TIMEOUT_RESPOSTA_MS = 60_000;

    public RespostaChat enviarMensagem(String mensagem, String sessionId) throws Exception {
        HttpURLConnection conexao = null;

        try {
            conexao = (HttpURLConnection) new URL(URL_CHAT_LOCAL).openConnection();
            conexao.setRequestMethod("POST");
            conexao.setConnectTimeout(TIMEOUT_CONEXAO_MS);
            conexao.setReadTimeout(TIMEOUT_RESPOSTA_MS);
            conexao.setDoOutput(true);
            conexao.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conexao.setRequestProperty("Accept", "application/json");
            conexao.setRequestProperty("X-Usuario-Id", String.valueOf(USUARIO_TESTE_ID));
            conexao.setRequestProperty("X-Perfil", PERFIL_TESTE);

            JSONObject corpo = new JSONObject();
            corpo.put("usuario_id", USUARIO_TESTE_ID);
            corpo.put("mensagem", mensagem);
            if (sessionId != null && !sessionId.isEmpty()) {
                corpo.put("session_id", sessionId);
            }

            byte[] dados = corpo.toString().getBytes(StandardCharsets.UTF_8);
            conexao.setFixedLengthStreamingMode(dados.length);

            try (OutputStream saida = conexao.getOutputStream()) {
                saida.write(dados);
            }

            int codigo = conexao.getResponseCode();
            InputStream fluxo = codigo >= 200 && codigo < 300
                    ? conexao.getInputStream()
                    : conexao.getErrorStream();
            String respostaJson = lerUtf8(fluxo);

            if (codigo < 200 || codigo >= 300) {
                throw new IllegalStateException(
                        "API respondeu HTTP " + codigo + ": " + respostaJson);
            }

            JSONObject objeto = new JSONObject(respostaJson);
            String resposta = objeto.optString("answer", "").trim();
            String novaSessao = objeto.optString("session_id", "").trim();

            if (resposta.isEmpty()) {
                throw new IllegalStateException("A API não retornou o campo answer.");
            }

            return new RespostaChat(resposta, novaSessao);
        } finally {
            if (conexao != null) {
                conexao.disconnect();
            }
        }
    }

    private static String lerUtf8(InputStream fluxo) throws Exception {
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
