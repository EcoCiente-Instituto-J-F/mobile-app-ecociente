package com.example.ecociente.repository;

import com.example.ecociente.model.SindicoCooperativa;
import com.example.ecociente.model.SindicoHistoricoColeta;
import com.example.ecociente.model.SindicoSolicitacao;
import com.example.ecociente.model.StatusAgendamento;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Sem API de cooperativas e histórico: vazio. */
public final class SindicoColetaRepository {
    private static final SindicoColetaRepository INSTANCIA = new SindicoColetaRepository();

    // Cooperativas próximas e histórico de coletas ainda não existem em nenhuma API.
    private final List<SindicoCooperativa> cooperativas = Collections.emptyList();
    private final List<SindicoHistoricoColeta> historico = Collections.emptyList();
    private final Map<Integer, Integer> avaliacoes = new HashMap<>();
    private final List<SindicoSolicitacao> solicitacoes = new ArrayList<>();
    private int proximaSolicitacao = 1;

    private SindicoColetaRepository() { }

    public static SindicoColetaRepository getInstance() { return INSTANCIA; }
    public List<SindicoCooperativa> getCooperativas() { return cooperativas; }

    public SindicoCooperativa buscar(int id) {
        for (SindicoCooperativa cooperativa : cooperativas) {
            if (cooperativa.getId() == id) return cooperativa;
        }
        return null;
    }

    public List<SindicoHistoricoColeta> getHistorico() { return historico; }

    public List<SindicoSolicitacao> getSolicitacoes() {
        return Collections.unmodifiableList(solicitacoes);
    }

    public SindicoSolicitacao buscarSolicitacao(int id) {
        for (SindicoSolicitacao solicitacao : solicitacoes) {
            if (solicitacao.getId() == id) return solicitacao;
        }
        return null;
    }

    public SindicoSolicitacao criarSolicitacao(int cooperativaId, String inicio, boolean recorrente) {
        SindicoCooperativa cooperativa = buscar(cooperativaId);
        if (cooperativa == null) throw new IllegalArgumentException("Cooperativa não encontrada");
        SindicoSolicitacao nova = new SindicoSolicitacao(proximaSolicitacao++, cooperativaId,
                cooperativa.getNome(), inicio, recorrente, StatusAgendamento.AGENDADO);
        solicitacoes.add(0, nova);
        return nova;
    }

    public void cancelarSolicitacao(int id) {
        for (int i = 0; i < solicitacoes.size(); i++) {
            if (solicitacoes.get(i).getId() == id) {
                solicitacoes.set(i, solicitacoes.get(i).comStatus(StatusAgendamento.CANCELADO));
                return;
            }
        }
        throw new IllegalArgumentException("Solicitação não encontrada");
    }

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
