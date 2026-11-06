package com.example.ecociente.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// Os getters têm os nomes do FirebaseUser para as telas não mudarem as chamadas.
public final class UsuarioLogado {

    private final String uid;
    private final String email;
    private final String nome;

    public UsuarioLogado(@NonNull String uid, @Nullable String email, @Nullable String nome) {
        this.uid = uid;
        this.email = email;
        this.nome = nome;
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    @Nullable
    public String getDisplayName() {
        return nome;
    }
}
