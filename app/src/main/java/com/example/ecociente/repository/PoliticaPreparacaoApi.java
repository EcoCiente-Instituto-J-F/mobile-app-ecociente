package com.example.ecociente.repository;

final class PoliticaPreparacaoApi {
    private PoliticaPreparacaoApi() {}

    static boolean credenciaisProntas(String token, int usuarioId) {
        return token != null && !token.trim().isEmpty() && usuarioId > 0;
    }

    static boolean deveRepetir(int tentativasRealizadas, int maximoTentativas) {
        return tentativasRealizadas > 0 && tentativasRealizadas < maximoTentativas;
    }
}
