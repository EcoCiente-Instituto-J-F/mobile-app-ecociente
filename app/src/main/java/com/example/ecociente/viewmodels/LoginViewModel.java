package com.example.ecociente.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.ecociente.R;
import com.example.ecociente.model.ResultadoCadastro;
import com.example.ecociente.model.ResultadoLogin;
import com.example.ecociente.model.SessaoExterna;
import com.example.ecociente.model.TipoPerfil;
import com.example.ecociente.model.TipoUsuario;
import com.example.ecociente.repository.AutenticacaoExternaRepository;
import com.example.ecociente.repository.CadastroRepository;
import com.example.ecociente.repository.LoginRepository;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// ViewModel do Login. A Activity continua responsável por obter a credencial
// do Google/Facebook (essas APIs exigem uma Activity, não dá pra mover pra cá),
// mas tudo que vem depois disso - autenticar no Firebase e tratar erro - é aqui.
//
// Contas criadas fora do app (ex.: síndico cadastrado pela landing page) existem só no Postgres.
// Quando o Firebase não conhece o e-mail, o login tenta a API; se ela confirmar a senha, a conta
// do Firebase e o perfil no Firestore são criados na hora, com o perfil que a API devolveu.
public class LoginViewModel extends AndroidViewModel {
    private final LoginRepository repositorio = new LoginRepository();
    private final CadastroRepository cadastroRepositorio = new CadastroRepository();
    private final AutenticacaoExternaRepository apiExterna = new AutenticacaoExternaRepository();
    private final MutableLiveData<Boolean> carregando = new MutableLiveData<>(false);

    public LoginViewModel(@NonNull Application aplicacao) {
        super(aplicacao);
    }

    @NonNull
    public LiveData<Boolean> getCarregando() {
        return carregando;
    }

    @NonNull
    public LiveData<ResultadoLogin> entrarComEmail(@NonNull String email, @NonNull String senha) {

        carregando.setValue(true);

        MediatorLiveData<ResultadoLogin> resultado = new MediatorLiveData<>();

        LiveData<ResultadoLogin> pelaFirebase = repositorio.signInWithEmailAndPassword(email, senha);

        resultado.addSource(
                pelaFirebase,
                valor -> {
                    resultado.removeSource(pelaFirebase);

                    if (valor.isSucesso()) {
                        confirmarNaApi(email, senha, valor, resultado);
                        return;
                    }

                    entrarPelaApi(email, senha, valor, resultado);
                });

        return resultado;
    }

    // O Firebase aceitou, mas a conta também precisa existir na API (Postgres): é de lá que vêm o
    // perfil e o token das outras APIs. Se a API recusar, desfaz o login; se estiver fora do ar,
    // continua entrando sem token, como antes.
    private void confirmarNaApi(
            @NonNull String email,
            @NonNull String senha,
            @NonNull ResultadoLogin loginDoFirebase,
            @NonNull MediatorLiveData<ResultadoLogin> resultado) {

        apiExterna.autenticar(
                getApplication(),
                email,
                senha,
                (sessao, recusada) -> {
                    carregando.setValue(false);

                    if (sessao == null && recusada) {
                        repositorio.sair();
                        resultado.setValue(
                                ResultadoLogin.erro(getApplication().getString(R.string.login_conta_nao_confirmada)));
                        return;
                    }

                    resultado.setValue(loginDoFirebase);
                });
    }

    private void entrarPelaApi(
            @NonNull String email,
            @NonNull String senha,
            @NonNull ResultadoLogin falhaDoFirebase,
            @NonNull MediatorLiveData<ResultadoLogin> resultado) {

        apiExterna.autenticar(
                getApplication(),
                email,
                senha,
                sessao -> {
                    if (sessao == null) {
                        carregando.setValue(false);
                        resultado.setValue(falhaDoFirebase);
                        return;
                    }

                    LiveData<ResultadoCadastro> criacao =
                            cadastroRepositorio.cadastrar(
                                    sessao.getNome(), email, senha, dadosDoPerfil(sessao, email));

                    resultado.addSource(
                            criacao,
                            conta -> {
                                resultado.removeSource(criacao);

                                carregando.setValue(false);

                                if (conta.isSucesso()) {
                                    resultado.setValue(ResultadoLogin.sucesso());
                                } else if (conta.isContaJaExistente()) {
                                    resultado.setValue(falhaDoFirebase);
                                } else {
                                    resultado.setValue(ResultadoLogin.erro(conta.getMensagemErro()));
                                }
                            });
                });
    }

    @NonNull
    private Map<String, Object> dadosDoPerfil(@NonNull SessaoExterna sessao, @NonNull String email) {

        String perfil = sessao.getPerfil().toUpperCase(Locale.ROOT);

        boolean cooperativa = "COOPERATIVA".equals(perfil);
        boolean sindico = "SINDICO".equals(perfil);
        boolean morador = "MORADOR".equals(perfil);

        Map<String, Object> dados = new HashMap<>();

        dados.put("nome", sessao.getNome());
        dados.put("email", email.trim().toLowerCase(Locale.ROOT));
        dados.put("tipoPerfil", cooperativa ? TipoPerfil.COOPERATIVA : sindico ? TipoPerfil.SINDICO : TipoPerfil.USUARIO);
        dados.put(
                "tipoUsuario",
                cooperativa
                        ? TipoUsuario.COOPERATIVA
                        : sindico ? TipoPerfil.SINDICO : morador ? TipoUsuario.MORADOR : TipoUsuario.COMUM);
        dados.put("possuiCodigoCondominio", morador || sindico);
        dados.put("codigoCondominio", "");

        if (!cooperativa && !morador && !sindico) {
            dados.put("endereco", "Não informado");
        }

        return dados;
    }

    @NonNull
    public LiveData<ResultadoLogin> entrarComGoogle(@NonNull String idTokenGoogle) {
        AuthCredential credencial = GoogleAuthProvider.getCredential(idTokenGoogle, null);
        return executar(repositorio.signInWithCredential(credencial, "Google"));
    }

    @NonNull
    public LiveData<ResultadoLogin> entrarComFacebook(@NonNull String tokenFacebook) {
        AuthCredential credencial = FacebookAuthProvider.getCredential(tokenFacebook);
        return executar(repositorio.signInWithCredential(credencial, "Facebook"));
    }

    @NonNull
    private LiveData<ResultadoLogin> executar(@NonNull LiveData<ResultadoLogin> chamada) {
        carregando.setValue(true);

        MediatorLiveData<ResultadoLogin> resultado = new MediatorLiveData<>();
        resultado.addSource(
                chamada,
                valor -> {
                    carregando.setValue(false);
                    resultado.setValue(valor);
                });

        return resultado;
    }
}
