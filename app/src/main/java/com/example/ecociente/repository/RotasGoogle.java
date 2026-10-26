package com.example.ecociente.repository;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.ecociente.R;
import com.example.ecociente.model.SindicoCooperativa;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Distância de carro, da localização do aparelho até o endereço de cada cooperativa, pela Routes API
// do Google (computeRouteMatrix). A chave vem de MAPS_API_KEY no local.properties; sem ela, ou se a
// chamada falhar, devolve um mapa vazio e quem chamou usa outra forma de calcular.
public final class RotasGoogle {
    private static final String TAG = "RotasGoogle";
    private static final String URL_MATRIZ =
            "https://routes.googleapis.com/distanceMatrix/v2:computeRouteMatrix";
    // A API aceita no máximo 50 endereços por chamada.
    private static final int DESTINOS_POR_CHAMADA = 40;

    private RotasGoogle() {}

    // Bloqueante: chamar fora da thread principal.
    @NonNull
    public static Map<Integer, Float> distanciasEmMetros(
            @NonNull Context contexto,
            @NonNull Location origem,
            @NonNull List<SindicoCooperativa> cooperativas) {

        Map<Integer, Float> distancias = new HashMap<>();
        String chave = contexto.getString(R.string.maps_api_key).trim();

        if (chave.isEmpty()) {
            return distancias;
        }

        for (int inicio = 0; inicio < cooperativas.size(); inicio += DESTINOS_POR_CHAMADA) {
            List<SindicoCooperativa> lote =
                    cooperativas.subList(inicio, Math.min(inicio + DESTINOS_POR_CHAMADA, cooperativas.size()));
            try {
                preencher(contexto, chave, origem, lote, distancias);
            } catch (IOException | JSONException erro) {
                Log.w(TAG, "Routes API indisponível: " + erro.getClass().getSimpleName());
                return new HashMap<>();
            }
        }

        return distancias;
    }

    private static void preencher(
            Context contexto,
            String chave,
            Location origem,
            List<SindicoCooperativa> lote,
            Map<Integer, Float> distancias) throws IOException, JSONException {

        HttpURLConnection conexao = (HttpURLConnection) new URL(URL_MATRIZ).openConnection();

        try {
            conexao.setRequestMethod("POST");
            conexao.setConnectTimeout(15000);
            conexao.setReadTimeout(20000);
            conexao.setDoOutput(true);
            conexao.setRequestProperty("Content-Type", "application/json");
            conexao.setRequestProperty("X-Goog-Api-Key", chave);
            conexao.setRequestProperty("X-Goog-FieldMask", "originIndex,destinationIndex,distanceMeters,condition");
            // Chaves restritas a apps Android exigem identificar o app.
            conexao.setRequestProperty("X-Android-Package", contexto.getPackageName());
            String assinatura = assinaturaSha1(contexto);
            if (assinatura != null) {
                conexao.setRequestProperty("X-Android-Cert", assinatura);
            }

            try (OutputStream saida = conexao.getOutputStream()) {
                saida.write(montarCorpo(origem, lote).toString().getBytes(StandardCharsets.UTF_8));
            }

            if (conexao.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP " + conexao.getResponseCode());
            }

            JSONArray elementos = new JSONArray(LeituraHttp.texto(conexao.getInputStream()));

            for (int i = 0; i < elementos.length(); i++) {
                JSONObject elemento = elementos.getJSONObject(i);
                if (!"ROUTE_EXISTS".equals(elemento.optString("condition"))) {
                    continue;
                }
                int indice = elemento.getInt("destinationIndex");
                distancias.put(lote.get(indice).getId(), (float) elemento.getDouble("distanceMeters"));
            }

        } finally {
            conexao.disconnect();
        }
    }

    private static JSONObject montarCorpo(Location origem, List<SindicoCooperativa> lote)
            throws JSONException {

        JSONObject coordenadas =
                new JSONObject().put("latitude", origem.getLatitude()).put("longitude", origem.getLongitude());
        JSONObject ponto = new JSONObject().put("location", new JSONObject().put("latLng", coordenadas));

        JSONArray destinos = new JSONArray();
        for (SindicoCooperativa cooperativa : lote) {
            destinos.put(new JSONObject().put("waypoint",
                    new JSONObject().put("address", cooperativa.getEnderecoCompleto())));
        }

        return new JSONObject()
                .put("origins", new JSONArray().put(new JSONObject().put("waypoint", ponto)))
                .put("destinations", destinos)
                .put("travelMode", "DRIVE");
    }


    @SuppressWarnings("deprecation") // GET_SIGNATURES é a única opção antes do Android 9
    private static String assinaturaSha1(Context contexto) {
        try {
            PackageManager gerenciador = contexto.getPackageManager();
            Signature assinatura;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageInfo pacote = gerenciador.getPackageInfo(
                        contexto.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
                assinatura = pacote.signingInfo.getApkContentsSigners()[0];
            } else {
                PackageInfo pacote = gerenciador.getPackageInfo(
                        contexto.getPackageName(), PackageManager.GET_SIGNATURES);
                assinatura = pacote.signatures[0];
            }

            StringBuilder hexa = new StringBuilder();
            for (byte valor : MessageDigest.getInstance("SHA-1").digest(assinatura.toByteArray())) {
                hexa.append(String.format("%02X", valor));
            }
            return hexa.toString();

        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException | RuntimeException erro) {
            return null;
        }
    }
}
