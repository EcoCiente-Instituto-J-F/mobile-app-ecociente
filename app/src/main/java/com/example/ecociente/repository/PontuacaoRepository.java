package com.example.ecociente.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.MovimentacaoPontos;
import com.example.ecociente.model.PosicaoNoRanking;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

// Saldo, histórico e posição do usuário na ds-pontuacao-api. O usuário só consulta os próprios pontos.
public class PontuacaoRepository {

    private static final String TAG = "Pontuacao";

    private static final String URL_BASE = "https://ds-pontuacao-api.onrender.com";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    public interface Retorno<T> {
        // null quando a API não respondeu (sem login, fora do ar, sem permissão).
        void aoConcluir(@Nullable T resultado);
    }

    public static final class PaginaDeMovimentacoes {
        @NonNull public final List<MovimentacaoPontos> itens;
        public final boolean ultimaPagina;

        PaginaDeMovimentacoes(@NonNull List<MovimentacaoPontos> itens, boolean ultimaPagina) {
            this.itens = itens;
            this.ultimaPagina = ultimaPagina;
        }
    }

    public void saldo(@NonNull Context contexto, @NonNull Retorno<Long> retorno) {

        int usuarioId = AutenticacaoExternaRepository.usuarioIdSalvo(contexto);

        executar(
                contexto,
                usuarioId,
                "/api/v1/pontuacoes/usuarios/" + usuarioId + "/saldo",
                corpo -> PontuacaoJson.saldo(new JSONObject(corpo)),
                retorno);
    }

    public void movimentacoes(
            @NonNull Context contexto,
            int pagina,
            int tamanho,
            @NonNull Retorno<PaginaDeMovimentacoes> retorno) {

        int usuarioId = AutenticacaoExternaRepository.usuarioIdSalvo(contexto);

        executar(
                contexto,
                usuarioId,
                "/api/v1/pontuacoes/usuarios/" + usuarioId + "/movimentacoes?pagina=" + pagina
                        + "&tamanho=" + tamanho,
                corpo -> {
                    JSONObject json = new JSONObject(corpo);

                    return new PaginaDeMovimentacoes(
                            PontuacaoJson.movimentacoes(json), PontuacaoJson.ultimaPagina(json));
                },
                retorno);
    }

    public void posicaoNoRanking(@NonNull Context contexto, @NonNull Retorno<PosicaoNoRanking> retorno) {

        int usuarioId = AutenticacaoExternaRepository.usuarioIdSalvo(contexto);

        executar(
                contexto,
                usuarioId,
                "/api/v1/rankings/moradores/" + usuarioId + "/posicao?condominioId="
                        + PerfilApiRepository.condominioId(contexto),
                corpo -> PontuacaoJson.posicao(new JSONObject(corpo)),
                retorno);
    }

    private interface Conversor<T> {
        T converter(@NonNull String corpo) throws Exception;
    }

    private <T> void executar(
            @NonNull Context contexto,
            int usuarioId,
            @NonNull String caminho,
            @NonNull Conversor<T> conversor,
            @NonNull Retorno<T> retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    T resultado = null;

                    try {
                        // Sem o id do usuário (login ainda não feito na API), não há o que consultar.
                        String corpo = usuarioId > 0 ? ApiAutenticada.obter(contextoApp, URL_BASE + caminho) : null;

                        if (corpo != null) {
                            resultado = conversor.converter(corpo);
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível consultar " + caminho, erro);
                    }

                    T resultadoFinal = resultado;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resultadoFinal));
                });
    }
}
