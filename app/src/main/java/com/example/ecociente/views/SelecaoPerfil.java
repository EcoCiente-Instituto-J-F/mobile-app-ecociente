package com.example.ecociente.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.example.ecociente.R;
import com.example.ecociente.model.TipoPerfil;
import com.example.ecociente.ui.Motion;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class SelecaoPerfil extends AppCompatActivity {

    public static final String EXTRA_TIPO_PERFIL = "tipoPerfil";


    private MaterialCardView painelSelecaoPerfil;
    private MaterialCardView cardOpcaoUsuario;
    private MaterialCardView cardOpcaoCooperativa;

    private ImageView indicadorOpcaoUsuario;
    private ImageView indicadorOpcaoCooperativa;

    private MaterialButton botaoContinuarPerfil;

    private Motion motion;

    private String tipoPerfilSelecionado = TipoPerfil.USUARIO;
    private boolean fechando = false;

    @Override
    protected void onCreate(Bundle estadoSalvo) {

        super.onCreate(estadoSalvo);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_selecao_perfil);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ajustarJanelaParaBaseDaTela();

        inicializarComponentes();

        configurarCliques();

        animarEntrada();
    }

    private void ajustarJanelaParaBaseDaTela() {

        getWindow().setGravity(Gravity.BOTTOM);

        getWindow().setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);

        WindowManager.LayoutParams parametros = getWindow().getAttributes();
        parametros.dimAmount = 0.38f;

        getWindow().setAttributes(parametros);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    private void inicializarComponentes() {

        painelSelecaoPerfil = findViewById(R.id.painelSelecaoPerfil);

        cardOpcaoUsuario = findViewById(R.id.cardOpcaoUsuario);
        cardOpcaoCooperativa = findViewById(R.id.cardOpcaoCooperativa);

        indicadorOpcaoUsuario = findViewById(R.id.indicadorOpcaoUsuario);
        indicadorOpcaoCooperativa = findViewById(R.id.indicadorOpcaoCooperativa);

        botaoContinuarPerfil = findViewById(R.id.botaoContinuarPerfil);

        motion = new Motion();
    }

    private void configurarCliques() {

        cardOpcaoUsuario.setOnClickListener(view -> selecionarPerfil(TipoPerfil.USUARIO));
        cardOpcaoCooperativa.setOnClickListener(view -> selecionarPerfil(TipoPerfil.COOPERATIVA));
        botaoContinuarPerfil.setOnClickListener(view -> continuar());

        Motion.pressFeedback(cardOpcaoUsuario);
        Motion.pressFeedback(cardOpcaoCooperativa);
        Motion.pressFeedback(botaoContinuarPerfil);
    }

    private void animarEntrada() {

        painelSelecaoPerfil.post(
                () -> {
                    float deslocamento = Math.max(
                            painelSelecaoPerfil.getHeight(),
                            220f * getResources().getDisplayMetrics().density);

                    painelSelecaoPerfil.setTranslationY(deslocamento);
                    painelSelecaoPerfil.setAlpha(0.97f);

                    painelSelecaoPerfil
                            .animate()
                            .translationY(0f)
                            .alpha(1f)
                            .setDuration(360L)
                            .setInterpolator(new FastOutSlowInInterpolator())
                            .start();

                    View titulo = findViewById(R.id.tituloSelecaoPerfil);
                    View subtitulo = findViewById(R.id.subtituloSelecaoPerfil);
                    View containerBotao = findViewById(R.id.containerBotaoContinuarPerfil);

                    motion.staggerIn(
                            titulo,
                            subtitulo,
                            cardOpcaoUsuario,
                            cardOpcaoCooperativa,
                            containerBotao);
                });
    }

    private void selecionarPerfil(String tipo) {

        tipoPerfilSelecionado = tipo;

        boolean usuarioSelecionado = TipoPerfil.USUARIO.equals(tipo);

        atualizarVisualOpcao(
                cardOpcaoUsuario,
                indicadorOpcaoUsuario,
                usuarioSelecionado);

        atualizarVisualOpcao(
                cardOpcaoCooperativa,
                indicadorOpcaoCooperativa,
                !usuarioSelecionado);
    }

    private void atualizarVisualOpcao(
            MaterialCardView card,
            ImageView indicador,
            boolean selecionado) {

        int corBorda =
                selecionado
                        ? ContextCompat.getColor(this, R.color.verde_escuro_principal)
                        : android.graphics.Color.parseColor("#D7E6E1");

        card.setStrokeColor(corBorda);
        card.setStrokeWidth(dp(selecionado ? 2 : 1));

        indicador.setImageResource(
                selecionado
                        ? R.drawable.ic_radio_selecionado
                        : R.drawable.ic_radio_nao_selecionado);

        card.animate()
                .scaleX(selecionado ? 1.01f : 1f)
                .scaleY(selecionado ? 1.01f : 1f)
                .setDuration(Motion.STATE_MS)
                .setInterpolator(new FastOutSlowInInterpolator())
                .start();
    }

    private void continuar() {

        if (fechando) {
            return;
        }

        fechando = true;

        Intent resultado = new Intent();
        resultado.putExtra(EXTRA_TIPO_PERFIL, tipoPerfilSelecionado);

        painelSelecaoPerfil
                .animate()
                .translationY(painelSelecaoPerfil.getHeight())
                .alpha(0.97f)
                .setDuration(220L)
                .setInterpolator(new FastOutSlowInInterpolator())
                .withEndAction(
                        () -> {
                            setResult(Activity.RESULT_OK, resultado);
                            finish();
                            overridePendingTransition(0, 0);
                        })
                .start();
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {

        if (motion != null) {
            motion.cancelAll();
        }

        super.onDestroy();
    }
}