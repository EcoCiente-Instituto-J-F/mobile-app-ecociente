package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// Quem responde às requisições que a ConexaoDemo recebe no lugar da rede.
public interface TratadorDemo {

    @NonNull
    RespostaDemo tratar(
            @NonNull String metodo, @NonNull String url, @Nullable String autorizacao, @NonNull byte[] corpo);
}
