package com.example.ecociente.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.model.SindicoPosicaoRanking;
import com.example.ecociente.ui.Iniciais;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;

// GET /api/v1/rankings/torres da ds-pontuacao-api (exige o JWT da ds-autenticacao-api).
public class RankingExternoRepository {

    private static final String TAG = "RankingExterno";

    // Trocar pela URL do deploy da ds-pontuacao-api.
    private static final String URL_BASE = "https://ds-pontuacao-api.onrender.com";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    private final AutenticacaoExternaRepository autenticacao = new AutenticacaoExternaRepository();

    public interface RetornoMoradores {
        // null quando a API não respondeu; lista vazia = ninguém pontuou no ciclo.
        void aoConcluir(@Nullable List<ItemLista> moradores);
    }

    public interface Retorno {
        // null quando a API não respondeu (sem JWT, 403, fora do ar); lista vazia = sem pontuação.
        void aoConcluir(@Nullable List<SindicoPosicaoRanking> posicoes);
    }

    public void listarTorres(
            @NonNull Context contexto, int condominioId, int limite, @NonNull Retorno retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    List<SindicoPosicaoRanking> posicoes = null;

                    try {
                        String corpo =
                                obter(
                                        contextoApp,
                                        "/api/v1/rankings/torres?condominioId=" + condominioId
                                                + "&limit=" + limite);

                        if (corpo != null) {
                            posicoes = converter(new JSONObject(corpo));
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível carregar o ranking", erro);
                    }

                    List<SindicoPosicaoRanking> resultado = posicoes;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resultado));
                });
    }

    // GET /api/v1/rankings/moradores: ranking semanal dos moradores do condomínio.
    public void listarMoradores(
            @NonNull Context contexto, int condominioId, int limite, @NonNull RetornoMoradores retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    List<ItemLista> moradores = null;

                    try {
                        String corpo =
                                obter(
                                        contextoApp,
                                        "/api/v1/rankings/moradores?condominioId=" + condominioId
                                                + "&limit=" + limite);

                        if (corpo != null) {
                            moradores = converterMoradores(new JSONObject(corpo));
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível carregar o ranking de moradores", erro);
                    }

                    List<ItemLista> resultado = moradores;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resultado));
                });
    }

    @NonNull
    private List<ItemLista> converterMoradores(@NonNull JSONObject resposta) {

        List<ItemLista> moradores = new ArrayList<>();

        JSONArray ranking = resposta.optJSONArray("ranking");

        for (int i = 0; ranking != null && i < ranking.length(); i++) {
            JSONObject item = ranking.optJSONObject(i);

            if (item == null) {
                continue;
            }

            moradores.add(
                    new ItemLista(
                            item.optInt("posicao", i + 1) + "º  " + item.optString("nome", ""),
                            "",
                            NumberFormat.getIntegerInstance(new Locale("pt", "BR"))
                                    .format(item.optLong("pontos", 0))
                                    + " pts"));
        }

        return moradores;
    }

    @NonNull
    private List<SindicoPosicaoRanking> converter(@NonNull JSONObject resposta) {

        List<SindicoPosicaoRanking> posicoes = new ArrayList<>();

        JSONArray ranking = resposta.optJSONArray("ranking");

        for (int i = 0; ranking != null && i < ranking.length(); i++) {
            JSONObject item = ranking.optJSONObject(i);

            if (item == null) {
                continue;
            }

            String nome = item.optString("nome", "");

            posicoes.add(
                    new SindicoPosicaoRanking(
                            Iniciais.de(nome), nome, (int) item.optLong("pontos", 0), false));
        }

        return posicoes;
    }

    @Nullable
    private String obter(@NonNull Context contexto, @NonNull String caminho) throws IOException {

        String token = autenticacao.tokenParaUso(contexto);

        if (token == null) {
            return null;
        }

        int[] codigo = new int[1];
        String corpo = requisitar(caminho, token, codigo);

        if (codigo[0] == 401 || codigo[0] == 403) {
            String renovado = autenticacao.renovarToken(contexto);

            if (renovado != null) {
                corpo = requisitar(caminho, renovado, codigo);
            }
        }

        if (codigo[0] < 200 || codigo[0] >= 300) {
            Log.w(TAG, "HTTP " + codigo[0] + " em " + caminho);
            return null;
        }

        return corpo;
    }

    @NonNull
    private String requisitar(@NonNull String caminho, @NonNull String token, @NonNull int[] codigo)
            throws IOException {

        HttpURLConnection conexao = (HttpURLConnection) new URL(URL_BASE + caminho).openConnection();

        try {
            conexao.setRequestMethod("GET");
            conexao.setRequestProperty("Accept", "application/json");
            conexao.setRequestProperty("Authorization", "Bearer " + token);
            conexao.setConnectTimeout(180_000);
            conexao.setReadTimeout(180_000);

            codigo[0] = conexao.getResponseCode();

            InputStream fluxo = codigo[0] >= 400 ? conexao.getErrorStream() : conexao.getInputStream();

            if (fluxo == null) {
                return "";
            }

            try (InputStream entrada = fluxo;
                    ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int lidos;

                while ((lidos = entrada.read(buffer)) != -1) {
                    saida.write(buffer, 0, lidos);
                }

                return saida.toString(StandardCharsets.UTF_8.name());
            }
        } finally {
            conexao.disconnect();
        }
    }
}
