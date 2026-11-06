package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// Leitura de imagem do Cloudinary de verdade: as mídias do catálogo continuam vindo de lá. O OkHttp não passa pelo URL.openConnection, então não cai de volta na fábrica da demo. Sem internet dá IOException, e o CarregadorMidias usa o drawable de fallback. Só GET: o upload nunca sai do aparelho (api.cloudinary.com vai para a ConexaoDemo).
public final class ConexaoImagemCloudinary extends ConexaoBasica {

    private static final OkHttpClient CLIENTE = new OkHttpClient();

    private final Object trava = new Object();

    @Nullable private Response resposta;

    public ConexaoImagemCloudinary(@NonNull URL url) {
        super(url);
    }

    @Override
    public void connect() throws IOException {
        responder();
    }

    @Override
    public void disconnect() {
        synchronized (trava) {
            if (resposta != null) {
                resposta.close();
            }
        }
    }

    @Override
    public int getResponseCode() throws IOException {
        return responder().code();
    }

    @NonNull
    @Override
    public String getResponseMessage() throws IOException {
        return responder().message();
    }

    @NonNull
    @Override
    public InputStream getInputStream() throws IOException {
        Response recebida = responder();

        if (recebida.code() >= 400 || recebida.body() == null) {
            throw new IOException("Server returned HTTP response code: " + recebida.code() + " for URL: " + url);
        }

        return recebida.body().byteStream();
    }

    @Nullable
    @Override
    public InputStream getErrorStream() {
        synchronized (trava) {
            if (resposta == null || resposta.code() < 400 || resposta.body() == null) {
                return null;
            }

            return resposta.body().byteStream();
        }
    }

    @NonNull
    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new ProtocolException("A demonstração só lê imagens do Cloudinary");
    }

    @Nullable
    @Override
    public String getHeaderField(@Nullable String nome) {
        if (nome == null) {
            return null;
        }

        try {
            return responder().header(nome);

        } catch (IOException erro) {
            return null;
        }
    }

    @Nullable
    @Override
    public String getHeaderField(int posicao) {
        try {
            Response recebida = responder();

            return posicao >= 0 && posicao < recebida.headers().size() ? recebida.headers().value(posicao) : null;

        } catch (IOException erro) {
            return null;
        }
    }

    @Nullable
    @Override
    public String getHeaderFieldKey(int posicao) {
        try {
            Response recebida = responder();

            return posicao >= 0 && posicao < recebida.headers().size() ? recebida.headers().name(posicao) : null;

        } catch (IOException erro) {
            return null;
        }
    }

    // O pedido sai uma vez, sob trava; chamadas seguintes reaproveitam a resposta.
    @NonNull
    private Response responder() throws IOException {
        synchronized (trava) {
            if (resposta != null) {
                return resposta;
            }

            if (!"GET".equals(getRequestMethod())) {
                throw new ProtocolException("A demonstração só lê imagens do Cloudinary");
            }

            marcarEnviado();
            connected = true;

            OkHttpClient cliente =
                    CLIENTE.newBuilder()
                            .connectTimeout(getConnectTimeout(), TimeUnit.MILLISECONDS)
                            .readTimeout(getReadTimeout(), TimeUnit.MILLISECONDS)
                            .followRedirects(getInstanceFollowRedirects())
                            .build();

            try {
                Request.Builder pedido = new Request.Builder().url(url.toString()).get();

                for (java.util.Map.Entry<String, java.util.List<String>> cabecalho :
                        getRequestProperties().entrySet()) {
                    pedido.header(cabecalho.getKey(), cabecalho.getValue().get(0));
                }

                resposta = cliente.newCall(pedido.build()).execute();

            } catch (IllegalArgumentException erro) {
                throw new IOException("Pedido de imagem inválido: " + url, erro);
            }

            responseCode = resposta.code();

            return resposta;
        }
    }
}
