package com.example.ecociente.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.model.PreferenciasNotificacao;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.model.ResultadoPerfil;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.FirebaseApp;
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

    @Nullable
    public FirebaseUser usuarioAtual() {
        return autenticacao.getCurrentUser();
    }

    public void encerrarSessao() {
        autenticacao.signOut();

        AutenticacaoExternaRepository.encerrarSessao(
                FirebaseApp.getInstance().getApplicationContext());
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
    public LiveData<ResultadoPerfil> carregarPerfil(@NonNull String uid) {
        MutableLiveData<ResultadoPerfil> resultado = new MutableLiveData<>();

        bancoFirestore
                .collection(COLECAO_USUARIOS)
                .document(uid)
                .get()
                .addOnSuccessListener(
                        documento -> {
                            PerfilUsuario perfil =
                                    documento.exists() ? documento.toObject(PerfilUsuario.class) : null;

                            resultado.setValue(
                                    perfil != null
                                            ? ResultadoPerfil.encontrado(perfil)
                                            : ResultadoPerfil.inexistente());
                        })
                .addOnFailureListener(
                        erro -> {
                            Log.e(TAG, "Erro ao carregar perfil", erro);
                            resultado.setValue(ResultadoPerfil.falha());
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
