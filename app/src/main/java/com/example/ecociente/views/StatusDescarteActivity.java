package com.example.ecociente.views;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.ecociente.R;
import com.example.ecociente.model.PostagemFeed;
import com.example.ecociente.repository.PostagemRepository;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.Motion;

// Andamento de um descarte do próprio usuário: registrado, em análise e o resultado da validação.
public class StatusDescarteActivity extends AppCompatActivity {

    private static final String EXTRA_FOTO = "foto";
    private static final String EXTRA_CATEGORIA = "categoria";
    private static final String EXTRA_APROVADA = "aprovada";
    private static final String EXTRA_REPROVADA = "reprovada";
    private static final String EXTRA_QUANDO = "quando";

    public static Intent criarIntent(@NonNull Context contexto, @NonNull PostagemFeed postagem) {

        return new Intent(contexto, StatusDescarteActivity.class)
                .putExtra(EXTRA_FOTO, postagem.getFotoUrl())
                .putExtra(EXTRA_CATEGORIA, postagem.getCategoria())
                .putExtra(EXTRA_APROVADA, PostagemRepository.estaAprovada(postagem))
                .putExtra(EXTRA_REPROVADA, PostagemRepository.estaReprovada(postagem))
                .putExtra(EXTRA_QUANDO, postagem.getQuando());
    }

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        JanelaEdgeToEdge.aplicar(this);

        setContentView(R.layout.activity_status_descarte);

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizStatusDescarte));

        View voltar = findViewById(R.id.botaoVoltarStatusDescarte);
        voltar.setOnClickListener(view -> finish());
        Motion.pressFeedback(voltar);

        String categoria = valor(EXTRA_CATEGORIA);
        String quando = valor(EXTRA_QUANDO);

        Glide.with(this).load(valor(EXTRA_FOTO)).into((ImageView) findViewById(R.id.fotoStatusDescarte));

        TextView chip = findViewById(R.id.categoriaStatusDescarte);
        chip.setText(categoria.isEmpty() ? getString(R.string.descarte) : categoria);

        ((TextView) findViewById(R.id.quandoStatusDescarte)).setText(quando);

        montarEtapas(
                getIntent().getBooleanExtra(EXTRA_APROVADA, false),
                getIntent().getBooleanExtra(EXTRA_REPROVADA, false),
                quando);

        Motion.entrar(this, R.id.tituloStatusDescarte, R.id.subtituloStatusDescarte);
    }

    private String valor(String chave) {
        String texto = getIntent().getStringExtra(chave);
        return texto == null ? "" : texto;
    }

    private void montarEtapas(boolean aprovada, boolean reprovada, String quando) {

        LinearLayout etapas = findViewById(R.id.etapasStatusDescarte);

        boolean decidida = aprovada || reprovada;

        adicionarEtapa(etapas, R.string.etapa_registrado, quando, R.string.etapa_registrado_texto,
                R.drawable.ic_etapa_check, R.color.verde_escuro_principal, false);

        adicionarEtapa(etapas, R.string.etapa_em_analise, "",
                decidida ? R.string.etapa_em_analise_feita : R.string.etapa_em_analise_andamento,
                R.drawable.ic_etapa_check, R.color.verde_escuro_principal, false);

        if (aprovada) {
            adicionarEtapa(etapas, R.string.etapa_validado, "", R.string.etapa_validado_texto,
                    R.drawable.ic_estrela, R.color.verde_escuro_principal, true);
        } else if (reprovada) {
            adicionarEtapa(etapas, R.string.etapa_recusado, "", R.string.etapa_recusado_texto,
                    R.drawable.ic_etapa_fechar, R.color.erro_texto, true);
        } else {
            adicionarEtapa(etapas, R.string.etapa_aguardando, "", R.string.etapa_aguardando_texto,
                    R.drawable.ic_etapa_fechar, R.color.cinza_borda_desabilitada, true);
        }

        if (decidida) {
            findViewById(R.id.resultadoStatusDescarte).setVisibility(View.VISIBLE);

            ((TextView) findViewById(R.id.tituloResultadoStatusDescarte))
                    .setText(aprovada ? R.string.resultado_validado : R.string.resultado_recusado);
            ((TextView) findViewById(R.id.textoResultadoStatusDescarte))
                    .setText(aprovada ? R.string.resultado_validado_texto : R.string.resultado_recusado_texto);
        }
    }

    private void adicionarEtapa(
            LinearLayout etapas,
            @StringRes int titulo,
            String data,
            @StringRes int descricao,
            @DrawableRes int icone,
            int cor,
            boolean ultima) {

        View etapa = LayoutInflater.from(this).inflate(R.layout.item_etapa_status, etapas, false);

        FrameLayout circulo = etapa.findViewById(R.id.circuloEtapa);
        circulo.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, cor)));

        ((ImageView) etapa.findViewById(R.id.iconeEtapa)).setImageResource(icone);
        ((TextView) etapa.findViewById(R.id.tituloEtapa)).setText(titulo);

        TextView textoData = etapa.findViewById(R.id.dataEtapa);
        textoData.setText(data);
        textoData.setVisibility(data.isEmpty() ? View.GONE : View.VISIBLE);

        ((TextView) etapa.findViewById(R.id.descricaoEtapa)).setText(descricao);

        if (ultima) {
            etapa.findViewById(R.id.linhaEtapa).setVisibility(View.INVISIBLE);
        }

        etapas.addView(etapa);
    }
}
