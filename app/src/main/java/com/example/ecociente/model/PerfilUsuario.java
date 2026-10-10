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
    private String fotoUrl = "";
    private String tipoPerfil = "";
    private String nomeCooperativa = "";
    private String cnpj = "";
    private String emailCooperativa = "";
    private String numero = "";
    private String complemento = "";
    private String cidade = "";
    private String estado = "";
    private String cep = "";
    private boolean possuiCodigoCondominio;

    public PerfilUsuario() {}

    @NonNull
    public String getTipoPerfil() {
        return tipoPerfil == null ? "" : tipoPerfil;
    }

    public void setTipoPerfil(String tipoPerfil) {
        this.tipoPerfil = tipoPerfil;
    }

    public boolean isPossuiCodigoCondominio() {
        return possuiCodigoCondominio;
    }

    public void setPossuiCodigoCondominio(boolean possuiCodigoCondominio) {
        this.possuiCodigoCondominio = possuiCodigoCondominio;
    }

    @NonNull
    public String getNumero() {
        return numero == null ? "" : numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    @NonNull
    public String getComplemento() {
        return complemento == null ? "" : complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    @NonNull
    public String getCidade() {
        return cidade == null ? "" : cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    @NonNull
    public String getEstado() {
        return estado == null ? "" : estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @NonNull
    public String getCep() {
        return cep == null ? "" : cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    @NonNull
    public String getCnpj() {
        return cnpj == null ? "" : cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    @NonNull
    public String getEmailCooperativa() {
        return emailCooperativa == null ? "" : emailCooperativa;
    }

    public void setEmailCooperativa(String emailCooperativa) {
        this.emailCooperativa = emailCooperativa;
    }

    @NonNull
    public String getNomeCooperativa() {
        return nomeCooperativa == null ? "" : nomeCooperativa;
    }

    public void setNomeCooperativa(String nomeCooperativa) {
        this.nomeCooperativa = nomeCooperativa;
    }

    @NonNull
    public String getFotoUrl() {
        return fotoUrl == null ? "" : fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

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
