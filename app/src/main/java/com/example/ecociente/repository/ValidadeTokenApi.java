package com.example.ecociente.repository;

final class ValidadeTokenApi {

    private static final long FOLGA_MS = 60_000L;

    private ValidadeTokenApi() {
    }

    static long calcularVencimento(long agoraMs, long duracaoSegundos) {
        if (agoraMs < 0 || duracaoSegundos <= 0 ||
                duracaoSegundos > (Long.MAX_VALUE - agoraMs) / 1_000L) {
            return 0L;
        }
        return agoraMs + duracaoSegundos * 1_000L;
    }

    static boolean deveRenovar(long vencimentoMs, long agoraMs) {
        return vencimentoMs <= 0L ||
                vencimentoMs <= agoraMs ||
                vencimentoMs - agoraMs <= FOLGA_MS;
    }
}
