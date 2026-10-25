package com.example.ecociente.views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.ecociente.R;
import com.example.ecociente.midias.MidiasEco;
import com.example.ecociente.midias.Midia;
import com.example.ecociente.midias.CarregadorMidias;
import com.example.ecociente.model.CursoEducativo;
import com.example.ecociente.model.FluxoEducativo;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.Motion;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

/** Tela com titulo, descricao, progresso e todas as aulas do curso. */
public class CursoEducativoActivity extends AppCompatActivity {
    private CursoEducativo curso;
    private final Motion movimento = new Motion();

    @NonNull
    public static Intent criarIntent(@NonNull Context contexto, @NonNull CursoEducativo curso) {
        return FluxoEducativo.comCurso(new Intent(contexto, CursoEducativoActivity.class), curso);
    }

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        JanelaEdgeToEdge.aplicar(this);
        setContentView(R.layout.activity_curso_educativo);
        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizCursoEducativo));
        curso = FluxoEducativo.lerCurso(getIntent());
        if (curso == null || curso.aulas.isEmpty()) {
            finish();
            return;
        }
        findViewById(R.id.voltarCursoEducativo).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tituloCursoEducativo)).setText(curso.titulo);
        ((TextView) findViewById(R.id.descricaoCursoEducativo)).setText(curso.descricao);
        CarregadorMidias.mostrar(findViewById(R.id.imagemCursoEducativo),
                MidiasEco.obter(this).iconeCurso(curso), false, R.drawable.ic_guia_verde);
        ((TextView) findViewById(R.id.indicadoresCursoEducativo)).setText(
                curso.aulas.size() + (curso.aulas.size() == 1 ? " aula" : " aulas")
                        + "   \u2022   No seu ritmo");
        MaterialButton continuar = findViewById(R.id.botaoContinuarCursoEducativo);
        continuar.setOnClickListener(v -> abrirAula(primeiraPendente()));
        Motion.pressFeedback(continuar);
        movimento.staggerIn(findViewById(R.id.cartaoCapaCursoEducativo),
                findViewById(R.id.listaAulasCursoEducativo));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (curso != null && !curso.aulas.isEmpty()) atualizarAulas();
    }

    @NonNull
    private CursoEducativo.Aula primeiraPendente() {
        for (CursoEducativo.Aula aula : curso.aulas) {
            if (!FluxoEducativo.concluido(this, curso, aula)) return aula;
        }
        return curso.aulas.get(0);
    }

    private void atualizarAulas() {
        LinearLayout lista = findViewById(R.id.listaAulasCursoEducativo);
        lista.removeAllViews();
        int concluidas = FluxoEducativo.totalAulasConcluidas(this, curso);
        ((TextView) findViewById(R.id.contagemAulasCursoEducativo)).setText(
                String.format(Locale.getDefault(), "%d/%d conclu\u00eddas", concluidas, curso.aulas.size()));
        ProgressBar progresso = findViewById(R.id.progressoCursoEducativo);
        int destino = (int) (100f * concluidas / Math.max(1, curso.aulas.size()));
        ObjectAnimator animador = ObjectAnimator.ofInt(progresso, "progress", progresso.getProgress(), destino);
        animador.setDuration(450L);
        animador.start();
        for (int i = 0; i < curso.aulas.size(); i++) {
            CursoEducativo.Aula aula = curso.aulas.get(i);
            View cartao = montarAula(aula, i);
            lista.addView(cartao);
            if (i < 7) movimento.fadeUp(cartao, 50L + i * 40L);
        }
        MaterialButton continuar = findViewById(R.id.botaoContinuarCursoEducativo);
        continuar.setText(concluidas == curso.aulas.size()
                ? "Revisar aulas do curso" : concluidas == 0
                ? "Come\u00e7ar primeira aula" : "Continuar de onde parei");
    }

    private View montarAula(CursoEducativo.Aula aula, int indice) {
        boolean finalizada = FluxoEducativo.concluido(this, curso, aula);
        MaterialCardView cartao = new MaterialCardView(this);
        cartao.setRadius(dp(17));
        cartao.setCardElevation(0f);
        cartao.setStrokeWidth(dp(1));
        cartao.setStrokeColor(Color.parseColor("#D8E5DC"));
        cartao.setCardBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(11);
        cartao.setLayoutParams(lp);

        LinearLayout linha = new LinearLayout(this);
        linha.setPadding(dp(13), dp(17), dp(13), dp(17));
        linha.setOrientation(LinearLayout.HORIZONTAL);
        linha.setGravity(Gravity.CENTER_VERTICAL);
        cartao.addView(linha);
        TextView numero = texto(finalizada ? "\u2713" : String.format(Locale.getDefault(), "%02d", indice + 1),
                16, Color.parseColor("#064E3B"), true);
        numero.setGravity(Gravity.CENTER);
        numero.setBackground(fundo(finalizada ? "#D5F1DB" : "#E7F1EC", 12));
        linha.addView(numero, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout detalhe = new LinearLayout(this);
        detalhe.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams detalheLp = new LinearLayout.LayoutParams(0, -2, 1f);
        detalheLp.leftMargin = dp(13);
        linha.addView(detalhe, detalheLp);
        TextView nome = texto(aula.titulo, 15, Color.parseColor("#1B3530"), true);
        nome.setMaxLines(2);
        detalhe.addView(nome);
        TextView legenda = texto(finalizada ? "Conclu\u00edda" : "Pronta para come\u00e7ar",
                12, Color.parseColor("#70827B"), false);
        LinearLayout.LayoutParams legendaLp = new LinearLayout.LayoutParams(-1, -2);
        legendaLp.topMargin = dp(5);
        detalhe.addView(legenda, legendaLp);
        TextView seta = texto(finalizada ? "\u2605" : "\u203a", 24,
                Color.parseColor("#064E3B"), true);
        seta.setGravity(Gravity.CENTER);
        linha.addView(seta, new LinearLayout.LayoutParams(dp(28), dp(46)));
        cartao.setContentDescription("Abrir aula " + aula.ordem + ": " + aula.titulo);
        cartao.setOnClickListener(v -> abrirAula(aula));
        Motion.pressFeedback(cartao);
        return cartao;
    }

    private void abrirAula(CursoEducativo.Aula aula) {
        startActivity(InicioConteudoEducativoActivity.criarIntent(this, curso, aula));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private TextView texto(String valor, int size, int cor, boolean bold) {
        TextView t = new TextView(this);
        t.setText(valor);
        t.setTextSize(size);
        t.setTextColor(cor);
        t.setTypeface(ResourcesCompat.getFont(this, bold ? R.font.nunito_bold : R.font.nunito_regular));
        return t;
    }

    private GradientDrawable fundo(String hex, int raio) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(Color.parseColor(hex));
        d.setCornerRadius(dp(raio));
        return d;
    }

    private int dp(float valor) { return Math.round(getResources().getDisplayMetrics().density * valor); }

    @Override
    protected void onDestroy() { movimento.cancelAll(); super.onDestroy(); }
}
