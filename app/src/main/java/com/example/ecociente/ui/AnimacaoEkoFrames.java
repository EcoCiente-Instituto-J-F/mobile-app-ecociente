
package com.example.ecociente.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

public final class AnimacaoEkoFrames {

    private final FrameLayout container;
    private final ImageView imagemAtual;
    private final ImageView proximaImagem;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final int[] frames;
    private final long intervalo;
    private final long transicao;

    private int indiceAtual = 0;
    private boolean executando = false;

    private final Runnable proximoFrame = new Runnable() {
        @Override
        public void run() {
            if (!executando) return;
            trocarImagem();
        }
    };

    public AnimacaoEkoFrames(
            @NonNull FrameLayout container,
            @NonNull int[] frames,
            long intervalo,
            long transicao
    ) {
        if (frames.length == 0) {
            throw new IllegalArgumentException(
                    "Informe pelo menos um frame do Eko"
            );
        }

        this.container = container;
        this.frames = frames.clone();
        this.intervalo = Math.max(0, intervalo);
        this.transicao = Math.max(0, transicao);

        Context context = container.getContext();

        imagemAtual = criarImagem(context);
        proximaImagem = criarImagem(context);

        container.addView(imagemAtual);
        container.addView(proximaImagem);

        imagemAtual.setImageResource(this.frames[0]);
        imagemAtual.setAlpha(1f);

        proximaImagem.setAlpha(0f);
    }

    private ImageView criarImagem(Context context) {
        ImageView imagem = new ImageView(context);

        imagem.setScaleType(ImageView.ScaleType.FIT_CENTER);

        imagem.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        return imagem;
    }

    public void iniciar() {
        if (executando) return;

        executando = true;
        handler.postDelayed(proximoFrame, intervalo);
    }

    private void trocarImagem() {
        if (!executando || frames.length < 2) return;

        int proximoIndice = (indiceAtual + 1) % frames.length;

        proximaImagem.setImageResource(frames[proximoIndice]);
        proximaImagem.setAlpha(0f);

        imagemAtual.animate().cancel();
        proximaImagem.animate().cancel();

        proximaImagem.animate()
                .alpha(1f)
                .setDuration(transicao)
                .start();

        imagemAtual.animate()
                .alpha(0f)
                .setDuration(transicao)
                .withEndAction(() -> {
                    if (!executando) return;

                    indiceAtual = proximoIndice;

                    Drawable drawable = proximaImagem.getDrawable();

                    imagemAtual.setImageDrawable(drawable);
                    imagemAtual.setAlpha(1f);

                    proximaImagem.setAlpha(0f);
                    proximaImagem.setImageDrawable(null);

                    handler.postDelayed(proximoFrame, intervalo);
                })
                .start();
    }

    public void parar() {
        executando = false;

        handler.removeCallbacks(proximoFrame);

        imagemAtual.animate().cancel();
        proximaImagem.animate().cancel();

        imagemAtual.setImageResource(frames[indiceAtual]);
        imagemAtual.setAlpha(1f);

        proximaImagem.setAlpha(0f);
        proximaImagem.setImageDrawable(null);
    }

    public void reiniciar() {
        parar();
        indiceAtual = 0;
        imagemAtual.setImageResource(frames[0]);
        iniciar();
    }

    public void liberar() {
        parar();
        imagemAtual.setImageDrawable(null);
        proximaImagem.setImageDrawable(null);
    }
}
