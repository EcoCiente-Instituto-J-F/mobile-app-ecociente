package com.example.ecociente.views;

import android.widget.TextView;
import com.example.ecociente.model.DadosDemonstracao;
import com.example.ecociente.ui.Motion;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecociente.R;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.Navegacao;

/** Detalhamento do dashboard; os números vêm da API de dashboard. */
public final class SindicoDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        JanelaEdgeToEdge.aplicar(this);
        setContentView(R.layout.activity_sindico_dashboard);

        Motion.entrar(
                this,
                R.id.tituloSindicoDashboard,
                R.id.graficoQuizzesSindico,
                R.id.graficoCooperativasSindico);

        View raiz = findViewById(R.id.raizSindicoDashboard);
        InsetsSistema.aplicarComoPadding(raiz);

        findViewById(R.id.voltarSindicoDashboard).setOnClickListener(view -> finish());
        if (DadosDemonstracao.ATIVO) {
            exibirDadosDeExemplo();
        } else {
            exibirSemDados();
        }

        BarraNavegacaoView barra = findViewById(R.id.barraInferiorSindico);
        barra.configurar(ItensBarra.sindico());
        Navegacao.marcarNaBarra(this, barra, ItensBarra.HOME);
        barra.setOnAssistenteClickListener(
                view -> startActivity(new Intent(this, ChatActivity.class)));
        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDoSindico(this, indice, ItensBarra.NENHUM));
    }

    // Números de exemplo, até a API de dashboard existir.
    private void exibirDadosDeExemplo() {
        ((TextView) findViewById(R.id.valorAulasConcluidas)).setText("80");
        ((TextView) findViewById(R.id.valorMoradoresNoApp)).setText("1.200");
        ((TextView) findViewById(R.id.valorPontosDoMes)).setText(R.string.sindico_pontos_mes);
        ((TextView) findViewById(R.id.textoMateriaisPercentuais))
                .setText(R.string.sindico_materiais_percentuais);

        ((SindicoDashboardGraficoView) findViewById(R.id.graficoQuizzesSindico)).setDados(
                new String[]{"Composta-\ngem", "Vidro", "Sustentabi-\nlidade", "Papel"},
                new int[]{14, 18, 14, 26},
                new int[]{R.color.verde_escuro_principal, R.color.verde_claro_ecociente,
                        R.color.cinza_borda_clara_home, R.color.rosa_ecociente}, 30);
        ((SindicoDashboardGraficoView) findViewById(R.id.graficoCooperativasSindico))
                .setDados(new String[]{"Coop\nEstrela", "Coop\nLua", "Coop\nSol", "Coop\nVerde"},
                        new int[]{3, 9, 12, 15},
                        new int[]{R.color.verde_escuro_principal, R.color.verde_claro_ecociente,
                                R.color.rosa_ecociente, R.color.amarelo_avaliacao}, 15);
    }

    private void exibirSemDados() {
        for (int id : new int[]{R.id.valorAulasConcluidas, R.id.valorMoradoresNoApp,
                R.id.valorPontosDoMes, R.id.textoMateriaisPercentuais}) {
            ((TextView) findViewById(id)).setText(R.string.sindico_sem_dado);
        }

        ((SindicoDashboardGraficoView) findViewById(R.id.graficoQuizzesSindico))
                .setDados(new String[0], new int[0], new int[0], 1);
        ((SindicoDashboardGraficoView) findViewById(R.id.graficoCooperativasSindico))
                .setDados(new String[0], new int[0], new int[0], 1);
    }

    private void navegar(Class<?> destino) {
        Intent intent = new Intent(this, destino);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
