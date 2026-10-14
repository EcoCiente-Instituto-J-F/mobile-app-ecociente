package com.example.ecociente.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.example.ecociente.R;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum TipoLista {
    GUIA(R.string.guia, R.drawable.icon_guia_verde),
    FEED(R.string.feed, R.drawable.ic_nav_feed_verde),
    CONDOMINIOS(R.string.condominios_atendidos, R.drawable.ic_perfil_cooperativa),
    AVALIACOES(R.string.avaliacoes_recebidas, R.drawable.ic_estrela),
    RANKING(R.string.ranking_do_morador, R.drawable.ic_trofeu);

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
                // Carregado da ds-mobile-api em TelaListaActivity (aulas dos cursos).
                return Collections.emptyList();

            case FEED:
                return Arrays.asList(
                        new ItemLista("Coleta antecipada nesta semana", "Aviso da cooperativa EcoCoop", "Hoje"),
                        new ItemLista("Novo ponto de descarte de vidro", "Disponível na portaria do bloco B", "Ontem"),
                        new ItemLista("Meta de reciclagem do mês atingida", "O condomínio reciclou 320 kg em setembro", "Seg"),
                        new ItemLista("Lembrete: separe o óleo de cozinha", "Entregue em garrafas PET fechadas", "05/10"),
                        new ItemLista("Reunião de moradores sobre coleta", "Quinta-feira, 19h, no salão de festas", "02/10"));

            case CONDOMINIOS:
                // API: GET /api/v1/cooperativas/me/condominios
                return Collections.emptyList();

            case AVALIACOES:
                // API: GET /api/v1/cooperativas/me/avaliacoes
                return Collections.emptyList();

            default:
                // API: GET /api/v1/rankings/moradores?condominioId= (ds-pontuacao-api)
                return Collections.emptyList();
        }
    }
}
