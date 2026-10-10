package com.example.ecociente;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.Coletas;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.model.StatusAgendamento;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class ColetasTest {

    private Solicitacao coleta(int id, String inicio, StatusAgendamento status) {
        return new Solicitacao(id, 1, 1, inicio, inicio, status, false);
    }

    @Test
    public void canceladasERecusadasNaoEstaoAtivas() {
        assertFalse(Coletas.estaAtiva(coleta(1, "2026-05-01T08:00:00", StatusAgendamento.CANCELADO)));
        assertFalse(Coletas.estaAtiva(coleta(2, "2026-05-01T08:00:00", StatusAgendamento.RECUSADO)));
        assertTrue(Coletas.estaAtiva(coleta(3, "2026-05-01T08:00:00", StatusAgendamento.CONFIRMADO)));
        assertTrue(Coletas.estaAtiva(coleta(4, "2026-05-01T08:00:00", StatusAgendamento.AGENDADO)));
    }

    @Test
    public void ativasDoMesFiltraOrdenaEIgnoraOutrosMeses() {
        List<Solicitacao> todas =
                Arrays.asList(
                        coleta(1, "2026-05-24T08:00:00", StatusAgendamento.CONFIRMADO),
                        coleta(2, "2026-05-01T08:00:00", StatusAgendamento.AGENDADO),
                        coleta(3, "2026-06-02T08:00:00", StatusAgendamento.AGENDADO),
                        coleta(4, "2026-05-12T08:00:00", StatusAgendamento.CANCELADO));

        List<Solicitacao> maio = Coletas.ativasDoMes(todas, 2026, java.util.Calendar.MAY);

        assertEquals(2, maio.size());
        assertEquals(2, maio.get(0).getId());
        assertEquals(1, maio.get(1).getId());
    }

    @Test
    public void diasDoMesMarcaSoOsDiasAtivos() {
        List<Solicitacao> todas =
                Arrays.asList(
                        coleta(1, "2026-05-24T08:00:00", StatusAgendamento.CONFIRMADO),
                        coleta(2, "2026-05-12T08:00:00", StatusAgendamento.RECUSADO));

        Set<Integer> dias = Coletas.diasDoMes(todas, 2026, java.util.Calendar.MAY);

        assertEquals(1, dias.size());
        assertTrue(dias.contains(24));
    }
}
