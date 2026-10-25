package com.example.ecociente.views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;
import com.example.ecociente.R;
import com.example.ecociente.model.CursoEducativo;
import com.example.ecociente.model.FluxoEducativo;
import com.example.ecociente.midias.CarregadorMidias;
import com.example.ecociente.midias.FotoPanoramicaView;
import com.example.ecociente.midias.Midia;
import com.example.ecociente.midias.MidiasEco;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.Motion;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Leitura paginada com fotos distintas e ornamentos que participam do fluxo do layout. */
public class LeituraEducativaActivity extends AppCompatActivity {
    private CursoEducativo curso;
    private CursoEducativo.Aula aula;
    private List<FluxoEducativo.Pagina> paginas;
    private MidiasEco.PlanoLeitura plano;
    private long sementeVisual;
    private int etapa;
    private boolean finalizou;
    private final Motion movimento = new Motion();
    private ProgressBar barra;
    private NestedScrollView scroll;
    private LinearLayout corpo;
    private ObjectAnimator progressoAnimado;
    private ImageView ekoTopo;

    @NonNull public static Intent criarIntent(@NonNull Context contexto, @NonNull CursoEducativo curso,
                                              @Nullable CursoEducativo.Aula aula) {
        Intent intent = FluxoEducativo.comCurso(new Intent(contexto, LeituraEducativaActivity.class), curso);
        intent.putExtra(FluxoEducativo.EXTRA_AULA_ID, aula == null ? FluxoEducativo.SEM_AULA : aula.id);
        return intent;
    }

