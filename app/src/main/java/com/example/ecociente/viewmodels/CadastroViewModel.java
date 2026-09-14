package com.example.ecociente.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.ecociente.model.ResultadoCadastro;
import com.example.ecociente.repository.CadastroRepository;
import java.util.Map;

public class CadastroViewModel extends ViewModel {
    private final CadastroRepository repositorio = new CadastroRepository();
    private final MutableLiveData<Boolean> carregando = new MutableLiveData<>(false);

    @NonNull
    public LiveData<Boolean> getCarregando() {
        return carregando;
    }

    @NonNull
    public LiveData<ResultadoCadastro> cadastrar(
            @NonNull String nome,
            @NonNull String email,
            @NonNull String senha,
            @NonNull Map<String, Object> dadosUsuario) {
        carregando.setValue(true);

        MediatorLiveData<ResultadoCadastro> resultado = new MediatorLiveData<>();
        resultado.addSource(
                repositorio.cadastrar(nome, email, senha, dadosUsuario),
                valor -> {
                    carregando.setValue(false);
                    resultado.setValue(valor);
                });

        return resultado;
    }
}
