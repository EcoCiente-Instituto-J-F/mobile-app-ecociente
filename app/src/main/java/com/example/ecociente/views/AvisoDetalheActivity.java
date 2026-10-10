package com.example.ecociente.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecociente.R;
import com.example.ecociente.model.Aviso;
import com.example.ecociente.ui.FormatoDataApi;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.JanelaEdgeToEdge;

public class AvisoDetalheActivity extends AppCompatActivity {

    private static final String EXTRA_ID = "avisoId";

    @NonNull
    public static Intent criarIntent(@NonNull Context contexto, int idAviso) {

        return new Intent(contexto, AvisoDetalheActivity.class).putExtra(EXTRA_ID, idAviso);
    }

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        JanelaEdgeToEdge.aplicar(this);

        Aviso aviso = Aviso.porId(getIntent().getIntExtra(EXTRA_ID, 0));

        if (aviso == null) {
            finish();
            return;
        }

        aviso.marcarComoLida();

        setContentView(R.layout.activity_aviso_detalhe);

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizAvisoDetalhe));

        findViewById(R.id.botaoVoltarAvisoDetalhe).setOnClickListener(view -> finish());

        AvisosActivity.aplicarChip(findViewById(R.id.chipAvisoDetalhe), aviso.categoria);

        ((TextView) findViewById(R.id.tituloAvisoDetalhe)).setText(aviso.titulo);

        ((TextView) findViewById(R.id.origemAvisoDetalhe))
                .setText(
                        getString(
                                R.string.aviso_origem,
                                aviso.remetente,
                                FormatoDataApi.relativa(aviso.enviadoEm, System.currentTimeMillis())));

        ((TextView) findViewById(R.id.mensagemAvisoDetalhe)).setText(aviso.mensagem);
    }
}
