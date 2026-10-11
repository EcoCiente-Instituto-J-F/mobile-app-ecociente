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
import com.example.ecociente.model.EstadoHome;
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

    private static final String TAG_FCM = "FCM_ECOCIENTE";

    private static final long FADE_SAIDA_MS = 130L;
    private static final long FADE_ENTRADA_MS = 170L;

    private final Motion motion = new Motion();

    private HomeViewModel viewModel;

    private SwipeRefreshLayout atualizacaoHome;
    private TextView textoMensagemMotivacional;

    private boolean homeCarregada;

    private final ActivityResultLauncher<String> solicitacaoNotificacaoLauncher =
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

        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        findViewById(R.id.botaoTentarNovamente)
                .setOnClickListener(view -> tentarNovamente());

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
            Navegacao.marcarNaBarra(
                    this,
                    findViewById(R.id.barraNavegacao),
                    ItensBarra.HOME
            );
        }
    }

    private void tentarNovamente() {
        findViewById(R.id.estadoErroHome)
                .setVisibility(View.GONE);

        findViewById(R.id.indicadorCarregamentoHome)
                .setVisibility(View.VISIBLE);

        viewModel.carregar();
    }

    private void renderizar(@NonNull EstadoHome estado) {
        switch (estado.getTipo()) {

            case ERRO_REDE:
                findViewById(R.id.indicadorCarregamentoHome)
                        .setVisibility(View.GONE);

                findViewById(R.id.estadoErroHome)
                        .setVisibility(View.VISIBLE);

                break;

            case SEM_LOGIN:
                abrirLogin(
                        R.string.home_sem_login,
                        false
                );
                break;

            case CADASTRO_INCOMPLETO:
                abrirLogin(
                        R.string.home_cadastro_incompleto,
                        true
                );
                break;

            case SEM_ACESSO:
                abrirLogin(
                        R.string.home_sem_acesso,
                        true
                );
                break;

            case COOPERATIVA:
                abrirHomeCooperativa();
                break;

            case CONDOMINIO:
                abrirHomeCondominio();
                break;

            case PRONTA:
                carregarHome(estado.getNome());
                break;

            default:
                break;
        }
    }

    private void carregarHome(@NonNull String nomeCompleto) {
        setContentView(R.layout.activity_home_usuario_comum);

        prepararNotificacoesTeste();

        homeCarregada = true;

        atualizacaoHome =
                findViewById(R.id.atualizacaoHome);

        textoMensagemMotivacional =
                findViewById(R.id.textoMensagemMotivacional);

        configurarAtualizacao();

        InsetsSistema.aplicarComoPadding(
                findViewById(R.id.raizHomeUsuarioComum),
                this::ajustarIndicadorDeAtualizacao
        );

        ((TextView) findViewById(R.id.textoSaudacao))
                .setText(
                        getString(
                                R.string.home_saudacao,
                                primeiroNome(nomeCompleto)
                        )
                );

        View imagemPerfil =
                findViewById(R.id.imagemPerfilHome);

        imagemPerfil.setOnClickListener(
                ancora ->
                        MenuPerfil.mostrar(
                                this,
                                ancora,
                                () -> startActivity(
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
        observarFoto();
        observarMensagem();
        animarEntradaDaHome();

        viewModel.atualizarMensagem();
    }

    private void prepararNotificacoesTeste() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            int permissao =
                    ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                    );

            if (permissao == PackageManager.PERMISSION_GRANTED) {

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
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {

                        Log.e(
                                TAG_FCM,
                                "Erro ao obter token FCM",
                                task.getException()
                        );

                        return;
                    }

                    String token = task.getResult();

                    if (token == null || token.trim().isEmpty()) {

                        Log.e(
                                TAG_FCM,
                                "Firebase retornou um token FCM vazio."
                        );

                        return;
                    }

                    Log.d(
                            TAG_FCM,
                            "TOKEN_FCM = " + token
                    );
                });
    }

    private void configurarAtualizacao() {

        atualizacaoHome.setColorSchemeColors(
                ContextCompat.getColor(
                        this,
                        R.color.verde_escuro_principal
                )
        );

        atualizacaoHome.setProgressBackgroundColorSchemeColor(
                ContextCompat.getColor(
                        this,
                        R.color.branco
                )
        );

        atualizacaoHome.setDistanceToTriggerSync(
                Dimensoes.dpParaPx(
                        this,
                        76
                )
        );

        atualizacaoHome.setOnRefreshListener(
                viewModel::atualizarMensagem
        );
    }

    private void ajustarIndicadorDeAtualizacao(Insets sistema) {

        /*
         * A raiz da Home já recebe os insets do sistema como padding.
         * Por isso o indicador precisa apenas do deslocamento visual interno,
         * sem somar novamente a altura da status bar.
         */

        atualizacaoHome.setProgressViewOffset(
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
                findViewById(R.id.barraNavegacao);

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

        barra.setOnItemClickListener(
                indice -> {

                    if (indice == ItensBarra.TERCEIRO) {

                        abrirQuizzes();

                    } else if (indice == ItensBarra.QUARTO) {

                        abrirPerfilPelaBarra();
                    }
                }
        );
    }

    private void configurarMicrointeracoes() {

        Motion.pressFeedback(
                findViewById(R.id.botaoNotificacoes)
        );

        Motion.pressFeedback(
                findViewById(R.id.imagemPerfilHome)
        );

        Motion.pressFeedback(
                findViewById(R.id.cardAnaliseQuiz)
        );

        Motion.pressFeedback(
                findViewById(R.id.acaoVerTodos)
        );

        Motion.pressFeedback(
                findViewById(R.id.cardConteudoGuia)
        );

        Motion.pressFeedback(
                findViewById(R.id.cardConteudoDica)
        );

        Motion.pressFeedback(
                findViewById(R.id.cardConteudoArtigo)
        );

        Motion.pressFeedback(
                findViewById(R.id.cardConteudoGuiaExtra)
        );

        Motion.pressFeedback(
                findViewById(R.id.cardConteudoDicaExtra)
        );
    }

    private void animarEntradaDaHome() {

        motion.fadeUp(
                findViewById(R.id.cabecalhoHome),
                0L
        );

        motion.fadeUp(
                findViewById(R.id.textoSaudacao),
                45L
        );

        motion.fadeUp(
                findViewById(R.id.textoMensagemMotivacional),
                80L
        );

        motion.fadeUp(
                findViewById(R.id.cardAnaliseQuiz),
                125L
        );

        motion.fadeUp(
                findViewById(R.id.cabecalhoConteudos),
                185L
        );

        motion.fadeUp(
                findViewById(R.id.cardConteudoGuia),
                235L
        );

        motion.fadeUp(
                findViewById(R.id.cardConteudoDica),
                275L
        );

        motion.fadeUp(
                findViewById(R.id.cardConteudoArtigo),
                315L
        );

        motion.fadeUp(
                findViewById(R.id.cardConteudoGuiaExtra),
                355L
        );

        motion.fadeUp(
                findViewById(R.id.cardConteudoDicaExtra),
                395L
        );

        motion.fadeUp(
                findViewById(R.id.barraNavegacao),
                145L
        );
    }

    private void observarFoto() {

        viewModel
                .getFotoUrl()
                .observe(
                        this,
                        url ->
                                AvatarPerfil.exibir(
                                        findViewById(R.id.imagemPerfilHome),
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
                                atualizacaoHome.setRefreshing(false);
                            }
                        }
                );
    }

    private void exibirMensagemComFade(
            @NonNull String mensagem
    ) {

        if (mensagem.equals(
                textoMensagemMotivacional
                        .getText()
                        .toString()
        )) {
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
                                    .setText(mensagem);

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

    private void abrirQuizzes() {

        Navegacao.abrirPelaBarra(
                this,
                new Intent(
                        this,
                        QuizActivity.class
                ),
                ItensBarra.HOME
        );
    }

    private void abrirPerfilPelaBarra() {

        Navegacao.abrirPelaBarra(
                this,
                new Intent(
                        this,
                        GerenciarPerfilActivity.class
                ),
                ItensBarra.HOME
        );
    }

    private void abrirHomeCooperativa() {

        startActivity(
                new Intent(
                        this,
                        CooperativaHomeActivity.class
                )
        );

        overridePendingTransition(
                0,
                0
        );

        finish();
    }

    private void abrirHomeCondominio() {

        startActivity(
                new Intent(
                        this,
                        CondominioHomeActivity.class
                )
        );

        overridePendingTransition(
                0,
                0
        );

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
                : nome.split("\\s+")[0];
    }

    @Override
    protected void onDestroy() {

        motion.cancelAll();

        super.onDestroy();
    }
}