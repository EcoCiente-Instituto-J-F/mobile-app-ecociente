package com.example.ecociente.repository;

final class PoliticaValidadeSessao {
    private static final long INTERVALO_RENOVACAO_MS = 4 * 60_000L;

    private PoliticaValidadeSessao() {}

    static boolean deveRenovar(long emitidoEmMs, long agoraMs) {
        if (emitidoEmMs <= 0 || agoraMs < emitidoEmMs) {
            return true;
        }
        return agoraMs - emitidoEmMs >= INTERVALO_RENOVACAO_MS;
    }
}
