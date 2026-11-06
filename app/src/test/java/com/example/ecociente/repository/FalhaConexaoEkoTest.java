package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import org.junit.Test;

public class FalhaConexaoEkoTest {
    @Test
    public void preservaCausaDeFalhaAntesDoEnvio() {
        ConnectException causa = new ConnectException("connection refused");
        ChatRepository.FalhaConexaoAntesEnvio falha = new ChatRepository.FalhaConexaoAntesEnvio(causa);
        assertNotNull(falha.getCause());
        assertEquals(causa, falha.getCause());
    }

    @Test
    public void erroDeTimeoutPodeSerPreservadoComoFalhaDeConexao() {
        SocketTimeoutException causa = new SocketTimeoutException("connect timeout");
        ChatRepository.FalhaConexaoAntesEnvio falha = new ChatRepository.FalhaConexaoAntesEnvio(causa);
        assertEquals(causa, falha.getCause());
    }
}
