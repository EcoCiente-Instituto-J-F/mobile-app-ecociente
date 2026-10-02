package com.example.ecociente.repository;

import com.example.ecociente.model.SindicoCooperativa;
import com.example.ecociente.model.SindicoHistoricoColeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Fonte local substituível por uma API quando houver contrato e autorização do síndico. */
public final class SindicoColetaRepository {
    private static final SindicoColetaRepository INSTANCIA = new SindicoColetaRepository();

    private final List<SindicoCooperativa> cooperativas = Collections.unmodifiableList(Arrays.asList(
            new SindicoCooperativa(1, "Cooperativa Recicla Mais", "2,3 km", "4,7 (124 avaliações)"),
            new SindicoCooperativa(2, "Cooperativa VerdeMais", "3,1 km", "4,8 (96 avaliações)"),
            new SindicoCooperativa(3, "Cooperativa Nova Vida", "4,2 km", "4,6 (82 avaliações)"),
            new SindicoCooperativa(4, "Cooperativa EcoAção", "5,0 km", "4,9 (71 avaliações)"),
            new SindicoCooperativa(5, "Cooperativa Reciclar Juntos", "6,4 km", "4,5 (58 avaliações)")));
    private final List<SindicoHistoricoColeta> historico = Collections.unmodifiableList(Arrays.asList(
            new SindicoHistoricoColeta(101, "Cooperativa Recicla Mais", "27/09/2026", "4,7 (124 avaliações)"),
            new SindicoHistoricoColeta(102, "Cooperativa VerdeMais", "23/09/2026", "4,8 (96 avaliações)"),
            new SindicoHistoricoColeta(103, "Cooperativa Nova Vida", "18/09/2026", "4,6 (82 avaliações)"),
            new SindicoHistoricoColeta(104, "Cooperativa Recicla Mais", "12/09/2026", "4,7 (124 avaliações)"),
            new SindicoHistoricoColeta(105, "Cooperativa EcoAção", "06/09/2026", "4,9 (71 avaliações)"),
            new SindicoHistoricoColeta(106, "Cooperativa VerdeMais", "31/08/2026", "4,8 (96 avaliações)"),
            new SindicoHistoricoColeta(107, "Cooperativa Reciclar Juntos", "25/08/2026", "4,5 (58 avaliações)"),
            new SindicoHistoricoColeta(108, "Cooperativa Nova Vida", "19/08/2026", "4,6 (82 avaliações)")));
    private final Map<Integer, Integer> avaliacoes = new HashMap<>();

    private SindicoColetaRepository() {
        avaliacoes.put(108, 4);
    }

    public static SindicoColetaRepository getInstance() { return INSTANCIA; }
    public List<SindicoCooperativa> getCooperativas() { return cooperativas; }

    public SindicoCooperativa buscar(int id) {
        for (SindicoCooperativa cooperativa : cooperativas) {
            if (cooperativa.getId() == id) return cooperativa;
        }
        return null;
    }

    public List<SindicoHistoricoColeta> getHistorico() { return historico; }

    public int getAvaliacao(int coletaId) {
        Integer nota = avaliacoes.get(coletaId);
        return nota == null ? 0 : nota;
    }

    public void avaliar(int coletaId, int nota) {
        if (nota < 1 || nota > 5) throw new IllegalArgumentException("A nota deve ser de 1 a 5");
        boolean encontrada = false;
        for (SindicoHistoricoColeta coleta : historico) {
            if (coleta.getId() == coletaId) {
                encontrada = true;
                break;
            }
        }
        if (!encontrada) throw new IllegalArgumentException("Coleta não encontrada");
        avaliacoes.put(coletaId, nota);
    }
}
