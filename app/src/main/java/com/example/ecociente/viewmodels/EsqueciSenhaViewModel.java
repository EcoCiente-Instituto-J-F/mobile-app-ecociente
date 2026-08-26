package com.example.ecociente.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.repository.EsqueciSenhaRepository;

// ViewModel compartilhado pelos 3 passos do fluxo "esqueci a senha" (criado com
// escopo na Activity, então sobrevive à troca de Fragment). Guarda o estado do
// fluxo (email, código) e expõe o resultado de cada chamada como LiveData -
// os Fragments só observam, sem lidar com rede/estado de carregamento.
public class EsqueciSenhaViewModel extends ViewModel {
    private final EsqueciSenhaRepository repositorio = new EsqueciSenhaRepository();
    private final MutableLiveData<Boolean> carregando = new MutableLiveData<>(false);
    private String email = "";
    private String codigo = "";

    @NonNull
    public LiveData<Boolean> getCarregando() {
        return carregando;
    }

    @NonNull
    public LiveData<ResultadoApi> enviarCodigo(@NonNull String email) {
        this.email = email;
        return executar(repositorio.enviarCodigo(email));
    }

    @NonNull
    public LiveData<ResultadoApi> verificarCodigo(@NonNull String codigo) {
        this.codigo = codigo;
        return executar(repositorio.verificarCodigo(email, codigo));
    }

    @NonNull
    public LiveData<ResultadoApi> redefinirSenha(@NonNull String novaSenha) {
        return executar(repositorio.redefinirSenha(email, codigo, novaSenha));
    }

    @NonNull
    private LiveData<ResultadoApi> executar(@NonNull LiveData<ResultadoApi> chamada) {
        carregando.setValue(true);

        MediatorLiveData<ResultadoApi> resultado = new MediatorLiveData<>();
        resultado.addSource(
                chamada,
                valor -> {
                    carregando.setValue(false);
                    resultado.setValue(valor);
                });

        return resultado;
    }
}
