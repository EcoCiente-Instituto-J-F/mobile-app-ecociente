package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

// POST /api/agendamentos/{id}/avaliacao da ds-mobile-api: corpo e mensagens de erro.
final class AvaliacaoColetaJson {

    private AvaliacaoColetaJson() {}

    // O comentário vai só quando o síndico escreveu algo.
    @NonNull
    static String corpo(int nota, @Nullable String comentario) {

        try {
            JSONObject corpo = new JSONObject().put("nota", nota);

            if (comentario != null && !comentario.trim().isEmpty()) {
                corpo.put("comentario", comentario.trim());
            }

            return corpo.toString();

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    // A API devolve { status, codigo, mensagem } em português; sem corpo legível, vale o código HTTP.
    @NonNull
    static String mensagemDoErro(int codigoHttp, @Nullable String corpo) {

        if (corpo != null && !corpo.isEmpty()) {
            try {
                String mensagem = new JSONObject(corpo).optString("mensagem", "");

                if (!mensagem.isEmpty()) {
                    return mensagem;
                }
            } catch (JSONException ignorado) {
                // cai no texto padrão do código
            }
        }

        switch (codigoHttp) {
            case 401:
                return "Sua sessão expirou. Entre novamente.";

            case 403:
                return "Só o síndico responsável pelo condomínio pode avaliar a coleta.";

            case 404:
                return "Esta coleta não foi encontrada.";

            case 409:
                return "Esta coleta não pode ser avaliada agora.";

            default:
                return "Não foi possível enviar a avaliação. Tente de novo.";
        }
    }
}
