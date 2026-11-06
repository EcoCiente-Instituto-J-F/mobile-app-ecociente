package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.function.LongSupplier;
import org.json.JSONException;
import org.json.JSONObject;

// ds-mobile-api, conta: POST /auth/login, GET e PATCH /api/usuarios/me, PATCH /api/usuarios/me/endereco.
final class RotasConta implements RotasDemo {

    private static final String HOST = "ds-mobile-api.onrender.com";

    private final EstadoDemo estado;
    private final LongSupplier relogio;

    RotasConta(@NonNull EstadoDemo estado, @NonNull LongSupplier relogio) {
        this.estado = estado;
        this.relogio = relogio;
    }

    @Nullable
    @Override
    public RespostaDemo tratar(@NonNull RequisicaoDemo r) {
        if (!HOST.equals(r.host)) {
            return null;
        }

        if (r.e("POST", "/auth/login")) {
            return entrar(r);
        }

        if (r.e("GET", "/api/usuarios/me")) {
            RespostaDemo recusa = exigirToken(r);

            return recusa != null ? recusa : new RespostaDemo(200, ContaDemo.me(estado.raiz()).toString());
        }

        if (r.e("PATCH", "/api/usuarios/me")) {
            return alterarPerfil(r);
        }

        if (r.e("PATCH", "/api/usuarios/me/endereco")) {
            return alterarEndereco(r);
        }

        return null;
    }

    // Senha errada e e-mail desconhecido dão a mesma resposta, como a API real.
    @NonNull
    private RespostaDemo entrar(@NonNull RequisicaoDemo r) {
        JSONObject corpo = r.corpoJson();

        if (corpo == null) {
            return RespostaDemo.erro(400, "REQUISICAO_INVALIDA");
        }

        JSONObject raiz = estado.raiz();

        if (!ContaDemo.credenciaisConferem(raiz, corpo.optString("email", ""), corpo.optString("senha", null))) {
            return RespostaDemo.erro(401, "CREDENCIAIS_INVALIDAS");
        }

        try {
            JSONObject conta = ContaDemo.conta(raiz);
            JSONObject me = ContaDemo.me(raiz);

            // Um token só por sessão: o app renova o login a cada poucos minutos e não pode invalidar o anterior.
            if (conta.optString("token", "").isEmpty()) {
                conta.put("token", "demo-" + Long.toHexString(relogio.getAsLong()) + "-" + me.optInt("id"));
                estado.salvar();
            }

            return new RespostaDemo(
                    200,
                    new JSONObject()
                            .put("token", conta.getString("token"))
                            .put("tipoToken", "Bearer")
                            .put("expiraEmSegundos", 3600)
                            .put("usuarioId", me.optInt("id"))
                            .put("nome", me.optString("nome", ""))
                            .put("email", conta.optString("email", ""))
                            .put("perfil", "MORADOR")
                            .toString());

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    // Nome, foto (urlAvatar) e telefone; os campos que a API real aceita e o app envia.
    @NonNull
    private RespostaDemo alterarPerfil(@NonNull RequisicaoDemo r) {
        RespostaDemo recusa = exigirToken(r);

        if (recusa != null) {
            return recusa;
        }

        JSONObject corpo = r.corpoJson();

        if (corpo == null) {
            return RespostaDemo.erro(400, "REQUISICAO_INVALIDA");
        }

        JSONObject me = ContaDemo.me(estado.raiz());

        if (corpo.has("nome")) {
            ContaDemo.aplicarCampo(me, "nome", corpo.optString("nome", ""));
        }

        if (corpo.has("urlAvatar")) {
            ContaDemo.aplicarCampo(me, "fotoUrl", corpo.optString("urlAvatar", ""));
        }

        if (corpo.has("telefone")) {
            ContaDemo.aplicarCampo(me, "telefone", corpo.optString("telefone", ""));
        }

        estado.salvar();

        return new RespostaDemo(200, me.toString());
    }

    // A API exige CEP e número; logradouro, cidade e estado ficam como estão (a real os resolve pelo CEP).
    @NonNull
    private RespostaDemo alterarEndereco(@NonNull RequisicaoDemo r) {
        RespostaDemo recusa = exigirToken(r);

        if (recusa != null) {
            return recusa;
        }

        JSONObject corpo = r.corpoJson();

        if (corpo == null
                || corpo.optString("cep", "").trim().isEmpty()
                || corpo.optString("numero", "").trim().isEmpty()) {
            return RespostaDemo.erro(400, "REQUISICAO_INVALIDA");
        }

        JSONObject me = ContaDemo.me(estado.raiz());

        ContaDemo.aplicarCampo(me, "cep", corpo.optString("cep", "").trim());
        ContaDemo.aplicarCampo(me, "numero", corpo.optString("numero", "").trim());
        ContaDemo.aplicarCampo(me, "complemento", corpo.optString("complemento", "").trim());

        estado.salvar();

        return new RespostaDemo(200, me.toString());
    }

    // null quando o pedido traz o token que o /auth/login entregou; senão a resposta 401.
    @Nullable
    private RespostaDemo exigirToken(@NonNull RequisicaoDemo r) {
        String token = ContaDemo.conta(estado.raiz()).optString("token", "");

        if (!token.isEmpty() && ("Bearer " + token).equals(r.autorizacao)) {
            return null;
        }

        return RespostaDemo.erro(401, "NAO_AUTORIZADO");
    }
}
