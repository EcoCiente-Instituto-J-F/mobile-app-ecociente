package com.example.ecociente.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.ecociente.R;
import com.example.ecociente.midias.MidiasEco;
import com.example.ecociente.midias.Midia;
import com.example.ecociente.midias.CarregadorMidias;
import com.example.ecociente.model.QuizConteudo;
import com.example.ecociente.model.DadosDemonstracaoEducacao;
import com.example.ecociente.repository.EducacaoRepository;
import android.widget.Toast;
import com.example.ecociente.ui.Motion;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class InicioQuizActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORIA = "categoriaQuiz";
    public static final String EXTRA_TITULO = "eco.quiz.titulo";
    public static final String EXTRA_QUIZ_ID = "eco.quiz.id";
    public static final String EXTRA_PERGUNTAS = "perguntasQuiz";

    private final FirebaseAuth autenticacao = FirebaseAuth.getInstance();
    private final Motion motion = new Motion();
    private final EducacaoRepository repositorio = new EducacaoRepository();

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

        setContentView(R.layout.activity_inicio_quiz);

        configurarInsetsDaTela();

        if (!DadosDemonstracaoEducacao.ATIVO && autenticacao.getCurrentUser() == null) {
            finish();
            return;
        }

        if (QuizConteudo.obterPerguntas().isEmpty()) {
            finish();
            return;
        }

        configurarConteudo();
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

    private void configurarConteudo() {
        TextView textoTitulo = findViewById(R.id.textoTituloInicioQuiz);
        TextView textoPerguntas = findViewById(R.id.textoPerguntasInicioQuiz);

        String categoria = getIntent().getStringExtra(EXTRA_CATEGORIA);
        if (categoria == null || categoria.trim().isEmpty()) {
            categoria = getString(R.string.inicio_quiz_titulo_padrao);
        }

        int quantidadePerguntas = QuizConteudo.obterPerguntas().size();

        String titulo = getIntent().getStringExtra(EXTRA_TITULO);
        if (titulo == null || titulo.trim().isEmpty()) titulo = categoria;
        int quizId = getIntent().getIntExtra(EXTRA_QUIZ_ID, QuizConteudo.getQuizId());
        CarregadorMidias.mostrar(findViewById(R.id.imagemIlustracaoInicioQuiz),
                MidiasEco.obter(this).capaQuiz(quizId, titulo, categoria),
                false, R.drawable.quiz_sustentabilidade_inicio);
        textoTitulo.setText(categoria);
        textoPerguntas.setText(
                getString(R.string.inicio_quiz_perguntas_formatado, quantidadePerguntas)
        );
    }

    private void configurarAcoes() {
        ImageView botaoVoltar = findViewById(R.id.botaoVoltarInicioQuiz);
        MaterialButton botaoIniciar = findViewById(R.id.botaoIniciarQuiz);

        botaoVoltar.setOnClickListener(view -> voltarParaQuizzes());

        botaoIniciar.setOnClickListener(view -> {
            if (DadosDemonstracaoEducacao.ehQuizDemonstracao(QuizConteudo.getQuizId())) {
                // Não chama POST /tentativas: o quiz fictício será corrigido no dispositivo.
                abrirPerguntas();
                return;
            }
            botaoIniciar.setEnabled(false);
            botaoIniciar.setText("Iniciando...");
            repositorio.iniciarTentativa(this, QuizConteudo.getQuizId(), (sucesso, id, erro) -> {
                if (isFinishing() || isDestroyed()) return;
                botaoIniciar.setEnabled(true);
                botaoIniciar.setText("Iniciar quiz");
                if (!sucesso) {
                    Toast.makeText(this, erro, Toast.LENGTH_LONG).show();
                    return;
                }
                QuizConteudo.definirTentativa(id);
                abrirPerguntas();
            });
        });

        Motion.pressFeedback(botaoVoltar);
        Motion.pressFeedback(botaoIniciar);
    }

    private void abrirPerguntas() {
        String categoria = getIntent().getStringExtra(EXTRA_CATEGORIA);
        if (categoria == null || categoria.trim().isEmpty()) {
            categoria = getString(R.string.inicio_quiz_titulo_padrao);
        }
        Intent rota = new Intent(this, QuizPerguntasActivity.class);
        rota.putExtra(QuizPerguntasActivity.EXTRA_CATEGORIA, categoria);
        startActivity(rota);
        overridePendingTransition(0, 0);
    }

    private void animarEntrada() {
        motion.fadeUp(findViewById(R.id.logoInicioQuiz), 0L);
        motion.fadeUp(findViewById(R.id.imagemIlustracaoInicioQuiz), 70L);
        motion.fadeUp(findViewById(R.id.blocoInformacoesInicioQuiz), 120L);
        motion.fadeUp(findViewById(R.id.botaoIniciarQuiz), 175L);
    }

    private void configurarInsetsDaTela() {
        View raiz = findViewById(R.id.raizInicioQuiz);

        final int paddingEsquerdoOriginal = raiz.getPaddingLeft();
        final int paddingTopoOriginal = raiz.getPaddingTop();
        final int paddingDireitoOriginal = raiz.getPaddingRight();
        final int paddingInferiorOriginal = raiz.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(
                raiz,
                (view, insets) -> {
                    Insets sistema = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.displayCutout()
                    );

                    view.setPadding(
                            paddingEsquerdoOriginal + sistema.left,
                            paddingTopoOriginal + sistema.top,
                            paddingDireitoOriginal + sistema.right,
                            paddingInferiorOriginal + sistema.bottom
                    );

                    return insets;
                }
        );

        WindowInsetsControllerCompat controlador =
                WindowCompat.getInsetsController(getWindow(), raiz);

        if (controlador != null) {
            controlador.setAppearanceLightStatusBars(false);
            controlador.setAppearanceLightNavigationBars(false);
        }

        ViewCompat.requestApplyInsets(raiz);
    }

    private void voltarParaQuizzes() {
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        motion.cancelAll();
        super.onDestroy();
    }
}
