package com.example.ecociente.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.ecociente.model.ResultadoApi;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;

// Fala com a ds-cadastro-api (Java/Postgres, repositório separado deste
// app). Escolhe o endpoint certo (comum/morador) a partir dos mesmos dados
// que já vão pro Firestore, pra não duplicar coleta de campos no cadastro.
public class CadastroExternoRepository {

    private static final String TAG = "CadastroExternaEcoCiente";

    private static final String URL_BASE = "https://ds-cadastro-api.onrender.com/api/cadastro";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    @NonNull
    public LiveData<ResultadoApi> cadastrar(
            @NonNull Map<String, Object> dadosUsuario, @NonNull String senha) {

        MutableLiveData<ResultadoApi> resultado = new MutableLiveData<>();

        EXECUTOR.execute(
                () -> {
                    ResultadoApi resposta;

                    try {
                        resposta = enviarCadastro(dadosUsuario, senha);

                    } catch (Exception erro) {
                        Log.e(TAG, "Erro ao cadastrar na API externa", erro);

                        resposta =
                                ResultadoApi.erro(
                                        "Não foi possível conectar ao servidor de cadastro.");
                    }

                    ResultadoApi respostaFinal = resposta;

                    PRINCIPAL.post(() -> resultado.setValue(respostaFinal));
                });

        return resultado;
    }

    @NonNull
    private ResultadoApi enviarCadastro(
            @NonNull Map<String, Object> dadosUsuario, @NonNull String senha) throws Exception {

        String tipoUsuario = String.valueOf(dadosUsuario.getOrDefault("tipoUsuario", "comum"));

        // Cooperativa precisa de CNPJ, que o cadastro do app ainda não
        // coleta - segue só pelo Firebase até esse campo existir.
        if ("cooperativa".equals(tipoUsuario)) {
            return ResultadoApi.sucesso();
        }

        String nome = String.valueOf(dadosUsuario.getOrDefault("nome", ""));
        String email = String.valueOf(dadosUsuario.getOrDefault("email", ""));
        String dataNascimento = converterDataParaIso(String.valueOf(dadosUsuario.getOrDefault("dataNascimento", "")));
        String cpf = String.valueOf(dadosUsuario.getOrDefault("cpf", ""));

        JSONObject corpo = new JSONObject();
        corpo.put("nomeUsuario", nome);
        corpo.put("email", email);
        corpo.put("senha", senha);
        corpo.put("dataNascimento", dataNascimento);
        corpo.put("cpf", cpf);

        String endpoint;

        if ("morador".equals(tipoUsuario)) {
            endpoint = URL_BASE + "/morador";

            corpo.put("codigoCondominio", dadosUsuario.getOrDefault("codigoCondominio", ""));

        } else {
            endpoint = URL_BASE + "/comum";

            corpo.put("cep", dadosUsuario.getOrDefault("cep", ""));
            corpo.put("numero", dadosUsuario.getOrDefault("numero", ""));
            corpo.put("complemento", dadosUsuario.getOrDefault("complemento", ""));
        }

        return enviar(endpoint, corpo);
    }

    @NonNull
    private ResultadoApi enviar(@NonNull String endpoint, @NonNull JSONObject corpo) throws Exception {

        HttpURLConnection conexao = (HttpURLConnection) new URL(endpoint).openConnection();

        try {
            conexao.setRequestMethod("POST");
            conexao.setRequestProperty("Content-Type", "application/json");
            conexao.setRequestProperty("Accept", "application/json");
            conexao.setDoOutput(true);
            conexao.setUseCaches(false);
            // Render free tier "dorme" a API após inatividade; medido ~72s
            // para o primeiro request acordar o serviço.
            conexao.setConnectTimeout(90_000);
            conexao.setReadTimeout(90_000);

            try (OutputStream saida = conexao.getOutputStream()) {
                saida.write(corpo.toString().getBytes(StandardCharsets.UTF_8));
            }

            int codigoResposta = conexao.getResponseCode();

            if (codigoResposta >= 200 && codigoResposta < 300) {
                return ResultadoApi.sucesso();
            }

            String corpoErro = lerFluxo(conexao.getErrorStream());

            return ResultadoApi.erro(extrairMensagemErro(corpoErro));

        } finally {
            conexao.disconnect();
        }
    }

    // A API devolve { "status", "codigoError", "details": [{ "field", "message" }] }
    // - mostramos a primeira mensagem de validação, se existir.
    @NonNull
    private String extrairMensagemErro(String corpoErro) {

        try {
            JSONObject json = new JSONObject(corpoErro);

            JSONArray detalhes = json.optJSONArray("details");

            if (detalhes != null && detalhes.length() > 0) {

                String mensagem = detalhes.getJSONObject(0).optString("message", "");

                if (!mensagem.isEmpty()) {
                    return mensagem;
                }
            }

        } catch (Exception erro) {
            Log.w(TAG, "Não foi possível interpretar o erro da API de cadastro", erro);
        }

        return "Não foi possível concluir seu cadastro. Tente novamente.";
    }

    // A tela guarda a data como dd/MM/yyyy, mas a API espera yyyy-MM-dd.
    @NonNull
    private String converterDataParaIso(@NonNull String dataBr) throws ParseException {

        SimpleDateFormat entrada = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        entrada.setLenient(false);

        SimpleDateFormat saida = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        return saida.format(entrada.parse(dataBr));
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
