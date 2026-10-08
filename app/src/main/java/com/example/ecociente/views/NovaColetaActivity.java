package com.example.ecociente.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecociente.R;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.model.TipoLista;
import com.example.ecociente.ui.FormatoDataApi;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NovaColetaActivity extends AppCompatActivity {

    private static final String EXTRA_SOLICITACAO = "solicitacaoEditada";

    private AutoCompleteTextView campoCondominio;
    private EditText campoData;
    private EditText campoInicio;
    private EditText campoFim;
    private MaterialSwitch campoRecorrencia;

    @NonNull
    public static Intent criarIntent(@NonNull Context contexto, @Nullable Solicitacao solicitacao) {

        return new Intent(contexto, NovaColetaActivity.class).putExtra(EXTRA_SOLICITACAO, solicitacao);
    }

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        JanelaEdgeToEdge.aplicar(this);

        setContentView(R.layout.activity_nova_coleta);

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizNovaColeta));

        findViewById(R.id.botaoVoltarNovaColeta).setOnClickListener(view -> finish());

        campoCondominio = findViewById(R.id.campoCondominioColeta);
        campoData = findViewById(R.id.campoDataColeta);
        campoInicio = findViewById(R.id.campoInicioColeta);
        campoFim = findViewById(R.id.campoFimColeta);
        campoRecorrencia = findViewById(R.id.campoRecorrenciaColeta);

        campoCondominio.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nomesDosCondominios()));

        campoData.setOnClickListener(view -> escolherData());
        campoInicio.setOnClickListener(view -> escolherHora(campoInicio));
        campoFim.setOnClickListener(view -> escolherHora(campoFim));

        findViewById(R.id.botaoSalvarColeta).setOnClickListener(view -> salvar());

        preencherEdicao((Solicitacao) getIntent().getSerializableExtra(EXTRA_SOLICITACAO));
    }

    @NonNull
    private String[] nomesDosCondominios() {

        List<ItemLista> itens = TipoLista.CONDOMINIOS.itens();

        String[] nomes = new String[itens.size()];

        for (int i = 0; i < nomes.length; i++) {
            nomes[i] = itens.get(i).titulo;
        }

        return nomes;
    }

    private void preencherEdicao(@Nullable Solicitacao solicitacao) {

        if (solicitacao == null) {
            return;
        }

        ((android.widget.TextView) findViewById(R.id.tituloNovaColeta)).setText(R.string.editar_coleta);

        campoCondominio.setText(getString(R.string.solicitacao_condominio, solicitacao.getCondominioId()), false);
        campoData.setText(FormatoDataApi.data(solicitacao.getDataInicio()));
        campoInicio.setText(FormatoDataApi.hora(solicitacao.getDataInicio()));
        campoFim.setText(FormatoDataApi.hora(solicitacao.getDataFim()));
        campoRecorrencia.setChecked(solicitacao.possuiRecorrencia());
    }

    private void escolherData() {

        MaterialDatePicker<Long> seletor =
                MaterialDatePicker.Builder.datePicker()
                        .setTheme(R.style.ThemeOverlay_EcoCiente_MaterialDatePicker)
                        .setTitleText(R.string.data)
                        .build();

        seletor.addOnPositiveButtonClickListener(
                selecao -> {
                    SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                    formato.setTimeZone(TimeZone.getTimeZone("UTC"));

                    campoData.setText(formato.format(new Date(selecao)));
                });

        seletor.show(getSupportFragmentManager(), "data_coleta");
    }

    private void escolherHora(@NonNull EditText campo) {

        MaterialTimePicker seletor =
                new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).setHour(8).setMinute(0).build();

        seletor.addOnPositiveButtonClickListener(
                view ->
                        campo.setText(
                                String.format(Locale.getDefault(), "%02d:%02d", seletor.getHour(), seletor.getMinute())));

        seletor.show(getSupportFragmentManager(), "hora_coleta");
    }

    private void salvar() {

        String inicio = campoInicio.getText().toString();
        String fim = campoFim.getText().toString();

        if (campoCondominio.getText().length() == 0
                || campoData.getText().length() == 0
                || inicio.isEmpty()
                || fim.isEmpty()) {
            Toast.makeText(this, R.string.preencha_todos_os_campos, Toast.LENGTH_SHORT).show();
            return;
        }

        if (fim.compareTo(inicio) <= 0) {
            Toast.makeText(this, R.string.fim_antes_do_inicio, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, R.string.coleta_salva_exemplo, Toast.LENGTH_SHORT).show();

        finish();
    }
}
