package com.example.ecociente.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.ecociente.model.ResultadoLogin;
import com.example.ecociente.repository.LoginRepository;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;

// ViewModel do Login. A Activity continua responsável por obter a credencial
// do Google/Facebook (essas APIs exigem uma Activity, não dá pra mover pra cá),
// mas tudo que vem depois disso - autenticar no Firebase e tratar erro - é aqui.
public class LoginViewModel extends ViewModel {
    private final LoginRepository repositorio = new LoginRepository();
    private final MutableLiveData<Boolean> carregando = new MutableLiveData<>(false);

    @NonNull
    public LiveData<Boolean> getCarregando() {
        return carregando;
    }

    @NonNull
    public LiveData<ResultadoLogin> entrarComEmail(@NonNull String email, @NonNull String senha) {
        return executar(repositorio.signInWithEmailAndPassword(email, senha));
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
        resultado.addSource(chamada, valor -> {
            carregando.setValue(false);
            resultado.setValue(valor);
        });

        return resultado;
    }
}
