package com.example.ecociente.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.ecociente.R;
import com.example.ecociente.model.TipoLista;
import com.example.ecociente.repository.AquecimentoDasApis;
import com.example.ecociente.repository.PerfilApiRepository;
import com.example.ecociente.repository.PontuacaoRepository;
import com.example.ecociente.ui.AvisoFlutuante;
import com.example.ecociente.model.EstadoHome;
import com.example.ecociente.model.PerfilAcesso;
import com.example.ecociente.model.TipoPerfil;
import com.example.ecociente.model.TipoUsuario;
import com.example.ecociente.repository.AutenticacaoExternaRepository;
import com.example.ecociente.ui.AvatarPerfil;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.Dimensoes;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.MenuPerfil;
import com.example.ecociente.ui.Motion;
import com.example.ecociente.ui.Navegacao;
import com.example.ecociente.viewmodels.HomeViewModel;

public class MainActivity extends AppCompatActivity {

    private static final long FADE_SAIDA_MS = 130L;
    private static final long FADE_ENTRADA_MS = 170L;

    private HomeViewModel viewModel;

    private SwipeRefreshLayout atualizacaoHome;
    private TextView textoMensagemMotivacional;

    private boolean homeCarregada;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        JanelaEdgeToEdge.aplicar(this);

        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        findViewById(R.id.botaoTentarNovamente).setOnClickListener(view -> tentarNovamente());

        viewModel.getEstado().observe(this, this::renderizar);

