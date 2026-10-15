package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.model.StatusAgendamento;
import java.util.ArrayList;
import java.util.List;

public final class SolicitacoesExemplosRepository {

    private static final SolicitacoesExemplosRepository INSTANCIA = new SolicitacoesExemplosRepository();

    private final List<Solicitacao> solicitacoes = new ArrayList<>(Solicitacao.exemplos());

    private SolicitacoesExemplosRepository() {}

    @NonNull
    public static SolicitacoesExemplosRepository getInstance() {
        return INSTANCIA;
    }

    @NonNull
    public synchronized List<Solicitacao> listar(@NonNull StatusAgendamento status) {

        List<Solicitacao> filtradas = new ArrayList<>();

        for (Solicitacao solicitacao : solicitacoes) {
            if (solicitacao.getStatus() == status) {
                filtradas.add(solicitacao);
            }
        }

        return filtradas;
    }

    public synchronized void alterarStatus(int id, @NonNull StatusAgendamento status) {

        for (int i = 0; i < solicitacoes.size(); i++) {
            if (solicitacoes.get(i).getId() == id) {
                solicitacoes.set(i, solicitacoes.get(i).comStatus(status));
                return;
            }
        }
    }
}
