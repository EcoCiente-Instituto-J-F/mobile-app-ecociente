package com.example.ecociente.midias;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.signature.ObjectKey;
import com.example.ecociente.R;
import java.util.concurrent.atomic.AtomicLong;

/** Carregamento ligado ao ciclo de vida da tela. Falhas sao assincronas (nao try/asGif). */
public final class CarregadorMidias {
    private static final AtomicLong SEQUENCIA = new AtomicLong();
    private CarregadorMidias() { }

    public static void mostrar(ImageView view, @Nullable Midia midia, boolean foto, int fallback) {
        mostrar(view, midia, foto, fallback, false, null);
    }

    public static void mostrar(ImageView view, @Nullable Midia midia, boolean foto, int fallback,
                               boolean exigirGifEko, @Nullable Runnable falha) {
        if (!contextoAtivo(view)) return;
        limpar(view);
        long sequencia = SEQUENCIA.incrementAndGet();
        view.setTag(R.id.tagEcoMidia, sequencia);
        view.setImageTintList(null);
        view.setColorFilter(null);
        view.setScaleType(foto ? ImageView.ScaleType.CENTER_CROP : ImageView.ScaleType.FIT_CENTER);
        if (fallback != 0) view.setImageResource(fallback);
        if (midia == null) {
            avisarFalha(view, sequencia, falha);
            return;
        }
        Object modelo;
        try {
            if ("drawable".equals(midia.origem)) {
                int recurso = view.getResources().getIdentifier(midia.caminho, "drawable", view.getContext().getPackageName());
                if (recurso == 0) throw new IllegalArgumentException("Drawable ausente");
                modelo = recurso;
            } else if ("asset".equals(midia.origem)) {
                if (!midia.caminho.startsWith("midias/") || midia.caminho.contains(".."))
                    throw new IllegalArgumentException("Asset invalido");
                modelo = Uri.parse("file:///android_asset/" + midia.caminho);
            } else {
                modelo = EnderecoMidia.cloudinary(midia, foto);
            }
        } catch (Exception e) {
            avisarFalha(view, sequencia, falha);
            return;
        }
        RequestBuilder<Drawable> pedido = Glide.with(view).asDrawable().load(modelo)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .signature(new ObjectKey(midia.id + ":" + midia.versao))
                .dontAnimate();
        // Limite de decodificacao, incluindo GIFs: nunca pede o original gigante.
        if (foto) pedido = pedido.override(960, 540).centerCrop();
        else if (midia.animada) pedido = pedido.override(280, 280).fitCenter();
        else pedido = pedido.override(640, 640).fitCenter();
        if (fallback != 0 && !exigirGifEko) pedido = pedido.placeholder(fallback).error(fallback);
        pedido.listener(new RequestListener<Drawable>() {
            @Override public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                  Target<Drawable> target, boolean primeira) {
                avisarFalha(view, sequencia, falha);
                return exigirGifEko;
            }
            @Override public boolean onResourceReady(Drawable resource, Object model,
                    Target<Drawable> target, DataSource fonte, boolean primeira) {
                if (exigirGifEko && (!midia.ehGifEko() || !(resource instanceof GifDrawable)
                        || ((GifDrawable) resource).getFrameCount() < 2)) {
                    avisarFalha(view, sequencia, falha);
                    return true;
                }
                if (resource instanceof GifDrawable) ((GifDrawable) resource).setLoopCount(GifDrawable.LOOP_FOREVER);
                return false; // O ImageViewTarget do Glide cuida de iniciar/parar a animacao.
            }
        }).into(view);
    }

    private static void avisarFalha(ImageView view, long sequencia, @Nullable Runnable falha) {
        if (falha == null) return;
        // Nunca inicia outra requisicao de dentro do callback interno do Glide.
        view.post(() -> {
            Object atual = view.getTag(R.id.tagEcoMidia);
            if (contextoAtivo(view) && atual instanceof Long && ((Long) atual) == sequencia) falha.run();
        });
    }

    public static void limpar(ImageView view) {
        view.setTag(R.id.tagEcoMidia, null);
        if (contextoAtivo(view)) Glide.with(view).clear(view);
        view.setImageDrawable(null);
        view.animate().cancel();
    }

    private static boolean contextoAtivo(View view) {
        android.content.Context contexto = view.getContext();
        while (contexto instanceof android.content.ContextWrapper) {
            if (contexto instanceof android.app.Activity) {
                android.app.Activity a = (android.app.Activity) contexto;
                return !a.isDestroyed() && !a.isFinishing();
            }
            android.content.Context proximo = ((android.content.ContextWrapper) contexto).getBaseContext();
            if (proximo == contexto) break;
            contexto = proximo;
        }
        return true;
    }

    public static void limparArvore(View raiz) {
        if (raiz instanceof ImageView) limpar((ImageView) raiz);
        if (raiz instanceof android.view.ViewGroup) {
            android.view.ViewGroup grupo = (android.view.ViewGroup) raiz;
            for (int i = 0; i < grupo.getChildCount(); i++) limparArvore(grupo.getChildAt(i));
        }
    }
}
