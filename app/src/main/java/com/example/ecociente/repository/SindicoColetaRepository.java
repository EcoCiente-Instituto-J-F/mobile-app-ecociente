package com.example.ecociente.repository;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.ecociente.model.SindicoCooperativa;
import com.example.ecociente.model.Solicitacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Guarda na memória o que a API devolveu: cooperativas da cidade e coletas do condomínio. */
public final class SindicoColetaRepository {
    private static final SindicoColetaRepository INSTANCIA = new SindicoColetaRepository();

    // Vêm de GET /api/cooperativas e de GET /api/v1/agendamentos.
    private volatile List<SindicoCooperativa> cooperativas = Collections.emptyList();
    private volatile List<Solicitacao> solicitacoes = Collections.emptyList();
    // Guarda localmente apenas a confirmação recebida da API, por usuário e agendamento.
    private static final String PREF_AVALIACOES = "avaliacoes_coletas_sindico";

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

    public boolean foiAvaliada(Context contexto, int agendamentoId) {
        return preferencias(contexto).getBoolean(chaveAvaliacao(contexto, agendamentoId), false);
    }

    public void marcarAvaliada(Context contexto, int agendamentoId) {
        preferencias(contexto).edit().putBoolean(chaveAvaliacao(contexto, agendamentoId), true).apply();
    }

    private static SharedPreferences preferencias(Context contexto) {
        return contexto.getApplicationContext().getSharedPreferences(PREF_AVALIACOES, Context.MODE_PRIVATE);
    }

    private static String chaveAvaliacao(Context contexto, int agendamentoId) {
        int usuarioId = AutenticacaoExternaRepository.usuarioIdSalvo(contexto);
        return "usuario_" + usuarioId + "_agendamento_" + agendamentoId;
    }
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
