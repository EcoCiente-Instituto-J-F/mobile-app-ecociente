package com.example.ecociente.views;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecociente.R;
import com.example.ecociente.model.CursoResumoHome;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.model.ResultadoSolicitacoes;
import com.example.ecociente.repository.CursosHomeRepository;
import com.example.ecociente.ui.AvatarPerfil;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.MenuPerfil;
import com.example.ecociente.ui.Motion;
import com.example.ecociente.ui.Navegacao;
import com.example.ecociente.ui.SemanaColetasView;
import com.example.ecociente.viewmodels.CalendarioViewModel;
import com.example.ecociente.viewmodels.PerfilViewModel;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.card.MaterialCardView;
import androidx.core.content.res.ResourcesCompat;

public class CondominioHomeActivity extends AppCompatActivity {

    private final CursosHomeRepository cursosRepository = new CursosHomeRepository();

    private PerfilViewModel perfilViewModel;
    private CalendarioViewModel calendarioViewModel;

    private final Motion motion = new Motion();

    private TextView textoSaudacao;
    private ImageView imagemPerfil;
    private SemanaColetasView semana;
    private View textoErroSemana;
    private LinearLayout listaCursosCondominio;
    private TextView textoEstadoCursos;
    private ImageView imagemAnimacaoEkoCursos;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        JanelaEdgeToEdge.aplicar(this);

        setContentView(R.layout.activity_home_condominio);

        perfilViewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        calendarioViewModel = new ViewModelProvider(this).get(CalendarioViewModel.class);

