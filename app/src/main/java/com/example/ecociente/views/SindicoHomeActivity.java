package com.example.ecociente.views;

import com.example.ecociente.ui.Motion;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecociente.R;
import com.example.ecociente.viewmodels.CalendarioViewModel;
import com.example.ecociente.ui.SemanaColetasView;
import com.example.ecociente.model.ResultadoSolicitacoes;
import com.example.ecociente.model.SindicoDashboard;
import com.example.ecociente.viewmodels.SindicoViewModel;

import java.util.Locale;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.MenuPerfil;
import com.example.ecociente.ui.Navegacao;
import com.example.ecociente.viewmodels.PerfilViewModel;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.model.SindicoAviso;
import android.widget.ImageView;
import com.example.ecociente.model.CondominioAtual;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.repository.RankingExternoRepository;
import com.example.ecociente.ui.AvatarPerfil;
import com.google.firebase.auth.FirebaseUser;

public final class SindicoHomeActivity extends AppCompatActivity {
    private CalendarioViewModel calendarioViewModel;
    private SemanaColetasView semana;

    @Override
    protected void onStart() {
        super.onStart();
        if (calendarioViewModel != null && calendarioViewModel.getColetasDaSemana().getValue() != null) {
            calendarioViewModel.carregarSemana();
        }
    }

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        JanelaEdgeToEdge.aplicar(this);
        setContentView(R.layout.activity_sindico_home);

        Motion.entrar(
                this,
                R.id.sindicoSaudacao,
                R.id.sindicoCondominio,
                R.id.semanaColetas,
                R.id.sindicoDashboardResumo,
                R.id.sindicoRankingTitulo,
                R.id.sindicoRanking);

        View raiz = findViewById(R.id.raizSindicoHome);
        InsetsSistema.aplicarComoPadding(raiz);

        SindicoViewModel viewModel = new ViewModelProvider(this).get(SindicoViewModel.class);
        SindicoDashboard dados = viewModel.getDashboard();
        TextView saudacao = findViewById(R.id.sindicoSaudacao);
        ImageView imagemPerfil = findViewById(R.id.imagemPerfilSindico);
        saudacao.setText(getString(R.string.home_saudacao, dados.getNome()));
        PerfilViewModel perfilViewModel = new ViewModelProvider(this).get(PerfilViewModel.class);
        FirebaseUser usuario = perfilViewModel.usuarioAtual();
        if (usuario != null) {
            perfilViewModel.buscarPerfil(usuario.getUid()).observe(this, perfil -> {
                String nome = PerfilUsuario.nomeExibicao(perfil, usuario).trim();
                if (!nome.isEmpty()) {
                    saudacao.setText(getString(R.string.home_saudacao, nome.split("\\s+")[0]));
                }
                if (perfil != null) {
                    AvatarPerfil.exibir(imagemPerfil, perfil.getFotoUrl());
                }
            });
        }
        ((TextView) findViewById(R.id.sindicoCondominio)).setText(dados.getCondominio());
        ((SindicoGraficoView) findViewById(R.id.sindicoGrafico))
                .setDados(dados.getEvolucao(), dados.getDatasEvolucao());

