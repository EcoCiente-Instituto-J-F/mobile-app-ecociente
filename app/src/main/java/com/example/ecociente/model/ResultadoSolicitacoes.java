package com.example.ecociente.model;

import androidx.annotation.NonNull;
import java.util.Collections;
import java.util.List;

public final class ResultadoSolicitacoes {

    public enum Tipo {
        SUCESSO,
        SESSAO_EXPIRADA,
        ERRO
    }

    private final Tipo tipo;
    private final List<Solicitacao> itens;
    private final boolean ultimaPagina;

    private ResultadoSolicitacoes(Tipo tipo, List<Solicitacao> itens, boolean ultimaPagina) {
        this.tipo = tipo;
        this.itens = itens;
        this.ultimaPagina = ultimaPagina;
    }

    @NonNull
    public static ResultadoSolicitacoes sucesso(
            @NonNull List<Solicitacao> itens, boolean ultimaPagina) {
        return new ResultadoSolicitacoes(Tipo.SUCESSO, itens, ultimaPagina);
    }

    @NonNull
    public static ResultadoSolicitacoes sessaoExpirada() {
        return new ResultadoSolicitacoes(Tipo.SESSAO_EXPIRADA, Collections.emptyList(), true);
    }

    @NonNull
    public static ResultadoSolicitacoes erro() {
        return new ResultadoSolicitacoes(Tipo.ERRO, Collections.emptyList(), true);
    }

    @NonNull
    public Tipo getTipo() {
        return tipo;
    }

    @NonNull
    public List<Solicitacao> getItens() {
        return itens;
    }

    public boolean ehUltimaPagina() {
        return ultimaPagina;
    }
}
