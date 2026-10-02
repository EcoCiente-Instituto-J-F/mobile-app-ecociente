package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

// O projeto ecociente-9325f usa um banco Firestore com o nome literal
// "default", não o banco especial "(default)" que FirebaseFirestore.getInstance()
// usa por padrão. Sem isso, toda leitura/escrita falha com
// "NOT_FOUND: The database (default) does not exist".
public final class FirestoreProvider {

    private static final String NOME_BANCO = "default";

    private FirestoreProvider() {}

    @NonNull
    public static FirebaseFirestore obterInstancia() {
        return FirebaseFirestore.getInstance(FirebaseApp.getInstance(), NOME_BANCO);
    }
}