        semana = findViewById(R.id.semanaColetas);
        View textoErroSemana = findViewById(R.id.textoErroSemana);
        calendarioViewModel = new ViewModelProvider(this).get(CalendarioViewModel.class);
        calendarioViewModel.getColetasDaSemana().observe(this, resultado -> {
            boolean sucesso = resultado.getTipo() == ResultadoSolicitacoes.Tipo.SUCESSO;
            semana.exibir(sucesso ? resultado.getItens() : null);
            textoErroSemana.setVisibility(sucesso ? View.GONE : View.VISIBLE);
        });
        calendarioViewModel.carregarSemana();
        montarMetricas(dados);
        montarRanking(dados);
        findViewById(R.id.sindicoDashboardResumo).setOnClickListener(view -> startActivity(
                new Intent(this, SindicoDashboardActivity.class)));
        View.OnClickListener abrirRanking = view -> startActivity(
                new Intent(this, SindicoRankingActivity.class));
        findViewById(R.id.sindicoRankingTitulo).setOnClickListener(abrirRanking);
        findViewById(R.id.sindicoRanking).setOnClickListener(abrirRanking);
        BarraNavegacaoView barra = findViewById(R.id.barraInferiorSindico);
        barra.configurar(ItensBarra.sindico());
        Navegacao.marcarNaBarra(this, barra, ItensBarra.HOME);
        barra.setOnAssistenteClickListener(
                view -> startActivity(new Intent(this, ChatActivity.class)));
        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDoSindico(this, indice, ItensBarra.HOME));
        findViewById(R.id.botaoAvisosSindico).setOnClickListener(
                view -> startActivity(CentralNotificacoesActivity.criarIntent(
                        this, CentralNotificacoesActivity.Destino.SINDICO)));
        findViewById(R.id.botaoMensagensSindico).setOnClickListener(
                view -> startActivity(MensagensActivity.criarIntentSindico(this)));
        findViewById(R.id.linkCalendarioCompleto).setOnClickListener(
                view -> startActivity(new Intent(this, CalendarioColetasActivity.class)));
        View pontoAvisos = findViewById(R.id.pontoAvisosSindico);
        viewModel.getAvisos().observe(this, avisos -> {
            boolean haNovos = false;
            if (avisos != null) {
                for (SindicoAviso aviso : avisos) {
                    haNovos = haNovos || aviso.isNovo();
                }
            }
            pontoAvisos.setVisibility(haNovos ? View.VISIBLE : View.GONE);
        });
        findViewById(R.id.botaoPerfilSindico).setOnClickListener(
                ancora -> MenuPerfil.mostrar(
                        this,
                        ancora,
                        () -> startActivity(new Intent(this, GerenciarPerfilActivity.class)),
                        this::confirmarSaida));
    }

    private void confirmarSaida() {
        MenuPerfil.confirmarSaida(this, () -> {
            new ViewModelProvider(this).get(PerfilViewModel.class).encerrarSessao();
            Navegacao.abrirLoginLimpandoPilha(this);
        });
    }

    private void montarMetricas(SindicoDashboard dados) {
        LinearLayout painel = findViewById(R.id.sindicoMetricas);
        String[] nomes = {getString(R.string.sindico_participacao_dos_moradores), getString(R.string.sindico_descartes_registrados_no_mes),
                getString(R.string.sindico_taxa_de_adesao_a_coleta_seletiva)};
        String[] valores = {dados.getParticipacao() + "%", String.valueOf(dados.getDescartes()),
                dados.getAdesao() + "%"};
        int[] variacoes = {dados.getVariacaoParticipacao(), dados.getVariacaoDescartes(),
                dados.getVariacaoAdesao()};
        int[] icones = {R.drawable.sindico_people, R.drawable.sindico_recycle,
                R.drawable.sindico_check};

        for (int i = 0; i < nomes.length; i++) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(8), dp(10), dp(8), dp(8));
            card.setBackgroundResource(R.drawable.fundo_cartao_borda);
            LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(0, -2, 1);
            if (i > 0) parametros.leftMargin = dp(6);
            painel.addView(card, parametros);

            // ícone dentro de um círculo verde-claro, como no design
            android.widget.FrameLayout circulo = new android.widget.FrameLayout(this);
            circulo.setBackgroundResource(R.drawable.sindico_icone_circulo);
            card.addView(circulo, new LinearLayout.LayoutParams(dp(30), dp(30)));
            android.widget.ImageView icone = new android.widget.ImageView(this);
            icone.setImageResource(icones[i]);
            icone.setContentDescription(null);
            android.widget.FrameLayout.LayoutParams dentro =
                    new android.widget.FrameLayout.LayoutParams(dp(18), dp(18), Gravity.CENTER);
            circulo.addView(icone, dentro);

            TextView nome = adicionarTexto(card, nomes[i], 11, R.color.cinza_texto_home, false);
            nome.setMaxLines(2);
            nome.setMinLines(2);
            ((LinearLayout.LayoutParams) nome.getLayoutParams()).topMargin = dp(8);
            nome.setLayoutParams(nome.getLayoutParams());

            adicionarTexto(card, valores[i], 22, R.color.verde_escuro_principal, true);

            LinearLayout linha = new LinearLayout(this);
            linha.setGravity(Gravity.CENTER_VERTICAL);
            card.addView(linha, new LinearLayout.LayoutParams(-1, -2));

            boolean queda = variacoes[i] < 0;
            int cor = queda ? R.color.rosa_ecociente : R.color.verde_escuro_principal;
            TextView variacao = adicionarTexto(linha,
                    (queda ? "↓ " : "↑ ") + Math.abs(variacoes[i]) + "%", 12, cor, true);
            variacao.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
            adicionarTexto(linha, getString(R.string.sindico_vs_mes_anterior), 8,
                    R.color.cinza_texto_auxiliar, false);
        }
    }

    private void montarRanking(SindicoDashboard dados) {
        exibirRanking(dados.getBlocosRanking(), dados.getIniciaisRanking(), dados.getPontosRanking());

        if (CondominioAtual.ID <= 0) {
            return;
        }

        new RankingExternoRepository()
                .listarTorres(
                        this,
                        CondominioAtual.ID,
                        5,
                        posicoes -> {
                            if (posicoes == null || isFinishing() || isDestroyed()) {
                                return;
                            }

                            String[] nomes = new String[posicoes.size()];
                            String[] siglas = new String[posicoes.size()];
                            int[] total = new int[posicoes.size()];

                            for (int i = 0; i < nomes.length; i++) {
                                nomes[i] = posicoes.get(i).getNome();
                                siglas[i] = posicoes.get(i).getIniciais();
                                total[i] = posicoes.get(i).getPontos();
                            }

                            exibirRanking(nomes, siglas, total);
                        });
    }

    private void exibirRanking(String[] blocos, String[] iniciais, int[] pontos) {
        LinearLayout ranking = findViewById(R.id.sindicoRanking);
        ranking.removeAllViews();
        for (int i = 0; i < blocos.length; i++) {
            LinearLayout linha = new LinearLayout(this);
            linha.setGravity(Gravity.CENTER_VERTICAL);
            linha.setMinimumHeight(dp(52));
            ranking.addView(linha, new LinearLayout.LayoutParams(-1, dp(52)));

            adicionarTexto(linha, (i + 1) + "º", 16, R.color.cinza_texto_home, true);
            TextView avatar = adicionarTexto(linha, iniciais[i], 12, R.color.branco, true);
            avatar.setGravity(Gravity.CENTER);
            avatar.setBackgroundResource(R.drawable.sindico_avatar_ranking);
            LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(32), dp(32));
            avatarParams.leftMargin = dp(12);
            avatarParams.rightMargin = dp(12);
            avatar.setLayoutParams(avatarParams);

            TextView nome = adicionarTexto(linha, blocos[i], 14, R.color.preto_texto_home, true);
            nome.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
            adicionarTexto(linha, String.format(Locale.forLanguageTag("pt-BR"), "%,d pts", pontos[i]),
                    13, R.color.cinza_texto_home, true);
        }
    }

    private TextView adicionarTexto(LinearLayout container, String texto, int tamanhoSp,
                                    int cor, boolean negrito) {
        TextView campo = new TextView(this);
        campo.setText(texto);
        campo.setTextSize(tamanhoSp);
        campo.setTextColor(ContextCompat.getColor(this, cor));
        campo.setTypeface(ResourcesCompat.getFont(this,
                negrito ? R.font.nunito_bold : R.font.nunito_regular));
        container.addView(campo);
        return campo;
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}
