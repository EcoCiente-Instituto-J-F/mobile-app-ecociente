package com.example.ecociente.viewmodels;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecociente.model.SindicoAviso;
import com.example.ecociente.model.SindicoDashboard;
import com.example.ecociente.model.SindicoPosicaoRanking;
import com.example.ecociente.repository.PerfilApiRepository;
import com.example.ecociente.repository.RankingExternoRepository;
import com.example.ecociente.repository.SindicoRepository;

import java.util.List;

public final class SindicoViewModel extends AndroidViewModel {
    private static final String TAG = "SindicoViewModel";

    private final SindicoRepository repositorio = SindicoRepository.getInstance();
    private final MutableLiveData<List<SindicoPosicaoRanking>> rankingTorres = new MutableLiveData<>();
    private boolean carregandoRankingTorres;

    public SindicoViewModel(@NonNull Application aplicacao) {
        super(aplicacao);
    }

    // null quando a API falha ou quando o perfil não possui condomínio válido.
    public LiveData<List<SindicoPosicaoRanking>> getRankingTorres() { return rankingTorres; }

    public void carregarRankingTorres(int limite) {
        if (limite <= 0 || carregandoRankingTorres) return;

        int condominioId = PerfilApiRepository.condominioId(getApplication());
        if (condominioId > 0) {
            consultarRankingTorres(condominioId, limite);
            return;
        }

        // O perfil é carregado em segundo plano. Não consultar o ranking com condominioId=0:
        // aguardar /api/usuarios/me e só então disparar a chamada à API de pontuação.
        carregandoRankingTorres = true;
        Log.i(TAG, "Aguardando perfil para obter o condominioId antes de carregar o ranking");
        new PerfilApiRepository().atualizar(getApplication(), () -> {
            int idAtualizado = PerfilApiRepository.condominioId(getApplication());
            if (idAtualizado <= 0) {
                carregandoRankingTorres = false;
                Log.w(TAG, "Ranking não consultado: perfil não retornou um condominioId válido");
                rankingTorres.setValue(null);
                return;
            }
            consultarRankingTorres(idAtualizado, limite);
        });
    }

    private void consultarRankingTorres(int condominioId, int limite) {
        if (condominioId <= 0) {
            carregandoRankingTorres = false;
            Log.w(TAG, "Ranking não consultado: condominioId inválido (" + condominioId + ")");
            rankingTorres.setValue(null);
            return;
        }

        carregandoRankingTorres = true;
        Log.i(TAG, "Consultando ranking de torres do condomínio " + condominioId);
        new RankingExternoRepository().listarTorres(
                getApplication(),
                condominioId,
                limite,
                resultado -> {
                    carregandoRankingTorres = false;
                    rankingTorres.setValue(resultado);
                });
    }

    public SindicoDashboard getDashboard() { return repositorio.getDashboard(); }
    public LiveData<List<SindicoAviso>> getAvisos() { return repositorio.observarAvisos(); }

    public void salvarRascunho(String titulo, String mensagem, String destinatario) {
        repositorio.salvarRascunho(titulo, mensagem, destinatario);
    }
}
