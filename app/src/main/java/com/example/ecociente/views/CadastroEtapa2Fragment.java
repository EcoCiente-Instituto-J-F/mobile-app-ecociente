package com.example.ecociente.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecociente.R;
import com.example.ecociente.model.ResultadoCadastro;
import com.example.ecociente.viewmodels.CadastroViewModel;
import com.google.android.material.button.MaterialButton;
import java.util.Arrays;
import java.util.List;

public class CadastroEtapa2Fragment extends Fragment {

    private EditText campoEndereco;
    private EditText campoNumero;
    private EditText campoCep;
    private EditText campoComplemento;
    private EditText campoCidade;

    private AutoCompleteTextView campoEstado;

    private ImageView botaoVoltarEtapa1;
    private ImageView iconeSetaEstado;

    private MaterialButton botaoCadastrar;

    private ProgressBar indicadorCarregamento;

    private CadastroViewModel viewModel;

    private boolean alterandoCep = false;

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

        viewModel = new ViewModelProvider(requireActivity()).get(CadastroViewModel.class);

        viewModel.getCarregando().observe(getViewLifecycleOwner(), this::definirCarregando);

        configurarBotaoVoltar();

        configurarMascaraCep();

        configurarListaEstados();

        configurarCadastro();

        recuperarDadosPreenchidos();
    }

    private void inicializarComponentes(View view) {

        campoEndereco = view.findViewById(R.id.campoEndereco);

        campoNumero = view.findViewById(R.id.campoNumero);

        campoCep = view.findViewById(R.id.campoCep);

        campoComplemento = view.findViewById(R.id.campoComplemento);

        campoCidade = view.findViewById(R.id.campoCidade);

        campoEstado = view.findViewById(R.id.campoEstado);

        botaoVoltarEtapa1 = view.findViewById(R.id.botaoVoltarEtapa1);

        iconeSetaEstado = view.findViewById(R.id.iconeSetaEstado);

        botaoCadastrar = view.findViewById(R.id.botaoCadastrar);

        indicadorCarregamento = view.findViewById(R.id.indicadorCarregamentoCadastro);
    }

    private void configurarBotaoVoltar() {

        botaoVoltarEtapa1.setOnClickListener(
                view -> {
                    salvarDadosAtuais();

                    ((Login) requireActivity()).voltarCadastroEtapa1();
                });
    }

    private void configurarMascaraCep() {

        campoCep.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence texto, int inicio, int quantidade, int depois) {}

                    @Override
                    public void onTextChanged(
                            CharSequence texto, int inicio, int antes, int quantidade) {}

                    @Override
                    public void afterTextChanged(Editable editable) {

                        if (alterandoCep) {
                            return;
                        }

                        alterandoCep = true;

                        String numeros = editable.toString().replaceAll("\\D", "");

                        if (numeros.length() > 8) {

                            numeros = numeros.substring(0, 8);
                        }

                        String formatado;

                        if (numeros.length() > 5) {

                            formatado = numeros.substring(0, 5) + "-" + numeros.substring(5);

                        } else {

                            formatado = numeros;
                        }

                        campoCep.setText(formatado);

                        campoCep.setSelection(campoCep.getText().length());

                        alterandoCep = false;
                    }
                });
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

        campoEstado.setDropDownVerticalOffset(dpParaPx(6));

        campoEstado.setOnClickListener(view -> campoEstado.showDropDown());

        iconeSetaEstado.setOnClickListener(
                view -> {
                    campoEstado.requestFocus();

                    campoEstado.showDropDown();
                });
    }

    private int dpParaPx(int dp) {

        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void configurarCadastro() {

        botaoCadastrar.setOnClickListener(view -> validarEtapa2());
    }

    private void validarEtapa2() {

        String endereco = campoEndereco.getText().toString().trim();

        String numero = campoNumero.getText().toString().trim();

        String cep = campoCep.getText().toString().trim();

        String complemento = campoComplemento.getText().toString().trim();

        String cidade = campoCidade.getText().toString().trim();

        String estado = campoEstado.getText().toString().trim();

        if (endereco.isEmpty()) {

            mostrarMensagem("Digite seu endereço.");

            campoEndereco.requestFocus();

            return;
        }

        if (numero.isEmpty()) {

            mostrarMensagem("Digite o número do endereço.");

            campoNumero.requestFocus();

            return;
        }

        if (cep.length() != 9) {

            mostrarMensagem("Digite um CEP válido.");

            campoCep.requestFocus();

            return;
        }

        if (cidade.isEmpty()) {

            mostrarMensagem("Digite sua cidade.");

            campoCidade.requestFocus();

            return;
        }

        if (estado.isEmpty() || !estadoValido(estado)) {

            mostrarMensagem("Selecione um estado.");

            campoEstado.showDropDown();

            return;
        }

        Login telaAutenticacao = (Login) requireActivity();

        telaAutenticacao.salvarDadosEtapa2(endereco, numero, cep, complemento, cidade, estado);

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

        if (resultado.getAviso() != null) {

            mostrarMensagem(resultado.getAviso());
        }

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
                || campoEstado == null) {

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
    }

    private void recuperarDadosPreenchidos() {

        Login telaAutenticacao = (Login) requireActivity();

        campoEndereco.setText(telaAutenticacao.getEndereco());

        campoNumero.setText(telaAutenticacao.getNumero());

        campoCep.setText(telaAutenticacao.getCep());

        campoComplemento.setText(telaAutenticacao.getComplemento());

        campoCidade.setText(telaAutenticacao.getCidade());

        campoEstado.setText(telaAutenticacao.getEstado(), false);
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

    private void mostrarMensagem(String mensagem) {

        if (!isAdded()) {
            return;
        }

        Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show();
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

        botaoVoltarEtapa1 = null;

        iconeSetaEstado = null;

        botaoCadastrar = null;

        indicadorCarregamento = null;

        super.onDestroyView();
    }
}
