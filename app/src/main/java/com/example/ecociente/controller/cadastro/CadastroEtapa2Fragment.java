package com.example.ecociente.controller.cadastro;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ecociente.R;
import com.google.android.material.button.MaterialButton;

public class CadastroEtapa2Fragment extends Fragment {

    private EditText campoEndereco;
    private EditText campoNumero;
    private EditText campoCep;
    private EditText campoComplemento;
    private EditText campoCidade;
    private Spinner campoEstado;
    private MaterialButton botaoCadastrar;

    public CadastroEtapa2Fragment() {
        super(R.layout.fragment_cadastro_etapa2);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estadoSalvo) {
        super.onViewCreated(view, estadoSalvo);

        buscarComponentes(view);
        configurarCliques();
    }

    private void buscarComponentes(View view) {
        campoEndereco = view.findViewById(R.id.campoEndereco);
        campoNumero = view.findViewById(R.id.campoNumero);
        campoCep = view.findViewById(R.id.campoCep);
        campoComplemento = view.findViewById(R.id.campoComplemento);
        campoCidade = view.findViewById(R.id.campoCidade);
        campoEstado = view.findViewById(R.id.campoEstado);
        botaoCadastrar = view.findViewById(R.id.botaoCadastrar);
    }

    private void configurarCliques() {
        botaoCadastrar.setOnClickListener(clique -> finalizarCadastro());
    }

    private void finalizarCadastro() {
        String endereco = campoEndereco.getText().toString().trim();
        String numero = campoNumero.getText().toString().trim();
        String cep = campoCep.getText().toString().trim();
        String cidade = campoCidade.getText().toString().trim();

        if (endereco.isEmpty() || numero.isEmpty() || cep.isEmpty() || cidade.isEmpty()) {
            mostrarMensagem("Preencha todos os campos obrigatórios");
            return;
        }

        mostrarMensagem("Dados preenchidos corretamente");
    }

    private void mostrarMensagem(String mensagem) {
        Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show();
    }
}