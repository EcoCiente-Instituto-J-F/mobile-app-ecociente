package com.example.ecociente.repository;

import com.example.ecociente.model.SindicoCooperativa;
import com.example.ecociente.model.Solicitacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/** Guarda na memória o que a API devolveu: cooperativas da cidade e coletas do condomínio. */
public final class SindicoColetaRepository {
    private static final SindicoColetaRepository INSTANCIA = new SindicoColetaRepository();

    // Vêm de GET /api/cooperativas e de GET /api/v1/agendamentos.
    private volatile List<SindicoCooperativa> cooperativas = Collections.emptyList();
    private volatile List<Solicitacao> solicitacoes = Collections.emptyList();
    // A listagem de coletas não diz quais já foram avaliadas: guarda as avaliadas nesta sessão.
    private final Set<Integer> avaliadas = Collections.synchronizedSet(new HashSet<>());

    private SindicoColetaRepository() { }

    public static SindicoColetaRepository getInstance() { return INSTANCIA; }
    public List<SindicoCooperativa> getCooperativas() { return cooperativas; }
    public void definirCooperativas(List<SindicoCooperativa> novas) {
        cooperativas = Collections.unmodifiableList(new ArrayList<>(novas));
    }

    public SindicoCooperativa buscar(int id) {
        for (SindicoCooperativa cooperativa : cooperativas) {
            if (cooperativa.getId() == id) return cooperativa;
        }
        return null;
    }

    public boolean foiAvaliada(int agendamentoId) { return avaliadas.contains(agendamentoId); }
    public void marcarAvaliada(int agendamentoId) { avaliadas.add(agendamentoId); }
    public List<Solicitacao> getSolicitacoes() { return solicitacoes; }
    public void definirSolicitacoes(List<Solicitacao> novas) {
        solicitacoes = Collections.unmodifiableList(new ArrayList<>(novas));
    }

    // Coleta recém-criada, antes de a próxima consulta à API trazê-la de volta.
    public void adicionarSolicitacao(Solicitacao nova) {
        List<Solicitacao> atualizadas = new ArrayList<>(solicitacoes);
        atualizadas.add(0, nova);
        definirSolicitacoes(atualizadas);
    }
}
