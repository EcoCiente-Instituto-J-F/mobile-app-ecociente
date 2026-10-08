package com.example.ecociente.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecociente.R;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.model.ResultadoSolicitacoes;
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

public class CondominioHomeActivity extends AppCompatActivity {

    private PerfilViewModel perfilViewModel;
    private CalendarioViewModel calendarioViewModel;

    private final Motion motion = new Motion();

    private TextView textoSaudacao;
    private ImageView imagemPerfil;
    private SemanaColetasView semana;
    private View textoErroSemana;

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

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizHomeCondominio));

        configurarCliques();

        configurarNavegacao();

        calendarioViewModel.getColetasDaSemana().observe(this, this::exibirSemana);

        calendarioViewModel.carregarSemana();

        motion.staggerIn(
                findViewById(R.id.cabecalhoHomeCondominio),
                findViewById(R.id.blocoSaudacaoCondominio),
                findViewById(R.id.cardSemanaColetas),
                findViewById(R.id.cabecalhoConteudosCondominio));
    }

    @Override
    protected void onStart() {
        super.onStart();

        carregarPerfil();

        if (calendarioViewModel.getColetasDaSemana().getValue() != null) {
            calendarioViewModel.carregarSemana();
        }
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

    private void configurarCliques() {

        imagemPerfil.setOnClickListener(
                ancora ->
                        MenuPerfil.mostrar(
                                this,
                                ancora,
                                () -> startActivity(new Intent(this, GerenciarPerfilActivity.class)),
                                this::confirmarSaida));

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
