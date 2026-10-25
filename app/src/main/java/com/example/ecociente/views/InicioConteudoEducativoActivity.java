package com.example.ecociente.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.ecociente.R;
import com.example.ecociente.model.CursoEducativo;
import com.example.ecociente.model.FluxoEducativo;
import com.example.ecociente.ui.AnimacaoEkoFrames;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.Motion;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Apresentacao de aulas, guias e outros conteudos educativos.
 *
 * O mascote utiliza imagens PNG sequenciais com crossfade por
 * AnimacaoEkoFrames. Nao depende de GIF, Glide ou internet.
 */
public class InicioConteudoEducativoActivity extends AppCompatActivity {

    private static final String CHAVE_FRASE = "eco.frase.introducao";
    private static final int MAX_FRAMES_ESTUDO = 30;
    private static final long TEMPO_POSE_MS = 1800L;
    private static final long TEMPO_TRANSICAO_MS = 450L;

    // Temporariamente locais. Futuramente podem vir de um endpoint de conteudos.
    private static final String[] FRASES_ESTUDO = {
            "Cada descoberta é uma nova oportunidade de cuidar do planeta.",
            "Aprender hoje é transformar o amanhã, uma atitude de cada vez.",
            "O conhecimento é uma semente: vamos cultivar um futuro mais verde!",
            "Sua curiosidade pode inspirar mudanças que fazem a diferença.",
            "Você está dando mais um passo para viver de forma sustentável.",
            "Conhecer o meio ambiente é o primeiro passo para protegê-lo.",
            "As pequenas escolhas de hoje ajudam a construir um mundo melhor.",
            "Vamos descobrir juntos como cuidar melhor dos nossos recursos?",
            "Cada aula traz uma ideia nova para praticar no dia a dia.",
            "O planeta agradece a quem aprende e compartilha o que descobriu.",
            "O Eko acredita nas suas ideias para transformar o mundo!",
            "Bora aprender? O futuro sustentável começa com conhecimento!"
    };

    private final Motion movimento = new Motion();

    private CursoEducativo curso;
    private CursoEducativo.Aula aula;
    private AnimacaoEkoFrames animacaoEko;
    private int fraseAtual;

