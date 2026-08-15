package com.example.ecociente.login;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

// Camada de dados (Model) do login: só sabe falar com o FirebaseAuth e devolver
// o resultado como LiveData, sem nenhuma lógica de tela (isso fica no ViewModel).
class LoginRepository {
    private static final String TAG = "LoginEcoCiente";
    private final FirebaseAuth autenticacao = FirebaseAuth.getInstance();

    @NonNull
    LiveData<ResultadoLogin> signInWithEmailAndPassword(@NonNull String email, @NonNull String senha) {
        MutableLiveData<ResultadoLogin> resultado = new MutableLiveData<>();

        autenticacao.signInWithEmailAndPassword(email, senha).addOnCompleteListener(tarefa -> {
            if (tarefa.isSuccessful()) {
                resultado.setValue(confirmarUsuarioLogado());
                return;
            }

            Log.e(TAG, "Erro no login por e-mail", tarefa.getException());

            resultado.setValue(ResultadoLogin.erro("E-mail ou senha incorretos"));
        });

        return resultado;
    }

    @NonNull
    LiveData<ResultadoLogin> signInWithCredential(@NonNull AuthCredential credencial, @NonNull String nomeProvedor) {
        MutableLiveData<ResultadoLogin> resultado = new MutableLiveData<>();

        autenticacao.signInWithCredential(credencial).addOnCompleteListener(tarefa -> {
            if (tarefa.isSuccessful()) {
                resultado.setValue(confirmarUsuarioLogado());
                return;
            }

            Exception erro = tarefa.getException();

            Log.e(TAG, "Erro no Firebase com " + nomeProvedor, erro);

            if (erro instanceof FirebaseAuthUserCollisionException) {
                resultado.setValue(ResultadoLogin.erro("Já existe uma conta com esse e-mail usando outro método de login"));
                return;
            }

            resultado.setValue(ResultadoLogin.erro("Não foi possível entrar com " + nomeProvedor));
        });

        return resultado;
    }

    // Mesma checagem defensiva que já existia: garante que o Firebase realmente
    // deixou um usuário autenticado antes de reportar sucesso.
    @NonNull
    private ResultadoLogin confirmarUsuarioLogado() {
        if (autenticacao.getCurrentUser() == null) {
            return ResultadoLogin.erro("Não foi possível identificar o usuário");
        }

        return ResultadoLogin.sucesso();
    }
}
