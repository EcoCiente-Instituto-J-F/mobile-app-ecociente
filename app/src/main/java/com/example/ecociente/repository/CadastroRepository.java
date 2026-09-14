package com.example.ecociente.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.ecociente.model.ResultadoCadastro;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;

public class CadastroRepository {
    private static final String TAG = "CadastroEcoCiente";

    private final FirebaseAuth autenticacao = FirebaseAuth.getInstance();
    private final FirebaseFirestore bancoFirestore = FirebaseFirestore.getInstance();

    @NonNull
    public LiveData<ResultadoCadastro> cadastrar(
            @NonNull String nome,
            @NonNull String email,
            @NonNull String senha,
            @NonNull Map<String, Object> dadosUsuario) {
        MutableLiveData<ResultadoCadastro> resultado = new MutableLiveData<>();

        autenticacao
                .createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(
                        tarefa -> {
                            if (!tarefa.isSuccessful()) {
                                Log.e(TAG, "Erro ao criar conta", tarefa.getException());

                                resultado.setValue(traduzirErroCadastro(tarefa.getException()));
                                return;
                            }

                            atualizarNomeESalvarDados(nome, dadosUsuario, resultado);
                        });

        return resultado;
    }

    private void atualizarNomeESalvarDados(
            @NonNull String nome,
            @NonNull Map<String, Object> dadosUsuario,
            @NonNull MutableLiveData<ResultadoCadastro> resultado) {
        FirebaseUser usuario = autenticacao.getCurrentUser();

        if (usuario == null) {
            resultado.setValue(
                    ResultadoCadastro.erro("A conta foi criada, mas não foi possível carregar o usuário."));
            return;
        }

        UserProfileChangeRequest perfil =
                new UserProfileChangeRequest.Builder().setDisplayName(nome).build();

        usuario.updateProfile(perfil)
                .addOnCompleteListener(
                        tarefaPerfil -> {
                            if (!tarefaPerfil.isSuccessful()) {
                                Log.e(
                                        TAG,
                                        "Erro ao atualizar nome do perfil",
                                        tarefaPerfil.getException());
                            }

                            salvarDadosUsuario(usuario.getUid(), dadosUsuario, resultado);
                        });
    }

    private void salvarDadosUsuario(
            @NonNull String uid,
            @NonNull Map<String, Object> dadosUsuario,
            @NonNull MutableLiveData<ResultadoCadastro> resultado) {
        bancoFirestore
                .collection("usuarios")
                .document(uid)
                .set(dadosUsuario)
                .addOnCompleteListener(
                        tarefa -> {
                            if (!tarefa.isSuccessful()) {
                                Log.e(TAG, "Erro ao salvar dados do usuário", tarefa.getException());

                                resultado.setValue(
                                        ResultadoCadastro.sucessoComAviso(
                                                uid,
                                                "Conta criada, mas não foi possível salvar seus dados."));
                                return;
                            }

                            resultado.setValue(ResultadoCadastro.sucesso(uid));
                        });
    }

    @NonNull
    private ResultadoCadastro traduzirErroCadastro(Exception erro) {
        if (erro instanceof FirebaseAuthUserCollisionException) {
            return ResultadoCadastro.erro("Já existe uma conta cadastrada com este e-mail.");
        }

        if (erro instanceof FirebaseAuthWeakPasswordException) {
            return ResultadoCadastro.erro("A senha informada é muito fraca.");
        }

        if (erro instanceof FirebaseAuthInvalidCredentialsException) {
            return ResultadoCadastro.erro("O e-mail informado é inválido.");
        }

        if (erro != null && erro.getLocalizedMessage() != null) {
            return ResultadoCadastro.erro(erro.getLocalizedMessage());
        }

        return ResultadoCadastro.erro("Não foi possível criar sua conta.");
    }
}
