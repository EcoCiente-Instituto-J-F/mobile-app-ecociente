package com.example.ecociente.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.example.ecociente.R;
import com.example.ecociente.model.CursoEducativo;
import com.example.ecociente.model.FluxoEducativo;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.Motion;
import com.google.android.material.button.MaterialButton;

/** Celebracao da leitura. Reutiliza ConfettiView do resultado do quiz. */
public class ConclusaoEducativaActivity extends AppCompatActivity {
    // Textos motivacionais locais, por enquanto sem API de mensagens no backend.
    private static final String[] MENSAGENS = {
            "Cada atitude consciente pode transformar o futuro do planeta.",
            "Seu aprendizado de hoje inspira as boas escolhas de amanhã.",
            "O planeta precisa de pessoas que aprendem e colocam em prática.",
            "Um novo conhecimento pode ser o início de uma grande mudança.",
            "Você está construindo um futuro mais sustentável, um passo por vez.",
            "Aprender a cuidar da natureza já é um gesto de carinho com o mundo.",
            "Pequenas escolhas, quando se multiplicam, criam grandes transformações.",
            "O cuidado com o planeta cresce com cada descoberta sua.",
            "Hoje você aprendeu; amanhã pode inspirar outras pessoas.",
            "Sua curiosidade e sua dedicação ajudam a fazer a diferença."
    };
    private static final String FINAL_EKO = "O Eko está orgulhoso de você!";

    private final Motion movimento = new Motion();
    private CursoEducativo curso;
    private CursoEducativo.Aula aula;
    private int mensagemAtual;

    @NonNull
    public static Intent criarIntent(@NonNull Context contexto, @NonNull CursoEducativo curso,
                                      @Nullable CursoEducativo.Aula aula) {
        Intent tela = FluxoEducativo.comCurso(new Intent(contexto, ConclusaoEducativaActivity.class), curso);
        tela.putExtra(FluxoEducativo.EXTRA_AULA_ID, aula == null ? FluxoEducativo.SEM_AULA : aula.id);
        return tela;
    }

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        JanelaEdgeToEdge.aplicar(this);
        WindowInsetsControllerCompat barras = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        barras.setAppearanceLightStatusBars(false);
        barras.setAppearanceLightNavigationBars(false);
        setContentView(R.layout.activity_conclusao_educativa);
        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizConclusaoEducativa));
        curso = FluxoEducativo.lerCurso(getIntent());
        if (curso == null) { finish(); return; }
        aula = FluxoEducativo.lerAula(getIntent(), curso);
        boolean cursoCompleto = aula != null && !curso.aulas.isEmpty()
                && FluxoEducativo.totalAulasConcluidas(this, curso) == curso.aulas.size();
        String tipo = cursoCompleto ? "curso" : FluxoEducativo.tipo(curso, aula).toLowerCase(java.util.Locale.ROOT);
        ((TextView) findViewById(R.id.tituloConclusaoEducativa)).setText(
                "Você concluiu " + (tipo.equals("aula") || tipo.equals("dica") ? "a " : "o ") + tipo + "!");
        int indiceMensagem = estado == null
                ? new java.util.Random().nextInt(MENSAGENS.length)
                : estado.getInt("eco.mensagem.conclusao", 0);
        indiceMensagem = Math.floorMod(indiceMensagem, MENSAGENS.length);
        mensagemAtual = indiceMensagem;
        ((TextView) findViewById(R.id.subtituloConclusaoEducativa)).setText(
                MENSAGENS[indiceMensagem] + "\n" + FINAL_EKO);
        MaterialButton voltar = findViewById(R.id.botaoVoltarConclusaoEducativa);
        voltar.setText(curso.aulas.isEmpty() ? "Voltar à biblioteca" : "Continuar aprendendo");
        voltar.setOnClickListener(v -> voltarAoCatalogo());
        Motion.pressFeedback(voltar);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { voltarAoCatalogo(); }
        });
        ImageView eko = findViewById(R.id.ekoConclusaoEducativa);
        carregarEkoComemoracao(eko);
        eko.setScaleX(.65f);
        eko.setScaleY(.65f);
        eko.setAlpha(0f);
        eko.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(650)
                .setInterpolator(new android.view.animation.OvershootInterpolator(1.7f)).start();
        movimento.fadeUp(findViewById(R.id.logoTopoConclusaoEducativa), 40);
        movimento.fadeUp(findViewById(R.id.etiquetaConclusaoEducativa), 90);
        movimento.fadeUp(findViewById(R.id.tituloConclusaoEducativa), 230);
        movimento.fadeUp(findViewById(R.id.subtituloConclusaoEducativa), 310);
        movimento.fadeUp(voltar, 480);
    }

    private void carregarEkoComemoracao(@NonNull ImageView eko) {
        int resId = getResources().getIdentifier("eko_comemoracao", "drawable", getPackageName());
        if (resId == 0) {
            resId = getResources().getIdentifier("eko_comemoracao", "drawable-nodpi", getPackageName());
        }
        if (resId == 0) {
            resId = R.drawable.mascote_eko;
        }

        try {
            Glide.with(this)
                    .asGif()
                    .load(resId)
                    .into(eko);
        } catch (Exception erro) {
            Glide.with(this)
                    .load(resId)
                    .into(eko);
        }
    }

    private void voltarAoCatalogo() {
        Intent destino;
        if (curso != null && !curso.aulas.isEmpty()) {
            destino = CursoEducativoActivity.criarIntent(this, curso);
        } else {
            destino = new Intent(this, BibliotecaActivity.class);
        }
        destino.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(destino);
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle estado) {
        estado.putInt("eco.mensagem.conclusao", mensagemAtual);
        super.onSaveInstanceState(estado);
    }

    @Override
    protected void onDestroy() {
        movimento.cancelAll();
        super.onDestroy();
    }
}
