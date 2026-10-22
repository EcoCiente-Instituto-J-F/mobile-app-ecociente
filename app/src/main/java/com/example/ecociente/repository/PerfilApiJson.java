package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import com.example.ecociente.model.PerfilDaApi;

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

    // Dados pessoais, endereço, telefone e foto do usuário (e da cooperativa, se houver).
    @NonNull
    static PerfilDaApi perfilDaApi(@NonNull JSONObject perfil) {

        JSONObject endereco = perfil.optJSONObject("endereco");
        JSONObject cooperativa = perfil.optJSONObject("cooperativa");

        String telefone = "";
        JSONArray telefones = perfil.optJSONArray("telefones");

        for (int i = 0; telefones != null && i < telefones.length(); i++) {
            JSONObject item = telefones.optJSONObject(i);

            if (item != null && item.optBoolean("ativo", true) && !item.optString("numero", "").trim().isEmpty()) {
                telefone = item.optString("numero", "");
                break;
            }
        }

        return new PerfilDaApi(
                texto(perfil, "nome"),
                texto(perfil, "urlAvatar"),
                telefone,
                texto(perfil, "cpf"),
                endereco == null ? "" : texto(endereco, "cep"),
                endereco == null ? "" : texto(endereco, "estado"),
                endereco == null ? "" : texto(endereco, "cidade"),
                endereco == null ? "" : texto(endereco, "logradouro"),
                endereco == null ? "" : texto(endereco, "numero"),
                endereco == null ? "" : texto(endereco, "complemento"),
                cooperativa == null ? "" : texto(cooperativa, "nome"),
                cooperativa == null ? "" : texto(cooperativa, "email"));
    }

    // Texto do campo, vazio quando ausente ou null (o optString devolveria a palavra "null").
    @NonNull
    private static String texto(@NonNull JSONObject objeto, @NonNull String campo) {
        return objeto.isNull(campo) ? "" : objeto.optString(campo, "");
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
