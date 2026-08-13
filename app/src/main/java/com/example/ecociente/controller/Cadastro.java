package com.example.ecociente.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.ecociente.R;
import com.example.ecociente.controller.cadastro.CadastroEtapa1Fragment;
import com.example.ecociente.controller.cadastro.CadastroEtapa2Fragment;

public class Cadastro extends AppCompatActivity {
    private TextView abaLogin;
    private String nome;
    private String dataNascimento;
    private String email;
    private String senha;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        setContentView(R.layout.activity_cadastro);

        buscarComponentes();
        configurarCliques();

        if (estadoSalvo == null) {
            abrirEtapa1();
        }
    }

    private void buscarComponentes() {
        abaLogin = findViewById(R.id.abaLogin);
    }

    private void configurarCliques() {
        abaLogin.setOnClickListener(clique -> abrirLogin());
    }

    public void salvarDadosEtapa1(String nome, String dataNascimento, String email, String senha) {
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.email = email;
        this.senha = senha;

        abrirEtapa2();
    }

    public void abrirEtapa1() {
        trocarFragmento(new CadastroEtapa1Fragment());
    }

    private void abrirEtapa2() {
        trocarFragmento(new CadastroEtapa2Fragment());
    }

    private void trocarFragmento(Fragment fragmento) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.containerFragmentCadastro, fragmento)
                .commit();
    }

    private void abrirLogin() {
        Intent rota = new Intent(Cadastro.this, Login.class);
        startActivity(rota);
        finish();
    }

    public String getNome() {
        return nome;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }
}