package com.example.ecociente.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ecociente.model.ItemLista;
import com.example.ecociente.model.SindicoItemUnidade;
import com.example.ecociente.model.SindicoPosicaoRanking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.json.JSONObject;

public class RankingExternoRepository {
    private static final String TAG = "RankingExterno";
    private static final String URL_BASE = ConfiguracaoServidores.PONTUACAO_API;
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final ExecutorService REDE = Executors.newFixedThreadPool(3);
    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    public interface RetornoMoradores {
        void aoConcluir(@Nullable List<ItemLista> moradores);
    }

    public interface Retorno {
        void aoConcluir(@Nullable List<SindicoPosicaoRanking> posicoes);
    }

    private <T> void consultarComPrazo(
            @NonNull Callable<T> chamada, @NonNull T demonstracao,
            @NonNull Consumer<T> retorno) {
        EXECUTOR.execute(() -> {
            Future<T> tarefa = REDE.submit(chamada);
            T resultado;
            try {
                resultado = tarefa.get(10, TimeUnit.SECONDS);
                if (resultado == null) {
                    Log.w(TAG, "API indisponivel; mostrando ranking demonstrativo");
                    resultado = demonstracao;
                }
            } catch (TimeoutException erro) {
                Log.w(TAG, "Ranking ultrapassou 10 segundos; mostrando demonstracao");
                tarefa.cancel(true);
                resultado = demonstracao;
            } catch (Exception erro) {
                Log.w(TAG, "Falha no ranking; mostrando demonstracao", erro);
                tarefa.cancel(true);
                resultado = demonstracao;
            }
            T finalResultado = resultado;
            PRINCIPAL.post(() -> retorno.accept(finalResultado));
        });
    }

    private static List<SindicoPosicaoRanking> posicoesDemonstracao(boolean torre) {
        String tipo = torre ? "Torre" : "Morador";
        return Arrays.asList(
                new SindicoPosicaoRanking("D1", "DEMONSTRAÇÃO - " + tipo + " A", 350, false),
                new SindicoPosicaoRanking("D2", "DEMONSTRAÇÃO - " + tipo + " B", 240, false),
                new SindicoPosicaoRanking("D3", "DEMONSTRAÇÃO - " + tipo + " C", 120, false));
    }

    private static List<ItemLista> moradoresDemonstracao() {
        return Arrays.asList(
                new ItemLista("DEMONSTRAÇÃO - 1º Morador A", "Ranking fictício (não oficial)", "350 pts fictícios"),
                new ItemLista("DEMONSTRAÇÃO - 2º Morador B", "Ranking fictício (não oficial)", "240 pts fictícios"),
                new ItemLista("DEMONSTRAÇÃO - 3º Morador C", "Ranking fictício (não oficial)", "120 pts fictícios"));
    }

    public void listarTorres(
            @NonNull Context contexto, int condominioId, int limite, @NonNull Retorno retorno) {
        if (condominioId <= 0) {
            PRINCIPAL.post(() -> retorno.aoConcluir(null));
            return;
        }
        Context app = contexto.getApplicationContext();
        consultarComPrazo(() -> {
            String corpo = ApiAutenticada.obter(app,
                    URL_BASE + "/api/v1/rankings/torres?condominioId=" + condominioId + "&limit=" + limite);
            if (corpo == null) return null;
            List<SindicoPosicaoRanking> posicoes = RankingJson.torres(new JSONObject(corpo));
            completarComTorresDoCondominio(app, condominioId, posicoes);
            return posicoes;
        }, posicoesDemonstracao(true), retorno::aoConcluir);
    }

    private void completarComTorresDoCondominio(
            @NonNull Context contexto, int condominioId,
            @NonNull List<SindicoPosicaoRanking> posicoes) {
        for (SindicoItemUnidade torre : PerfilApiRepository.torresDoCondominio(contexto)) {
            if (Thread.currentThread().isInterrupted()) return;
            if (RankingJson.contem(posicoes, torre.getNome())) continue;
            try {
                String corpo = ApiAutenticada.obter(contexto,
                        URL_BASE + "/api/v1/rankings/torres/" + torre.getId()
                                + "/posicao?condominioId=" + condominioId);
                if (corpo != null) {
                    posicoes.add(RankingJson.torreDaPosicao(new JSONObject(corpo), torre.getNome()));
                }
            } catch (Exception erro) {
                Log.w(TAG, "Erro ao consultar torre " + torre.getId(), erro);
            }
        }
        posicoes.sort((a, b) -> Integer.compare(b.getPontos(), a.getPontos()));
    }

    public void listarMoradoresNoRanking(
            @NonNull Context contexto, int condominioId, int limite, @NonNull Retorno retorno) {
        if (condominioId <= 0) {
            PRINCIPAL.post(() -> retorno.aoConcluir(null));
            return;
        }
        Context app = contexto.getApplicationContext();
        consultarComPrazo(() -> {
            String corpo = ApiAutenticada.obter(app,
                    URL_BASE + "/api/v1/rankings/moradores?condominioId=" + condominioId
                            + "&limit=" + limite);
            return corpo == null ? null : RankingJson.posicoes(new JSONObject(corpo));
        }, posicoesDemonstracao(false), retorno::aoConcluir);
    }

    public void listarMoradores(
            @NonNull Context contexto, int condominioId, int limite,
            @NonNull RetornoMoradores retorno) {
        if (condominioId <= 0) {
            PRINCIPAL.post(() -> retorno.aoConcluir(null));
            return;
        }
        Context app = contexto.getApplicationContext();
        consultarComPrazo(() -> {
            String corpo = ApiAutenticada.obter(app,
                    URL_BASE + "/api/v1/rankings/moradores?condominioId=" + condominioId
                            + "&limit=" + limite);
            return corpo == null ? null : RankingJson.moradores(new JSONObject(corpo));
        }, moradoresDemonstracao(), retorno::aoConcluir);
    }
}
