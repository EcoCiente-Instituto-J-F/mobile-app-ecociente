package com.example.ecociente.repository;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.model.PostagemFeed;
import com.example.ecociente.model.ResultadoApi;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;

// Descartes do morador na ds-mobile-api: feed do condomínio, "meus descartes", categorias e registro.
public class PostagemRepository {

    private static final String TAG = "PostagemRepository";

    private static final String URL_BASE = ConfiguracaoServidores.MOBILE_API;

    private static final String URL_PONTUACAO = ConfiguracaoServidores.PONTUACAO_API;

    private static final int POR_PAGINA = 20;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    public interface RetornoPostagens {
        // null quando a API não respondeu (sem credenciais, 4xx, fora do ar).
        void aoConcluir(@Nullable List<PostagemFeed> postagens);

        // 403: o morador ainda não tem vínculo aprovado com o condomínio.
        default void aoNegar() {
            aoConcluir(null);
        }
    }

    public interface RetornoCategorias {
        // null quando a API não respondeu; cada item traz o id em `chave`.
        void aoConcluir(@Nullable List<ItemLista> categorias);
    }

    public interface RetornoRegistro {
        void aoConcluir(@NonNull ResultadoApi resultado);
    }

    public void feed(@NonNull Context contexto, int condominioId, @NonNull RetornoPostagens retorno) {
        listar(contexto, "/api/postagens?condominioId=" + condominioId + "&size=" + POR_PAGINA, retorno);
    }

    // Descartes do condomínio que ainda esperam a decisão do síndico (até as 100 mais recentes). Rota própria do síndico responsável: o feed dos moradores (GET /api/postagens) exige ser morador do condomínio.
    public void emAnalise(@NonNull Context contexto, int condominioId, @NonNull RetornoPostagens retorno) {
        listar(contexto, "/api/condominios/" + condominioId + "/postagens/em-analise?size=100", retorno);
    }

