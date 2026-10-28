package com.example.ecociente.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.widget.NestedScrollView;
import com.example.ecociente.R;
import com.example.ecociente.model.Conversa;
import com.example.ecociente.model.MensagemConversa;
import java.util.ArrayList;
import java.util.List;
import com.example.ecociente.ui.Iniciais;
import com.google.android.material.button.MaterialButton;
import com.example.ecociente.ui.IndicadorCarregamento;
import com.example.ecociente.ui.AvisoFlutuante;
import com.example.ecociente.repository.ConversaRepository;

public class ConversaActivity extends AppCompatActivity {

    private static final String EXTRA_ID = "idConversa";
    private static final String EXTRA_NOME = "nomeConversa";
    private static final String EXTRA_SINDICO = "conversaDoSindico";

    private final List<MensagemConversa> mensagens = new ArrayList<>();
    private final ConversaRepository repositorio = new ConversaRepository();
    private int conversaId;
    private String nome;
    private String meuLado;
    private boolean carregou;
    private LinearLayout container;
    private NestedScrollView rolagem;
    private EditText campo;
    private View botaoEnviar;

    @NonNull
    public static Intent criarIntent(@NonNull Context contexto, @NonNull Conversa conversa, boolean sindico) {
        return criarIntent(contexto, conversa.id, conversa.nome, sindico);
    }

    @NonNull
    public static Intent criarIntent(@NonNull Context contexto, int conversaId, @NonNull String nome, boolean sindico) {
        return new Intent(contexto, ConversaActivity.class)
                .putExtra(EXTRA_ID, conversaId)
                .putExtra(EXTRA_NOME, nome)
                .putExtra(EXTRA_SINDICO, sindico);
    }

    @Override
    protected void onCreate(@Nullable Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_conversa);

        conversaId = getIntent().getIntExtra(EXTRA_ID, 0);
        nome = getIntent().getStringExtra(EXTRA_NOME);

        if (conversaId <= 0 || nome == null) {
            finish();
            return;
        }

        meuLado = getIntent().getBooleanExtra(EXTRA_SINDICO, false)
                ? ConversaRepository.ladoDoSindico()
                : ConversaRepository.ladoDaCooperativa();

        configurarInsets();

        container = findViewById(R.id.containerMensagensConversa);
        rolagem = findViewById(R.id.rolagemConversa);
        campo = findViewById(R.id.campoMensagemConversa);
        botaoEnviar = findViewById(R.id.botaoEnviarConversa);

        ((TextView) findViewById(R.id.tituloConversa)).setText(nome);
        campo.setHint(R.string.conversa_hint_mensagem);

        findViewById(R.id.botaoVoltarConversa).setOnClickListener(view -> finish());
        botaoEnviar.setOnClickListener(view -> enviar());
        campo.setOnEditorActionListener(
                (view, acao, evento) -> {
                    enviar();
                    return true;
                });

        carregar();
    }

    private void carregar() {
        container.removeAllViews();
        IndicadorCarregamento.mostrar(container, R.string.conversa_carregando);

        repositorio.mensagens(this, conversaId, meuLado, carregadas -> {
            if (isFinishing() || isDestroyed()) return;

            if (carregadas == null) {
                mostrarErro();
                return;
            }

            carregou = true;
            mensagens.clear();
            mensagens.addAll(carregadas);
            exibirMensagens();
            atualizarCampo();
            repositorio.marcarComoLida(this, conversaId);
        });
    }

    private void mostrarErro() {
        container.removeAllViews();

        TextView mensagem = new TextView(this);
        mensagem.setText(R.string.conversa_erro_mensagens);
        mensagem.setTextColor(getColor(R.color.cinza_texto_home));
        mensagem.setTextSize(14);
        mensagem.setPadding(32, 112, 32, 24);
        container.addView(mensagem);

        MaterialButton tentar = (MaterialButton) getLayoutInflater().inflate(R.layout.botao_dialogo, container, false);
        tentar.setText(R.string.tentar_novamente);
        tentar.setOnClickListener(view -> carregar());
        container.addView(tentar);

        atualizarCampo();
    }

    // O condomínio não abre a conversa: enquanto a cooperativa não escrever, o campo fica travado.
    private void atualizarCampo() {
        boolean podeEscrever = carregou && ConversaRepository.podeEscrever(meuLado, mensagens.size());

        campo.setEnabled(podeEscrever);
        botaoEnviar.setEnabled(podeEscrever);

        if (carregou && !podeEscrever) {
            campo.setHint(R.string.conversa_aguarde_cooperativa);
        } else {
            campo.setHint(R.string.conversa_hint_mensagem);
        }
    }

    private void configurarInsets() {

        View raiz = findViewById(R.id.raizConversa);

        ViewCompat.setOnApplyWindowInsetsListener(
                raiz,
                (view, insets) -> {
                    Insets barras =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.statusBars()
                                            | WindowInsetsCompat.Type.navigationBars());

                    Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());

                    view.setPadding(
                            barras.left, barras.top, barras.right, Math.max(barras.bottom, teclado.bottom));

                    return insets;
                });

        WindowInsetsControllerCompat controlador = WindowCompat.getInsetsController(getWindow(), raiz);

        controlador.setAppearanceLightStatusBars(false);
        controlador.setAppearanceLightNavigationBars(true);

        ViewCompat.requestApplyInsets(raiz);
    }

    private void enviar() {
        String texto = campo.getText().toString().trim();

        if (texto.isEmpty() || !campo.isEnabled()) {
            return;
        }

        campo.setEnabled(false);
        botaoEnviar.setEnabled(false);

        repositorio.enviar(this, conversaId, texto, meuLado, (enviada, erro) -> {
            if (isFinishing() || isDestroyed()) return;

            if (enviada == null) {
                atualizarCampo();
                AvisoFlutuante.mostrar(this, erro);
                return;
            }

            campo.setText("");

            InputMethodManager teclado = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

            if (teclado != null) {
                teclado.hideSoftInputFromWindow(campo.getWindowToken(), 0);
            }

            mensagens.add(enviada);
            exibirMensagens();
            atualizarCampo();
        });
    }

    private void exibirMensagens() {

        container.removeAllViews();

        if (mensagens.isEmpty()) {
            TextView vazio = new TextView(this);
            vazio.setText(R.string.conversa_sem_mensagens);
            vazio.setTextColor(getColor(R.color.cinza_texto_home));
            vazio.setTextSize(14);
            vazio.setPadding(32, 112, 32, 24);
            container.addView(vazio);
            return;
        }

        LayoutInflater inflador = LayoutInflater.from(this);

        String iniciais = Iniciais.de(nome);

        for (MensagemConversa mensagem : mensagens) {

            if (mensagem.enviada) {
                View linha = inflador.inflate(R.layout.item_mensagem_enviada, container, false);

                ((TextView) linha.findViewById(R.id.textoMensagemEnviada)).setText(mensagem.texto);
                ((TextView) linha.findViewById(R.id.horaMensagemEnviada)).setText(mensagem.hora);

                container.addView(linha);

            } else {
                View linha = inflador.inflate(R.layout.item_mensagem_recebida, container, false);

                ((TextView) linha.findViewById(R.id.textoIniciaisMensagem)).setText(iniciais);
                ((TextView) linha.findViewById(R.id.textoMensagemRecebida)).setText(mensagem.texto);
                ((TextView) linha.findViewById(R.id.horaMensagemRecebida)).setText(mensagem.hora);

                container.addView(linha);
            }
        }

        rolagem.post(() -> rolagem.fullScroll(View.FOCUS_DOWN));
    }
}
