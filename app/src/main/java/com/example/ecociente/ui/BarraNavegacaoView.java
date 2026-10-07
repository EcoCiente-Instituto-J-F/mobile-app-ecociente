package com.example.ecociente.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import com.example.ecociente.R;

public class BarraNavegacaoView extends FrameLayout {

    public static final int QUANTIDADE_ITENS = 4;

    private static final int ALTURA_MINIMA_DP = 89;
    private static final int[] DESLOCAMENTO_ITENS_DP = {5, 2, -2, -5};
    private static final int POSICAO_ESPACO_ASSISTENTE = 2;

    public interface OnItemClickListener {
        void aoClicar(int indice);
    }

    private final View indicador;
    private final View assistente;
    private final View[] itens = new View[QUANTIDADE_ITENS];
    private final ImageView[] icones = new ImageView[QUANTIDADE_ITENS];
    private final TextView[] textos = new TextView[QUANTIDADE_ITENS];

    private final Typeface fonteAtiva;
    private final Typeface fonteInativa;

    private ItemBarra[] descricao = new ItemBarra[0];
    private int selecionado = -1;

    @Nullable private OnItemClickListener ouvinte;

    public BarraNavegacaoView(@NonNull Context contexto, @Nullable AttributeSet atributos) {
        super(contexto, atributos);

        setClipChildren(false);
        setClipToPadding(false);
        setMinimumHeight(Math.round(ALTURA_MINIMA_DP * getResources().getDisplayMetrics().density));

        LayoutInflater inflador = LayoutInflater.from(contexto);

        inflador.inflate(R.layout.view_barra_navegacao, this, true);

        indicador = findViewById(R.id.indicadorBarraNavegacao);
        assistente = findViewById(R.id.botaoAssistenteBarra);

        fonteAtiva = ResourcesCompat.getFont(contexto, R.font.nunito_semibold);
        fonteInativa = ResourcesCompat.getFont(contexto, R.font.nunito_regular);

        montarItens(inflador, findViewById(R.id.linhaItensBarraNavegacao));

        Motion.pressFeedback(assistente);
    }

    public void configurar(@NonNull ItemBarra... novosItens) {

        if (novosItens.length != QUANTIDADE_ITENS) {
            throw new IllegalArgumentException("A barra precisa de exatamente 4 itens");
        }

        descricao = novosItens;

        for (int i = 0; i < QUANTIDADE_ITENS; i++) {
            textos[i].setText(descricao[i].rotulo);
            itens[i].setContentDescription(getContext().getString(descricao[i].rotulo));
        }

        aplicarEstado(selecionado);
    }

    public void selecionar(int indice) {

        aplicarEstado(indice);

        indicador.setVisibility(indice == ItensBarra.NENHUM ? INVISIBLE : VISIBLE);

        if (indice == ItensBarra.NENHUM) {
            return;
        }

        IndicadorNavegacao.posicionarSobre(indicador, itens[indice]);
    }

    public void selecionar(int indice, int indiceOrigem) {

        if (indiceOrigem < 0 || indice < 0 || indiceOrigem == indice) {
            selecionar(indice);
            return;
        }

        aplicarEstado(indiceOrigem);

        IndicadorNavegacao.deslizar(indicador, itens[indiceOrigem], itens[indice]);

        indicador.postDelayed(() -> aplicarEstado(indice), Motion.ENTER_MS);
    }

    public void setOnItemClickListener(@Nullable OnItemClickListener novoOuvinte) {
        ouvinte = novoOuvinte;
    }

    public void setOnAssistenteClickListener(@Nullable OnClickListener acao) {
        assistente.setOnClickListener(acao);
    }

    private void montarItens(LayoutInflater inflador, LinearLayout linha) {

        float densidade = getResources().getDisplayMetrics().density;

        for (int i = 0; i < QUANTIDADE_ITENS; i++) {

            if (i == POSICAO_ESPACO_ASSISTENTE) {
                linha.addView(new Space(getContext()), new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
            }

            View item = inflador.inflate(R.layout.item_barra_navegacao, linha, false);

            item.setTranslationX(DESLOCAMENTO_ITENS_DP[i] * densidade);

            int indice = i;
            item.setOnClickListener(
                    view -> {
                        if (ouvinte != null) {
                            ouvinte.aoClicar(indice);
                        }
                    });

            Motion.pressFeedback(item);

            itens[i] = item;
            icones[i] = item.findViewById(R.id.iconeItemBarra);
            textos[i] = item.findViewById(R.id.textoItemBarra);

            linha.addView(item);
        }
    }

    private void aplicarEstado(int indiceAtivo) {

        selecionado = indiceAtivo;

        for (int i = 0; i < descricao.length; i++) {

            boolean ativo = i == indiceAtivo;

            int cor =
                    ContextCompat.getColor(
                            getContext(),
                            ativo ? R.color.verde_escuro_principal : R.color.cinza_secundario_home);

            icones[i].setImageResource(ativo ? descricao[i].iconeAtivo : descricao[i].iconeInativo);

            if (descricao[i].iconeAtivo == descricao[i].iconeInativo) {
                icones[i].setColorFilter(cor);
            } else {
                icones[i].clearColorFilter();
            }

            textos[i].setTextColor(cor);

            textos[i].setTypeface(ativo ? fonteAtiva : fonteInativa);
        }
    }
}
