package com.example.ecociente.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.ecociente.R;

/** Barra compartilhada das telas de demonstração do síndico. */
public final class SindicoBottomBar extends FrameLayout {
    public enum Aba { HOME, SOLICITACAO, HISTORICO, UNIDADES }

    private final View[] abas = new View[4];
    private final View[] indicadores = new View[4];
    private final ImageView[] icones = new ImageView[4];
    private final TextView[] textos = new TextView[4];

    public SindicoBottomBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inicializar();
    }

    private void inicializar() {
        inflate(getContext(), R.layout.view_sindico_bottom_bar, this);
        int[] idsAbas = {R.id.sindicoNavHome, R.id.sindicoNavSolicitacao,
                R.id.sindicoNavHistorico, R.id.sindicoNavUnidades};
        int[] idsIndicadores = {R.id.sindicoNavIndicadorHome,
                R.id.sindicoNavIndicadorSolicitacao, R.id.sindicoNavIndicadorHistorico,
                R.id.sindicoNavIndicadorUnidades};
        int[] idsIcones = {R.id.sindicoNavIconeHome, R.id.sindicoNavIconeSolicitacao,
                R.id.sindicoNavIconeHistorico, R.id.sindicoNavIconeUnidades};
        int[] idsTextos = {R.id.sindicoNavTextoHome, R.id.sindicoNavTextoSolicitacao,
                R.id.sindicoNavTextoHistorico, R.id.sindicoNavTextoUnidades};
        for (int i = 0; i < abas.length; i++) {
            abas[i] = findViewById(idsAbas[i]);
            indicadores[i] = findViewById(idsIndicadores[i]);
            icones[i] = findViewById(idsIcones[i]);
            textos[i] = findViewById(idsTextos[i]);
        }
        selecionar(Aba.HOME);
    }

    public void selecionar(@NonNull Aba aba) {
        for (int i = 0; i < abas.length; i++) {
            boolean selecionada = i == aba.ordinal();
            abas[i].setSelected(selecionada);
            indicadores[i].setVisibility(selecionada ? VISIBLE : INVISIBLE);
            int cor = ContextCompat.getColor(getContext(), selecionada
                    ? R.color.verde_escuro_principal : R.color.cinza_icone);
            icones[i].setColorFilter(cor);
            textos[i].setTextColor(cor);
            textos[i].setTypeface(ResourcesCompat.getFont(getContext(), selecionada
                    ? R.font.nunito_bold : R.font.nunito_regular));
            abas[i].setContentDescription(getContext().getString(selecionada
                    ? R.string.sindico_aba_selecionada : i <= Aba.UNIDADES.ordinal()
                    ? R.string.sindico_aba_abrir : R.string.sindico_aba_indisponivel, textos[i].getText()));
        }
    }

    public void aoTocarHome(@Nullable View.OnClickListener acao) {
        View home = abas[Aba.HOME.ordinal()];
        home.setOnClickListener(acao);
        home.setFocusable(acao != null);
        home.setContentDescription(getContext().getString(acao == null
                ? R.string.sindico_home_do_sindico_aba_selecionada : R.string.sindico_voltar_a_home_do_sindico));
    }

    public void aoTocarSolicitacao(@Nullable View.OnClickListener acao) {
        View solicitacao = abas[Aba.SOLICITACAO.ordinal()];
        solicitacao.setOnClickListener(acao);
        solicitacao.setFocusable(acao != null);
        solicitacao.setContentDescription(getContext().getString(acao == null
                ? R.string.sindico_solicitacao_aba_selecionada : R.string.sindico_abrir_solicitacoes_de_coleta));
    }

    public void aoTocarHistorico(@Nullable View.OnClickListener acao) {
        View historico = abas[Aba.HISTORICO.ordinal()];
        historico.setOnClickListener(acao);
        historico.setFocusable(acao != null);
        historico.setContentDescription(getContext().getString(acao == null
                ? R.string.sindico_historico_aba_selecionada : R.string.sindico_abrir_historico_de_coletas));
    }

    public void aoTocarUnidades(@Nullable View.OnClickListener acao) {
        View unidades = abas[Aba.UNIDADES.ordinal()];
        unidades.setOnClickListener(acao);
        unidades.setFocusable(acao != null);
        unidades.setContentDescription(getContext().getString(acao == null
                ? R.string.sindico_unidades_aba_selecionada : R.string.sindico_abrir_dados_do_condominio));
    }
}
