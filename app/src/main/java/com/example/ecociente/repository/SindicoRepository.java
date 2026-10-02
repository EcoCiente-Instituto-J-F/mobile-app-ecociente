package com.example.ecociente.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecociente.model.SindicoAviso;
import com.example.ecociente.model.SindicoDashboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Fonte de demonstração. Substituir esta classe pela API */
public final class SindicoRepository {
    private static final SindicoRepository INSTANCIA = new SindicoRepository();

    private final MutableLiveData<List<SindicoAviso>> avisos = new MutableLiveData<>();
    private final SindicoDashboard dashboard = new SindicoDashboard(
            "Júlio", "Condomínio Residencial Jardim das Flores", 68, 248, 86,
            -12, 18, 6,
            new int[]{90, 130, 175, 185, 300},
            new String[]{"01/mai", "08/mai", "15/mai", "22/mai", "29/mai"},
            new String[]{"Seg", "Ter", "Qua", "Qui", "Sex"},
            new String[]{"12/05", "13/05", "14/05", "15/05", "16/05"},
            new boolean[]{false, true, false, false, false},
            new String[]{"Bloco B", "Bloco E", "Bloco C", "Bloco A", "Bloco D"},
            new String[]{"BC", "BE", "CC", "BA", "BD"},
            new int[]{1380, 1310, 1275, 1190, 1115});

    private SindicoRepository() {
        List<SindicoAviso> exemplos = new ArrayList<>();
        exemplos.add(new SindicoAviso("Mudança no horário da coleta de recicláveis",
                "A partir da próxima semana, a coleta de recicláveis no condomínio será realizada às terças e quintas-feiras.",
                "Todos os moradores", "Síndico/Gestão", "Há 2 dias", "Coleta", false, true));
        exemplos.add(new SindicoAviso("Dicas de separação de resíduos eletrônicos",
                "A cooperativa parceira compartilha algumas dicas importantes para o descarte correto de eletrônicos.",
                "Todos os moradores", "Cooperativa VerdeMais", "Há 3 dias", "Orientação", false, false));
        exemplos.add(new SindicoAviso("Manutenção na área de coleta",
                "Informamos que, na próxima segunda-feira, a área de coleta passará por manutenção. Pedimos que sigam a sinalização no local.",
                "Todos os moradores", "Síndico/Gestão", "10 de mai.", "Manutenção", false, true));
        exemplos.add(new SindicoAviso("Campanha de reciclagem de garrafas PET",
                "A cooperativa inicia uma nova campanha de reciclagem de garrafas PET. Separe e entregue suas garrafas no ponto de coleta.",
                "Todos os moradores", "Cooperativa VerdeMais", "8 de mai.", "Campanha", false, false));
        exemplos.add(new SindicoAviso("Reunião do conselho do condomínio",
                "Informamos que a reunião do conselho será realizada no dia 12 de maio, às 19h, no salão de festas.",
                "Todos os moradores", "Síndico/Gestão", "5 de mai.", "Aviso geral", false, false));
        exemplos.add(new SindicoAviso("Novo ponto de descarte de óleo de cozinha",
                "O recipiente para óleo usado está disponível próximo à entrada da área de coleta. Guarde o óleo frio em uma garrafa bem fechada antes de depositá-lo.",
                "Todos os moradores", "Síndico/Gestão", "3 de mai.", "Orientação", false, false));
        exemplos.add(new SindicoAviso("Mutirão de reciclagem no sábado",
                "Neste sábado, haverá um mutirão para recolher papel, plástico e metal. A equipe estará no pátio do condomínio das 9h às 12h.",
                "Todos os moradores", "Síndico/Gestão", "1 de mai.", "Campanha", false, false));
        exemplos.add(new SindicoAviso("Como descartar pilhas e baterias",
                "Pilhas e baterias não devem ir para a coleta comum. Entregue esses materiais no recipiente identificado na portaria.",
                "Todos os moradores", "Cooperativa VerdeMais", "28 de abr.", "Orientação", false, false));
        avisos.setValue(Collections.unmodifiableList(exemplos));
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
