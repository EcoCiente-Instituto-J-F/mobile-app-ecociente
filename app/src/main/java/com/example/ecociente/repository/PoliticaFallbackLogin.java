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

    static boolean tentarRenderAposErro(IOException erro) {
        return erro instanceof SocketTimeoutException
                || erro instanceof SocketException
                || erro instanceof UnknownHostException;
    }
}
