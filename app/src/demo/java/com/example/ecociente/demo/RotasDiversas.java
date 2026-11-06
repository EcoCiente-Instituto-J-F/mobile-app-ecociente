package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// O que sobra: chat da Eko (dois hosts), CEP (viacep), troca de senha, exclusão de conta e registro de dispositivo.
final class RotasDiversas implements RotasDemo {

    private static final String MOBILE = "ds-mobile-api.onrender.com";
    private static final String CHAT_RENDER = "ia-eko.onrender.com";
    private static final String CHAT_PRINCIPAL = "api.52.71.117.143.sslip.io";
    private static final String VIACEP = "viacep.com.br";
    private static final String CAMINHO_CHAT = "/api/v1/chat";
    private static final String DISPOSITIVOS = "/api/notificacoes/dispositivos";

    private final EstadoDemo estado;
    private final AtomicInteger sessoes = new AtomicInteger();

    RotasDiversas(@NonNull EstadoDemo estado) {
        this.estado = estado;
    }

    @Nullable
    @Override
    public RespostaDemo tratar(@NonNull RequisicaoDemo r) {
        if ((CHAT_RENDER.equals(r.host) || CHAT_PRINCIPAL.equals(r.host)) && r.e("POST", CAMINHO_CHAT)) {
            return conversar(r);
        }

        if (VIACEP.equals(r.host) && r.metodo.equals("GET")) {
            return cep(r);
        }

        if (!MOBILE.equals(r.host)) {
            return null;
        }

        if (r.e("POST", "/senhas/alterar")) {
            return alterarSenha(r);
        }

        if (r.e("DELETE", "/api/usuarios/me")) {
            RespostaDemo recusa = exigirToken(r);

            return recusa != null ? recusa : new RespostaDemo(204, "");
        }

        if (r.e("POST", DISPOSITIVOS)
                || (r.metodo.equals("DELETE") && r.caminho.startsWith(DISPOSITIVOS + "/"))) {
            RespostaDemo recusa = exigirToken(r);

            return recusa != null ? recusa : new RespostaDemo(204, "");
        }

        return null;
    }

    @NonNull
    private RespostaDemo conversar(@NonNull RequisicaoDemo r) {
        RespostaDemo recusa = exigirToken(r);

        if (recusa != null) {
            return recusa;
        }

        JSONObject corpo = r.corpoJson();
        String mensagem = corpo == null ? "" : corpo.optString("mensagem", "").trim();

        if (mensagem.isEmpty()) {
            return RespostaDemo.erro(400, "REQUISICAO_INVALIDA");
        }

        String sessao = corpo.optString("session_id", "").trim();

        if (sessao.isEmpty()) {
            sessao = "demo-" + sessoes.incrementAndGet();
        }

        try {
            return new RespostaDemo(
                    200,
                    new JSONObject()
                            .put("answer", RespostasEko.responder(mensagem, estado))
                            .put("session_id", sessao)
                            .toString());

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    // ViaCEP: 8 dígitos, exceto os que começam com 00000, dão um endereço fictício de São Paulo. O resto é {"erro":true}.
    @NonNull
    private RespostaDemo cep(@NonNull RequisicaoDemo r) {
        String[] partes = r.caminho.split("/");
        String cep = partes.length >= 4 && "ws".equals(partes[1]) ? partes[2] : "";

        try {
            if (!cep.matches("\\d{8}") || cep.startsWith("00000")) {
                return new RespostaDemo(200, new JSONObject().put("erro", true).toString());
            }

            return new RespostaDemo(
                    200,
                    new JSONObject()
                            .put("cep", cep.substring(0, 5) + "-" + cep.substring(5))
                            .put("logradouro", "Rua das Palmeiras")
                            .put("complemento", "")
                            .put("bairro", "Centro")
                            .put("localidade", "São Paulo")
                            .put("uf", "SP")
                            .toString());

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    // Mesmas regras do ProvedorContaDemo.alterarSenha; erros no formato { details: [{ message }] } que o app exibe.
    @NonNull
    private RespostaDemo alterarSenha(@NonNull RequisicaoDemo r) {
        RespostaDemo recusa = exigirToken(r);

        if (recusa != null) {
            return recusa;
        }

        JSONObject corpo = r.corpoJson();

        if (corpo == null) {
            return RespostaDemo.erro(400, "REQUISICAO_INVALIDA");
        }

        JSONObject conta = ContaDemo.conta(estado.raiz());
        String nova = corpo.optString("novaSenha", "");

        if (!corpo.optString("senhaAtual", "").equals(conta.optString("senha", ""))) {
            return erroComMensagem(400, "SENHA_INVALIDA", "Senha atual incorreta.");
        }

        if (nova.isEmpty() || !nova.equals(corpo.optString("confirmacaoSenha", nova))) {
            return erroComMensagem(400, "SENHA_INVALIDA", "Não foi possível atualizar a senha.");
        }

        try {
            conta.put("senha", nova);

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }

        estado.salvar();

        return new RespostaDemo(204, "");
    }

    @NonNull
    private static RespostaDemo erroComMensagem(int codigo, @NonNull String negocio, @NonNull String mensagem) {
        try {
            return new RespostaDemo(
                    codigo,
                    new JSONObject()
                            .put("status", codigo)
                            .put("codigoError", negocio)
                            .put("details", new JSONArray().put(new JSONObject().put("field", "senha").put("message", mensagem)))
                            .toString());

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    @Nullable
    private RespostaDemo exigirToken(@NonNull RequisicaoDemo r) {
        return ContaDemo.tokenConfere(estado.raiz(), r.autorizacao) ? null : RespostaDemo.erro(401, "NAO_AUTORIZADO");
    }
}
