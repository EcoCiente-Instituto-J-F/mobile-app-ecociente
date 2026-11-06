package com.example.ecociente.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecociente.R;
import com.example.ecociente.model.CursoResumoHome;
import com.example.ecociente.repository.CursosHomeRepository;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.Navegacao;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/** Aba de cursos acessível pela navegação inferior do morador. */
public class CursosActivity extends AppCompatActivity {

    public static final String EXTRA_CURSO_ID = "cursoSelecionadoId";

    private final CursosHomeRepository cursosRepository = new CursosHomeRepository();

    private LinearLayout listaCursos;
    private TextView estadoCursos;
    private int cursoSelecionadoId;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        JanelaEdgeToEdge.aplicar(this);
        setContentView(R.layout.activity_cursos_condominio);
        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizCursosCondominio));

        cursoSelecionadoId = getIntent().getIntExtra(EXTRA_CURSO_ID, 0);
        listaCursos = findViewById(R.id.listaCursosAba);
        estadoCursos = findViewById(R.id.estadoCursosAba);

        configurarNavegacao();
        estadoCursos.setOnClickListener(view -> carregarCursos());
        carregarCursos();
    }

    private void configurarNavegacao() {
        BarraNavegacaoView barra = findViewById(R.id.barraNavegacaoCursos);
        barra.configurar(ItensBarra.morador());
        Navegacao.marcarNaBarra(this, barra, ItensBarra.SEGUNDO);
        barra.setOnAssistenteClickListener(view ->
                startActivity(new Intent(this, ChatActivity.class)));

        barra.setOnItemClickListener(indice -> {
            if (indice == ItensBarra.HOME) {
                Navegacao.abrirHomePelaBarra(
                        this, CondominioHomeActivity.class, ItensBarra.SEGUNDO);
            } else if (indice == ItensBarra.TERCEIRO) {
                Intent rota = new Intent(this, QuizActivity.class)
                        .putExtra(QuizActivity.EXTRA_MORADOR, true);
                Navegacao.abrirPelaBarra(this, rota, ItensBarra.SEGUNDO);
                finish();
            }
        });
    }

    private void carregarCursos() {
        estadoCursos.setText(R.string.cursos_aba_carregando);
        estadoCursos.setVisibility(View.VISIBLE);
        listaCursos.setVisibility(View.GONE);
        listaCursos.removeAllViews();

        cursosRepository.listar(this, (sucesso, cursos) -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            listaCursos.removeAllViews();
            if (!sucesso) {
                estadoCursos.setText(R.string.cursos_aba_erro);
                estadoCursos.setVisibility(View.VISIBLE);
                return;
            }

            if (cursos.isEmpty()) {
                estadoCursos.setText(R.string.cursos_aba_vazio);
                estadoCursos.setVisibility(View.VISIBLE);
                return;
            }

            estadoCursos.setVisibility(View.GONE);
            listaCursos.setVisibility(View.VISIBLE);
            for (CursoResumoHome curso : cursos) {
                listaCursos.addView(criarCardCurso(curso));
            }
        });
    }

    private View criarCardCurso(@NonNull CursoResumoHome curso) {
        MaterialCardView card = new MaterialCardView(this);
        boolean selecionado = curso.id == cursoSelecionadoId;
        card.setCardBackgroundColor(getColor(
                selecionado ? R.color.verde_fundo_card_claro : R.color.branco));
        card.setStrokeColor(getColor(
                selecionado ? R.color.verde_escuro_principal : R.color.cinza_borda_clara_home));
        card.setStrokeWidth(dp(selecionado ? 2 : 1));
        card.setRadius(dp(15));
        card.setCardElevation(dp(1));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dp(14);
        card.setLayoutParams(cardParams);

        LinearLayout conteudo = new LinearLayout(this);
        conteudo.setOrientation(LinearLayout.VERTICAL);
        conteudo.setPadding(dp(17), dp(16), dp(17), dp(15));
        card.addView(conteudo);

        TextView titulo = texto(curso.titulo, 17, R.color.verde_escuro_principal, true);
        conteudo.addView(titulo);

        String descricao = curso.descricao == null || curso.descricao.trim().isEmpty()
                ? getString(R.string.home_curso_sem_descricao)
                : curso.descricao.trim();
        TextView resumo = texto(descricao, 13, R.color.cinza_texto_home, false);
        resumo.setMaxLines(4);
        resumo.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams resumoParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        resumoParams.topMargin = dp(7);
        conteudo.addView(resumo, resumoParams);

        TextView detalhes = texto(
                getString(R.string.cursos_aba_ver_detalhes), 12,
                R.color.verde_escuro_principal, true);
        LinearLayout.LayoutParams detalheParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        detalheParams.topMargin = dp(12);
        conteudo.addView(detalhes, detalheParams);

        card.setContentDescription(curso.titulo);
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle(curso.titulo)
                .setMessage(descricao)
                .setPositiveButton(R.string.cursos_aba_fechar, null)
                .show());
        return card;
    }

    private TextView texto(String valor, float tamanho, int cor, boolean negrito) {
        TextView texto = new TextView(this);
        texto.setText(valor);
        texto.setTextSize(tamanho);
        texto.setTextColor(getColor(cor));
        texto.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(
                this, negrito ? R.font.nunito_bold : R.font.nunito_regular));
        return texto;
    }

    private int dp(float valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}
