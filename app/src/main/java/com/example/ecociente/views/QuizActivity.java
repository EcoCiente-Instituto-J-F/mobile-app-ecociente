package com.example.ecociente.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.ecociente.R;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.Motion;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    public static final String EXTRA_ANIMAR_NAVEGACAO = "animarNavegacaoQuiz";

    private final FirebaseAuth autenticacao = FirebaseAuth.getInstance();
    private final List<CardQuizView> cardsQuizzes = new ArrayList<>();
    private final Motion motion = new Motion();

    private TextView textoQuantidadeQuizzes;

    private BarraNavegacaoView barra;

    private static final QuizItem[] QUIZZES = {
            new QuizItem(
                    "Tipos de plástico e reciclagem",
                    "Plástico",
                    "10 perguntas",
                    "6 min",
                    "80% de acerto"
            ),
            new QuizItem(
                    "Impacto do lixo no meio ambiente",
                    "Sustentabilidade",
                    "8 perguntas",
                    "4 min",
                    "75% de acerto"
            ),
            new QuizItem(
                    "Descarte correto de eletrônicos",
                    "Eletrônicos",
                    "10 perguntas",
                    "6 min",
                    null
            ),
            new QuizItem(
                    "Como fazer compostagem em casa",
                    "Compostagem",
                    "7 perguntas",
                    "4 min",
                    "90% de acerto"
            ),
            new QuizItem(
                    "Descarte de papel e papelão",
                    "Papel",
                    "9 perguntas",
                    "5 min",
                    null
            ),
            new QuizItem(
                    "Pegada de carbono no dia a dia",
                    "Sustentabilidade",
                    "10 perguntas",
                    "6 min",
                    null
            ),
            new QuizItem(
                    "Economia de água em casa",
                    "Água",
                    "8 perguntas",
                    "5 min",
                    null
            ),
            new QuizItem(
                    "Coleta seletiva sem erros",
                    "Reciclagem",
                    "12 perguntas",
                    "7 min",
                    "85% de acerto"
            ),
            new QuizItem(
                    "Consumo consciente no cotidiano",
                    "Consumo",
                    "9 perguntas",
                    "5 min",
                    null
            ),
            new QuizItem(
                    "Reciclagem de vidro e metal",
                    "Materiais",
                    "11 perguntas",
                    "6 min",
                    null
            )
    };

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        /*
         * A Home já trabalha em edge-to-edge e aplica os Insets manualmente.
         * O Quiz replica exatamente a mesma estratégia para que cabeçalho e
         * barra inferior ocupem a mesma posição física em qualquer aparelho.
         */
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_quiz_usuario_comum);

        configurarInsetsDaTela();


        /*
         * A tela só é acessada a partir da área autenticada do aplicativo.
         * Não fazemos uma segunda consulta ao Firestore apenas para desenhar
         * uma tela estática; isso evita atraso e flicker na abertura.
         */
        if (autenticacao.getCurrentUser() == null) {
            finish();
            return;
        }

        montarTela();

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        fecharQuiz();
                    }
                }
        );
    }

    private void montarTela() {
        textoQuantidadeQuizzes = findViewById(R.id.textoQuantidadeQuizzes);

        barra = findViewById(R.id.barraNavegacao);

        configurarCabecalho();
        preencherListaQuizzes();
        configurarBusca();
        configurarNavegacao();
        atualizarQuantidadeQuizzes(QUIZZES.length);
        animarEntradaDaTela();
        mostrarBarra();
    }

    private void configurarCabecalho() {
        findViewById(R.id.botaoVoltarQuiz).setOnClickListener(view -> fecharQuiz());

        /*
         * As ações de notificação/perfil serão conectadas depois.
         * Mantemos apenas o feedback de toque do próprio Material agora.
         */
        Motion.pressFeedback(findViewById(R.id.botaoVoltarQuiz));
        Motion.pressFeedback(findViewById(R.id.botaoNotificacoesQuiz));
    }

    private void preencherListaQuizzes() {
        LinearLayout listaQuizzes = findViewById(R.id.listaQuizzes);
        LayoutInflater inflater = LayoutInflater.from(this);

        listaQuizzes.removeAllViews();
        cardsQuizzes.clear();

        int indice = 0;

        for (QuizItem quiz : QUIZZES) {
            MaterialCardView card = (MaterialCardView)
                    inflater.inflate(R.layout.item_quiz_usuario_comum, listaQuizzes, false);

            preencherCard(card, quiz);

            listaQuizzes.addView(card);
            cardsQuizzes.add(new CardQuizView(card, quiz));

            motion.fadeUp(card, 120L + Math.min(indice, 6) * 42L);
            indice++;
        }
    }

    private void preencherCard(@NonNull MaterialCardView card, @NonNull QuizItem quiz) {
        TextView textoTituloQuiz = card.findViewById(R.id.textoTituloQuiz);
        TextView textoCategoriaQuiz = card.findViewById(R.id.textoCategoriaQuiz);
        TextView textoPerguntasQuiz = card.findViewById(R.id.textoPerguntasQuiz);
        TextView textoDuracaoQuiz = card.findViewById(R.id.textoDuracaoQuiz);
        LinearLayout boxResultadoQuiz = card.findViewById(R.id.boxResultadoQuiz);
        TextView textoResultadoQuiz = card.findViewById(R.id.textoResultadoQuiz);
        MaterialButton botaoComecarQuiz = card.findViewById(R.id.botaoComecarQuiz);

        textoTituloQuiz.setText(quiz.titulo);
        textoCategoriaQuiz.setText(quiz.categoria);
        textoPerguntasQuiz.setText(quiz.perguntas);
        textoDuracaoQuiz.setText(quiz.duracao);

        if (quiz.resultado != null && !quiz.resultado.trim().isEmpty()) {
            boxResultadoQuiz.setVisibility(View.VISIBLE);
            textoResultadoQuiz.setText(quiz.resultado);
            botaoComecarQuiz.setVisibility(View.GONE);
        } else {
            boxResultadoQuiz.setVisibility(View.GONE);
            botaoComecarQuiz.setVisibility(View.VISIBLE);
        }

        Motion.pressFeedback(card);
        Motion.pressFeedback(botaoComecarQuiz);
    }

    private void configurarBusca() {
        EditText campoPesquisaQuiz = findViewById(R.id.campoPesquisaQuiz);

        campoPesquisaQuiz.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence texto, int inicio, int quantidade, int depois) {
            }

            @Override
            public void onTextChanged(CharSequence texto, int inicio, int antes, int quantidade) {
                filtrarQuizzes(texto.toString());
            }

            @Override
            public void afterTextChanged(Editable texto) {
            }
        });
    }

    private void filtrarQuizzes(@NonNull String filtro) {
        String filtroNormalizado = filtro.trim().toLowerCase(Locale.ROOT);
        int quantidadeVisivel = 0;

        for (CardQuizView cardQuizView : cardsQuizzes) {
            boolean mostrar =
                    filtroNormalizado.isEmpty()
                            || cardQuizView.quiz.titulo.toLowerCase(Locale.ROOT).contains(filtroNormalizado)
                            || cardQuizView.quiz.categoria.toLowerCase(Locale.ROOT).contains(filtroNormalizado);

            if (mostrar) {
                if (cardQuizView.card.getVisibility() != View.VISIBLE) {
                    cardQuizView.card.setVisibility(View.VISIBLE);
                    cardQuizView.card.setAlpha(0f);
                    cardQuizView.card.animate()
                            .alpha(1f)
                            .setDuration(Motion.STATE_MS)
                            .start();
                }
                quantidadeVisivel++;
            } else {
                cardQuizView.card.setVisibility(View.GONE);
            }
        }

        atualizarQuantidadeQuizzes(quantidadeVisivel);
    }

    private void atualizarQuantidadeQuizzes(int quantidade) {
        textoQuantidadeQuizzes.setText(
                getString(R.string.quizzes_disponiveis_formatado, quantidade)
        );
    }

    private void configurarNavegacao() {
        barra.configurar(ItensBarra.usuario());

        barra.setOnAssistenteClickListener(view -> startActivity(new Intent(this, ChatActivity.class)));

        /*
         * O Guia ainda não possui rota final nesta etapa.
         * Não mostramos Toasts de "em breve" para não quebrar a sensação
         * profissional da barra.
         */
        barra.setOnItemClickListener(
                indice -> {
                    if (indice == ItensBarra.HOME) {
                        fecharQuiz();

                    } else if (indice == ItensBarra.PERFIL) {
                        abrirPerfil();
                    }
                });
    }

    private void abrirPerfil() {
        Intent rota = new Intent(this, GerenciarPerfilActivity.class);

        rota.putExtra(
                GerenciarPerfilActivity.EXTRA_ORIGEM_NAVEGACAO, GerenciarPerfilActivity.ORIGEM_QUIZ);

        startActivity(rota);

        finish();

        overridePendingTransition(0, 0);
    }

    private void animarEntradaDaTela() {
        motion.fadeUp(findViewById(R.id.cabecalhoQuiz), 0L);
        motion.fadeUp(findViewById(R.id.cardPesquisaQuiz), 55L);
        motion.fadeUp(findViewById(R.id.linhaResumoQuiz), 95L);
    }

    private void mostrarBarra() {
        boolean animar = getIntent().getBooleanExtra(EXTRA_ANIMAR_NAVEGACAO, true);

        barra.selecionar(ItensBarra.QUIZ_OU_CONDOMINIOS, animar ? ItensBarra.HOME : -1);
    }

    private void configurarInsetsDaTela() {
        View raiz = findViewById(R.id.raizQuizUsuarioComum);

        if (raiz == null) {
            return;
        }

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
            controlador.setAppearanceLightStatusBars(true);
            controlador.setAppearanceLightNavigationBars(true);
        }

        ViewCompat.requestApplyInsets(raiz);
    }

    private void fecharQuiz() {
        finish();

        /*
         * A animação visual acontece nos próprios componentes.
         * Removemos o fade de Activity que causava o "apagão" branco.
         */
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        motion.cancelAll();
        super.onDestroy();
    }

    private static final class CardQuizView {
        private final MaterialCardView card;
        private final QuizItem quiz;

        private CardQuizView(@NonNull MaterialCardView card, @NonNull QuizItem quiz) {
            this.card = card;
            this.quiz = quiz;
        }
    }

    private static final class QuizItem {
        private final String titulo;
        private final String categoria;
        private final String perguntas;
        private final String duracao;
        private final String resultado;

        private QuizItem(String titulo, String categoria, String perguntas, String duracao, String resultado) {
            this.titulo = titulo;
            this.categoria = categoria;
            this.perguntas = perguntas;
            this.duracao = duracao;
            this.resultado = resultado;
        }
    }
}
