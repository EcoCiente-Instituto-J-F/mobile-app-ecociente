package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.model.StatusAgendamento;
import org.json.JSONObject;

// Lê um agendamento da ds-mobile-api: a listagem manda "statusAgendamento" e a criação, "status".
final class CalendarioJson {

    private CalendarioJson() {}

    @NonNull
    static Solicitacao solicitacao(@NonNull JSONObject item) {

        String status = item.has("statusAgendamento")
                ? item.optString("statusAgendamento") : item.optString("status");

        return new Solicitacao(
                        item.optInt("id"),
                        item.optInt("condominioId"),
                        item.optInt("cooperativaId"),
                        texto(item, "dataInicio"),
                        texto(item, "dataFim"),
                        StatusAgendamento.daApi(status),
                        item.optBoolean("possuiRecorrencia"))
                .comNomeCondominio(texto(item, "condominioNome"))
                .comDadosDoPedido(texto(item, "origemSolicitacao"), texto(item, "cooperativaNome"));
    }

    @Nullable
    private static String texto(@NonNull JSONObject item, @NonNull String campo) {
        return item.isNull(campo) ? null : item.optString(campo);
    }
}
