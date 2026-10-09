package com.example.ecociente.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.annotation.NonNull;

public final class AoDigitar implements TextWatcher {

    private final Runnable acao;

    private AoDigitar(@NonNull Runnable acao) {
        this.acao = acao;
    }

    public static void em(@NonNull EditText campo, @NonNull Runnable acao) {
        campo.addTextChangedListener(new AoDigitar(acao));
    }

    @Override
    public void beforeTextChanged(CharSequence texto, int inicio, int quantidade, int depois) {}

    @Override
    public void onTextChanged(CharSequence texto, int inicio, int antes, int quantidade) {}

    @Override
    public void afterTextChanged(Editable texto) {
        acao.run();
    }
}
