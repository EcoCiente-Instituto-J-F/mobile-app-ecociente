package com.example.ecociente.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.example.ecociente.R;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum TipoLista {
    GUIA(R.string.guia, R.drawable.ic_guia_verde),
    FEED(R.string.feed, R.drawable.ic_nav_feed_verde),
    CONDOMINIOS(R.string.condominios_atendidos, R.drawable.ic_perfil_cooperativa),
    AVALIACOES(R.string.avaliacoes_recebidas, R.drawable.ic_estrela),
    RANKING(R.string.ranking_do_morador, R.drawable.ic_trofeu),
    PONTOS(R.string.meus_pontos, R.drawable.ic_trofeu),
    SALVOS(R.string.conteudos_salvos, R.drawable.ic_guia_verde);

    @StringRes public final int titulo;
    @DrawableRes public final int icone;

    TipoLista(@StringRes int titulo, @DrawableRes int icone) {
        this.titulo = titulo;
        this.icone = icone;
    }

    @NonNull
    public List<ItemLista> itens() {

        switch (this) {
            case GUIA:
            case SALVOS:
            case AVALIACOES:
            case CONDOMINIOS:
            case PONTOS:
                return Collections.emptyList();

            case FEED:
                return Collections.emptyList();

            default:
                return Arrays.asList(
                        new ItemLista("1º  Rafael Mendes", "Bloco B · Apto 402", "1.980 pts"),
                        new ItemLista("2º  Ana Paula Souza", "Bloco A · Apto 101", "1.840 pts"),
                        new ItemLista("3º  Marina Alves", "Bloco A · Apto 201", "1.720 pts"),
                        new ItemLista("4º  Carlos Eduardo Lima", "Bloco A · Apto 102", "1.560 pts"),
                        new ItemLista("5º  Juliana Costa", "Bloco B · Apto 302", "1.410 pts"),
                        new ItemLista("6º  Pedro Henrique Dias", "Bloco B · Apto 101", "1.250 pts"));
        }
    }
}
