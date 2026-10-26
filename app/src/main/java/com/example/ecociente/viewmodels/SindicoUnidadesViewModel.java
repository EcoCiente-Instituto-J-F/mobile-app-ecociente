package com.example.ecociente.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.ecociente.model.SindicoDadosCondominio;
import com.example.ecociente.model.SindicoItemUnidade;
import com.example.ecociente.repository.SindicoUnidadesRepository;

import java.util.List;

public final class SindicoUnidadesViewModel extends AndroidViewModel {
    private final SindicoUnidadesRepository repositorio = SindicoUnidadesRepository.getInstance();

    public SindicoUnidadesViewModel(@NonNull Application aplicacao) {
        super(aplicacao);

        repositorio.carregar(aplicacao);
    }

    public SindicoDadosCondominio getDados() { return repositorio.getDados(); }
    public void salvarDados(SindicoDadosCondominio dados) { repositorio.salvarDados(dados); }
    public void aplicarDadosDaApi(SindicoDadosCondominio api) { repositorio.aplicarDadosDaApi(api); }
    public String getFotoUri() { return repositorio.getFotoUri(); }
    public void salvarFotoUri(String uri) { repositorio.salvarFotoUri(uri); }
    public List<SindicoItemUnidade> getAreas() { return repositorio.getAreas(); }
    public void salvarArea(int id, String nome, String local) {
        repositorio.salvarArea(id, nome, local);
    }
    public void excluirArea(int id) { repositorio.excluirArea(id); }
}
