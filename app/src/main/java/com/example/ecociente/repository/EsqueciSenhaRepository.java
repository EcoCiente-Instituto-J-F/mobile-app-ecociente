package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.ecociente.model.ResultadoApi;
import org.json.JSONException;
import org.json.JSONObject;

public class EsqueciSenhaRepository {

    @NonNull
    public LiveData<ResultadoApi> enviarCodigo(@NonNull String email) {
        try {
            return chamar("esqueceu", new JSONObject().put("email", email));
        } catch (JSONException erro) {
            return erroInesperado();
        }
    }

    // A API não tem passo separado de verificação: o código só é conferido aqui, junto com a nova senha.
    @NonNull
    public LiveData<ResultadoApi> redefinirSenha(
            @NonNull String email, @NonNull String codigo, @NonNull String novaSenha) {
        try {
            return chamar(
                    "redefinir",
                    new JSONObject()
                            .put("email", email)
                            .put("token", codigo)
                            .put("novaSenha", novaSenha)
                            .put("confirmacaoSenha", novaSenha));
        } catch (JSONException erro) {
            return erroInesperado();
        }
    }

    @NonNull
    private LiveData<ResultadoApi> chamar(@NonNull String endpoint, @NonNull JSONObject corpo) {
        MutableLiveData<ResultadoApi> resultado = new MutableLiveData<>();

        ApiEsqueciSenha.chamar(
                endpoint,
                corpo,
                (sucesso, mensagemErro) ->
                        resultado.setValue(
                                sucesso
                                        ? ResultadoApi.sucesso()
                                        : ResultadoApi.erro(mensagemErro)));

        return resultado;
    }

    @NonNull
    private LiveData<ResultadoApi> erroInesperado() {
        MutableLiveData<ResultadoApi> resultado = new MutableLiveData<>();

        resultado.setValue(ResultadoApi.erro("Erro inesperado. Tente novamente"));

        return resultado;
    }
}
