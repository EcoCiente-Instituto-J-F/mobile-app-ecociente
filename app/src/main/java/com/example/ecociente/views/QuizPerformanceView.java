package com.example.ecociente.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.example.ecociente.R;

public class QuizPerformanceView extends View {

    private static final int[] VALORES = {
            70,
            50,
            80,
            90,
            70,
            70
    };

    private static final String[] DATAS = {
            "01/05",
            "08/05",
            "15/05",
            "22/05",
            "29/05",
            "05/06"
    };

    private final Paint tinta =
            new Paint(
                    Paint.ANTI_ALIAS_FLAG
            );

    private final Path caminhoLinha =
            new Path();

    private final Path caminhoArea =
            new Path();

    private final Typeface fonteRegular;
    private final Typeface fonteBold;

    public QuizPerformanceView(
            Context contexto,
            @Nullable AttributeSet atributos
    ) {

        super(
                contexto,
                atributos
        );

        fonteRegular =
                ResourcesCompat.getFont(
                        contexto,
                        R.font.nunito_regular
                );

        fonteBold =
                ResourcesCompat.getFont(
                        contexto,
                        R.font.nunito_bold
                );

        if (fonteRegular != null) {
            tinta.setTypeface(fonteRegular);
        }
    }

    @Override
    protected void onDraw(
            Canvas canvas
    ) {

        super.onDraw(canvas);

        float esquerda =
                dp(34);

        float direita =
                getWidth() - dp(24);

        float topo =
                dp(29);

        float base =
                getHeight() - dp(25);

        float largura =
                direita - esquerda;

        float altura =
                base - topo;

        if (
                largura <= 0
                        || altura <= 0
        ) {
            return;
        }

        desenharTitulo(
                canvas
        );

        desenharGrade(
                canvas,
                esquerda,
                direita,
                topo,
                base,
                altura
        );

        desenharSerie(
                canvas,
                esquerda,
                direita,
                topo,
                base,
                largura,
                altura
        );

        desenharDatas(
                canvas,
                esquerda,
                largura,
                base
        );
    }

    private void desenharTitulo(
            Canvas canvas
    ) {

        tinta.setShader(null);

        tinta.setPathEffect(null);

        tinta.setStyle(
                Paint.Style.FILL
        );

        tinta.setColor(
                Color.rgb(
                        36,
                        36,
                        36
                )
        );

        tinta.setTextSize(
                dp(10.3f)
        );

        tinta.setTextAlign(
                Paint.Align.CENTER
        );

        if (fonteBold != null) {
            tinta.setTypeface(fonteBold);
        }

        canvas.drawText(
                "Desempenho nos últimos quizzes",

                getWidth() / 2f,

                dp(16),

                tinta
        );
    }

    private void desenharGrade(
            Canvas canvas,
            float esquerda,
            float direita,
            float topo,
            float base,
            float altura
    ) {

        tinta.setShader(null);

        if (fonteRegular != null) {
            tinta.setTypeface(fonteRegular);
        }

        tinta.setStyle(
                Paint.Style.STROKE
        );

        tinta.setStrokeWidth(
                dp(0.7f)
        );

        tinta.setColor(
                Color.rgb(
                        214,
                        220,
                        217
                )
        );

        tinta.setPathEffect(
                new DashPathEffect(
                        new float[]{
                                dp(2),
                                dp(2)
                        },
                        0
                )
        );

        for (
                int i = 0;
                i <= 2;
                i++
        ) {

            float y =
                    base
                            -
                            (
                                    altura
                                            *
                                            i
                                            /
                                            2f
                            );

            canvas.drawLine(
                    esquerda,
                    y,
                    direita,
                    y,
                    tinta
            );
        }

        for (
                int i = 0;
                i < VALORES.length;
                i++
        ) {

            float x =
                    esquerda
                            +
                            (
                                    direita
                                            -
                                            esquerda
                            )
                                    *
                                    i
                                    /
                                    (
                                            VALORES.length
                                                    -
                                                    1f
                                    );

            canvas.drawLine(
                    x,
                    topo,
                    x,
                    base,
                    tinta
            );
        }

        tinta.setPathEffect(
                null
        );

        tinta.setStyle(
                Paint.Style.FILL
        );

        tinta.setTextSize(
                dp(9)
        );

        tinta.setTextAlign(
                Paint.Align.RIGHT
        );

        tinta.setColor(
                Color.rgb(
                        102,
                        102,
                        102
                )
        );

        canvas.drawText(
                "100",
                esquerda - dp(6),
                topo + dp(3),
                tinta
        );

        canvas.drawText(
                "50",
                esquerda - dp(6),
                base - altura / 2f + dp(3),
                tinta
        );

        canvas.drawText(
                "0",
                esquerda - dp(6),
                base + dp(3),
                tinta
        );
    }

