package com.example.ecociente.repository;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// As APIs ficam em hospedagem gratuita e dormem sem uso: a primeira chamada leva de 70 a 150 s. Um GET qualquer acorda o serviço, então o app faz isso na abertura, antes de o usuário precisar dos dados. Os endereços vêm da ConfiguracaoServidores.
public final class AquecimentoDasApis {

    private static final String TAG = "Aquecimento";

    private static final String MOBILE = ConfiguracaoServidores.MOBILE_API + "/";
    private static final String PONTUACAO = ConfiguracaoServidores.PONTUACAO_API + "/";

    // Acordar de novo antes disso não adianta: o serviço ainda está de pé.
    private static final long INTERVALO_MS = 10 * 60_000L;

    private static final int TEMPO_MAXIMO_MS = 180_000;

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);

    private static long ultimoAquecimento;
    private static String ultimoPerfil;

    private AquecimentoDasApis() {}

    // Só acorda o que o perfil salvo vai usar. Sem perfil (primeiro acesso), só a autenticação.
    public static synchronized void aquecer(@NonNull Context contexto) {

        long agora = System.currentTimeMillis();

        String perfil = AutenticacaoExternaRepository.perfilSalvo(contexto);

        // Perfil novo (o login acabou de acontecer) acorda de novo, mesmo dentro do intervalo.
        if (agora - ultimoAquecimento < INTERVALO_MS && Objects.equals(perfil, ultimoPerfil)) {
            return;
        }

        ultimoAquecimento = agora;
        ultimoPerfil = perfil;

        for (String endereco : servidoresDo(perfil)) {
            EXECUTOR.execute(() -> acordar(endereco));
        }
    }

    @NonNull
    static List<String> servidoresDo(String perfil) {

        List<String> servidores = new ArrayList<>();

        // Login, cadastro, senha, perfil, calendário, feed e quiz passam todos por ela.
        servidores.add(MOBILE);

        // Ranking e pontos ainda são de outro serviço; a cooperativa não os usa.
        if (!"COOPERATIVA".equals(perfil)) {
            servidores.add(PONTUACAO);
        }

        return servidores;
    }

    private static void acordar(@NonNull String endereco) {

        HttpURLConnection conexao = null;

        try {
            conexao = (HttpURLConnection) new URL(endereco).openConnection();
            conexao.setRequestMethod("GET");
            conexao.setConnectTimeout(TEMPO_MAXIMO_MS);
            conexao.setReadTimeout(TEMPO_MAXIMO_MS);

            // O código não importa (404 e 401 também): a resposta só prova que o serviço acordou.
            Log.d(TAG, endereco + " -> " + conexao.getResponseCode());

        } catch (Exception erro) {
            Log.w(TAG, "Não foi possível acordar " + endereco, erro);

        } finally {
            if (conexao != null) {
                conexao.disconnect();
            }
        }
    }
}
