package com.example.ecociente.midias;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecociente.R;

/** Um unico ImageView real, movido entre os slots. Impossivel mostrar tres ao mesmo tempo. */
public final class DecoracaoQuizView {
    private final ImageView imagem;
    private final FrameLayout esquerda, direita, rodape;
    private final View linhaTopo;
    private final MidiasEco midias;
    private int geracao;

    public DecoracaoQuizView(AppCompatActivity activity) {
        imagem = activity.findViewById(R.id.ilustracaoPlanetaQuiz);
        esquerda = activity.findViewById(R.id.slotQuizTopoEsquerdo);
        direita = activity.findViewById(R.id.slotQuizTopoDireito);
        rodape = activity.findViewById(R.id.slotQuizRodape);
        linhaTopo = activity.findViewById(R.id.linhaDecoracaoTopoQuiz);
        midias = MidiasEco.obter(activity);
    }

    public void mostrar(long semente, int indice) {
        RegrasMidias.DecoracaoQuiz escolha = midias.decoracaoQuiz(semente, indice);
        aplicar(escolha.posicao, escolha.midia);
    }

    private void aplicar(int posicao, Midia midia) {
        int minhaGeracao = ++geracao;
        boolean embaixo = posicao == RegrasMidias.ACIMA_BOTAO && midia != null && midia.ehGifEko();
        if (!embaixo && posicao == RegrasMidias.ACIMA_BOTAO) posicao = RegrasMidias.TOPO_DIREITO;
        CarregadorMidias.limpar(imagem);
        ViewGroup pai = (ViewGroup) imagem.getParent();
        if (pai != null) pai.removeView(imagem);
        esquerda.setVisibility(View.GONE);
        direita.setVisibility(View.GONE);
        rodape.setVisibility(View.GONE);
        linhaTopo.setVisibility(embaixo ? View.GONE : View.VISIBLE);
        FrameLayout destino = embaixo ? rodape : posicao == RegrasMidias.TOPO_ESQUERDO ? esquerda : direita;
        destino.setVisibility(View.VISIBLE);
        destino.addView(imagem, new FrameLayout.LayoutParams(-1, -1));
        imagem.setVisibility(View.VISIBLE);
        imagem.setAlpha(1f);
        imagem.setTranslationX(0f);
        imagem.setTranslationY(0f);
        Midia mostrar = midia == null ? midias.planetaPadrao() : midia;
        // Se o arquivo nao for realmente um GIF, recoloca a alternativa local no topo.
        CarregadorMidias.mostrar(imagem, mostrar, false, 0, embaixo, () -> {
            if (minhaGeracao == geracao) aplicarFallback();
        });
    }

    private void aplicarFallback() {
        ++geracao;
        CarregadorMidias.limpar(imagem);
        ViewGroup pai = (ViewGroup) imagem.getParent();
        if (pai != null) pai.removeView(imagem);
        esquerda.setVisibility(View.GONE);
        rodape.setVisibility(View.GONE);
        linhaTopo.setVisibility(View.VISIBLE);
        direita.setVisibility(View.VISIBLE);
        direita.addView(imagem, new FrameLayout.LayoutParams(-1, -1));
        imagem.setImageResource(R.drawable.quiz_planeta_pergunta);
    }
}
