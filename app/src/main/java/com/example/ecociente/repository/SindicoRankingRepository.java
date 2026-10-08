package com.example.ecociente.repository;

import com.example.ecociente.model.SindicoPosicaoRanking;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Duas listas estáticas de demonstração; não consulta banco nem API. */
public final class SindicoRankingRepository {
    private static final SindicoRankingRepository INSTANCIA = new SindicoRankingRepository();

    private final List<SindicoPosicaoRanking> porBloco = Collections.unmodifiableList(
            Arrays.asList(
                    new SindicoPosicaoRanking("RM", "Residencial Monte Verde", 1980, false),
                    new SindicoPosicaoRanking("AP", "Aurora Park", 1790, false),
                    new SindicoPosicaoRanking("AP", "Alameda Primavera", 1650, false),
                    new SindicoPosicaoRanking("AP", "Adriel P.", 1380, false),
                    new SindicoPosicaoRanking("AP", "Adriel P.", 1310, false),
                    new SindicoPosicaoRanking("JU", "Você", 1250, true),
                    new SindicoPosicaoRanking("AP", "Adriel P.", 1167, false),
                    new SindicoPosicaoRanking("AP", "Adriel P.", 1111, false),
                    new SindicoPosicaoRanking("AP", "Adriel P.", 1000, false),
                    new SindicoPosicaoRanking("AP", "Adriel P.", 925, false)));

    private final List<SindicoPosicaoRanking> porApartamento = Collections.unmodifiableList(
            Arrays.asList(
                    new SindicoPosicaoRanking("101", "Apto 101", 1860, false),
                    new SindicoPosicaoRanking("202", "Apto 202", 1740, false),
                    new SindicoPosicaoRanking("104", "Apto 104", 1620, false),
                    new SindicoPosicaoRanking("102", "Apto 102", 1500, false),
                    new SindicoPosicaoRanking("203", "Apto 203", 1390, false),
                    new SindicoPosicaoRanking("120", "Seu apartamento", 1250, true),
                    new SindicoPosicaoRanking("201", "Apto 201", 1170, false),
                    new SindicoPosicaoRanking("302", "Apto 302", 1090, false),
                    new SindicoPosicaoRanking("303", "Apto 303", 980, false),
                    new SindicoPosicaoRanking("304", "Apto 304", 890, false)));

    private SindicoRankingRepository() { }

    public static SindicoRankingRepository getInstance() { return INSTANCIA; }

    public List<SindicoPosicaoRanking> getRanking(boolean apartamentos) {
        return apartamentos ? porApartamento : porBloco;
    }
}
