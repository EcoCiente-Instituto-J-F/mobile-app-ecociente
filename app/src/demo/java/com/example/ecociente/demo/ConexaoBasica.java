package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// O que as conexões falsas têm em comum: cabeçalhos do pedido guardados por nome (sem diferenciar maiúsculas) e métodos HTTP livres. O URLConnection padrão não guarda cabeçalho nenhum e lança se a conexão já abriu, e o HttpURLConnection padrão recusa PATCH, que o app usa; no Android o HttpURLConnection real aceita.
abstract class ConexaoBasica extends HttpURLConnection {

    private final Map<String, String> cabecalhos = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    // Vira true quando o pedido sai: depois disso o método não muda mais.
    private volatile boolean enviado;

    ConexaoBasica(@NonNull URL url) {
        super(url);
    }

    final void marcarEnviado() {
        enviado = true;
    }

    final boolean foiEnviado() {
        return enviado;
    }

    @Override
    public void setRequestMethod(@NonNull String metodo) throws ProtocolException {
        if (enviado) {
            throw new ProtocolException("Can't reset method: already connected");
        }

        if (metodo.isEmpty() || !metodo.matches("[A-Za-z]+")) {
            throw new ProtocolException("Invalid HTTP method: " + metodo);
        }

        this.method = metodo.toUpperCase(java.util.Locale.ROOT);
    }

    @Override
    public void setRequestProperty(@NonNull String chave, @Nullable String valor) {
        synchronized (cabecalhos) {
            if (valor == null) {
                cabecalhos.remove(chave);
            } else {
                cabecalhos.put(chave, valor);
            }
        }
    }

    @Override
    public void addRequestProperty(@NonNull String chave, @Nullable String valor) {
        synchronized (cabecalhos) {
            String atual = cabecalhos.get(chave);

            if (valor != null) {
                cabecalhos.put(chave, atual == null ? valor : atual + ", " + valor);
            }
        }
    }

    @Nullable
    @Override
    public String getRequestProperty(@NonNull String chave) {
        synchronized (cabecalhos) {
            return cabecalhos.get(chave);
        }
    }

    @NonNull
    @Override
    public Map<String, List<String>> getRequestProperties() {
        Map<String, List<String>> copia = new LinkedHashMap<>();

        synchronized (cabecalhos) {
            for (Map.Entry<String, String> cabecalho : cabecalhos.entrySet()) {
                List<String> valores = new ArrayList<>();
                valores.add(cabecalho.getValue());
                copia.put(cabecalho.getKey(), Collections.unmodifiableList(valores));
            }
        }

        return Collections.unmodifiableMap(copia);
    }

    @Override
    public boolean usingProxy() {
        return false;
    }
}
