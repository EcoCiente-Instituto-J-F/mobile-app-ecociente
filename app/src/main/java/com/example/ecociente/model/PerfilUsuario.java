package com.example.ecociente.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class PerfilUsuario {

    private String nome = "";
    private String email = "";
    private String telefone = "";
    private String endereco = "";
    private String cpf = "";

    public PerfilUsuario() {}

    @NonNull
    public String getNome() {
        return nome == null ? "" : nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @NonNull
    public String getEmail() {
        return email == null ? "" : email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @NonNull
    public String getTelefone() {
        return telefone == null ? "" : telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    @NonNull
    public String getEndereco() {
        return endereco == null ? "" : endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    @NonNull
    public String getCpf() {
        return cpf == null ? "" : cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    // O documento no Firestore pode ainda não ter nome/email preenchidos
    // (ex.: acabou de logar com Google/Facebook) - nesse caso cai pro que
    // já está no FirebaseAuth em vez de mostrar campo vazio.
    @NonNull
    public static String nomeExibicao(@Nullable PerfilUsuario perfil, @NonNull FirebaseUser usuario) {

        String nome = perfil != null && !perfil.getNome().isEmpty() ? perfil.getNome() : usuario.getDisplayName();

        return nome != null ? nome : "";
    }

    @NonNull
    public static String emailExibicao(@Nullable PerfilUsuario perfil, @NonNull FirebaseUser usuario) {

        String email = perfil != null && !perfil.getEmail().isEmpty() ? perfil.getEmail() : usuario.getEmail();

        return email != null ? email : "";
    }
}
