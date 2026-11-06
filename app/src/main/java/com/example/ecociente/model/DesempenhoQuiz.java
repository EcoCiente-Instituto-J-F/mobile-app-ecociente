package com.example.ecociente.model;

import androidx.annotation.NonNull;

// Tentativa de quiz concluída, vinda do histórico da API. `nota` vai de 0 a 100.
public final class DesempenhoQuiz {

    public final int tentativaId;
    @NonNull public final String titulo;
    public final double nota;
    @NonNull public final String concluidoEm;

    public DesempenhoQuiz(int tentativaId, @NonNull String titulo, double nota, @NonNull String concluidoEm) {
        this.tentativaId = tentativaId;
        this.titulo = titulo;
        this.nota = nota;
        this.concluidoEm = concluidoEm;
    }
}
