package com.example.ecociente.views;

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

        View raiz = findViewById(R.id.raizSindicoDashboard);
        InsetsSistema.aplicarComoPadding(raiz);

        findViewById(R.id.voltarSindicoDashboard).setOnClickListener(view -> finish());
        // API: notas por quiz e desempenho por cooperativa (dashboard do sindico, sem endpoint)
        ((SindicoDashboardGraficoView) findViewById(R.id.graficoQuizzesSindico))
                .setDados(new String[0], new int[0], new int[0], 1);
        ((SindicoDashboardGraficoView) findViewById(R.id.graficoCooperativasSindico))
                .setDados(new String[0], new int[0], new int[0], 1);

        BarraNavegacaoView barra = findViewById(R.id.barraInferiorSindico);
        barra.configurar(ItensBarra.sindico());
        Navegacao.marcarNaBarra(this, barra, ItensBarra.HOME);
        barra.setOnAssistenteClickListener(
                view -> startActivity(new Intent(this, ChatActivity.class)));
        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDoSindico(this, indice, ItensBarra.NENHUM));
    }

    private void navegar(Class<?> destino) {
        Intent intent = new Intent(this, destino);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
