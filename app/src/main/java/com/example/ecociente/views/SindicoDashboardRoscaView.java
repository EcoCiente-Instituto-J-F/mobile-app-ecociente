package com.example.ecociente.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.ecociente.R;

/** Distribuição ilustrativa de materiais, sem dependência de serviços externos. */
public final class SindicoDashboardRoscaView extends View {
    private final Paint tinta = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final int[] PERCENTUAIS = {25, 50, 8, 12, 5};
    private static final int[] CORES = {R.color.verde_escuro_principal,
            R.color.rosa_ecociente, R.color.verde_claro_ecociente,
            R.color.amarelo_avaliacao, R.color.amarelo_botao_avaliar};

    public SindicoDashboardRoscaView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float raio = Math.min(getWidth(), getHeight()) / 2f - dp(19);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        RectF area = new RectF(cx - raio, cy - raio, cx + raio, cy + raio);
        tinta.setStyle(Paint.Style.STROKE);
        tinta.setStrokeWidth(dp(29));
        float inicio = -90;
        for (int i = 0; i < PERCENTUAIS.length; i++) {
            float angulo = PERCENTUAIS[i] * 3.6f;
            tinta.setColor(ContextCompat.getColor(getContext(), CORES[i]));
            canvas.drawArc(area, inicio, angulo, false, tinta);
            inicio += angulo;
        }
        tinta.setStyle(Paint.Style.FILL);
        tinta.setColor(ContextCompat.getColor(getContext(), R.color.verde_escuro_principal));
        tinta.setTextAlign(Paint.Align.CENTER);
        tinta.setFakeBoldText(true);
        tinta.setTextSize(sp(18));
        canvas.drawText("100%", cx, cy + dp(6), tinta);
        tinta.setFakeBoldText(false);
    }

    private float dp(float valor) {
        return valor * getResources().getDisplayMetrics().density;
    }

    private float sp(float valor) {
        return valor * getResources().getDisplayMetrics().scaledDensity;
    }
}
