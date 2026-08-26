package com.example.ecociente.views;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.ecociente.R;
import com.google.android.material.card.MaterialCardView;

public class Login extends AppCompatActivity {

    private static final String CHAVE_NOME = "nome";
    private static final String CHAVE_DATA_NASCIMENTO = "dataNascimento";
    private static final String CHAVE_EMAIL = "email";
    private static final String CHAVE_ENDERECO = "endereco";
    private static final String CHAVE_NUMERO = "numero";
    private static final String CHAVE_CEP = "cep";
    private static final String CHAVE_COMPLEMENTO = "complemento";
    private static final String CHAVE_CIDADE = "cidade";
    private static final String CHAVE_ESTADO = "estado";

    private static final String CHAVE_POSSUI_CODIGO_CONDOMINIO = "possuiCodigoCondominio";

    private static final String CHAVE_CODIGO_CONDOMINIO = "codigoCondominio";

    private View raizLogin;
    private View conteudoPainelLogin;

    private MaterialCardView seletorLoginCadastro;
    private MaterialCardView indicadorAba;

    private TextView abaLogin;
    private TextView abaCadastro;

    private ViewTreeObserver.OnGlobalFocusChangeListener observadorFoco;

    private boolean navegacaoHabilitada = true;

    private String nome = "";
    private String dataNascimento = "";
    private String email = "";
    private String senha = "";

    private String endereco = "";
    private String numero = "";
    private String cep = "";
    private String complemento = "";
    private String cidade = "";
    private String estado = "";

