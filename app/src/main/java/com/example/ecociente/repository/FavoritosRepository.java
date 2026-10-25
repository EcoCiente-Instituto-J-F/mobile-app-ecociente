package com.example.ecociente.repository;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.HashSet;
import java.util.Set;

// Conteúdos salvos no aparelho, separados por usuário (a troca de conta não mistura os favoritos).
public final class FavoritosRepository {

    private static final String PREFERENCIAS = "conteudos_salvos";
    private static final String SEM_USUARIO = "sem_usuario";

    private FavoritosRepository() {}

    public static boolean ehSalvo(@NonNull Context contexto, @NonNull String chave) {
        return salvos(contexto).contains(chave);
    }

    // Salva ou tira dos salvos; devolve true se o item ficou salvo.
    public static boolean alternar(@NonNull Context contexto, @NonNull String chave) {

        Set<String> salvos = new HashSet<>(salvos(contexto));

        boolean salvar = !salvos.remove(chave);

        if (salvar) {
            salvos.add(chave);
        }

        preferencias(contexto).edit().putStringSet(chaveDoUsuario(), salvos).apply();

        return salvar;
    }

    @NonNull
    private static Set<String> salvos(@NonNull Context contexto) {
        return preferencias(contexto).getStringSet(chaveDoUsuario(), new HashSet<>());
    }

    @NonNull
    private static SharedPreferences preferencias(@NonNull Context contexto) {
        return contexto.getApplicationContext().getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
    }

    @NonNull
    private static String chaveDoUsuario() {

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        return usuario != null ? usuario.getUid() : SEM_USUARIO;
    }
}
