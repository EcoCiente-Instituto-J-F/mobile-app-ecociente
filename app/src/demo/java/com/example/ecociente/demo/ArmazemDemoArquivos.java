package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
            return new String(Files.readAllBytes(arquivo.toPath()), StandardCharsets.UTF_8);

        } catch (IOException erro) {
            LOG.log(Level.WARNING, "Não foi possível ler o estado da demo", erro);
            return null;
        }
    }

    @Override
    public synchronized void salvar(@NonNull String json) {
        try {
            Files.write(temporario.toPath(), json.getBytes(StandardCharsets.UTF_8));
            Files.move(temporario.toPath(), arquivo.toPath(), StandardCopyOption.REPLACE_EXISTING);

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

            Files.write(foto.toPath(), jpeg);

            return foto.toURI().toString().replaceFirst("^file:/(?!/)", "file:///");

        } catch (IOException erro) {
            throw new IllegalStateException("Não foi possível gravar a foto da demo", erro);
        }
    }

    private static void apagar(@NonNull File alvo) {
        if (alvo.exists() && !alvo.delete()) {
            LOG.warning("Não foi possível apagar " + alvo);
        }
    }
}