    @NonNull
    public static Intent criarIntent(@NonNull Context contexto,
                                     @NonNull CursoEducativo curso,
                                     @Nullable CursoEducativo.Aula aula) {
        Intent intent = FluxoEducativo.comCurso(
                new Intent(contexto, InicioConteudoEducativoActivity.class), curso
        );
        intent.putExtra(
                FluxoEducativo.EXTRA_AULA_ID,
                aula == null ? FluxoEducativo.SEM_AULA : aula.id
        );
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle estado) {
        super.onCreate(estado);

        JanelaEdgeToEdge.aplicar(this);
        WindowInsetsControllerCompat barras = WindowCompat.getInsetsController(
                getWindow(), getWindow().getDecorView()
        );
        barras.setAppearanceLightStatusBars(false);
        barras.setAppearanceLightNavigationBars(false);

        setContentView(R.layout.activity_inicio_conteudo_educativo);
        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizInicioConteudo));

        curso = FluxoEducativo.lerCurso(getIntent());
        if (curso == null) {
            finish();
            return;
        }
        aula = FluxoEducativo.lerAula(getIntent(), curso);

        String tipo = FluxoEducativo.tipo(curso, aula);
        ((TextView) findViewById(R.id.tipoInicioConteudo)).setText(
                tipo.toUpperCase(Locale.ROOT)
                        + (aula == null ? "" :
                        " " + String.format(Locale.getDefault(), "%02d", aula.ordem))
        );
        ((TextView) findViewById(R.id.tituloInicioConteudo))
                .setText(FluxoEducativo.nome(curso, aula));
        ((TextView) findViewById(R.id.descricaoInicioConteudo))
                .setText(FluxoEducativo.descricao(curso, aula));

        // Usa somente o container existente no XML. A ImageView antiga foi removida.
        FrameLayout containerEko = findViewById(R.id.containerAnimacaoEkoEstudar);
        animacaoEko = new AnimacaoEkoFrames(
                containerEko,
                encontrarFramesEstudo(),
                TEMPO_POSE_MS,
                TEMPO_TRANSICAO_MS
        );

        int indiceSorteado = estado == null
                ? new Random().nextInt(FRASES_ESTUDO.length)
                : estado.getInt(CHAVE_FRASE, 0);
        fraseAtual = Math.floorMod(indiceSorteado, FRASES_ESTUDO.length);
        ((TextView) findViewById(R.id.mensagemMotivacionalInicio))
                .setText(FRASES_ESTUDO[fraseAtual]);

        List<FluxoEducativo.Pagina> paginas = FluxoEducativo.paginas(curso, aula);
        MaterialButton comecar = findViewById(R.id.botaoComecarConteudo);
        comecar.setEnabled(!paginas.isEmpty());
        atualizarBotao();

        findViewById(R.id.voltarInicioConteudo).setOnClickListener(v -> finish());
        comecar.setOnClickListener(v -> {
            startActivity(LeituraEducativaActivity.criarIntent(this, curso, aula));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        Motion.pressFeedback(comecar);

        movimento.fadeUp(containerEko, 50);
        movimento.fadeUp(findViewById(R.id.tituloInicioConteudo), 120);
        movimento.fadeUp(findViewById(R.id.descricaoInicioConteudo), 200);
        movimento.fadeUp(findViewById(R.id.mensagemMotivacionalInicio), 260);
        movimento.fadeUp(comecar, 310);
    }

    /**
     * Procura frame_1_gif_estudar.png, frame_2_gif_estudar.png, etc.
     * Arquivos em drawable-nodpi tambem sao encontrados como tipo "drawable".
     * Caso ainda nao tenham sido adicionados, exibe o mascote estatico.
     * A busca por nome evita falha de compilacao se algum PNG ainda faltar.
     */
    @NonNull
    private int[] encontrarFramesEstudo() {
        List<Integer> ids = new ArrayList<>();
        for (int numero = 1; numero <= MAX_FRAMES_ESTUDO; numero++) {
            String nome = "frame_" + numero + "_gif_estudar";
            int id = getResources().getIdentifier(nome, "drawable", getPackageName());
            if (id != 0) {
                ids.add(id);
            }
        }

        if (ids.isEmpty()) {
            return new int[]{ R.drawable.mascote_eko };
        }

        int[] resultado = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            resultado[i] = ids.get(i);
        }
        return resultado;
    }

    private void atualizarBotao() {
        if (curso == null) return;

        MaterialButton comecar = findViewById(R.id.botaoComecarConteudo);
        String tipo = FluxoEducativo.tipo(curso, aula).toLowerCase(Locale.ROOT);
        boolean concluido = FluxoEducativo.concluido(this, curso, aula);
        int paginaSalva = FluxoEducativo.paginaSalva(this, curso, aula);

        if (FluxoEducativo.paginas(curso, aula).isEmpty()) {
            comecar.setText("Conteúdo ainda não disponível");
        } else {
            comecar.setText(concluido
                    ? "Revisar " + tipo
                    : paginaSalva > 0 ? "Continuar " + tipo : "Começar " + tipo);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle estado) {
        estado.putInt(CHAVE_FRASE, fraseAtual);
        super.onSaveInstanceState(estado);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (animacaoEko != null) {
            animacaoEko.iniciar();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarBotao();
    }

    @Override
    protected void onStop() {
        if (animacaoEko != null) {
            animacaoEko.parar();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (animacaoEko != null) {
            animacaoEko.liberar();
            animacaoEko = null;
        }
        movimento.cancelAll();
        super.onDestroy();
    }
}
