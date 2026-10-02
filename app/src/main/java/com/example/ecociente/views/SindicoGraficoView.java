package com.example.ecociente.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.ecociente.R;

/** Gráfico de demonstração; os pontos vêm do modelo para futura troca da fonte de dados. */
public final class SindicoGraficoView extends View {
    private final Paint tinta = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int[] valores = {90, 130, 175, 185, 300};
    private String[] datas = {"01/mai", "08/mai", "15/mai", "22/mai", "29/mai"};

    public SindicoGraficoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDados(int[] novosValores, String[] novasDatas) {
        if (novosValores != null && novasDatas != null
                && novosValores.length == novasDatas.length && novosValores.length > 1) {
            valores = novosValores.clone();
            datas = novasDatas.clone();
            setContentDescription("Evolução dos descartes: " + valores[0] + " em " + datas[0]
                    + " até " + valores[valores.length - 1] + " em " + datas[datas.length - 1]);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float esquerda = dp(35);
        float direita = getWidth() - dp(13);
        float topo = dp(8);
        float base = getHeight() - dp(27);
        float largura = direita - esquerda;
        float altura = base - topo;
        if (largura <= 0 || altura <= 0) return;

        tinta.setStyle(Paint.Style.STROKE);
        tinta.setStrokeWidth(dp(1));
        tinta.setColor(ContextCompat.getColor(getContext(), R.color.sindico_borda));
        for (int i = 0; i <= 3; i++) {
            float y = base - altura * i / 3f;
            canvas.drawLine(esquerda, y, direita, y, tinta);
        }

        tinta.setStyle(Paint.Style.FILL);
        tinta.setColor(ContextCompat.getColor(getContext(), R.color.sindico_cinza));
        tinta.setTextSize(10 * getResources().getDisplayMetrics().scaledDensity);
        for (int i = 0; i <= 3; i++) {
            canvas.drawText(String.valueOf(i * 100), dp(2), base - altura * i / 3f + dp(3), tinta);
        }

        Path linha = new Path();
        for (int i = 0; i < valores.length; i++) {
            float x = esquerda + largura * i / (valores.length - 1);
            float y = base - altura * Math.min(300, Math.max(0, valores[i])) / 300f;
            if (i == 0) linha.moveTo(x, y);
            else linha.lineTo(x, y);
            String data = datas[i];
            canvas.drawText(data, x - tinta.measureText(data) / 2f, getHeight() - dp(6), tinta);
        }

        tinta.setColor(ContextCompat.getColor(getContext(), R.color.sindico_verde));
        tinta.setStyle(Paint.Style.STROKE);
        tinta.setStrokeWidth(dp(2));
        canvas.drawPath(linha, tinta);
        tinta.setStyle(Paint.Style.FILL);
        for (int i = 0; i < valores.length; i++) {
            float x = esquerda + largura * i / (valores.length - 1);
            float y = base - altura * Math.min(300, Math.max(0, valores[i])) / 300f;
            canvas.drawCircle(x, y, dp(3), tinta);
        }
    }

    private float dp(float valor) {
        return valor * getResources().getDisplayMetrics().density;
    }
}
