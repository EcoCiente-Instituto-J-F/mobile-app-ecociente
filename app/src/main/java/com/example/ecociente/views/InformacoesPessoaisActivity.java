package com.example.ecociente.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecociente.R;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.repository.PerfilRepository;
import com.example.ecociente.viewmodels.PerfilViewModel;
import com.google.firebase.auth.FirebaseUser;

public class InformacoesPessoaisActivity extends AppCompatActivity {

    private PerfilViewModel viewModel;

    private String uid;

    private TextView textoNomeCabecalho;
    private TextView textoEmailCabecalho;

    private View linhaNome;
    private View linhaEmail;
    private View linhaTelefone;
    private View linhaEndereco;
    private View linhaCpf;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        setContentView(R.layout.activity_informacoes_pessoais);

        viewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        inicializarComponentes();

        findViewById(R.id.botaoVoltarInformacoesPessoais).setOnClickListener(view -> finish());

        carregarPerfil();
    }

    private void inicializarComponentes() {

        textoNomeCabecalho = findViewById(R.id.textoNomeInformacoesPessoais);

        textoEmailCabecalho = findViewById(R.id.textoEmailInformacoesPessoais);

        linhaNome = findViewById(R.id.linhaCampoNome);

        linhaEmail = findViewById(R.id.linhaCampoEmail);

        linhaTelefone = findViewById(R.id.linhaCampoTelefone);

        linhaEndereco = findViewById(R.id.linhaCampoEndereco);

        linhaCpf = findViewById(R.id.linhaCampoCpf);

        configurarLinha(linhaNome, R.drawable.ic_perfil_usuario, R.string.perfil_rotulo_nome);

        configurarLinha(linhaEmail, R.drawable.icon_email, R.string.perfil_rotulo_email);

        configurarLinha(linhaTelefone, R.drawable.ic_telefone_perfil, R.string.perfil_rotulo_telefone);

        configurarLinha(linhaEndereco, R.drawable.ic_endereco_perfil, R.string.perfil_rotulo_endereco);

        configurarLinha(linhaCpf, R.drawable.ic_cpf_perfil, R.string.perfil_rotulo_cpf);

        // Trocar o e-mail é o identificador de login no Firebase Auth e exige um
        // fluxo próprio (reautenticação + verificação) - fora do escopo por ora.
        linhaEmail.setClickable(false);

        linhaNome.setOnClickListener(
                view -> abrirEdicao(PerfilRepository.CAMPO_NOME, R.string.perfil_rotulo_nome, linhaNome));

        linhaTelefone.setOnClickListener(
                view ->
                        abrirEdicao(
                                PerfilRepository.CAMPO_TELEFONE,
                                R.string.perfil_rotulo_telefone,
                                linhaTelefone));

        linhaEndereco.setOnClickListener(
                view ->
                        abrirEdicao(
                                PerfilRepository.CAMPO_ENDERECO,
                                R.string.perfil_rotulo_endereco,
                                linhaEndereco));

        linhaCpf.setOnClickListener(
                view -> abrirEdicao(PerfilRepository.CAMPO_CPF, R.string.perfil_rotulo_cpf, linhaCpf));
    }

    private void configurarLinha(View linha, int icone, int rotulo) {

        ((ImageView) linha.findViewById(R.id.iconeCampoPessoal)).setImageResource(icone);

        ((TextView) linha.findViewById(R.id.rotuloCampoPessoal)).setText(rotulo);
    }

    private void carregarPerfil() {

        FirebaseUser usuario = viewModel.usuarioAtual();

        if (usuario == null) {
            return;
        }

        uid = usuario.getUid();

        viewModel.buscarPerfil(uid).observe(this, perfil -> preencherCampos(usuario, perfil));
    }

    private void preencherCampos(FirebaseUser usuario, PerfilUsuario perfil) {

        String nome = PerfilUsuario.nomeExibicao(perfil, usuario);

        String email = PerfilUsuario.emailExibicao(perfil, usuario);

        textoNomeCabecalho.setText(nome);

        textoEmailCabecalho.setText(email);

        definirValor(linhaNome, nome);

        definirValor(linhaEmail, email);

        definirValor(linhaTelefone, perfil != null ? perfil.getTelefone() : "");

        definirValor(linhaEndereco, perfil != null ? perfil.getEndereco() : "");

        definirValor(linhaCpf, perfil != null ? perfil.getCpf() : "");
    }

    private void definirValor(View linha, String valor) {

        TextView textoValor = linha.findViewById(R.id.valorCampoPessoal);

        textoValor.setText(
                valor == null || valor.isEmpty()
                        ? getString(R.string.perfil_valor_nao_informado)
                        : valor);
    }

    private void abrirEdicao(String campo, int rotulo, View linha) {

        if (uid == null) {
            return;
        }

        TextView textoValor = linha.findViewById(R.id.valorCampoPessoal);

        String valorAtual = textoValor.getText().toString();

        View corpo = LayoutInflater.from(this).inflate(R.layout.dialog_editar_campo, null);

        EditText campoValor = corpo.findViewById(R.id.campoEditarValor);

        campoValor.setText(valorAtual);

        campoValor.setSelection(campoValor.getText().length());

        new AlertDialog.Builder(this)
                .setTitle(rotulo)
                .setView(corpo)
                .setPositiveButton(
                        R.string.perfil_salvar,
                        (dialogo, botao) ->
                                salvarCampo(campo, campoValor.getText().toString().trim(), linha))
                .setNegativeButton(R.string.perfil_cancelar, null)
                .show();
    }

    private void salvarCampo(String campo, String novoValor, View linha) {

        viewModel
                .atualizarCampo(uid, campo, novoValor)
                .observe(
                        this,
                        resultado -> {
                            if (resultado == null) {
                                return;
                            }

                            if (!resultado.isSucesso()) {
                                Toast.makeText(this, resultado.getMensagemErro(), Toast.LENGTH_SHORT)
                                        .show();
                                return;
                            }

                            // Atualiza só a linha editada, sem recarregar o perfil
                            // inteiro do Firestore de novo.
                            definirValor(linha, novoValor);

                            if (campo.equals(PerfilRepository.CAMPO_NOME)) {
                                textoNomeCabecalho.setText(novoValor);
                            }
                        });
    }
}