    // PATCH /api/v1/postagens/{id}/decisao da ds-pontuacao-api: só o síndico do condomínio (ou admin).
    public void decidir(
            @NonNull Context contexto, int postagemId, boolean aprovar, @NonNull RetornoRegistro retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    ResultadoApi resultado;

                    try {
                        ApiAutenticada.Resposta resposta =
                                ApiAutenticada.alterarComCodigo(
                                        contextoApp,
                                        URL_PONTUACAO + "/api/v1/postagens/" + postagemId + "/decisao",
                                        PostagemJson.corpoDecisao(aprovar));

                        if (resposta != null && resposta.sucesso()) {
                            resultado = ResultadoApi.sucesso();
                        } else {
                            resultado =
                                    ResultadoApi.erro(
                                            PostagemJson.mensagemDaDecisao(resposta == null ? 401 : resposta.codigo));
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível registrar a decisão", erro);

                        resultado = ResultadoApi.erro(PostagemJson.mensagemDaDecisao(0));
                    }

                    ResultadoApi resultadoFinal = resultado;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resultadoFinal));
                });
    }

    // Só descartes em análise (janela de 24 h) aceitam voto.
    public static boolean estaEmAnalise(@NonNull PostagemFeed postagem) {
        return PostagemJson.statusLegivel("em_analise").equals(postagem.getStatus());
    }

    public static boolean estaAprovada(@NonNull PostagemFeed postagem) {
        return PostagemJson.statusLegivel("aprovada").equals(postagem.getStatus());
    }

    public static boolean estaReprovada(@NonNull PostagemFeed postagem) {
        return PostagemJson.statusLegivel("reprovada").equals(postagem.getStatus());
    }

    // POST /api/postagens/{id}/votos: o morador aprova ou denuncia (com o id do motivo) um descarte em análise.
    public void votar(
            @NonNull Context contexto,
            int postagemId,
            boolean aprovar,
            int motivoDenunciaId,
            @NonNull RetornoRegistro retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    ResultadoApi resultado;

                    try {
                        ApiAutenticada.Resposta resposta =
                                ApiAutenticada.enviarComCodigo(
                                        contextoApp,
                                        URL_BASE + "/api/postagens/" + postagemId + "/votos",
                                        PostagemJson.corpoVoto(aprovar, motivoDenunciaId));

                        if (resposta == null) {
                            resultado = ResultadoApi.erro(PostagemJson.mensagemDoVoto(401, null));
                        } else if (resposta.sucesso()) {
                            resultado = ResultadoApi.sucesso();
                        } else {
                            resultado =
                                    ResultadoApi.erro(PostagemJson.mensagemDoVoto(resposta.codigo, resposta.corpo));
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível registrar o voto", erro);

                        resultado = ResultadoApi.erro(PostagemJson.mensagemDoVoto(0, null));
                    }

                    ResultadoApi resultadoFinal = resultado;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resultadoFinal));
                });
    }

    public void minhas(@NonNull Context contexto, @NonNull RetornoPostagens retorno) {
        listar(contexto, "/api/postagens/minhas?size=" + POR_PAGINA, retorno);
    }

    private void listar(@NonNull Context contexto, @NonNull String caminho, @NonNull RetornoPostagens retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    List<PostagemFeed> postagens = null;
                    boolean negado = false;

                    try {
                        ApiAutenticada.Resposta resposta = ApiAutenticada.obterComCodigo(contextoApp, URL_BASE + caminho);

                        if (resposta != null && resposta.sucesso()) {
                            postagens = PostagemJson.postagens(new JSONObject(resposta.corpo), System.currentTimeMillis());
                        } else if (resposta != null) {
                            negado = resposta.codigo == 403;
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível carregar as postagens", erro);
                    }

                    List<PostagemFeed> resultado = postagens;
                    boolean negadoFinal = negado;

                    PRINCIPAL.post(
                            () -> {
                                if (negadoFinal) {
                                    retorno.aoNegar();
                                } else {
                                    retorno.aoConcluir(resultado);
                                }
                            });
                });
    }

    public void categorias(@NonNull Context contexto, @NonNull RetornoCategorias retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    List<ItemLista> categorias = null;

                    try {
                        String corpo = ApiAutenticada.obter(contextoApp, URL_BASE + "/api/categorias-residuos");

                        if (corpo != null) {
                            categorias = PostagemJson.categorias(new JSONArray(corpo));
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível carregar as categorias", erro);
                    }

                    List<ItemLista> resultado = categorias;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resultado));
                });
    }

    // Calcula o hash, sobe a foto ao Cloudinary e registra a postagem. `capturadaEmMs` é o momento em que a câmera entregou a foto.
    public void registrar(
            @NonNull Context contexto,
            @NonNull Uri foto,
            int condominioId,
            int categoriaId,
            long capturadaEmMs,
            @NonNull RetornoRegistro retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    ResultadoApi resultado;

                    try {
                        String hash = PostagemJson.sha256(lerBytes(contextoApp, foto));

                        String urlFoto = new FotoPerfilRepository().urlDaFotoDoFeed(contextoApp, foto);

                        String corpo =
                                PostagemJson.corpoCriar(
                                        condominioId, categoriaId, urlFoto, hash, PostagemJson.iso(capturadaEmMs));

                        ApiAutenticada.Resposta resposta =
                                ApiAutenticada.enviarComCodigo(contextoApp, URL_BASE + "/api/postagens", corpo);

                        if (resposta == null) {
                            resultado = ResultadoApi.erro(PostagemJson.mensagemDoErro(401, null));
                        } else if (resposta.sucesso()) {
                            resultado = ResultadoApi.sucesso();
                        } else {
                            resultado =
                                    ResultadoApi.erro(PostagemJson.mensagemDoErro(resposta.codigo, resposta.corpo));
                        }

                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível registrar o descarte", erro);

                        resultado = ResultadoApi.erro(PostagemJson.mensagemDoErro(0, null));
                    }

                    ResultadoApi resultadoFinal = resultado;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resultadoFinal));
                });
    }

    @NonNull
    private static byte[] lerBytes(@NonNull Context contexto, @NonNull Uri uri) throws Exception {

        try (InputStream entrada = contexto.getContentResolver().openInputStream(uri);
                ByteArrayOutputStream saida = new ByteArrayOutputStream()) {

            if (entrada == null) {
                throw new IllegalStateException("Não foi possível abrir a foto");
            }

            byte[] buffer = new byte[8192];
            int lidos;

            while ((lidos = entrada.read(buffer)) != -1) {
                saida.write(buffer, 0, lidos);
            }

            return saida.toByteArray();
        }
    }
}
