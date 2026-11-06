package com.example.ecociente.repository;

public final class PoliticaPreparacaoApi {
    private PoliticaPreparacaoApi() {}

    public static boolean credenciaisProntas(String token, int usuarioId) {
        return token != null && !token.trim().isEmpty() && usuarioId > 0;
    }

    public static boolean deveRepetir(int tentativasRealizadas, int maximoTentativas) {
        return tentativasRealizadas > 0 && tentativasRealizadas < maximoTentativas;
    }
}