    private void desenharSerie(
            Canvas canvas,
            float esquerda,
            float direita,
            float topo,
            float base,
            float largura,
            float altura
    ) {

        caminhoLinha.reset();
        caminhoArea.reset();

        for (
                int i = 0;
                i < VALORES.length;
                i++
        ) {

            float x =
                    esquerda
                            +
                            largura
                                    *
                                    i
                                    /
                                    (
                                            VALORES.length
                                                    -
                                                    1f
                                    );

            float y =
                    base
                            -
                            (
                                    VALORES[i]
                                            /
                                            100f
                                            *
                                            altura
                            );

            if (i == 0) {

                caminhoLinha.moveTo(
                        x,
                        y
                );

                caminhoArea.moveTo(
                        x,
                        base
                );

                caminhoArea.lineTo(
                        x,
                        y
                );

            } else {

                caminhoLinha.lineTo(
                        x,
                        y
                );

                caminhoArea.lineTo(
                        x,
                        y
                );
            }
        }

        caminhoArea.lineTo(
                direita,
                base
        );

        caminhoArea.close();


        LinearGradient gradiente =
                new LinearGradient(

                        0,
                        topo,

                        0,
                        base,

                        Color.argb(
                                62,
                                6,
                                78,
                                59
                        ),

                        Color.argb(
                                8,
                                6,
                                78,
                                59
                        ),

                        Shader.TileMode.CLAMP
                );


        tinta.setStyle(
                Paint.Style.FILL
        );

        tinta.setShader(
                gradiente
        );

        canvas.drawPath(
                caminhoArea,
                tinta
        );

        tinta.setShader(
                null
        );


        tinta.setStyle(
                Paint.Style.STROKE
        );

        tinta.setStrokeWidth(
                dp(1.7f)
        );

        tinta.setStrokeCap(
                Paint.Cap.ROUND
        );

        tinta.setStrokeJoin(
                Paint.Join.ROUND
        );

        tinta.setColor(
                Color.rgb(
                        6,
                        78,
                        59
                )
        );

        canvas.drawPath(
                caminhoLinha,
                tinta
        );


        tinta.setStyle(
                Paint.Style.FILL
        );

        for (
                int i = 0;
                i < VALORES.length;
                i++
        ) {

            float x =
                    esquerda
                            +
                            largura
                                    *
                                    i
                                    /
                                    (
                                            VALORES.length
                                                    -
                                                    1f
                                    );

            float y =
                    base
                            -
                            (
                                    VALORES[i]
                                            /
                                            100f
                                            *
                                            altura
                            );


            tinta.setColor(
                    Color.WHITE
            );

            canvas.drawCircle(
                    x,
                    y,
                    dp(3.2f),
                    tinta
            );


            tinta.setStyle(
                    Paint.Style.STROKE
            );

            tinta.setStrokeWidth(
                    dp(1.1f)
            );

            tinta.setColor(
                    Color.rgb(
                            6,
                            78,
                            59
                    )
            );

            canvas.drawCircle(
                    x,
                    y,
                    dp(3.2f),
                    tinta
            );


            tinta.setStyle(
                    Paint.Style.FILL
            );
        }
    }

    private void desenharDatas(
            Canvas canvas,
            float esquerda,
            float largura,
            float base
    ) {

        tinta.setShader(null);

        tinta.setPathEffect(null);

        tinta.setStyle(
                Paint.Style.FILL
        );

        tinta.setColor(
                Color.rgb(
                        102,
                        102,
                        102
                )
        );

        if (fonteRegular != null) {
            tinta.setTypeface(fonteRegular);
        }

        tinta.setTextSize(
                dp(8.8f)
        );

        tinta.setTextAlign(
                Paint.Align.CENTER
        );

        for (
                int i = 0;
                i < DATAS.length;
                i++
        ) {

            float x = esquerda + largura * i / (DATAS.length - 1f);

            canvas.drawText(
                    DATAS[i],
                    x,
                    base + dp(15),
                    tinta
            );
        }
    }

    private float dp(float valor) {
        return valor * getResources().getDisplayMetrics().density;
    }
}