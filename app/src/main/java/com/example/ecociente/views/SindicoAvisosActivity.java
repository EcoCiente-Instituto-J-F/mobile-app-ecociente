package com.example.ecociente.views;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecociente.R;
import com.example.ecociente.model.SindicoAviso;
import com.example.ecociente.viewmodels.SindicoViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public final class SindicoAvisosActivity extends AppCompatActivity {
    private SindicoViewModel viewModel;
    private LinearLayout lista;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_sindico_avisos);

        View raiz = findViewById(R.id.raizSindicoAvisos);
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (view, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(raiz);

        lista = findViewById(R.id.listaSindicoAvisos);
        viewModel = new ViewModelProvider(this).get(SindicoViewModel.class);
        viewModel.getAvisos().observe(this, this::mostrarAvisos);
        findViewById(R.id.botaoVoltarAvisos).setOnClickListener(view -> finish());
        findViewById(R.id.botaoNovoAviso).setOnClickListener(view -> abrirFormulario());
        getSupportFragmentManager().setFragmentResultListener("rascunhoSindico", this,
                (chave, resultado) -> Snackbar.make(raiz,
                        R.string.sindico_rascunho_salvo, Snackbar.LENGTH_LONG).show());
    }

    private void mostrarAvisos(List<SindicoAviso> avisos) {
        lista.removeAllViews();
        if (avisos == null || avisos.isEmpty()) {
            TextView vazio = new TextView(this);
            vazio.setText("Nenhum aviso por enquanto. Crie um rascunho para começar.");
            vazio.setTextColor(getColor(R.color.sindico_cinza));
            vazio.setTextSize(14);
            vazio.setPadding(12, 32, 12, 32);
            lista.addView(vazio);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (SindicoAviso aviso : avisos) {
            View cartao = inflater.inflate(R.layout.item_sindico_aviso, lista, false);
            ((TextView) cartao.findViewById(R.id.tituloSindicoAviso)).setText(aviso.getTitulo());
            ((TextView) cartao.findViewById(R.id.resumoSindicoAviso)).setText(aviso.getMensagem());
            ((TextView) cartao.findViewById(R.id.metaSindicoAviso)).setText(
                    aviso.getAutor() + "  ·  " + aviso.getMomento());
            cartao.findViewById(R.id.pontoNovoSindicoAviso).setVisibility(
                    aviso.isNovo() ? View.VISIBLE : View.GONE);
            TextView categoria = cartao.findViewById(R.id.categoriaSindicoAviso);
            categoria.setText(aviso.getCategoria());
            categoria.setBackground(criarSelo(aviso.getCategoria()));
            ((ImageView) cartao.findViewById(R.id.iconeSindicoAviso)).setImageResource(
                    "Orientação".equals(aviso.getCategoria()) || "Campanha".equals(aviso.getCategoria())
                            ? R.drawable.sindico_recycle : R.drawable.sindico_building);
            cartao.setContentDescription(aviso.getTitulo() + ". " + aviso.getCategoria()
                    + ". Toque para ler o aviso completo.");
            cartao.setOnClickListener(view -> abrirAviso(aviso));
            lista.addView(cartao);
        }
    }

    private GradientDrawable criarSelo(String categoria) {
        int cor;
        switch (categoria) {
            case "Orientação": cor = R.color.sindico_azul_claro; break;
            case "Manutenção": cor = R.color.sindico_laranja_claro; break;
            case "Campanha": cor = R.color.sindico_lilas_claro; break;
            case "Aviso geral": cor = R.color.sindico_borda; break;
            default: cor = R.color.sindico_verde_claro;
        }
        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(getColor(cor));
        fundo.setCornerRadius(24 * getResources().getDisplayMetrics().density);
        return fundo;
    }

    private void abrirAviso(SindicoAviso aviso) {
        String detalhe = aviso.getMensagem() + "\n\nDestinatário: " + aviso.getDestinatario()
                + "\nAutor: " + aviso.getAutor() + " · " + aviso.getMomento();
        if (aviso.isRascunho()) detalhe += "\n\nRascunho local, ainda não enviado.";
        new MaterialAlertDialogBuilder(this)
                .setTitle(aviso.getTitulo())
                .setMessage(detalhe)
                .setPositiveButton("Fechar", null)
                .show();
    }

    private void abrirFormulario() {
        if (getSupportFragmentManager().findFragmentByTag("criarAvisoSindico") == null) {
            new SindicoCriarAvisoDialogFragment().show(getSupportFragmentManager(),
                    "criarAvisoSindico");
        }
    }
}