        if (viewModel.getEstado().getValue() == null) {
            viewModel.carregar();
        }
    }

    @Override
    protected void onStart() {
        boolean jaCarregada = homeCarregada;

        super.onStart();

        if (jaCarregada) {
            viewModel.atualizarMensagem();

            viewModel.recarregarFoto();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        if (homeCarregada) {
            Navegacao.marcarNaBarra(this, findViewById(R.id.barraNavegacao), ItensBarra.HOME);
        }
    }

    private void tentarNovamente() {

        findViewById(R.id.estadoErroHome).setVisibility(View.GONE);

        findViewById(R.id.indicadorCarregamentoHome).setVisibility(View.VISIBLE);

        viewModel.carregar();
    }

    private static final long ESPERA_PERFIL_MS = 25_000;

    private boolean esperouLoginDaApi;

    private boolean tentouObterPerfil;

    private static final String TAG_ROTA = "RotaHome";

    private void renderizar(@NonNull EstadoHome estado) {

        switch (estado.getTipo()) {
            case ERRO_REDE:
                findViewById(R.id.indicadorCarregamentoHome).setVisibility(View.GONE);
                findViewById(R.id.estadoErroHome).setVisibility(View.VISIBLE);
                break;

            case SEM_LOGIN:
                abrirLogin(R.string.home_sem_login, false);
                break;

            case CADASTRO_INCOMPLETO:
                abrirLogin(R.string.home_cadastro_incompleto, true);
                break;

            case SEM_ACESSO:
                abrirLogin(R.string.home_sem_acesso, true);
                break;

            case COOPERATIVA:
            case SINDICO:
            case CONDOMINIO:
            case PRONTA:
                // Primeiro acesso da sessão: o perfil da API ainda pode estar a caminho.
                if (!esperouLoginDaApi && AutenticacaoExternaRepository.loginEmAndamento()) {
                    esperouLoginDaApi = true;

                    findViewById(R.id.indicadorCarregamentoHome).setVisibility(View.VISIBLE);

                    AutenticacaoExternaRepository.aguardarLogin(
                            ESPERA_PERFIL_MS,
                            () -> {
                                if (!isFinishing() && !isDestroyed()) {
                                    renderizar(estado);
                                }
                            });
                    break;
                }

                boolean precisaDoPerfil = estado.getTipo() == EstadoHome.Tipo.CONDOMINIO
                        || estado.getTipo() == EstadoHome.Tipo.PRONTA;

                if (precisaDoPerfil && !tentouObterPerfil && AutenticacaoExternaRepository.perfilSalvo(this) == null) {
                    tentouObterPerfil = true;

                    findViewById(R.id.indicadorCarregamentoHome).setVisibility(View.VISIBLE);

                    AutenticacaoExternaRepository.garantirPerfil(
                            this,
                            () -> {
                                if (!isFinishing() && !isDestroyed()) {
                                    renderizar(estado);
                                }
                            });
                    break;
                }

                abrirHomeDoPerfil(estado);
                break;

            default:
                break;
        }
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

    private void carregarHome(@NonNull String nomeCompleto) {

        setContentView(R.layout.activity_home_usuario_comum);

        AquecimentoDasApis.aquecer(this);
        new PerfilApiRepository().atualizarSeNecessario(this);
        carregarPontosNoCard(false);

        homeCarregada = true;

        atualizacaoHome = findViewById(R.id.atualizacaoHome);
        textoMensagemMotivacional = findViewById(R.id.textoMensagemMotivacional);

        configurarAtualizacao();

        InsetsSistema.aplicarComoPadding(
                findViewById(R.id.raizHomeUsuarioComum), this::ajustarIndicadorDeAtualizacao);

        ((TextView) findViewById(R.id.textoSaudacao))
                .setText(getString(R.string.home_saudacao, primeiroNome(nomeCompleto)));

        Motion.entrar(
                this,
                R.id.cabecalhoHome,
                R.id.textoSaudacao,
                R.id.textoMensagemMotivacional,
                R.id.cardAnaliseQuiz,
                R.id.cabecalhoConteudos);

        findViewById(R.id.imagemPerfilHome)
                .setOnClickListener(
                        ancora ->
                                MenuPerfil.mostrar(
                                        this,
                                        ancora,
                                        () -> startActivity(new Intent(this, GerenciarPerfilActivity.class)),
                                        this::confirmarSaida));

        configurarBarra();

        findViewById(R.id.botaoNotificacoes)
                .setOnClickListener(view -> startActivity(CentralNotificacoesActivity.criarIntent(this, CentralNotificacoesActivity.Destino.CONDOMINIO)));

        observarFoto();

        observarMensagem();

        viewModel.atualizarMensagem();
    }

    private void configurarAtualizacao() {

        atualizacaoHome.setColorSchemeColors(ContextCompat.getColor(this, R.color.verde_escuro_principal));

        atualizacaoHome.setProgressBackgroundColorSchemeColor(
                ContextCompat.getColor(this, R.color.branco));

        atualizacaoHome.setDistanceToTriggerSync(Dimensoes.dpParaPx(this, 76));

        atualizacaoHome.setOnRefreshListener(viewModel::atualizarMensagem);
    }

    private void ajustarIndicadorDeAtualizacao(Insets sistema) {

        atualizacaoHome.setProgressViewOffset(
                false,
                sistema.top + Dimensoes.dpParaPx(this, 4),
                sistema.top + Dimensoes.dpParaPx(this, 48));
    }

    private void configurarBarra() {

        BarraNavegacaoView barra = findViewById(R.id.barraNavegacao);

        barra.configurar(ItensBarra.usuario());

        Navegacao.marcarNaBarra(this, barra, ItensBarra.HOME);

        barra.setOnAssistenteClickListener(view -> startActivity(new Intent(this, ChatActivity.class)));

        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDoUsuario(this, indice, ItensBarra.HOME, false));
    }

    private void observarFoto() {

        viewModel
                .getFotoUrl()
                .observe(
                        this,
                        url -> AvatarPerfil.exibir(findViewById(R.id.imagemPerfilHome), url));
    }

    private void observarMensagem() {

        viewModel.getMensagem().observe(this, this::exibirMensagemComFade);

        viewModel
                .getAtualizandoMensagem()
                .observe(
                        this,
                        atualizando -> {
                            if (!atualizando) {
                                atualizacaoHome.setRefreshing(false);
                            }
                        });
    }

    private void exibirMensagemComFade(@NonNull String mensagem) {

        if (mensagem.equals(textoMensagemMotivacional.getText().toString())) {
            return;
        }

        textoMensagemMotivacional.animate().cancel();

        textoMensagemMotivacional
                .animate()
                .alpha(0f)
                .setDuration(FADE_SAIDA_MS)
                .withEndAction(
                        () -> {
                            textoMensagemMotivacional.setText(mensagem);

                            textoMensagemMotivacional
                                    .animate()
                                    .alpha(1f)
                                    .setDuration(FADE_ENTRADA_MS)
                                    .start();
                        })
                .start();
    }

    private void abrirHomeCooperativa() {

        startActivity(new Intent(this, CooperativaHomeActivity.class));

        overridePendingTransition(0, 0);

        finish();
    }

    private static PerfilAcesso.DestinoHome destinoDoFirestore(@NonNull EstadoHome estado) {

        switch (estado.getTipo()) {
            case COOPERATIVA:
                return PerfilAcesso.DestinoHome.COOPERATIVA;

            case SINDICO:
                return PerfilAcesso.DestinoHome.SINDICO;

            case CONDOMINIO:
                return PerfilAcesso.DestinoHome.CONDOMINIO;

            default:
                return PerfilAcesso.DestinoHome.USUARIO_COMUM;
        }
    }

    // O perfil que a API confirmou no login vale mais que o do Firestore.
    private void abrirHomeDoPerfil(@NonNull EstadoHome estado) {

        String perfilApi = AutenticacaoExternaRepository.perfilSalvo(this);
        PerfilAcesso.DestinoHome doFirestore = destinoDoFirestore(estado);
        PerfilAcesso.DestinoHome destino = PerfilAcesso.destinoFinal(perfilApi, doFirestore);

        Log.i(TAG_ROTA, "perfil da API=" + perfilApi + " -> " + destino);

        if (destino != doFirestore) {
            corrigirPerfilNoFirestore(destino);
        }

        switch (destino) {
            case SINDICO:
                abrirHomeSindico();
                break;

            case COOPERATIVA:
                abrirHomeCooperativa();
                break;

            case CONDOMINIO:
                abrirHomeCondominio();
                break;

            default:
                com.google.firebase.auth.FirebaseUser usuario =
                        com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                String nome = !estado.getNome().isEmpty()
                        ? estado.getNome()
                        : usuario != null && usuario.getDisplayName() != null ? usuario.getDisplayName() : "";

                carregarHome(nome);
                break;
        }
    }

    // Deixa o documento do usuário igual ao que a API confirmou, para as outras telas.
    private void corrigirPerfilNoFirestore(@NonNull PerfilAcesso.DestinoHome destino) {

        com.google.firebase.auth.FirebaseUser usuario =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            return;
        }

        boolean sindico = destino == PerfilAcesso.DestinoHome.SINDICO;
        boolean cooperativa = destino == PerfilAcesso.DestinoHome.COOPERATIVA;
        boolean morador = destino == PerfilAcesso.DestinoHome.CONDOMINIO;

        com.example.ecociente.repository.PerfilRepository repositorio =
                new com.example.ecociente.repository.PerfilRepository();

        repositorio.atualizarCampo(
                usuario.getUid(),
                "tipoPerfil",
                cooperativa ? TipoPerfil.COOPERATIVA : sindico ? TipoPerfil.SINDICO : TipoPerfil.USUARIO);
        repositorio.atualizarCampo(
                usuario.getUid(),
                "tipoUsuario",
                cooperativa
                        ? TipoUsuario.COOPERATIVA
                        : sindico ? TipoPerfil.SINDICO : morador ? TipoUsuario.MORADOR : TipoUsuario.COMUM);
        repositorio.atualizarCampo(usuario.getUid(), "possuiCodigoCondominio", morador || sindico);
    }

    private void abrirHomeSindico() {

        startActivity(new Intent(this, SindicoHomeActivity.class));

        overridePendingTransition(0, 0);

        finish();
    }

    private void abrirHomeCondominio() {

        startActivity(new Intent(this, CondominioHomeActivity.class));

        overridePendingTransition(0, 0);

        finish();
    }

    private void confirmarSaida() {

        MenuPerfil.confirmarSaida(this, () -> abrirLogin(R.string.sessao_encerrada, true));
    }

    private void abrirLogin(@StringRes int mensagem, boolean encerrarSessao) {

        if (encerrarSessao) {
            viewModel.encerrarSessao();
        }

        AvisoFlutuante.mostrar(this, mensagem);

        Navegacao.abrirLoginLimpandoPilha(this);
    }

    @NonNull
    private String primeiroNome(@NonNull String nomeCompleto) {

        String nome = nomeCompleto.trim();

        return nome.isEmpty() ? getString(R.string.home_usuario_padrao) : nome.split("\\s+")[0];
    }
}
