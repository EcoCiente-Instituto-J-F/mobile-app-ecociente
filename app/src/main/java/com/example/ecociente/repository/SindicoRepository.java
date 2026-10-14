package com.example.ecociente.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecociente.model.SindicoAviso;
import com.example.ecociente.model.SindicoDashboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Sem dados de exemplo: cada metodo abaixo aguarda a API correspondente. */
public final class SindicoRepository {
    private static final SindicoRepository INSTANCIA = new SindicoRepository();

    private final MutableLiveData<List<SindicoAviso>> avisos = new MutableLiveData<>();

    // API: GET /api/v1/sindico/dashboard/resumo e /dashboard/desempenho (contrato-dashboard-sindico.md)
    private final SindicoDashboard dashboard = new SindicoDashboard(
            "", "", 0, 0, 0,
            0, 0, 0,
            new int[0], new String[0],
            new String[0], new String[0], new int[0]);

    private SindicoRepository() {
        // API: GET /api/v1/avisos?condominioId= (ds-notificacoes-api)
        avisos.setValue(Collections.emptyList());
    }

    public static SindicoRepository getInstance() { return INSTANCIA; }
    public SindicoDashboard getDashboard() { return dashboard; }
    public LiveData<List<SindicoAviso>> observarAvisos() { return avisos; }

    // API: POST /api/v1/avisos (hoje so guarda em memoria como rascunho)
    public void salvarRascunho(String titulo, String mensagem, String destinatario) {
        List<SindicoAviso> atuais = avisos.getValue();
        List<SindicoAviso> novos = atuais == null ? new ArrayList<>() : new ArrayList<>(atuais);
        novos.add(0, new SindicoAviso(titulo, mensagem, destinatario,
                "Síndico/Gestão", "Agora", "Rascunho", true, true));
        avisos.setValue(Collections.unmodifiableList(novos));
    }
}
