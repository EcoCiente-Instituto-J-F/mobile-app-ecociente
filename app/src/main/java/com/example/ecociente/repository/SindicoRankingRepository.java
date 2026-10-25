package com.example.ecociente.repository;

import com.example.ecociente.model.SindicoPosicaoRanking;

import java.util.Collections;
import java.util.List;

/** Rankings que a API ainda não entrega: vazios. O ranking por torre vem da ds-pontuacao-api. */
public final class SindicoRankingRepository {
    private static final SindicoRankingRepository INSTANCIA = new SindicoRankingRepository();

    private final List<SindicoPosicaoRanking> porBloco = Collections.emptyList();

    // O ranking por apartamento ainda não existe em nenhuma API.
    private final List<SindicoPosicaoRanking> porApartamento = Collections.emptyList();

    private SindicoRankingRepository() { }

    public static SindicoRankingRepository getInstance() { return INSTANCIA; }

    public List<SindicoPosicaoRanking> getRanking(boolean apartamentos) {
        return apartamentos ? porApartamento : porBloco;
    }
}
