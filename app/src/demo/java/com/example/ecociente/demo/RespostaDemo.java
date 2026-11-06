package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Resposta da API falsa: código HTTP e corpo JSON em UTF-8.
public final class RespostaDemo {

    public final int codigo;
    public final byte[] corpo;

    public RespostaDemo(int codigo, @NonNull String json) {
        this.codigo = codigo;
        this.corpo = json.getBytes(StandardCharsets.UTF_8);
    }

    @NonNull
    public String texto() {
        return new String(corpo, StandardCharsets.UTF_8);
    }

    // Mesmo formato de erro da ds-mobile-api: { "status", "codigoError", "details": [] }.
    @NonNull
    public static RespostaDemo erro(int codigo, @NonNull String codigoNegocio) {
        try {
            return new RespostaDemo(
                    codigo,
                    new JSONObject()
                            .put("status", codigo)
                            .put("codigoError", codigoNegocio)
                            .put("details", new JSONArray())
                            .toString());
        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }
}
