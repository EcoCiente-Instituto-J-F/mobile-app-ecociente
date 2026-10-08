package com.example.ecociente.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.R;
import com.example.ecociente.model.ResultadoSolicitacoes;
import com.example.ecociente.model.Solicitacao;
import java.util.Calendar;

public class ProximaColetaView extends FrameLayout {

    private static final long DIA_EM_MS = 24L * 60 * 60 * 1000;

    private final TextView data;
    private final TextView horario;
    private final TextView rotuloFaltam;
    private final TextView valorFaltam;
    private final View linhaHorario;
    private final View tipo;
    private final View caixaFaltam;
    private final View cartao;

    public ProximaColetaView(@NonNull Context contexto, @Nullable AttributeSet atributos) {
        super(contexto, atributos);

        setClipChildren(false);
        setClipToPadding(false);

        LayoutInflater.from(contexto).inflate(R.layout.view_proxima_coleta, this, true);

        data = findViewById(R.id.textoDataProximaColeta);
        horario = findViewById(R.id.textoHorarioProximaColeta);
        rotuloFaltam = findViewById(R.id.textoRotuloFaltam);
        valorFaltam = findViewById(R.id.textoValorFaltam);
        linhaHorario = findViewById(R.id.linhaHorarioProximaColeta);
        tipo = findViewById(R.id.textoTipoColeta);
        caixaFaltam = findViewById(R.id.caixaFaltamDias);
        cartao = findViewById(R.id.cartaoProximaColeta);
    }

    public void exibir(
            @NonNull ResultadoSolicitacoes resultado,
            @NonNull Runnable aoTentarNovamente,
            @NonNull Runnable aoReautenticar) {

        linhaHorario.setVisibility(GONE);
        tipo.setVisibility(GONE);
        caixaFaltam.setVisibility(GONE);
        cartao.setOnClickListener(null);
        cartao.setClickable(false);

        if (resultado.getTipo() == ResultadoSolicitacoes.Tipo.SESSAO_EXPIRADA) {
            data.setText(R.string.proxima_coleta_sessao);
            cartao.setOnClickListener(view -> aoReautenticar.run());
            return;
        }

        if (resultado.getTipo() == ResultadoSolicitacoes.Tipo.ERRO) {
            data.setText(R.string.proxima_coleta_erro);
            cartao.setOnClickListener(view -> aoTentarNovamente.run());
            return;
        }

        if (resultado.getItens().isEmpty()) {
            data.setText(R.string.proxima_coleta_nenhuma);
            return;
        }

        Solicitacao coleta = resultado.getItens().get(0);

        tipo.setVisibility(VISIBLE);

        data.setText(FormatoDataApi.dataExtensa(coleta.getDataInicio()));

        String inicio = FormatoDataApi.horaCurta(coleta.getDataInicio());
        String fim = FormatoDataApi.horaCurta(coleta.getDataFim());

        if (!inicio.isEmpty()) {
            horario.setText(
                    fim.isEmpty()
                            ? getContext().getString(R.string.proxima_coleta_horario_inicio, inicio)
                            : getContext().getString(R.string.proxima_coleta_horario, inicio, fim));
            linhaHorario.setVisibility(VISIBLE);
        }

        exibirFaltam(FormatoDataApi.calendario(coleta.getDataInicio()));
    }

    private void exibirFaltam(@Nullable Calendar inicio) {

        if (inicio == null) {
            return;
        }

        long dias =
                Math.round(
                        (inicioDoDia(inicio) - inicioDoDia(Calendar.getInstance()))
                                / (double) DIA_EM_MS);

        if (dias <= 0) {
            rotuloFaltam.setText(R.string.proxima_coleta_rotulo_coleta);
            valorFaltam.setText(R.string.proxima_coleta_hoje);

        } else if (dias == 1) {
            rotuloFaltam.setText(R.string.proxima_coleta_rotulo_coleta);
            valorFaltam.setText(R.string.proxima_coleta_amanha);

        } else {
            rotuloFaltam.setText(R.string.proxima_coleta_rotulo_faltam);
            valorFaltam.setText(getContext().getString(R.string.proxima_coleta_dias, (int) dias));
        }

        caixaFaltam.setVisibility(VISIBLE);
    }

    private long inicioDoDia(@NonNull Calendar dia) {

        Calendar copia = (Calendar) dia.clone();

        copia.set(Calendar.HOUR_OF_DAY, 0);
        copia.set(Calendar.MINUTE, 0);
        copia.set(Calendar.SECOND, 0);
        copia.set(Calendar.MILLISECOND, 0);

        return copia.getTimeInMillis();
    }
}
