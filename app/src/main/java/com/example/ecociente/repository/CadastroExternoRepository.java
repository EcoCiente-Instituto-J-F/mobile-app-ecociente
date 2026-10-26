package com.example.ecociente.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.model.TipoUsuario;
import java.io.IOException;
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
import org.json.JSONException;
import org.json.JSONObject;

// Fala com a ds-cadastro-api (Java/Postgres, repositório separado deste
// app). Escolhe o endpoint certo (comum/morador) a partir dos mesmos dados
// que já vão pro Firestore, pra não duplicar coleta de campos no cadastro.
public class CadastroExternoRepository {

    private static final String TAG = "CadastroExternaEcoCiente";

    private static final String URL_BASE = ConfiguracaoServidores.URL_CADASTRO;

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
            @NonNull Map<String, Object> dadosUsuario, @NonNull String senha) throws IOException, JSONException, ParseException {

        switch (texto(dadosUsuario, "tipoUsuario", TipoUsuario.COMUM)) {
            case TipoUsuario.COOPERATIVA:
                return enviar(URL_BASE + "/cooperativa", corpoCooperativa(dadosUsuario, senha));

            case TipoUsuario.MORADOR:
                return enviar(URL_BASE + "/morador", corpoMorador(dadosUsuario, senha));

            default:
                return enviar(URL_BASE + "/comum", corpoComum(dadosUsuario, senha));
        }
    }

    @NonNull
    private JSONObject corpoBasePessoa(
            @NonNull Map<String, Object> dados, @NonNull String senha) throws IOException, JSONException, ParseException {

        return new JSONObject()
                .put("nomeUsuario", texto(dados, "nome"))
                .put("email", email(dados, "email"))
                .put("senha", senha)
                .put("dataNascimento", converterDataParaIso(texto(dados, "dataNascimento")))
                .put("cpf", texto(dados, "cpf"));
    }

    @NonNull
    private JSONObject corpoComum(
            @NonNull Map<String, Object> dados, @NonNull String senha) throws IOException, JSONException, ParseException {

        return corpoBasePessoa(dados, senha)
                .put("cep", texto(dados, "cep"))
                .put("numero", texto(dados, "numero"))
                .put("complemento", texto(dados, "complemento"));
    }

    @NonNull
    private JSONObject corpoMorador(
            @NonNull Map<String, Object> dados, @NonNull String senha) throws IOException, JSONException, ParseException {

        return corpoBasePessoa(dados, senha).put("codigoCondominio", texto(dados, "codigoCondominio"));
    }

    @NonNull
    private JSONObject corpoCooperativa(
            @NonNull Map<String, Object> dados, @NonNull String senha) throws IOException, JSONException, ParseException {

        return new JSONObject()
                .put("nomeResponsavel", texto(dados, "nome"))
                .put("email", email(dados, "email"))
                .put("senha", senha)
                .put("nomeCooperativa", texto(dados, "nomeCooperativa"))
                .put("cnpj", apenasDigitos(texto(dados, "cnpj")))
                .put("emailCooperativa", email(dados, "emailCooperativa"))
                .put("telefone", apenasDigitos(texto(dados, "telefone")))
                .put("cep", texto(dados, "cep"))
                .put("numero", texto(dados, "numero"))
                .put("complemento", texto(dados, "complemento"));
    }

    @NonNull
    private String texto(@NonNull Map<String, Object> dados, @NonNull String chave) {
        return texto(dados, chave, "");
    }

    @NonNull
    private String texto(
            @NonNull Map<String, Object> dados, @NonNull String chave, @NonNull String padrao) {
        return String.valueOf(dados.getOrDefault(chave, padrao));
    }

    @NonNull
    private String email(@NonNull Map<String, Object> dados, @NonNull String chave) {
        return texto(dados, chave).trim().toLowerCase(Locale.ROOT);
    }

    @NonNull
    private String apenasDigitos(@NonNull String valor) {
        return valor.replaceAll("\\D", "");
    }

    @NonNull
    private ResultadoApi enviar(@NonNull String endpoint, @NonNull JSONObject corpo) throws IOException {

        HttpURLConnection conexao = (HttpURLConnection) new URL(endpoint).openConnection();

        try {
            conexao.setRequestMethod("POST");
            conexao.setRequestProperty("Content-Type", "application/json");
            conexao.setRequestProperty("Accept", "application/json");
            conexao.setDoOutput(true);
            conexao.setUseCaches(false);
            conexao.setConnectTimeout(ConfiguracaoServidores.TIMEOUT_CONEXAO_MS);
            conexao.setReadTimeout(ConfiguracaoServidores.TIMEOUT_RESPOSTA_CADASTRO_MS);

            try (OutputStream saida = conexao.getOutputStream()) {
                saida.write(corpo.toString().getBytes(StandardCharsets.UTF_8));
            }

            int codigoResposta = conexao.getResponseCode();

            if (codigoResposta >= 200 && codigoResposta < 300) {
                return ResultadoApi.sucesso();
            }

            String corpoErro = LeituraHttp.texto(conexao.getErrorStream());

            String mensagem = extrairMensagemErro(corpoErro);
            if (codigoResposta == 404 && corpoErro.contains("EMAIL_ALREADY_EXISTS")) {
                mensagem = "Este e-mail ja existe no PostgreSQL. Entre pela tela de Login para sincronizar o Firebase. Nao repita o cadastro.";
            }
            return ResultadoApi.erro(mensagem);

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

        } catch (JSONException erro) {
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
}
