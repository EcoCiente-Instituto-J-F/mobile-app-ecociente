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
import com.example.ecociente.viewmodels.PerfilViewModel;
import com.google.android.material.button.MaterialButton;

public class GerenciarSenhaActivity extends AppCompatActivity {

    private PerfilViewModel viewModel;

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

        inicializarComponentes();

        viewModel.getCarregando().observe(this, this::definirCarregando);

        findViewById(R.id.botaoVoltarGerenciarSenha).setOnClickListener(view -> finish());

        configurarVisibilidadeSenha();

        botaoSalvar.setOnClickListener(view -> validarESalvar());
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

            icone.setImageResource(R.drawable.icon_olho_fechado);

        } else {

            campo.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

            icone.setImageResource(R.drawable.icon_olho_aberto);
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

        if (senhaNova.length() < 6) {

            mostrarMensagem(getString(R.string.perfil_senha_nova_curta));

            campoSenhaNova.requestFocus();

            return;
        }

        if (!senhaNova.equals(senhaConfirmar)) {

            mostrarMensagem(getString(R.string.perfil_senhas_diferentes));

            campoSenhaConfirmar.requestFocus();

            return;
        }

        viewModel
                .alterarSenha(senhaAtual, senhaNova)
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
