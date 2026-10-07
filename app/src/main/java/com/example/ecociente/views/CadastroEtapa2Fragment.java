package com.example.ecociente.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecociente.R;
import com.example.ecociente.model.ResultadoCadastro;
import com.example.ecociente.model.TipoDocumento;
import com.example.ecociente.ui.Dimensoes;
import com.example.ecociente.ui.FieldFeedback;
import com.example.ecociente.ui.MascaraTexto;
import com.example.ecociente.ui.Motion;
import com.example.ecociente.viewmodels.CadastroViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.Arrays;
import java.util.List;

public class CadastroEtapa2Fragment extends Fragment {

    private static final String MASCARA_CEP = "#####-###";
    private static final String MASCARA_TELEFONE = "(##) #####-####";

    private EditText campoEndereco;
    private EditText campoNumero;
    private EditText campoCep;
    private EditText campoComplemento;
    private EditText campoCidade;
    private EditText campoTelefone;
    private EditText campoCpf;
    private EditText campoNomeCooperativa;
    private EditText campoEmailCooperativa;

    private AutoCompleteTextView campoEstado;

    private ImageView botaoVoltarEtapa1;
    private ImageView iconeSetaEstado;

    private MaterialButton botaoCadastrar;

    private MaterialCardView containerEndereco;
    private MaterialCardView containerNumero;
    private MaterialCardView containerCep;
    private MaterialCardView containerCidade;
    private MaterialCardView containerEstado;
    private MaterialCardView containerNomeCooperativa;
    private MaterialCardView containerEmailCooperativa;

    private ProgressBar indicadorCarregamento;
    private Motion motion;

