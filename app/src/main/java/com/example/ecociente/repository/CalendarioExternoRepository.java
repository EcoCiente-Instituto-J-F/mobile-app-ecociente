package com.example.ecociente.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.model.ResultadoSolicitacoes;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.model.StatusAgendamento;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;

// ds-calendario-api: lista, cria e atualiza agendamentos conforme o perfil do JWT.
public class CalendarioExternoRepository {

    public interface Retorno {
        void aoConcluir(@NonNull ResultadoSolicitacoes resultado);
    }

    public interface RetornoStatus {
        void aoConcluir(@NonNull ResultadoApi resultado);
    }

    private static final String TAG = "CalendarioApi";
    private static final String MENSAGEM_PADRAO = "Não foi possível conectar ao servidor";
    private static final String MENSAGEM_SESSAO = "Sua sessão expirou. Entre novamente.";
    private static final String URL_AGENDAMENTOS =
            "https://ds-calendario-api.onrender.com/api/v1/agendamentos";
    private static final int TAMANHO_PAGINA = 10;
    private static final int TAMANHO_PAGINA_MES = 100;
    private static final int TIMEOUT_MS = 180_000;

    private final AutenticacaoExternaRepository autenticacao = new AutenticacaoExternaRepository();

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    public void listar(
            @NonNull Context contexto,
            @NonNull StatusAgendamento status,
            int pagina,
            @NonNull Retorno retorno) {

        String consulta = "?status=" + status.name() + "&page=" + pagina + "&size=" + TAMANHO_PAGINA;

        executar(contexto, URL_AGENDAMENTOS + consulta, retorno);
    }

    public void criar(
            @NonNull Context contexto,
            int condominioId,
            int cooperativaId,
            @NonNull String dataInicio,
            @NonNull RetornoStatus retorno) {
        JSONObject corpo = new JSONObject();
        try {
            corpo.put("condominioId", condominioId)
                    .put("cooperativaId", cooperativaId)
                    .put("dataInicio", dataInicio);
        } catch (Exception erro) {
            retorno.aoConcluir(ResultadoApi.erro(MENSAGEM_PADRAO));
            return;
        }
        executarAcao(contexto, "POST", URL_AGENDAMENTOS, corpo, retorno, true);
    }

    public void alterarStatus(
            @NonNull Context contexto,
            int id,
            @NonNull StatusAgendamento status,
            @NonNull RetornoStatus retorno) {
        JSONObject corpo = new JSONObject();
        try {
            corpo.put("status", status.name());
        } catch (Exception erro) {
            retorno.aoConcluir(ResultadoApi.erro(MENSAGEM_PADRAO));
            return;
        }
        executarAcao(contexto, "PATCH", URL_AGENDAMENTOS + "/" + id + "/status",
                corpo, retorno, false);
    }

    private void executarAcao(
            Context contexto,
            String metodo,
            String endereco,
            JSONObject corpo,
            RetornoStatus retorno,
            boolean criacao) {
        Context app = contexto.getApplicationContext();
        EXECUTOR.execute(() -> {
            ResultadoApi resultado;
            try {
                String token = autenticacao.tokenParaUso(app);
                if (token == null) {
                    resultado = ResultadoApi.erro(MENSAGEM_SESSAO);
                } else {
                    int codigo = enviarJson(metodo, endereco, token, corpo);
                    if (codigo == HttpURLConnection.HTTP_UNAUTHORIZED
                            || codigo == HttpURLConnection.HTTP_FORBIDDEN) {
                        String renovado = autenticacao.renovarToken(app);
                        if (renovado != null) {
                            codigo = enviarJson(metodo, endereco, renovado, corpo);
                        }
                    }
                    resultado = codigo >= 200 && codigo < 300
                            ? ResultadoApi.sucesso()
                            : ResultadoApi.erro(criacao
                                    ? mensagemDaCriacao(codigo) : mensagemDoCodigo(codigo));
                    if (codigo < 200 || codigo >= 300) {
                        Log.w(TAG, metodo + " agendamento respondeu HTTP " + codigo);
                    }
                }
            } catch (Exception erro) {
                Log.e(TAG, "Falha ao " + (criacao ? "criar solicitação" : "alterar status"), erro);
                resultado = ResultadoApi.erro(MENSAGEM_PADRAO);
            }
            ResultadoApi finalResultado = resultado;
            PRINCIPAL.post(() -> retorno.aoConcluir(finalResultado));
        });
    }

