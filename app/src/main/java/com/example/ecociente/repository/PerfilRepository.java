package com.example.ecociente.repository;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.model.PreferenciasNotificacao;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.model.SessaoExterna;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PerfilRepository {
    private static final String TAG = "PerfilEcoCiente";
    private static final String COLECAO_USUARIOS = "usuarios";
    private static final String CAMPO_PREFERENCIAS = "preferenciasNotificacao";

    public static final String CAMPO_NOME = "nome";
    public static final String CAMPO_TELEFONE = "telefone";
    public static final String CAMPO_ENDERECO = "endereco";
    public static final String CAMPO_CPF = "cpf";
    public static final String CAMPO_FOTO_URL = "fotoUrl";

    private final FirebaseAuth autenticacao = FirebaseAuth.getInstance();
    private final FirebaseFirestore bancoFirestore = FirestoreProvider.obterInstancia();
    private final AutenticacaoExternaRepository autenticacaoExterna = new AutenticacaoExternaRepository();
    private final AlterarSenhaExternaRepository senhaExterna = new AlterarSenhaExternaRepository();

    @Nullable
    public FirebaseUser usuarioAtual() {
        return autenticacao.getCurrentUser();
    }

    public void encerrarSessao() {
        autenticacao.signOut();
    }

    @NonNull
    public LiveData<PerfilUsuario> buscarPerfil(@NonNull String uid) {
        MutableLiveData<PerfilUsuario> resultado = new MutableLiveData<>();

        bancoFirestore
                .collection(COLECAO_USUARIOS)
                .document(uid)
                .get()
                .addOnSuccessListener(
                        documento -> resultado.setValue(documento.toObject(PerfilUsuario.class)))
                .addOnFailureListener(
                        erro -> {
                            Log.e(TAG, "Erro ao buscar perfil", erro);
                            resultado.setValue(null);
                        });

        return resultado;
    }

    @NonNull
    public LiveData<ResultadoApi> atualizarCampo(
            @NonNull String uid, @NonNull String campo, @NonNull Object valor) {
        MutableLiveData<ResultadoApi> resultado = new MutableLiveData<>();

        bancoFirestore
                .collection(COLECAO_USUARIOS)
                .document(uid)
                .update(campo, valor)
                .addOnSuccessListener(ignorado -> resultado.setValue(ResultadoApi.sucesso()))
                .addOnFailureListener(
                        erro -> {
                            Log.e(TAG, "Erro ao atualizar campo " + campo, erro);
                            resultado.setValue(
                                    ResultadoApi.erro("Não foi possível salvar. Tente novamente."));
                        });

        return resultado;
    }

    @NonNull
    public LiveData<PreferenciasNotificacao> buscarPreferencias(@NonNull String uid) {
        MutableLiveData<PreferenciasNotificacao> resultado = new MutableLiveData<>();

        bancoFirestore
                .collection(COLECAO_USUARIOS)
                .document(uid)
                .get()
                .addOnSuccessListener(
                        documento -> {
                            PreferenciasNotificacao preferencias =
                                    documento.contains(CAMPO_PREFERENCIAS)
                                            ? documento.get(
                                                    CAMPO_PREFERENCIAS, PreferenciasNotificacao.class)
                                            : new PreferenciasNotificacao();

                            resultado.setValue(preferencias);
                        })
                .addOnFailureListener(
                        erro -> {
                            Log.e(TAG, "Erro ao buscar preferências de notificação", erro);
                            resultado.setValue(null);
                        });

        return resultado;
    }

    @NonNull
    public LiveData<ResultadoApi> atualizarPreferencia(
            @NonNull String uid, @NonNull String chave, boolean valor) {
        MutableLiveData<ResultadoApi> resultado = new MutableLiveData<>();

        bancoFirestore
                .collection(COLECAO_USUARIOS)
                .document(uid)
                .update(CAMPO_PREFERENCIAS + "." + chave, valor)
                .addOnSuccessListener(ignorado -> resultado.setValue(ResultadoApi.sucesso()))
                .addOnFailureListener(
                        erro -> {
                            Log.e(TAG, "Erro ao atualizar preferência " + chave, erro);
                            resultado.setValue(
                                    ResultadoApi.erro("Não foi possível salvar. Tente novamente."));
                        });

        return resultado;
    }

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

    @NonNull
    public LiveData<ResultadoApi> excluirConta(@NonNull String senhaAtual) {
        MutableLiveData<ResultadoApi> resultado = new MutableLiveData<>();

        FirebaseUser usuario = autenticacao.getCurrentUser();

        if (usuario == null || usuario.getEmail() == null) {
            resultado.setValue(ResultadoApi.erro("Não foi possível identificar o usuário."));
            return resultado;
        }

        String uid = usuario.getUid();

        AuthCredential credencial = EmailAuthProvider.getCredential(usuario.getEmail(), senhaAtual);

        usuario.reauthenticate(credencial)
                .addOnSuccessListener(ignorado -> excluirDocumentoEConta(usuario, uid, resultado))
                .addOnFailureListener(
                        erro -> {
                            Log.e(TAG, "Erro ao reautenticar para excluir conta", erro);

                            if (erro instanceof FirebaseAuthInvalidCredentialsException) {
                                resultado.setValue(ResultadoApi.erro("Senha incorreta."));
                                return;
                            }

                            resultado.setValue(ResultadoApi.erro("Não foi possível confirmar sua senha."));
                        });

        return resultado;
    }

    private void excluirDocumentoEConta(
            @NonNull FirebaseUser usuario,
            @NonNull String uid,
            @NonNull MutableLiveData<ResultadoApi> resultado) {

        bancoFirestore
                .collection(COLECAO_USUARIOS)
                .document(uid)
                .delete()
                .addOnCompleteListener(
                        tarefaFirestore -> {
                            if (!tarefaFirestore.isSuccessful()) {
                                Log.e(
                                        TAG,
                                        "Erro ao apagar documento do usuário",
                                        tarefaFirestore.getException());
                            }

                            usuario.delete()
                                    .addOnSuccessListener(
                                            ignorado -> resultado.setValue(ResultadoApi.sucesso()))
                                    .addOnFailureListener(
                                            erro -> {
                                                Log.e(TAG, "Erro ao excluir conta", erro);
                                                resultado.setValue(
                                                        ResultadoApi.erro(
                                                                "Não foi possível excluir sua conta."));
                                            });
                        });
    }
}
