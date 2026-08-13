package com.example.ecociente.controller;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;
import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import com.example.ecociente.MainActivity;
import com.example.ecociente.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import java.util.Arrays;

public class Login extends AppCompatActivity {
    private static final String TAG = "LoginEcoCiente";
    private EditText campoEmail;
    private EditText campoSenha;
    private MaterialButton botaoLogin;
    private MaterialButton botaoGoogle;
    private MaterialButton botaoFacebook;
    private ImageView iconeOlhoSenha;
    private FirebaseAuth autenticacao;
    private CredentialManager gerenciadorCredenciais;
    private CallbackManager gerenciadorRetornoFacebook;
    private boolean senhaVisivel = false;
    private boolean loginEmAndamento = false;
    private TextView abaCadastro;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        setContentView(R.layout.activity_login);

        buscarComponentes();
        inicializarAutenticacao();
        configurarRetornoFacebook();
        configurarCliques();
    }

    private void buscarComponentes() {
        campoEmail = findViewById(R.id.campoEmail);
        campoSenha = findViewById(R.id.campoSenha);
        botaoLogin = findViewById(R.id.botaoLogin);
        botaoGoogle = findViewById(R.id.botaoGoogle);
        botaoFacebook = findViewById(R.id.botaoFacebook);
        iconeOlhoSenha = findViewById(R.id.iconeOlhoSenha);
        abaCadastro = findViewById(R.id.abaCadastro);
    }

    private void inicializarAutenticacao() {
        autenticacao = FirebaseAuth.getInstance();

        gerenciadorCredenciais = CredentialManager.create(getApplicationContext());

        gerenciadorRetornoFacebook = CallbackManager.Factory.create();
    }

    private void configurarCliques() {
        botaoLogin.setOnClickListener(clique -> fazerLoginComEmail());
        botaoGoogle.setOnClickListener(clique -> fazerLoginComGoogle());
        botaoFacebook.setOnClickListener(clique -> fazerLoginComFacebook());
        iconeOlhoSenha.setOnClickListener(clique -> alternarVisibilidadeSenha());
        abaCadastro.setOnClickListener(clique -> abrirCadastro());
    }

    private void fazerLoginComEmail() {
        if (loginEmAndamento) {
            return;
        }

        String email = campoEmail.getText().toString().trim();

        String senha = campoSenha.getText().toString();

        if (email.isEmpty() || senha.isEmpty()) {
            mostrarMensagem("Preencha o e-mail e a senha");
            return;
        }

        definirLoginEmAndamento(true);

        autenticacao.signInWithEmailAndPassword(email, senha).addOnCompleteListener(this, tarefa -> {
                    definirLoginEmAndamento(false);

                    if (tarefa.isSuccessful()) {
                        finalizarLogin();
                        return;
                    }

                    Log.e(TAG, "Erro no login por e-mail", tarefa.getException());

                    mostrarMensagem("E-mail ou senha incorretos");
                });
    }

    private void fazerLoginComGoogle() {
        if (loginEmAndamento) {
            return;
        }

        definirLoginEmAndamento(true);

        GetGoogleIdOption opcaoGoogle = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .setAutoSelectEnabled(false)
                        .build();

        GetCredentialRequest solicitacao =
                new GetCredentialRequest.Builder().addCredentialOption(opcaoGoogle).build();

        gerenciadorCredenciais.getCredentialAsync(this, solicitacao, new CancellationSignal(), ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<
                        GetCredentialResponse,
                        GetCredentialException>() {

                    @Override
                    public void onResult(@NonNull GetCredentialResponse resposta) {
                        tratarCredencialGoogle(
                                resposta.getCredential()
                        );
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException erro) {
                        definirLoginEmAndamento(false);

                        Log.e(TAG, "Erro ao obter credencial Google", erro);

                        if (erro instanceof GetCredentialCancellationException) {
                            mostrarMensagem("Login com Google cancelado");
                            return;
                        }

                        mostrarMensagem("Não foi possível entrar com o Google");
                    }
                }
        );
    }

    private void tratarCredencialGoogle(@NonNull Credential credencial) {
        if (!(credencial instanceof CustomCredential)) {
            definirLoginEmAndamento(false);

            mostrarMensagem("Credencial do Google inválida");
            return;
        }

        CustomCredential credencialPersonalizada = (CustomCredential) credencial;

        if (!TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credencialPersonalizada.getType())) {
            definirLoginEmAndamento(false);

            mostrarMensagem("Credencial do Google não reconhecida");
            return;
        }

        try {
            GoogleIdTokenCredential credencialGoogle = GoogleIdTokenCredential.createFrom(credencialPersonalizada.getData());

            autenticarGoogleNoFirebase(credencialGoogle.getIdToken());

        } catch (Exception erro) {
            definirLoginEmAndamento(false);

            Log.e(TAG, "Erro ao interpretar token Google", erro);

            mostrarMensagem("Não foi possível validar a conta Google");
        }
    }

    private void autenticarGoogleNoFirebase(@NonNull String tokenGoogle) {
        AuthCredential credencialFirebase = GoogleAuthProvider.getCredential(tokenGoogle, null);

        autenticarCredencialSocial(credencialFirebase, "Google");
    }

    private void configurarRetornoFacebook() {
        LoginManager
                .getInstance()
                .registerCallback(
                        gerenciadorRetornoFacebook,
                        new FacebookCallback<LoginResult>() {

                            @Override
                            public void onSuccess(@NonNull LoginResult resultado) {
                                autenticarFacebookNoFirebase(
                                        resultado.getAccessToken()
                                );
                            }

                            @Override
                            public void onCancel() {
                                definirLoginEmAndamento(false);

                                mostrarMensagem("Login com Facebook cancelado");
                            }

                            @Override
                            public void onError(@NonNull FacebookException erro) {
                                definirLoginEmAndamento(false);

                                Log.e(TAG, "Erro no login do Facebook", erro);

                                mostrarMensagem("Não foi possível entrar com o Facebook");
                            }
                        }
                );
    }

    private void fazerLoginComFacebook() {
        if (loginEmAndamento) {
            return;
        }

        definirLoginEmAndamento(true);

        LoginManager
                .getInstance()
                .logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
    }

    private void autenticarFacebookNoFirebase(@NonNull AccessToken tokenFacebook) {
        AuthCredential credencialFirebase = FacebookAuthProvider.getCredential(tokenFacebook.getToken());

        autenticarCredencialSocial(credencialFirebase, "Facebook");
    }

    private void autenticarCredencialSocial(@NonNull AuthCredential credencial, @NonNull String nomeProvedor) {
        autenticacao
                .signInWithCredential(credencial)
                .addOnCompleteListener(this, tarefa -> {
                    definirLoginEmAndamento(false);

                    if (tarefa.isSuccessful()) {
                        finalizarLogin();
                        return;
                    }

                    Exception erro = tarefa.getException();

                    Log.e(TAG, "Erro no Firebase com " + nomeProvedor, erro);

                    if (erro instanceof FirebaseAuthUserCollisionException) {

                        mostrarMensagem("Já existe uma conta com esse e-mail usando outro método de login");
                        return;
                    }

                    mostrarMensagem("Não foi possível entrar com " + nomeProvedor);
                });
    }

    private void definirLoginEmAndamento(boolean emAndamento) {
        loginEmAndamento = emAndamento;

        botaoLogin.setEnabled(!emAndamento);
        botaoGoogle.setEnabled(!emAndamento);
        botaoFacebook.setEnabled(!emAndamento);

        float transparencia = emAndamento ? 0.55f : 1f;

        botaoLogin.setAlpha(transparencia);
        botaoGoogle.setAlpha(transparencia);
        botaoFacebook.setAlpha(transparencia);
    }

    private void finalizarLogin() {FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        if (usuarioAtual == null) {
            mostrarMensagem("Não foi possível identificar o usuário");
            return;
        }

        mostrarMensagem("Login realizado com sucesso");

        Intent rota = new Intent(Login.this, MainActivity.class);

        rota.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(rota);
        finish();
    }

    private void alternarVisibilidadeSenha() {
        if (senhaVisivel) {
            campoSenha.setTransformationMethod(PasswordTransformationMethod.getInstance());

            iconeOlhoSenha.setImageResource(R.drawable.icon_olho_fechado);

            iconeOlhoSenha.setContentDescription("Mostrar senha");

            senhaVisivel = false;

        } else {
            campoSenha.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            iconeOlhoSenha.setImageResource(R.drawable.icon_olho_aberto);
            iconeOlhoSenha.setContentDescription("Ocultar senha");
            senhaVisivel = true;
        }

        campoSenha.setSelection(campoSenha.getText().length());
    }

    private void mostrarMensagem(@NonNull String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }

    private void abrirCadastro() {
        Intent rota = new Intent(Login.this, Cadastro.class);
        startActivity(rota);
        finish();
    }

    @Override
    protected void onActivityResult(int codigoSolicitacao, int codigoResultado, Intent dados) {
        super.onActivityResult(codigoSolicitacao, codigoResultado, dados);

        gerenciadorRetornoFacebook.onActivityResult(codigoSolicitacao, codigoResultado, dados);
    }

    @Override
    protected void onDestroy() {
        if (gerenciadorRetornoFacebook != null) {
            LoginManager.getInstance().unregisterCallback(gerenciadorRetornoFacebook);
        }

        super.onDestroy();
    }
}