package com.example.ecociente.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.ecociente.model.EstadoHome;
import com.example.ecociente.model.PerfilAcesso;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.model.ResultadoPerfil;
import com.example.ecociente.repository.NotificacaoMotivacionalRepository;
import com.example.ecociente.repository.PerfilRepository;
import com.google.firebase.auth.FirebaseUser;

public class HomeViewModel extends ViewModel {
    private final PerfilRepository perfilRepositorio = new PerfilRepository();
    private final NotificacaoMotivacionalRepository mensagemRepositorio =
            new NotificacaoMotivacionalRepository();

    private final MediatorLiveData<EstadoHome> estado = new MediatorLiveData<>();
    private final MediatorLiveData<String> fotoUrl = new MediatorLiveData<>();
    private final MutableLiveData<String> mensagem = new MutableLiveData<>();
    private final MutableLiveData<Boolean> atualizandoMensagem = new MutableLiveData<>(false);

    @NonNull
    public LiveData<EstadoHome> getEstado() {
        return estado;
    }

    @NonNull
    public LiveData<String> getFotoUrl() {
        return fotoUrl;
    }

    @NonNull
    public LiveData<String> getMensagem() {
        return mensagem;
    }

    @NonNull
    public LiveData<Boolean> getAtualizandoMensagem() {
        return atualizandoMensagem;
    }

    public void encerrarSessao() {
        perfilRepositorio.encerrarSessao();
    }

    public void carregar() {

        FirebaseUser usuario = perfilRepositorio.usuarioAtual();

        if (usuario == null) {
            estado.setValue(EstadoHome.de(EstadoHome.Tipo.SEM_LOGIN));
            return;
        }

        estado.setValue(EstadoHome.de(EstadoHome.Tipo.CARREGANDO));

        LiveData<ResultadoPerfil> consulta = perfilRepositorio.carregarPerfil(usuario.getUid());

        estado.addSource(
                consulta,
                resultado -> {
                    estado.removeSource(consulta);

                    estado.setValue(interpretar(usuario, resultado));
                });
    }

    public void recarregarFoto() {

        FirebaseUser usuario = perfilRepositorio.usuarioAtual();

        if (usuario == null) {
            return;
        }

        LiveData<PerfilUsuario> consulta = perfilRepositorio.buscarPerfil(usuario.getUid());

        fotoUrl.addSource(
                consulta,
                perfil -> {
                    fotoUrl.removeSource(consulta);

                    if (perfil != null && !perfil.getFotoUrl().isEmpty()) {
                        fotoUrl.setValue(perfil.getFotoUrl());
                    }
                });
    }

    public void atualizarMensagem() {

        atualizandoMensagem.setValue(true);

        mensagemRepositorio.buscar(
                texto -> {
                    atualizandoMensagem.setValue(false);

                    if (texto != null) {
                        mensagem.setValue(texto);
                    }
                });
    }

    @NonNull
    private EstadoHome interpretar(@NonNull FirebaseUser usuario, @NonNull ResultadoPerfil resultado) {

        if (resultado.isFalha()) {
            return EstadoHome.de(EstadoHome.Tipo.ERRO_REDE);
        }

        PerfilUsuario perfil = resultado.getPerfil();

        if (perfil == null) {
            return EstadoHome.de(EstadoHome.Tipo.CADASTRO_INCOMPLETO);
        }

        switch (PerfilAcesso.destinoDaHome(perfil)) {
            case COOPERATIVA:
                return EstadoHome.de(EstadoHome.Tipo.COOPERATIVA);

            case CONDOMINIO:
                return EstadoHome.de(EstadoHome.Tipo.CONDOMINIO);

            case USUARIO_COMUM:
                fotoUrl.setValue(perfil.getFotoUrl());

                return EstadoHome.pronta(PerfilUsuario.nomeExibicao(perfil, usuario));

            default:
                return EstadoHome.de(EstadoHome.Tipo.SEM_ACESSO);
        }
    }
}
