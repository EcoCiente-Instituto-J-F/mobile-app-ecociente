package com.example.ecociente.views;

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

public final class SindicoHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        JanelaEdgeToEdge.aplicar(this);
        setContentView(R.layout.activity_sindico_home);

        View raiz = findViewById(R.id.raizSindicoHome);
        InsetsSistema.aplicarComoPadding(raiz);

        SindicoViewModel viewModel = new ViewModelProvider(this).get(SindicoViewModel.class);
        SindicoDashboard dados = viewModel.getDashboard();
        ((TextView) findViewById(R.id.sindicoSaudacao)).setText(
                getString(R.string.home_saudacao, dados.getNome()));
        ((TextView) findViewById(R.id.sindicoCondominio)).setText(dados.getCondominio());
        ((SindicoGraficoView) findViewById(R.id.sindicoGrafico))
                .setDados(dados.getEvolucao(), dados.getDatasEvolucao());

        montarCalendario(dados);
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

    private void montarCalendario(SindicoDashboard dados) {
        LinearLayout dias = findViewById(R.id.sindicoDiasSemana);
        dias.removeAllViews();
        String[] nomes = dados.getDiasSemana();
        String[] datas = dados.getDatasSemana();
        boolean[] coletaDias = dados.getColetaSemana();
        for (int i = 0; i < nomes.length; i++) {
            LinearLayout dia = new LinearLayout(this);
            dia.setOrientation(LinearLayout.VERTICAL);
            dia.setGravity(Gravity.CENTER);
            dia.setBackgroundResource(R.drawable.sindico_card);
            LinearLayout.LayoutParams tamanho = new LinearLayout.LayoutParams(0, dp(65), 1);
            if (i > 0) tamanho.leftMargin = dp(5);
            dias.addView(dia, tamanho);

            boolean coleta = coletaDias[i];
            adicionarTexto(dia, nomes[i], 10, R.color.verde_escuro_principal, true);
            adicionarTexto(dia, datas[i], 9, R.color.cinza_texto_home, false);
            TextView marcador = adicionarTexto(dia, "", 20,
                    coleta ? R.color.verde_escuro_principal : R.color.rosa_ecociente, true);
            marcador.setBackgroundResource(R.drawable.sindico_circulo);
            marcador.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(this,
                            coleta ? R.color.verde_escuro_principal : R.color.rosa_ecociente)));
            marcador.setWidth(dp(13));
            marcador.setHeight(dp(13));
            marcador.setContentDescription(getString(coleta ? R.string.coleta : R.string.nao_ha_coleta));
            adicionarTexto(dia, getString(coleta ? R.string.coleta : R.string.nao_ha_coleta), 8,
                    R.color.cinza_texto_home, false);
        }
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
            card.setPadding(dp(7), dp(8), dp(7), dp(6));
            card.setBackgroundResource(R.drawable.sindico_card);
            LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(0, dp(100), 1);
            if (i > 0) parametros.leftMargin = dp(5);
            painel.addView(card, parametros);

            android.widget.ImageView icone = new android.widget.ImageView(this);
            icone.setImageResource(icones[i]);
            icone.setContentDescription(null);
            card.addView(icone, new LinearLayout.LayoutParams(dp(21), dp(21)));
            TextView nome = adicionarTexto(card, nomes[i], 9, R.color.cinza_texto_home, false);
            nome.setMaxLines(2);
            nome.setMinHeight(dp(28));
            adicionarTexto(card, valores[i], 18, R.color.verde_escuro_principal, true);
            String variacao = (variacoes[i] > 0 ? "+" : "") + variacoes[i] + "%";
            adicionarTexto(card, getString(R.string.sindico_variacao_mes, variacao), 8,
                    variacoes[i] < 0 ? R.color.rosa_ecociente : R.color.verde_escuro_principal, false);
        }
    }

    private void montarRanking(SindicoDashboard dados) {
        LinearLayout ranking = findViewById(R.id.sindicoRanking);
        String[] blocos = dados.getBlocosRanking();
        String[] iniciais = dados.getIniciaisRanking();
        int[] pontos = dados.getPontosRanking();
        for (int i = 0; i < blocos.length; i++) {
            LinearLayout linha = new LinearLayout(this);
            linha.setGravity(Gravity.CENTER_VERTICAL);
            linha.setMinimumHeight(dp(46));
            ranking.addView(linha, new LinearLayout.LayoutParams(-1, dp(46)));

            adicionarTexto(linha, (i + 1) + "º", 14, R.color.cinza_texto_home, true);
            TextView avatar = adicionarTexto(linha, iniciais[i], 10, R.color.branco, true);
            avatar.setGravity(Gravity.CENTER);
            avatar.setBackgroundResource(R.drawable.sindico_avatar_ranking);
            LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(27), dp(27));
            avatarParams.leftMargin = dp(10);
            avatarParams.rightMargin = dp(10);
            avatar.setLayoutParams(avatarParams);

            TextView nome = adicionarTexto(linha, blocos[i], 12, R.color.preto_texto_home, true);
            nome.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
            adicionarTexto(linha, String.format(Locale.forLanguageTag("pt-BR"), "%,d pts", pontos[i]),
                    11, R.color.cinza_texto_home, true);
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
