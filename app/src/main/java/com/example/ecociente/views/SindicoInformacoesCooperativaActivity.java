package com.example.ecociente.views;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecociente.R;
import com.example.ecociente.model.SindicoCooperativa;
import com.example.ecociente.viewmodels.SindicoColetaViewModel;

/** Pop-up somente de leitura com informações fictícias da cooperativa selecionada. */
public final class SindicoInformacoesCooperativaActivity extends AppCompatActivity {
    public static final String EXTRA_COOPERATIVA_ID = "cooperativa_id";

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        setContentView(R.layout.dialog_sindico_informacoes_cooperativa);

        int id = getIntent().getIntExtra(EXTRA_COOPERATIVA_ID, -1);
        SindicoCooperativa cooperativa = new ViewModelProvider(this)
                .get(SindicoColetaViewModel.class).buscar(id);
        if (cooperativa == null) {
            finish();
            return;
        }

        ImageView foto = findViewById(R.id.fotoInformacoesCooperativa);
        foto.setClipToOutline(true);
        ((TextView) findViewById(R.id.nomeInformacoesCooperativa)).setText(cooperativa.getNome());
        ((TextView) findViewById(R.id.enderecoInformacoesCooperativa))
                .setText(cooperativa.getEndereco());
        ((TextView) findViewById(R.id.cidadeInformacoesCooperativa)).setText(cooperativa.getCidade());
        ((TextView) findViewById(R.id.estadoInformacoesCooperativa)).setText(cooperativa.getEstado());
        ((TextView) findViewById(R.id.cepInformacoesCooperativa)).setText(cooperativa.getCep());
        findViewById(R.id.voltarInformacoesCooperativa).setOnClickListener(view -> finish());
    }
}
