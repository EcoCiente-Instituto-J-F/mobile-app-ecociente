package com.example.ecociente.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.repository.EsqueciSenhaRepository;
import java.util.Locale;

// Compartilhado pelos 3 Fragments (escopo na Activity), guarda email/código
// entre os passos e expõe o resultado de cada chamada como LiveData.
public class EsqueciSenhaViewModel extends ViewModelComCarregamento {
    private final EsqueciSenhaRepository repositorio = new EsqueciSenhaRepository();
    private String email = "";
    private String codigo = "";

    @NonNull
    public LiveData<ResultadoApi> enviarCodigo(@NonNull String email) {
        this.email = email.trim().toLowerCase(Locale.ROOT);
        return executar(repositorio.enviarCodigo(this.email));
    }

    public void definirCodigo(@NonNull String codigo) {
        this.codigo = codigo;
    }

    @NonNull
    public LiveData<ResultadoApi> redefinirSenha(@NonNull String novaSenha) {
        return executar(repositorio.redefinirSenha(email, codigo, novaSenha));
    }
}
