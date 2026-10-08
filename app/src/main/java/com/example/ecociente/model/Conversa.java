package com.example.ecociente.model;

import androidx.annotation.NonNull;
import java.util.Arrays;
import java.util.List;

public final class Conversa {

    public final String nome;
    public final String ultimaMensagem;
    public final String hora;
    public final boolean naoLida;
    public final boolean arquivada;

    private Conversa(String nome, String ultimaMensagem, String hora, boolean naoLida, boolean arquivada) {
        this.nome = nome;
        this.ultimaMensagem = ultimaMensagem;
        this.hora = hora;
        this.naoLida = naoLida;
        this.arquivada = arquivada;
    }

    @NonNull
    public static List<Conversa> exemplos() {
        return Arrays.asList(
                new Conversa("Condomínio Raio de Luz", "Quando vão passar?", "08:30", true, false),
                new Conversa("Residencial Jardim das Flores", "Podem confirmar a coleta de amanhã?", "08:05", true, false),
                new Conversa("Condomínio Vila Verde", "Obrigado pela coleta de hoje!", "Ontem", false, false),
                new Conversa("Edifício Solar", "Temos 12 sacos de recicláveis para amanhã.", "Ontem", false, false),
                new Conversa("Residencial Aurora", "Vocês aceitam vidro também?", "Seg", true, false),
                new Conversa("Condomínio Parque das Árvores", "Podemos mudar o horário para 10h?", "Seg", false, false),
                new Conversa("Condomínio Bela Vista", "Combinado, até quinta.", "05/10", false, true),
                new Conversa("Residencial Primavera", "Muito obrigada pelo atendimento.", "02/10", false, true));
    }
}
