package com.example.ecociente.model;

import androidx.annotation.NonNull;
import java.util.Arrays;
import java.util.List;

public final class Conversa {

    public final String nome;
    public final String ultimaMensagem;
    public final String hora;
    public final int naoLidas;
    public final boolean arquivada;

    private Conversa(String nome, String ultimaMensagem, String hora, int naoLidas, boolean arquivada) {
        this.nome = nome;
        this.ultimaMensagem = ultimaMensagem;
        this.hora = hora;
        this.naoLidas = naoLidas;
        this.arquivada = arquivada;
    }

    public boolean temNaoLidas() {

        return naoLidas > 0;
    }

    @NonNull
    public static List<Conversa> exemplos() {
        return Arrays.asList(
                new Conversa("Condomínio Raio de Luz", "Quando vão passar?", "08:30", 2, false),
                new Conversa("Residencial Jardim das Flores", "Podem confirmar a coleta de amanhã?", "08:05", 1, false),
                new Conversa("Condomínio Vila Verde", "Obrigado pela coleta de hoje!", "Ontem", 0, false),
                new Conversa("Edifício Solar", "Temos 12 sacos de recicláveis para amanhã.", "Ontem", 0, false),
                new Conversa("Residencial Aurora", "Vocês aceitam vidro também?", "Seg", 3, false),
                new Conversa("Condomínio Parque das Árvores", "Podemos mudar o horário para 10h?", "Seg", 0, false),
                new Conversa("Condomínio Bela Vista", "Combinado, até quinta.", "05/10", 0, true),
                new Conversa("Residencial Primavera", "Muito obrigada pelo atendimento.", "02/10", 0, true));
    }
}