    private CadastroViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle estadoSalvo) {

        return inflater.inflate(R.layout.fragment_cadastro_etapa2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estadoSalvo) {

        super.onViewCreated(view, estadoSalvo);

        inicializarComponentes(view);

        configurarModoCooperativa();

        viewModel = new ViewModelProvider(requireActivity()).get(CadastroViewModel.class);

        viewModel.getCarregando().observe(getViewLifecycleOwner(), this::definirCarregando);

        configurarBotaoVoltar();

        configurarMascaras();

        configurarListaEstados();

        configurarFeedbackDeCampos();

        configurarCadastro();

        recuperarDadosPreenchidos();

        motion.staggerIn(
                view.findViewById(R.id.tituloCadastroEtapa2),
                view.findViewById(R.id.containerEtapasEtapa2),
                containerEndereco,
                view.findViewById(R.id.linhaNumeroCep),
                view.findViewById(R.id.containerComplemento),
                containerCidade,
                containerEstado,
                botaoCadastrar
        );
    }

    private void inicializarComponentes(View view) {

        campoEndereco = view.findViewById(R.id.campoEndereco);

        campoNumero = view.findViewById(R.id.campoNumero);

        campoCep = view.findViewById(R.id.campoCep);

        campoComplemento = view.findViewById(R.id.campoComplemento);

        campoCidade = view.findViewById(R.id.campoCidade);

        campoEstado = view.findViewById(R.id.campoEstado);

        campoTelefone = view.findViewById(R.id.campoTelefone);

        campoCpf = view.findViewById(R.id.campoCpf);

        campoNomeCooperativa = view.findViewById(R.id.campoNomeCooperativa);

        campoEmailCooperativa = view.findViewById(R.id.campoEmailCooperativa);

        botaoVoltarEtapa1 = view.findViewById(R.id.botaoVoltarEtapa1);

        iconeSetaEstado = view.findViewById(R.id.iconeSetaEstado);

        botaoCadastrar = view.findViewById(R.id.botaoCadastrar);

        containerEndereco = view.findViewById(R.id.containerEndereco);
        containerNumero = view.findViewById(R.id.containerNumero);
        containerCep = view.findViewById(R.id.containerCep);
        containerCidade = view.findViewById(R.id.containerCidade);
        containerEstado = view.findViewById(R.id.containerEstado);
        containerNomeCooperativa = view.findViewById(R.id.containerNomeCooperativa);
        containerEmailCooperativa = view.findViewById(R.id.containerEmailCooperativa);

        indicadorCarregamento = view.findViewById(R.id.indicadorCarregamentoCadastro);
        motion = new Motion();
    }

    private void configurarBotaoVoltar() {

        botaoVoltarEtapa1.setOnClickListener(
                view -> {
                    salvarDadosAtuais();

                    ((Login) requireActivity()).voltarCadastroEtapa1();
                });
    }

    private void configurarMascaras() {

        campoCep.addTextChangedListener(new MascaraTexto(campoCep, MASCARA_CEP));

        campoTelefone.addTextChangedListener(new MascaraTexto(campoTelefone, MASCARA_TELEFONE));

        campoCpf.addTextChangedListener(new MascaraTexto(campoCpf, tipoDocumento().getMascara()));
    }

    private TipoDocumento tipoDocumento() {

        return cadastroDeCooperativa() ? TipoDocumento.CNPJ : TipoDocumento.CPF;
    }

    private boolean cadastroDeCooperativa() {

        return ((Login) requireActivity()).isCooperativa();
    }

    private void configurarModoCooperativa() {

        if (!cadastroDeCooperativa()) {
            return;
        }

        containerNomeCooperativa.setVisibility(View.VISIBLE);

        containerEmailCooperativa.setVisibility(View.VISIBLE);

        campoCpf.setHint(R.string.cnpj);

        ConstraintLayout.LayoutParams parametros =
                (ConstraintLayout.LayoutParams) containerEndereco.getLayoutParams();

        parametros.topMargin = Dimensoes.dpParaPx(requireContext(), 14);

        containerEndereco.setLayoutParams(parametros);
    }

    private void configurarListaEstados() {

        String[] estados = getResources().getStringArray(R.array.estados_brasil);

        ArrayAdapter<String> adaptador =
                new ArrayAdapter<>(
                        requireContext(),
                        R.layout.item_estado_dropdown,
                        android.R.id.text1,
                        estados);

        campoEstado.setAdapter(adaptador);

        campoEstado.setThreshold(0);

        campoEstado.setDropDownBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.fundo_dropdown_estados));

        campoEstado.setDropDownVerticalOffset(Dimensoes.dpParaPx(requireContext(), 6));

        campoEstado.setOnClickListener(view -> campoEstado.showDropDown());

        iconeSetaEstado.setOnClickListener(
                view -> {
                    campoEstado.requestFocus();

                    campoEstado.showDropDown();
                });
    }

    private void configurarFeedbackDeCampos() {
        int verde = ContextCompat.getColor(requireContext(), R.color.verde_escuro_principal);

        campoEndereco.addTextChangedListener(limparErroAoDigitar(containerEndereco, verde));
        campoNumero.addTextChangedListener(limparErroAoDigitar(containerNumero, verde));
        campoCep.addTextChangedListener(limparErroAoDigitar(containerCep, verde));
        campoCidade.addTextChangedListener(limparErroAoDigitar(containerCidade, verde));
        campoEstado.addTextChangedListener(limparErroAoDigitar(containerEstado, verde));
        campoNomeCooperativa.addTextChangedListener(limparErroAoDigitar(containerNomeCooperativa, verde));
        campoEmailCooperativa.addTextChangedListener(limparErroAoDigitar(containerEmailCooperativa, verde));
    }

    private TextWatcher limparErroAoDigitar(MaterialCardView card, int normalColor) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            @Override public void onTextChanged(CharSequence s, int st, int before, int count) { }
            @Override public void afterTextChanged(Editable s) {
                FieldFeedback.clear(card, normalColor);
            }
        };
    }

    private void configurarCadastro() {

        botaoCadastrar.setOnClickListener(view -> validarEtapa2());
        Motion.pressFeedback(botaoCadastrar);
        Motion.pressFeedback(botaoVoltarEtapa1);
    }

    private void validarEtapa2() {

        String endereco = campoEndereco.getText().toString().trim();

        String numero = campoNumero.getText().toString().trim();

        String cep = campoCep.getText().toString().trim();

        String complemento = campoComplemento.getText().toString().trim();

        String cidade = campoCidade.getText().toString().trim();

        String estado = campoEstado.getText().toString().trim();

        String telefone = campoTelefone.getText().toString().trim();

        String cpf = campoCpf.getText().toString().trim();

        String nomeCooperativa = campoNomeCooperativa.getText().toString().trim();

        String emailCooperativa = campoEmailCooperativa.getText().toString().trim();

        if (cadastroDeCooperativa()) {

            if (nomeCooperativa.isEmpty()) {
                mostrarErroCampo(
                        containerNomeCooperativa, campoNomeCooperativa, "Digite o nome da cooperativa.");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(emailCooperativa).matches()) {
                mostrarErroCampo(
                        containerEmailCooperativa,
                        campoEmailCooperativa,
                        "Digite um e-mail válido para a cooperativa.");
                return;
            }
        }

        if (endereco.isEmpty()) {
            mostrarErroCampo(containerEndereco, campoEndereco, "Digite seu endereço.");
            return;
        }

        if (numero.isEmpty()) {
            mostrarErroCampo(containerNumero, campoNumero, "Digite o número do endereço.");
            return;
        }

        if (cep.length() != 9) {
            mostrarErroCampo(containerCep, campoCep, "Digite um CEP válido.");
            return;
        }

        if (cidade.isEmpty()) {
            mostrarErroCampo(containerCidade, campoCidade, "Digite sua cidade.");
            return;
        }

        if (estado.isEmpty() || !estadoValido(estado)) {
            FieldFeedback.error(containerEstado, "Selecione um estado.", motion);
            FieldFeedback.showErrorSnackbar(requireView(), "Selecione um estado.");
            campoEstado.requestFocus();
            campoEstado.showDropDown();
            return;
        }

        if (TipoDocumento.apenasDigitos(telefone).length() < 10) {

            mostrarMensagem("Digite um telefone válido.");

            campoTelefone.requestFocus();

            return;
        }

        if (!tipoDocumento().valido(cpf)) {

            mostrarMensagem(cadastroDeCooperativa() ? "Digite um CNPJ válido." : "Digite um CPF válido.");

            campoCpf.requestFocus();

            return;
        }

        Login telaAutenticacao = (Login) requireActivity();

        telaAutenticacao.salvarDadosEtapa2(endereco, numero, cep, complemento, cidade, estado);

        if (cadastroDeCooperativa()) {

            telaAutenticacao.salvarDadosContato(telefone, "");

            telaAutenticacao.salvarDadosCooperativa(
                    nomeCooperativa, TipoDocumento.apenasDigitos(cpf), emailCooperativa);

        } else {

            telaAutenticacao.salvarDadosContato(telefone, cpf);
        }

        cadastrarUsuarioFirebase();
    }

    private boolean estadoValido(String estadoInformado) {

        List<String> estados = Arrays.asList(getResources().getStringArray(R.array.estados_brasil));

        return estados.contains(estadoInformado);
    }

    private void cadastrarUsuarioFirebase() {

        Login telaAutenticacao = (Login) requireActivity();

        String email = telaAutenticacao.getEmail();

        String senha = telaAutenticacao.getSenha();

        if (senha.isEmpty()) {

            mostrarMensagem("A senha não foi encontrada. Volte à primeira etapa.");

            return;
        }

        viewModel
                .cadastrar(
                        telaAutenticacao.getNome(), email, senha, telaAutenticacao.montarDadosCadastro())
                .observe(getViewLifecycleOwner(), this::tratarResultadoCadastro);
    }

    private void tratarResultadoCadastro(ResultadoCadastro resultado) {

        if (!isAdded()) {
            return;
        }

        if (!resultado.isSucesso()) {

            mostrarMensagem(resultado.getMensagemErro());

            return;
        }

        ((Login) requireActivity()).autenticarNaApiExterna();

        entrarNoEcoCiente();
    }

    private void definirCarregando(boolean carregando) {

        botaoCadastrar.setEnabled(!carregando);

        botaoVoltarEtapa1.setEnabled(!carregando);

        campoEndereco.setEnabled(!carregando);

        campoNumero.setEnabled(!carregando);

        campoCep.setEnabled(!carregando);

        campoComplemento.setEnabled(!carregando);

        campoCidade.setEnabled(!carregando);

        campoEstado.setEnabled(!carregando);

        campoTelefone.setEnabled(!carregando);

        campoCpf.setEnabled(!carregando);

        iconeSetaEstado.setEnabled(!carregando);

        if (carregando) {

            indicadorCarregamento.setVisibility(View.VISIBLE);

            botaoCadastrar.setText("");

        } else {

            indicadorCarregamento.setVisibility(View.GONE);

            botaoCadastrar.setText(R.string.cadastrar_se);
        }

        if (getActivity() instanceof Login) {

            ((Login) getActivity()).definirNavegacaoHabilitada(!carregando);
        }
    }

    private void salvarDadosAtuais() {

        if (!(getActivity() instanceof Login)
                || campoEndereco == null
                || campoNumero == null
                || campoCep == null
                || campoComplemento == null
                || campoCidade == null
                || campoEstado == null
                || campoTelefone == null
                || campoCpf == null
                || campoNomeCooperativa == null
                || campoEmailCooperativa == null) {

            return;
        }

        ((Login) getActivity())
                .salvarDadosEtapa2(
                        campoEndereco.getText().toString().trim(),
                        campoNumero.getText().toString().trim(),
                        campoCep.getText().toString().trim(),
                        campoComplemento.getText().toString().trim(),
                        campoCidade.getText().toString().trim(),
                        campoEstado.getText().toString().trim());

        Login telaAutenticacao = (Login) getActivity();

        if (telaAutenticacao.isCooperativa()) {

            telaAutenticacao.salvarDadosContato(campoTelefone.getText().toString().trim(), "");

            telaAutenticacao.salvarDadosCooperativa(
                    campoNomeCooperativa.getText().toString().trim(),
                    TipoDocumento.apenasDigitos(campoCpf.getText().toString()),
                    campoEmailCooperativa.getText().toString().trim());

            return;
        }

        telaAutenticacao.salvarDadosContato(
                campoTelefone.getText().toString().trim(), campoCpf.getText().toString().trim());
    }

    private void recuperarDadosPreenchidos() {

        Login telaAutenticacao = (Login) requireActivity();

        campoEndereco.setText(telaAutenticacao.getEndereco());

        campoNumero.setText(telaAutenticacao.getNumero());

        campoCep.setText(telaAutenticacao.getCep());

        campoComplemento.setText(telaAutenticacao.getComplemento());

        campoCidade.setText(telaAutenticacao.getCidade());

        campoEstado.setText(telaAutenticacao.getEstado(), false);

        campoTelefone.setText(telaAutenticacao.getTelefone());

        if (telaAutenticacao.isCooperativa()) {

            campoCpf.setText(telaAutenticacao.getCnpj());

            campoNomeCooperativa.setText(telaAutenticacao.getNomeCooperativa());

            campoEmailCooperativa.setText(telaAutenticacao.getEmailCooperativa());

            return;
        }

        campoCpf.setText(telaAutenticacao.getCpf());
    }

    private void entrarNoEcoCiente() {

        if (!isAdded()) {
            return;
        }

        Intent rota = new Intent(requireContext(), MainActivity.class);

        rota.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(rota);

        requireActivity().finish();
    }

    private void mostrarErroCampo(
            MaterialCardView card,
            EditText campo,
            String mensagem
    ) {
        FieldFeedback.error(card, mensagem, motion);
        FieldFeedback.showErrorSnackbar(requireView(), mensagem);
        campo.requestFocus();
    }

    private void mostrarMensagem(String mensagem) {

        if (!isAdded()) {
            return;
        }

        FieldFeedback.showErrorSnackbar(requireView(), mensagem);
    }

    @Override
    public void onDestroyView() {

        salvarDadosAtuais();

        if (getActivity() instanceof Login) {

            ((Login) getActivity()).definirNavegacaoHabilitada(true);
        }

        campoEndereco = null;

        campoNumero = null;

        campoCep = null;

        campoComplemento = null;

        campoCidade = null;

        campoEstado = null;

        campoTelefone = null;

        campoCpf = null;

        campoNomeCooperativa = null;

        campoEmailCooperativa = null;

        botaoVoltarEtapa1 = null;

        iconeSetaEstado = null;

        botaoCadastrar = null;

        containerEndereco = null;
        containerNumero = null;
        containerCep = null;
        containerCidade = null;
        containerEstado = null;

        indicadorCarregamento = null;

        if (motion != null) {
            motion.cancelAll();
            motion = null;
        }

        super.onDestroyView();
    }
}
