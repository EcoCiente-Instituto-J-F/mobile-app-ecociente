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
import com.example.ecociente.model.TipoLista;
import com.example.ecociente.model.ResultadoSolicitacoes;
import com.example.ecociente.repository.AquecimentoDasApis;
import com.example.ecociente.repository.PerfilApiRepository;
import com.example.ecociente.repository.PontuacaoRepository;
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
import com.example.ecociente.repository.RankingExternoRepository;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.util.Iniciais;

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

        AquecimentoDasApis.aquecer(this);
        new PerfilApiRepository().atualizarSeNecessario(this);
        carregarPontosNoCard(true);

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

        carregarPodio();

        motion.staggerIn(
                findViewById(R.id.cabecalhoHomeCondominio),
                findViewById(R.id.blocoSaudacaoCondominio),
                findViewById(R.id.cardSemanaColetas),
                findViewById(R.id.cardRanking),
                findViewById(R.id.cabecalhoConteudosCondominio));
    }

    private void carregarPontosNoCard(boolean morador) {

        android.widget.TextView texto = findViewById(R.id.textoSaldoPontos);

        findViewById(R.id.cardPontos)
                .setOnClickListener(
                        view -> startActivity(TelaListaActivity.criarIntent(this, TipoLista.PONTOS, morador)));

        new PontuacaoRepository()
                .saldo(
                        this,
                        saldo -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            texto.setText(
                                    saldo == null
                                            ? getString(R.string.pontos_indisponivel)
                                            : getString(
                                                    R.string.pontos_saldo,
                                                    java.text.NumberFormat.getIntegerInstance(
                                                                    new java.util.Locale("pt", "BR"))
                                                            .format(saldo)));
                        });
    }

    // Os três primeiros do ranking dos moradores, direto no cartão da home.
    private void carregarPodio() {

        new RankingExternoRepository()
                .listarMoradores(
                        this,
                        PerfilApiRepository.condominioId(this),
                        3,
                        moradores -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            android.widget.TextView estado = findViewById(R.id.textoEstadoRankingHome);
                            View podio = findViewById(R.id.podioHome);

                            if (moradores == null || moradores.isEmpty()) {
                                podio.setVisibility(View.GONE);
                                estado.setVisibility(View.VISIBLE);
                                estado.setText(
                                        moradores == null
                                                ? R.string.ranking_home_indisponivel
                                                : R.string.ranking_sem_pontuacao);
                                return;
                            }

                            estado.setVisibility(View.GONE);
                            podio.setVisibility(View.VISIBLE);

                            preencherPodio(moradores, 0, R.id.avatarPrimeiroHome, R.id.pontosPrimeiroHome);
                            preencherPodio(moradores, 1, R.id.avatarSegundoHome, R.id.pontosSegundoHome);
                            preencherPodio(moradores, 2, R.id.avatarTerceiroHome, R.id.pontosTerceiroHome);
                        });
    }

    private void preencherPodio(
            java.util.List<ItemLista> moradores, int posicao, int idAvatar, int idPontos) {

        android.widget.TextView avatar = findViewById(idAvatar);
        android.widget.TextView pontos = findViewById(idPontos);

        if (posicao >= moradores.size()) {
            avatar.setText("");
            avatar.setContentDescription(null);
            pontos.setText("");
            return;
        }

        ItemLista morador = moradores.get(posicao);

        // O título vem como "1º  Nome".
        String nome = morador.titulo.replaceFirst("^\\d+º\\s*", "");

        avatar.setText(Iniciais.de(nome));
        avatar.setContentDescription(nome + ", " + morador.destaque);
        pontos.setText(morador.destaque);
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

        findViewById(R.id.botaoNotificacoesCondominio)
                .setOnClickListener(
                        view ->
                                startActivity(
                                        CentralNotificacoesActivity.criarIntent(
                                                this, CentralNotificacoesActivity.Destino.CONDOMINIO)));

        findViewById(R.id.cardRanking)
                .setOnClickListener(
                        view -> startActivity(TelaListaActivity.criarIntent(this, TipoLista.RANKING, true)));

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
                indice -> Navegacao.irParaItemDoUsuario(this, indice, ItensBarra.HOME, true));
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

    private void confirmarSaida() {

        MenuPerfil.confirmarSaida(
                this,
                () -> {
                    perfilViewModel.encerrarSessao();

                    Navegacao.abrirLoginLimpandoPilha(this);
                });
    }
}
