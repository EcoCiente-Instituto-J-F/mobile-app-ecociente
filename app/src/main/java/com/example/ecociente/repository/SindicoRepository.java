package com.example.ecociente.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecociente.model.SindicoAviso;
import com.example.ecociente.model.SindicoDashboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Avisos criados no aparelho e o dashboard; sem API, começam vazios. */
public final class SindicoRepository {
    private static final SindicoRepository INSTANCIA = new SindicoRepository();

    private final MutableLiveData<List<SindicoAviso>> avisos = new MutableLiveData<>();
    private final SindicoDashboard dashboard = SindicoDashboard.vazio();

    private SindicoRepository() {
        avisos.setValue(Collections.emptyList());
    }

    public static SindicoRepository getInstance() { return INSTANCIA; }
    public SindicoDashboard getDashboard() { return dashboard; }
    public LiveData<List<SindicoAviso>> observarAvisos() { return avisos; }

    public void salvarRascunho(String titulo, String mensagem, String destinatario) {
        List<SindicoAviso> atuais = avisos.getValue();
        List<SindicoAviso> novos = atuais == null ? new ArrayList<>() : new ArrayList<>(atuais);
        novos.add(0, new SindicoAviso(titulo, mensagem, destinatario,
                "Síndico/Gestão", "Agora", "Rascunho", true, true));
        avisos.setValue(Collections.unmodifiableList(novos));
    }
}