        textoSaudacao = findViewById(R.id.textoSaudacaoCondominio);
        imagemPerfil = findViewById(R.id.imagemPerfilCondominio);
        semana = findViewById(R.id.semanaColetas);
        textoErroSemana = findViewById(R.id.textoErroSemana);
        listaCursosCondominio = findViewById(R.id.listaCursosCondominio);
        textoEstadoCursos = findViewById(R.id.textoEstadoCursosCondominio);
        imagemAnimacaoEkoCursos = findViewById(R.id.animacaoEkoCursosHome);

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizHomeCondominio));

        configurarCliques();

        configurarNavegacao();

        calendarioViewModel.getColetasDaSemana().observe(this, this::exibirSemana);

        calendarioViewModel.carregarSemana();
        configurarCursosHome();
        carregarCursosHome();

        motion.staggerIn(
                findViewById(R.id.cabecalhoHomeCondominio),
                findViewById(R.id.blocoSaudacaoCondominio),
                findViewById(R.id.cardSemanaColetas),
                findViewById(R.id.cabecalhoConteudosCondominio),
                findViewById(R.id.secaoCursosCondominio));
    }

    @Override
    protected void onStart() {
        super.onStart();

        carregarPerfil();

        if (calendarioViewModel.getColetasDaSemana().getValue() != null) {
            calendarioViewModel.carregarSemana();
        }

        iniciarAnimacaoEkoCursos();
    }

    @Override
    protected void onStop() {
        pararAnimacaoEkoCursos();
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        Navegacao.marcarNaBarra(this, findViewById(R.id.barraNavegacao), ItensBarra.HOME);
    }

    @Override
    protected void onDestroy() {
        motion.cancelAll();

        super.onDestroy();
    }

    private void configurarCursosHome() {
        textoEstadoCursos.setOnClickListener(view -> carregarCursosHome());
    }

    private void carregarCursosHome() {
        textoEstadoCursos.setText(R.string.home_cursos_carregando);
        textoEstadoCursos.setVisibility(View.VISIBLE);
        listaCursosCondominio.removeAllViews();

        cursosRepository.listar(this, (sucesso, cursos) -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            listaCursosCondominio.removeAllViews();

            if (!sucesso) {
                textoEstadoCursos.setText(R.string.home_cursos_erro);
                textoEstadoCursos.setVisibility(View.VISIBLE);
                return;
            }

            if (cursos.isEmpty()) {
                textoEstadoCursos.setText(R.string.home_cursos_vazio);
                textoEstadoCursos.setVisibility(View.VISIBLE);
                return;
            }

            textoEstadoCursos.setVisibility(View.GONE);
            for (CursoResumoHome curso : cursos) {
                listaCursosCondominio.addView(criarCardCursoHome(curso));
            }
        });
    }

    private View criarCardCursoHome(CursoResumoHome curso) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(getColor(R.color.verde_fundo_card_claro));
        card.setStrokeColor(getColor(R.color.cinza_borda_clara_home));
        card.setStrokeWidth(dp(1));
        card.setRadius(dp(15));
        card.setCardElevation(dp(1));
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(dp(246), dp(174));
        cardParams.setMarginEnd(dp(12));
        card.setLayoutParams(cardParams);

        LinearLayout conteudo = new LinearLayout(this);
        conteudo.setOrientation(LinearLayout.VERTICAL);
        conteudo.setPadding(dp(15), dp(14), dp(15), dp(12));
        card.addView(conteudo);

        TextView etiqueta = textoCursoHome(
                getString(R.string.home_curso_etiqueta),
                10,
                getColor(R.color.verde_escuro_principal),
                R.font.nunito_bold);
        etiqueta.setBackgroundResource(R.drawable.fundo_selo_conteudo_home);
        etiqueta.setPadding(dp(9), dp(4), dp(9), dp(4));
        conteudo.addView(etiqueta, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView titulo = textoCursoHome(
                curso.titulo,
                16,
                getColor(R.color.verde_escuro_principal),
                R.font.nunito_bold);
        titulo.setMaxLines(2);
        titulo.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams tituloParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tituloParams.topMargin = dp(10);
        conteudo.addView(titulo, tituloParams);

        String descricao = curso.descricao.isEmpty()
                ? getString(R.string.home_curso_sem_descricao)
                : curso.descricao;
        TextView resumo = textoCursoHome(
                descricao,
                12,
                getColor(R.color.cinza_texto_home),
                R.font.nunito_regular);
        resumo.setMaxLines(3);
        resumo.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams resumoParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        resumoParams.topMargin = dp(6);
        conteudo.addView(resumo, resumoParams);

        TextView acao = textoCursoHome(
                getString(R.string.home_curso_ver_detalhes),
                11,
                getColor(R.color.verde_escuro_principal),
                R.font.nunito_bold);
        LinearLayout.LayoutParams acaoParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        acaoParams.topMargin = dp(7);
        acao.setGravity(Gravity.BOTTOM | Gravity.START);
        conteudo.addView(acao, acaoParams);

        card.setContentDescription(curso.titulo);
        card.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle(curso.titulo)
                .setMessage(descricao)
                .setPositiveButton(R.string.home_curso_fechar, null)
                .show());
        return card;
    }

    private TextView textoCursoHome(String valor, float tamanho, int cor, int fonte) {
        TextView texto = new TextView(this);
        texto.setText(valor);
        texto.setTextSize(tamanho);
        texto.setTextColor(cor);
        texto.setTypeface(ResourcesCompat.getFont(this, fonte));
        return texto;
    }

    private int dp(float valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    private void iniciarAnimacaoEkoCursos() {
        if (imagemAnimacaoEkoCursos != null
                && imagemAnimacaoEkoCursos.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) imagemAnimacaoEkoCursos.getDrawable()).start();
        }
    }

    private void pararAnimacaoEkoCursos() {
        if (imagemAnimacaoEkoCursos != null
                && imagemAnimacaoEkoCursos.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) imagemAnimacaoEkoCursos.getDrawable()).stop();
        }
    }

    private void configurarCliques() {

        imagemPerfil.setOnClickListener(
                ancora ->
                        MenuPerfil.mostrar(
                                this,
                                ancora,
                                () -> startActivity(new Intent(this, GerenciarPerfilActivity.class)),
                                this::confirmarSaida));

        findViewById(R.id.botaoCooperativasProximas)
                .setOnClickListener(view -> startActivity(new Intent(this, CooperativasProximasActivity.class)));

        findViewById(R.id.botaoVerCalendarioCompleto)
                .setOnClickListener(view -> startActivity(new Intent(this, CalendarioColetasActivity.class)));

        findViewById(R.id.cardSemanaColetas)
                .setOnClickListener(
                        view -> {
                            if (textoErroSemana.getVisibility() == View.VISIBLE) {
                                calendarioViewModel.carregarSemana();
                            }
                        });
    }

    private void configurarNavegacao() {

        BarraNavegacaoView barra = findViewById(R.id.barraNavegacao);

        barra.configurar(ItensBarra.morador());

        Navegacao.marcarNaBarra(this, barra, ItensBarra.HOME);

        barra.setOnAssistenteClickListener(view -> startActivity(new Intent(this, ChatActivity.class)));

        barra.setOnItemClickListener(
                indice -> {
                    if (indice == ItensBarra.TERCEIRO) {
                        abrirQuizzes();
                    }
                });
    }

    private void carregarPerfil() {

        FirebaseUser usuario = perfilViewModel.usuarioAtual();

        if (usuario == null) {
            return;
        }

        perfilViewModel
                .buscarPerfil(usuario.getUid())
                .observe(this, perfil -> preencherCabecalho(usuario, perfil));
    }

    private void preencherCabecalho(FirebaseUser usuario, PerfilUsuario perfil) {

        String nome = PerfilUsuario.nomeExibicao(perfil, usuario).trim();

        String primeiroNome = nome.isEmpty() ? "" : nome.split("\\s+")[0];

        textoSaudacao.setText(
                primeiroNome.isEmpty()
                        ? getString(R.string.home_usuario_padrao)
                        : getString(R.string.home_saudacao, primeiroNome));

        if (perfil != null) {
            AvatarPerfil.exibir(imagemPerfil, perfil.getFotoUrl());
        }
    }

    private void exibirSemana(@NonNull ResultadoSolicitacoes resultado) {

        boolean sucesso = resultado.getTipo() == ResultadoSolicitacoes.Tipo.SUCESSO;

        semana.exibir(sucesso ? resultado.getItens() : null);

        textoErroSemana.setVisibility(sucesso ? View.GONE : View.VISIBLE);
    }

    private void abrirQuizzes() {

        Navegacao.abrirPelaBarra(
                this,
                new Intent(this, QuizActivity.class).putExtra(QuizActivity.EXTRA_MORADOR, true),
                ItensBarra.HOME);
    }

    private void confirmarSaida() {

        MenuPerfil.confirmarSaida(
                this,
                () -> {
                    perfilViewModel.encerrarSessao();

                    Navegacao.abrirLoginLimpandoPilha(this);
                });
    }
}
