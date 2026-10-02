package com.example.ecociente.viewmodels;

import androidx.lifecycle.ViewModel;

import com.example.ecociente.model.SindicoCooperativa;
import com.example.ecociente.model.SindicoHistoricoColeta;
import com.example.ecociente.repository.SindicoColetaRepository;

import java.util.List;

public final class SindicoColetaViewModel extends ViewModel {
    private final SindicoColetaRepository repositorio = SindicoColetaRepository.getInstance();

    public List<SindicoCooperativa> getCooperativas() { return repositorio.getCooperativas(); }
    public SindicoCooperativa buscar(int id) { return repositorio.buscar(id); }
    public List<SindicoHistoricoColeta> getHistorico() { return repositorio.getHistorico(); }
    public int getAvaliacao(int coletaId) { return repositorio.getAvaliacao(coletaId); }
    public void avaliar(int coletaId, int nota) { repositorio.avaliar(coletaId, nota); }
}
