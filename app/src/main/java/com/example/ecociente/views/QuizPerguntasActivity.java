package com.example.ecociente.views;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.FrameLayout;
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
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class QuizPerguntasActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORIA = "categoriaQuizPerguntas";
    public static final String EXTRA_RESPOSTAS = "respostasQuiz";

    private final Motion motion = new Motion();
    private final List<View> pontosProgresso = new ArrayList<>();
    private final List<View> conectoresProgresso = new ArrayList<>();

    private List<QuizConteudo.Pergunta> perguntas;
    private int[] respostas;
    private int indiceAtual;
    private String categoria;

    private TextView textoContador;
    private TextView textoCategoria;
    private TextView textoEnunciado;
    private LinearLayout conteudoQuestao;
    private LinearLayout containerProgresso;
    private MaterialButton botaoProximo;

    private MaterialCardView[] cardsOpcoes;
    private TextView[] textosOpcoes;
    private FrameLayout[] caixasCheck;
    private ImageView[] iconesCheck;

    private int verde;
    private int branco;
    private int preto;
    private int cinzaFuturo;

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

        setContentView(R.layout.activity_quiz_perguntas);
        configurarInsetsDaTela();

        perguntas = QuizConteudo.obterPerguntas();
        respostas = new int[perguntas.size()];
        Arrays.fill(respostas, -1);

        categoria = getIntent().getStringExtra(EXTRA_CATEGORIA);
        if (categoria == null || categoria.trim().isEmpty()) {
            categoria = getString(R.string.inicio_quiz_titulo_padrao);
        }

        verde = ContextCompat.getColor(this, R.color.verde_escuro_principal);
        branco = ContextCompat.getColor(this, R.color.branco);
        preto = ContextCompat.getColor(this, R.color.black);
        cinzaFuturo = Color.parseColor("#D3D3D3");

        vincularViews();
        criarProgresso();
        configurarAcoes();
        exibirPergunta();
        animarEntradaInicial();

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        voltarPerguntaOuIntroducao();
                    }
                }
        );
    }

    private void vincularViews() {
        textoContador = findViewById(R.id.textoContadorPergunta);
        textoCategoria = findViewById(R.id.textoCategoriaPergunta);
        textoEnunciado = findViewById(R.id.textoEnunciadoPergunta);
        conteudoQuestao = findViewById(R.id.conteudoQuestao);
        containerProgresso = findViewById(R.id.containerProgressoQuiz);
        botaoProximo = findViewById(R.id.botaoProximaPergunta);

        cardsOpcoes = new MaterialCardView[]{
                findViewById(R.id.cardOpcao1),
                findViewById(R.id.cardOpcao2),
                findViewById(R.id.cardOpcao3),
                findViewById(R.id.cardOpcao4)
        };

        textosOpcoes = new TextView[]{
                findViewById(R.id.textoOpcao1),
                findViewById(R.id.textoOpcao2),
                findViewById(R.id.textoOpcao3),
                findViewById(R.id.textoOpcao4)
        };

        caixasCheck = new FrameLayout[]{
                findViewById(R.id.caixaCheckOpcao1),
                findViewById(R.id.caixaCheckOpcao2),
                findViewById(R.id.caixaCheckOpcao3),
                findViewById(R.id.caixaCheckOpcao4)
        };

        iconesCheck = new ImageView[]{
                findViewById(R.id.iconeCheckOpcao1),
                findViewById(R.id.iconeCheckOpcao2),
                findViewById(R.id.iconeCheckOpcao3),
                findViewById(R.id.iconeCheckOpcao4)
        };
    }

    private void configurarAcoes() {
        ImageView botaoVoltar = findViewById(R.id.botaoVoltarPerguntas);
        botaoVoltar.setOnClickListener(view -> voltarPerguntaOuIntroducao());
        Motion.pressFeedback(botaoVoltar);

        for (int i = 0; i < cardsOpcoes.length; i++) {
            final int indiceOpcao = i;
            cardsOpcoes[i].setOnClickListener(view -> selecionarOpcao(indiceOpcao));
            Motion.pressFeedback(cardsOpcoes[i]);
        }

        botaoProximo.setOnClickListener(view -> avancarOuFinalizar());
        Motion.pressFeedback(botaoProximo);
    }

    private void selecionarOpcao(int indiceOpcao) {
        respostas[indiceAtual] = indiceOpcao;
        atualizarEstadoOpcoes();
        atualizarEstadoBotao();

        cardsOpcoes[indiceOpcao].performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
        cardsOpcoes[indiceOpcao]
                .animate()
                .scaleX(.985f)
                .scaleY(.985f)
                .setDuration(70L)
                .withEndAction(() -> cardsOpcoes[indiceOpcao]
                        .animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(130L)
                        .start())
                .start();
    }

    private void avancarOuFinalizar() {
        if (respostas[indiceAtual] < 0) {
            motion.shake(botaoProximo);
            return;
        }

        if (indiceAtual == perguntas.size() - 1) {
            mostrarConfirmacaoFinalizacao();
            return;
        }

        animarTrocaPergunta(true);
    }

    private void voltarPerguntaOuIntroducao() {
        if (indiceAtual <= 0) {
            finish();
            overridePendingTransition(0, 0);
            return;
        }

        animarTrocaPergunta(false);
    }

    private void animarTrocaPergunta(boolean avancar) {
        float distancia = dp(26);
        float saida = avancar ? -distancia : distancia;
        float entrada = avancar ? distancia : -distancia;

        conteudoQuestao.animate()
                .alpha(0f)
                .translationX(saida)
                .setDuration(130L)
                .withEndAction(() -> {
                    indiceAtual += avancar ? 1 : -1;
                    exibirPergunta();
                    conteudoQuestao.setTranslationX(entrada);
                    conteudoQuestao.setAlpha(0f);
                    conteudoQuestao.animate()
                            .alpha(1f)
                            .translationX(0f)
                            .setDuration(230L)
                            .start();
                })
                .start();
    }

    private void exibirPergunta() {
        QuizConteudo.Pergunta pergunta = perguntas.get(indiceAtual);

        textoContador.setText(String.format(
                Locale.getDefault(),
                "%02d/%02d",
                indiceAtual + 1,
                perguntas.size()
        ));
        textoCategoria.setText(categoria);
        textoEnunciado.setText(pergunta.getEnunciado());

        String[] opcoes = pergunta.getOpcoes();
        for (int i = 0; i < textosOpcoes.length; i++) {
            textosOpcoes[i].setText(opcoes[i]);
        }

        atualizarEstadoOpcoes();
        atualizarEstadoBotao();
        atualizarProgresso();
    }

    private void atualizarEstadoOpcoes() {
        int selecionada = respostas[indiceAtual];

        for (int i = 0; i < cardsOpcoes.length; i++) {
            boolean ativa = i == selecionada;
            MaterialCardView card = cardsOpcoes[i];
            TextView texto = textosOpcoes[i];
            FrameLayout caixaCheck = caixasCheck[i];
            ImageView iconeCheck = iconesCheck[i];

            if (ativa) {
                card.setCardBackgroundColor(verde);
                card.setStrokeWidth(0);
                texto.setTextColor(branco);
                caixaCheck.setBackgroundResource(R.drawable.fundo_checkbox_quiz_selecionado);
                iconeCheck.setVisibility(View.VISIBLE);
            } else {
                card.setCardBackgroundColor(branco);
                card.setStrokeColor(verde);
                card.setStrokeWidth(dpInt(1));
                texto.setTextColor(preto);
                caixaCheck.setBackgroundResource(R.drawable.fundo_checkbox_quiz);
                iconeCheck.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void atualizarEstadoBotao() {
        boolean respondida = respostas[indiceAtual] >= 0;
        botaoProximo.setEnabled(respondida);
        botaoProximo.animate().alpha(respondida ? 1f : .35f).setDuration(150L).start();
        botaoProximo.setText(
                indiceAtual == perguntas.size() - 1
                        ? R.string.quiz_finalizar
                        : R.string.quiz_proximo
        );
        botaoProximo.setBackgroundTintList(ColorStateList.valueOf(verde));
    }

    private void criarProgresso() {
        containerProgresso.removeAllViews();
        pontosProgresso.clear();
        conectoresProgresso.clear();

        for (int i = 0; i < perguntas.size(); i++) {
            View ponto = new View(this);
            LinearLayout.LayoutParams pontoParams = new LinearLayout.LayoutParams(dpInt(14), dpInt(14));
            ponto.setLayoutParams(pontoParams);
            containerProgresso.addView(ponto);
            pontosProgresso.add(ponto);

            if (i < perguntas.size() - 1) {
                View conector = new View(this);
                LinearLayout.LayoutParams conectorParams = new LinearLayout.LayoutParams(dpInt(12), dpInt(2));
                conector.setLayoutParams(conectorParams);
                containerProgresso.addView(conector);
                conectoresProgresso.add(conector);
            }
        }

        atualizarProgresso();
    }

    private void atualizarProgresso() {
        for (int i = 0; i < pontosProgresso.size(); i++) {
            boolean ativo = i <= indiceAtual;
            pontosProgresso.get(i).setBackground(criarCirculo(ativo ? verde : cinzaFuturo));
        }

        for (int i = 0; i < conectoresProgresso.size(); i++) {
            conectoresProgresso.get(i).setBackgroundColor(i < indiceAtual ? verde : cinzaFuturo);
        }

        View atual = pontosProgresso.get(indiceAtual);
        atual.setScaleX(.8f);
        atual.setScaleY(.8f);
        atual.animate()
                .scaleX(1.16f)
                .scaleY(1.16f)
                .setDuration(130L)
                .withEndAction(() -> atual.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150L)
                        .start())
                .start();
    }

    @NonNull
    private GradientDrawable criarCirculo(int cor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(cor);
        return drawable;
    }

    private void mostrarConfirmacaoFinalizacao() {
        QuizDialogos.mostrarConfirmacao(
                this,
                getString(R.string.quiz_dialogo_finalizar_titulo),
                getString(R.string.quiz_dialogo_finalizar_mensagem),
                getString(R.string.quiz_finalizar),
                getString(R.string.cancelar),
                this::abrirResultado
        );
    }

    private void abrirResultado() {
        Intent rota = new Intent(this, QuizResultadoActivity.class);
        rota.putExtra(QuizResultadoActivity.EXTRA_CATEGORIA, categoria);
        rota.putExtra(QuizResultadoActivity.EXTRA_RESPOSTAS, respostas);
        startActivity(rota);
        overridePendingTransition(0, 0);
        finish();
    }

    private void animarEntradaInicial() {
        motion.fadeUp(findViewById(R.id.textoContadorPergunta), 0L);
        motion.fadeUp(containerProgresso, 45L);
        motion.fadeUp(findViewById(R.id.cardPerguntaQuiz), 90L);
        motion.fadeUp(findViewById(R.id.ilustracaoPlanetaQuiz), 125L);
        motion.fadeUp(botaoProximo, 170L);
    }

    private void configurarInsetsDaTela() {
        View raiz = findViewById(R.id.raizQuizPerguntas);

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
