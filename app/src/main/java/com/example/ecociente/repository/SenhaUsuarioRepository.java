package com.example.ecociente.repository;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.model.SessaoExterna;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

public class SenhaUsuarioRepository {
    private static final String TAG = "SenhaUsuarioEcoCiente";

    private final FirebaseAuth autenticacao = FirebaseAuth.getInstance();
    private final AutenticacaoExternaRepository autenticacaoExterna = new AutenticacaoExternaRepository();
    private final AlterarSenhaExternaRepository senhaExterna = new AlterarSenhaExternaRepository();

    // A senha precisa mudar no Firebase e no Postgres. O token do login expira em minutos,
    // então pegamos um novo com a senha atual; contas sem registro no Postgres (ou com a API
    // fora do ar) trocam só no Firebase.
    @NonNull
    public LiveData<ResultadoApi> alterarSenha(
            @NonNull Context contexto, @NonNull String senhaAtual, @NonNull String novaSenha) {
        MutableLiveData<ResultadoApi> resultado = new MutableLiveData<>();

        FirebaseUser usuario = autenticacao.getCurrentUser();

        if (usuario == null || usuario.getEmail() == null) {
            resultado.setValue(ResultadoApi.erro("Não foi possível identificar o usuário."));
            return resultado;
        }

        autenticacaoExterna.autenticar(
                contexto,
                usuario.getEmail(),
                senhaAtual,
                sessao -> alterarSenhaNoFirebase(usuario, senhaAtual, novaSenha, sessao, resultado));

        return resultado;
    }

    private void alterarSenhaNoFirebase(
            @NonNull FirebaseUser usuario,
            @NonNull String senhaAtual,
            @NonNull String novaSenha,
            @Nullable SessaoExterna sessao,
            @NonNull MutableLiveData<ResultadoApi> resultado) {

        AuthCredential credencial = EmailAuthProvider.getCredential(usuario.getEmail(), senhaAtual);

        usuario.reauthenticate(credencial)
                .addOnSuccessListener(
                        ignorado ->
                                usuario.updatePassword(novaSenha)
                                        .addOnSuccessListener(
                                                ignorado2 ->
                                                        sincronizarSenhaNoPostgres(
                                                                usuario,
                                                                senhaAtual,
                                                                novaSenha,
                                                                sessao,
                                                                resultado))
                                        .addOnFailureListener(
                                                erro -> {
                                                    Log.e(TAG, "Erro ao atualizar senha", erro);
                                                    resultado.setValue(
                                                            ResultadoApi.erro(
                                                                    "Não foi possível atualizar a senha."));
                                                }))
                .addOnFailureListener(
                        erro -> {
                            Log.e(TAG, "Erro ao reautenticar para trocar senha", erro);

                            if (erro instanceof FirebaseAuthInvalidCredentialsException) {
                                resultado.setValue(ResultadoApi.erro("Senha atual incorreta."));
                                return;
                            }

                            resultado.setValue(ResultadoApi.erro("Não foi possível confirmar sua senha atual."));
                        });
    }

    // Se o Postgres recusar depois do Firebase já ter mudado, volta o Firebase para a senha
    // antiga: assim os dois lugares nunca ficam com senhas diferentes.
    private void sincronizarSenhaNoPostgres(
            @NonNull FirebaseUser usuario,
            @NonNull String senhaAtual,
            @NonNull String novaSenha,
            @Nullable SessaoExterna sessao,
            @NonNull MutableLiveData<ResultadoApi> resultado) {

        if (sessao == null) {
            resultado.setValue(ResultadoApi.sucesso());
            return;
        }

        senhaExterna.alterar(
                sessao.getToken(),
                senhaAtual,
                novaSenha,
                respostaExterna -> {
                    if (respostaExterna.isSucesso()) {
                        resultado.setValue(respostaExterna);
                        return;
                    }

                    usuario.updatePassword(senhaAtual)
                            .addOnCompleteListener(
                                    tarefa -> {
                                        if (!tarefa.isSuccessful()) {
                                            Log.e(TAG, "Não foi possível desfazer a senha no Firebase", tarefa.getException());
                                        }

                                        resultado.setValue(respostaExterna);
                                    });
                });
    }
}