    @Override protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        JanelaEdgeToEdge.aplicar(this);
        setContentView(R.layout.activity_leitura_educativa);
        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizLeituraEducativa));
        curso = FluxoEducativo.lerCurso(getIntent());
        if (curso == null) { finish(); return; }
        aula = FluxoEducativo.lerAula(getIntent(), curso);
        paginas = FluxoEducativo.paginas(curso, aula);
        if (paginas.isEmpty()) { finish(); return; }
        barra = findViewById(R.id.barraProgressoLeitura);
        scroll = findViewById(R.id.scrollPaginasLeitura);
        corpo = findViewById(R.id.corpoPaginaLeitura);
        ekoTopo = findViewById(R.id.ekoTopoLeitura);
        int salva = FluxoEducativo.paginaSalva(this, curso, aula);
        if (FluxoEducativo.concluido(this, curso, aula)) salva = 0;
        etapa = Math.max(0, Math.min(paginas.size() - 1, estado == null ? salva : estado.getInt("midias.etapa", salva)));
        sementeVisual = estado == null ? new Random().nextLong() : estado.getLong("midias.semente", 0L);
        plano = MidiasEco.obter(this).planejarLeitura(curso, aula, paginas, sementeVisual);
        CarregadorMidias.mostrar(ekoTopo, plano.eko, false, R.drawable.mascote_eko);
        findViewById(R.id.botaoFecharLeitura).setOnClickListener(v -> confirmarSaida());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { confirmarSaida(); }
        });
        mostrarEtapa(false);
        if (estado != null) {
            int posicao = estado.getInt("midias.scroll", 0);
            scroll.post(() -> scroll.scrollTo(0, posicao));
        }
    }

    private void mostrarEtapa(boolean animarProgresso) {
        FluxoEducativo.Pagina pagina = paginas.get(etapa);
        ((TextView) findViewById(R.id.contadorEtapasLeitura)).setText(
                String.format(Locale.getDefault(), "Etapa %d de %d", etapa + 1, paginas.size()));
        int destino = Math.round(100f * (etapa + 1) / paginas.size());
        if (progressoAnimado != null) progressoAnimado.cancel();
        if (animarProgresso) {
            progressoAnimado = ObjectAnimator.ofInt(barra, "progress", barra.getProgress(), destino);
            progressoAnimado.setDuration(400L);
            progressoAnimado.start();
        } else barra.setProgress(destino);
        FluxoEducativo.salvarPagina(this, curso, aula, etapa);
        movimento.cancelAll();
        CarregadorMidias.limparArvore(corpo);
        corpo.removeAllViews();
        scroll.scrollTo(0, 0);
        List<View> entradas = new ArrayList<>();

        TextView titulo = texto(pagina.titulo, 27, "#153F34", true);
        titulo.setLineSpacing(dp(2), 1.03f);
        View linhaTitulo = comIlustracao(titulo, plano.iconesTitulo.get(etapa), false, true);
        adicionar(linhaTitulo, 17);
        entradas.add(linhaTitulo);

        // Enquanto o Cloudinary estiver vazio, preserva a faixa panoramica (16:9)
        // usando ilustracoes locais DIFERENTES por etapa. Nao as trata como fotos reais.
        Midia foto = plano.fotos.get(etapa);
        FotoPanoramicaView cartao = new FotoPanoramicaView(this);
        cartao.setCardBackgroundColor(Color.parseColor("#F1F6F3"));
        cartao.setCardElevation(0f);
        cartao.setStrokeWidth(0);
        cartao.setRadius(dp(17));
        ImageView imagem = new ImageView(this);
        int ilustracaoLocal = ilustracaoPanoramica(etapa);
        imagem.setImageResource(ilustracaoLocal);
        imagem.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imagem.setPadding(dp(36), dp(9), dp(36), dp(9));
        imagem.setContentDescription("Ilustracao educativa do tema");
        cartao.addView(imagem, new androidx.cardview.widget.CardView.LayoutParams(-1, -1));
        adicionar(cartao, 20);
        entradas.add(cartao);
        if (foto != null) {
            imagem.setPadding(0, 0, 0, 0);
            imagem.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imagem.setContentDescription(foto.descricao.isEmpty() ? "Foto educativa do tema" : foto.descricao);
            CarregadorMidias.mostrar(imagem, foto, true, ilustracaoLocal, false, () -> {
                imagem.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imagem.setPadding(dp(36), dp(9), dp(36), dp(9));
                imagem.setContentDescription("Ilustracao educativa do tema");
            });
        }

        // Ornamento inferior fica junto ao ultimo paragrafo, sem apagar/reformular conteudo.
        String conteudo = pagina.texto;
        int separador = conteudo.lastIndexOf("\n\n");
        Midia ornamento = plano.iconesTexto.get(etapa);
        if (ornamento != null && separador >= 0) {
            TextView partePrincipal = paragrafo(conteudo.substring(0, separador));
            adicionar(partePrincipal, 8);
            entradas.add(partePrincipal);
            TextView ultimo = paragrafo(conteudo.substring(separador + 2));
            View linha = comIlustracao(ultimo, ornamento, true, false);
            adicionar(linha, 15);
            entradas.add(linha);
        } else {
            View linha = comIlustracao(paragrafo(conteudo), ornamento, true, false);
            adicionar(linha, 8);
            entradas.add(linha);
        }
        // Nao renderiza a antiga caixa "Dica do Eko". O mascote permanece no cabecalho.

        MaterialButton avancar = new MaterialButton(this);
        avancar.setText(etapa == paginas.size() - 1
                ? "Finalizar " + FluxoEducativo.tipo(curso, aula).toLowerCase(Locale.ROOT) : "Continuar  \u2192");
        avancar.setTextSize(16);
        avancar.setAllCaps(false);
        avancar.setTypeface(ResourcesCompat.getFont(this, R.font.nunito_extrabold));
        avancar.setTextColor(Color.WHITE);
        avancar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#064E3B")));
        avancar.setCornerRadius(dp(15));
        avancar.setMinHeight(dp(59));
        avancar.setPadding(dp(16), dp(13), dp(16), dp(13));
        adicionar(avancar, 28);
        avancar.setOnClickListener(v -> { avancar.setEnabled(false); avancar(); });
        Motion.pressFeedback(avancar);
        entradas.add(avancar);
        movimento.staggerIn(entradas.toArray(new View[0]));
        scroll.post(() -> scroll.scrollTo(0, 0));
    }

    /** Alterna ilustracoes LOCAIS enquanto as fotos reais nao foram cadastradas. */
    private int ilustracaoPanoramica(int indice) {
        int[] desenhos = {
                R.drawable.png_greenrecyclecontainer,
                R.drawable.png_greenpapers,
                R.drawable.png_greenplastic
        };
        return desenhos[Math.floorMod(indice, desenhos.length)];
    }

    /** Espaco real para a figura. Telas estreitas/fontes grandes usam uma coluna. */
    private View comIlustracao(TextView texto, @Nullable Midia midia, boolean esquerda, boolean titulo) {
        if (midia == null) return texto;
        LinearLayout linha = new LinearLayout(this);
        linha.setGravity(Gravity.CENTER_VERTICAL);
        boolean lado = getResources().getConfiguration().screenWidthDp >= 360
                && getResources().getConfiguration().fontScale <= 1.25f;
        linha.setOrientation(lado ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        ImageView icone = new ImageView(this);
        icone.setContentDescription(null);
        icone.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        int tamanho = dp(titulo ? 68 : 62);
        LinearLayout.LayoutParams iconeLp = new LinearLayout.LayoutParams(tamanho, tamanho);
        if (lado) {
            if (esquerda) iconeLp.setMarginEnd(dp(13)); else iconeLp.setMarginStart(dp(13));
        } else { iconeLp.gravity = Gravity.END; iconeLp.topMargin = dp(9); }
        LinearLayout.LayoutParams textoLp = lado ? new LinearLayout.LayoutParams(0, -2, 1f)
                : new LinearLayout.LayoutParams(-1, -2);
        if (lado && esquerda) linha.addView(icone, iconeLp);
        linha.addView(texto, textoLp);
        if (!lado || !esquerda) linha.addView(icone, iconeLp);
        CarregadorMidias.mostrar(icone, midia, false, 0, false, () -> {
            icone.setVisibility(View.GONE); // o peso devolve toda a largura ao texto
        });
        return linha;
    }

    private void adicionar(View view, int margemTopo) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(margemTopo);
        corpo.addView(view, lp);
    }

    private TextView paragrafo(String valor) {
        TextView t = texto(valor, 17, "#35463E", false);
        t.setLineSpacing(dp(5), 1.06f);
        return t;
    }

    private void avancar() {
        if (etapa < paginas.size() - 1) { etapa++; mostrarEtapa(true); return; }
        finalizou = true;
        FluxoEducativo.concluir(this, curso, aula);
        startActivity(ConclusaoEducativaActivity.criarIntent(this, curso, aula));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void confirmarSaida() {
        new AlertDialog.Builder(this).setTitle("Sair da leitura?")
                .setMessage("Seu progresso nesta leitura ficar\u00e1 salvo neste aparelho.")
                .setPositiveButton("Salvar e sair", (d, qual) -> finish())
                .setNegativeButton("Continuar lendo", null).show();
    }

    @Override protected void onSaveInstanceState(@NonNull Bundle estado) {
        estado.putLong("midias.semente", sementeVisual);
        estado.putInt("midias.etapa", etapa);
        estado.putInt("midias.scroll", scroll == null ? 0 : scroll.getScrollY());
        super.onSaveInstanceState(estado);
    }

    @Override protected void onPause() {
        if (!finalizou && curso != null && paginas != null && !paginas.isEmpty())
            FluxoEducativo.salvarPagina(this, curso, aula, etapa);
        super.onPause();
    }

    private TextView texto(String valor, int tamanho, String cor, boolean destaque) {
        TextView t = new TextView(this);
        t.setText(valor);
        t.setTextSize(tamanho);
        t.setTextColor(Color.parseColor(cor));
        t.setTypeface(ResourcesCompat.getFont(this, destaque ? R.font.nunito_bold : R.font.nunito_regular));
        return t;
    }
    private GradientDrawable fundo(String cor, float raio) {
        GradientDrawable d = new GradientDrawable(); d.setColor(Color.parseColor(cor)); d.setCornerRadius(raio); return d;
    }
    private int dp(float valor) { return Math.round(valor * getResources().getDisplayMetrics().density); }
    @Override protected void onDestroy() {
        if (progressoAnimado != null) progressoAnimado.cancel();
        movimento.cancelAll();
        super.onDestroy();
    }
}
