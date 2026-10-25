package com.example.ecociente.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.repository.PerfilRepository;
import java.util.LinkedHashMap;
import java.util.Map;

// Dados do usuário em GET /api/usuarios/me (Postgres). A API é a fonte da verdade: o que ela
// informa vale mais que o documento do Firestore, que é ajustado para ficar igual.
public final class PerfilDaApi {

    private static final PerfilDaApi VAZIO = new PerfilDaApi("", "", "", "", "", "", "", "", "", "", "", "");

    private final String nome;
    private final String urlAvatar;
    private final String telefone;
    private final String cpf;
    private final String cep;
    private final String estado;
    private final String cidade;
    private final String logradouro;
    private final String numero;
    private final String complemento;
    private final String nomeCooperativa;
    private final String emailCooperativa;

    public PerfilDaApi(
            @Nullable String nome,
            @Nullable String urlAvatar,
            @Nullable String telefone,
            @Nullable String cpf,
            @Nullable String cep,
            @Nullable String estado,
            @Nullable String cidade,
            @Nullable String logradouro,
            @Nullable String numero,
            @Nullable String complemento,
            @Nullable String nomeCooperativa,
            @Nullable String emailCooperativa) {
        this.nome = limpo(nome);
        this.urlAvatar = limpo(urlAvatar);
        this.telefone = limpo(telefone);
        this.cpf = limpo(cpf);
        this.cep = limpo(cep);
        this.estado = limpo(estado);
        this.cidade = limpo(cidade);
        this.logradouro = limpo(logradouro);
        this.numero = limpo(numero);
        this.complemento = limpo(complemento);
        this.nomeCooperativa = limpo(nomeCooperativa);
        this.emailCooperativa = limpo(emailCooperativa);
    }

    @NonNull
    public static PerfilDaApi vazio() {
        return VAZIO;
    }

    @NonNull
    public String getNome() {
        return nome;
    }

    @NonNull
    public String getUrlAvatar() {
        return urlAvatar;
    }

    @NonNull
    public String getTelefone() {
        return telefone;
    }

    @NonNull
    public String getCpf() {
        return cpf;
    }

    @NonNull
    public String getCep() {
        return cep;
    }

    @NonNull
    public String getEstado() {
        return estado;
    }

    @NonNull
    public String getCidade() {
        return cidade;
    }

    @NonNull
    public String getLogradouro() {
        return logradouro;
    }

    @NonNull
    public String getNumero() {
        return numero;
    }

    @NonNull
    public String getComplemento() {
        return complemento;
    }

    @NonNull
    public String getNomeCooperativa() {
        return nomeCooperativa;
    }

    @NonNull
    public String getEmailCooperativa() {
        return emailCooperativa;
    }

    // Passa para `perfil` o que a API informou (campo vazio na API não apaga nada) e devolve só o
    // que mudou, no formato campo do Firestore -> novo valor, para o documento ser ajustado.
    @NonNull
    public Map<String, String> aplicarEm(@NonNull PerfilUsuario perfil) {

        Map<String, String> mudancas = new LinkedHashMap<>();

        boolean cooperativa = PerfilAcesso.ehCooperativa(perfil.getTipoPerfil());

        if (cooperativa) {
            if (!nomeCooperativa.isEmpty() && !nomeCooperativa.equals(perfil.getNomeCooperativa())) {
                perfil.setNomeCooperativa(nomeCooperativa);
                mudancas.put(PerfilRepository.CAMPO_NOME_COOPERATIVA, nomeCooperativa);
            }

            if (!emailCooperativa.isEmpty() && !emailCooperativa.equals(perfil.getEmailCooperativa())) {
                perfil.setEmailCooperativa(emailCooperativa);
                mudancas.put(PerfilRepository.CAMPO_EMAIL_COOPERATIVA, emailCooperativa);
            }

        } else if (!nome.isEmpty() && !nome.equals(perfil.getNome())) {
            perfil.setNome(nome);
            mudancas.put(PerfilRepository.CAMPO_NOME, nome);
        }

        String fone = formatarTelefone(telefone);

        if (!fone.isEmpty() && !fone.equals(perfil.getTelefone())) {
            perfil.setTelefone(fone);
            mudancas.put(PerfilRepository.CAMPO_TELEFONE, fone);
        }

        if (!urlAvatar.isEmpty() && !urlAvatar.equals(perfil.getFotoUrl())) {
            perfil.setFotoUrl(urlAvatar);
            mudancas.put(PerfilRepository.CAMPO_FOTO_URL, urlAvatar);
        }

        String documento = formatarCpf(cpf);

        if (!cooperativa && !documento.isEmpty() && !documento.equals(perfil.getCpf())) {
            perfil.setCpf(documento);
            mudancas.put(PerfilRepository.CAMPO_CPF, documento);
        }

        String cepFormatado = formatarCep(cep);

        if (!cepFormatado.isEmpty() && !cepFormatado.equals(perfil.getCep())) {
            perfil.setCep(cepFormatado);
            mudancas.put(PerfilRepository.CAMPO_CEP, cepFormatado);
        }

        if (!logradouro.isEmpty() && !logradouro.equals(perfil.getEndereco())) {
            perfil.setEndereco(logradouro);
            mudancas.put(PerfilRepository.CAMPO_ENDERECO, logradouro);
        }

        if (!numero.isEmpty() && !numero.equals(perfil.getNumero())) {
            perfil.setNumero(numero);
            mudancas.put(PerfilRepository.CAMPO_NUMERO, numero);
        }

        if (!complemento.isEmpty() && !complemento.equals(perfil.getComplemento())) {
            perfil.setComplemento(complemento);
            mudancas.put(PerfilRepository.CAMPO_COMPLEMENTO, complemento);
        }

        if (!cidade.isEmpty() && !cidade.equals(perfil.getCidade())) {
            perfil.setCidade(cidade);
            mudancas.put(PerfilRepository.CAMPO_CIDADE, cidade);
        }

        if (!estado.isEmpty() && !estado.equals(perfil.getEstado())) {
            perfil.setEstado(estado);
            mudancas.put(PerfilRepository.CAMPO_ESTADO, estado);
        }

        return mudancas;
    }

    @NonNull
    static String formatarTelefone(@NonNull String valor) {

        String digitos = valor.replaceAll("\\D", "");

        if (digitos.length() == 11) {
            return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 7) + "-" + digitos.substring(7);
        }

        if (digitos.length() == 10) {
            return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 6) + "-" + digitos.substring(6);
        }

        return valor;
    }

    @NonNull
    static String formatarCep(@NonNull String valor) {

        String digitos = valor.replaceAll("\\D", "");

        return digitos.length() == 8 ? digitos.substring(0, 5) + "-" + digitos.substring(5) : valor;
    }

    @NonNull
    static String formatarCpf(@NonNull String valor) {

        String digitos = valor.replaceAll("\\D", "");

        return digitos.length() == 11
                ? digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "." + digitos.substring(6, 9)
                        + "-" + digitos.substring(9)
                : valor;
    }

    @NonNull
    private static String limpo(@Nullable String valor) {
        return valor == null || "null".equals(valor) ? "" : valor.trim();
    }
}
