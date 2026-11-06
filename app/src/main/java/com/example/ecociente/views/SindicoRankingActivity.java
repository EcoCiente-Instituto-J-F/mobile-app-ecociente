package com.example.ecociente.views;

import com.example.ecociente.ui.EstadoTela;
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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.Navegacao;

/** Ranking do condomínio, por torre e por morador, os dois vindos da ds-pontuacao-api. */
public final class SindicoRankingActivity extends AppCompatActivity {
    private List<SindicoPosicaoRanking> torresDaApi;
    private List<SindicoPosicaoRanking> moradoresDaApi;
    private boolean erroNoRanking;
    private boolean erroNosMoradores;

    private static final String ESTADO_MORADORES = "rankingPorMorador";
    private boolean porMorador;

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

        porMorador = estadoSalvo != null && estadoSalvo.getBoolean(ESTADO_MORADORES);
        findViewById(R.id.voltarSindicoRanking).setOnClickListener(view -> finish());
        findViewById(R.id.rankingPorBloco).setOnClickListener(view -> selecionarModo(false));
        findViewById(R.id.rankingPorMorador).setOnClickListener(
                view -> selecionarModo(true));

        BarraNavegacaoView barra = findViewById(R.id.barraInferiorSindico);
        barra.configurar(ItensBarra.sindico());
        Navegacao.marcarNaBarra(this, barra, ItensBarra.HOME);
        barra.setOnAssistenteClickListener(
                view -> startActivity(new Intent(this, ChatActivity.class)));
        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDoSindico(this, indice, ItensBarra.NENHUM));
        atualizarRanking();
        carregarRanking();
    }

    private void carregarRanking() {
        erroNoRanking = false;
        erroNosMoradores = false;

        new RankingExternoRepository()
                .listarMoradoresNoRanking(
                        this,
                        PerfilApiRepository.condominioId(this),
                        100,
                        posicoes -> {
                            if (isFinishing() || isDestroyed()) return;

                            if (posicoes == null) {
                                erroNosMoradores = true;
                            } else {
                                moradoresDaApi = posicoes;
                            }
                            atualizarRanking();
                        });

        new RankingExternoRepository()
                .listarTorres(
                        this,
                        PerfilApiRepository.condominioId(this),
                        20,
                        posicoes -> {
                            if (isFinishing() || isDestroyed()) return;

                            if (posicoes == null) {
                                erroNoRanking = true;
                            } else {
                                torresDaApi = posicoes;
                            }
                            atualizarRanking();
                        });
    }

    private void selecionarModo(boolean moradores) {
        if (porMorador == moradores) return;
        porMorador = moradores;
        atualizarRanking();
    }

    private void atualizarRanking() {
        TextView bloco = findViewById(R.id.rankingPorBloco);
        TextView apartamento = findViewById(R.id.rankingPorMorador);
        bloco.setBackgroundResource(porMorador
                ? android.R.color.transparent : R.drawable.sindico_ranking_modo_ativo);
        apartamento.setBackgroundResource(porMorador
                ? R.drawable.sindico_ranking_modo_ativo : android.R.color.transparent);
        bloco.setContentDescription(porMorador ? "Ver ranking por bloco ou torre"
                : "Ranking por bloco ou torre, selecionado");
        apartamento.setContentDescription(porMorador
                ? "Ranking por morador, selecionado" : "Ver ranking por morador");

        List<SindicoPosicaoRanking> carregadas = porMorador ? moradoresDaApi : torresDaApi;
        List<SindicoPosicaoRanking> posicoes =
                carregadas != null ? carregadas : Collections.emptyList();
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

        boolean falhou = (porMorador ? erroNosMoradores : erroNoRanking) && carregadas == null;

        if (posicoes.isEmpty()) {
            if (falhou) {
                EstadoTela.erro(lista, R.string.ranking_erro, () -> {
                    erroNoRanking = false;
                    erroNosMoradores = false;
                    atualizarRanking();
                    carregarRanking();
                });
            } else {
                EstadoTela.vazio(lista, R.string.sindico_ranking_vazio);
            }
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 3; i < posicoes.size(); i++) {
            SindicoPosicaoRanking posicao = posicoes.get(i);
            View linha = inflater.inflate(R.layout.item_sindico_ranking, lista, false);
            ((TextView) linha.findViewById(R.id.posicaoItemRanking))
                    .setText(getString(R.string.sindico_posicao_ordinal, i + 1));
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

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        estado.putBoolean(ESTADO_MORADORES, porMorador);
        super.onSaveInstanceState(estado);
    }
}
