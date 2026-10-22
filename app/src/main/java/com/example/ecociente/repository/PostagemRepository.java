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
import java.util.ArrayList;

// Descartes do morador na ds-mobile-api: feed do condomínio, "meus descartes", categorias e registro.
public class PostagemRepository {

    private static final String TAG = "PostagemRepository";

    private static final String URL_BASE = "https://ds-mobile-api.onrender.com";

    private static final String URL_PONTUACAO = "https://ds-pontuacao-api.onrender.com";

    private static final int POR_PAGINA = 20;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    public interface RetornoPostagens {
        // null quando a API não respondeu (sem credenciais, 4xx, fora do ar).
        void aoConcluir(@Nullable List<PostagemFeed> postagens);
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

    // Descartes do condomínio que ainda esperam a decisão do síndico (até as 100 postagens mais recentes).
    public void emAnalise(@NonNull Context contexto, int condominioId, @NonNull RetornoPostagens retorno) {
        listar(
                contexto,
                "/api/postagens?condominioId=" + condominioId + "&size=100",
                postagens -> {
                    if (postagens == null) {
                        retorno.aoConcluir(null);
                        return;
                    }

                    List<PostagemFeed> pendentes = new ArrayList<>();
                    String emAnalise = PostagemJson.statusLegivel("em_analise");

                    for (PostagemFeed postagem : postagens) {
                        if (emAnalise.equals(postagem.getStatus())) {
                            pendentes.add(postagem);
                        }
                    }

                    retorno.aoConcluir(pendentes);
                });
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

    public void minhas(@NonNull Context contexto, @NonNull RetornoPostagens retorno) {
        listar(contexto, "/api/postagens/minhas?size=" + POR_PAGINA, retorno);
    }

    private void listar(@NonNull Context contexto, @NonNull String caminho, @NonNull RetornoPostagens retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    List<PostagemFeed> postagens = null;

                    try {
                        String corpo = ApiAutenticada.obter(contextoApp, URL_BASE + caminho);

                        if (corpo != null) {
                            postagens = PostagemJson.postagens(new JSONObject(corpo), System.currentTimeMillis());
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível carregar as postagens", erro);
                    }

                    List<PostagemFeed> resultado = postagens;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resultado));
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

    // Calcula o hash, sobe a foto ao Cloudinary e registra a postagem. `capturadaEmMs` é o
    // momento em que a câmera entregou a foto.
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
