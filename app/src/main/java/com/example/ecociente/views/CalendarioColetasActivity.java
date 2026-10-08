package com.example.ecociente.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecociente.R;
import com.example.ecociente.model.Coletas;
import com.example.ecociente.model.ResultadoSolicitacoes;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.ui.CalendarioMensalView;
import com.example.ecociente.ui.FormatoDataApi;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.Navegacao;
import com.example.ecociente.ui.ProximaColetaView;
import com.example.ecociente.viewmodels.CalendarioViewModel;
import com.example.ecociente.viewmodels.PerfilViewModel;
import java.util.Collections;
import java.util.List;

public class CalendarioColetasActivity extends AppCompatActivity {

    private CalendarioViewModel viewModel;

    private CalendarioMensalView calendarioMensal;
    private LinearLayout listaColetas;
    private View textoSemColetas;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        JanelaEdgeToEdge.aplicar(this);

        setContentView(R.layout.activity_calendario_coletas);

        viewModel = new ViewModelProvider(this).get(CalendarioViewModel.class);

        calendarioMensal = findViewById(R.id.calendarioMensal);
        listaColetas = findViewById(R.id.listaColetasAgendadas);
        textoSemColetas = findViewById(R.id.textoSemColetasAgendadas);

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizCalendarioColetas));

        findViewById(R.id.botaoVoltarCalendario).setOnClickListener(view -> finish());

        configurarCalendario();

        if (!viewModel.jaCarregou()) {
            viewModel.carregar();
        }
    }

    private void configurarCalendario() {

        calendarioMensal.setOnNavegacaoListener(
                new CalendarioMensalView.OnNavegacaoListener() {
                    @Override
                    public void aoMesAnterior() {
                        viewModel.mesAnterior();
                    }

                    @Override
                    public void aoProximoMes() {
                        viewModel.proximoMes();
                    }
                });

        ProximaColetaView proximaColeta = findViewById(R.id.proximaColeta);

        viewModel
                .getProxima()
                .observe(
                        this,
                        resultado ->
                                proximaColeta.exibir(resultado, viewModel::carregar, this::sair));

        viewModel.getColetasDoMes().observe(this, resultado -> atualizar());

        viewModel.getMesExibido().observe(this, mes -> atualizar());
    }

    private void atualizar() {

        int[] mes = viewModel.getMesExibido().getValue();

        ResultadoSolicitacoes resultado = viewModel.getColetasDoMes().getValue();

        List<Solicitacao> coletas =
                resultado == null ? Collections.emptyList() : resultado.getItens();

        calendarioMensal.exibir(mes[0], mes[1], Coletas.diasDoMes(coletas, mes[0], mes[1]));

        exibirLista(Coletas.ativasDoMes(coletas, mes[0], mes[1]));
    }

    private void exibirLista(List<Solicitacao> coletas) {

        listaColetas.removeAllViews();

        textoSemColetas.setVisibility(coletas.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflador = LayoutInflater.from(this);

        for (Solicitacao coleta : coletas) {

            View item = inflador.inflate(R.layout.item_coleta_agendada, listaColetas, false);

            String inicio = FormatoDataApi.horaCurta(coleta.getDataInicio());
            String fim = FormatoDataApi.horaCurta(coleta.getDataFim());

            String horario =
                    fim.isEmpty()
                            ? getString(R.string.proxima_coleta_horario_inicio, inicio)
                            : getString(R.string.proxima_coleta_horario, inicio, fim);

            ((TextView) item.findViewById(R.id.textoDataColetaAgendada))
                    .setText(
                            getString(
                                    R.string.coleta_agendada_linha,
                                    FormatoDataApi.dataExtensa(coleta.getDataInicio()),
                                    horario));

            listaColetas.addView(item);
        }
    }

    private void sair() {

        new ViewModelProvider(this).get(PerfilViewModel.class).encerrarSessao();

        Navegacao.abrirLoginLimpandoPilha(this);
    }
}
