package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.ecociente.model.ResultadoApi;
import org.json.JSONException;
import org.json.JSONObject;

// Camada de dados (Model) do fluxo "esqueci a senha": só sabe montar as
// chamadas HTTP pro backend e devolver o resultado como LiveData, sem
// nenhuma lógica de tela (isso fica no ViewModel).
public class EsqueciSenhaRepository {

    @NonNull
    public LiveData<ResultadoApi> enviarCodigo(@NonNull String email) {
        return chamar("enviarCodigoRecuperacao", corpo(email, null, null));
    }

    @NonNull
    public LiveData<ResultadoApi> verificarCodigo(@NonNull String email, @NonNull String codigo) {
        return chamar("verificarCodigoRecuperacao", corpo(email, codigo, null));
    }

    @NonNull
    public LiveData<ResultadoApi> redefinirSenha(@NonNull String email, @NonNull String codigo, @NonNull String novaSenha) {
        return chamar("redefinirSenhaComCodigo", corpo(email, codigo, novaSenha));
    }

    @NonNull
    private LiveData<ResultadoApi> chamar(String endpoint, @Nullable JSONObject corpo) {
        MutableLiveData<ResultadoApi> resultado = new MutableLiveData<>();

        if (corpo == null) {
            resultado.setValue(ResultadoApi.erro("Erro inesperado. Tente novamente"));
            return resultado;
        }

        ApiEsqueciSenha.chamar(endpoint, corpo, (sucesso, mensagemErro) ->
                resultado.setValue(sucesso ? ResultadoApi.sucesso() : ResultadoApi.erro(mensagemErro)));

        return resultado;
    }

    @Nullable
    private JSONObject corpo(String email, @Nullable String codigo, @Nullable String novaSenha) {
        try {
            JSONObject corpo = new JSONObject();
            corpo.put("email", email);

            if (codigo != null) {
                corpo.put("codigo", codigo);
            }

            if (novaSenha != null) {
                corpo.put("novaSenha", novaSenha);
            }

            return corpo;
        } catch (JSONException erro) {
            return null;
        }
    }
}
