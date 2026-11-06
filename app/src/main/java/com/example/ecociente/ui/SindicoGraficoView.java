package com.example.ecociente.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.ecociente.R;

/** Evolução do descarte; os pontos vêm do modelo (API de dashboard). */
public final class SindicoGraficoView extends View {
    private final Paint tinta = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int[] valores = new int[0];
    private String[] datas = new String[0];
    private final Path linha = new Path();
    private final Path area = new Path();
    private DashPathEffect tracejado;
    private LinearGradient degrade;

    public SindicoGraficoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDados(int[] novosValores, String[] novasDatas) {
        if (novosValores != null && novasDatas != null
                && novosValores.length == novasDatas.length && novosValores.length > 1) {
            valores = novosValores.clone();
            datas = novasDatas.clone();
            setContentDescription(getContext().getString(R.string.sindico_grafico_descricao, valores[0], datas[0],
                    valores[valores.length - 1], datas[datas.length - 1]));
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int largura, int altura, int larguraAnterior, int alturaAnterior) {
        super.onSizeChanged(largura, altura, larguraAnterior, alturaAnterior);
        degrade = null;
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
        tinta.setColor(ContextCompat.getColor(getContext(), R.color.cinza_borda_clara_home));
        if (tracejado == null) {
            tracejado = new DashPathEffect(new float[] {dp(4), dp(4)}, 0);
        }
        tinta.setPathEffect(tracejado);
        for (int i = 0; i <= 3; i++) {
            float y = base - altura * i / 3f;
            canvas.drawLine(esquerda, y, direita, y, tinta);
        }
        tinta.setPathEffect(null);

        tinta.setStyle(Paint.Style.FILL);
        tinta.setColor(ContextCompat.getColor(getContext(), R.color.cinza_texto_home));
        tinta.setTextSize(11 * getResources().getDisplayMetrics().scaledDensity);
        for (int i = 0; i <= 3; i++) {
            canvas.drawText(String.valueOf(i * 100), dp(2), base - altura * i / 3f + dp(3), tinta);
        }

        linha.reset();
        for (int i = 0; i < valores.length; i++) {
            float x = esquerda + largura * i / (valores.length - 1);
            float y = base - altura * Math.min(300, Math.max(0, valores[i])) / 300f;
            if (i == 0) linha.moveTo(x, y);
            else linha.lineTo(x, y);
            String data = datas[i];
            canvas.drawText(data, x - tinta.measureText(data) / 2f, getHeight() - dp(6), tinta);
        }

        if (valores.length < 2) return;

        // área sob a linha, em rosa que some para baixo
        area.reset();
        area.addPath(linha);
        area.lineTo(direita, base);
        area.lineTo(esquerda, base);
        area.close();
        int rosa = ContextCompat.getColor(getContext(), R.color.rosa_ecociente);
        tinta.setStyle(Paint.Style.FILL);
        if (degrade == null) {
            degrade = new LinearGradient(0, topo, 0, base,
                    (rosa & 0x00FFFFFF) | 0x66000000, (rosa & 0x00FFFFFF) | 0x08000000,
                    Shader.TileMode.CLAMP);
        }
        tinta.setShader(degrade);
        canvas.drawPath(area, tinta);
        tinta.setShader(null);

        int verde = ContextCompat.getColor(getContext(), R.color.verde_escuro_principal);
        tinta.setColor(verde);
        tinta.setStyle(Paint.Style.STROKE);
        tinta.setStrokeWidth(dp(2));
        canvas.drawPath(linha, tinta);
        for (int i = 0; i < valores.length; i++) {
            float x = esquerda + largura * i / (valores.length - 1);
            float y = base - altura * Math.min(300, Math.max(0, valores[i])) / 300f;
            tinta.setStyle(Paint.Style.FILL);
            tinta.setColor(ContextCompat.getColor(getContext(), R.color.branco));
            canvas.drawCircle(x, y, dp(4), tinta);
            tinta.setStyle(Paint.Style.STROKE);
            tinta.setColor(verde);
            canvas.drawCircle(x, y, dp(4), tinta);
        }
    }

    private float dp(float valor) {
        return valor * getResources().getDisplayMetrics().density;
    }
}
