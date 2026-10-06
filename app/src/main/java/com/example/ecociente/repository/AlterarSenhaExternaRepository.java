package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import com.example.ecociente.model.ResultadoApi;
import org.json.JSONException;
import org.json.JSONObject;

// POST /senhas/alterar da ds-esqueceusenha-api: exige o JWT da ds-autenticacao-api e
// identifica o usuário pelo token, por isso não recebe o e-mail.
public class AlterarSenhaExternaRepository {

    public interface Retorno {
        void aoConcluir(@NonNull ResultadoApi resultado);
    }

    public void alterar(
            @NonNull String token,
            @NonNull String senhaAtual,
            @NonNull String novaSenha,
            @NonNull Retorno retorno) {
        try {
            JSONObject corpo =
                    new JSONObject()
                            .put("senhaAtual", senhaAtual)
                            .put("novaSenha", novaSenha)
                            .put("confirmacaoSenha", novaSenha);

            ApiEsqueciSenha.chamar(
                    "alterar",
                    corpo,
                    token,
                    (sucesso, mensagemErro) ->
                            retorno.aoConcluir(
                                    sucesso
                                            ? ResultadoApi.sucesso()
                                            : ResultadoApi.erro(mensagemErro)));

        } catch (JSONException erro) {
            retorno.aoConcluir(ResultadoApi.erro("Erro inesperado. Tente novamente"));
        }
    }
}
