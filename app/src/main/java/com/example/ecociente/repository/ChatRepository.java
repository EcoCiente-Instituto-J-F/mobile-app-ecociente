package com.example.ecociente.repository;

import android.content.Context;

import com.example.ecociente.R;
import com.example.ecociente.model.RespostaChat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class ChatRepository {

    private static final int TIMEOUT_CONEXAO_MS = 10_000;
    private static final int TIMEOUT_RESPOSTA_MS = 120_000;

    private final AutenticacaoExternaRepository autenticacao = new AutenticacaoExternaRepository();

    private String sessaoConhecida;
    private String enderecoSessao;

    public RespostaChat enviarMensagem(Context contexto, String mensagem, String sessionId) throws IOException, JSONException {
        String token = autenticacao.tokenParaUso(contexto);
        if (token == null) {
            throw new IllegalStateException("Sem credenciais para falar com a Eko.");
        }

        String enderecoPrincipal = contexto.getString(R.string.eko_chat_url).trim();
        String enderecoRender = ConfiguracaoServidores.CHAT_IA_RENDER;

        boolean continuarNoRender = sessionId != null && sessionId.equals(sessaoConhecida)
                && enderecoRender.equals(enderecoSessao);
        String endereco = continuarNoRender ? enderecoRender : enderecoPrincipal;

        JSONObject corpo = new JSONObject().put("mensagem", mensagem);
        if (sessionId != null && !sessionId.isEmpty()) {
            corpo.put("session_id", sessionId);
        }

        RespostaHttp resposta;
        try {
            resposta = enviarComRenovacao(contexto, endereco, corpo, token);
        } catch (FalhaConexaoAntesEnvio falha) {
            if (!endereco.equals(enderecoPrincipal) || enderecoPrincipal.equals(enderecoRender)) {
                throw falha;
            }
            endereco = enderecoRender;
            JSONObject corpoRender = new JSONObject().put("mensagem", mensagem);
            resposta = enviarComRenovacao(contexto, enderecoRender, corpoRender, token);
        }

        if (resposta.codigo < 200 || resposta.codigo >= 300) {
            throw new IOException("Eko respondeu HTTP " + resposta.codigo);
        }

        JSONObject objeto = new JSONObject(resposta.conteudo);
        String texto = objeto.optString("answer", "").trim();
        String novaSessao = objeto.optString("session_id", "").trim();

        if (texto.isEmpty()) {
            throw new IOException("A API não retornou o campo answer.");
        }

        sessaoConhecida = novaSessao;
        enderecoSessao = endereco;
        return new RespostaChat(texto, novaSessao);
    }

    private RespostaHttp enviarComRenovacao(Context contexto, String endereco, JSONObject corpo, String token)
            throws IOException {
        RespostaHttp resultado = enviar(endereco, corpo, token);
        if (resultado.codigo == 401) {
            String renovado = autenticacao.renovarToken(contexto);
            if (renovado != null) {
                resultado = enviar(endereco, corpo, renovado);
            }
        }
        return resultado;
    }

    private RespostaHttp enviar(String endereco, JSONObject corpo, String token) throws IOException {
        HttpURLConnection conexao = null;
        try {
            conexao = (HttpURLConnection) new URL(endereco).openConnection();
            conexao.setRequestMethod("POST");
            conexao.setConnectTimeout(TIMEOUT_CONEXAO_MS);
            conexao.setReadTimeout(TIMEOUT_RESPOSTA_MS);
            conexao.setDoOutput(true);
            conexao.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conexao.setRequestProperty("Accept", "application/json");
            conexao.setRequestProperty("Authorization", "Bearer " + token);

            byte[] dados = corpo.toString().getBytes(StandardCharsets.UTF_8);
            conexao.setFixedLengthStreamingMode(dados.length);

            try {
                conexao.connect();
            } catch (UnknownHostException | ConnectException | NoRouteToHostException | SocketTimeoutException erro) {
                throw new FalhaConexaoAntesEnvio(erro);
            }

            try (OutputStream saida = conexao.getOutputStream()) {
                saida.write(dados);
            }

            int codigo = conexao.getResponseCode();
            return new RespostaHttp(
                    codigo,
                    lerUtf8(codigo >= 200 && codigo < 300
                            ? conexao.getInputStream() : conexao.getErrorStream())
            );
        } finally {
            if (conexao != null) {
                conexao.disconnect();
            }
        }
    }

    static final class FalhaConexaoAntesEnvio extends IOException {
        FalhaConexaoAntesEnvio(IOException causa) {
            super("Conexao indisponivel antes de enviar a mensagem", causa);
        }
    }

    private static final class RespostaHttp {
        final int codigo;
        final String conteudo;

        RespostaHttp(int codigo, String conteudo) {
            this.codigo = codigo;
            this.conteudo = conteudo;
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
