package com.example.ecociente.views;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.ecociente.R;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.ui.Motion;
import com.example.ecociente.viewmodels.PerfilViewModel;
import com.google.firebase.auth.FirebaseUser;
import java.io.File;

public class GerenciarPerfilActivity extends AppCompatActivity {

    public static final String EXTRA_ORIGEM_NAVEGACAO = "origemNavegacaoPerfil";
    public static final String ORIGEM_HOME = "home";
    public static final String ORIGEM_QUIZ = "quiz";

    private PerfilViewModel viewModel;

    private final Motion motion = new Motion();

    private TextView textoNome;
    private TextView textoEmail;
    private TextView textoIniciaisAvatar;
    private ImageView imagemFoto;
    private View indicadorFoto;

    private Uri uriFotoCamera;

    private final ActivityResultLauncher<PickVisualMediaRequest> seletorFoto =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::enviarFoto);

    private final ActivityResultLauncher<Uri> camera =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    tirou -> {
                        if (tirou) {
                            enviarFoto(uriFotoCamera);
                        }
                    });

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        setContentView(R.layout.activity_gerenciar_perfil);

        viewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        inicializarComponentes();

        configurarCliques();

        configurarNavegacao();

        carregarPerfil();

        motion.staggerIn(
                findViewById(R.id.cabecalhoPerfil),
                findViewById(R.id.tituloSecaoConta),
                findViewById(R.id.containerOpcoesConta),
                findViewById(R.id.tituloSecaoSessao),
                findViewById(R.id.containerOpcoesSessao));
    }

    @Override
    protected void onDestroy() {
        motion.cancelAll();

        super.onDestroy();
    }

    private void inicializarComponentes() {

        textoNome = findViewById(R.id.textoNomePerfil);

        textoEmail = findViewById(R.id.textoEmailPerfil);

        textoIniciaisAvatar = findViewById(R.id.textoIniciaisAvatarPerfil);

        imagemFoto = findViewById(R.id.imagemFotoPerfil);

        indicadorFoto = findViewById(R.id.indicadorFotoPerfil);
    }

    private void configurarCliques() {

        findViewById(R.id.containerAvatarPerfil).setOnClickListener(view -> mostrarOpcoesFoto());

        findViewById(R.id.linhaInformacoesPessoais)
                .setOnClickListener(
                        view ->
                                startActivity(
                                        new Intent(this, InformacoesPessoaisActivity.class)));

        findViewById(R.id.linhaGerenciarSenha)
                .setOnClickListener(
                        view -> startActivity(new Intent(this, GerenciarSenhaActivity.class)));

        findViewById(R.id.linhaNotificacoes)
                .setOnClickListener(
                        view -> startActivity(new Intent(this, NotificacoesActivity.class)));

        findViewById(R.id.linhaSairConta).setOnClickListener(view -> confirmarSaida());

        findViewById(R.id.linhaExcluirConta).setOnClickListener(view -> pedirSenhaParaExcluir());
    }

    private void carregarPerfil() {

        FirebaseUser usuario = viewModel.usuarioAtual();

        if (usuario == null) {
            return;
        }

        viewModel
                .buscarPerfil(usuario.getUid())
                .observe(this, perfil -> preencherCabecalho(usuario, perfil));
    }

    private void preencherCabecalho(FirebaseUser usuario, PerfilUsuario perfil) {

        String nomeExibido = PerfilUsuario.nomeExibicao(perfil, usuario);

        textoNome.setText(nomeExibido);

        textoEmail.setText(PerfilUsuario.emailExibicao(perfil, usuario));

        textoIniciaisAvatar.setText(obterInicial(nomeExibido));

        if (perfil != null && !perfil.getFotoUrl().isEmpty()) {
            mostrarFoto(perfil.getFotoUrl());
        }
    }

    private void configurarNavegacao() {

        View botaoHome = findViewById(R.id.botaoNavHomePerfil);
        View botaoGuia = findViewById(R.id.botaoNavGuiaPerfil);
        View botaoQuiz = findViewById(R.id.botaoNavQuizPerfil);
        View botaoPerfil = findViewById(R.id.botaoNavPerfilPerfil);
        View botaoAssistente = findViewById(R.id.botaoNavAssistentePerfil);
        View indicador = findViewById(R.id.indicadorNavegacaoPerfil);

        botaoHome.setOnClickListener(view -> abrirHome());
        botaoQuiz.setOnClickListener(view -> abrirQuiz());
        botaoAssistente.setOnClickListener(view -> startActivity(new Intent(this, ChatActivity.class)));

        Motion.pressFeedback(botaoHome);
        Motion.pressFeedback(botaoGuia);
        Motion.pressFeedback(botaoQuiz);
        Motion.pressFeedback(botaoPerfil);
        Motion.pressFeedback(botaoAssistente);

        indicador.post(
                () -> {
                    float destino =
                            botaoPerfil.getX()
                                    + (botaoPerfil.getWidth() / 2f)
                                    - (indicador.getWidth() / 2f);

                    String origem = getIntent().getStringExtra(EXTRA_ORIGEM_NAVEGACAO);

                    if (origem == null) {
                        indicador.setTranslationX(destino);
                        return;
                    }

                    View botaoOrigem = ORIGEM_QUIZ.equals(origem) ? botaoQuiz : botaoHome;

                    indicador.setTranslationX(
                            botaoOrigem.getX()
                                    + (botaoOrigem.getWidth() / 2f)
                                    - (indicador.getWidth() / 2f));

                    indicador
                            .animate()
                            .translationX(destino)
                            .setDuration(Motion.ENTER_MS)
                            .setInterpolator(new FastOutSlowInInterpolator())
                            .start();
                });

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.raizPerfil),
                (raiz, insets) -> {
                    Insets sistema =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                                            | WindowInsetsCompat.Type.displayCutout());

                    raiz.setPadding(sistema.left, sistema.top, sistema.right, sistema.bottom);

                    return insets;
                });
    }

    private void abrirHome() {

        Intent rota = new Intent(this, MainActivity.class);

        rota.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(rota);

        overridePendingTransition(0, 0);
    }

    private void abrirQuiz() {

        Intent rota = new Intent(this, QuizActivity.class);

        rota.putExtra(QuizActivity.EXTRA_ANIMAR_NAVEGACAO, false);

        startActivity(rota);

        finish();

        overridePendingTransition(0, 0);
    }

    private void mostrarOpcoesFoto() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.perfil_foto_alterar)
                .setItems(
                        new CharSequence[] {
                            getString(R.string.perfil_foto_tirar),
                            getString(R.string.perfil_foto_galeria)
                        },
                        (dialogo, opcao) -> {
                            if (opcao == 0) {
                                abrirCamera();
                            } else {
                                abrirGaleria();
                            }
                        })
                .show();
    }

    private void abrirGaleria() {

        seletorFoto.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
    }

    private void abrirCamera() {

        File pasta = new File(getCacheDir(), "fotos");

        pasta.mkdirs();

        File[] antigas = pasta.listFiles();

        if (antigas != null) {
            for (File antiga : antigas) {
                antiga.delete();
            }
        }

        File arquivo = new File(pasta, "perfil_" + System.currentTimeMillis() + ".jpg");

        uriFotoCamera = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", arquivo);

        try {
            camera.launch(uriFotoCamera);

        } catch (ActivityNotFoundException erro) {
            Toast.makeText(this, R.string.perfil_foto_erro_camera, Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarFoto(@Nullable Uri imagem) {

        FirebaseUser usuario = viewModel.usuarioAtual();

        if (imagem == null || usuario == null) {
            return;
        }

        indicadorFoto.setVisibility(View.VISIBLE);

        viewModel
                .atualizarFotoPerfil(getApplicationContext(), usuario.getUid(), imagem)
                .observe(
                        this,
                        resultado -> {
                            indicadorFoto.setVisibility(View.GONE);

                            if (!resultado.isSucesso()) {
                                Toast.makeText(this, resultado.getMensagemErro(), Toast.LENGTH_LONG)
                                        .show();
                                return;
                            }

                            mostrarFoto(resultado.getUrl());

                            Toast.makeText(this, R.string.perfil_foto_sucesso, Toast.LENGTH_SHORT)
                                    .show();
                        });
    }

    private void mostrarFoto(String url) {

        textoIniciaisAvatar.setVisibility(View.GONE);

        imagemFoto.setVisibility(View.VISIBLE);

        Glide.with(this).load(url).into(imagemFoto);
    }

    private String obterInicial(String nome) {

        if (nome == null || nome.trim().isEmpty()) {
            return "?";
        }

        return nome.trim().substring(0, 1).toUpperCase();
    }

    private void confirmarSaida() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.perfil_sair_titulo)
                .setMessage(R.string.perfil_sair_mensagem)
                .setPositiveButton(
                        R.string.perfil_sair_confirmar,
                        (dialogo, botao) -> {
                            viewModel.encerrarSessao();

                            voltarParaLogin();
                        })
                .setNegativeButton(R.string.perfil_cancelar, null)
                .show();
    }

    private void pedirSenhaParaExcluir() {

        View corpo = LayoutInflater.from(this).inflate(R.layout.dialog_confirmar_senha, null);

        EditText campoSenha = corpo.findViewById(R.id.campoConfirmarSenhaExclusao);

        new AlertDialog.Builder(this)
                .setTitle(R.string.perfil_excluir_titulo)
                .setView(corpo)
                .setPositiveButton(
                        R.string.perfil_excluir_confirmar,
                        (dialogo, botao) -> {
                            String senha = campoSenha.getText().toString();

                            if (senha.isEmpty()) {
                                Toast.makeText(this, R.string.perfil_excluir_senha_vazia, Toast.LENGTH_SHORT)
                                        .show();
                                return;
                            }

                            excluirConta(senha);
                        })
                .setNegativeButton(R.string.perfil_cancelar, null)
                .show();
    }

    private void excluirConta(String senha) {

        viewModel
                .excluirConta(senha)
                .observe(
                        this,
                        resultado -> {
                            if (resultado == null) {
                                return;
                            }

                            if (!resultado.isSucesso()) {
                                Toast.makeText(this, resultado.getMensagemErro(), Toast.LENGTH_LONG)
                                        .show();
                                return;
                            }

                            Toast.makeText(this, R.string.perfil_excluir_sucesso, Toast.LENGTH_LONG).show();

                            voltarParaLogin();
                        });
    }

    private void voltarParaLogin() {

        Intent rota = new Intent(this, Login.class);

        rota.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(rota);

        finish();
    }
}
