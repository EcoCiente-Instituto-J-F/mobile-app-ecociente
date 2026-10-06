package com.example.ecociente.views;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.inputmethod.EditorInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecociente.R;
import com.example.ecociente.ui.FieldFeedback;
import com.example.ecociente.ui.Motion;
import com.example.ecociente.repository.AutenticacaoExternaRepository;
import com.example.ecociente.viewmodels.LoginViewModel;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.Arrays;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginEcoCiente";
    private EditText campoEmail;
    private EditText campoSenha;
    private MaterialButton botaoLogin;
    private MaterialButton botaoGoogle;
    private MaterialButton botaoFacebook;
    private ImageView iconeOlhoSenha;
    private MaterialCardView containerEmail;
    private MaterialCardView containerSenha;
    private TextView erroEmail;
    private TextView erroSenha;
    private ProgressBar indicadorCarregamento;
    private Motion motion;
    private CredentialManager gerenciadorCredenciais;
    private CallbackManager gerenciadorRetornoFacebook;
    private CancellationSignal sinalCancelamentoGoogle;
    private LoginViewModel viewModel;
    private boolean senhaVisivel = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle estadoSalvo) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estadoSalvo) {
        super.onViewCreated(view, estadoSalvo);

        buscarComponentes(view);
        inicializarAutenticacao();
        configurarRetornoFacebook();
        configurarCliques();
        configurarFeedbackDeCampos();
        recuperarEmailPreenchido();
        motion.staggerIn(view.findViewById(R.id.tituloLogin), containerEmail, containerSenha, botaoLogin, botaoGoogle, botaoFacebook);
    }

    private void buscarComponentes(View view) {
        campoEmail = view.findViewById(R.id.campoEmail);
        campoSenha = view.findViewById(R.id.campoSenha);
        botaoLogin = view.findViewById(R.id.botaoLogin);
        botaoGoogle = view.findViewById(R.id.botaoGoogle);
        botaoFacebook = view.findViewById(R.id.botaoFacebook);
        iconeOlhoSenha = view.findViewById(R.id.iconeOlhoSenha);
        containerEmail = view.findViewById(R.id.containerEmail);
        containerSenha = view.findViewById(R.id.containerSenha);
        erroEmail = view.findViewById(R.id.erroEmailLogin);
        erroSenha = view.findViewById(R.id.erroSenhaLogin);
        indicadorCarregamento = view.findViewById(R.id.indicadorCarregamentoLogin);
        motion = new Motion();
    }

    private void inicializarAutenticacao() {
        gerenciadorCredenciais = CredentialManager.create(requireContext());
        gerenciadorRetornoFacebook = CallbackManager.Factory.create();
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        viewModel.getCarregando().observe(getViewLifecycleOwner(), this::definirLoginEmAndamento);
    }

    private void configurarCliques() {
        botaoLogin.setOnClickListener(clique -> fazerLoginComEmail());
        botaoGoogle.setOnClickListener(clique -> fazerLoginComGoogle());
        botaoFacebook.setOnClickListener(clique -> fazerLoginComFacebook());
        iconeOlhoSenha.setOnClickListener(clique -> alternarVisibilidadeSenha());
        campoSenha.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                fazerLoginComEmail();
                return true;
            }
            return false;
        });

        View textoEsqueceuSenha = requireView().findViewById(R.id.textoEsqueceuSenha);

        textoEsqueceuSenha.setOnClickListener(clique -> irParaEsqueciSenha());
        Motion.pressFeedback(botaoLogin);
        Motion.pressFeedback(botaoGoogle);
        Motion.pressFeedback(botaoFacebook);
    }

    private void configurarFeedbackDeCampos() {
        int verde = ContextCompat.getColor(requireContext(), R.color.verde_escuro_principal);
        campoEmail.setOnFocusChangeListener((v, focused) -> {
            String email = campoEmail.getText().toString().trim();
            if (focused || Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                FieldFeedback.clear(containerEmail, erroEmail, verde);
            } else if (!email.isEmpty()) {
                FieldFeedback.error(containerEmail, erroEmail, "Informe um e-mail válido.", motion);
            }
        });
        campoEmail.addTextChangedListener(limparErroAoDigitar(containerEmail, erroEmail, verde));
        campoSenha.addTextChangedListener(limparErroAoDigitar(containerSenha, erroSenha, verde));
    }

    private TextWatcher limparErroAoDigitar(MaterialCardView card, TextView message, int normalColor) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { FieldFeedback.clear(card, message, normalColor); }
        };
    }

    private void recuperarEmailPreenchido() {
        if (!(requireActivity() instanceof Login)) {
            return;
        }

        String emailSalvo = ((Login) requireActivity()).getEmail();

        if (!emailSalvo.isEmpty()) {
            campoEmail.setText(emailSalvo);
        }
    }

    private void irParaEsqueciSenha() {
        Intent rota = new Intent(requireContext(), EsqueciSenha.class);

        startActivity(rota);
    }

    private void fazerLoginComEmail() {
        String email = campoEmail.getText().toString().trim();
        String senha = campoSenha.getText().toString();

        if (email.isEmpty()) {
            mostrarErroCampo(containerEmail, erroEmail, "Preencha o e-mail.");

            campoEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarErroCampo(containerEmail, erroEmail, "Informe um e-mail válido.");

            campoEmail.requestFocus();
            return;
        }

        if (senha.isEmpty()) {
            mostrarErroCampo(containerSenha, erroSenha, "Preencha a senha.");

            campoSenha.requestFocus();
            return;
        }

        viewModel
                .entrarComEmail(email, senha)
                .observe(
                        getViewLifecycleOwner(),
                        resultado -> {
                            if (!resultado.isSucesso()) {
                                mostrarErroGlobal(resultado.getMensagemErro());
                                return;
                            }

                            autenticarNaApiExterna(email, senha);

                            finalizarLogin();
                        });
    }

    // Best-effort: roda em paralelo e nunca bloqueia nem falha o login pelo
    // Firebase. Contas antigas, sem registro no Postgres, só geram um aviso.
    private void autenticarNaApiExterna(String email, String senha) {

        new AutenticacaoExternaRepository()
                .autenticar(
                        requireContext(),
                        email,
                        senha,
                        sessao -> {
                            if (sessao == null) {
                                Log.w(TAG, "Login na API externa não disponível para " + email);
                            }
                        });
    }

    private void fazerLoginComGoogle() {
        GetGoogleIdOption opcaoGoogle =
                new GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .setAutoSelectEnabled(false)
                        .build();

        GetCredentialRequest solicitacao =
                new GetCredentialRequest.Builder().addCredentialOption(opcaoGoogle).build();

        sinalCancelamentoGoogle = new CancellationSignal();

        gerenciadorCredenciais.getCredentialAsync(
                requireActivity(),
                solicitacao,
                sinalCancelamentoGoogle,
                ContextCompat.getMainExecutor(requireContext()),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(@NonNull GetCredentialResponse resposta) {
                        if (!isAdded()) {
                            return;
                        }
                        tratarCredencialGoogle(resposta.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException erro) {
                        Log.e(TAG, "Erro ao obter credencial Google", erro);

                        if (!isAdded()) {
                            return;
                        }

                        if (erro instanceof GetCredentialCancellationException) {
                            mostrarMensagem("Login com Google cancelado");
                            return;
                        }

                        mostrarMensagem("Não foi possível entrar com o Google");
                    }
                });
    }

    private void tratarCredencialGoogle(@NonNull Credential credencial) {
        if (!(credencial instanceof CustomCredential)) {
            mostrarMensagem("Credencial do Google inválida");
            return;
        }

        CustomCredential credencialPersonalizada = (CustomCredential) credencial;

        if (!TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credencialPersonalizada.getType())) {
            mostrarMensagem("Credencial do Google não reconhecida");
            return;
        }

        try {
            GoogleIdTokenCredential credencialGoogle =
                    GoogleIdTokenCredential.createFrom(credencialPersonalizada.getData());

            viewModel
                    .entrarComGoogle(credencialGoogle.getIdToken())
                    .observe(
                            getViewLifecycleOwner(),
                            resultado -> {
                                if (!resultado.isSucesso()) {
                                    mostrarMensagem(resultado.getMensagemErro());
                                    return;
                                }
                                finalizarLogin();
                            });

        } catch (Exception erro) {
            Log.e(TAG, "Erro ao interpretar token Google", erro);

            mostrarMensagem("Não foi possível validar a conta Google");
        }
    }

    private void configurarRetornoFacebook() {
        if (!FacebookSdk.isInitialized()) {
            Log.w(TAG, "Facebook SDK não inicializado, login por Facebook desabilitado");

            botaoFacebook.setEnabled(false);
            return;
        }

        LoginManager.getInstance()
                .registerCallback(
                        gerenciadorRetornoFacebook,
                        new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(@NonNull LoginResult resultado) {
                                autenticarFacebookNoFirebase(resultado.getAccessToken());
                            }

                            @Override
                            public void onCancel() {
                                mostrarMensagem("Login com Facebook cancelado");
                            }

                            @Override
                            public void onError(@NonNull FacebookException erro) {
                                Log.e(TAG, "Erro no login do Facebook", erro);

                                mostrarMensagem("Não foi possível entrar com o Facebook");
                            }
                        });
    }

    private void fazerLoginComFacebook() {
        LoginManager.getInstance()
                .logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
    }

    private void autenticarFacebookNoFirebase(@NonNull AccessToken tokenFacebook) {
        viewModel
                .entrarComFacebook(tokenFacebook.getToken())
                .observe(
                        getViewLifecycleOwner(),
                        resultado -> {
                            if (!resultado.isSucesso()) {
                                mostrarMensagem(resultado.getMensagemErro());
                                return;
                            }
                            finalizarLogin();
                        });
    }

    private void definirLoginEmAndamento(boolean emAndamento) {
        if (botaoLogin == null) {
            return;
        }

        botaoLogin.setEnabled(!emAndamento);
        botaoGoogle.setEnabled(!emAndamento);
        botaoFacebook.setEnabled(!emAndamento);
        campoEmail.setEnabled(!emAndamento);
        campoSenha.setEnabled(!emAndamento);
        iconeOlhoSenha.setEnabled(!emAndamento);

        indicadorCarregamento.setVisibility(emAndamento ? View.VISIBLE : View.GONE);
        botaoLogin.setText(emAndamento ? "" : getString(R.string.login));
        botaoGoogle.setAlpha(emAndamento ? 0.55f : 1f);
        botaoFacebook.setAlpha(emAndamento ? 0.55f : 1f);

        if (getActivity() instanceof Login) {

            ((Login) getActivity()).definirNavegacaoHabilitada(!emAndamento);
        }
    }

    private void finalizarLogin() {

        if (!isAdded()) {

            return;
        }

        Intent rota = new Intent(requireContext(), MainActivity.class);

        rota.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(rota);

        requireActivity().finish();
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

    private void mostrarErroCampo(MaterialCardView card, TextView message, String text) {
        FieldFeedback.error(card, message, text, motion);
    }

    private void mostrarMensagem(@NonNull String mensagem) {
        mostrarErroGlobal(mensagem);
    }

    private void mostrarErroGlobal(@NonNull String mensagem) {
        if (isAdded()) {
            FieldFeedback.showErrorSnackbar(requireView(), mensagem);
        }
    }

    @Override
    public void onActivityResult(
            int codigoSolicitacao, int codigoResultado, @Nullable Intent dados) {

        super.onActivityResult(codigoSolicitacao, codigoResultado, dados);

        if (gerenciadorRetornoFacebook != null) {

            gerenciadorRetornoFacebook.onActivityResult(codigoSolicitacao, codigoResultado, dados);
        }
    }

    @Override
    public void onDestroyView() {

        if (sinalCancelamentoGoogle != null) {

            sinalCancelamentoGoogle.cancel();

            sinalCancelamentoGoogle = null;
        }

        if (gerenciadorRetornoFacebook != null && FacebookSdk.isInitialized()) {

            LoginManager.getInstance().unregisterCallback(gerenciadorRetornoFacebook);
        }

        if (getActivity() instanceof Login) {

            ((Login) getActivity()).definirNavegacaoHabilitada(true);
        }

        campoEmail = null;

        campoSenha = null;

        botaoLogin = null;

        botaoGoogle = null;

        botaoFacebook = null;

        iconeOlhoSenha = null;
        if (motion != null) motion.cancelAll();
        containerEmail = null;
        containerSenha = null;
        erroEmail = null;
        erroSenha = null;
        indicadorCarregamento = null;
        motion = null;

        super.onDestroyView();
    }
}
