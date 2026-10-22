package com.example.ecociente.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.model.SindicoPosicaoRanking;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;
import com.example.ecociente.model.SindicoItemUnidade;

// GET /api/v1/rankings/torres da ds-pontuacao-api (exige o JWT da ds-autenticacao-api).
public class RankingExternoRepository {

    private static final String TAG = "RankingExterno";

    // Trocar pela URL do deploy da ds-pontuacao-api.
    private static final String URL_BASE = "https://ds-pontuacao-api.onrender.com";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

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
                                ApiAutenticada.obter(
                                        contextoApp,
                                        URL_BASE + "/api/v1/rankings/torres?condominioId=" + condominioId
                                                + "&limit=" + limite);

                        if (corpo != null) {
                            posicoes = RankingJson.torres(new JSONObject(corpo));
                            completarComTorresDoCondominio(contextoApp, condominioId, posicoes);
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível carregar o ranking", erro);
                    }

                    List<SindicoPosicaoRanking> resultado = posicoes;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resultado));
                });
    }

    // Torres que a /me conhece e o ranking não trouxe entram pela posição própria da torre
    // (GET /rankings/torres/{id}/posicao); se essa consulta falhar, a torre só não aparece.
    private void completarComTorresDoCondominio(
            @NonNull Context contexto, int condominioId, @NonNull List<SindicoPosicaoRanking> posicoes) {

        for (SindicoItemUnidade torre : PerfilApiRepository.torresDoCondominio(contexto)) {

            if (RankingJson.contem(posicoes, torre.getNome())) {
                continue;
            }

            try {
                String corpo =
                        ApiAutenticada.obter(
                                contexto,
                                URL_BASE + "/api/v1/rankings/torres/" + torre.getId()
                                        + "/posicao?condominioId=" + condominioId);

                if (corpo != null) {
                    posicoes.add(RankingJson.torreDaPosicao(new JSONObject(corpo), torre.getNome()));
                }
            } catch (Exception erro) {
                Log.w(TAG, "Não foi possível consultar a posição da torre " + torre.getId(), erro);
            }
        }

        posicoes.sort((a, b) -> b.getPontos() - a.getPontos());
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
                                ApiAutenticada.obter(
                                        contextoApp,
                                        URL_BASE + "/api/v1/rankings/moradores?condominioId=" + condominioId
                                                + "&limit=" + limite);

                        if (corpo != null) {
                            moradores = RankingJson.moradores(new JSONObject(corpo));
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível carregar o ranking de moradores", erro);
                    }

                    List<ItemLista> resultado = moradores;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resultado));
                });
    }
}
