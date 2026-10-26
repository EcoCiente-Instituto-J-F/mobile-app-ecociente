package com.example.ecociente.viewmodels;

import androidx.lifecycle.ViewModel;

import com.example.ecociente.model.SindicoCooperativa;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.repository.SindicoColetaRepository;

import java.util.List;

public final class SindicoColetaViewModel extends ViewModel {
    private final SindicoColetaRepository repositorio = SindicoColetaRepository.getInstance();

    public List<SindicoCooperativa> getCooperativas() { return repositorio.getCooperativas(); }
    public void definirCooperativas(List<SindicoCooperativa> cooperativas) { repositorio.definirCooperativas(cooperativas); }
    public SindicoCooperativa buscar(int id) { return repositorio.buscar(id); }
    public boolean foiAvaliada(int agendamentoId) { return repositorio.foiAvaliada(agendamentoId); }
    public void marcarAvaliada(int agendamentoId) { repositorio.marcarAvaliada(agendamentoId); }
    public List<Solicitacao> getSolicitacoes() { return repositorio.getSolicitacoes(); }
    public void definirSolicitacoes(List<Solicitacao> solicitacoes) { repositorio.definirSolicitacoes(solicitacoes); }
    public void adicionarSolicitacao(Solicitacao solicitacao) { repositorio.adicionarSolicitacao(solicitacao); }
}
