package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

// Conexão para host fora da lista da demo: tudo lança IOException, como uma rede sem rota. Garante que nada sai do aparelho.
final class ConexaoRecusada extends ConexaoBasica {

    ConexaoRecusada(@NonNull URL url) {
        super(url);
    }

    @NonNull
    private IOException recusa() {
        return new IOException("A demonstração não acessa " + url.getHost());
    }

    @Override
    public void connect() throws IOException {
        throw recusa();
    }

    @Override
    public void disconnect() {}

    @Override
    public int getResponseCode() throws IOException {
        throw recusa();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        throw recusa();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw recusa();
    }

    @Override
    public InputStream getErrorStream() {
        return null;
    }
}
