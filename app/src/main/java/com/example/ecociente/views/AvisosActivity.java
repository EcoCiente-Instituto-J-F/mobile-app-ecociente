package com.example.ecociente.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import com.example.ecociente.R;
import com.example.ecociente.model.Aviso;
import com.example.ecociente.model.CategoriaAviso;
import com.example.ecociente.ui.FormatoDataApi;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.JanelaEdgeToEdge;

public class AvisosActivity extends AppCompatActivity {

    private static final String EXTRA_PODE_ENVIAR = "podeEnviarAviso";

    private LinearLayout lista;

    @NonNull
    public static Intent criarIntent(@NonNull Context contexto, boolean podeEnviar) {

        return new Intent(contexto, AvisosActivity.class).putExtra(EXTRA_PODE_ENVIAR, podeEnviar);
    }

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        JanelaEdgeToEdge.aplicar(this);

        setContentView(R.layout.activity_avisos);

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizAvisos));

        lista = findViewById(R.id.listaAvisos);

        findViewById(R.id.botaoVoltarAvisos).setOnClickListener(view -> finish());

        View botaoNovo = findViewById(R.id.botaoNovoAviso);

        if (getIntent().getBooleanExtra(EXTRA_PODE_ENVIAR, false)) {
            botaoNovo.setVisibility(View.VISIBLE);
            botaoNovo.setOnClickListener(view -> startActivity(new Intent(this, NovoAvisoActivity.class)));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        exibirAvisos();
    }

    private void exibirAvisos() {

        lista.removeAllViews();

        LayoutInflater inflador = LayoutInflater.from(this);

        for (Aviso aviso : Aviso.exemplos()) {

            View card = inflador.inflate(R.layout.item_aviso, lista, false);

            ((ImageView) card.findViewById(R.id.iconeRemetenteAviso))
                    .setImageResource(aviso.deCooperativa ? R.drawable.ic_reciclar_chat : R.drawable.ic_perfil_cooperativa);

            ((TextView) card.findViewById(R.id.textoTituloAviso)).setText(aviso.titulo);
            ((TextView) card.findViewById(R.id.textoPreviaAviso)).setText(aviso.mensagem);
            ((TextView) card.findViewById(R.id.textoRemetenteAviso)).setText(aviso.remetente);
            ((TextView) card.findViewById(R.id.textoTempoAviso))
                    .setText(FormatoDataApi.relativa(aviso.enviadoEm, System.currentTimeMillis()));

            card.findViewById(R.id.pontoAvisoNaoLido).setVisibility(aviso.isLida() ? View.INVISIBLE : View.VISIBLE);

            aplicarChip(card.findViewById(R.id.chipCategoriaAviso), aviso.categoria);

            card.setOnClickListener(view -> startActivity(AvisoDetalheActivity.criarIntent(this, aviso.id)));

            lista.addView(card);
        }
    }

    public static void aplicarChip(@NonNull TextView chip, @NonNull CategoriaAviso categoria) {

        chip.setText(categoria.rotulo);

        chip.setTextColor(ContextCompat.getColor(chip.getContext(), categoria.texto));

        ViewCompat.setBackgroundTintList(
                chip, ContextCompat.getColorStateList(chip.getContext(), categoria.fundo));
    }
}
