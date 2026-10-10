package com.example.ecociente.views;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecociente.R;
import com.example.ecociente.ui.AvatarPerfil;
import com.example.ecociente.ui.Motion;
import com.example.ecociente.viewmodels.PerfilViewModel;
import com.example.ecociente.viewmodels.SenhaViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;
import java.util.regex.Pattern;
import com.example.ecociente.ui.DialogoEco;

public class GerenciarSenhaActivity extends AppCompatActivity {

    // Mesma regra da ds-esqueceusenha-api.
    private static final Pattern PADRAO_SENHA_VALIDA =
            Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{8,100}$");

    private PerfilViewModel viewModel;
    private SenhaViewModel senhaViewModel;

    private final Motion motion = new Motion();

    private EditText campoSenhaAtual;
    private EditText campoSenhaNova;
    private EditText campoSenhaConfirmar;

    private MaterialButton botaoSalvar;

    private View indicadorCarregamento;

    private boolean senhaAtualVisivel = false;
    private boolean senhaNovaVisivel = false;
    private boolean senhaConfirmarVisivel = false;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        setContentView(R.layout.activity_gerenciar_senha);

        viewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        senhaViewModel = new ViewModelProvider(this).get(SenhaViewModel.class);

        inicializarComponentes();

        senhaViewModel.getCarregando().observe(this, this::definirCarregando);

        findViewById(R.id.botaoVoltarGerenciarSenha).setOnClickListener(view -> finish());

        configurarVisibilidadeSenha();

        carregarFotoPerfil();

        botaoSalvar.setOnClickListener(view -> validarESalvar());

        motion.staggerIn(
                findViewById(R.id.cabecalhoGerenciarSenha), findViewById(R.id.painelGerenciarSenha));
    }

    @Override
    protected void onDestroy() {
        motion.cancelAll();

        super.onDestroy();
    }

    private void carregarFotoPerfil() {

        FirebaseUser usuario = viewModel.usuarioAtual();

        if (usuario == null) {
            return;
        }

        viewModel
                .buscarPerfil(usuario.getUid())
                .observe(
                        this,
                        perfil -> {
                            if (perfil != null) {
                                AvatarPerfil.exibir(
                                        findViewById(R.id.imagemAvatarGerenciarSenha),
                                        perfil.getFotoUrl());
                            }
                        });
    }

    private void inicializarComponentes() {

        campoSenhaAtual = findViewById(R.id.campoSenhaAtual);

        campoSenhaNova = findViewById(R.id.campoSenhaNova);

        campoSenhaConfirmar = findViewById(R.id.campoSenhaConfirmar);

        botaoSalvar = findViewById(R.id.botaoSalvarSenha);

        indicadorCarregamento = findViewById(R.id.indicadorCarregamentoSenha);
    }

    private void configurarVisibilidadeSenha() {

        ImageView iconeOlhoAtual = findViewById(R.id.iconeOlhoSenhaAtual);

        ImageView iconeOlhoNova = findViewById(R.id.iconeOlhoSenhaNova);

        ImageView iconeOlhoConfirmar = findViewById(R.id.iconeOlhoSenhaConfirmar);

        iconeOlhoAtual.setOnClickListener(
                view ->
                        senhaAtualVisivel =
                                alternarVisibilidadeSenha(campoSenhaAtual, iconeOlhoAtual, senhaAtualVisivel));

        iconeOlhoNova.setOnClickListener(
                view ->
                        senhaNovaVisivel =
                                alternarVisibilidadeSenha(campoSenhaNova, iconeOlhoNova, senhaNovaVisivel));

        iconeOlhoConfirmar.setOnClickListener(
                view ->
                        senhaConfirmarVisivel =
                                alternarVisibilidadeSenha(
                                        campoSenhaConfirmar, iconeOlhoConfirmar, senhaConfirmarVisivel));
    }

    private boolean alternarVisibilidadeSenha(
            EditText campo, ImageView icone, boolean visivelAtualmente) {

        if (visivelAtualmente) {

            campo.setTransformationMethod(PasswordTransformationMethod.getInstance());

            icone.setImageResource(R.drawable.ic_olho_fechado);

        } else {

            campo.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

            icone.setImageResource(R.drawable.ic_olho_aberto);
        }

        campo.setSelection(campo.getText().length());

        return !visivelAtualmente;
    }

    private void validarESalvar() {

        String senhaAtual = campoSenhaAtual.getText().toString();

        String senhaNova = campoSenhaNova.getText().toString();

        String senhaConfirmar = campoSenhaConfirmar.getText().toString();

        if (senhaAtual.isEmpty()) {

            mostrarMensagem(getString(R.string.perfil_senha_atual_vazia));

            campoSenhaAtual.requestFocus();

            return;
        }

        if (!PADRAO_SENHA_VALIDA.matcher(senhaNova).matches()) {

            mostrarMensagem(getString(R.string.perfil_senha_nova_curta));

            campoSenhaNova.requestFocus();

            return;
        }

        if (!senhaNova.equals(senhaConfirmar)) {

            mostrarMensagem(getString(R.string.perfil_senhas_diferentes));

            campoSenhaConfirmar.requestFocus();

            return;
        }

        new DialogoEco.Builder(this)
                .titulo(R.string.salvar_alteracoes_titulo)
                .mensagem(R.string.salvar_alteracoes_mensagem)
                .botao(
                        R.string.confirmar,
                        DialogoEco.Estilo.PREENCHIDO,
                        () -> alterarSenha(senhaAtual, senhaNova))
                .botao(R.string.perfil_cancelar, DialogoEco.Estilo.CONTORNO, null)
                .mostrar();
    }

    private void alterarSenha(String senhaAtual, String senhaNova) {

        senhaViewModel
                .alterarSenha(getApplicationContext(), senhaAtual, senhaNova)
                .observe(
                        this,
                        resultado -> {
                            if (resultado == null) {
                                return;
                            }

                            if (!resultado.isSucesso()) {

                                mostrarMensagem(resultado.getMensagemErro());

                                return;
                            }

                            mostrarMensagem(getString(R.string.perfil_senha_sucesso));

                            finish();
                        });
    }

    private void definirCarregando(boolean carregando) {

        botaoSalvar.setEnabled(!carregando);

        campoSenhaAtual.setEnabled(!carregando);

        campoSenhaNova.setEnabled(!carregando);

        campoSenhaConfirmar.setEnabled(!carregando);

        indicadorCarregamento.setVisibility(carregando ? View.VISIBLE : View.GONE);

        botaoSalvar.setText(carregando ? "" : getString(R.string.perfil_salvar));
    }

    private void mostrarMensagem(String mensagem) {

        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }
}
