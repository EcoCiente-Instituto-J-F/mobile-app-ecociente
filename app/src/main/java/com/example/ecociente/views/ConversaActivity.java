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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.example.ecociente.ui.Iniciais;

public class ConversaActivity extends AppCompatActivity {

    private static final String EXTRA_NOME = "nomeConversa";
    private static final String EXTRA_PREVIA = "previaConversa";
    private static final String EXTRA_HORA = "horaConversa";

    private final List<MensagemConversa> mensagens = new ArrayList<>();

    private String nome;
    private LinearLayout container;
    private NestedScrollView rolagem;
    private EditText campo;

    @NonNull
    public static Intent criarIntent(@NonNull Context contexto, @NonNull Conversa conversa) {

        return new Intent(contexto, ConversaActivity.class)
                .putExtra(EXTRA_NOME, conversa.nome)
                .putExtra(EXTRA_PREVIA, conversa.ultimaMensagem)
                .putExtra(EXTRA_HORA, conversa.hora);
    }

    @NonNull
    public static Intent criarIntent(@NonNull Context contexto, @NonNull String nome) {

        return new Intent(contexto, ConversaActivity.class)
                .putExtra(EXTRA_NOME, nome)
                .putExtra(EXTRA_PREVIA, "Quando vocês podem passar?")
                .putExtra(EXTRA_HORA, "08:30");
    }

    @Override
    protected void onCreate(@Nullable Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_conversa);

        nome = getIntent().getStringExtra(EXTRA_NOME);

        if (nome == null) {
            finish();
            return;
        }

        configurarInsets();

        container = findViewById(R.id.containerMensagensConversa);
        rolagem = findViewById(R.id.rolagemConversa);
        campo = findViewById(R.id.campoMensagemConversa);

        ((TextView) findViewById(R.id.tituloConversa)).setText(nome);

        findViewById(R.id.botaoVoltarConversa).setOnClickListener(view -> finish());

        findViewById(R.id.botaoEnviarConversa).setOnClickListener(view -> enviar());

        campo.setOnEditorActionListener(
                (view, acao, evento) -> {
                    enviar();
                    return true;
                });

        mensagens.addAll(
                MensagemConversa.exemplos(
                        getIntent().getStringExtra(EXTRA_PREVIA), getIntent().getStringExtra(EXTRA_HORA)));

        exibirMensagens();
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

        if (texto.isEmpty()) {
            return;
        }

        campo.setText("");

        InputMethodManager teclado = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if (teclado != null) {
            teclado.hideSoftInputFromWindow(campo.getWindowToken(), 0);
        }

        mensagens.add(
                new MensagemConversa(texto, new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()), true));

        exibirMensagens();
    }

    private void exibirMensagens() {

        container.removeAllViews();

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
