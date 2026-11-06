package com.example.ecociente.views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecociente.R;
import com.example.ecociente.model.MensagemChat;
import com.example.ecociente.ui.Motion;
import com.example.ecociente.viewmodels.ChatViewModel;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private static final long DURACAO_TRANSICAO = 360L;

    private View raizChat;
    private View estadoInicialChat;
    private View estadoConversaChat;
    private EditText campoMensagemChat;
    private ImageButton botaoEnviarChat;
    private ImageButton botaoVoltarChat;
    private ImageButton botaoSairChat;
    private LinearLayout containerMensagensChat;
    private NestedScrollView rolagemMensagensChat;
    private ChatViewModel viewModel;
    private final List<ObjectAnimator> animacoesDigitacao = new ArrayList<>();
    private final Handler animadorTexto = new Handler(Looper.getMainLooper());
    private Runnable proximoTrecho;
    private boolean conversaIniciada;
    private int totalMensagensAnterior;
    private boolean animacaoTextoEmCurso;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_chat);

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        inicializarComponentes();
        configurarInsetsDoSistema();
        configurarAcoes();
        observarEstadoDoChat();
        Motion.entrar(this, R.id.estadoInicialChat, R.id.botaoSairChat);
    }

    private void inicializarComponentes() {
        raizChat = findViewById(R.id.raizChat);
        estadoInicialChat = findViewById(R.id.estadoInicialChat);
        estadoConversaChat = findViewById(R.id.estadoConversaChat);
        campoMensagemChat = findViewById(R.id.campoMensagemChat);
        botaoEnviarChat = findViewById(R.id.botaoEnviarChat);
        botaoVoltarChat = findViewById(R.id.botaoVoltarChat);
        botaoSairChat = findViewById(R.id.botaoSairChat);
        containerMensagensChat = findViewById(R.id.containerMensagensChat);
        rolagemMensagensChat = findViewById(R.id.rolagemMensagensChat);
    }

    private void configurarInsetsDoSistema() {
        ViewCompat.setOnApplyWindowInsetsListener(
                raizChat,
                (view, insets) -> {
                    Insets barras = insets.getInsets(
                            WindowInsetsCompat.Type.statusBars()
                                    | WindowInsetsCompat.Type.navigationBars());
                    Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());

                    view.setPadding(
                            barras.left,
                            barras.top,
                            barras.right,
                            Math.max(barras.bottom, teclado.bottom));
                    return insets;
                });

        WindowInsetsControllerCompat controlador =
                WindowCompat.getInsetsController(getWindow(), raizChat);
        controlador.setAppearanceLightStatusBars(false);
        controlador.setAppearanceLightNavigationBars(true);
        ViewCompat.requestApplyInsets(raizChat);
    }

    private void configurarAcoes() {
        botaoEnviarChat.setOnClickListener(view -> enviarMensagem());
        Motion.pressFeedback(botaoEnviarChat);
        Motion.pressFeedback(botaoVoltarChat);
        botaoVoltarChat.setOnClickListener(view -> voltarParaInicioChat());
        botaoSairChat.setOnClickListener(view -> finish());

        campoMensagemChat.setOnEditorActionListener(
                (view, actionId, evento) -> {
                    enviarMensagem();
                    return true;
                });

        configurarSugestao(
                R.id.opcaoMaterialReciclavel,
                getString(R.string.chat_opcao_material));
        configurarSugestao(
                R.id.opcaoComoReciclar,
                getString(R.string.chat_opcao_reciclar));
        configurarSugestao(
                R.id.opcaoCooperativaProxima,
                getString(R.string.chat_opcao_cooperativa));
    }

    private void configurarSugestao(int idCard, String mensagem) {
        MaterialCardView card = findViewById(idCard);
        Motion.pressFeedback(card);
        card.setOnClickListener(
                view -> {
                    campoMensagemChat.setText(mensagem);
                    campoMensagemChat.setSelection(mensagem.length());
                    campoMensagemChat.requestFocus();
                });
    }

    private void observarEstadoDoChat() {
        viewModel.getMensagens().observe(
                this,
                mensagens -> {
                    renderizarMensagens(mensagens);
                    if (!mensagens.isEmpty() && !conversaIniciada) {
                        conversaIniciada = true;
                        exibirEstadoConversaSemAnimacao();
                    }
                });

        viewModel.getEnvioEmAndamento().observe(
                this,
                emAndamento -> {
                    boolean enviando = Boolean.TRUE.equals(emAndamento);
                    botaoEnviarChat.setEnabled(!enviando);
                    botaoEnviarChat.setAlpha(enviando ? 0.55f : 1f);
                    List<MensagemChat> mensagensAtuais = viewModel.getMensagens().getValue();
                    if (mensagensAtuais != null && !animacaoTextoEmCurso) {
                        renderizarMensagens(mensagensAtuais);
                    }
                });
    }

    private void enviarMensagem() {
        String mensagem = campoMensagemChat.getText().toString().trim();

        if (mensagem.isEmpty()
                || Boolean.TRUE.equals(viewModel.getEnvioEmAndamento().getValue())) {
            campoMensagemChat.requestFocus();
            return;
        }

        campoMensagemChat.setText("");
        esconderTeclado();

        if (!conversaIniciada) {
            conversaIniciada = true;
            transicionarParaConversa();
        }

        viewModel.enviarMensagem(this, mensagem);
    }

    private void renderizarMensagens(List<MensagemChat> mensagens) {
        boolean chegouResposta = mensagens.size() > totalMensagensAnterior
                && !mensagens.isEmpty()
                && mensagens.get(mensagens.size() - 1).getAutor() == MensagemChat.Autor.EKO
                && mensagens.get(mensagens.size() - 1).getTipo() == MensagemChat.Tipo.TEXTO;
        totalMensagensAnterior = mensagens.size();

        interromperAnimacoes();
        containerMensagensChat.removeAllViews();

        int ultimoIndice = mensagens.size() - 1;
        for (int indice = 0; indice < mensagens.size(); indice++) {
            MensagemChat mensagem = mensagens.get(indice);
            if (mensagem.getAutor() == MensagemChat.Autor.USUARIO) {
                adicionarMensagemUsuarioNaTela(mensagem.getTexto());
            } else {
                String texto = mensagem.getTipo() == MensagemChat.Tipo.ERRO_CONEXAO
                        ? getString(R.string.chat_erro_conexao)
                        : mensagem.getTexto();
                View linha = adicionarMensagemEkoNaTela(chegouResposta && indice == ultimoIndice ? "" : texto);
                if (chegouResposta && indice == ultimoIndice) {
                    linha.setAlpha(0f);
                    linha.setTranslationY(dpParaPx(12));
                    linha.animate().alpha(1f).translationY(0f).setDuration(Motion.ENTER_MS).start();
                    if (animacoesAtivas()) {
                        iniciarExibicaoGradual(linha.findViewById(R.id.textoMensagemEko), texto);
                    } else {
                        ((TextView) linha.findViewById(R.id.textoMensagemEko)).setText(texto);
                    }
                }
            }
        }

        boolean aguardandoResposta = Boolean.TRUE.equals(viewModel.getEnvioEmAndamento().getValue());
        boolean ultimaMensagemDoUsuario = !mensagens.isEmpty()
                && mensagens.get(ultimoIndice).getAutor() == MensagemChat.Autor.USUARIO;
        if (aguardandoResposta && ultimaMensagemDoUsuario) {
            adicionarIndicadorDigitacaoEko();
        }
        rolarParaUltimaMensagem();
    }

    private boolean animacoesAtivas() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O || ValueAnimator.areAnimatorsEnabled();
    }

    private void iniciarExibicaoGradual(TextView destino, String texto) {
        int totalCaracteres = texto.codePointCount(0, texto.length());
        if (totalCaracteres == 0) return;

        int caracteresPorPasso = Math.max(1, (int) Math.ceil(totalCaracteres / 110.0));
        animacaoTextoEmCurso = true;
        int[] indiceUtf16 = {0};

        proximoTrecho = new Runnable() {
            @Override
            public void run() {
                if (isFinishing() || isDestroyed()) {
                    animacaoTextoEmCurso = false;
                    return;
                }
                int caracteresRestantes = texto.codePointCount(indiceUtf16[0], texto.length());
                int avancar = Math.min(caracteresPorPasso, caracteresRestantes);
                indiceUtf16[0] = texto.offsetByCodePoints(indiceUtf16[0], avancar);
                destino.setText(texto.substring(0, indiceUtf16[0]));
                rolarParaUltimaMensagem();

                if (indiceUtf16[0] < texto.length()) {
                    animadorTexto.postDelayed(this, 23L);
                } else {
                    animacaoTextoEmCurso = false;
                    proximoTrecho = null;
                }
            }
        };
        animadorTexto.post(proximoTrecho);
    }

    private void adicionarIndicadorDigitacaoEko() {
        View linha = LayoutInflater.from(this).inflate(
                R.layout.item_digitacao_eko, containerMensagensChat, false);
        containerMensagensChat.addView(linha);

        if (!animacoesAtivas()) return;

        int[] ids = {
                R.id.pontoDigitacaoEko1,
                R.id.pontoDigitacaoEko2,
                R.id.pontoDigitacaoEko3
        };
        for (int i = 0; i < ids.length; i++) {
            View ponto = linha.findViewById(ids[i]);
            ObjectAnimator salto = ObjectAnimator.ofFloat(
                    ponto, View.TRANSLATION_Y, 0f, -dpParaPx(9), 0f);
            salto.setDuration(750L);
            salto.setStartDelay(i * 160L);
            salto.setRepeatCount(ValueAnimator.INFINITE);
            salto.start();
            animacoesDigitacao.add(salto);
        }
    }

    private void interromperAnimacoes() {
        for (ObjectAnimator animacao : animacoesDigitacao) animacao.cancel();
        animacoesDigitacao.clear();
        if (proximoTrecho != null) {
            animadorTexto.removeCallbacks(proximoTrecho);
            proximoTrecho = null;
        }
        animacaoTextoEmCurso = false;
    }

    private void rolarParaUltimaMensagem() {
        rolagemMensagensChat.post(() -> {
            if (!isFinishing() && !isDestroyed()) {
                rolagemMensagensChat.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void adicionarMensagemUsuarioNaTela(String mensagem) {
        View linhaMensagem = LayoutInflater.from(this)
                .inflate(R.layout.item_mensagem_usuario, containerMensagensChat, false);
        TextView textoMensagem = linhaMensagem.findViewById(R.id.textoMensagemUsuario);
        textoMensagem.setText(mensagem);
        containerMensagensChat.addView(linhaMensagem);
    }

    private View adicionarMensagemEkoNaTela(String mensagem) {
        View linhaMensagem = LayoutInflater.from(this)
                .inflate(R.layout.item_mensagem_eko, containerMensagensChat, false);
        TextView textoMensagem = linhaMensagem.findViewById(R.id.textoMensagemEko);
        textoMensagem.setText(mensagem);
        containerMensagensChat.addView(linhaMensagem);
        return linhaMensagem;
    }

    private void transicionarParaConversa() {
        botaoSairChat.setVisibility(View.GONE);
        estadoConversaChat.setVisibility(View.VISIBLE);
        estadoConversaChat.setAlpha(0f);
        estadoConversaChat.setTranslationY(dpParaPx(28));

        estadoInicialChat
                .animate()
                .alpha(0f)
                .translationY(-dpParaPx(20))
                .setDuration(DURACAO_TRANSICAO)
                .start();

        estadoConversaChat
                .animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(DURACAO_TRANSICAO)
                .withEndAction(
                        () -> {
                            estadoInicialChat.setVisibility(View.GONE);
                            estadoInicialChat.setAlpha(1f);
                            estadoInicialChat.setTranslationY(0f);
                            raizChat.setBackgroundColor(getColor(R.color.branco));
                        })
                .start();
    }

    private void exibirEstadoConversaSemAnimacao() {
        botaoSairChat.setVisibility(View.GONE);
        estadoInicialChat.setVisibility(View.GONE);
        estadoConversaChat.setVisibility(View.VISIBLE);
        estadoConversaChat.setAlpha(1f);
        estadoConversaChat.setTranslationY(0f);
        raizChat.setBackgroundColor(getColor(R.color.branco));
    }

    private void voltarParaInicioChat() {
        esconderTeclado();
        interromperAnimacoes();
        conversaIniciada = false;
        estadoConversaChat.animate().cancel();
        estadoInicialChat.animate().cancel();
        estadoConversaChat.setVisibility(View.GONE);
        estadoConversaChat.setAlpha(1f);
        estadoConversaChat.setTranslationY(0f);
        estadoInicialChat.setVisibility(View.VISIBLE);
        estadoInicialChat.setAlpha(1f);
        estadoInicialChat.setTranslationY(0f);
        botaoSairChat.setVisibility(View.VISIBLE);
        raizChat.setBackgroundColor(getColor(R.color.verde_escuro_principal));
    }

    private void esconderTeclado() {
        InputMethodManager teclado = getSystemService(InputMethodManager.class);
        if (teclado != null) {
            teclado.hideSoftInputFromWindow(campoMensagemChat.getWindowToken(), 0);
        }
        campoMensagemChat.clearFocus();
    }

    @Override
    protected void onStop() {
        interromperAnimacoes();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        interromperAnimacoes();
        super.onDestroy();
    }

    private float dpParaPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
