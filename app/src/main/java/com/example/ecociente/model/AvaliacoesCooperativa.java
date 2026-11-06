package com.example.ecociente.model;

import androidx.annotation.NonNull;
import java.util.List;

// Avaliações que os condomínios deram à cooperativa (ds-mobile-api).
public final class AvaliacoesCooperativa {

    private final double media;
    private final long total;
    private final List<ItemLista> itens;

    public AvaliacoesCooperativa(double media, long total, @NonNull List<ItemLista> itens) {
        this.media = media;
        this.total = total;
        this.itens = itens;
    }

    public double getMedia() {
        return media;
    }

    public long getTotal() {
        return total;
    }

    @NonNull
    public List<ItemLista> getItens() {
        return itens;
    }
}
