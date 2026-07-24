package com.example.ecociente.controller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecociente.R;

import java.util.Random;

public class Carregamento extends AppCompatActivity {
    private static final int[][] FAIXAS_DE_PROGRESSO = {
            {1, 14},
            {15, 24},
            {25, 33},
            {34, 49},
            {50, 64},
            {65, 79},
            {80, 94},
            {95, 99},
            {100, 100}
    };

    private static final int[] SEQUENCIA_DAS_PLANTAS = {0, 1, 2, 3, 2, 1};

    private static final long TEMPO_ENTRE_PLANTAS = 1_500L;

    private static final int TEMPO_MINIMO_PROGRESSO = 1_200;
    private static final int TEMPO_MAXIMO_PROGRESSO = 1_600;

    private final Handler manipulador = new Handler(Looper.getMainLooper());

    private final Random sorteador = new Random();

    private TextView textoPorcentagem;

    private FrameLayout areaPreenchimentoBarra;
    private View preenchimentoBarra;

    private ImageView imagemPlantaPequena;
    private ImageView imagemPlantaMedia;
    private ImageView imagemPlantaGrande;

    private int indiceFaixaAtual = 0;
    private int indiceSequenciaPlantas = 0;
    private int porcentagemAtual = 0;

    private boolean proximaTelaAberta = false;

    private final Runnable animarPlantas = new Runnable() {
        @Override
        public void run() {

            if (isFinishing() || proximaTelaAberta) {
                return;
            }

            indiceSequenciaPlantas++;

            if (indiceSequenciaPlantas >= SEQUENCIA_DAS_PLANTAS.length) {
                indiceSequenciaPlantas = 0;
            }

            int quantidadeDePlantas = SEQUENCIA_DAS_PLANTAS[indiceSequenciaPlantas];

            atualizarPlantas(quantidadeDePlantas);

            manipulador.postDelayed(this, TEMPO_ENTRE_PLANTAS
            );
        }
    };

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        setContentView(R.layout.activity_carregamento);

        buscarComponentes();

        iniciarBarraDeCarregamento();
        iniciarAnimacaoDasPlantas();
    }

    private void buscarComponentes() {

        textoPorcentagem = findViewById(R.id.textoPorcentagem);

        areaPreenchimentoBarra = findViewById(R.id.areaPreenchimentoBarra);

        preenchimentoBarra = findViewById(R.id.preenchimentoBarra);

        imagemPlantaPequena = findViewById(R.id.imagemPlantaPequena);

        imagemPlantaMedia = findViewById(R.id.imagemPlantaMedia);

        imagemPlantaGrande = findViewById(R.id.imagemPlantaGrande);
    }

    private void iniciarBarraDeCarregamento() {
        atualizarTela(0);

        manipulador.postDelayed(this::avancarCarregamento, 900L);
    }

    private void iniciarAnimacaoDasPlantas() {
        indiceSequenciaPlantas = 0;

        atualizarPlantas(SEQUENCIA_DAS_PLANTAS[indiceSequenciaPlantas]);

        manipulador.postDelayed(animarPlantas, TEMPO_ENTRE_PLANTAS);
    }

    private void avancarCarregamento() {
        if (isFinishing() || proximaTelaAberta) {
            return;
        }

        if (indiceFaixaAtual >= FAIXAS_DE_PROGRESSO.length) {
            abrirSplashScreen();
            return;
        }

        int valorMinimo = FAIXAS_DE_PROGRESSO[indiceFaixaAtual][0];

        int valorMaximo = FAIXAS_DE_PROGRESSO[indiceFaixaAtual][1];

        int novaPorcentagem = sortearNumero(valorMinimo, valorMaximo);

        atualizarTela(novaPorcentagem);

        indiceFaixaAtual++;

        if (novaPorcentagem == 100) {
            manipulador.postDelayed(this::abrirSplashScreen, 700L);
            return;
        }

        int tempoProximaAtualizacao = sortearNumero(TEMPO_MINIMO_PROGRESSO, TEMPO_MAXIMO_PROGRESSO);

        manipulador.postDelayed(this::avancarCarregamento, tempoProximaAtualizacao);
    }

    private int sortearNumero(int valorMinimo, int valorMaximo) {
        if (valorMinimo == valorMaximo) {
            return valorMinimo;
        }

        return valorMinimo + sorteador.nextInt(valorMaximo - valorMinimo + 1);
    }

    private void atualizarTela(int porcentagem) {
        porcentagemAtual = porcentagem;

        textoPorcentagem.setText(
                getString(R.string.carregando_porcentagem, porcentagem)
        );

        atualizarLarguraDaBarra();
    }

    private void atualizarLarguraDaBarra() {
        areaPreenchimentoBarra.post(() -> {
            int larguraDisponivel = areaPreenchimentoBarra.getWidth() - areaPreenchimentoBarra.getPaddingLeft() - areaPreenchimentoBarra.getPaddingRight();

            int novaLargura = Math.round(larguraDisponivel * (porcentagemAtual / 100f));

            if (porcentagemAtual > 0) {
                int larguraMinima = areaPreenchimentoBarra.getHeight() - areaPreenchimentoBarra.getPaddingTop() - areaPreenchimentoBarra.getPaddingBottom();

                novaLargura = Math.max(novaLargura, larguraMinima);
            }

            novaLargura = Math.min(novaLargura, larguraDisponivel);

            FrameLayout.LayoutParams parametros = (FrameLayout.LayoutParams) preenchimentoBarra.getLayoutParams();

            parametros.width = novaLargura;

            preenchimentoBarra.setLayoutParams(parametros);
        });
    }

    private void atualizarPlantas(int quantidadeDePlantas) {
        definirVisibilidadeDaPlanta(imagemPlantaPequena, quantidadeDePlantas >= 1);

        definirVisibilidadeDaPlanta(imagemPlantaMedia, quantidadeDePlantas >= 2);

        definirVisibilidadeDaPlanta(imagemPlantaGrande, quantidadeDePlantas >= 3);
    }

    private void definirVisibilidadeDaPlanta(ImageView planta, boolean deveAparecer) {
        planta.animate().cancel();

        if (deveAparecer) {
            if (planta.getVisibility() == View.VISIBLE) {
                return;
            }

            planta.setVisibility(View.VISIBLE);
            planta.setAlpha(0f);
            planta.setScaleX(0.75f);
            planta.setScaleY(0.75f);

            planta.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(250L).start();
        } else {
            if (planta.getVisibility() != View.VISIBLE) {
                return;
            }

            planta.animate().alpha(0f).scaleX(0.80f).scaleY(0.80f).setDuration(250L).withEndAction(() -> {
                        planta.setVisibility(View.INVISIBLE);
                        planta.setAlpha(1f);
                        planta.setScaleX(1f);
                        planta.setScaleY(1f);
                    }).start();
        }
    }

    private void abrirSplashScreen() {
        if (proximaTelaAberta || isFinishing()) {
            return;
        }

        proximaTelaAberta = true;

        manipulador.removeCallbacksAndMessages(null);

        Intent rota = new Intent(Carregamento.this, SplashScreen.class);

        startActivity(rota);
        finish();
    }

    @Override
    protected void onDestroy() {
        manipulador.removeCallbacksAndMessages(null);

        if (imagemPlantaPequena != null) {
            imagemPlantaPequena.animate().cancel();
        }

        if (imagemPlantaMedia != null) {
            imagemPlantaMedia.animate().cancel();
        }

        if (imagemPlantaGrande != null) {
            imagemPlantaGrande.animate().cancel();
        }

        super.onDestroy();
    }
}