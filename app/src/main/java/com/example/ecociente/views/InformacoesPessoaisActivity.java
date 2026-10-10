package com.example.ecociente.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecociente.R;
import com.example.ecociente.model.PerfilAcesso;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.repository.PerfilRepository;
import com.example.ecociente.ui.AvatarPerfil;
import com.example.ecociente.ui.Motion;
import com.example.ecociente.viewmodels.PerfilViewModel;
import com.google.firebase.auth.FirebaseUser;
import com.example.ecociente.ui.DialogoEco;

public class InformacoesPessoaisActivity extends AppCompatActivity {

    private PerfilViewModel viewModel;

    private final Motion motion = new Motion();

    private String uid;

    private boolean cooperativa;

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

        motion.staggerIn(
                findViewById(R.id.cabecalhoInformacoesPessoais),
                findViewById(R.id.tituloInformacoesPessoais),
                findViewById(R.id.containerCamposPessoais));
    }

    @Override
    protected void onDestroy() {
        motion.cancelAll();

        super.onDestroy();
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

        configurarLinha(linhaEmail, R.drawable.ic_email, R.string.perfil_rotulo_email);

        configurarLinha(linhaTelefone, R.drawable.ic_telefone_perfil, R.string.perfil_rotulo_telefone);

        configurarLinha(linhaEndereco, R.drawable.ic_endereco_perfil, R.string.perfil_rotulo_endereco);

        configurarLinha(linhaCpf, R.drawable.ic_cpf_perfil, R.string.perfil_rotulo_cpf);

        // Trocar o e-mail é o identificador de login no Firebase Auth e exige um
        // fluxo próprio (reautenticação + verificação) - fora do escopo por ora.
        // Na cooperativa o e-mail da linha é o de contato (emailCooperativa), esse pode ser editado.
        linhaEmail.setClickable(false);

        linhaNome.setOnClickListener(
                view ->
                        abrirEdicao(
                                cooperativa
                                        ? PerfilRepository.CAMPO_NOME_COOPERATIVA
                                        : PerfilRepository.CAMPO_NOME,
                                cooperativa ? R.string.nome_cooperativa : R.string.perfil_rotulo_nome,
                                linhaNome));

        linhaEmail.setOnClickListener(
                view -> {
                    if (cooperativa) {
                        abrirEdicao(
                                PerfilRepository.CAMPO_EMAIL_COOPERATIVA,
                                R.string.perfil_rotulo_email_cooperativa,
                                linhaEmail);
                    }
                });

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
                view -> {
                    if (!cooperativa) {
                        abrirEdicao(PerfilRepository.CAMPO_CPF, R.string.perfil_rotulo_cpf, linhaCpf);
                    }
                });
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

        cooperativa = perfil != null && PerfilAcesso.ehCooperativa(perfil.getTipoPerfil());

        String emailLogin = PerfilUsuario.emailExibicao(perfil, usuario);

        if (cooperativa) {
            preencherCooperativa(perfil, emailLogin);

        } else {
            String nome = PerfilUsuario.nomeExibicao(perfil, usuario);

            textoNomeCabecalho.setText(nome);

            textoEmailCabecalho.setText(emailLogin);

            definirValor(linhaNome, nome);

            definirValor(linhaEmail, emailLogin);

            definirValor(linhaCpf, perfil != null ? perfil.getCpf() : "");
        }

        definirValor(linhaTelefone, perfil != null ? perfil.getTelefone() : "");

        definirValor(linhaEndereco, perfil != null ? perfil.getEndereco() : "");

        if (cooperativa) {
            montarInformacoesBasicas(perfil);
        }

        if (perfil != null) {
            AvatarPerfil.exibir(
                    findViewById(R.id.imagemAvatarInformacoesPessoais), perfil.getFotoUrl());
        }
    }

    private void preencherCooperativa(PerfilUsuario perfil, String emailLogin) {

        configurarLinha(linhaNome, R.drawable.ic_predio, R.string.nome_cooperativa);

        configurarLinha(linhaEmail, R.drawable.ic_email, R.string.perfil_rotulo_email_cooperativa);

        configurarLinha(linhaCpf, R.drawable.ic_cpf_perfil, R.string.cnpj);

        linhaEmail.setClickable(true);

        linhaCpf.setClickable(false);

        textoNomeCabecalho.setText(perfil.getNomeCooperativa());

        textoEmailCabecalho.setText(emailLogin);

        definirValor(linhaNome, perfil.getNomeCooperativa());

        definirValor(
                linhaEmail,
                perfil.getEmailCooperativa().isEmpty() ? emailLogin : perfil.getEmailCooperativa());

        definirValor(linhaCpf, formatarCnpj(perfil.getCnpj()));
    }

    private void montarInformacoesBasicas(PerfilUsuario perfil) {

        LinearLayout linhas = findViewById(R.id.linhasInformacoesBasicas);

        linhas.removeAllViews();

        adicionarLinhaBasica(linhas, PerfilRepository.CAMPO_NUMERO, R.string.rotulo_numero, perfil.getNumero(), true);
        adicionarLinhaBasica(
                linhas, PerfilRepository.CAMPO_COMPLEMENTO, R.string.rotulo_complemento, perfil.getComplemento(), true);
        adicionarLinhaBasica(linhas, PerfilRepository.CAMPO_CIDADE, R.string.rotulo_cidade, perfil.getCidade(), true);
        adicionarLinhaBasica(linhas, PerfilRepository.CAMPO_ESTADO, R.string.rotulo_estado, perfil.getEstado(), true);
        adicionarLinhaBasica(linhas, PerfilRepository.CAMPO_CEP, R.string.rotulo_cep, perfil.getCep(), false);

        findViewById(R.id.tituloInformacoesBasicas).setVisibility(View.VISIBLE);
        findViewById(R.id.containerInformacoesBasicas).setVisibility(View.VISIBLE);
    }

    private void adicionarLinhaBasica(
            LinearLayout linhas, String campo, int rotulo, String valor, boolean comDivisor) {

        LayoutInflater inflador = LayoutInflater.from(this);

        View linha = inflador.inflate(R.layout.item_campo_pessoal, linhas, false);

        configurarLinha(linha, R.drawable.ic_endereco_perfil, rotulo);

        definirValor(linha, valor);

        linha.setOnClickListener(view -> abrirEdicao(campo, rotulo, linha));

        linhas.addView(linha);

        if (comDivisor) {
            linhas.addView(inflador.inflate(R.layout.divisor_campo_pessoal, linhas, false));
        }
    }

    private String formatarCnpj(String valor) {

        String digitos = valor.replaceAll("\\D", "");

        if (digitos.length() != 14) {
            return valor;
        }

        return digitos.substring(0, 2)
                + "."
                + digitos.substring(2, 5)
                + "."
                + digitos.substring(5, 8)
                + "/"
                + digitos.substring(8, 12)
                + "-"
                + digitos.substring(12);
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

        new DialogoEco.Builder(this)
                .titulo(rotulo)
                .conteudo(corpo)
                .botao(
                        R.string.perfil_salvar,
                        DialogoEco.Estilo.PREENCHIDO,
                        () -> salvarCampo(campo, campoValor.getText().toString().trim(), linha))
                .botao(R.string.perfil_cancelar, DialogoEco.Estilo.CONTORNO, null)
                .mostrar();
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

                            if (campo.equals(PerfilRepository.CAMPO_NOME)
                                    || campo.equals(PerfilRepository.CAMPO_NOME_COOPERATIVA)) {
                                textoNomeCabecalho.setText(novoValor);
                            }
                        });
    }
}
