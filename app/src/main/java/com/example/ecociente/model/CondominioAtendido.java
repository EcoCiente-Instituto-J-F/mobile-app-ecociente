package com.example.ecociente.model;

import androidx.annotation.NonNull;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/** Condomínio com atendimento confirmado ou realizado pela cooperativa autenticada. */
public final class CondominioAtendido {
    private final int id;
    private final String nome;
    private final String endereco;
    private final long totalTorres;
    private final List<String> diasColeta;

    public CondominioAtendido(int id, @NonNull String nome, @NonNull String endereco,
                              long totalTorres, @NonNull List<String> diasColeta) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.totalTorres = totalTorres;
        this.diasColeta = Collections.unmodifiableList(new ArrayList<>(diasColeta));
    }

    public int getId() { return id; }
    @NonNull public String getNome() { return nome; }
    @NonNull public String getEndereco() { return endereco; }
    public long getTotalTorres() { return totalTorres; }
    @NonNull public List<String> getDiasColeta() { return diasColeta; }
}
