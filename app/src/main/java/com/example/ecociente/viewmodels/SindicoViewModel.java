package com.example.ecociente.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.ecociente.model.SindicoAviso;
import com.example.ecociente.model.SindicoDashboard;
import com.example.ecociente.repository.SindicoRepository;

import java.util.List;

public final class SindicoViewModel extends ViewModel {
    private final SindicoRepository repositorio = SindicoRepository.getInstance();

    public SindicoDashboard getDashboard() { return repositorio.getDashboard(); }
    public LiveData<List<SindicoAviso>> getAvisos() { return repositorio.observarAvisos(); }

    public void salvarRascunho(String titulo, String mensagem, String destinatario) {
        repositorio.salvarRascunho(titulo, mensagem, destinatario);
    }
}
