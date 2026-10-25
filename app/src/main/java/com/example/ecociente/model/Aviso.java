package com.example.ecociente.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class Aviso {

    public final int id;
    public final String titulo;
    public final String mensagem;
    public final CategoriaAviso categoria;
    public final boolean deCooperativa;
    public final String remetente;
    public final String enviadoEm;

    private boolean lida;

    private Aviso(
            int id,
            String titulo,
            String mensagem,
            CategoriaAviso categoria,
            boolean deCooperativa,
            String remetente,
            long horasAtras,
            boolean lida) {
        this.id = id;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.categoria = categoria;
        this.deCooperativa = deCooperativa;
        this.remetente = remetente;
        this.enviadoEm =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                        .format(new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(horasAtras)));
        this.lida = lida;
    }

    public boolean isLida() {
        return lida;
    }

    public void marcarComoLida() {
        lida = true;
    }

    // Ainda não há API de avisos: a lista é vazia.
    @NonNull
    public static synchronized List<Aviso> lista() {

        return Collections.emptyList();
    }

    public static int quantidadeNaoLidos() {

        int quantidade = 0;

        for (Aviso aviso : lista()) {
            if (!aviso.isLida()) {
                quantidade++;
            }
        }

        return quantidade;
    }

    @Nullable
    public static Aviso porId(int id) {

        for (Aviso aviso : lista()) {
            if (aviso.id == id) {
                return aviso;
            }
        }

        return null;
    }
}
