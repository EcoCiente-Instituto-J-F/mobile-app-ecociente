package com.example.ecociente.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;

public class NotificacaoMotivacionalRepository {

    private static final String TAG =
            "NotificacaoHome";

    private static final String URL_NOTIFICACAO =
            "https://mobile-app-ecociente.vercel.app/api/notificacaoMotivacional";

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL =
            new Handler(
                    Looper.getMainLooper()
            );

    /*
     * Guarda o ID da última notificação recebida
     * durante esta sessão da Home.
     *
     * Na próxima chamada enviamos esse ID para
     * o backend evitar repetir a mesma mensagem.
     */
    @Nullable
    private String ultimoIdNotificacao;

    public interface Retorno {

        void aoConcluir(
                @Nullable String mensagem
        );
    }

    public void buscar(
            @NonNull Retorno retorno
    ) {

        EXECUTOR.execute(
                () -> {

                    String mensagem = null;

                    try {

                        mensagem =
                                buscarMensagem();

                    } catch (Exception erro) {

                        Log.w(
                                TAG,
                                "Não foi possível atualizar a mensagem motivacional",
                                erro
                        );
                    }

                    String mensagemFinal =
                            mensagem;

                    PRINCIPAL.post(
                            () ->
                                    retorno.aoConcluir(
                                            mensagemFinal
                                    )
                    );
                }
        );
    }

    @NonNull
    private String buscarMensagem()
            throws Exception {

        String endereco =
                criarUrlRequisicao();

        HttpURLConnection conexao =
                (HttpURLConnection)
                        new URL(
                                endereco
                        ).openConnection();

        try {

            conexao.setRequestMethod(
                    "GET"
            );

            conexao.setRequestProperty(
                    "Accept",
                    "application/json"
            );

            /*
             * Evita cache local da chamada HTTP.
             */
            conexao.setUseCaches(
                    false
            );

            conexao.setConnectTimeout(
                    10_000
            );

            conexao.setReadTimeout(
                    10_000
            );

            int codigoResposta =
                    conexao.getResponseCode();

            if (
                    codigoResposta < 200
                            ||
                            codigoResposta >= 300
            ) {

                throw new IOException(
                        "Resposta HTTP "
                                +
                                codigoResposta
                );
            }

            JSONObject resposta =
                    new JSONObject(
                            lerFluxo(
                                    conexao.getInputStream()
                            )
                    );

            String mensagem =
                    resposta
                            .optString(
                                    "mensagem",
                                    ""
                            )
                            .trim();

            if (mensagem.isEmpty()) {

                throw new IOException(
                        "Resposta sem mensagem"
                );
            }

            /*
             * Salvamos o ID para enviar na próxima
             * requisição e evitar repetir.
             */
            Object id =
                    resposta.opt(
                            "id"
                    );

            if (
                    id != null
                            &&
                            id != JSONObject.NULL
            ) {

                String idConvertido =
                        String.valueOf(
                                id
                        ).trim();

                if (!idConvertido.isEmpty()) {

                    ultimoIdNotificacao =
                            idConvertido;
                }
            }

            return mensagem;

        } finally {

            conexao.disconnect();
        }
    }

    @NonNull
    private String criarUrlRequisicao()
            throws Exception {

        /*
         * Primeira execução:
         *
         * /api/notificacaoMotivacional
         */
        if (
                ultimoIdNotificacao == null
                        ||
                        ultimoIdNotificacao.isEmpty()
        ) {

            return URL_NOTIFICACAO;
        }

        /*
         * Próximas:
         *
         * /api/notificacaoMotivacional?excluirId=3
         */
        String idCodificado =
                URLEncoder.encode(
                        ultimoIdNotificacao,
                        StandardCharsets.UTF_8.name()
                );

        return URL_NOTIFICACAO
                +
                "?excluirId="
                +
                idCodificado;
    }

    @NonNull
    private String lerFluxo(
            @NonNull InputStream fluxo
    ) throws IOException {

        try (
                InputStream entrada =
                        fluxo;

                ByteArrayOutputStream saida =
                        new ByteArrayOutputStream()
        ) {

            byte[] buffer =
                    new byte[1024];

            int lidos;

            while (
                    (
                            lidos =
                                    entrada.read(
                                            buffer
                                    )
                    )
                            != -1
            ) {

                saida.write(
                        buffer,
                        0,
                        lidos
                );
            }

            return saida.toString(
                    StandardCharsets.UTF_8.name()
            );
        }
    }
}