package com.example.ecociente.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ecociente.R;

import com.example.ecociente.model.PerfilAcesso;

import com.example.ecociente.repository.NotificacaoMotivacionalRepository;

import com.google.android.material.button.MaterialButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private final FirebaseAuth autenticacao =
            FirebaseAuth.getInstance();

    private final FirebaseFirestore bancoFirestore =
            FirebaseFirestore.getInstance();

    private final NotificacaoMotivacionalRepository repositorioNotificacao =
            new NotificacaoMotivacionalRepository();


    private TextView textoMensagemMotivacional;

    private SwipeRefreshLayout atualizacaoHome;


    private boolean homeCarregada =
            false;

    private boolean atividadeVisivel =
            false;


    @Override
    protected void onCreate(
            Bundle estadoSalvo
    ) {

        super.onCreate(
                estadoSalvo
        );


        /*
         * Mantemos o edge-to-edge que já está
         * funcionando na Home.
         */
        WindowCompat.setDecorFitsSystemWindows(
                getWindow(),
                false
        );


        getWindow().setStatusBarColor(
                Color.TRANSPARENT
        );


        getWindow().setNavigationBarColor(
                Color.TRANSPARENT
        );


        if (
                Build.VERSION.SDK_INT
                        >=
                        Build.VERSION_CODES.Q
        ) {

            getWindow()
                    .setNavigationBarContrastEnforced(
                            false
                    );
        }


        setContentView(
                R.layout.activity_main
        );


        configurarEstadoInicial();

        validarPerfilECarregarHome();
    }


    private void configurarEstadoInicial() {

        MaterialButton botaoTentarNovamente =
                findViewById(
                        R.id.botaoTentarNovamente
                );


        botaoTentarNovamente
                .setOnClickListener(
                        view -> {

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


                            validarPerfilECarregarHome();
                        }
                );
    }


    private void validarPerfilECarregarHome() {

        FirebaseUser usuario =
                autenticacao.getCurrentUser();


        if (usuario == null) {

            abrirLogin(
                    "Entre na sua conta para continuar.",
                    false
            );

            return;
        }


        bancoFirestore
                .collection(
                        "usuarios"
                )
                .document(
                        usuario.getUid()
                )
                .get()
                .addOnSuccessListener(
                        documento ->

                                validarDocumentoPerfil(
                                        usuario,
                                        documento
                                )
                )
                .addOnFailureListener(
                        erro ->

                                mostrarErroCarregamento()
                );
    }


    private void validarDocumentoPerfil(
            @NonNull FirebaseUser usuario,
            @NonNull DocumentSnapshot documento
    ) {

        if (!documento.exists()) {

            abrirLogin(
                    "Complete seu cadastro antes de acessar a home.",
                    true
            );

            return;
        }


        String tipoPerfil =
                documento.getString(
                        "tipoPerfil"
                );


        Boolean possuiCodigoCondominio =
                documento.getBoolean(
                        "possuiCodigoCondominio"
                );


        String endereco =
                documento.getString(
                        "endereco"
                );


        if (
                !PerfilAcesso.ehUsuarioComum(
                        tipoPerfil,
                        possuiCodigoCondominio,
                        endereco
                )
        ) {

            abrirLogin(
                    "Esta home é exclusiva para usuários sem vínculo com condomínio.",
                    true
            );

            return;
        }


        String nome =
                documento.getString(
                        "nome"
                );


        if (
                nome == null
                        ||
                        nome.trim().isEmpty()
        ) {

            nome =
                    usuario.getDisplayName();
        }


        carregarHome(
                nome
        );
    }


    private void carregarHome(
            String nomeCompleto
    ) {

        setContentView(
                R.layout.activity_home_usuario_comum
        );


        /*
         * Envolvemos a Home atual em um
         * SwipeRefreshLayout em tempo de execução.
         *
         * Assim NÃO precisamos alterar o seu XML
         * atual e não estragamos os ajustes visuais
         * da Home/barra de navegação.
         */
        configurarPullToRefresh();


        /*
         * Safe Area.
         */
        configurarInsetsDaHome();


        homeCarregada =
                true;


        TextView textoSaudacao =
                findViewById(
                        R.id.textoSaudacao
                );


        textoMensagemMotivacional =
                findViewById(
                        R.id.textoMensagemMotivacional
                );


        textoSaudacao.setText(
                getString(
                        R.string.home_saudacao,
                        obterPrimeiroNome(
                                nomeCompleto
                        )
                )
        );


        /*
         * Se a Activity já estiver visível,
         * buscamos a primeira mensagem agora.
         */
        if (atividadeVisivel) {

            buscarMensagemMotivacional();
        }
    }


    /*
     * ========================================================
     * PULL TO REFRESH
     * ========================================================
     */

    private void configurarPullToRefresh() {

        View raizHome =
                findViewById(
                        R.id.raizHomeUsuarioComum
                );


        if (raizHome == null) {
            return;
        }


        ViewParent paiAtual =
                raizHome.getParent();


        if (!(paiAtual instanceof ViewGroup)) {
            return;
        }


        ViewGroup grupoPai =
                (ViewGroup) paiAtual;


        int posicao =
                grupoPai.indexOfChild(
                        raizHome
                );


        ViewGroup.LayoutParams parametrosOriginais =
                raizHome.getLayoutParams();


        /*
         * Tiramos temporariamente a Home
         * do container.
         */
        grupoPai.removeView(
                raizHome
        );


        /*
         * Criamos o SwipeRefreshLayout.
         */
        atualizacaoHome =
                new SwipeRefreshLayout(
                        this
                );


        /*
         * Ele ocupa exatamente o mesmo espaço
         * que a Home ocupava.
         */
        atualizacaoHome.setLayoutParams(
                parametrosOriginais
        );


        atualizacaoHome.setBackgroundColor(
                ContextCompat.getColor(
                        this,
                        R.color.branco
                )
        );


        /*
         * Cor verde EcoCiente do spinner.
         */
        atualizacaoHome.setColorSchemeColors(
                ContextCompat.getColor(
                        this,
                        R.color.verde_escuro_principal
                )
        );


        /*
         * Fundo branco do indicador.
         */
        atualizacaoHome
                .setProgressBackgroundColorSchemeColor(
                        ContextCompat.getColor(
                                this,
                                R.color.branco
                        )
                );


        /*
         * Distância necessária para disparar
         * a atualização.
         *
         * Dá aquela sensação de puxar e soltar
         * parecida com Instagram.
         */
        atualizacaoHome.setDistanceToTriggerSync(
                dpParaPx(
                        76
                )
        );


        /*
         * Ao soltar:
         *
         * atualiza SOMENTE a mensagem motivacional.
         */
        atualizacaoHome.setOnRefreshListener(
                this::buscarMensagemMotivacional
        );


        /*
         * Colocamos a Home dentro do
         * SwipeRefreshLayout.
         */
        atualizacaoHome.addView(
                raizHome,

                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );


        /*
         * E devolvemos tudo para a posição
         * original na Activity.
         */
        grupoPai.addView(
                atualizacaoHome,
                posicao
        );
    }


    /*
     * ========================================================
     * SAFE AREA
     * ========================================================
     */

    private void configurarInsetsDaHome() {

        View raizHome =
                findViewById(
                        R.id.raizHomeUsuarioComum
                );


        if (raizHome == null) {
            return;
        }


        final int paddingEsquerdoOriginal =
                raizHome.getPaddingLeft();


        final int paddingTopoOriginal =
                raizHome.getPaddingTop();


        final int paddingDireitoOriginal =
                raizHome.getPaddingRight();


        final int paddingInferiorOriginal =
                raizHome.getPaddingBottom();


        ViewCompat.setOnApplyWindowInsetsListener(
                raizHome,

                (view, insets) -> {

                    Insets sistema =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                                            |
                                            WindowInsetsCompat.Type.displayCutout()
                            );


                    view.setPadding(
                            paddingEsquerdoOriginal
                                    +
                                    sistema.left,

                            paddingTopoOriginal
                                    +
                                    sistema.top,

                            paddingDireitoOriginal
                                    +
                                    sistema.right,

                            paddingInferiorOriginal
                                    +
                                    sistema.bottom
                    );


                    /*
                     * Também posicionamos o spinner
                     * abaixo da barra do sistema.
                     */
                    if (atualizacaoHome != null) {

                        atualizacaoHome
                                .setProgressViewOffset(
                                        false,

                                        sistema.top
                                                +
                                                dpParaPx(4),

                                        sistema.top
                                                +
                                                dpParaPx(48)
                                );
                    }


                    return insets;
                }
        );


        WindowInsetsControllerCompat controlador =
                WindowCompat.getInsetsController(
                        getWindow(),
                        raizHome
                );


        if (controlador != null) {

            controlador
                    .setAppearanceLightStatusBars(
                            true
                    );


            controlador
                    .setAppearanceLightNavigationBars(
                            true
                    );
        }


        ViewCompat.requestApplyInsets(
                raizHome
        );
    }


    /*
     * ========================================================
     * MENSAGEM MOTIVACIONAL
     * ========================================================
     */

    private void buscarMensagemMotivacional() {

        if (!homeCarregada) {

            finalizarAtualizacaoManual();

            return;
        }


        repositorioNotificacao.buscar(
                mensagem -> {

                    /*
                     * O callback sempre termina
                     * o indicador de refresh,
                     * com sucesso ou erro.
                     */
                    finalizarAtualizacaoManual();


                    if (
                            mensagem == null
                                    ||
                                    textoMensagemMotivacional == null
                                    ||
                                    isFinishing()
                    ) {

                        return;
                    }


                    String mensagemAtual =
                            textoMensagemMotivacional
                                    .getText()
                                    .toString();


                    /*
                     * Se por algum motivo só existir
                     * uma mensagem no banco, evitamos
                     * uma animação inútil.
                     */
                    if (
                            mensagem.equals(
                                    mensagemAtual
                            )
                    ) {

                        return;
                    }


                    /*
                     * Pequeno fade para a troca não
                     * acontecer de forma seca.
                     */
                    textoMensagemMotivacional
                            .animate()
                            .cancel();


                    textoMensagemMotivacional
                            .animate()
                            .alpha(0f)
                            .setDuration(130L)
                            .withEndAction(
                                    () -> {

                                        if (
                                                textoMensagemMotivacional
                                                        ==
                                                        null
                                        ) {

                                            return;
                                        }


                                        textoMensagemMotivacional
                                                .setText(
                                                        mensagem
                                                );


                                        textoMensagemMotivacional
                                                .animate()
                                                .alpha(1f)
                                                .setDuration(170L)
                                                .start();
                                    }
                            )
                            .start();
                }
        );
    }


    private void finalizarAtualizacaoManual() {

        if (
                atualizacaoHome != null
                        &&
                        atualizacaoHome.isRefreshing()
        ) {

            atualizacaoHome.setRefreshing(
                    false
            );
        }
    }


    /*
     * ========================================================
     * OUTROS MÉTODOS
     * ========================================================
     */

    private void mostrarErroCarregamento() {

        findViewById(
                R.id.indicadorCarregamentoHome
        ).setVisibility(
                View.GONE
        );


        findViewById(
                R.id.estadoErroHome
        ).setVisibility(
                View.VISIBLE
        );
    }


    private void abrirLogin(
            String mensagem,
            boolean encerrarSessao
    ) {

        if (encerrarSessao) {

            autenticacao.signOut();
        }


        Toast.makeText(
                this,
                mensagem,
                Toast.LENGTH_LONG
        ).show();


        Intent rota =
                new Intent(
                        this,
                        Login.class
                );


        rota.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );


        startActivity(
                rota
        );


        finish();
    }


    @NonNull
    private String obterPrimeiroNome(
            String nomeCompleto
    ) {

        if (
                nomeCompleto == null
                        ||
                        nomeCompleto
                                .trim()
                                .isEmpty()
        ) {

            return getString(
                    R.string.home_usuario_padrao
            );
        }


        return nomeCompleto
                .trim()
                .split("\\s+")[0];
    }


    private int dpParaPx(
            int dp
    ) {

        return Math.round(
                dp
                        *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }


    /*
     * ========================================================
     * CICLO DE VIDA
     * ========================================================
     */


    @Override
    protected void onStart() {

        super.onStart();


        atividadeVisivel =
                true;


        /*
         * Se a Home já estava carregada,
         * significa que o usuário saiu
         * e voltou para ela.
         *
         * Buscamos outra mensagem.
         */
        if (homeCarregada) {

            buscarMensagemMotivacional();
        }
    }


    @Override
    protected void onStop() {
        atividadeVisivel = false;

        super.onStop();
    }


    @Override
    protected void onDestroy() {
        textoMensagemMotivacional = null;
        atualizacaoHome = null;

        super.onDestroy();
    }
}