package com.example.ecociente.ui;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.res.ResourcesCompat;

import com.example.ecociente.R;
import com.example.ecociente.model.DesempenhoQuiz;
import com.example.ecociente.util.FormatoDataApi;
import java.util.List;

public class QuizPerformanceView extends View {

    private static final int MAXIMO_DE_TENTATIVAS = 6;

    // Notas de 0 a 100 e rótulos de data, da tentativa mais antiga para a mais recente.
    private float[] valores = new float[0];
    private String[] datas = new String[0];
    private String mensagem = "";

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
                dp(40);

        float direita =
                getWidth() - dp(24);

        float topo =
                dp(36);

        float base =
                getHeight() - dp(30);

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

        if (valores.length == 0) {
            desenharMensagem(canvas, esquerda + largura / 2f, topo + altura / 2f);
            return;
        }

        desenharSerie(canvas, esquerda, direita, topo, base, largura, altura);

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
                dp(13.2f)
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

                dp(21),

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
                i < valores.length;
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
                                            Math.max(valores.length - 1f, 1f)
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
                dp(10.5f)
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
                i < valores.length;
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
                                            Math.max(valores.length - 1f, 1f)
                                    );

            float y =
                    base
                            -
                            (
                                    valores[i]
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
                dp(2.1f)
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
                i < valores.length;
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
                                            Math.max(valores.length - 1f, 1f)
                                    );

            float y =
                    base
                            -
                            (
                                    valores[i]
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
                    dp(3.8f),
                    tinta
            );


            tinta.setStyle(
                    Paint.Style.STROKE
            );

            tinta.setStrokeWidth(
                    dp(1.3f)
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
                    dp(3.8f),
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
                dp(10.2f)
        );

        tinta.setTextAlign(
                Paint.Align.CENTER
        );

        for (
                int i = 0;
                i < datas.length;
                i++
        ) {

            float x = esquerda + largura * i / (Math.max(datas.length - 1f, 1f));

            canvas.drawText(
                    datas[i],
                    x,
                    base + dp(19),
                    tinta
            );
        }
    }

    // A API entrega da tentativa mais recente para a mais antiga; o gráfico vai da mais antiga para a mais
    // recente e mostra só as últimas seis.
    public void mostrar(@NonNull List<DesempenhoQuiz> resultados) {

        int quantidade = Math.min(resultados.size(), MAXIMO_DE_TENTATIVAS);

        valores = new float[quantidade];
        datas = new String[quantidade];

        StringBuilder descricao = new StringBuilder(getContext().getString(R.string.quiz_desempenho_descricao));

        for (int i = 0; i < quantidade; i++) {
            DesempenhoQuiz desempenho = resultados.get(quantidade - 1 - i);

            valores[i] = (float) desempenho.nota;
            datas[i] = FormatoDataApi.diaMes(desempenho.concluidoEm);
            descricao.append(i == 0 ? ": " : "; ").append(datas[i]).append(", ").append(Math.round(valores[i])).append("%");
        }

        mensagem = quantidade == 0 ? getContext().getString(R.string.quiz_desempenho_vazio) : "";
        setContentDescription(quantidade == 0 ? mensagem : descricao.toString());
        invalidate();
    }

    public void mostrarCarregando() {
        limpar(R.string.quiz_desempenho_carregando);
    }

    public void mostrarErro() {
        limpar(R.string.quiz_desempenho_erro);
    }

    private void limpar(@StringRes int texto) {
        valores = new float[0];
        datas = new String[0];
        mensagem = getContext().getString(texto);
        setContentDescription(mensagem);
        invalidate();
    }

    private void desenharMensagem(Canvas canvas, float x, float y) {
        tinta.setShader(null);
        tinta.setPathEffect(null);
        tinta.setStyle(Paint.Style.FILL);
        tinta.setColor(Color.rgb(90, 90, 90));
        tinta.setTextSize(dp(12));
        tinta.setTextAlign(Paint.Align.CENTER);

        if (fonteRegular != null) {
            tinta.setTypeface(fonteRegular);
        }

        canvas.drawText(mensagem, x, y, tinta);
    }

    private float dp(float valor) {
        return valor * getResources().getDisplayMetrics().density;
    }
}