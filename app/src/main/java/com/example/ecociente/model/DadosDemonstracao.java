package com.example.ecociente.model;

// Liga os dados de exemplo da agenda, das solicitações e do feed (as APIs ainda não devolvem esses dados).
// Mude para false para ver as telas como ficam sem dados.
public final class DadosDemonstracao {

    public static final boolean ATIVO = true;

    // O síndico nunca vê exemplo: o que a API não entrega aparece vazio.
    public static boolean ativoPara(String perfil) {
        return ATIVO && !"SINDICO".equals(perfil);
    }

    private DadosDemonstracao() {}
}
