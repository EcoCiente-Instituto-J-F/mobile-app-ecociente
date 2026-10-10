package com.example.ecociente.views;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.ecociente.R;
import com.example.ecociente.model.QuizConteudo;
import com.example.ecociente.ui.Motion;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class QuizRevisaoActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORIA = "categoriaQuizRevisao";
    public static final String EXTRA_RESPOSTAS = "respostasQuizRevisao";
    public static final String EXTRA_INDICE_INICIAL = "indiceInicialQuizRevisao";

    private static final float DISTANCIA_MINIMA_SWIPE_DP = 58f;

    private final Motion motion = new Motion();
    private final List<Integer> indicesErrados = new ArrayList<>();
    private final List<View> viewsConteudoAnimado = new ArrayList<>();

    private List<QuizConteudo.Pergunta> perguntas;
    private int[] respostas;
    private String categoria;
    private int posicaoAtual;

    private View painelSwipe;
    private TextView textoNumero;
    private TextView textoPergunta;
    private TextView textoRespostaUsuario;
    private TextView textoRespostaCorreta;
    private TextView textoExplicacao;
    private LinearLayout indicadores;

    private float toqueInicialX;
    private boolean animandoTroca;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setStatusBarContrastEnforced(false);
            getWindow().setNavigationBarContrastEnforced(false);
        }

        setContentView(R.layout.activity_quiz_revisao);
        configurarInsetsDaTela();

        perguntas = QuizConteudo.obterPerguntas();
        respostas = getIntent().getIntArrayExtra(EXTRA_RESPOSTAS);
        categoria = getIntent().getStringExtra(EXTRA_CATEGORIA);

        if (categoria == null || categoria.trim().isEmpty()) {
            categoria = getString(R.string.inicio_quiz_titulo_padrao);
        }

        if (respostas == null || respostas.length != perguntas.size()) {
            respostas = new int[perguntas.size()];
            Arrays.fill(respostas, -1);
        }

        montarListaDeErros();

        if (indicesErrados.isEmpty()) {
            finish();
            return;
        }

        vincularViews();
        definirPosicaoInicial();
        preencherQuestao();
        configurarAcoes();
        configurarSwipe();
        animarEntrada();

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        voltarParaResultado();
                    }
                }
        );
    }

    private void montarListaDeErros() {
        indicesErrados.clear();

        for (int i = 0; i < perguntas.size(); i++) {
            if (respostas[i] != perguntas.get(i).getIndiceCorreto()) {
                indicesErrados.add(i);
            }
        }
    }

    private void vincularViews() {
        painelSwipe = findViewById(R.id.painelRevisaoSwipe);
        textoNumero = findViewById(R.id.textoNumeroQuestaoRevisao);
        textoPergunta = findViewById(R.id.textoPerguntaRevisao);
        textoRespostaUsuario = findViewById(R.id.textoRespostaUsuarioRevisao);
        textoRespostaCorreta = findViewById(R.id.textoRespostaCorretaRevisao);
        textoExplicacao = findViewById(R.id.textoExplicacaoRevisao);
        indicadores = findViewById(R.id.indicadoresRevisao);

        // Somente o conteudo da questao desliza. O titulo, o subtitulo e as
        // bolinhas permanecem parados; nas bolinhas muda apenas a cor ativa.
        viewsConteudoAnimado.add(textoPergunta);
        viewsConteudoAnimado.add(findViewById(R.id.cardRespostasRevisao));
        viewsConteudoAnimado.add(findViewById(R.id.cabecalhoExplicacaoRevisao));
        viewsConteudoAnimado.add(textoExplicacao);
    }

    private void definirPosicaoInicial() {
        int indiceInicial = getIntent().getIntExtra(EXTRA_INDICE_INICIAL, indicesErrados.get(0));
        int encontrada = indicesErrados.indexOf(indiceInicial);
        posicaoAtual = encontrada >= 0 ? encontrada : 0;
    }

    private void configurarAcoes() {
        ImageView botaoVoltar = findViewById(R.id.botaoVoltarRevisaoQuiz);
        MaterialButton botaoRepetir = findViewById(R.id.botaoRepetirQuizRevisao);

        botaoVoltar.setOnClickListener(view -> voltarParaResultado());
        botaoRepetir.setOnClickListener(view -> repetirQuiz());

        Motion.pressFeedback(botaoVoltar);
        Motion.pressFeedback(botaoRepetir);
    }

    private void configurarSwipe() {
        painelSwipe.setOnTouchListener((view, evento) -> {
            if (animandoTroca) {
                return true;
            }

            if (evento.getActionMasked() == MotionEvent.ACTION_DOWN) {
                toqueInicialX = evento.getX();
                return true;
            }

            if (evento.getActionMasked() == MotionEvent.ACTION_UP
                    || evento.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                float delta = evento.getX() - toqueInicialX;
                float limite = dp(DISTANCIA_MINIMA_SWIPE_DP);

                if (Math.abs(delta) >= limite && indicesErrados.size() > 1) {
                    trocarQuestao(delta < 0 ? 1 : -1);
                }
                return true;
            }

            return true;
        });
    }

    private void trocarQuestao(int direcao) {
        if (animandoTroca || indicesErrados.size() <= 1) {
            return;
        }

        int novaPosicao = posicaoAtual + direcao;

        if (novaPosicao < 0) {
            novaPosicao = indicesErrados.size() - 1;
        } else if (novaPosicao >= indicesErrados.size()) {
            novaPosicao = 0;
        }

        final int destino = novaPosicao;
        final float distancia = dp(34);
        final float saida = direcao > 0 ? -distancia : distancia;
        final float entrada = -saida;

        animandoTroca = true;

        for (View item : viewsConteudoAnimado) {
            item.animate()
                    .translationX(saida)
                    .alpha(0f)
                    .setDuration(130L)
                    .start();
        }

        painelSwipe.postDelayed(() -> {
            posicaoAtual = destino;
            preencherQuestao();

            for (View item : viewsConteudoAnimado) {
                item.setTranslationX(entrada);
                item.setAlpha(0f);
                item.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(190L)
                        .start();
            }

            painelSwipe.postDelayed(() -> animandoTroca = false, 195L);
        }, 135L);
    }

    private void preencherQuestao() {
        int indiceOriginal = indicesErrados.get(posicaoAtual);
        QuizConteudo.Pergunta pergunta = perguntas.get(indiceOriginal);
        String[] opcoes = pergunta.getOpcoes();

        String respostaUsuario = respostas[indiceOriginal] >= 0
                && respostas[indiceOriginal] < opcoes.length
                ? opcoes[respostas[indiceOriginal]]
                : getString(R.string.quiz_sem_resposta);

        String respostaCorreta = opcoes[pergunta.getIndiceCorreto()];

        textoNumero.setText(String.format(
                Locale.getDefault(),
                getString(R.string.quiz_revisao_numero_formatado),
                indiceOriginal + 1
        ));
        textoPergunta.setText(pergunta.getEnunciado());
        textoRespostaUsuario.setText(respostaUsuario);
        textoRespostaCorreta.setText(respostaCorreta);
        textoExplicacao.setText(pergunta.getExplicacao());

        atualizarIndicadores();
    }

    private void atualizarIndicadores() {
        if (indicadores.getChildCount() != indicesErrados.size()) {
            indicadores.removeAllViews();

            for (int i = 0; i < indicesErrados.size(); i++) {
                View ponto = new View(this);
                int tamanho = dpInt(10);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(tamanho, tamanho);

                if (i > 0) {
                    params.leftMargin = dpInt(6);
                }

                ponto.setLayoutParams(params);
                indicadores.addView(ponto);
            }
        }

        int corAtiva = ContextCompat.getColor(this, R.color.verde_escuro_principal);
        int corInativa = Color.parseColor("#D5D5D5");

        for (int i = 0; i < indicadores.getChildCount(); i++) {
            View ponto = indicadores.getChildAt(i);
            GradientDrawable fundo = new GradientDrawable();
            fundo.setShape(GradientDrawable.OVAL);
            fundo.setColor(i == posicaoAtual ? corAtiva : corInativa);
            ponto.setBackground(fundo);
        }
    }

    private void voltarParaResultado() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void repetirQuiz() {
        Intent rota = new Intent(this, QuizPerguntasActivity.class);
        rota.putExtra(QuizPerguntasActivity.EXTRA_CATEGORIA, categoria);
        rota.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(rota);
        overridePendingTransition(0, 0);
        finish();
    }

    private void animarEntrada() {
        motion.fadeUp(findViewById(R.id.textoNumeroQuestaoRevisao), 0L);
        motion.fadeUp(findViewById(R.id.textoSubtituloRevisao), 35L);
        motion.fadeUp(findViewById(R.id.indicadoresRevisao), 70L);
        motion.fadeUp(findViewById(R.id.textoPerguntaRevisao), 105L);
        motion.fadeUp(findViewById(R.id.cardRespostasRevisao), 145L);
        motion.fadeUp(findViewById(R.id.cabecalhoExplicacaoRevisao), 190L);
        motion.fadeUp(findViewById(R.id.textoExplicacaoRevisao), 225L);
        motion.fadeUp(findViewById(R.id.botaoRepetirQuizRevisao), 260L);
    }

    private void configurarInsetsDaTela() {
        View raiz = findViewById(R.id.raizQuizRevisao);

        final int esquerda = raiz.getPaddingLeft();
        final int topo = raiz.getPaddingTop();
        final int direita = raiz.getPaddingRight();
        final int inferior = raiz.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(raiz, (view, insets) -> {
            Insets sistema = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
            );

            view.setPadding(
                    esquerda + sistema.left,
                    topo + sistema.top,
                    direita + sistema.right,
                    inferior + sistema.bottom
            );

            return insets;
        });

        WindowInsetsControllerCompat controlador = WindowCompat.getInsetsController(getWindow(), raiz);
        if (controlador != null) {
            controlador.setAppearanceLightStatusBars(true);
            controlador.setAppearanceLightNavigationBars(true);
        }

        ViewCompat.requestApplyInsets(raiz);
    }

    private int dpInt(float valor) {
        return Math.round(dp(valor));
    }

    private float dp(float valor) {
        return valor * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDestroy() {
        motion.cancelAll();
        super.onDestroy();
    }
}
