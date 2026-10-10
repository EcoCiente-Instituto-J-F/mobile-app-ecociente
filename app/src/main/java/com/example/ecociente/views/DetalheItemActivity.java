package com.example.ecociente.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecociente.R;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.JanelaEdgeToEdge;

public class DetalheItemActivity extends AppCompatActivity {

    private static final String EXTRA_ITEM = "itemLista";

    @NonNull
    public static Intent criarIntent(@NonNull Context contexto, @NonNull ItemLista item) {

        return new Intent(contexto, DetalheItemActivity.class).putExtra(EXTRA_ITEM, item);
    }

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        JanelaEdgeToEdge.aplicar(this);

        ItemLista item = (ItemLista) getIntent().getSerializableExtra(EXTRA_ITEM);

        if (item == null || item.detalhes == null) {
            finish();
            return;
        }

        setContentView(R.layout.activity_detalhe_item);

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizDetalheItem));

        findViewById(R.id.botaoVoltarDetalheItem).setOnClickListener(view -> finish());

        ((TextView) findViewById(R.id.tituloDetalheItem)).setText(item.titulo);

        LinearLayout campos = findViewById(R.id.camposDetalheItem);

        LayoutInflater inflador = LayoutInflater.from(this);

        for (String[] campo : item.detalhes) {

            View linha = inflador.inflate(R.layout.item_campo_detalhe, campos, false);

            ((TextView) linha.findViewById(R.id.rotuloCampoDetalhe)).setText(campo[0]);
            ((TextView) linha.findViewById(R.id.valorCampoDetalhe)).setText(campo[1]);

            campos.addView(linha);
        }
    }
}
