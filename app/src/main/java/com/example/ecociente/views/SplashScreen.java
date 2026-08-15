package com.example.ecociente.views;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecociente.R;

public class SplashScreen extends AppCompatActivity {
    private static final String MARCADOR_LOG = "SplashScreen";
    private static final float FATOR_ZOOM_VIDEO = 2.10f;
    private static final float LARGURA_ORIGINAL_VIDEO = 894f;
    private static final float ALTURA_ORIGINAL_VIDEO = 496f;
    private static final long TEMPO_APOS_VIDEO = 1_500L;
    private static final long TEMPO_MAXIMO_COBERTURA = 1_000L;
    private final Handler manipulador = new Handler(Looper.getMainLooper());
    private FrameLayout areaVideo;
    private VideoView videoAnimacaoLogo;
    private View coberturaVideo;
    private boolean carregamentoAberto = false;
    private boolean coberturaRemovida = false;
    private final Runnable removerCoberturaPorSeguranca = this::removerCoberturaVideo;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        setContentView(R.layout.activity_splashscreen);

        buscarComponentes();

        areaVideo.post(() -> {ajustarTamanhoDoVideo();reproduzirVideo();});
    }

    private void buscarComponentes() {
        areaVideo = findViewById(R.id.areaVideo);
        videoAnimacaoLogo = findViewById(R.id.videoAnimacaoLogo);
        coberturaVideo = findViewById(R.id.coberturaVideo);
    }

    private void ajustarTamanhoDoVideo() {
        int larguraVisivelDaTela = areaVideo.getWidth();

        int larguraAmpliadaDoVideo = Math.round(larguraVisivelDaTela * FATOR_ZOOM_VIDEO);

        int alturaAmpliadaDoVideo = Math.round(larguraAmpliadaDoVideo * ALTURA_ORIGINAL_VIDEO / LARGURA_ORIGINAL_VIDEO);

        FrameLayout.LayoutParams parametros = new FrameLayout.LayoutParams(larguraAmpliadaDoVideo, alturaAmpliadaDoVideo);

        parametros.gravity = Gravity.CENTER;

        videoAnimacaoLogo.setLayoutParams(parametros);
    }

    private void reproduzirVideo() {
        Uri caminhoVideo = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.logo_video_splash);

        coberturaVideo.setVisibility(View.VISIBLE);
        coberturaVideo.setAlpha(1f);

        videoAnimacaoLogo.setOnPreparedListener(this::prepararReprodutor);

        videoAnimacaoLogo.setOnCompletionListener(reprodutor -> manipulador.postDelayed(this::abrirCarregamento, TEMPO_APOS_VIDEO));

        videoAnimacaoLogo.setOnErrorListener((reprodutor, codigoErro, detalheErro) -> {Log.e(MARCADOR_LOG, "Erro ao reproduzir o vídeo. Código: " + codigoErro + " | Detalhe: " + detalheErro);

                    manipulador.postDelayed(this::abrirCarregamento, 1_000L);

                    return true;
                }
        );

        videoAnimacaoLogo.setVideoURI(caminhoVideo);
        videoAnimacaoLogo.requestFocus();
    }

    private void prepararReprodutor(MediaPlayer reprodutor) {
        reprodutor.setVolume(0f, 0f);
        reprodutor.setLooping(false);

        reprodutor.setOnInfoListener((mediaPlayer, informacao, detalhe) -> {
                    if (informacao == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        removerCoberturaVideo();
                        return true;
                    }

                    return false;
                }
        );

        videoAnimacaoLogo.start();

        manipulador.postDelayed(
                removerCoberturaPorSeguranca,
                TEMPO_MAXIMO_COBERTURA
        );
    }

    private void removerCoberturaVideo() {
        if (coberturaRemovida || isFinishing()) {
            return;
        }

        coberturaRemovida = true;

        manipulador.removeCallbacks(removerCoberturaPorSeguranca);

        coberturaVideo
                .animate()
                .alpha(0f)
                .setDuration(120L)
                .withEndAction(() -> {
                    coberturaVideo.setVisibility(View.GONE);
                    coberturaVideo.setAlpha(1f);
                })
                .start();
    }

    private void abrirCarregamento() {
        if (carregamentoAberto || isFinishing()) {
            return;
        }

        carregamentoAberto = true;

        Intent rota = new Intent(
                SplashScreen.this,
                Carregamento.class
        );

        startActivity(rota);
        finish();
    }

    @Override
    protected void onDestroy() {
        manipulador.removeCallbacksAndMessages(null);

        if (coberturaVideo != null) {
            coberturaVideo.animate().cancel();
        }

        if (videoAnimacaoLogo != null) {
            videoAnimacaoLogo.setOnPreparedListener(null);
            videoAnimacaoLogo.setOnCompletionListener(null);
            videoAnimacaoLogo.setOnErrorListener(null);

            videoAnimacaoLogo.stopPlayback();
        }

        super.onDestroy();
    }
}
