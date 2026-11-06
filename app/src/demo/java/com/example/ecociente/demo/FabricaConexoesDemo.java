package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

// Instalada com URL.setURLStreamHandlerFactory: todo `new URL("https://...").openConnection()` do app passa por aqui. Os hosts da API vão para a ConexaoDemo, a leitura de imagem do Cloudinary sai de verdade, e qualquer outro host recebe IOException.
public final class FabricaConexoesDemo implements URLStreamHandlerFactory {

    private static final Set<String> HOSTS_DA_API =
            new HashSet<>(
                    Arrays.asList(
                            "ds-mobile-api.onrender.com",
                            "ds-pontuacao-api.onrender.com",
                            "ia-eko.onrender.com",
                            "api.52.71.117.143.sslip.io",
                            "api.cloudinary.com",
                            "viacep.com.br"));

    private static final String HOST_IMAGENS = "res.cloudinary.com";

    private final TratadorDemo tratador;
    private final long atrasoMs;
    private final Function<URL, HttpURLConnection> abrirImagem;

    // `abrirImagem` é a ConexaoImagemCloudinary no app; nos testes, uma conexão falsa.
    public FabricaConexoesDemo(
            @NonNull TratadorDemo tratador,
            long atrasoMs,
            @NonNull Function<URL, HttpURLConnection> abrirImagem) {
        this.tratador = tratador;
        this.atrasoMs = atrasoMs;
        this.abrirImagem = abrirImagem;
    }

    // Só http e https; file, jar e os demais ficam com o tratamento padrão do Java.
    @Override
    public URLStreamHandler createURLStreamHandler(@NonNull String protocolo) {
        if (!"http".equals(protocolo) && !"https".equals(protocolo)) {
            return null;
        }

        int portaPadrao = "http".equals(protocolo) ? 80 : 443;

        return new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL url) throws IOException {
                return abrir(url);
            }

            @Override
            protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
                return abrir(url);
            }

            @Override
            protected int getDefaultPort() {
                return portaPadrao;
            }
        };
    }

    @NonNull
    private HttpURLConnection abrir(@NonNull URL url) {
        String host = url.getHost().toLowerCase(Locale.ROOT);

        if (HOSTS_DA_API.contains(host)) {
            return new ConexaoDemo(url, tratador, atrasoMs);
        }

        if (HOST_IMAGENS.equals(host)) {
            return new ConexaoRecusada(url);
        }

        return new ConexaoRecusada(url);
    }
}
