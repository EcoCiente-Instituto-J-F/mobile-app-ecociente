package com.example.ecociente.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.ecociente.model.CursoResumoHome;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Carrega até três cursos ativos da ds-mobile-api para a home do condomínio. */
public final class CursosHomeRepository {
    private static final String TAG = "CursosHomeRepository";
    private static final String URL_BASE = "https://ds-mobile-api.onrender.com";
    private static final int LIMITE_CURSOS = 3;
    private static final int TIMEOUT_MS = 180_000;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    private final AutenticacaoExternaRepository autenticacao =
            new AutenticacaoExternaRepository();

    public interface Retorno {
        void aoConcluir(boolean sucesso, @NonNull List<CursoResumoHome> cursos);
    }

    public void listar(@NonNull Context contexto, @NonNull Retorno retorno) {
        Context app = contexto.getApplicationContext();

        EXECUTOR.execute(() -> {
            List<CursoResumoHome> cursos = new ArrayList<>();
            boolean sucesso = false;

            try {
                String token = autenticacao.tokenParaUso(app);
                if (token == null || token.trim().isEmpty()) {
                    throw new IOException("Sessão da API indisponível");
                }

                Resposta resposta = requisitar("/api/cursos", token);
                if (resposta.codigo == HttpURLConnection.HTTP_UNAUTHORIZED
                        || resposta.codigo == HttpURLConnection.HTTP_FORBIDDEN) {
                    String renovado = autenticacao.renovarToken(app);
                    if (renovado != null && !renovado.trim().isEmpty()) {
                        resposta = requisitar("/api/cursos", renovado);
                    }
                }

                if (resposta.codigo < 200 || resposta.codigo >= 300) {
                    throw new IOException("GET /api/cursos respondeu HTTP " + resposta.codigo);
                }

                JSONArray itens = new JSONArray(resposta.corpo);
                for (int i = 0; i < itens.length() && cursos.size() < LIMITE_CURSOS; i++) {
                    JSONObject item = itens.optJSONObject(i);
                    if (item == null) {
                        continue;
                    }

                    int id = item.optInt("id", 0);
                    String titulo = item.optString("titulo", "").trim();
                    String descricao = item.optString("descricao", "").trim();

                    if (id > 0 && !titulo.isEmpty()) {
                        cursos.add(new CursoResumoHome(id, titulo, descricao));
                    }
                }

                sucesso = true;
            } catch (Exception erro) {
                Log.w(TAG, "Não foi possível carregar os cursos da home", erro);
            }

            boolean resultado = sucesso;
            List<CursoResumoHome> lista = Collections.unmodifiableList(cursos);
            PRINCIPAL.post(() -> retorno.aoConcluir(resultado, lista));
        });
    }

    @NonNull
    private Resposta requisitar(@NonNull String caminho, @NonNull String token)
            throws IOException {
        HttpURLConnection conexao =
                (HttpURLConnection) new URL(URL_BASE + caminho).openConnection();

        try {
            conexao.setRequestMethod("GET");
            conexao.setRequestProperty("Accept", "application/json");
            conexao.setRequestProperty("Authorization", "Bearer " + token);
            conexao.setUseCaches(false);
            conexao.setConnectTimeout(TIMEOUT_MS);
            conexao.setReadTimeout(TIMEOUT_MS);

            int codigo = conexao.getResponseCode();
            InputStream entrada = codigo >= 400
                    ? conexao.getErrorStream()
                    : conexao.getInputStream();

            return new Resposta(codigo, entrada == null ? "" : ler(entrada));
        } finally {
            conexao.disconnect();
        }
    }

    @NonNull
    private String ler(@NonNull InputStream entrada) throws IOException {
        try (InputStream fluxo = entrada;
             ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int lidos;
            while ((lidos = fluxo.read(buffer)) != -1) {
                saida.write(buffer, 0, lidos);
            }
            return saida.toString(StandardCharsets.UTF_8.name());
        }
    }

    private static final class Resposta {
        final int codigo;
        @NonNull final String corpo;

        Resposta(int codigo, @NonNull String corpo) {
            this.codigo = codigo;
            this.corpo = corpo;
        }
    }
}
