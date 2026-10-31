package com.example.ecociente.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;

// Consulta pública de CEP (ViaCEP). Serve para manter cidade e estado do perfil iguais ao que a
// ds-mobile-api grava quando o CEP muda, já que ela só responde 204 e não devolve o endereço.
public class ConsultaCepRepository {

    private static final String TAG = "ConsultaCep";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    public static final class Localidade {
        private final String cidade;
        private final String estado;

        Localidade(@NonNull String cidade, @NonNull String estado) {
            this.cidade = cidade;
            this.estado = estado;
        }

        @NonNull
        public String getCidade() {
            return cidade;
        }

        @NonNull
        public String getEstado() {
            return estado;
        }
    }

    public interface Retorno {
        void aoConcluir(@Nullable Localidade localidade);
    }

    public void buscar(@NonNull String cep, @NonNull Retorno retorno) {

        String digitos = cep.replaceAll("\\D", "");

        if (digitos.length() != 8) {
            retorno.aoConcluir(null);
            return;
        }

        EXECUTOR.execute(
                () -> {
                    Localidade localidade = null;

                    try {
                        localidade = consultar(digitos);
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível consultar o CEP", erro);
                    }

                    Localidade resultado = localidade;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resultado));
                });
    }

    @Nullable
    private Localidade consultar(@NonNull String digitos) throws IOException, JSONException {

        HttpURLConnection conexao =
                (HttpURLConnection)
                        new URL("https://viacep.com.br/ws/" + digitos + "/json/").openConnection();

        try {
            conexao.setRequestProperty("Accept", "application/json");
            conexao.setConnectTimeout(15_000);
            conexao.setReadTimeout(15_000);

            if (conexao.getResponseCode() != 200) {
                return null;
            }

            JSONObject json = new JSONObject(LeituraHttp.texto(conexao.getInputStream()));

            String cidade = json.optString("localidade", "");
            String estado = json.optString("uf", "");

            if (json.optBoolean("erro", false) || cidade.isEmpty() || estado.isEmpty()) {
                return null;
            }

            return new Localidade(cidade, estado);

        } finally {
            conexao.disconnect();
        }
    }
}