    private boolean possuiCodigoCondominio = false;
    private String codigoCondominio = "";

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        /*
         * Permitimos que o app use toda a tela.
         * Os espaços das barras do Android e do teclado
         * são tratados manualmente logo abaixo.
         */
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_login);

        recuperarDadosSalvos(estadoSalvo);

        inicializarComponentes();

        configurarInsetsDoSistema();

        configurarAjusteAutomaticoAoTeclado();

        configurarNavegacao();

        configurarBotaoVoltar();

        if (estadoSalvo == null) {

            abrirLogin(false);

        } else {

            indicadorAba.post(this::sincronizarInterfaceComFragmentAtual);
        }
    }

    private void inicializarComponentes() {

        raizLogin = findViewById(R.id.raizLogin);

        conteudoPainelLogin = findViewById(R.id.conteudoPainelLogin);

        seletorLoginCadastro = findViewById(R.id.seletorLoginCadastro);

        indicadorAba = findViewById(R.id.indicadorAba);

        abaLogin = findViewById(R.id.abaLogin);

        abaCadastro = findViewById(R.id.abaCadastro);
    }

    private void configurarInsetsDoSistema() {

        final int paddingEsquerdaPainel = conteudoPainelLogin.getPaddingLeft();

        final int paddingTopoPainel = conteudoPainelLogin.getPaddingTop();

        final int paddingDireitaPainel = conteudoPainelLogin.getPaddingRight();

        final int paddingInferiorPainel = conteudoPainelLogin.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(
                raizLogin,
                (view, insets) -> {
                    Insets barraStatus = insets.getInsets(WindowInsetsCompat.Type.statusBars());

                    Insets barraNavegacao =
                            insets.getInsets(WindowInsetsCompat.Type.navigationBars());

                    Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());

                    /*
                     * Impede logo/conteúdo de ficar
                     * atrás da barra superior.
                     */
                    view.setPadding(0, barraStatus.top, 0, 0);

                    /*
                     * Quando não há teclado:
                     * respeita a barra de navegação/home.
                     *
                     * Quando há teclado:
                     * utiliza a altura do próprio teclado.
                     */
                    int espacoInferior = Math.max(barraNavegacao.bottom, teclado.bottom);

                    conteudoPainelLogin.setPadding(
                            paddingEsquerdaPainel,
                            paddingTopoPainel,
                            paddingDireitaPainel,
                            paddingInferiorPainel + espacoInferior);

                    return insets;
                });

        WindowInsetsControllerCompat controlador =
                WindowCompat.getInsetsController(getWindow(), raizLogin);

        /*
         * Status bar fica sobre fundo verde.
         */
        controlador.setAppearanceLightStatusBars(false);

        /*
         * Barra de navegação normalmente fica
         * sobre a área branca.
         */
        controlador.setAppearanceLightNavigationBars(true);

        ViewCompat.requestApplyInsets(raizLogin);
    }

    private void configurarAjusteAutomaticoAoTeclado() {

        observadorFoco =
                (focoAntigo, focoNovo) -> {
                    if (!(focoNovo instanceof EditText)) {
                        return;
                    }

                    /*
                     * Primeiro ajuste enquanto
                     * o teclado está surgindo.
                     */
                    focoNovo.postDelayed(() -> trazerCampoParaAreaVisivel(focoNovo), 260);

                    /*
                     * Segundo ajuste depois que
                     * a animação do teclado terminou.
                     *
                     * Isso evita Senha, Confirmar senha,
                     * Código do condomínio etc.
                     * ficarem escondidos.
                     */
                    focoNovo.postDelayed(() -> trazerCampoParaAreaVisivel(focoNovo), 480);
                };

        raizLogin.getViewTreeObserver().addOnGlobalFocusChangeListener(observadorFoco);
    }

    private void trazerCampoParaAreaVisivel(View campo) {

        if (!campo.isAttachedToWindow() || !campo.hasFocus()) {
            return;
        }

        int folgaAbaixo = dpParaPx(110);

        Rect area = new Rect(0, 0, campo.getWidth(), campo.getHeight() + folgaAbaixo);

        /*
         * O NestedScrollView pai recebe a solicitação
         * e desloca o formulário automaticamente.
         */
        campo.requestRectangleOnScreen(area, true);
    }

    private int dpParaPx(int dp) {

        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void configurarNavegacao() {

        abaLogin.setOnClickListener(
                view -> {
                    if (!navegacaoHabilitada) {
                        return;
                    }

                    abrirLogin(true);
                });

        abaCadastro.setOnClickListener(
                view -> {
                    if (!navegacaoHabilitada) {
                        return;
                    }

                    abrirCadastroEtapa1(true);
                });
    }

    private void configurarBotaoVoltar() {

        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {

                                if (!navegacaoHabilitada) {
                                    return;
                                }

                                Fragment fragmentAtual =
                                        getSupportFragmentManager()
                                                .findFragmentById(
                                                        R.id.containerFragmentAutenticacao);

                                if (fragmentAtual instanceof CadastroEtapa2Fragment) {

                                    voltarCadastroEtapa1();

                                    return;
                                }

                                if (fragmentAtual instanceof CadastroEtapa1Fragment) {

                                    abrirLogin(true);

                                    return;
                                }

                                finish();
                            }
                        });
    }

    public void abrirLogin() {

        abrirLogin(true);
    }

    private void abrirLogin(boolean animar) {

        Fragment fragmentAtual =
                getSupportFragmentManager().findFragmentById(R.id.containerFragmentAutenticacao);

        if (fragmentAtual instanceof LoginFragment) {

            atualizarVisualAba(false, animar);

            return;
        }

        FragmentTransaction transacao = getSupportFragmentManager().beginTransaction();

        if (animar) {

            transacao.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        }

        transacao.replace(R.id.containerFragmentAutenticacao, new LoginFragment()).commit();

        atualizarVisualAba(false, animar);
    }

    public void abrirCadastroEtapa1() {

        abrirCadastroEtapa1(true);
    }

    private void abrirCadastroEtapa1(boolean animar) {

        Fragment fragmentAtual =
                getSupportFragmentManager().findFragmentById(R.id.containerFragmentAutenticacao);

        if (fragmentAtual instanceof CadastroEtapa1Fragment) {

            atualizarVisualAba(true, animar);

            return;
        }

        FragmentTransaction transacao = getSupportFragmentManager().beginTransaction();

        if (animar) {

            transacao.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        }

        transacao
                .replace(R.id.containerFragmentAutenticacao, new CadastroEtapa1Fragment())
                .commit();

        atualizarVisualAba(true, animar);
    }

    public void abrirCadastroEtapa2() {

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.containerFragmentAutenticacao, new CadastroEtapa2Fragment())
                .commit();

        atualizarVisualAba(true, false);
    }

    public void voltarCadastroEtapa1() {

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.containerFragmentAutenticacao, new CadastroEtapa1Fragment())
                .commit();

        atualizarVisualAba(true, false);
    }

    private void atualizarVisualAba(boolean cadastroSelecionado, boolean animar) {

        seletorLoginCadastro.post(
                () -> {
                    float destino = cadastroSelecionado ? indicadorAba.getWidth() : 0f;

                    if (animar) {

                        indicadorAba.animate().translationX(destino).setDuration(200).start();

                    } else {

                        indicadorAba.setTranslationX(destino);
                    }

                    int branco = ContextCompat.getColor(this, R.color.branco);

                    int verde = ContextCompat.getColor(this, R.color.verde_escuro_principal);

                    if (cadastroSelecionado) {

                        abaLogin.setTextColor(verde);

                        abaCadastro.setTextColor(branco);

                    } else {

                        abaLogin.setTextColor(branco);

                        abaCadastro.setTextColor(verde);
                    }
                });
    }

    private void sincronizarInterfaceComFragmentAtual() {

        Fragment fragmentAtual =
                getSupportFragmentManager().findFragmentById(R.id.containerFragmentAutenticacao);

        boolean cadastroSelecionado =
                fragmentAtual instanceof CadastroEtapa1Fragment
                        || fragmentAtual instanceof CadastroEtapa2Fragment;

        atualizarVisualAba(cadastroSelecionado, false);
    }

    public void definirNavegacaoHabilitada(boolean habilitada) {

        navegacaoHabilitada = habilitada;

        abaLogin.setEnabled(habilitada);

        abaCadastro.setEnabled(habilitada);
    }

    public void salvarDadosEtapa1(String nome, String dataNascimento, String email, String senha) {

        this.nome = nome;

        this.dataNascimento = dataNascimento;

        this.email = email;

        this.senha = senha;
    }

    public void salvarDadosEtapa2(
            String endereco,
            String numero,
            String cep,
            String complemento,
            String cidade,
            String estado) {

        this.endereco = endereco;

        this.numero = numero;

        this.cep = cep;

        this.complemento = complemento;

        this.cidade = cidade;

        this.estado = estado;
    }

    public void salvarDadosCodigoCondominio(
            boolean possuiCodigoCondominio, String codigoCondominio) {

        this.possuiCodigoCondominio = possuiCodigoCondominio;

        this.codigoCondominio = codigoCondominio;
    }

    public String getNome() {
        return nome;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getNumero() {
        return numero;
    }

    public String getCep() {
        return cep;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getCidade() {
        return cidade;
    }

    public String getEstado() {
        return estado;
    }

    public boolean isPossuiCodigoCondominio() {
        return possuiCodigoCondominio;
    }

    public String getCodigoCondominio() {
        return codigoCondominio;
    }

    private void recuperarDadosSalvos(Bundle estadoSalvo) {

        if (estadoSalvo == null) {
            return;
        }

        nome = estadoSalvo.getString(CHAVE_NOME, "");

        dataNascimento = estadoSalvo.getString(CHAVE_DATA_NASCIMENTO, "");

        email = estadoSalvo.getString(CHAVE_EMAIL, "");

        endereco = estadoSalvo.getString(CHAVE_ENDERECO, "");

        numero = estadoSalvo.getString(CHAVE_NUMERO, "");

        cep = estadoSalvo.getString(CHAVE_CEP, "");

        complemento = estadoSalvo.getString(CHAVE_COMPLEMENTO, "");

        cidade = estadoSalvo.getString(CHAVE_CIDADE, "");

        estado = estadoSalvo.getString(CHAVE_ESTADO, "");

        possuiCodigoCondominio = estadoSalvo.getBoolean(CHAVE_POSSUI_CODIGO_CONDOMINIO, false);

        codigoCondominio = estadoSalvo.getString(CHAVE_CODIGO_CONDOMINIO, "");
    }

    @Override
    protected void onSaveInstanceState(Bundle estadoSaida) {

        estadoSaida.putString(CHAVE_NOME, nome);

        estadoSaida.putString(CHAVE_DATA_NASCIMENTO, dataNascimento);

        estadoSaida.putString(CHAVE_EMAIL, email);

        estadoSaida.putString(CHAVE_ENDERECO, endereco);

        estadoSaida.putString(CHAVE_NUMERO, numero);

        estadoSaida.putString(CHAVE_CEP, cep);

        estadoSaida.putString(CHAVE_COMPLEMENTO, complemento);

        estadoSaida.putString(CHAVE_CIDADE, cidade);

        estadoSaida.putString(CHAVE_ESTADO, estado);

        estadoSaida.putBoolean(CHAVE_POSSUI_CODIGO_CONDOMINIO, possuiCodigoCondominio);

        estadoSaida.putString(CHAVE_CODIGO_CONDOMINIO, codigoCondominio);

        super.onSaveInstanceState(estadoSaida);
    }

    @Override
    protected void onDestroy() {

        if (raizLogin != null
                && observadorFoco != null
                && raizLogin.getViewTreeObserver().isAlive()) {

            raizLogin.getViewTreeObserver().removeOnGlobalFocusChangeListener(observadorFoco);
        }
        super.onDestroy();
    }
}
