package com.example.ecociente.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecociente.model.SindicoAviso;
import com.example.ecociente.model.SindicoDashboard;
import com.example.ecociente.model.SindicoPosicaoRanking;
import com.example.ecociente.repository.PerfilApiRepository;
import com.example.ecociente.repository.RankingExternoRepository;
import com.example.ecociente.repository.SindicoRepository;

import java.util.List;

public final class SindicoViewModel extends AndroidViewModel {
    private final SindicoRepository repositorio = SindicoRepository.getInstance();
    private final MutableLiveData<List<SindicoPosicaoRanking>> rankingTorres = new MutableLiveData<>();

    public SindicoViewModel(@NonNull Application aplicacao) {
        super(aplicacao);
    }

    // Só emite depois da consulta; null quando a API não respondeu (sem JWT, 403, fora do ar).
    public LiveData<List<SindicoPosicaoRanking>> getRankingTorres() { return rankingTorres; }

    public void carregarRankingTorres(int limite) {
        new RankingExternoRepository()
                .listarTorres(
                        getApplication(),
                        PerfilApiRepository.condominioId(getApplication()),
                        limite,
                        rankingTorres::setValue);
    }

    public SindicoDashboard getDashboard() { return repositorio.getDashboard(); }
    public LiveData<List<SindicoAviso>> getAvisos() { return repositorio.observarAvisos(); }

    public void salvarRascunho(String titulo, String mensagem, String destinatario) {
        repositorio.salvarRascunho(titulo, mensagem, destinatario);
    }
}
