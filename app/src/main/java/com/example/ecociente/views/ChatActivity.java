package com.example.ecociente.views;

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
import com.example.ecociente.viewmodels.ChatViewModel;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

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
    private boolean conversaIniciada;

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

        viewModel.enviarMensagem(mensagem);
    }

    private void renderizarMensagens(List<MensagemChat> mensagens) {
        containerMensagensChat.removeAllViews();

        for (MensagemChat mensagem : mensagens) {
            if (mensagem.getAutor() == MensagemChat.Autor.USUARIO) {
                adicionarMensagemUsuarioNaTela(mensagem.getTexto());
            } else {
                String texto = mensagem.getTipo() == MensagemChat.Tipo.ERRO_CONEXAO
                        ? getString(R.string.chat_erro_conexao)
                        : mensagem.getTexto();
                adicionarMensagemEkoNaTela(texto);
            }
        }

        rolagemMensagensChat.post(
                () -> rolagemMensagensChat.fullScroll(View.FOCUS_DOWN));
    }

    private void adicionarMensagemUsuarioNaTela(String mensagem) {
        View linhaMensagem = LayoutInflater.from(this)
                .inflate(R.layout.item_mensagem_usuario, containerMensagensChat, false);
        TextView textoMensagem = linhaMensagem.findViewById(R.id.textoMensagemUsuario);
        textoMensagem.setText(mensagem);
        containerMensagensChat.addView(linhaMensagem);
    }

    private void adicionarMensagemEkoNaTela(String mensagem) {
        View linhaMensagem = LayoutInflater.from(this)
                .inflate(R.layout.item_mensagem_eko, containerMensagensChat, false);
        TextView textoMensagem = linhaMensagem.findViewById(R.id.textoMensagemEko);
        textoMensagem.setText(mensagem);
        containerMensagensChat.addView(linhaMensagem);
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

    private float dpParaPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
