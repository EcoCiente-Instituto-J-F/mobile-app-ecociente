package com.example.ecociente.viewmodels;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.model.PreferenciasNotificacao;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.model.ResultadoUploadFoto;
import com.example.ecociente.repository.AtualizacaoExternaRepository;
import com.example.ecociente.repository.ConsultaCepRepository;
import com.example.ecociente.repository.FotoPerfilRepository;
import com.example.ecociente.repository.PerfilRepository;
import com.google.firebase.auth.FirebaseUser;
import androidx.lifecycle.MutableLiveData;
import com.example.ecociente.repository.PerfilApiRepository;

public class PerfilViewModel extends ViewModelComCarregamento {
    private final PerfilRepository repositorio = new PerfilRepository();
    private final FotoPerfilRepository fotoRepositorio = new FotoPerfilRepository();
    private final AtualizacaoExternaRepository atualizacaoExterna = new AtualizacaoExternaRepository();
    private final MutableLiveData<Boolean> falhaDeSincronizacao = new MutableLiveData<>(false);
    private final Runnable avisarFalhaDeSincronizacao = () -> falhaDeSincronizacao.setValue(true);
    private final ConsultaCepRepository consultaCep = new ConsultaCepRepository();

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

    // Perfil do Firestore na hora e, quando a API responde, o mesmo perfil com os dados dela por cima
    // (o documento do Firestore é ajustado para ficar igual). Sem resposta da API, fica o do Firestore.
    @NonNull
    public LiveData<PerfilUsuario> buscarPerfilAtualizado(@NonNull Context contexto, @NonNull String uid) {

        Context contextoApp = contexto.getApplicationContext();

        MediatorLiveData<PerfilUsuario> resultado = new MediatorLiveData<>();

        LiveData<PerfilUsuario> doFirestore = repositorio.buscarPerfil(uid);

        resultado.addSource(
                doFirestore,
                perfil -> {
                    resultado.removeSource(doFirestore);

                    resultado.setValue(perfil);

                    if (perfil == null) {
                        return;
                    }

                    new PerfilApiRepository()
                            .atualizar(
                                    contextoApp,
                                    () -> {
                                        // Edição ainda a caminho da API: o app tem o dado mais novo.
                                        if (AtualizacaoExternaRepository.temAtualizacaoEmAndamento()) {
                                            return;
                                        }

                                        java.util.Map<String, String> mudancas =
                                                PerfilApiRepository.dadosDoPerfil(contextoApp).aplicarEm(perfil);

                                        if (mudancas.isEmpty()) {
                                            return;
                                        }

                                        for (java.util.Map.Entry<String, String> mudanca : mudancas.entrySet()) {
                                            repositorio.atualizarCampo(uid, mudanca.getKey(), mudanca.getValue());
                                        }

                                        resultado.setValue(perfil);
                                    });
                });

        return resultado;
    }

    public void sincronizarCampoComApi(
            @NonNull Context contexto,
            @NonNull String campo,
            @NonNull String valor,
            @Nullable PerfilUsuario perfil) {
        atualizacaoExterna.sincronizarCampo(
                contexto.getApplicationContext(), campo, valor, perfil, avisarFalhaDeSincronizacao);
    }

    // true quando a API recusou ou não recebeu a atualização; a tela mostra o aviso e chama
    // `falhaDeSincronizacaoAvisada`.
    public LiveData<Boolean> getFalhaDeSincronizacao() {
        return falhaDeSincronizacao;
    }

    public void falhaDeSincronizacaoAvisada() {
        falhaDeSincronizacao.setValue(false);
    }

    public void buscarLocalidadePorCep(
            @NonNull String cep, @NonNull ConsultaCepRepository.Retorno retorno) {
        consultaCep.buscar(cep, retorno);
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
    public LiveData<ResultadoApi> excluirConta(@NonNull String senhaAtual) {
        return executar(repositorio.excluirConta(senhaAtual));
    }

    @NonNull
    public LiveData<ResultadoUploadFoto> atualizarFotoPerfil(
            @NonNull Context contexto, @NonNull String uid, @NonNull Uri imagem) {
        return enviarFoto(contexto, uid, imagem, PerfilRepository.CAMPO_FOTO_URL, true);
    }

    // Foto do condomínio do síndico: vai ao Cloudinary e a URL fica no documento do usuário.
    // A API de cadastro ainda não tem campo para ela, por isso não sincroniza.
    @NonNull
    public LiveData<ResultadoUploadFoto> atualizarFotoCondominio(
            @NonNull Context contexto, @NonNull String uid, @NonNull Uri imagem) {
        return enviarFoto(contexto, uid, imagem, PerfilRepository.CAMPO_FOTO_CONDOMINIO_URL, false);
    }

    @NonNull
    private LiveData<ResultadoUploadFoto> enviarFoto(
            @NonNull Context contexto,
            @NonNull String uid,
            @NonNull Uri imagem,
            @NonNull String campo,
            boolean sincronizarComApi) {
        definirCarregando(true);

        // O envio sobrevive à tela: não prende a Activity nas callbacks.
        Context contextoApp = contexto.getApplicationContext();

        MediatorLiveData<ResultadoUploadFoto> resultado = new MediatorLiveData<>();

        LiveData<ResultadoUploadFoto> envio = fotoRepositorio.enviar(contextoApp, imagem);

        resultado.addSource(
                envio,
                respostaEnvio -> {
                    resultado.removeSource(envio);

                    if (!respostaEnvio.isSucesso()) {
                        definirCarregando(false);
                        resultado.setValue(respostaEnvio);
                        return;
                    }

                    LiveData<ResultadoApi> gravacao =
                            repositorio.atualizarCampo(uid, campo, respostaEnvio.getUrl());

                    resultado.addSource(
                            gravacao,
                            respostaGravacao -> {
                                definirCarregando(false);

                                // A URL do Cloudinary também vai para a API de cadastro (urlAvatar).
                                if (sincronizarComApi && respostaGravacao.isSucesso()) {
                                    atualizacaoExterna.sincronizarCampo(
                                            contextoApp,
                                            PerfilRepository.CAMPO_FOTO_URL,
                                            respostaEnvio.getUrl(),
                                            null,
                                            avisarFalhaDeSincronizacao);
                                }

                                resultado.setValue(
                                        respostaGravacao.isSucesso()
                                                ? respostaEnvio
                                                : ResultadoUploadFoto.erro(
                                                        respostaGravacao.getMensagemErro()));
                            });
                });

        return resultado;
    }
}
