package com.example.ecociente.views;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecociente.R;
import com.example.ecociente.repository.PerfilApiRepository;
import com.example.ecociente.ui.AvisoFlutuante;
import com.example.ecociente.model.SindicoCooperativa;
import com.example.ecociente.viewmodels.SindicoColetaViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import com.example.ecociente.repository.CalendarioExternoRepository;
import android.view.View;

public final class SindicoSolicitacaoColetaActivity extends AppCompatActivity {
    public static final String EXTRA_COOPERATIVA_ID = "cooperativa_id";
    private static final String ESTADO_DATA = "data_escolhida";
    private static final String ESTADO_HORA = "hora_escolhida";

    private TextView botaoData;
    private TextView botaoHora;
    private Calendar dataEscolhida;
    private int horaEscolhida = 12;
    private int minutoEscolhido = 30;

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
        }
        atualizarDataHora();
        botaoData.setOnClickListener(view -> escolherData());
        botaoHora.setOnClickListener(view -> escolherHora());
        findViewById(R.id.voltarSolicitacao).setOnClickListener(view -> finish());
        findViewById(R.id.enviarSolicitacao).setOnClickListener(view -> enviar(viewModel, cooperativa));
    }

    private void enviar(SindicoColetaViewModel viewModel, SindicoCooperativa cooperativa) {
        Calendar momento = (Calendar) dataEscolhida.clone();
        momento.set(Calendar.HOUR_OF_DAY, horaEscolhida);
        momento.set(Calendar.MINUTE, minutoEscolhido);
        momento.set(Calendar.SECOND, 0);
        if (momento.getTimeInMillis() <= System.currentTimeMillis()) {
            AvisoFlutuante.mostrar(this, R.string.sindico_horario_passado);
            return;
        }
        int condominioId = PerfilApiRepository.condominioId(this);
        if (condominioId <= 0) {
            AvisoFlutuante.mostrar(this, R.string.sindico_coleta_sem_condominio);
            return;
        }
        View enviar = findViewById(R.id.enviarSolicitacao);
        enviar.setEnabled(false);
        new CalendarioExternoRepository().solicitar(this, condominioId, cooperativa.getId(),
                momento.getTimeInMillis(), (criada, mensagemErro) -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (criada == null) {
                        enviar.setEnabled(true);
                        AvisoFlutuante.mostrar(this, mensagemErro);
                        return;
                    }
                    viewModel.adicionarSolicitacao(criada);
                    startActivity(SolicitacaoDetalheActivity.criarIntentSindico(
                            this, criada.comNomeCondominio(PerfilApiRepository.condominioNomeOuVazio(this)),
                            cooperativa.getNome()));
                    finish();
                });
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

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        estado.putLong(ESTADO_DATA, dataEscolhida.getTimeInMillis());
        estado.putInt(ESTADO_HORA, horaEscolhida * 60 + minutoEscolhido);
        super.onSaveInstanceState(estado);
    }
}
