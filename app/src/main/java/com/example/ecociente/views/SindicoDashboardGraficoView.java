package com.example.ecociente.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.ecociente.R;

/** Gráfico de barras simples para os dados ilustrativos do dashboard. */
public final class SindicoDashboardGraficoView extends View {
    private final Paint tinta = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String[] nomes = new String[0];
    private int[] valores = new int[0];
    private int[] cores = new int[0];
    private int maximo = 30;

    public SindicoDashboardGraficoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDados(String[] nomes, int[] valores, int[] cores, int maximo) {
        if (nomes.length != valores.length || nomes.length != cores.length) {
            throw new IllegalArgumentException("Cada barra precisa de nome, valor e cor");
        }
        this.nomes = nomes.clone();
        this.valores = valores.clone();
        this.cores = cores.clone();
        this.maximo = Math.max(1, maximo);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (nomes.length == 0) return;
        float esquerda = dp(28);
        float direita = getWidth() - dp(5);
        float topo = dp(19);
        float base = getHeight() - dp(43);
        if (direita <= esquerda || base <= topo) return;

        tinta.setTextSize(sp(10));
        tinta.setTextAlign(Paint.Align.RIGHT);
        for (int i = 0; i <= 3; i++) {
            float y = base - (base - topo) * i / 3f;
            tinta.setColor(ContextCompat.getColor(getContext(), R.color.cinza_borda_clara_home));
            tinta.setStrokeWidth(dp(1));
            canvas.drawLine(esquerda, y, direita, y, tinta);
            tinta.setColor(ContextCompat.getColor(getContext(), R.color.cinza_texto_home));
            canvas.drawText(String.valueOf(maximo * i / 3), esquerda - dp(5), y + dp(3), tinta);
        }

        float faixa = (direita - esquerda) / nomes.length;
        float largura = Math.min(dp(38), faixa * 0.56f);
        for (int i = 0; i < nomes.length; i++) {
            float centro = esquerda + faixa * (i + 0.5f);
            float altura = Math.max(0, Math.min(valores[i], maximo)) * (base - topo) / maximo;
            tinta.setColor(ContextCompat.getColor(getContext(), cores[i]));
            canvas.drawRoundRect(centro - largura / 2, base - altura,
                    centro + largura / 2, base, dp(3), dp(3), tinta);
            tinta.setColor(ContextCompat.getColor(getContext(), R.color.verde_escuro_principal));
            tinta.setTextAlign(Paint.Align.CENTER);
            tinta.setTextSize(sp(10));
            tinta.setFakeBoldText(true);
            canvas.drawText(String.valueOf(valores[i]), centro, base - altura - dp(5), tinta);
            tinta.setFakeBoldText(false);
            tinta.setColor(ContextCompat.getColor(getContext(), R.color.cinza_texto_home));
            tinta.setTextSize(sp(9));
            String[] partes = nomes[i].split("\\n");
            for (int linha = 0; linha < partes.length; linha++) {
                canvas.drawText(partes[linha], centro, base + dp(15 + 12 * linha), tinta);
            }
        }
    }

    private float dp(float valor) {
        return valor * getResources().getDisplayMetrics().density;
    }

    private float sp(float valor) {
        return valor * getResources().getDisplayMetrics().scaledDensity;
    }
}
