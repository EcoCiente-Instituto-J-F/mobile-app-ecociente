package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

// Lê GET /api/usuarios/me da ds-mobile-api e escolhe o condomínio do usuário.
final class PerfilApiJson {

    private PerfilApiJson() {}

    static final class Condominio {
        final int id;
        @NonNull final String nome;

        Condominio(int id, @NonNull String nome) {
            this.id = id;
            this.nome = nome;
        }
    }

    // Id da cooperativa do usuário (perfil COOPERATIVA); 0 quando não há.
    static int cooperativaId(@NonNull JSONObject perfil) {

        JSONObject cooperativa = perfil.optJSONObject("cooperativa");

        return cooperativa == null ? 0 : cooperativa.optInt("id", 0);
    }

    // O síndico fica com o condomínio em que é responsável; o morador, com o primeiro vínculo
    // aprovado e ativo. Sem nenhum dos dois, null.
    @Nullable
    static Condominio condominio(@NonNull JSONObject perfil) {

        JSONArray condominios = perfil.optJSONArray("condominios");

        Condominio aprovado = null;

        for (int i = 0; condominios != null && i < condominios.length(); i++) {
            JSONObject item = condominios.optJSONObject(i);

            if (item == null || item.optInt("id", 0) <= 0 || !item.optBoolean("ativo", true)) {
                continue;
            }

            Condominio atual = new Condominio(item.optInt("id"), item.optString("nome", ""));

            if (item.optBoolean("sindicoResponsavel", false)) {
                return atual;
            }

            JSONObject vinculo = item.optJSONObject("vinculo");

            if (aprovado == null && vinculo != null && vinculo.optBoolean("aprovado", false)) {
                aprovado = atual;
            }
        }

        return aprovado;
    }
}
