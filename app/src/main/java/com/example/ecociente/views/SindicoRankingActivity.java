package com.example.ecociente.views;

import com.example.ecociente.repository.PerfilApiRepository;
import com.example.ecociente.ui.Motion;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecociente.R;
import com.example.ecociente.model.SindicoPosicaoRanking;
import com.example.ecociente.repository.RankingExternoRepository;
import com.example.ecociente.repository.SindicoRankingRepository;

import java.util.List;
import java.util.Locale;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.Navegacao;

/** Ranking ilustrativo em dois modos; nenhum dado é lido de banco ou API. */
public final class SindicoRankingActivity extends AppCompatActivity {
    private List<SindicoPosicaoRanking> torresDaApi;

    private static final String ESTADO_APARTAMENTOS = "rankingPorApartamento";
    private boolean porApartamento;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        JanelaEdgeToEdge.aplicar(this);
        setContentView(R.layout.activity_sindico_ranking);

        Motion.entrar(
                this,
                R.id.tituloSindicoRanking,
                R.id.rankingPorBloco,
                R.id.avatarPrimeiroRanking,
                R.id.avatarSegundoRanking,
                R.id.avatarTerceiroRanking,
                R.id.listaSindicoRankingCompleta);

        View raiz = findViewById(R.id.raizSindicoRanking);
        InsetsSistema.aplicarComoPadding(raiz);

        porApartamento = estadoSalvo != null && estadoSalvo.getBoolean(ESTADO_APARTAMENTOS);
        findViewById(R.id.voltarSindicoRanking).setOnClickListener(view -> finish());
        findViewById(R.id.rankingPorBloco).setOnClickListener(view -> selecionarModo(false));
        findViewById(R.id.rankingPorApartamento).setOnClickListener(
                view -> selecionarModo(true));

        BarraNavegacaoView barra = findViewById(R.id.barraInferiorSindico);
        barra.configurar(ItensBarra.sindico());
        Navegacao.marcarNaBarra(this, barra, ItensBarra.HOME);
        barra.setOnAssistenteClickListener(
                view -> startActivity(new Intent(this, ChatActivity.class)));
        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDoSindico(this, indice, ItensBarra.NENHUM));
        atualizarRanking();

        new RankingExternoRepository()
                .listarTorres(
                        this,
                        PerfilApiRepository.condominioId(this),
                        20,
                        posicoes -> {
                            if (posicoes != null && !isFinishing() && !isDestroyed()) {
                                torresDaApi = posicoes;
                                atualizarRanking();
                            }
                        });
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

        // Por bloco/torre: usa o ranking da API quando ele já chegou.
        if (!porApartamento && torresDaApi != null) {
            posicoes = torresDaApi;
        }
        int[] avatares = {R.id.avatarPrimeiroRanking, R.id.avatarSegundoRanking,
                R.id.avatarTerceiroRanking};
        int[] pontos = {R.id.pontosPrimeiroRanking, R.id.pontosSegundoRanking,
                R.id.pontosTerceiroRanking};
        for (int i = 0; i < avatares.length; i++) {
            if (i < posicoes.size()) {
                preencherPodio(posicoes.get(i), avatares[i], pontos[i]);
            } else {
                ((TextView) findViewById(pontos[i])).setText("");
                findViewById(avatares[i]).setContentDescription(null);
            }
        }

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
