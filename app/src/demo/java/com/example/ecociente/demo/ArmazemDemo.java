package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// Onde o estado da demo fica guardado entre as aberturas do app.
public interface ArmazemDemo {

    @Nullable
    String ler();

    void salvar(@NonNull String json);

    // Apaga o estado e as fotos gravadas.
    void apagarTudo();

    // Grava a foto e devolve uma URL file:// para ela.
    @NonNull
    String gravarFoto(@NonNull byte[] jpeg);
}
