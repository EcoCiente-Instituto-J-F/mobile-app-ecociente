package com.example.ecociente.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class PreferenciasNotificacao {

    public static final String CHAVE_NOTIFICACOES_GERAIS = "notificacoesGerais";
    public static final String CHAVE_LEMBRETES_AVISOS = "lembretesAvisos";
    public static final String CHAVE_NOVAS_ATIVIDADES = "novasAtividades";
    public static final String CHAVE_DICAS_SUSTENTAVEIS = "dicasSustentaveis";

    // Grupo de cada tipo de aviso na tela de notificações; NENHUM só depende das "gerais".
    public enum Grupo {
        LEMBRETES_AVISOS,
        NOVAS_ATIVIDADES,
        DICAS_SUSTENTAVEIS,
        NENHUM
    }

    private Boolean notificacoesGerais;
    private Boolean lembretesAvisos;
    private Boolean novasAtividades;
    private Boolean dicasSustentaveis;

    public PreferenciasNotificacao() {}

    public boolean isNotificacoesGerais() {
        return notificacoesGerais == null || notificacoesGerais;
    }

    public void setNotificacoesGerais(Boolean notificacoesGerais) {
        this.notificacoesGerais = notificacoesGerais;
    }

    public boolean isLembretesAvisos() {
        return lembretesAvisos != null && lembretesAvisos;
    }

    public void setLembretesAvisos(Boolean lembretesAvisos) {
        this.lembretesAvisos = lembretesAvisos;
    }

    public boolean isNovasAtividades() {
        return novasAtividades == null || novasAtividades;
    }

    public void setNovasAtividades(Boolean novasAtividades) {
        this.novasAtividades = novasAtividades;
    }

    public boolean isDicasSustentaveis() {
        return dicasSustentaveis == null || dicasSustentaveis;
    }

    public void setDicasSustentaveis(Boolean dicasSustentaveis) {
        this.dicasSustentaveis = dicasSustentaveis;
    }

    // As "gerais" desligadas silenciam tudo; senão vale o interruptor do grupo.
    public boolean permite(Grupo grupo) {
        if (!isNotificacoesGerais()) {
            return false;
        }

        switch (grupo) {
            case LEMBRETES_AVISOS:
                return isLembretesAvisos();
            case NOVAS_ATIVIDADES:
                return isNovasAtividades();
            case DICAS_SUSTENTAVEIS:
                return isDicasSustentaveis();
            default:
                return true;
        }
    }
}
