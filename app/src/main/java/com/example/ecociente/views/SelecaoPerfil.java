package com.example.ecociente.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.ecociente.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class SelecaoPerfil extends AppCompatActivity {

    public static final String EXTRA_TIPO_PERFIL = "tipoPerfil";

    private static final String TIPO_USUARIO = "usuario";
    private static final String TIPO_COOPERATIVA = "cooperativa";

    private MaterialCardView cardOpcaoUsuario;
    private MaterialCardView cardOpcaoCooperativa;

    private ImageView indicadorOpcaoUsuario;
    private ImageView indicadorOpcaoCooperativa;

    private MaterialButton botaoContinuarPerfil;

    private String tipoPerfilSelecionado = TIPO_USUARIO;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        setContentView(R.layout.activity_selecao_perfil);

        ajustarJanelaParaBaseDaTela();

        inicializarComponentes();

        configurarCliques();
    }

    private void ajustarJanelaParaBaseDaTela() {

        getWindow().setGravity(Gravity.BOTTOM);

        getWindow()
                .setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void inicializarComponentes() {

        cardOpcaoUsuario = findViewById(R.id.cardOpcaoUsuario);

        cardOpcaoCooperativa = findViewById(R.id.cardOpcaoCooperativa);

        indicadorOpcaoUsuario = findViewById(R.id.indicadorOpcaoUsuario);

        indicadorOpcaoCooperativa = findViewById(R.id.indicadorOpcaoCooperativa);

        botaoContinuarPerfil = findViewById(R.id.botaoContinuarPerfil);
    }

    private void configurarCliques() {

        cardOpcaoUsuario.setOnClickListener(view -> selecionarPerfil(TIPO_USUARIO));

        cardOpcaoCooperativa.setOnClickListener(view -> selecionarPerfil(TIPO_COOPERATIVA));

        botaoContinuarPerfil.setOnClickListener(view -> continuar());
    }

    private void selecionarPerfil(String tipo) {

        tipoPerfilSelecionado = tipo;

        boolean usuarioSelecionado = TIPO_USUARIO.equals(tipo);

        atualizarVisualOpcao(cardOpcaoUsuario, indicadorOpcaoUsuario, usuarioSelecionado);

        atualizarVisualOpcao(cardOpcaoCooperativa, indicadorOpcaoCooperativa, !usuarioSelecionado);
    }

    private void atualizarVisualOpcao(MaterialCardView card, ImageView indicador, boolean selecionado) {

        int corBorda = selecionado
                ? ContextCompat.getColor(this, R.color.verde_escuro_principal)
                : android.graphics.Color.parseColor("#D7E6E1");

        card.setStrokeColor(corBorda);

        indicador.setImageResource(
                selecionado ? R.drawable.ic_radio_selecionado : R.drawable.ic_radio_nao_selecionado);
    }

    private void continuar() {

        Intent resultado = new Intent();

        resultado.putExtra(EXTRA_TIPO_PERFIL, tipoPerfilSelecionado);

        setResult(Activity.RESULT_OK, resultado);

        finish();
    }
}
