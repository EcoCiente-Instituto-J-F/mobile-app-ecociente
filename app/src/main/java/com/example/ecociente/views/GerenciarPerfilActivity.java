package com.example.ecociente.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecociente.R;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.viewmodels.PerfilViewModel;
import com.google.firebase.auth.FirebaseUser;

public class GerenciarPerfilActivity extends AppCompatActivity {

    private PerfilViewModel viewModel;

    private TextView textoNome;
    private TextView textoEmail;
    private TextView textoIniciaisAvatar;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        setContentView(R.layout.activity_gerenciar_perfil);

        viewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        inicializarComponentes();

        configurarCliques();

        carregarPerfil();
    }

    private void inicializarComponentes() {

        textoNome = findViewById(R.id.textoNomePerfil);

        textoEmail = findViewById(R.id.textoEmailPerfil);

        textoIniciaisAvatar = findViewById(R.id.textoIniciaisAvatarPerfil);
    }

    private void configurarCliques() {

        findViewById(R.id.linhaInformacoesPessoais)
                .setOnClickListener(
                        view ->
                                startActivity(
                                        new Intent(this, InformacoesPessoaisActivity.class)));

        findViewById(R.id.linhaGerenciarSenha)
                .setOnClickListener(
                        view -> startActivity(new Intent(this, GerenciarSenhaActivity.class)));

        findViewById(R.id.linhaNotificacoes)
                .setOnClickListener(
                        view -> startActivity(new Intent(this, NotificacoesActivity.class)));

        findViewById(R.id.linhaSairConta).setOnClickListener(view -> confirmarSaida());

        findViewById(R.id.linhaExcluirConta).setOnClickListener(view -> pedirSenhaParaExcluir());
    }

    private void carregarPerfil() {

        FirebaseUser usuario = viewModel.usuarioAtual();

        if (usuario == null) {
            return;
        }

        viewModel
                .buscarPerfil(usuario.getUid())
                .observe(this, perfil -> preencherCabecalho(usuario, perfil));
    }

    private void preencherCabecalho(FirebaseUser usuario, PerfilUsuario perfil) {

        String nomeExibido = PerfilUsuario.nomeExibicao(perfil, usuario);

        textoNome.setText(nomeExibido);

        textoEmail.setText(PerfilUsuario.emailExibicao(perfil, usuario));

        textoIniciaisAvatar.setText(obterInicial(nomeExibido));
    }

    private String obterInicial(String nome) {

        if (nome == null || nome.trim().isEmpty()) {
            return "?";
        }

        return nome.trim().substring(0, 1).toUpperCase();
    }

    private void confirmarSaida() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.perfil_sair_titulo)
                .setMessage(R.string.perfil_sair_mensagem)
                .setPositiveButton(
                        R.string.perfil_sair_confirmar,
                        (dialogo, botao) -> {
                            viewModel.encerrarSessao();

                            voltarParaLogin();
                        })
                .setNegativeButton(R.string.perfil_cancelar, null)
                .show();
    }

    private void pedirSenhaParaExcluir() {

        View corpo = LayoutInflater.from(this).inflate(R.layout.dialog_confirmar_senha, null);

        EditText campoSenha = corpo.findViewById(R.id.campoConfirmarSenhaExclusao);

        new AlertDialog.Builder(this)
                .setTitle(R.string.perfil_excluir_titulo)
                .setView(corpo)
                .setPositiveButton(
                        R.string.perfil_excluir_confirmar,
                        (dialogo, botao) -> {
                            String senha = campoSenha.getText().toString();

                            if (senha.isEmpty()) {
                                Toast.makeText(this, R.string.perfil_excluir_senha_vazia, Toast.LENGTH_SHORT)
                                        .show();
                                return;
                            }

                            excluirConta(senha);
                        })
                .setNegativeButton(R.string.perfil_cancelar, null)
                .show();
    }

    private void excluirConta(String senha) {

        viewModel
                .excluirConta(senha)
                .observe(
                        this,
                        resultado -> {
                            if (resultado == null) {
                                return;
                            }

                            if (!resultado.isSucesso()) {
                                Toast.makeText(this, resultado.getMensagemErro(), Toast.LENGTH_LONG)
                                        .show();
                                return;
                            }

                            Toast.makeText(this, R.string.perfil_excluir_sucesso, Toast.LENGTH_LONG).show();

                            voltarParaLogin();
                        });
    }

    private void voltarParaLogin() {

        Intent rota = new Intent(this, Login.class);

        rota.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(rota);

        finish();
    }
}
