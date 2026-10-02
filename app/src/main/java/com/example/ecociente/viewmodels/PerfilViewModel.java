package com.example.ecociente.viewmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.model.PreferenciasNotificacao;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.repository.PerfilRepository;
import com.google.firebase.auth.FirebaseUser;

public class PerfilViewModel extends ViewModel {
    private final PerfilRepository repositorio = new PerfilRepository();
    private final MutableLiveData<Boolean> carregando = new MutableLiveData<>(false);

    @NonNull
    public LiveData<Boolean> getCarregando() {
        return carregando;
    }

    @Nullable
    public FirebaseUser usuarioAtual() {
        return repositorio.usuarioAtual();
    }

    public void encerrarSessao() {
        repositorio.encerrarSessao();
    }

    @NonNull
    public LiveData<PerfilUsuario> buscarPerfil(@NonNull String uid) {
        return executar(repositorio.buscarPerfil(uid));
    }

    @NonNull
    public LiveData<ResultadoApi> atualizarCampo(
            @NonNull String uid, @NonNull String campo, @NonNull Object valor) {
        return executar(repositorio.atualizarCampo(uid, campo, valor));
    }

    @NonNull
    public LiveData<PreferenciasNotificacao> buscarPreferencias(@NonNull String uid) {
        return executar(repositorio.buscarPreferencias(uid));
    }

    @NonNull
    public LiveData<ResultadoApi> atualizarPreferencia(
            @NonNull String uid, @NonNull String chave, boolean valor) {
        return executar(repositorio.atualizarPreferencia(uid, chave, valor));
    }

    @NonNull
    public LiveData<ResultadoApi> alterarSenha(
            @NonNull String senhaAtual, @NonNull String novaSenha) {
        return executar(repositorio.alterarSenha(senhaAtual, novaSenha));
    }

    @NonNull
    public LiveData<ResultadoApi> excluirConta(@NonNull String senhaAtual) {
        return executar(repositorio.excluirConta(senhaAtual));
    }

    @NonNull
    private <T> LiveData<T> executar(@NonNull LiveData<T> chamada) {
        carregando.setValue(true);

        MediatorLiveData<T> resultado = new MediatorLiveData<>();
        resultado.addSource(
                chamada,
                valor -> {
                    carregando.setValue(false);
                    resultado.setValue(valor);
                });

        return resultado;
    }
}
