package com.example.ecociente.repository;

import com.example.ecociente.model.SindicoPosicaoRanking;

import java.util.Collections;
import java.util.List;

/** Sem dados de exemplo. */
public final class SindicoRankingRepository {
    private static final SindicoRankingRepository INSTANCIA = new SindicoRankingRepository();

    private SindicoRankingRepository() { }

    public static SindicoRankingRepository getInstance() { return INSTANCIA; }

    // API: GET /api/v1/rankings/torres?condominioId= (ds-pontuacao-api);
    // por apartamento: depende da tabela de unidades, ainda sem endpoint.
    public List<SindicoPosicaoRanking> getRanking(boolean apartamentos) {
        return Collections.emptyList();
    }
}
