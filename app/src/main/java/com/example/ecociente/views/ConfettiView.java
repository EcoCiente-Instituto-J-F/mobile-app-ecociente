package com.example.ecociente.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Confete leve do EcoCiente, sem bibliotecas externas.
 *
 * As particulas nascem nos dois cantos inferiores da tela, explodem na diagonal
 * para o centro e depois caem com gravidade. Os emissores sao invisiveis.
 */
public class ConfettiView extends View {

    private static final int QUANTIDADE = 230;
    private static final long DURACAO_MS = 3800L;
    private static final int MAX_TENTATIVAS_MEDICAO = 8;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Particula> particulas = new ArrayList<>();
    private final Random random = new Random();

    private ValueAnimator animator;
    private float progresso;

    public ConfettiView(Context context) {
        super(context);
        inicializar();
    }

    public ConfettiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inicializar();
    }

    public ConfettiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inicializar();
    }

    private void inicializar() {
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setVisibility(GONE);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public void lancar() {
        cancelar();

        // IMPORTANTE: a View estava GONE quando as particulas eram criadas.
        // Nesse estado width/height podem ser 0 e os confetes acabavam nascendo
        // perto do canto superior esquerdo. Primeiro tornamos a camada visivel,
        // depois aguardamos a medicao real da tela.
        setAlpha(1f);
        setVisibility(VISIBLE);
        bringToFront();

        post(() -> iniciarQuandoMedido(0));
    }

    private void iniciarQuandoMedido(int tentativa) {
        if (getWidth() <= 1 || getHeight() <= 1) {
            if (tentativa < MAX_TENTATIVAS_MEDICAO) {
                postDelayed(() -> iniciarQuandoMedido(tentativa + 1), 16L);
            } else {
                setVisibility(GONE);
            }
            return;
        }

        criarParticulas();
        progresso = 0f;

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(DURACAO_MS);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            progresso = (float) animation.getAnimatedValue();

            if (progresso > .92f) {
                setAlpha(1f - ((progresso - .92f) / .08f));
            }

            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                finalizarAnimacao();
            }
        });
        animator.start();
    }

    public void cancelar() {
        if (animator != null) {
            animator.removeAllListeners();
            animator.cancel();
            animator = null;
        }
        finalizarAnimacao();
    }

    private void finalizarAnimacao() {
        animator = null;
        particulas.clear();
        setVisibility(GONE);
        setAlpha(1f);
        progresso = 0f;
        invalidate();
    }

    private void criarParticulas() {
        particulas.clear();

        int[] cores = {
                0xFF064E3B,
                0xFF075B44,
                0xFF0B6B4F,
                0xFF0E7A57,
                0xFF138B60,
                0xFF1C9A67,
                0xFF31AA6A,
                0xFF56B86B,
                0xFF78C56B,
                0xFF9BD16B,
                0xFFB9DC78,
                0xFFD6E8A3
        };

        int largura = getWidth();
        int altura = getHeight();

        for (int i = 0; i < QUANTIDADE; i++) {
            boolean esquerda = i % 2 == 0;

            Particula p = new Particula();

            // O "canhao" e invisivel, mas a origem fica dentro dos cantos
            // inferiores para a explosao aparecer imediatamente.
            p.xInicial = esquerda
                    ? dp(randomEntre(8, 30))
                    : largura - dp(randomEntre(8, 30));
            p.yInicial = altura - dp(randomEntre(10, 34));

            float direcao = esquerda ? 1f : -1f;

            // Explosao forte, diagonal e aberta em leque.
            p.velocidadeX = direcao * largura * randomEntreFloat(.34f, .88f);
            p.velocidadeY = -altura * randomEntreFloat(1.45f, 2.05f);

            // Faz o confete subir, perder forca e voltar a cair ainda durante
            // a animacao, em vez de apenas atravessar a tela para cima.
            p.gravidade = altura * randomEntreFloat(2.65f, 3.55f);

            p.tamanho = dp(randomEntre(5, 12));
            p.rotacaoInicial = random.nextFloat() * 360f;
            p.rotacoes = randomEntre(2, 8) * 360f * (random.nextBoolean() ? 1f : -1f);
            p.fase = random.nextFloat() * 6.28f;

            // Pequenos atrasos deixam o efeito parecido com duas rajadas de
            // confete, sem mostrar qualquer bastao/canhao na tela.
            p.atraso = randomEntreFloat(0f, .08f);
            if (i % 7 == 0) {
                p.atraso += randomEntreFloat(.08f, .15f);
            }

            p.cor = cores[random.nextInt(cores.length)];
            p.tipo = random.nextInt(3);
            particulas.add(p);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Particula p : particulas) {
            if (progresso < p.atraso) {
                continue;
            }

            float t = (progresso - p.atraso) / (1f - p.atraso);
            t = Math.max(0f, Math.min(1f, t));

            float x = p.xInicial
                    + (p.velocidadeX * t)
                    + ((float) Math.sin((t * 14f) + p.fase) * dp(8));

            float y = p.yInicial
                    + (p.velocidadeY * t)
                    + (.5f * p.gravidade * t * t);

            float rotacao = p.rotacaoInicial + (p.rotacoes * t);

            float alphaEntrada = Math.min(1f, t / .035f);
            float alphaSaida = Math.min(1f, (1f - t) / .13f);
            int alpha = (int) (255 * alphaEntrada * alphaSaida);

            paint.setColor(p.cor);
            paint.setAlpha(Math.max(0, Math.min(255, alpha)));

            canvas.save();
            canvas.rotate(rotacao, x, y);

            if (p.tipo == 0) {
                RectF rect = new RectF(
                        x - p.tamanho * .78f,
                        y - p.tamanho * .24f,
                        x + p.tamanho * .78f,
                        y + p.tamanho * .24f
                );
                canvas.drawRoundRect(rect, dp(1.7f), dp(1.7f), paint);
            } else if (p.tipo == 1) {
                RectF rect = new RectF(
                        x - p.tamanho * .40f,
                        y - p.tamanho * .67f,
                        x + p.tamanho * .40f,
                        y + p.tamanho * .67f
                );
                canvas.drawRoundRect(rect, dp(1.5f), dp(1.5f), paint);
            } else {
                canvas.drawCircle(x, y, p.tamanho * .34f, paint);
            }

            canvas.restore();
        }

        paint.setAlpha(255);
    }

    private int randomEntre(int minimo, int maximo) {
        return minimo + random.nextInt(Math.max(1, maximo - minimo + 1));
    }

    private float randomEntreFloat(float minimo, float maximo) {
        return minimo + (random.nextFloat() * (maximo - minimo));
    }

    private float dp(float valor) {
        return valor * getResources().getDisplayMetrics().density;
    }

    private static final class Particula {
        float xInicial;
        float yInicial;
        float velocidadeX;
        float velocidadeY;
        float gravidade;
        float tamanho;
        float rotacaoInicial;
        float rotacoes;
        float fase;
        float atraso;
        int cor;
        int tipo;
    }
}