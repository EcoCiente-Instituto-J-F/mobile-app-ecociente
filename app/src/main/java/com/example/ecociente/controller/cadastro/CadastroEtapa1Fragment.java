package com.example.ecociente.controller.cadastro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ecociente.R;
import com.example.ecociente.controller.Cadastro;
import com.google.android.material.button.MaterialButton;

public class CadastroEtapa1Fragment extends Fragment {

    private EditText campoNome;
    private EditText campoDataNascimento;
    private EditText campoEmail;
    private EditText campoSenha;
    private EditText campoConfirmarSenha;
    private MaterialButton botaoContinuar;

    public CadastroEtapa1Fragment() {
        super(R.layout.fragment_cadastro_etapa1);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estadoSalvo) {
        super.onViewCreated(view, estadoSalvo);

        buscarComponentes(view);
        configurarCliques();
        recuperarDadosPreenchidos();
    }

    private void buscarComponentes(View view) {
        campoNome = view.findViewById(R.id.campoNome);
        campoDataNascimento = view.findViewById(R.id.campoDataNascimento);
        campoEmail = view.findViewById(R.id.campoEmailCadastro);
        campoSenha = view.findViewById(R.id.campoSenhaCadastro);
        campoConfirmarSenha = view.findViewById(R.id.campoConfirmarSenha);
        botaoContinuar = view.findViewById(R.id.botaoContinuar);
    }

    private void configurarCliques() {
        botaoContinuar.setOnClickListener(clique -> continuarCadastro());
    }

    private void continuarCadastro() {
        String nome = campoNome.getText().toString().trim();
        String dataNascimento = campoDataNascimento.getText().toString().trim();
        String email = campoEmail.getText().toString().trim();
        String senha = campoSenha.getText().toString();
        String confirmarSenha = campoConfirmarSenha.getText().toString();

        if (nome.isEmpty() || dataNascimento.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
            mostrarMensagem("Preencha todos os campos");
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            mostrarMensagem("As senhas não coincidem");
            return;
        }

        if (senha.length() < 6) {
            mostrarMensagem("A senha deve ter pelo menos 6 caracteres");
            return;
        }

        Cadastro cadastro = (Cadastro) requireActivity();

        cadastro.salvarDadosEtapa1(nome, dataNascimento, email, senha);
    }

    private void recuperarDadosPreenchidos() {
        Cadastro cadastro = (Cadastro) requireActivity();

        if (cadastro.getNome() != null) {
            campoNome.setText(cadastro.getNome());
        }

        if (cadastro.getDataNascimento() != null) {
            campoDataNascimento.setText(cadastro.getDataNascimento());
        }

        if (cadastro.getEmail() != null) {
            campoEmail.setText(cadastro.getEmail());
        }

        if (cadastro.getSenha() != null) {
            campoSenha.setText(cadastro.getSenha());
            campoConfirmarSenha.setText(cadastro.getSenha());
        }
    }

    private void mostrarMensagem(String mensagem) {
        Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show();
    }
}