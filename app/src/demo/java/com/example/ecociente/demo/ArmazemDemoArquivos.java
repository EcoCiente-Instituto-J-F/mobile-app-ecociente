package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

// Estado em `demo_estado.json` e fotos em `demo_fotos/`, dentro da pasta recebida (filesDir no app). Grava num temporário e renomeia, para um corte de energia nunca deixar o arquivo pela metade.
public final class ArmazemDemoArquivos implements ArmazemDemo {

    private static final Logger LOG = Logger.getLogger("EcoCienteDemo");

    private final File arquivo;
    private final File temporario;
    private final File pastaFotos;

    public ArmazemDemoArquivos(@NonNull File pasta) {
        this.arquivo = new File(pasta, "demo_estado.json");
        this.temporario = new File(pasta, "demo_estado.json.tmp");
        this.pastaFotos = new File(pasta, "demo_fotos");
    }

    @Nullable
    @Override
    public synchronized String ler() {
        if (!arquivo.isFile()) {
            return null;
        }

        try {
            try (InputStream entrada = new FileInputStream(arquivo);
                    ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int lidos;

                while ((lidos = entrada.read(buffer)) != -1) {
                    saida.write(buffer, 0, lidos);
                }

                return saida.toString("UTF-8");
            }

        } catch (IOException erro) {
            LOG.log(Level.WARNING, "Não foi possível ler o estado da demo", erro);
            return null;
        }
    }

    @Override
    public synchronized void salvar(@NonNull String json) {
        try {
            escrever(temporario, json.getBytes(StandardCharsets.UTF_8));

            // O rename sobre um arquivo existente é atômico no Android; se falhar, apaga e tenta de novo.
            if (!temporario.renameTo(arquivo)) {
                if (arquivo.exists() && !arquivo.delete()) {
                    throw new IOException("Não foi possível substituir " + arquivo);
                }

                if (!temporario.renameTo(arquivo)) {
                    throw new IOException("Não foi possível renomear " + temporario);
                }
            }

        } catch (IOException erro) {
            // O estado continua valendo na memória; só não sobrevive ao fechar o app.
            LOG.log(Level.WARNING, "Não foi possível gravar o estado da demo", erro);
        }
    }

    @Override
    public synchronized void apagarTudo() {
        apagar(arquivo);
        apagar(temporario);

        File[] fotos = pastaFotos.listFiles();

        for (int i = 0; fotos != null && i < fotos.length; i++) {
            apagar(fotos[i]);
        }

        apagar(pastaFotos);
    }

    @NonNull
    @Override
    public synchronized String gravarFoto(@NonNull byte[] jpeg) {
        try {
            if (!pastaFotos.isDirectory() && !pastaFotos.mkdirs()) {
                throw new IOException("Não foi possível criar " + pastaFotos);
            }

            File foto = new File(pastaFotos, "foto_" + UUID.randomUUID() + ".jpg");

            escrever(foto, jpeg);

            return foto.toURI().toString().replaceFirst("^file:/(?!/)", "file:///");

        } catch (IOException erro) {
            throw new IllegalStateException("Não foi possível gravar a foto da demo", erro);
        }
    }

    private static void escrever(@NonNull File destino, @NonNull byte[] dados) throws IOException {
        try (OutputStream saida = new FileOutputStream(destino)) {
            saida.write(dados);
            saida.flush();
        }
    }

    private static void apagar(@NonNull File alvo) {
        if (alvo.exists() && !alvo.delete()) {
            LOG.warning("Não foi possível apagar " + alvo);
        }
    }
}
