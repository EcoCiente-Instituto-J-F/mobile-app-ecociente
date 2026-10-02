package com.example.ecociente.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecociente.R;
import com.example.ecociente.model.SindicoHistoricoColeta;
import com.example.ecociente.viewmodels.SindicoColetaViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

/** Histórico ilustrativo: avaliações ficam só na memória desta execução. */
public final class SindicoHistoricoActivity extends AppCompatActivity {
    private static final String ESTADO_FILTRO = "filtroHistorico";
    private SindicoColetaViewModel viewModel;
    private LinearLayout lista;
    private int filtro;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_sindico_historico);

        View raiz = findViewById(R.id.raizSindicoHistorico);
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (view, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(raiz);

        viewModel = new ViewModelProvider(this).get(SindicoColetaViewModel.class);
        lista = findViewById(R.id.listaSindicoHistorico);
        filtro = estadoSalvo == null ? 0 : estadoSalvo.getInt(ESTADO_FILTRO, 0);
        findViewById(R.id.voltarHistorico).setOnClickListener(view -> finish());
        findViewById(R.id.filtrarHistorico).setOnClickListener(view -> abrirFiltro());

        SindicoBottomBar barra = findViewById(R.id.barraInferiorSindico);
        barra.selecionar(SindicoBottomBar.Aba.HISTORICO);
        barra.aoTocarHistorico(null);
        barra.aoTocarHome(view -> navegar(SindicoHomeActivity.class));
        barra.aoTocarSolicitacao(view -> navegar(SindicoCooperativasActivity.class));
        barra.aoTocarUnidades(view -> navegar(SindicoUnidadesActivity.class));

        getSupportFragmentManager().setFragmentResultListener(
                SindicoAvaliarColetaDialogFragment.RESULTADO_AVALIACAO, this,
                (chave, resultado) -> {
                    mostrarHistorico();
                    Snackbar.make(raiz, "Avaliação salva somente nesta demonstração.",
                            Snackbar.LENGTH_LONG).show();
                });
        mostrarHistorico();
    }

    private void mostrarHistorico() {
        lista.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        int encontrados = 0;
        for (SindicoHistoricoColeta coleta : viewModel.getHistorico()) {
            int nota = viewModel.getAvaliacao(coleta.getId());
            if ((filtro == 1 && nota != 0) || (filtro == 2 && nota == 0)) continue;
            encontrados++;
            View cartao = inflater.inflate(R.layout.item_sindico_historico, lista, false);
            ((TextView) cartao.findViewById(R.id.nomeHistorico)).setText(coleta.getCooperativa());
            ((TextView) cartao.findViewById(R.id.dataHistorico)).setText(coleta.getData());
            ((TextView) cartao.findViewById(R.id.avaliacaoPublicaHistorico))
                    .setText(coleta.getAvaliacaoPublica());

            FrameLayout acao = cartao.findViewById(R.id.acaoAvaliarHistorico);
            TextView textoAcao = cartao.findViewById(R.id.textoAcaoAvaliarHistorico);
            if (nota > 0) {
                textoAcao.setText(nota + "/5 estrelas");
                textoAcao.setBackgroundResource(R.drawable.sindico_status_concluida);
                textoAcao.setTextColor(getColor(R.color.branco));
                acao.setContentDescription("Avaliação local: " + nota + " de 5 estrelas. Toque para alterar.");
            } else {
                acao.setContentDescription("Avaliar coleta de " + coleta.getCooperativa());
            }
            acao.setOnClickListener(view -> abrirAvaliacao(coleta));
            LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(-1, -2);
            parametros.bottomMargin = dp(12);
            lista.addView(cartao, parametros);
        }
        if (encontrados == 0) {
            TextView vazio = new TextView(this);
            vazio.setText(filtro == 1 ? "Todas as coletas de exemplo já foram avaliadas."
                    : "Nenhuma coleta avaliada neste filtro.");
            vazio.setTextColor(getColor(R.color.cinza_texto_home));
            vazio.setTextSize(14);
            vazio.setPadding(dp(8), dp(28), dp(8), dp(28));
            lista.addView(vazio);
        }
    }

    private void abrirFiltro() {
        String[] opcoes = {"Todas", "Para avaliar", "Avaliadas"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Filtrar histórico")
                .setSingleChoiceItems(opcoes, filtro, (dialogo, indice) -> {
                    filtro = indice;
                    mostrarHistorico();
                    dialogo.dismiss();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void abrirAvaliacao(SindicoHistoricoColeta coleta) {
        if (getSupportFragmentManager().findFragmentByTag("avaliarColetaSindico") == null) {
            SindicoAvaliarColetaDialogFragment.criar(coleta.getId(), coleta.getCooperativa())
                    .show(getSupportFragmentManager(), "avaliarColetaSindico");
        }
    }

    private void navegar(Class<?> destino) {
        Intent intent = new Intent(this, destino);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        estado.putInt(ESTADO_FILTRO, filtro);
        super.onSaveInstanceState(estado);
    }
}
