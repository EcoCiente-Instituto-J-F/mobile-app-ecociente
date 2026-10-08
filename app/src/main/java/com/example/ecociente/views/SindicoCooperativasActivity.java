package com.example.ecociente.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecociente.R;
import com.example.ecociente.model.SindicoCooperativa;
import com.example.ecociente.viewmodels.SindicoColetaViewModel;

import java.text.Normalizer;
import java.util.Locale;

/** Lista local de demonstração, sem busca remota ou solicitação real. */
public final class SindicoCooperativasActivity extends AppCompatActivity {
    private SindicoColetaViewModel viewModel;
    private LinearLayout lista;
    private EditText busca;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_sindico_cooperativas);

        View raiz = findViewById(R.id.raizSindicoCooperativas);
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (view, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(raiz);

        viewModel = new ViewModelProvider(this).get(SindicoColetaViewModel.class);
        lista = findViewById(R.id.listaCooperativas);
        busca = findViewById(R.id.buscaCooperativas);
        busca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                mostrarCooperativas(s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        findViewById(R.id.voltarCooperativas).setOnClickListener(view -> finish());
        SindicoBottomBar barra = findViewById(R.id.barraInferiorSindico);
        barra.selecionar(SindicoBottomBar.Aba.SOLICITACAO);
        barra.aoTocarHome(view -> voltarHome());
        barra.aoTocarSolicitacao(null);
        barra.aoTocarHistorico(view -> startActivity(
                new Intent(this, SindicoHistoricoActivity.class)));
        barra.aoTocarUnidades(view -> startActivity(
                new Intent(this, SindicoUnidadesActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mostrarCooperativas(busca.getText().toString());
    }

    private void mostrarCooperativas(String filtro) {
        lista.removeAllViews();
        String termo = normalizar(filtro);
        int encontrados = 0;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (SindicoCooperativa cooperativa : viewModel.getCooperativas()) {
            if (!normalizar(cooperativa.getNome()).contains(termo)) continue;
            encontrados++;
            View item = inflater.inflate(R.layout.item_sindico_cooperativa, lista, false);
            TextView nome = item.findViewById(R.id.nomeCooperativa);
            nome.setText(cooperativa.getNome());
            nome.setContentDescription("Ver informações de " + cooperativa.getNome());
            nome.setOnClickListener(view -> {
                Intent informacoes = new Intent(this, SindicoInformacoesCooperativaActivity.class);
                informacoes.putExtra(SindicoInformacoesCooperativaActivity.EXTRA_COOPERATIVA_ID,
                        cooperativa.getId());
                startActivity(informacoes);
            });
            ((TextView) item.findViewById(R.id.distanciaCooperativa))
                    .setText("⌖ " + cooperativa.getDistancia());
            ((TextView) item.findViewById(R.id.avaliacaoCooperativa))
                    .setText("★ " + cooperativa.getAvaliacao());
            Button acao = item.findViewById(R.id.acaoCooperativa);
            acao.setOnClickListener(view -> {
                Intent formulario = new Intent(this, SindicoSolicitacaoColetaActivity.class);
                formulario.putExtra(SindicoSolicitacaoColetaActivity.EXTRA_COOPERATIVA_ID,
                        cooperativa.getId());
                startActivity(formulario);
            });
            LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(-1, -2);
            parametros.bottomMargin = Math.round(12 * getResources().getDisplayMetrics().density);
            lista.addView(item, parametros);
        }
        if (encontrados == 0) {
            TextView vazio = new TextView(this);
            vazio.setText("Nenhuma cooperativa de exemplo encontrada.");
            vazio.setTextColor(getColor(R.color.cinza_texto_home));
            vazio.setTextSize(14);
            vazio.setPadding(8, 28, 8, 28);
            lista.addView(vazio);
        }
    }

    private String normalizar(String valor) {
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    private void voltarHome() {
        Intent home = new Intent(this, SindicoHomeActivity.class);
        home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(home);
        finish();
    }
}
