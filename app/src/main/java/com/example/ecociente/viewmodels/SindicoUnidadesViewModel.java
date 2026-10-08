package com.example.ecociente.viewmodels;

import androidx.lifecycle.ViewModel;

import com.example.ecociente.model.SindicoDadosCondominio;
import com.example.ecociente.model.SindicoApartamento;
import com.example.ecociente.model.SindicoItemUnidade;
import com.example.ecociente.repository.SindicoUnidadesRepository;

import java.util.List;

public final class SindicoUnidadesViewModel extends ViewModel {
    private final SindicoUnidadesRepository repositorio = SindicoUnidadesRepository.getInstance();

    public SindicoDadosCondominio getDados() { return repositorio.getDados(); }
    public void salvarDados(SindicoDadosCondominio dados) { repositorio.salvarDados(dados); }
    public String getFotoUri() { return repositorio.getFotoUri(); }
    public void salvarFotoUri(String uri) { repositorio.salvarFotoUri(uri); }
    public List<SindicoItemUnidade> getBlocos() { return repositorio.getBlocos(); }
    public List<SindicoItemUnidade> getAreas() { return repositorio.getAreas(); }
    public SindicoItemUnidade getBloco(int blocoId) { return repositorio.getBloco(blocoId); }
    public List<SindicoApartamento> getApartamentos(int blocoId) {
        return repositorio.getApartamentos(blocoId);
    }
    public SindicoApartamento getApartamento(int apartamentoId) {
        return repositorio.getApartamento(apartamentoId);
    }
    public boolean existeNumero(int blocoId, int apartamentoId, String numero) {
        return repositorio.existeNumero(blocoId, apartamentoId, numero);
    }
    public void salvarApartamento(int apartamentoId, int blocoId, String numero,
                                 String morador, String telefone) {
        repositorio.salvarApartamento(apartamentoId, blocoId, numero, morador, telefone);
    }
    public void excluirApartamento(int apartamentoId) {
        repositorio.excluirApartamento(apartamentoId);
    }
    public void salvarBloco(int id, String nome, int quantidade) {
        repositorio.salvarBloco(id, nome, quantidade);
    }
    public void salvarArea(int id, String nome, String local) {
        repositorio.salvarArea(id, nome, local);
    }
    public void excluirBloco(int id) { repositorio.excluirBloco(id); }
    public void excluirArea(int id) { repositorio.excluirArea(id); }
}
