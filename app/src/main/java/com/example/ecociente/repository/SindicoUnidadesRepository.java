package com.example.ecociente.repository;

import com.example.ecociente.model.SindicoDadosCondominio;
import com.example.ecociente.model.SindicoApartamento;
import com.example.ecociente.model.SindicoItemUnidade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Sem API de unidades: começa vazio e o que o síndico cadastra fica em memória. */
public final class SindicoUnidadesRepository {
    private static final SindicoUnidadesRepository INSTANCIA = new SindicoUnidadesRepository();

    private SindicoDadosCondominio dados = new SindicoDadosCondominio(true, "", "", "", "", "");
    private String fotoUri;
    private final List<SindicoItemUnidade> blocos = new ArrayList<>();
    private final List<SindicoItemUnidade> areas = new ArrayList<>();
    private final List<SindicoApartamento> apartamentos = new ArrayList<>();
    private int proximoId = 1;
    private int proximoApartamentoId = 1;

    private SindicoUnidadesRepository() { }

    public static SindicoUnidadesRepository getInstance() { return INSTANCIA; }
    public SindicoDadosCondominio getDados() { return dados; }
    public void salvarDados(SindicoDadosCondominio novosDados) { dados = novosDados; }
    public String getFotoUri() { return fotoUri; }
    public void salvarFotoUri(String uri) { fotoUri = uri; }
    public List<SindicoItemUnidade> getBlocos() {
        return Collections.unmodifiableList(new ArrayList<>(blocos));
    }
    public List<SindicoItemUnidade> getAreas() {
        return Collections.unmodifiableList(new ArrayList<>(areas));
    }

    public SindicoItemUnidade getBloco(int blocoId) {
        for (SindicoItemUnidade bloco : blocos) {
            if (bloco.getId() == blocoId) return bloco;
        }
        return null;
    }

    public List<SindicoApartamento> getApartamentos(int blocoId) {
        List<SindicoApartamento> resultado = new ArrayList<>();
        for (SindicoApartamento apartamento : apartamentos) {
            if (apartamento.getBlocoId() == blocoId) resultado.add(apartamento);
        }
        return Collections.unmodifiableList(resultado);
    }

    public SindicoApartamento getApartamento(int apartamentoId) {
        for (SindicoApartamento apartamento : apartamentos) {
            if (apartamento.getId() == apartamentoId) return apartamento;
        }
        return null;
    }

    public boolean existeNumero(int blocoId, int apartamentoId, String numero) {
        for (SindicoApartamento apartamento : apartamentos) {
            if (apartamento.getBlocoId() == blocoId && apartamento.getId() != apartamentoId
                    && apartamento.getNumero().equalsIgnoreCase(numero)) return true;
        }
        return false;
    }

    public void salvarApartamento(int apartamentoId, int blocoId, String numero,
                                 String morador, String telefone) {
        SindicoApartamento novo = new SindicoApartamento(
                apartamentoId < 0 ? proximoApartamentoId++ : apartamentoId,
                blocoId, numero, morador, telefone);
        if (apartamentoId >= 0) {
            for (int i = 0; i < apartamentos.size(); i++) {
                if (apartamentos.get(i).getId() == apartamentoId) {
                    apartamentos.set(i, novo);
                    return;
                }
            }
            throw new IllegalArgumentException("Unidade não encontrada");
        }
        apartamentos.add(novo);
    }

    public void excluirApartamento(int apartamentoId) {
        apartamentos.removeIf(apartamento -> apartamento.getId() == apartamentoId);
    }

    public void salvarBloco(int id, String nome, int quantidade) {
        salvar(blocos, id, nome, quantidade + " unidades");
    }

    public void salvarArea(int id, String nome, String local) {
        salvar(areas, id, nome, local);
    }

    private void salvar(List<SindicoItemUnidade> itens, int id, String nome, String detalhe) {
        SindicoItemUnidade novo = new SindicoItemUnidade(id < 0 ? proximoId++ : id,
                nome, detalhe);
        if (id >= 0) {
            for (int i = 0; i < itens.size(); i++) {
                if (itens.get(i).getId() == id) {
                    itens.set(i, novo);
                    return;
                }
            }
            throw new IllegalArgumentException("Item não encontrado");
        }
        itens.add(novo);
    }

    public void excluirBloco(int id) {
        excluir(blocos, id);
        apartamentos.removeIf(apartamento -> apartamento.getBlocoId() == id);
    }
    public void excluirArea(int id) { excluir(areas, id); }

    private void excluir(List<SindicoItemUnidade> itens, int id) {
        itens.removeIf(item -> item.getId() == id);
    }
}
