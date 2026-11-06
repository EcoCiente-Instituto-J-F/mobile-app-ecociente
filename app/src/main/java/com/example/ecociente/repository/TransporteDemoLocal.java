package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import com.example.ecociente.model.StatusAgendamento;

public final class TransporteDemoLocal {
    public interface Alterador {
        int alterar(String token, int agendamentoId, StatusAgendamento status);
    }
    private static volatile Alterador alterador;
    private TransporteDemoLocal() {}
    public static void instalar(@NonNull Alterador funcao) { alterador = funcao; }
    public static int alterarStatus(@NonNull String token, int id, @NonNull StatusAgendamento status) {
        Alterador atual = alterador;
        return atual == null ? 503 : atual.alterar(token, id, status);
    }
}
