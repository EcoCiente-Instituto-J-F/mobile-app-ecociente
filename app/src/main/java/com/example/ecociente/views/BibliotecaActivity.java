package com.example.ecociente.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecociente.R;
import com.example.ecociente.midias.MidiasEco;
import com.example.ecociente.midias.Midia;
import com.example.ecociente.midias.CarregadorMidias;
import com.example.ecociente.model.CursoEducativo;
import com.example.ecociente.model.DadosDemonstracaoEducacao;
import com.example.ecociente.repository.EducacaoRepository;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.Motion;
import com.example.ecociente.ui.Navegacao;
import com.google.android.material.card.MaterialCardView;
import androidx.core.content.res.ResourcesCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Biblioteca nativa com catálogo real ou dados locais de pré-visualização. */
public class BibliotecaActivity extends AppCompatActivity {
    public static final String EXTRA_MORADOR = "bibliotecaMorador";

    private final EducacaoRepository repositorio = new EducacaoRepository();
    private final Motion movimento = new Motion();
    private final List<CursoEducativo> cursos = new ArrayList<>();
    private final Set<String> salvos = new HashSet<>();
    private LinearLayout destaques;
    private LinearLayout listaAulas;
    private LinearLayout listaSalvos;
    private View secaoDestaques;
    private View secaoAulas;
    private View secaoSalvos;
    private TextView estado;
    private TextView botaoVerTodos;
    private EditText pesquisa;
    private HorizontalScrollView rolagemDestaques;
    private CursoEducativo selecionado;
    private boolean mostrarTodos = false;
    private boolean morador;
    private SharedPreferences preferencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JanelaEdgeToEdge.aplicar(this);
        setContentView(R.layout.activity_biblioteca);
        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizBiblioteca));
        morador = getIntent().getBooleanExtra(EXTRA_MORADOR, false);
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null && !DadosDemonstracaoEducacao.ATIVO) {
            Navegacao.abrirLoginLimpandoPilha(this);
            return;
        }
        // Os favoritos fictícios nunca são gravados na mesma preferência da conta real.
        String idUsuario = usuario == null ? "visitante" : usuario.getUid();
        String nomePreferencia = DadosDemonstracaoEducacao.ATIVO
                ? "biblioteca_demo_salvos_v2_" + idUsuario
                : "biblioteca_salvos_" + idUsuario;
        preferencias = getSharedPreferences(nomePreferencia, Context.MODE_PRIVATE);
        salvos.addAll(preferencias.getStringSet("itens", Collections.emptySet()));
        if (DadosDemonstracaoEducacao.ATIVO && !preferencias.contains("itens")) {
            salvos.addAll(Arrays.asList("curso:-104", "curso:-105", "curso:-106",
                    "curso:-107", "aula:-1001"));
            preferencias.edit().putStringSet("itens", new HashSet<>(salvos)).apply();
        }

        destaques = findViewById(R.id.listaDestaquesBiblioteca);
        listaAulas = findViewById(R.id.listaAulasBiblioteca);
        listaSalvos = findViewById(R.id.listaSalvosBiblioteca);
        secaoDestaques = findViewById(R.id.secaoDestaquesBiblioteca);
        secaoAulas = findViewById(R.id.secaoAulasBiblioteca);
        secaoSalvos = findViewById(R.id.secaoSalvosBiblioteca);
        botaoVerTodos = findViewById(R.id.botaoVerTodosSalvos);
        estado = findViewById(R.id.textoEstadoBiblioteca);
        pesquisa = findViewById(R.id.campoPesquisaBiblioteca);
        rolagemDestaques = findViewById(R.id.rolagemDestaquesBiblioteca);

        // Recurso opcional: quando livro_quiz_home.png for adicionado ao drawable-nodpi,
        // aparece automaticamente. Evita erro AAPT caso a imagem ainda nao exista.
        int idLivro = getResources().getIdentifier("livro_quiz_home", "drawable", getPackageName());
        if (idLivro != 0) {
            ((ImageView) findViewById(R.id.livroDecorativoBiblioteca)).setImageResource(idLivro);
        }
        findViewById(R.id.botaoRecarregarBiblioteca).setOnClickListener(v -> carregar());
        findViewById(R.id.botaoFecharAulasBiblioteca).setOnClickListener(v -> {
            selecionado = null;
            secaoAulas.setVisibility(View.GONE);
        });
        botaoVerTodos.setOnClickListener(v -> {
            mostrarTodos = !mostrarTodos;
            botaoVerTodos.setText(mostrarTodos ? "Ver menos" : "Ver todos");
            preencherSalvos();
        });
        pesquisa.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filtrar(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
        configurarNavegacao();
        movimento.fadeUp(findViewById(R.id.cabecalhoBiblioteca), 0);
        movimento.fadeUp(findViewById(R.id.blocoPesquisaBiblioteca), 60);
        carregar();
    }

    private void configurarNavegacao() {
        BarraNavegacaoView barra = findViewById(R.id.barraNavegacao);
        barra.configurar(morador ? ItensBarra.morador() : ItensBarra.usuario());
        Navegacao.marcarNaBarra(this, barra, ItensBarra.SEGUNDO);
        barra.setOnAssistenteClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));
        barra.setOnItemClickListener(indice -> Navegacao.irParaItemDoUsuario(
                this, indice, ItensBarra.SEGUNDO, morador));
    }

    private void carregar() {
        if (DadosDemonstracaoEducacao.ATIVO) {
            // Não faz requisições nem exibe erros/retry enquanto testa o design.
            mostrarCursos(DadosDemonstracaoEducacao.cursos());
            return;
        }
        estado.setText("Carregando cursos e aulas...");
        estado.setVisibility(View.VISIBLE);
        findViewById(R.id.botaoRecarregarBiblioteca).setVisibility(View.GONE);
        secaoDestaques.setVisibility(View.GONE);
        secaoAulas.setVisibility(View.GONE);
        secaoSalvos.setVisibility(View.GONE);
        repositorio.listarCursos(this, (sucesso, encontrados) -> {
            if (isFinishing() || isDestroyed()) return;
            if (!sucesso) {
                estado.setText(repositorio.obterUltimoErro());
                findViewById(R.id.botaoRecarregarBiblioteca).setVisibility(View.VISIBLE);
                return;
            }
            mostrarCursos(encontrados);
        });
    }

    private void mostrarCursos(@NonNull List<CursoEducativo> encontrados) {
        cursos.clear();
        cursos.addAll(encontrados);
        MidiasEco.obter(this).prepararCursos(cursos);
        findViewById(R.id.botaoRecarregarBiblioteca).setVisibility(View.GONE);
        if (cursos.isEmpty()) {
            estado.setText("Ainda não há conteúdos cadastrados na biblioteca.");
            estado.setVisibility(View.VISIBLE);
            secaoDestaques.setVisibility(View.GONE);
            secaoSalvos.setVisibility(View.GONE);
            return;
        }
        estado.setVisibility(View.GONE);
        secaoDestaques.setVisibility(View.VISIBLE);
        secaoSalvos.setVisibility(View.VISIBLE);
        secaoAulas.setVisibility(View.GONE);
        selecionado = null;
        preencherSalvos();
        filtrar(pesquisa.getText().toString());
        movimento.staggerIn(secaoDestaques, secaoSalvos);
        rolagemDestaques.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return false;
        });
    }

    private void filtrar(@NonNull String termo) {
        if (cursos.isEmpty() || destaques == null) return;
        String busca = termo.trim().toLowerCase(Locale.ROOT);
        CarregadorMidias.limparArvore(destaques);
        destaques.removeAllViews();
        int indice = 0;
        for (CursoEducativo curso : cursos) {
            if (!corresponde(curso, busca)) continue;
            // Carrossel com cinco cards no modo de demonstracao; busca no catalogo inteiro.
            if (DadosDemonstracaoEducacao.ATIVO && busca.isEmpty() && indice >= 5) break;
            View card = criarCardDestaque(curso, indice);
            destaques.addView(card);
            movimento.fadeUp(card, Math.min(indice, 5) * 50L);
            indice++;
        }
        if (rolagemDestaques != null && busca.isEmpty()) {
            rolagemDestaques.post(() -> rolagemDestaques.smoothScrollTo(0, 0));
        }
        if (indice == 0) {
            estado.setText("Nenhum curso encontrado para essa pesquisa.");
            estado.setVisibility(View.VISIBLE);
        } else {
            estado.setVisibility(View.GONE);
        }
        if (selecionado != null) mostrarAulas(selecionado, busca);
        preencherSalvos();
    }

    private boolean corresponde(CursoEducativo curso, String busca) {
        if (busca.isEmpty()) return true;
        if (curso.titulo.toLowerCase(Locale.ROOT).contains(busca)
                || curso.descricao.toLowerCase(Locale.ROOT).contains(busca)) return true;
        for (CursoEducativo.Aula aula : curso.aulas) {
            if (aula.titulo.toLowerCase(Locale.ROOT).contains(busca)) return true;
        }
        return false;
    }

    private View criarCardDestaque(CursoEducativo curso, int indice) {
        final int largura = dp(158);
        MaterialCardView card = caixa(largura, dp(259), 11);
        card.setCardElevation(dp(1));
        LinearLayout corpo = coluna(10);
        corpo.setClipChildren(false);
        card.addView(corpo, new androidx.cardview.widget.CardView.LayoutParams(-1, -1));

        FrameLayout imagemTopo = new FrameLayout(this);
        corpo.addView(imagemTopo, new LinearLayout.LayoutParams(-1, dp(112)));
        ImageView ilustra = new ImageView(this);
        CarregadorMidias.mostrar(ilustra, MidiasEco.obter(this).iconeCurso(curso), false, R.drawable.ic_guia_verde);
        ilustra.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ilustra.setContentDescription("Ilustração do conteúdo de sustentabilidade");
        FrameLayout.LayoutParams lpImagem = new FrameLayout.LayoutParams(-1, dp(91), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        imagemTopo.addView(ilustra, lpImagem);
        TextView etiqueta = texto(DadosDemonstracaoEducacao.ATIVO
                ? DadosDemonstracaoEducacao.tipoConteudo(curso.id) : "Guia",
                11, R.color.verde_escuro_principal, true);
        etiqueta.setPadding(dp(7), dp(3), dp(7), dp(3));
        etiqueta.setBackground(fundo(R.color.verde_fundo_icone_opcao, 7));
        imagemTopo.addView(etiqueta, new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.START));
        ImageView favorito = criarMarcador(salvos.contains("curso:" + curso.id));
        favorito.setContentDescription("Salvar ou remover conteúdo dos favoritos");
        favorito.setOnClickListener(v -> {
            alternarSalvo("curso:" + curso.id);
            favorito.animate().scaleX(1.20f).scaleY(1.20f).setDuration(100)
                    .withEndAction(() -> favorito.animate().scaleX(1f).scaleY(1f).setDuration(130).start()).start();
        });
        imagemTopo.addView(favorito, new FrameLayout.LayoutParams(dp(34), dp(34), Gravity.TOP | Gravity.END));

        TextView titulo = texto(curso.titulo, 13, R.color.preto_texto_home, true);
        titulo.setMaxLines(2);
        titulo.setMinLines(2);
        titulo.setEllipsize(android.text.TextUtils.TruncateAt.END);
        titulo.setLineSpacing(0, 1.03f);
        corpo.addView(titulo, new LinearLayout.LayoutParams(-1, dp(39)));
        TextView descricao = texto(curso.descricao.isEmpty() ? "Explore as aulas deste curso." : curso.descricao,
                11, R.color.cinza_texto_auxiliar, false);
        descricao.setMaxLines(3);
        descricao.setMinLines(3);
        descricao.setEllipsize(android.text.TextUtils.TruncateAt.END);
        descricao.setLineSpacing(0, 1.08f);
        LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(-1, dp(56));
        descLp.topMargin = dp(2);
        corpo.addView(descricao, descLp);
        LinearLayout rodape = new LinearLayout(this);
        rodape.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rodapeLp = new LinearLayout.LayoutParams(-1, dp(21));
        rodapeLp.topMargin = dp(4);
        corpo.addView(rodape, rodapeLp);
        String resumo = curso.aulas.isEmpty() ? "Conteudo avulso"
                : curso.aulas.size() + (curso.aulas.size() == 1 ? " aula" : " aulas");
        TextView leitura = texto(resumo, 10, R.color.cinza_texto_auxiliar, false);
        rodape.addView(leitura);

        LinearLayout.LayoutParams espacamento = new LinearLayout.LayoutParams(largura, dp(259));
        espacamento.setMargins(0, dp(2), dp(12), dp(5));
        card.setLayoutParams(espacamento);
        card.setOnClickListener(v -> {
            card.animate().scaleX(.97f).scaleY(.97f).setDuration(90).withEndAction(() -> {
                card.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                abrirCurso(curso);
            }).start();
        });
        return card;
    }

    private void mostrarAulas(CursoEducativo curso, @NonNull String filtro) {
        selecionado = curso;
        secaoAulas.setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.textoCursoSelecionado)).setText("Aulas — " + curso.titulo);
        listaAulas.removeAllViews();
        int visiveis = 0;
        for (CursoEducativo.Aula aula : curso.aulas) {
            if (!filtro.isEmpty() && !correspondeTituloOuCurso(curso, aula, filtro)) continue;
            View linha = criarLinhaAula(curso, aula);
            listaAulas.addView(linha);
            movimento.fadeUp(linha, Math.min(visiveis, 6) * 40L);
            visiveis++;
        }
        if (visiveis == 0) {
            listaAulas.addView(texto("Nenhuma aula disponível para este curso.", 13,
                    R.color.cinza_texto_auxiliar, false));
        }
        movimento.fadeUp(secaoAulas, 60);
    }

    private boolean correspondeTituloOuCurso(CursoEducativo curso, CursoEducativo.Aula aula, String busca) {
        return curso.titulo.toLowerCase(Locale.ROOT).contains(busca)
                || curso.descricao.toLowerCase(Locale.ROOT).contains(busca)
                || aula.titulo.toLowerCase(Locale.ROOT).contains(busca);
    }

    private View criarLinhaAula(CursoEducativo curso, CursoEducativo.Aula aula) {
        LinearLayout linha = new LinearLayout(this);
        linha.setGravity(Gravity.CENTER_VERTICAL);
        linha.setPadding(dp(10), dp(13), dp(8), dp(13));
        LinearLayout descricao = coluna(0);
        TextView titulo = texto(aula.titulo, 14, R.color.preto_texto_home, true);
        descricao.addView(titulo);
        TextView sub = texto("Aula " + aula.ordem + " · " + curso.titulo, 11,
                R.color.cinza_texto_auxiliar, false);
        descricao.addView(sub);
        descricao.setOnClickListener(v -> abrirAula(curso, aula));
        linha.addView(descricao, new LinearLayout.LayoutParams(0, -2, 1));
        ImageView favorito = criarMarcador(salvos.contains("aula:" + aula.id));
        favorito.setContentDescription("Salvar ou remover aula dos favoritos");
        favorito.setOnClickListener(v -> alternarSalvo("aula:" + aula.id));
        linha.addView(favorito, new LinearLayout.LayoutParams(dp(44), dp(45)));
        View separador = new View(this);
        separador.setBackgroundColor(getColor(R.color.cinza_borda_clara_home));
        LinearLayout bloco = coluna(0);
        bloco.addView(linha);
        bloco.addView(separador, new LinearLayout.LayoutParams(-1, dp(1)));
        return bloco;
    }

    private void abrirCurso(CursoEducativo curso) {
        if (curso.aulas.isEmpty()) {
            startActivity(InicioConteudoEducativoActivity.criarIntent(this, curso, null));
        } else {
            startActivity(CursoEducativoActivity.criarIntent(this, curso));
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void abrirAula(CursoEducativo curso, CursoEducativo.Aula aula) {
        startActivity(InicioConteudoEducativoActivity.criarIntent(this, curso, aula));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void preencherSalvos() {
        if (listaSalvos == null || cursos.isEmpty()) return;
        CarregadorMidias.limparArvore(listaSalvos);
        listaSalvos.removeAllViews();
        int adicionados = 0;
        int limite = mostrarTodos ? Integer.MAX_VALUE : 4;
        for (CursoEducativo curso : cursos) {
            if (salvos.contains("curso:" + curso.id) && adicionados < limite) {
                final CursoEducativo selecionavel = curso;
                String sub = DadosDemonstracaoEducacao.ATIVO
                        ? DadosDemonstracaoEducacao.tipoConteudo(curso.id)
                        : curso.aulas.isEmpty() ? "Guia" : "Curso · " + curso.aulas.size() + " aulas";
                listaSalvos.addView(criarLinhaSalva(curso.titulo, sub,
                        MidiasEco.obter(this).iconeCurso(curso),
                        () -> abrirCurso(selecionavel), () -> alternarSalvo("curso:" + curso.id)));
                adicionados++;
            }
            for (CursoEducativo.Aula aula : curso.aulas) {
                if (salvos.contains("aula:" + aula.id) && adicionados < limite) {
                    listaSalvos.addView(criarLinhaSalva(aula.titulo, curso.titulo,
                            MidiasEco.obter(this).iconeCurso(curso),
                            () -> abrirAula(curso, aula), () -> alternarSalvo("aula:" + aula.id)));
                    adicionados++;
                }
            }
        }
        if (adicionados == 0) {
            TextView vazio = texto("Seus cursos e aulas favoritos aparecerão aqui. Toque no marcador para salvar.",
                    13, R.color.cinza_texto_auxiliar, false);
            vazio.setPadding(0, dp(6), 0, dp(30));
            listaSalvos.addView(vazio);
        }
    }

    private View criarLinhaSalva(String titulo, String subtitulo, Midia imagem,
                                Runnable abrir, Runnable excluir) {
        LinearLayout linha = new LinearLayout(this);
        linha.setGravity(Gravity.CENTER_VERTICAL);
        linha.setMinimumHeight(dp(82));
        ImageView ilustra = new ImageView(this);
        CarregadorMidias.mostrar(ilustra, imagem, false, R.drawable.ic_guia_verde);
        ilustra.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ilustra.setContentDescription(null);
        ilustra.setPadding(dp(2), dp(5), dp(9), dp(5));
        linha.addView(ilustra, new LinearLayout.LayoutParams(dp(75), dp(69)));
        LinearLayout nomes = coluna(0);
        TextView principal = texto(titulo, 13, R.color.preto_texto_home, true);
        principal.setMaxLines(2);
        nomes.addView(principal);
        TextView categoria = texto(subtitulo, 10, R.color.verde_escuro_principal, false);
        categoria.setMaxLines(1);
        categoria.setEllipsize(android.text.TextUtils.TruncateAt.END);
        categoria.setPadding(dp(7), dp(3), dp(7), dp(3));
        categoria.setBackground(fundo(R.color.verde_fundo_icone_opcao, 7));
        LinearLayout.LayoutParams catLp = new LinearLayout.LayoutParams(-2, -2);
        catLp.topMargin = dp(7);
        nomes.addView(categoria, catLp);
        linha.addView(nomes, new LinearLayout.LayoutParams(0, -2, 1));
        View.OnClickListener acaoAbrir = v -> abrir.run();
        ilustra.setOnClickListener(acaoAbrir);
        nomes.setOnClickListener(acaoAbrir);
        ImageView marcador = criarMarcador(true);
        marcador.setContentDescription("Remover dos salvos");
        marcador.setOnClickListener(v -> excluir.run());
        linha.addView(marcador, new LinearLayout.LayoutParams(dp(34), dp(44)));
        TextView menu = texto("⋮", 22, R.color.cinza_texto_auxiliar, true);
        menu.setGravity(Gravity.CENTER);
        menu.setContentDescription("Abrir conteúdo salvo");
        menu.setOnClickListener(acaoAbrir);
        linha.addView(menu, new LinearLayout.LayoutParams(dp(25), dp(44)));
        View separador = new View(this);
        separador.setBackgroundColor(getColor(R.color.cinza_borda_clara_home));
        LinearLayout bloco = coluna(0);
        bloco.addView(linha);
        bloco.addView(separador, new LinearLayout.LayoutParams(-1, dp(1)));
        return bloco;
    }

    private void alternarSalvo(String chave) {
        if (!salvos.remove(chave)) salvos.add(chave);
        // Sempre gravar um novo Set: SharedPreferences não acompanha mutações na instância anterior.
        preferencias.edit().putStringSet("itens", new HashSet<>(salvos)).apply();
        filtrar(pesquisa.getText().toString());
    }

    private ImageView criarMarcador(boolean marcado) {
        ImageView imagem = new ImageView(this);
        imagem.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imagem.setPadding(dp(9), dp(9), dp(9), dp(9));
        imagem.setImageResource(marcado ? R.drawable.ic_bookmark_biblioteca_salvo
                : R.drawable.ic_bookmark_biblioteca);
        return imagem;
    }

    private MaterialCardView caixa(int largura, int altura, int raio) {
        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(dp(raio));
        card.setCardBackgroundColor(getColor(R.color.branco));
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(getColor(R.color.cinza_borda_clara_home));
        card.setCardElevation(dp(1));
        card.setLayoutParams(new LinearLayout.LayoutParams(largura, altura));
        return card;
    }

    private LinearLayout coluna(int padding) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(padding), dp(padding), dp(padding), dp(padding));
        return l;
    }

    private TextView texto(String conteudo, int tamanhoSp, int cor, boolean negrito) {
        TextView t = new TextView(this);
        t.setText(conteudo);
        t.setTextSize(tamanhoSp);
        t.setTextColor(getColor(cor));
        t.setFontFeatureSettings("kern");
        t.setTypeface(ResourcesCompat.getFont(this, negrito ? R.font.nunito_bold : R.font.nunito_regular));
        return t;
    }

    private GradientDrawable fundo(int cor, int raio) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(getColor(cor));
        d.setCornerRadius(dp(raio));
        return d;
    }

    private int dp(float valor) { return Math.round(valor * getResources().getDisplayMetrics().density); }

    @Override protected void onDestroy() {
        movimento.cancelAll();
        super.onDestroy();
    }
}
