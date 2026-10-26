package com.example.ecociente.midias;

import android.content.Context;
import android.view.View;
import com.google.android.material.card.MaterialCardView;

/** Reserva proporcao 16:9 antes da rede responder; nao depende do tamanho original da foto. */
public final class FotoPanoramicaView extends MaterialCardView {
    public FotoPanoramicaView(Context context) { super(context); }
    @Override protected void onMeasure(int larguraSpec, int alturaSpec) {
        int largura = View.MeasureSpec.getSize(larguraSpec);
        int altura = Math.max(1, Math.round(largura * 9f / 16f));
        super.onMeasure(larguraSpec, View.MeasureSpec.makeMeasureSpec(altura, View.MeasureSpec.EXACTLY));
    }
}
