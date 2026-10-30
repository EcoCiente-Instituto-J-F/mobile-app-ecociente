package com.example.ecociente.repository;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

final class PoliticaFallbackLogin {
    private PoliticaFallbackLogin() {}

    static boolean tentarRenderAposHttp(int codigo) {
        return codigo == 502 || codigo == 503 || codigo == 504;
    }

    // A API recusou a conta: senha errada, requisição inválida ou "Usuário não encontrado" (404 com o
    // código da própria API, para um 404 de rota inexistente não valer como recusa).
    static boolean credenciaisRecusadas(int codigo, String corpo) {
        if (codigo == 400 || codigo == 401 || codigo == 403) {
            return true;
        }

        return codigo == 404 && corpo != null && corpo.contains("RECURSO_NAO_ENCONTRADO");
    }

    static boolean tentarRenderAposErro(IOException erro) {
        return erro instanceof SocketTimeoutException
                || erro instanceof SocketException
                || erro instanceof UnknownHostException;
    }
}
