package com.example.ecociente.viewmodels;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.model.PreferenciasNotificacao;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.model.ResultadoUploadFoto;
import com.example.ecociente.repository.FotoPerfilRepository;
import com.example.ecociente.repository.PerfilRepository;
import com.google.firebase.auth.FirebaseUser;

public class PerfilViewModel extends ViewModel {
    private final PerfilRepository repositorio = new PerfilRepository();
    private final FotoPerfilRepository fotoRepositorio = new FotoPerfilRepository();
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
            @NonNull Context contexto, @NonNull String senhaAtual, @NonNull String novaSenha) {
        return executar(repositorio.alterarSenha(contexto, senhaAtual, novaSenha));
    }

    @NonNull
    public LiveData<ResultadoApi> excluirConta(@NonNull String senhaAtual) {
        return executar(repositorio.excluirConta(senhaAtual));
    }

    @NonNull
    public LiveData<ResultadoUploadFoto> atualizarFotoPerfil(
            @NonNull Context contexto, @NonNull String uid, @NonNull Uri imagem) {
        carregando.setValue(true);

        MediatorLiveData<ResultadoUploadFoto> resultado = new MediatorLiveData<>();

        LiveData<ResultadoUploadFoto> envio = fotoRepositorio.enviar(contexto, imagem);

        resultado.addSource(
                envio,
                respostaEnvio -> {
                    resultado.removeSource(envio);

                    if (!respostaEnvio.isSucesso()) {
                        carregando.setValue(false);
                        resultado.setValue(respostaEnvio);
                        return;
                    }

                    LiveData<ResultadoApi> gravacao =
                            repositorio.atualizarCampo(
                                    uid, PerfilRepository.CAMPO_FOTO_URL, respostaEnvio.getUrl());

                    resultado.addSource(
                            gravacao,
                            respostaGravacao -> {
                                carregando.setValue(false);

                                resultado.setValue(
                                        respostaGravacao.isSucesso()
                                                ? respostaEnvio
                                                : ResultadoUploadFoto.erro(
                                                        respostaGravacao.getMensagemErro()));
                            });
                });

        return resultado;
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
