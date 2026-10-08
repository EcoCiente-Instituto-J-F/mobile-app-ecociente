package com.example.ecociente.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ecociente.R;
import com.example.ecociente.model.SindicoPosicaoRanking;
import com.example.ecociente.repository.SindicoRankingRepository;

import java.util.List;
import java.util.Locale;

/** Ranking ilustrativo em dois modos; nenhum dado é lido de banco ou API. */
public final class SindicoRankingActivity extends AppCompatActivity {
    private static final String ESTADO_APARTAMENTOS = "rankingPorApartamento";
    private boolean porApartamento;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_sindico_ranking);

        View raiz = findViewById(R.id.raizSindicoRanking);
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (view, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(raiz);

        porApartamento = estadoSalvo != null && estadoSalvo.getBoolean(ESTADO_APARTAMENTOS);
        findViewById(R.id.voltarSindicoRanking).setOnClickListener(view -> finish());
        findViewById(R.id.rankingPorBloco).setOnClickListener(view -> selecionarModo(false));
        findViewById(R.id.rankingPorApartamento).setOnClickListener(
                view -> selecionarModo(true));

        SindicoBottomBar barra = findViewById(R.id.barraInferiorSindico);
        barra.selecionar(SindicoBottomBar.Aba.HOME);
        barra.aoTocarHome(view -> navegar(SindicoHomeActivity.class));
        barra.aoTocarSolicitacao(view -> navegar(SindicoCooperativasActivity.class));
        barra.aoTocarHistorico(view -> navegar(SindicoHistoricoActivity.class));
        barra.aoTocarUnidades(view -> navegar(SindicoUnidadesActivity.class));
        atualizarRanking();
    }

    private void selecionarModo(boolean apartamentos) {
        if (porApartamento == apartamentos) return;
        porApartamento = apartamentos;
        atualizarRanking();
    }

    private void atualizarRanking() {
        TextView bloco = findViewById(R.id.rankingPorBloco);
        TextView apartamento = findViewById(R.id.rankingPorApartamento);
        bloco.setBackgroundResource(porApartamento
                ? android.R.color.transparent : R.drawable.sindico_ranking_modo_ativo);
        apartamento.setBackgroundResource(porApartamento
                ? R.drawable.sindico_ranking_modo_ativo : android.R.color.transparent);
        bloco.setContentDescription(porApartamento ? "Ver ranking por bloco ou torre"
                : "Ranking por bloco ou torre, selecionado");
        apartamento.setContentDescription(porApartamento
                ? "Ranking por apartamento, selecionado" : "Ver ranking por apartamento");

        List<SindicoPosicaoRanking> posicoes = SindicoRankingRepository.getInstance()
                .getRanking(porApartamento);
        preencherPodio(posicoes.get(0), R.id.avatarPrimeiroRanking,
                R.id.pontosPrimeiroRanking);
        preencherPodio(posicoes.get(1), R.id.avatarSegundoRanking,
                R.id.pontosSegundoRanking);
        preencherPodio(posicoes.get(2), R.id.avatarTerceiroRanking,
                R.id.pontosTerceiroRanking);

        LinearLayout lista = findViewById(R.id.listaSindicoRankingCompleta);
        lista.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 3; i < posicoes.size(); i++) {
            SindicoPosicaoRanking posicao = posicoes.get(i);
            View linha = inflater.inflate(R.layout.item_sindico_ranking, lista, false);
            ((TextView) linha.findViewById(R.id.posicaoItemRanking))
                    .setText((i + 1) + "º");
            ((TextView) linha.findViewById(R.id.avatarItemRanking))
                    .setText(posicao.getIniciais());
            ((TextView) linha.findViewById(R.id.nomeItemRanking))
                    .setText(posicao.getNome());
            ((TextView) linha.findViewById(R.id.pontosItemRanking))
                    .setText(formatarPontos(posicao.getPontos()));
            if (posicao.isUsuarioAtual()) {
                linha.setBackgroundResource(R.drawable.sindico_unidade_vinculada);
                linha.setContentDescription((i + 1) + "º lugar: você, "
                        + formatarPontos(posicao.getPontos()));
            }
            lista.addView(linha);
        }
    }

    private void preencherPodio(SindicoPosicaoRanking posicao, int idAvatar, int idPontos) {
        TextView avatar = findViewById(idAvatar);
        avatar.setText("");
        avatar.setContentDescription(posicao.getNome() + ", "
                + formatarPontos(posicao.getPontos()));
        ((TextView) findViewById(idPontos)).setText(formatarPontos(posicao.getPontos()));
    }

    private String formatarPontos(int pontos) {
        return String.format(Locale.forLanguageTag("pt-BR"), "%,d pts", pontos);
    }

    private void navegar(Class<?> destino) {
        Intent intent = new Intent(this, destino);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        estado.putBoolean(ESTADO_APARTAMENTOS, porApartamento);
        super.onSaveInstanceState(estado);
    }
}
