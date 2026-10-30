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
import androidx.core.content.res.ResourcesCompat;

import com.example.ecociente.R;
import com.example.ecociente.model.DesempenhoQuiz;
import com.example.ecociente.util.FormatoDataApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizPerformanceView extends View {

    private enum Estado {
        DADOS,
        CARREGANDO,
        ERRO,
        VAZIO
    }

    private Estado estadoAtual = Estado.DADOS;

    private float[] valores = new float[]{70, 50, 80, 90, 70, 70};

    private String[] datas = new String[]{"01/05", "08/05", "15/05", "22/05", "29/05", "05/06"};

    private final Paint tinta = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path caminhoLinha = new Path();

    private final Path caminhoArea = new Path();

    private final Typeface fonteRegular;
    private final Typeface fonteBold;

    public QuizPerformanceView(Context contexto, @Nullable AttributeSet atributos) {
        super(contexto, atributos);

        fonteRegular = ResourcesCompat.getFont(contexto, R.font.nunito_regular);
        fonteBold = ResourcesCompat.getFont(contexto, R.font.nunito_bold);

        if (fonteRegular != null) {
            tinta.setTypeface(fonteRegular);
        }
    }

    public void mostrarCarregando() {
        estadoAtual = Estado.CARREGANDO;
        invalidate();
    }

    public void mostrarErro() {
        estadoAtual = Estado.ERRO;
        invalidate();
    }

    public void mostrar(@Nullable List<DesempenhoQuiz> historico) {
        if (historico == null) {
            mostrarErro();
            return;
        }

        if (historico.isEmpty()) {
            estadoAtual = Estado.VAZIO;
            invalidate();
            return;
        }

        int limite = Math.min(historico.size(), 6);
        List<DesempenhoQuiz> recentes = new ArrayList<>(historico.subList(0, limite));
        Collections.reverse(recentes);

        valores = new float[recentes.size()];
        datas = new String[recentes.size()];

        for (int i = 0; i < recentes.size(); i++) {
            DesempenhoQuiz item = recentes.get(i);
            valores[i] = (float) item.nota;
            String dataDiaMes = FormatoDataApi.diaMes(item.concluidoEm);
            datas[i] = !dataDiaMes.isEmpty() ? dataDiaMes : item.concluidoEm;
        }

        estadoAtual = Estado.DADOS;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float esquerda = dp(40);
        float direita = getWidth() - dp(24);
        float topo = dp(36);
        float base = getHeight() - dp(30);
        float largura = direita - esquerda;
        float altura = base - topo;

        if (largura <= 0 || altura <= 0) {
            return;
        }

        desenharTitulo(canvas);

        if (estadoAtual == Estado.CARREGANDO) {
            desenharMensagemCentral(canvas, getContext().getString(R.string.quiz_desempenho_carregando));
            return;
        }

        if (estadoAtual == Estado.ERRO) {
            desenharMensagemCentral(canvas, getContext().getString(R.string.quiz_desempenho_erro));
            return;
        }

        if (estadoAtual == Estado.VAZIO || valores == null || valores.length == 0) {
            desenharMensagemCentral(canvas, getContext().getString(R.string.quiz_desempenho_vazio));
            return;
        }

        desenharGrade(canvas, esquerda, direita, topo, base, altura);
        desenharSerie(canvas, esquerda, direita, topo, base, largura, altura);
        desenharDatas(canvas, esquerda, largura, base);
    }

    private void desenharMensagemCentral(Canvas canvas, String mensagem) {
        tinta.setShader(null);
        tinta.setPathEffect(null);
        tinta.setStyle(Paint.Style.FILL);
        tinta.setColor(Color.rgb(102, 102, 102));
        tinta.setTextSize(dp(12f));
        tinta.setTextAlign(Paint.Align.CENTER);
        if (fonteRegular != null) {
            tinta.setTypeface(fonteRegular);
        }
        canvas.drawText(mensagem, getWidth() / 2f, getHeight() / 2f + dp(10), tinta);
    }

    private void desenharTitulo(Canvas canvas) {
        tinta.setShader(null);
        tinta.setPathEffect(null);
        tinta.setStyle(Paint.Style.FILL);
        tinta.setColor(Color.rgb(36, 36, 36));
        tinta.setTextSize(dp(13.2f));
        tinta.setTextAlign(Paint.Align.CENTER);

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

        tinta.setStyle(Paint.Style.STROKE);
        tinta.setStrokeWidth(dp(0.7f));
        tinta.setColor(Color.rgb(214, 220, 217));
        tinta.setPathEffect(new DashPathEffect(new float[]{dp(2), dp(2)}, 0));

        for (int i = 0; i <= 2; i++) {
            float y = base - (altura * i / 2f);
            canvas.drawLine(esquerda, y, direita, y, tinta);
        }

        for (int i = 0; i < valores.length; i++) {
            float x = calcularX(i, esquerda, larguraHorizontal(esquerda, direita));
            canvas.drawLine(x, topo, x, base, tinta);
        }

        tinta.setPathEffect(null);
        tinta.setStyle(Paint.Style.FILL);
        tinta.setTextSize(dp(10.5f));
        tinta.setTextAlign(Paint.Align.RIGHT);
        tinta.setColor(Color.rgb(102, 102, 102));

        canvas.drawText("100", esquerda - dp(6), topo + dp(3), tinta);
        canvas.drawText("50", esquerda - dp(6), base - altura / 2f + dp(3), tinta);
        canvas.drawText("0", esquerda - dp(6), base + dp(3), tinta);
    }

    private float larguraHorizontal(float esquerda, float direita) {
        return direita - esquerda;
    }

    private float calcularX(int indice, float esquerda, float largura) {
        if (valores.length <= 1) {
            return esquerda + largura / 2f;
        }
        return esquerda + largura * indice / (valores.length - 1f);
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

        for (int i = 0; i < valores.length; i++) {
            float x = calcularX(i, esquerda, largura);
            float y = base - (Math.min(100f, Math.max(0f, valores[i])) / 100f * altura);

            if (i == 0) {
                caminhoLinha.moveTo(x, y);
                caminhoArea.moveTo(x, base);
                caminhoArea.lineTo(x, y);
            } else {
                caminhoLinha.lineTo(x, y);
                caminhoArea.lineTo(x, y);
            }
        }

        if (valores.length == 1) {
            float xUnico = calcularX(0, esquerda, largura);
            caminhoArea.lineTo(xUnico, base);
        } else {
            caminhoArea.lineTo(direita, base);
        }

        caminhoArea.close();

        LinearGradient gradiente = new LinearGradient(
                0, topo,
                0, base,
                Color.argb(62, 6, 78, 59),
                Color.argb(8, 6, 78, 59),
                Shader.TileMode.CLAMP
        );

        tinta.setStyle(Paint.Style.FILL);
        tinta.setShader(gradiente);
        canvas.drawPath(caminhoArea, tinta);
        tinta.setShader(null);

        tinta.setStyle(Paint.Style.STROKE);
        tinta.setStrokeWidth(dp(2.1f));
        tinta.setStrokeCap(Paint.Cap.ROUND);
        tinta.setStrokeJoin(Paint.Join.ROUND);
        tinta.setColor(Color.rgb(6, 78, 59));

        if (valores.length > 1) {
            canvas.drawPath(caminhoLinha, tinta);
        }

        tinta.setStyle(Paint.Style.FILL);

        for (int i = 0; i < valores.length; i++) {
            float x = calcularX(i, esquerda, largura);
            float y = base - (Math.min(100f, Math.max(0f, valores[i])) / 100f * altura);

            tinta.setColor(Color.WHITE);
            canvas.drawCircle(x, y, dp(3.8f), tinta);

            tinta.setStyle(Paint.Style.STROKE);
            tinta.setStrokeWidth(dp(1.3f));
            tinta.setColor(Color.rgb(6, 78, 59));
            canvas.drawCircle(x, y, dp(3.8f), tinta);

            tinta.setStyle(Paint.Style.FILL);
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
        tinta.setStyle(Paint.Style.FILL);
        tinta.setColor(Color.rgb(102, 102, 102));

        if (fonteRegular != null) {
            tinta.setTypeface(fonteRegular);
        }

        tinta.setTextSize(dp(10.2f));
        tinta.setTextAlign(Paint.Align.CENTER);

        for (int i = 0; i < datas.length; i++) {
            float x = calcularX(i, esquerda, largura);
            canvas.drawText(datas[i], x, base + dp(19), tinta);
        }
    }

    private float dp(float valor) {
        return valor * getResources().getDisplayMetrics().density;
    }
}
