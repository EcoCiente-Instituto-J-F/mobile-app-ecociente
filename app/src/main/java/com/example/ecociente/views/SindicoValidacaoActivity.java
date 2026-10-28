package com.example.ecociente.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ecociente.R;
import com.example.ecociente.model.PostagemFeed;
import com.example.ecociente.repository.PerfilApiRepository;
import com.example.ecociente.repository.PostagemRepository;
import com.example.ecociente.ui.AvisoFlutuante;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.IndicadorCarregamento;
import com.example.ecociente.util.Iniciais;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.Navegacao;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/** O síndico aprova ou reprova os descartes em análise do condomínio (ds-pontuacao-api). */
public final class SindicoValidacaoActivity extends AppCompatActivity {
    private LinearLayout lista;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        JanelaEdgeToEdge.aplicar(this);
        setContentView(R.layout.activity_sindico_validacao);

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizSindicoValidacao));
        lista = findViewById(R.id.listaSindicoValidacao);

        BarraNavegacaoView barra = findViewById(R.id.barraInferiorSindico);
        barra.configurar(ItensBarra.sindico());
        Navegacao.marcarNaBarra(this, barra, ItensBarra.HOME);
        barra.setOnAssistenteClickListener(
                view -> startActivity(new Intent(this, ChatActivity.class)));
        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDoSindico(this, indice, ItensBarra.NENHUM));
    }

    @Override
    protected void onStart() {
        super.onStart();
        carregar();
    }

    private void carregar() {
        lista.removeAllViews();
        IndicadorCarregamento.mostrar(lista, R.string.validacao_carregando);

        new PostagemRepository().emAnalise(this, PerfilApiRepository.condominioId(this), postagens -> {
            if (isFinishing() || isDestroyed()) return;

            if (postagens == null) {
                mostrarErro();
                return;
            }

            exibir(postagens);
        });
    }

    private void exibir(List<PostagemFeed> postagens) {
        lista.removeAllViews();

        LayoutInflater inflador = LayoutInflater.from(this);

        for (PostagemFeed postagem : postagens) {
            View cartao = inflador.inflate(R.layout.item_feed_postagem, lista, false);

            ((TextView) cartao.findViewById(R.id.iniciaisFeed)).setText(Iniciais.de(postagem.getAutor()));
            ((TextView) cartao.findViewById(R.id.autorFeed)).setText(postagem.getAutor());
            ((TextView) cartao.findViewById(R.id.quandoFeed)).setText(postagem.getQuando());
            ((TextView) cartao.findViewById(R.id.categoriaFeed)).setText(postagem.getCategoria());
            ((TextView) cartao.findViewById(R.id.statusFeed)).setText(postagem.getStatus());
            cartao.findViewById(R.id.categoriaFeed)
                    .setVisibility(postagem.getCategoria().isEmpty() ? View.GONE : View.VISIBLE);

            Glide.with(this).load(postagem.getFotoUrl()).into((ImageView) cartao.findViewById(R.id.fotoFeed));

            cartao.findViewById(R.id.acoesValidacaoFeed).setVisibility(View.VISIBLE);
            MaterialButton aprovar = cartao.findViewById(R.id.botaoAprovarFeed);
            MaterialButton reprovar = cartao.findViewById(R.id.botaoReprovarFeed);
            aprovar.setOnClickListener(view -> decidir(cartao, postagem, true, aprovar, reprovar));
            reprovar.setOnClickListener(view -> decidir(cartao, postagem, false, aprovar, reprovar));

            lista.addView(cartao);
        }

        if (postagens.isEmpty()) mostrarVazio();
    }

    private void decidir(View cartao, PostagemFeed postagem, boolean aprovar,
                         MaterialButton botaoAprovar, MaterialButton botaoReprovar) {
        botaoAprovar.setEnabled(false);
        botaoReprovar.setEnabled(false);

        new PostagemRepository().decidir(this, postagem.getId(), aprovar, resultado -> {
            if (isFinishing() || isDestroyed()) return;

            if (!resultado.isSucesso()) {
                botaoAprovar.setEnabled(true);
                botaoReprovar.setEnabled(true);
                AvisoFlutuante.mostrar(this, resultado.getMensagemErro());
                return;
            }

            lista.removeView(cartao);
            AvisoFlutuante.mostrar(this, aprovar ? R.string.validacao_aprovado : R.string.validacao_reprovado);
            if (lista.getChildCount() == 0) mostrarVazio();
        });
    }

    private void mostrarVazio() {
        TextView vazio = new TextView(this);
        vazio.setText(R.string.validacao_vazio);
        vazio.setTextColor(getColor(R.color.cinza_texto_home));
        vazio.setTextSize(14);
        vazio.setPadding(dp(26), dp(28), dp(26), dp(28));
        lista.addView(vazio);
    }

    private void mostrarErro() {
        lista.removeAllViews();

        TextView mensagem = new TextView(this);
        mensagem.setText(R.string.validacao_erro);
        mensagem.setTextColor(getColor(R.color.cinza_texto_home));
        mensagem.setTextSize(14);
        mensagem.setPadding(dp(26), dp(28), dp(26), dp(12));
        lista.addView(mensagem);

        MaterialButton tentar = (MaterialButton) getLayoutInflater().inflate(R.layout.botao_dialogo, lista, false);
        tentar.setText(R.string.tentar_novamente);
        tentar.setOnClickListener(view -> carregar());
        lista.addView(tentar);
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}
