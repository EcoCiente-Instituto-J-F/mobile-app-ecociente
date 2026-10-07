package com.example.ecociente.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.repository.SenhaUsuarioRepository;

public class SenhaViewModel extends ViewModelComCarregamento {
    private final SenhaUsuarioRepository repositorio = new SenhaUsuarioRepository();

    @NonNull
    public LiveData<ResultadoApi> alterarSenha(
            @NonNull Context contexto, @NonNull String senhaAtual, @NonNull String novaSenha) {
        return executar(repositorio.alterarSenha(contexto, senhaAtual, novaSenha));
    }
}
