package com.example.ecociente.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ecociente.R;
import com.example.ecociente.ui.QuizPerformanceView;
import com.example.ecociente.repository.TentativaQuizRepository;
import com.example.ecociente.repository.PontuacaoRepository;
import com.example.ecociente.model.TipoLista;
import com.example.ecociente.model.PerfilAcesso;
import com.example.ecociente.model.EstadoHome;
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
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_FCM =
            "FCM_ECOCIENTE";

    private static final String TAG_ROTA =
            "RotaHome";

    private static final long FADE_SAIDA_MS =
            130L;

    private static final long FADE_ENTRADA_MS =
            170L;

    private static final long ESPERA_PERFIL_MS =
            25_000L;

    private final Motion motion =
            new Motion();

    private HomeViewModel viewModel;

    private SwipeRefreshLayout atualizacaoHome;

    private TextView textoMensagemMotivacional;

    private boolean homeCarregada;

    private boolean esperouLoginDaApi;

    private boolean tentouObterPerfil;

    private final ActivityResultLauncher<String>
            solicitacaoNotificacaoLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    permitido -> {

                        if (permitido) {

                            Log.d(
                                    TAG_FCM,
                                    "Permissão de notificações concedida"
                            );

                            buscarTokenFCM();

                        } else {

                            Log.d(
                                    TAG_FCM,
                                    "Permissão de notificações negada"
                            );

                            Toast.makeText(
                                    this,
                                    "As notificações estão desativadas.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        JanelaEdgeToEdge.aplicar(this);

        setContentView(
                R.layout.activity_main
        );

        viewModel =
                new ViewModelProvider(this)
                        .get(HomeViewModel.class);

        findViewById(
                R.id.botaoTentarNovamente
        ).setOnClickListener(
                view -> tentarNovamente()
        );

        viewModel
                .getEstado()
                .observe(
                        this,
                        this::renderizar
                );

        if (
                viewModel
                        .getEstado()
                        .getValue()
                        == null
        ) {
            viewModel.carregar();
        }
    }

    @Override
    protected void onStart() {

        boolean jaCarregada =
                homeCarregada;

        super.onStart();

        if (jaCarregada) {

            viewModel.atualizarMensagem();

            viewModel.recarregarFoto();

            carregarDesempenhoEPontos();
        }
    }

    @Override
    protected void onNewIntent(
            Intent intent
    ) {
        super.onNewIntent(intent);

        setIntent(intent);

        if (homeCarregada) {

            Navegacao.marcarNaBarra(
                    this,
                    findViewById(
                            R.id.barraNavegacao
                    ),
                    ItensBarra.HOME
            );
        }
    }

    private void tentarNovamente() {

        findViewById(
                R.id.estadoErroHome
        ).setVisibility(
                View.GONE
        );

        findViewById(
                R.id.indicadorCarregamentoHome
        ).setVisibility(
                View.VISIBLE
        );

        viewModel.carregar();
    }

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

                // No primeiro acesso da sessão, o perfil retornado pela API ainda pode estar sendo carregado.
                if (estado.getTipo() != EstadoHome.Tipo.PRONTA
                        && !esperouLoginDaApi
                        && AutenticacaoExternaRepository.loginEmAndamento()) {

                    esperouLoginDaApi = true;
                    findViewById(R.id.indicadorCarregamentoHome).setVisibility(View.VISIBLE);
                    AutenticacaoExternaRepository.aguardarLogin(ESPERA_PERFIL_MS, () -> renderizarSeAberta(estado));
                    break;
                }

                if (estado.getTipo() == EstadoHome.Tipo.CONDOMINIO
                        && !tentouObterPerfil
                        && AutenticacaoExternaRepository.perfilSalvo(this) == null) {

                    tentouObterPerfil = true;
                    findViewById(R.id.indicadorCarregamentoHome).setVisibility(View.VISIBLE);
                    AutenticacaoExternaRepository.garantirPerfil(this, () -> renderizarSeAberta(estado));
                    break;
                }

                abrirHomeDoPerfil(estado);
                break;

            default:
                break;
        }
    }

    // Os callbacks de login e perfil podem chegar depois de a tela fechar.
    private void renderizarSeAberta(@NonNull EstadoHome estado) {
        if (!isFinishing() && !isDestroyed()) {
            renderizar(estado);
        }
    }

    private void carregarHome(
            @NonNull String nomeCompleto
    ) {

        setContentView(
                R.layout.activity_home_usuario_comum
        );

        prepararNotificacoesTeste();

        homeCarregada =
                true;

        atualizacaoHome =
                findViewById(
                        R.id.atualizacaoHome
                );

        findViewById(R.id.cardAnaliseQuiz)
                .setOnClickListener(view -> startActivity(
                        TelaListaActivity.criarIntent(this, TipoLista.ANALISE_QUIZ, false)));
        findViewById(R.id.cardPontos)
                .setOnClickListener(view -> startActivity(
                        TelaListaActivity.criarIntent(this, TipoLista.PONTOS, false)));
        carregarDesempenhoEPontos();

        textoMensagemMotivacional =
                findViewById(
                        R.id.textoMensagemMotivacional
                );

        configurarAtualizacao();

        InsetsSistema.aplicarComoPadding(
                findViewById(
                        R.id.raizHomeUsuarioComum
                ),
                this::ajustarIndicadorDeAtualizacao
        );

        ((TextView) findViewById(
                R.id.textoSaudacao
        )).setText(
                getString(
                        R.string.home_saudacao,
                        primeiroNome(
                                nomeCompleto
                        )
                )
        );

        View imagemPerfil =
                findViewById(
                        R.id.imagemPerfilHome
                );

        imagemPerfil.setOnClickListener(
                ancora ->
                        MenuPerfil.mostrar(
                                this,
                                ancora,
                                () ->
                                        startActivity(
                                                new Intent(
                                                        this,
                                                        GerenciarPerfilActivity.class
                                                )
                                        ),
                                this::confirmarSaida
                        )
        );

        configurarBarra();

        configurarMicrointeracoes();

        findViewById(
                R.id.botaoNotificacoes
        ).setOnClickListener(
                view ->
                        startActivity(
                                CentralNotificacoesActivity
                                        .criarIntent(
                                                this,
                                                CentralNotificacoesActivity
                                                        .Destino
                                                        .CONDOMINIO
                                        )
                        )
        );

        observarFoto();

        observarMensagem();

        animarEntradaDaHome();

        viewModel.atualizarMensagem();
    }

    private void prepararNotificacoesTeste() {

        if (
                Build.VERSION.SDK_INT
                        >= Build.VERSION_CODES.TIRAMISU
        ) {

            int permissao =
                    ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                    );

            if (
                    permissao
                            == PackageManager.PERMISSION_GRANTED
            ) {

                Log.d(
                        TAG_FCM,
                        "Permissão de notificações já concedida"
                );

                buscarTokenFCM();

            } else {

                Log.d(
                        TAG_FCM,
                        "Solicitando permissão de notificações"
                );

                solicitacaoNotificacaoLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                );
            }

        } else {

            Log.d(
                    TAG_FCM,
                    "Android anterior ao 13. Permissão explícita não necessária."
            );

            buscarTokenFCM();
        }
    }

    private void buscarTokenFCM() {

        Log.d(
                TAG_FCM,
                "Buscando token FCM..."
        );

        FirebaseMessaging
                .getInstance()
                .getToken()
                .addOnCompleteListener(
                        task -> {

                            if (
                                    !task.isSuccessful()
                            ) {

                                Log.e(
                                        TAG_FCM,
                                        "Erro ao obter token FCM",
                                        task.getException()
                                );

                                return;
                            }

                            String token =
                                    task.getResult();

                            if (
                                    token == null
                                            || token
                                            .trim()
                                            .isEmpty()
                            ) {

                                Log.e(
                                        TAG_FCM,
                                        "Firebase retornou um token FCM vazio."
                                );

                                return;
                            }

                            Log.d(TAG_FCM, "Token FCM obtido");
                        }
                );
    }

    private void configurarAtualizacao() {

        atualizacaoHome
                .setColorSchemeColors(
                        ContextCompat.getColor(
                                this,
                                R.color.verde_escuro_principal
                        )
                );

        atualizacaoHome
                .setProgressBackgroundColorSchemeColor(
                        ContextCompat.getColor(
                                this,
                                R.color.branco
                        )
                );

        atualizacaoHome
                .setDistanceToTriggerSync(
                        Dimensoes.dpParaPx(
                                this,
                                76
                        )
                );

        atualizacaoHome
                .setOnRefreshListener(
                        viewModel::atualizarMensagem
                );
    }

    private void ajustarIndicadorDeAtualizacao(
            Insets sistema
    ) {

        /*
         * A raiz da Home já recebe
         * os insets do sistema como padding.
         *
         * Portanto não somamos sistema.top
         * novamente ao indicador.
         */
        atualizacaoHome
                .setProgressViewOffset(
                        false,
                        Dimensoes.dpParaPx(
                                this,
                                4
                        ),
                        Dimensoes.dpParaPx(
                                this,
                                48
                        )
                );
    }

    private void configurarBarra() {

        BarraNavegacaoView barra =
                findViewById(
                        R.id.barraNavegacao
                );

        barra.configurar(
                ItensBarra.usuario()
        );

        Navegacao.marcarNaBarra(
                this,
                barra,
                ItensBarra.HOME
        );

        barra.setOnAssistenteClickListener(
                view ->
                        startActivity(
                                new Intent(
                                        this,
                                        ChatActivity.class
                                )
                        )
        );

        /*
         * Mantém a implementação nova
         * centralizada da navegação,
         * trazida pela main.
         */
        barra.setOnItemClickListener(
                indice ->
                        Navegacao.irParaItemDoUsuario(
                                this,
                                indice,
                                ItensBarra.HOME,
                                false
                        )
        );
    }

    private void abrirBiblioteca() {
        startActivity(new Intent(this, BibliotecaActivity.class));
        overridePendingTransition(0, 0);
    }

    private void configurarMicrointeracoes() {

        int[] entradasBiblioteca = {
                R.id.acaoVerTodos, R.id.cardConteudoGuia, R.id.cardConteudoDica,
                R.id.cardConteudoArtigo, R.id.cardConteudoGuiaExtra,
                R.id.cardConteudoDicaExtra};
        for (int id : entradasBiblioteca) {
            View entrada = findViewById(id);
            if (entrada != null) entrada.setOnClickListener(v -> abrirBiblioteca());
        }

        int[] comFeedbackAoToque = {
                R.id.botaoNotificacoes, R.id.imagemPerfilHome, R.id.cardAnaliseQuiz,
                R.id.acaoVerTodos, R.id.cardConteudoGuia, R.id.cardConteudoDica,
                R.id.cardConteudoArtigo, R.id.cardConteudoGuiaExtra, R.id.cardConteudoDicaExtra};
        for (int id : comFeedbackAoToque) {
            Motion.pressFeedback(findViewById(id));
        }
    }

    // Gráfico de quizzes e saldo de pontos vêm da API: a home os atualiza sempre que volta.
    private void carregarDesempenhoEPontos() {

        QuizPerformanceView grafico = findViewById(R.id.graficoDesempenhoQuiz);
        android.widget.TextView saldoPontos = findViewById(R.id.textoSaldoPontos);

        grafico.mostrarCarregando();
        saldoPontos.setText(R.string.pontos_carregando);

        new TentativaQuizRepository()
                .historico(
                        this,
                        historico -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            if (historico == null) {
                                grafico.mostrarErro();
                            } else {
                                grafico.mostrar(historico);
                            }
                        });

        new PontuacaoRepository()
                .saldo(
                        this,
                        saldo -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            saldoPontos.setText(
                                    saldo == null
                                            ? getString(R.string.pontos_indisponivel)
                                            : getString(
                                                    R.string.pontos_saldo,
                                                    java.text.NumberFormat.getIntegerInstance(
                                                                    new java.util.Locale("pt", "BR"))
                                                            .format(saldo)));
                        });
    }

    private void animarEntradaDaHome() {

        // Cada item entra com um pequeno atraso em relação ao anterior.
        int[] itens = {
                R.id.cabecalhoHome, R.id.textoSaudacao, R.id.textoMensagemMotivacional,
                R.id.cardAnaliseQuiz, R.id.cabecalhoConteudos, R.id.cardConteudoGuia,
                R.id.cardConteudoDica, R.id.cardConteudoArtigo, R.id.cardConteudoGuiaExtra,
                R.id.cardConteudoDicaExtra, R.id.barraNavegacao};
        long[] atrasosMs = {0L, 45L, 80L, 125L, 185L, 235L, 275L, 315L, 355L, 395L, 145L};

        for (int i = 0; i < itens.length; i++) {
            motion.fadeUp(findViewById(itens[i]), atrasosMs[i]);
        }
    }

    private void observarFoto() {

        viewModel
                .getFotoUrl()
                .observe(
                        this,
                        url ->
                                AvatarPerfil.exibir(
                                        findViewById(
                                                R.id.imagemPerfilHome
                                        ),
                                        url
                                )
                );
    }

    private void observarMensagem() {

        viewModel
                .getMensagem()
                .observe(
                        this,
                        this::exibirMensagemComFade
                );

        viewModel
                .getAtualizandoMensagem()
                .observe(
                        this,
                        atualizando -> {

                            if (!atualizando) {

                                atualizacaoHome
                                        .setRefreshing(
                                                false
                                        );
                            }
                        }
                );
    }

    private void exibirMensagemComFade(
            @NonNull String mensagem
    ) {

        if (
                mensagem.equals(
                        textoMensagemMotivacional
                                .getText()
                                .toString()
                )
        ) {
            return;
        }

        textoMensagemMotivacional
                .animate()
                .cancel();

        textoMensagemMotivacional
                .animate()
                .alpha(0f)
                .setDuration(
                        FADE_SAIDA_MS
                )
                .withEndAction(
                        () -> {

                            textoMensagemMotivacional
                                    .setText(
                                            mensagem
                                    );

                            textoMensagemMotivacional
                                    .animate()
                                    .alpha(1f)
                                    .setDuration(
                                            FADE_ENTRADA_MS
                                    )
                                    .start();
                        }
                )
                .start();
    }

    private static PerfilAcesso.DestinoHome destinoDoFirestore(
            @NonNull EstadoHome estado
    ) {

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

    /*
     * O perfil confirmado pela API prevalece sobre
     * o do Firestore; sem ele, vale o do Firestore.
     */
    private void abrirHomeDoPerfil(
            @NonNull EstadoHome estado
    ) {

        String perfilApi =
                AutenticacaoExternaRepository
                        .perfilSalvo(this);

        PerfilAcesso.DestinoHome doFirestore =
                destinoDoFirestore(estado);

        PerfilAcesso.DestinoHome destino =
                PerfilAcesso.destinoFinal(
                        perfilApi,
                        doFirestore
                );

        Log.i(
                TAG_ROTA,
                "perfil da API="
                        + perfilApi
                        + " -> "
                        + destino
        );

        if (destino != doFirestore) {
            viewModel.sincronizarPerfil(destino);
        }

        switch (destino) {

            case SINDICO:
                abrirHome(SindicoHomeActivity.class);
                break;

            case COOPERATIVA:
                abrirHome(CooperativaHomeActivity.class);
                break;

            case CONDOMINIO:
                abrirHome(CondominioHomeActivity.class);
                break;

            default:
                com.google.firebase.auth.FirebaseUser usuario =
                        com.google.firebase.auth.FirebaseAuth
                                .getInstance()
                                .getCurrentUser();

                String nome =
                        !estado.getNome().isEmpty()
                                ? estado.getNome()
                                : usuario != null
                                        && usuario.getDisplayName() != null
                                        ? usuario.getDisplayName()
                                        : "";

                carregarHome(nome);
                break;
        }
    }

    // Abre a home do perfil sem animação de troca e fecha o roteador, para o voltar não retornar a ele.
    private void abrirHome(@NonNull Class<?> home) {

        startActivity(new Intent(this, home));

        overridePendingTransition(0, 0);

        finish();
    }

    private void confirmarSaida() {

        MenuPerfil.confirmarSaida(
                this,
                () ->
                        abrirLogin(
                                R.string.sessao_encerrada,
                                true
                        )
        );
    }

    private void abrirLogin(
            @StringRes int mensagem,
            boolean encerrarSessao
    ) {

        if (encerrarSessao) {

            viewModel.encerrarSessao();
        }

        Toast.makeText(
                this,
                mensagem,
                Toast.LENGTH_LONG
        ).show();

        Navegacao.abrirLoginLimpandoPilha(
                this
        );
    }

    @NonNull
    private String primeiroNome(
            @NonNull String nomeCompleto
    ) {

        String nome =
                nomeCompleto.trim();

        return nome.isEmpty()
                ? getString(
                R.string.home_usuario_padrao
        )
                : nome.split(
                "\\s+"
        )[0];
    }

    @Override
    protected void onDestroy() {

        motion.cancelAll();

        super.onDestroy();
    }
}