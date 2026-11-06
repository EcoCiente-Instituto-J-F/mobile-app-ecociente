package com.example.ecociente.demo;

import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

// Armazém só de teste: guarda o JSON e as fotos na memória.
public final class ArmazemEmMemoria implements ArmazemDemo {

    public String json;
    public final List<byte[]> fotos = new ArrayList<>();
    public int apagamentos;

    @Nullable
    @Override
    public synchronized String ler() {
        return json;
    }

    @Override
    public synchronized void salvar(String novoJson) {
        json = novoJson;
    }

    @Override
    public synchronized void apagarTudo() {
        json = null;
        fotos.clear();
        apagamentos++;
    }

    @Override
    public synchronized String gravarFoto(byte[] jpeg) {
        fotos.add(jpeg);
        return "file:///memoria/foto_" + fotos.size() + ".jpg";
    }
}
