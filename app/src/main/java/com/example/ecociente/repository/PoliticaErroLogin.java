package com.example.ecociente.repository;

final class PoliticaErroLogin {
    private PoliticaErroLogin() {}

    // A API recusou a conta: senha errada, requisição inválida ou "Usuário não encontrado" (404 com o
    // código da própria API, para um 404 de rota inexistente não valer como recusa).
    static boolean credenciaisRecusadas(int codigo, String corpo) {
        if (codigo == 400 || codigo == 401 || codigo == 403) {
            return true;
        }

        return codigo == 404 && corpo != null && corpo.contains("RECURSO_NAO_ENCONTRADO");
    }
}
