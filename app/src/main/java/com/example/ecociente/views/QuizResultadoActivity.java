package com.example.ecociente.views;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
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
import androidx.core.widget.ImageViewCompat;

import com.example.ecociente.R;
import com.example.ecociente.model.QuizConteudo;
import com.example.ecociente.ui.Motion;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class QuizResultadoActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORIA = "categoriaQuizResultado";
    public static final String EXTRA_RESPOSTAS = "respostasQuizResultado";

    private final Motion motion = new Motion();

    private List<QuizConteudo.Pergunta> perguntas;
    private int[] respostas;
    private String categoria;
    private int pontuacao;

    private TextView textoPontuacao;
    private TextView textoMensagem;
    private LinearLayout listaResultados;
    private ConfettiView confetti;

    private int verde;
    private int rosa;
    private int branco;

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

        setContentView(R.layout.activity_quiz_resultado);
        configurarInsetsDaTela();

        verde = ContextCompat.getColor(this, R.color.verde_escuro_principal);
        rosa = ContextCompat.getColor(this, R.color.rosa_ecociente);
        branco = ContextCompat.getColor(this, R.color.branco);

        perguntas = QuizConteudo.obterPerguntas();
        categoria = getIntent().getStringExtra(EXTRA_CATEGORIA);
        if (categoria == null || categoria.trim().isEmpty()) {
            categoria = getString(R.string.inicio_quiz_titulo_padrao);
        }

        respostas = getIntent().getIntArrayExtra(EXTRA_RESPOSTAS);
        if (respostas == null || respostas.length != perguntas.size()) {
            respostas = new int[perguntas.size()];
            Arrays.fill(respostas, -1);
        }

        pontuacao = calcularPontuacao();

        vincularViews();
        preencherResultado();
        configurarAcoes();
        animarEntrada();

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        voltarParaQuizzes();
                    }
                }
        );
    }

    private void vincularViews() {
        textoPontuacao = findViewById(R.id.textoPontuacaoResultado);
        textoMensagem = findViewById(R.id.textoMensagemResultado);
        listaResultados = findViewById(R.id.listaResultadoPerguntas);
        confetti = findViewById(R.id.confettiResultadoQuiz);

        ((TextView) findViewById(R.id.textoCategoriaResultado)).setText(categoria);
    }

    private int calcularPontuacao() {
        int acertos = 0;
        for (int i = 0; i < perguntas.size(); i++) {
            if (respostas[i] == perguntas.get(i).getIndiceCorreto()) {
                acertos++;
            }
        }
        return acertos;
    }

    private void preencherResultado() {
        textoPontuacao.setText(String.format(Locale.getDefault(), "%02d/%02d", 0, perguntas.size()));
        textoMensagem.setText(mensagemParaPontuacao());

        listaResultados.removeAllViews();

        for (int i = 0; i < perguntas.size(); i++) {
            QuizConteudo.Pergunta pergunta = perguntas.get(i);
            boolean acertou = respostas[i] == pergunta.getIndiceCorreto();
            View item = criarItemResultado(i, pergunta, acertou);
            listaResultados.addView(item);
            motion.fadeUp(item, 180L + (i * 34L));
        }
    }

    @NonNull
    private View criarItemResultado(
            int indice,
            @NonNull QuizConteudo.Pergunta pergunta,
            boolean acertou
    ) {
        MaterialCardView card = new MaterialCardView(this);
        int corBase = acertou ? verde : rosa;
        int corPressionada = acertou
                ? Color.parseColor("#E4F0E6")
                : Color.parseColor("#F8D7E2");

        int[][] estados = new int[][]{
                new int[]{android.R.attr.state_pressed},
                new int[]{}
        };

        card.setCardBackgroundColor(new ColorStateList(
                estados,
                new int[]{corPressionada, corBase}
        ));
        card.setStrokeColor(ColorStateList.valueOf(corBase));
        card.setStrokeWidth(dpInt(1));
        card.setRippleColor(ColorStateList.valueOf(Color.TRANSPARENT));
        card.setCardElevation(0f);
        card.setRadius(dp(14));
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpInt(58)
        );
        cardParams.setMargins(0, 0, 0, dpInt(12));
        card.setLayoutParams(cardParams);

        LinearLayout linha = new LinearLayout(this);
        linha.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linha.setGravity(Gravity.CENTER_VERTICAL);
        linha.setOrientation(LinearLayout.HORIZONTAL);
        linha.setPadding(dpInt(16), 0, dpInt(12), 0);
        linha.setDuplicateParentStateEnabled(true);

        ImageView icone = new ImageView(this);
        icone.setLayoutParams(new LinearLayout.LayoutParams(dpInt(30), dpInt(30)));
        icone.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        icone.setImageResource(acertou ? R.drawable.quiz_check : R.drawable.quiz_x);
        icone.setDuplicateParentStateEnabled(true);
        ImageViewCompat.setImageTintList(icone, new ColorStateList(
                estados,
                new int[]{corBase, branco}
        ));
        icone.setContentDescription(null);

        TextView enunciado = new TextView(this);
        LinearLayout.LayoutParams textoParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        textoParams.setMargins(dpInt(10), 0, dpInt(8), 0);
        enunciado.setLayoutParams(textoParams);
        enunciado.setEllipsize(TextUtils.TruncateAt.END);
        enunciado.setMaxLines(1);
        enunciado.setIncludeFontPadding(false);
        enunciado.setText(pergunta.getEnunciado());
        enunciado.setDuplicateParentStateEnabled(true);
        enunciado.setTextColor(new ColorStateList(
                estados,
                new int[]{corBase, branco}
        ));
        enunciado.setTextSize(15f);

        TextView seta = new TextView(this);
        seta.setLayoutParams(new LinearLayout.LayoutParams(dpInt(26), dpInt(36)));
        seta.setGravity(Gravity.CENTER);
        seta.setIncludeFontPadding(false);
        seta.setText("›");
        seta.setDuplicateParentStateEnabled(true);
        seta.setTextColor(new ColorStateList(
                estados,
                new int[]{corBase, 0x99FFFFFF}
        ));
        seta.setTextSize(30f);

        linha.addView(icone);
        linha.addView(enunciado);
        linha.addView(seta);
        card.addView(linha);

        card.setOnClickListener(view -> abrirDetalhePergunta(indice, pergunta, acertou));
        Motion.pressFeedback(card);
        return card;
    }

    private void abrirDetalhePergunta(
            int indice,
            @NonNull QuizConteudo.Pergunta pergunta,
            boolean acertou
    ) {
        if (!acertou) {
            Intent rota = new Intent(this, QuizRevisaoActivity.class);
            rota.putExtra(QuizRevisaoActivity.EXTRA_CATEGORIA, categoria);
            rota.putExtra(QuizRevisaoActivity.EXTRA_RESPOSTAS, respostas);
            rota.putExtra(QuizRevisaoActivity.EXTRA_INDICE_INICIAL, indice);
            startActivity(rota);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return;
        }

        String[] opcoes = pergunta.getOpcoes();
        String respostaCorreta = opcoes[pergunta.getIndiceCorreto()];

        QuizDialogos.mostrarExplicacaoQuestao(
                this,
                getString(R.string.quiz_questao_numero, indice + 1),
                getString(R.string.quiz_resposta_correta, respostaCorreta),
                pergunta.getExplicacao(),
                getString(R.string.quiz_dialogo_fechar)
        );
    }

    @NonNull
    private String mensagemParaPontuacao() {
        if (pontuacao == perguntas.size()) {
            return getString(R.string.quiz_resultado_mensagem_perfeita);
        }
        if (pontuacao > 7) {
            return getString(R.string.quiz_resultado_mensagem_boa);
        }
        if (pontuacao >= 6) {
            return getString(R.string.quiz_resultado_mensagem_media);
        }
        return getString(R.string.quiz_resultado_mensagem_praticar);
    }

    private void configurarAcoes() {
        ImageView botaoVoltar = findViewById(R.id.botaoVoltarResultadoQuiz);
        MaterialButton botaoFinalizar = findViewById(R.id.botaoFinalizarResultadoQuiz);
        MaterialButton botaoRepetir = findViewById(R.id.botaoRepetirResultadoQuiz);

        botaoVoltar.setOnClickListener(view -> voltarParaQuizzes());
        botaoFinalizar.setOnClickListener(view -> voltarParaQuizzes());
        botaoRepetir.setOnClickListener(view -> repetirQuiz());

        Motion.pressFeedback(botaoVoltar);
        Motion.pressFeedback(botaoFinalizar);
        Motion.pressFeedback(botaoRepetir);
    }

    private void voltarParaQuizzes() {
        Intent rota = new Intent(this, QuizActivity.class);
        rota.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(rota);
        overridePendingTransition(0, 0);
        finish();
    }

    private void repetirQuiz() {
        Intent rota = new Intent(this, QuizPerguntasActivity.class);
        rota.putExtra(QuizPerguntasActivity.EXTRA_CATEGORIA, categoria);
        startActivity(rota);
        overridePendingTransition(0, 0);
        finish();
    }

    private void animarEntrada() {
        motion.fadeUp(findViewById(R.id.textoTituloResultado), 0L);
        motion.fadeUp(textoPontuacao, 45L);
        motion.fadeUp(textoMensagem, 90L);
        motion.fadeUp(findViewById(R.id.cardResumoResultado), 130L);
        motion.fadeUp(findViewById(R.id.botaoFinalizarResultadoQuiz), 190L);
        motion.fadeUp(findViewById(R.id.botaoRepetirResultadoQuiz), 235L);

        ValueAnimator contador = ValueAnimator.ofInt(0, pontuacao);
        contador.setDuration(850L);
        contador.setInterpolator(new OvershootInterpolator(.8f));
        contador.addUpdateListener(animation -> {
            int valor = (int) animation.getAnimatedValue();
            textoPontuacao.setText(String.format(
                    Locale.getDefault(),
                    "%02d/%02d",
                    valor,
                    perguntas.size()
            ));
        });
        contador.start();

        if (pontuacao > 7) {
            // O confete nasce dos dois cantos inferiores, explode em diagonal e cai.
            confetti.postDelayed(confetti::lancar, 520L);
        }
    }

    private void configurarInsetsDaTela() {
        View raiz = findViewById(R.id.raizQuizResultado);

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
        confetti.cancelar();
        motion.cancelAll();
        super.onDestroy();
    }
}
