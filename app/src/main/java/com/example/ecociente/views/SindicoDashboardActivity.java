package com.example.ecociente.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ecociente.R;

/** Detalhamento visual com números fictícios para demonstração. */
public final class SindicoDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_sindico_dashboard);

        View raiz = findViewById(R.id.raizSindicoDashboard);
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (view, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(raiz);

        findViewById(R.id.voltarSindicoDashboard).setOnClickListener(view -> finish());
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

        SindicoBottomBar barra = findViewById(R.id.barraInferiorSindico);
        barra.selecionar(SindicoBottomBar.Aba.HOME);
        barra.aoTocarHome(view -> navegar(SindicoHomeActivity.class));
        barra.aoTocarSolicitacao(view -> navegar(SindicoCooperativasActivity.class));
        barra.aoTocarHistorico(view -> navegar(SindicoHistoricoActivity.class));
        barra.aoTocarUnidades(view -> navegar(SindicoUnidadesActivity.class));
    }

    private void navegar(Class<?> destino) {
        Intent intent = new Intent(this, destino);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
