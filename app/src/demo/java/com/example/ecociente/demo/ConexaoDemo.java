package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

// No lugar da conexão de rede: junta o corpo que o app envia, espera um pouco (a demo parece falar com um servidor) e entrega o pedido ao TratadorDemo uma única vez, sob trava, mesmo que o app chame getResponseCode, getInputStream e getResponseCode de novo, ou de duas threads.
public final class ConexaoDemo extends ConexaoBasica {

    // Atraso do app; os testes passam 0.
    public static final long ATRASO_MS = 300;

    private static final Logger LOG = Logger.getLogger("EcoCienteDemo");

    private final TratadorDemo tratador;
    private final long atrasoMs;
    private final ByteArrayOutputStream corpoEnviado = new ByteArrayOutputStream();
    private final Object trava = new Object();

    // Só muda dentro de `trava`; volatile para a leitura sem trava (getErrorStream) enxergar.
    private volatile RespostaDemo resposta;

    public ConexaoDemo(@NonNull URL url, @NonNull TratadorDemo tratador, long atrasoMs) {
        super(url);
        this.tratador = tratador;
        this.atrasoMs = atrasoMs;
    }

    @Override
    public void connect() {
        connected = true;
    }

    @Override
    public void disconnect() {}

    @NonNull
    @Override
    public OutputStream getOutputStream() throws IOException {
        if (!doOutput) {
            throw new ProtocolException("cannot write to a URLConnection if doOutput=false - call setDoOutput(true)");
        }

        if (resposta != null) {
            throw new ProtocolException("Cannot write output after reading input.");
        }

        connected = true;

        return corpoEnviado;
    }

    @Override
    public int getResponseCode() throws IOException {
        return responder().codigo;
    }

    @NonNull
    @Override
    public String getResponseMessage() throws IOException {
        return mensagem(responder().codigo);
    }

    @NonNull
    @Override
    public InputStream getInputStream() throws IOException {
        RespostaDemo recebida = responder();

        if (recebida.codigo == 404 || recebida.codigo == 410) {
            throw new FileNotFoundException(url.toString());
        }

        if (recebida.codigo >= 400) {
            throw new IOException("Server returned HTTP response code: " + recebida.codigo + " for URL: " + url);
        }

        return new ByteArrayInputStream(recebida.corpo);
    }

    // Só há corpo de erro em status >= 400; antes de o pedido sair, ou com sucesso, é null.
    @Nullable
    @Override
    public InputStream getErrorStream() {
        RespostaDemo recebida = resposta;

        if (recebida == null || recebida.codigo < 400) {
            return null;
        }

        return new ByteArrayInputStream(recebida.corpo);
    }

    @Nullable
    @Override
    public String getHeaderField(@Nullable String nome) {
        if (nome == null) {
            return null;
        }

        try {
            RespostaDemo recebida = responder();

            if ("Content-Type".equalsIgnoreCase(nome)) {
                return "application/json; charset=utf-8";
            }

            if ("Content-Length".equalsIgnoreCase(nome)) {
                return String.valueOf(recebida.corpo.length);
            }

        } catch (IOException erro) {
            LOG.log(Level.FINE, "Pedido da demo falhou ao ler cabeçalho " + nome, erro);
        }

        return null;
    }

    @NonNull
    private RespostaDemo responder() throws IOException {
        synchronized (trava) {
            if (resposta != null) {
                return resposta;
            }

            marcarEnviado();
            connected = true;

            if (atrasoMs > 0) {
                try {
                    Thread.sleep(atrasoMs);

                } catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                    throw new InterruptedIOException("Pedido da demo interrompido");
                }
            }

            RespostaDemo nova;

            try {
                nova =
                        tratador.tratar(
                                getRequestMethod(),
                                url.toString(),
                                getRequestProperty("Authorization"),
                                corpoEnviado.toByteArray());

            } catch (RuntimeException erro) {
                LOG.log(Level.SEVERE, "Tratador da demo falhou em " + url, erro);
                nova = RespostaDemo.erro(500, "ERRO_INTERNO");
            }

            responseCode = nova.codigo;
            resposta = nova;

            return nova;
        }
    }

    @NonNull
    private static String mensagem(int codigo) {
        switch (codigo) {
            case 200:
                return "OK";
            case 201:
                return "Created";
            case 204:
                return "No Content";
            case 400:
                return "Bad Request";
            case 401:
                return "Unauthorized";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            case 409:
                return "Conflict";
            case 500:
                return "Internal Server Error";
            default:
                return "";
        }
    }
}
