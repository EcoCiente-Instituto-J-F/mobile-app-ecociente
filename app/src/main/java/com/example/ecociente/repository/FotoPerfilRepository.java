package com.example.ecociente.repository;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ecociente.model.ResultadoUploadFoto;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;

public class FotoPerfilRepository {

    private static final String TAG = "FotoPerfilEcoCiente";

    // Upload sem assinatura: o preset "ecociente_perfil" (Unsigned) limita pasta,
    // formatos e tamanho no painel do Cloudinary. O API secret nunca entra no app.
    private static final String CLOUD_NAME = "ncwfxyfi";
    private static final String UPLOAD_PRESET = "ecociente_perfil";
    // Preset (Unsigned) das fotos de descarte do feed, criado no painel do Cloudinary.
    private static final String UPLOAD_PRESET_FEED = "feed";
    private static final String URL_UPLOAD =
            "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

    private static final int LADO_MAXIMO_PX = 800;
    private static final int QUALIDADE_JPEG = 85;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    @NonNull
    public LiveData<ResultadoUploadFoto> enviar(@NonNull Context contexto, @NonNull Uri imagem) {
        return enviar(contexto, imagem, UPLOAD_PRESET, true);
    }

    // Foto de descarte do feed: preset próprio e a foto inteira, sem recorte quadrado.
    @NonNull
    public LiveData<ResultadoUploadFoto> enviarParaFeed(@NonNull Context contexto, @NonNull Uri imagem) {
        return enviar(contexto, imagem, UPLOAD_PRESET_FEED, false);
    }

    @NonNull
    private LiveData<ResultadoUploadFoto> enviar(
            @NonNull Context contexto, @NonNull Uri imagem, @NonNull String preset, boolean quadrada) {

        MutableLiveData<ResultadoUploadFoto> resultado = new MutableLiveData<>();

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    ResultadoUploadFoto resposta;

                    try {
                        byte[] jpeg = prepararImagem(contextoApp, imagem, quadrada);

                        resposta = ResultadoUploadFoto.sucesso(enviarParaCloudinary(jpeg, preset));

                    } catch (Exception erro) {
                        Log.e(TAG, "Erro ao enviar foto", erro);

                        resposta =
                                ResultadoUploadFoto.erro(
                                        "Não foi possível enviar a foto. Tente novamente.");
                    }

                    ResultadoUploadFoto respostaFinal = resposta;

                    PRINCIPAL.post(() -> resultado.setValue(respostaFinal));
                });

        return resultado;
    }

    // Glide já aplica a rotação do EXIF e reduz sem estourar memória; o recorte
    // quadrado combina com o avatar circular e deixa o upload bem menor.
    @NonNull
    private byte[] prepararImagem(@NonNull Context contexto, @NonNull Uri imagem, boolean quadrada)
            throws IOException, ExecutionException, InterruptedException {

        RequestBuilder<Bitmap> pedido =
                Glide.with(contexto).asBitmap().load(imagem).override(LADO_MAXIMO_PX, LADO_MAXIMO_PX);

        Bitmap bitmap =
                (quadrada ? pedido.centerCrop() : pedido.fitCenter())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .submit()
                        .get();

        try (ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, QUALIDADE_JPEG, saida);

            return saida.toByteArray();
        }
    }

    @NonNull
    private String enviarParaCloudinary(@NonNull byte[] jpeg, @NonNull String preset)
            throws IOException, JSONException {

        String limite = "EcoCiente" + System.currentTimeMillis();

        HttpURLConnection conexao = (HttpURLConnection) new URL(URL_UPLOAD).openConnection();

        try {
            conexao.setRequestMethod("POST");
            conexao.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + limite);
            conexao.setRequestProperty("Accept", "application/json");
            conexao.setDoOutput(true);
            conexao.setUseCaches(false);
            conexao.setConnectTimeout(20_000);
            conexao.setReadTimeout(60_000);

            try (OutputStream saida = conexao.getOutputStream()) {

                escrever(
                        saida,
                        "--" + limite + "\r\n"
                                + "Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n"
                                + preset + "\r\n");

                escrever(
                        saida,
                        "--" + limite + "\r\n"
                                + "Content-Disposition: form-data; name=\"file\"; filename=\"foto.jpg\"\r\n"
                                + "Content-Type: image/jpeg\r\n\r\n");

                saida.write(jpeg);

                escrever(saida, "\r\n--" + limite + "--\r\n");
            }

            int codigoResposta = conexao.getResponseCode();

            boolean deuCerto = codigoResposta >= 200 && codigoResposta < 300;

            String corpo = lerFluxo(deuCerto ? conexao.getInputStream() : conexao.getErrorStream());

            if (!deuCerto) {
                throw new IOException("Cloudinary respondeu " + codigoResposta + ": " + corpo);
            }

            return new JSONObject(corpo).getString("secure_url");

        } finally {
            conexao.disconnect();
        }
    }

    private void escrever(@NonNull OutputStream saida, @NonNull String texto) throws IOException {
        saida.write(texto.getBytes(StandardCharsets.UTF_8));
    }

    @NonNull
    private String lerFluxo(InputStream fluxo) throws IOException {

        if (fluxo == null) {
            return "";
        }

        try (InputStream entrada = fluxo;
                ByteArrayOutputStream saida = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int lidos;

            while ((lidos = entrada.read(buffer)) != -1) {
                saida.write(buffer, 0, lidos);
            }

            return saida.toString(StandardCharsets.UTF_8.name());
        }
    }
}
