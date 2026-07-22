package com.example.ecociente.controller;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecociente.MainActivity;
import com.example.ecociente.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    private EditText campoEmail;
    private EditText campoSenha;
    private MaterialButton botaoLogin;
    private ImageView iconeOlhoSenha;
    private FirebaseAuth autenticacao;
    private boolean senhaVisivel = false;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        setContentView(R.layout.activity_login);

        campoEmail = findViewById(R.id.campoEmail);
        campoSenha = findViewById(R.id.campoSenha);
        botaoLogin = findViewById(R.id.botaoLogin);
        iconeOlhoSenha = findViewById(R.id.iconeOlhoSenha);

        autenticacao = FirebaseAuth.getInstance();

        botaoLogin.setOnClickListener(clique -> fazerLogin());
        iconeOlhoSenha.setOnClickListener(clique -> alternarVisibilidadeSenha());
    }

    private void fazerLogin() {
        String email = campoEmail.getText().toString().trim();
        String senha = campoSenha.getText().toString();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha o e-mail e a senha", Toast.LENGTH_SHORT).show();
            return;
        }

        autenticacao
                .signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(tarefa -> {
                    if (tarefa.isSuccessful()) {
                        Toast.makeText(this, "Login realizado com sucesso", Toast.LENGTH_SHORT).show();

                        Intent rota = new Intent(Login.this, MainActivity.class);
                        startActivity(rota);
                        finish();
                    } else {
                        Toast.makeText(this, "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void alternarVisibilidadeSenha() {
        if (senhaVisivel) {
            campoSenha.setTransformationMethod(PasswordTransformationMethod.getInstance());
            iconeOlhoSenha.setImageResource(R.drawable.icon_olho_fechado);
            iconeOlhoSenha.setContentDescription("Mostrar senha");
            senhaVisivel = false;
        } else {
            campoSenha.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            iconeOlhoSenha.setImageResource(R.drawable.icon_olho_aberto);
            iconeOlhoSenha.setContentDescription("Ocultar senha");
            senhaVisivel = true;
        }
        campoSenha.setSelection(campoSenha.getText().length());
    }
}