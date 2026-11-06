package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

// O que uma rota precisa saber de um pedido: método, host, caminho sem barra final, query decodificada, token e corpo.
final class RequisicaoDemo {

    final String metodo;
    final String host;
    final String caminho;
    final Map<String, String> query;
    @Nullable final String autorizacao;
    final byte[] corpo;

    private RequisicaoDemo(
            String metodo,
            String host,
            String caminho,
            Map<String, String> query,
            @Nullable String autorizacao,
            byte[] corpo) {
        this.metodo = metodo;
        this.host = host;
        this.caminho = caminho;
        this.query = query;
        this.autorizacao = autorizacao;
        this.corpo = corpo;
    }

    @NonNull
    static RequisicaoDemo de(
            @NonNull String metodo, @NonNull String url, @Nullable String autorizacao, @NonNull byte[] corpo)
            throws URISyntaxException {

        URI uri = new URI(url);

        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        String caminho = uri.getRawPath() == null ? "" : uri.getRawPath();

        while (caminho.length() > 1 && caminho.endsWith("/")) {
            caminho = caminho.substring(0, caminho.length() - 1);
        }

        if (caminho.isEmpty()) {
            caminho = "/";
        }

        return new RequisicaoDemo(
                metodo.toUpperCase(Locale.ROOT), host, caminho, query(uri.getRawQuery()), autorizacao, corpo);
    }

    boolean e(@NonNull String metodoEsperado, @NonNull String caminhoEsperado) {
        return metodo.equals(metodoEsperado) && caminho.equals(caminhoEsperado);
    }

    // Corpo como objeto JSON; null quando vazio ou inválido.
    @Nullable
    JSONObject corpoJson() {
        if (corpo.length == 0) {
            return null;
        }

        try {
            return new JSONObject(new String(corpo, java.nio.charset.StandardCharsets.UTF_8));

        } catch (JSONException erro) {
            return null;
        }
    }

    @NonNull
    private static Map<String, String> query(@Nullable String bruta) {
        Map<String, String> query = new LinkedHashMap<>();

        if (bruta == null || bruta.isEmpty()) {
            return query;
        }

        for (String par : bruta.split("&")) {
            if (par.isEmpty()) {
                continue;
            }

            int igual = par.indexOf('=');

            query.put(
                    decodificar(igual < 0 ? par : par.substring(0, igual)),
                    igual < 0 ? "" : decodificar(par.substring(igual + 1)));
        }

        return query;
    }

    @NonNull
    private static String decodificar(@NonNull String texto) {
        try {
            return URLDecoder.decode(texto, "UTF-8");

        } catch (UnsupportedEncodingException | IllegalArgumentException erro) {
            return texto;
        }
    }
}
