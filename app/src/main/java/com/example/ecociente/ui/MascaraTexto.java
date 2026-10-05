package com.example.ecociente.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.annotation.NonNull;

public final class MascaraTexto implements TextWatcher {

    private static final char POSICAO_DIGITO = '#';

    private final EditText campo;
    private final String padrao;
    private final int maximoDigitos;

    private boolean aplicando;

    public MascaraTexto(@NonNull EditText campo, @NonNull String padrao) {
        this.campo = campo;
        this.padrao = padrao;
        this.maximoDigitos = (int) padrao.chars().filter(c -> c == POSICAO_DIGITO).count();
    }

    @NonNull
    public String formatar(@NonNull CharSequence texto) {

        StringBuilder formatado = new StringBuilder();

        int usados = 0;
        int posicaoPadrao = 0;

        for (int i = 0; i < texto.length() && usados < maximoDigitos; i++) {

            char caractere = texto.charAt(i);

            if (!Character.isDigit(caractere)) {
                continue;
            }

            while (padrao.charAt(posicaoPadrao) != POSICAO_DIGITO) {
                formatado.append(padrao.charAt(posicaoPadrao++));
            }

            formatado.append(caractere);

            posicaoPadrao++;
            usados++;
        }

        return formatado.toString();
    }

    @Override
    public void beforeTextChanged(CharSequence texto, int inicio, int quantidade, int depois) {}

    @Override
    public void onTextChanged(CharSequence texto, int inicio, int antes, int quantidade) {}

    @Override
    public void afterTextChanged(Editable editable) {

        if (aplicando) {
            return;
        }

        String formatado = formatar(editable);

        if (formatado.contentEquals(editable)) {
            return;
        }

        aplicando = true;

        campo.setText(formatado);

        campo.setSelection(campo.getText().length());

        aplicando = false;
    }
}