    private int enviarJson(String metodo, String endereco, String token, JSONObject corpo)
            throws Exception {
        HttpURLConnection conexao =
                (HttpURLConnection) new URL(endereco).openConnection();
        try {
            conexao.setRequestMethod(metodo);
            conexao.setRequestProperty("Content-Type", "application/json");
            conexao.setRequestProperty("Accept", "application/json");
            conexao.setRequestProperty("Authorization", "Bearer " + token);
            conexao.setDoOutput(true);
            conexao.setConnectTimeout(TIMEOUT_MS);
            conexao.setReadTimeout(TIMEOUT_MS);
            try (OutputStream saida = conexao.getOutputStream()) {
                saida.write(corpo.toString().getBytes(StandardCharsets.UTF_8));
            }
            return conexao.getResponseCode();
        } finally {
            conexao.disconnect();
        }
    }

    @NonNull
    private String mensagemDaCriacao(int codigo) {
        switch (codigo) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                return MENSAGEM_SESSAO;
            case HttpURLConnection.HTTP_FORBIDDEN:
                return "Sua conta não tem permissão para solicitar coleta para este condomínio.";
            case HttpURLConnection.HTTP_BAD_REQUEST:
                return "Confira a data e o horário escolhidos e tente novamente.";
            case HttpURLConnection.HTTP_NOT_FOUND:
                return "O condomínio ou a cooperativa não foi encontrado.";
            case HttpURLConnection.HTTP_CONFLICT:
                return "Não foi possível registrar esta solicitação porque há um conflito de agendamento.";
            default:
                return "Não foi possível enviar a solicitação (HTTP " + codigo + ").";
        }
    }

    // O Spring responde 403 sem corpo quando o JWT é inválido ou expirou (o 401 do filtro cai
    // no /error, que exige autenticação). Já o 403 de perfil não autorizado traz um corpo JSON.
    private boolean tokenRejeitado(int codigo, @NonNull HttpURLConnection conexao) throws IOException {

        if (codigo == HttpURLConnection.HTTP_UNAUTHORIZED) {
            return true;
        }

        if (codigo != HttpURLConnection.HTTP_FORBIDDEN) {
            return false;
        }

        InputStream corpo = conexao.getErrorStream();

        return corpo == null || corpo.read() == -1;
    }

    @NonNull
    private String mensagemDoCodigo(int codigo) {

        switch (codigo) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                return MENSAGEM_SESSAO;

            case HttpURLConnection.HTTP_FORBIDDEN:
                return "Você não pode responder esta solicitação.";

            case HttpURLConnection.HTTP_NOT_FOUND:
                return "Solicitação não encontrada.";

            case HttpURLConnection.HTTP_CONFLICT:
                return "Esta solicitação já foi respondida.";

            default:
                return MENSAGEM_PADRAO;
        }
    }

    public void proxima(@NonNull Context contexto, @NonNull Retorno retorno) {

        executar(contexto, URL_AGENDAMENTOS + "/proxima", retorno);
    }

    public void doMes(@NonNull Context contexto, int ano, int mes, @NonNull Retorno retorno) {

        Calendar inicio = Calendar.getInstance();
        inicio.clear();
        inicio.set(ano, mes, 1, 0, 0, 0);

        Calendar fim = (Calendar) inicio.clone();
        fim.add(Calendar.MONTH, 1);
        fim.add(Calendar.SECOND, -1);

        doPeriodo(contexto, inicio, fim, retorno);
    }

    public void doPeriodo(
            @NonNull Context contexto,
            @NonNull Calendar inicio,
            @NonNull Calendar fim,
            @NonNull Retorno retorno) {

        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

        String consulta =
                "?dataInicio="
                        + formato.format(inicio.getTime())
                        + "&dataFim="
                        + formato.format(fim.getTime())
                        + "&size="
                        + TAMANHO_PAGINA_MES;

        executar(contexto, URL_AGENDAMENTOS + consulta, retorno);
    }

    private void executar(
            @NonNull Context contexto, @NonNull String endereco, @NonNull Retorno retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    ResultadoSolicitacoes resultado;

                    try {
                        resultado = buscarComRenovacao(contextoApp, endereco);

                    } catch (Exception erro) {
                        Log.e(TAG, "Falha ao consultar agendamentos", erro);

                        resultado = ResultadoSolicitacoes.erro();
                    }

                    ResultadoSolicitacoes resultadoFinal = resultado;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resultadoFinal));
                });
    }

    @NonNull
    private ResultadoSolicitacoes buscarComRenovacao(Context contexto, String endereco)
            throws Exception {

        String token = autenticacao.tokenParaUso(contexto);

        if (token == null) {
            return ResultadoSolicitacoes.sessaoExpirada();
        }

        ResultadoSolicitacoes resultado = buscar(token, endereco);

        if (resultado.getTipo() == ResultadoSolicitacoes.Tipo.SESSAO_EXPIRADA) {
            String novoToken = autenticacao.renovarToken(contexto);

            if (novoToken != null) {
                resultado = buscar(novoToken, endereco);
            }
        }

        return resultado;
    }

    @NonNull
    private ResultadoSolicitacoes buscar(String token, String endereco) throws Exception {

        HttpURLConnection conexao = (HttpURLConnection) new URL(endereco).openConnection();

        try {
            conexao.setRequestMethod("GET");
            conexao.setRequestProperty("Accept", "application/json");
            conexao.setRequestProperty("Authorization", "Bearer " + token);
            conexao.setConnectTimeout(TIMEOUT_MS);
            conexao.setReadTimeout(TIMEOUT_MS);

            int codigo = conexao.getResponseCode();

            if (tokenRejeitado(codigo, conexao)) {
                Log.w(TAG, "Token rejeitado pela API (HTTP " + codigo + ")");

                return ResultadoSolicitacoes.sessaoExpirada();
            }

            if (codigo == HttpURLConnection.HTTP_NOT_FOUND) {
                return ResultadoSolicitacoes.sucesso(new ArrayList<>(), true);
            }

            if (codigo < 200 || codigo >= 300) {
                Log.w(TAG, "Resposta HTTP " + codigo);

                return ResultadoSolicitacoes.erro();
            }

            return converter(new JSONObject(lerFluxo(conexao.getInputStream())));

        } finally {
            conexao.disconnect();
        }
    }

    @NonNull
    private ResultadoSolicitacoes converter(@NonNull JSONObject resposta) {

        List<Solicitacao> itens = new ArrayList<>();

        JSONArray conteudo = resposta.optJSONArray("content");

        if (conteudo == null) {
            itens.add(solicitacao(resposta));

            return ResultadoSolicitacoes.sucesso(itens, true);
        }

        for (int i = 0; i < conteudo.length(); i++) {

            JSONObject item = conteudo.optJSONObject(i);

            if (item != null) {
                itens.add(solicitacao(item));
            }
        }

        return ResultadoSolicitacoes.sucesso(itens, resposta.optBoolean("last", true));
    }

    @NonNull
    private Solicitacao solicitacao(@NonNull JSONObject item) {

        return new Solicitacao(
                item.optInt("id"),
                item.optInt("condominioId"),
                item.optInt("cooperativaId"),
                texto(item, "dataInicio"),
                texto(item, "dataFim"),
                StatusAgendamento.daApi(item.optString("statusAgendamento")),
                item.optBoolean("possuiRecorrencia"));
    }

    @Nullable
    private String texto(@NonNull JSONObject item, @NonNull String campo) {
        return item.isNull(campo) ? null : item.optString(campo);
    }

    @NonNull
    private String lerFluxo(@NonNull InputStream fluxo) throws IOException {

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
