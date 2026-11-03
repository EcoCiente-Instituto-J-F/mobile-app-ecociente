package com.example.ecociente.ui;

import org.json.JSONException;
import org.json.JSONObject;

public final class FormatadorRespostaEko {

    private FormatadorRespostaEko() {
    }

    public static String paraExibicao(String original) {
        if (original == null) {
            return "";
        }
        String json = original.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            return original;
        }
        try {
            JSONObject dados = new JSONObject(json);
            String dominio = dados.optString("dominio", "").trim();
            if (!"educador".equalsIgnoreCase(dominio)
                    && !"faq".equalsIgnoreCase(dominio)) {
                return original;
            }
            Object valor = dados.opt("resposta");
            if (!(valor instanceof String) || ((String) valor).trim().isEmpty()) {
                return original;
            }
            StringBuilder exibicao = new StringBuilder(((String) valor).trim());
            for (String campo : new String[]{"recomendacao", "esclarecer"}) {
                Object complementar = dados.opt(campo);
                if (complementar instanceof String) {
                    String texto = ((String) complementar).trim();
                    if (!texto.isEmpty() && !texto.equals(exibicao.toString())) {
                        exibicao.append("\n\n").append(texto);
                    }
                }
            }
            return exibicao.toString();
        } catch (JSONException erro) {
            return original;
        }
    }
}
