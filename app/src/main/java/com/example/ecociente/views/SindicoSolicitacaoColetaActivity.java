package com.example.ecociente.views;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecociente.R;
import com.example.ecociente.model.SindicoCooperativa;
import com.example.ecociente.viewmodels.SindicoColetaViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/** Prévia de solicitação em formato de pop-up; não envia dados à cooperativa. */
public final class SindicoSolicitacaoColetaActivity extends AppCompatActivity {
    public static final String EXTRA_COOPERATIVA_ID = "cooperativa_id";
    private static final String ESTADO_DATA = "data_escolhida";
    private static final String ESTADO_HORA = "hora_escolhida";

    private TextView botaoData;
    private TextView botaoHora;
    private Calendar dataEscolhida;
    private int horaEscolhida = 12;
    private int minutoEscolhido = 30;
    private boolean recorrente;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        setContentView(R.layout.dialog_sindico_solicitacao_coleta);

        SindicoColetaViewModel viewModel = new ViewModelProvider(this)
                .get(SindicoColetaViewModel.class);
        int cooperativaId = getIntent().getIntExtra(EXTRA_COOPERATIVA_ID, -1);
        SindicoCooperativa cooperativa = viewModel.buscar(cooperativaId);
        if (cooperativa == null) {
            finish();
            return;
        }
        ((TextView) findViewById(R.id.nomeSolicitacao)).setText(cooperativa.getNome());
        botaoData = findViewById(R.id.dataSolicitacao);
        botaoHora = findViewById(R.id.horaSolicitacao);
        dataEscolhida = Calendar.getInstance();
        dataEscolhida.add(Calendar.DAY_OF_MONTH, 1);
        if (estadoSalvo != null) {
            dataEscolhida.setTimeInMillis(estadoSalvo.getLong(ESTADO_DATA,
                    dataEscolhida.getTimeInMillis()));
            int minutos = estadoSalvo.getInt(ESTADO_HORA, 750);
            horaEscolhida = minutos / 60;
            minutoEscolhido = minutos % 60;
            recorrente = estadoSalvo.getBoolean("recorrente", false);
        }
        atualizarDataHora();
        atualizarRecorrencia();
        botaoData.setOnClickListener(view -> escolherData());
        botaoHora.setOnClickListener(view -> escolherHora());
        findViewById(R.id.recorrenciaSim).setOnClickListener(view -> {
            recorrente = true;
            atualizarRecorrencia();
        });
        findViewById(R.id.recorrenciaNao).setOnClickListener(view -> {
            recorrente = false;
            atualizarRecorrencia();
        });
        findViewById(R.id.voltarSolicitacao).setOnClickListener(view -> finish());
    }

    private void escolherData() {
        DatePickerDialog dialogo = new DatePickerDialog(this, (seletor, ano, mes, dia) -> {
            dataEscolhida.set(ano, mes, dia);
            atualizarDataHora();
        }, dataEscolhida.get(Calendar.YEAR), dataEscolhida.get(Calendar.MONTH),
                dataEscolhida.get(Calendar.DAY_OF_MONTH));
        dialogo.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialogo.show();
    }

    private void escolherHora() {
        new TimePickerDialog(this, (seletor, hora, minuto) -> {
            horaEscolhida = hora;
            minutoEscolhido = minuto;
            atualizarDataHora();
        }, horaEscolhida, minutoEscolhido, true).show();
    }

    private void atualizarDataHora() {
        botaoData.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
                .format(dataEscolhida.getTime()));
        botaoHora.setText(String.format(Locale.forLanguageTag("pt-BR"), "%02d:%02d",
                horaEscolhida, minutoEscolhido));
    }

    private void atualizarRecorrencia() {
        Button sim = findViewById(R.id.recorrenciaSim);
        Button nao = findViewById(R.id.recorrenciaNao);
        sim.setAlpha(recorrente ? 1f : 0.7f);
        nao.setAlpha(recorrente ? 0.7f : 1f);
        sim.setContentDescription(recorrente ? "Sim, selecionado" : "Sim");
        nao.setContentDescription(recorrente ? "Não" : "Não, selecionado");
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        estado.putLong(ESTADO_DATA, dataEscolhida.getTimeInMillis());
        estado.putInt(ESTADO_HORA, horaEscolhida * 60 + minutoEscolhido);
        estado.putBoolean("recorrente", recorrente);
        super.onSaveInstanceState(estado);
    }
}
