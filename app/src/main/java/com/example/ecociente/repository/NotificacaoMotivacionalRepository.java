package com.example.ecociente.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NotificacaoMotivacionalRepository {

    private final SorteadorMensagensMotivacionais sorteador =
            new SorteadorMensagensMotivacionais();

    private final Handler principal = new Handler(Looper.getMainLooper());

    public interface Retorno {
        void aoConcluir(@Nullable String mensagem);
    }

    public void buscar(@NonNull Retorno retorno) {
        String mensagem = sorteador.sortear();
        principal.post(() -> retorno.aoConcluir(mensagem));
    }
}
