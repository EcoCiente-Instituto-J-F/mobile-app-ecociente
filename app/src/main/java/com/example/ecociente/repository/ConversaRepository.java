package com.example.ecociente.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.Conversa;
import com.example.ecociente.model.MensagemConversa;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

// Conversas entre condomínio e cooperativa, vindas de /api/conversas da ds-mobile-api. O lado de quem usa o app ("CONDOMINIO" para o síndico, "COOPERATIVA" para a cooperativa) decide qual nome aparece e quais mensagens são "enviadas".
public class ConversaRepository {

    private static final String TAG = "ConversaRepository";

    private static final String URL_CONVERSAS = ConfiguracaoServidores.MOBILE_API + "/api/conversas";

    private static final int TAMANHO_DA_PAGINA = 100;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    public static String ladoDoSindico() {
        return ConversaJson.LADO_CONDOMINIO;
    }

    public static String ladoDaCooperativa() {
        return ConversaJson.LADO_COOPERATIVA;
    }

    public static boolean podeEscrever(@NonNull String meuLado, int quantidadeDeMensagens) {
        return ConversaJson.podeEscrever(meuLado, quantidadeDeMensagens);
    }

    public interface RetornoConversas {
        // null quando a API não respondeu; lista vazia = nenhuma conversa.
        void aoConcluir(@Nullable List<Conversa> conversas);
    }

    public interface RetornoConversa {
        // null quando a API não abriu a conversa.
        void aoConcluir(@Nullable Conversa conversa);
    }

    public interface RetornoMensagens {
        // null quando a API não respondeu; lista vazia = conversa sem mensagens.
        void aoConcluir(@Nullable List<MensagemConversa> mensagens);
    }

    public interface RetornoEnvio {
        // `mensagem` é null quando o envio falhou, e `erro` explica o motivo.
        void aoConcluir(@Nullable MensagemConversa mensagem, @NonNull String erro);
    }

    // GET /api/conversas: as ativas, ou as arquivadas.
    public void listar(
            @NonNull Context contexto, boolean souCondominio, boolean arquivadas, @NonNull RetornoConversas retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    List<Conversa> conversas = null;

                    try {
                        String corpo =
                                ApiAutenticada.obter(
                                        contextoApp,
                                        URL_CONVERSAS + "?arquivadas=" + arquivadas + "&size=" + TAMANHO_DA_PAGINA);

                        if (corpo != null) {
                            conversas = ConversaJson.conversas(new JSONObject(corpo), souCondominio);
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível carregar as conversas", erro);
                    }

                    List<Conversa> resultado = conversas;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resultado));
                });
    }

    // POST /api/conversas: abre a conversa do condomínio com a cooperativa, ou devolve a que já existe.
    public void abrir(
            @NonNull Context contexto,
            int condominioId,
            int cooperativaId,
            boolean souCondominio,
            @NonNull RetornoConversa retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    Conversa conversa = null;

                    try {
                        ApiAutenticada.Resposta resposta =
                                ApiAutenticada.enviarComCodigo(
                                        contextoApp,
                                        URL_CONVERSAS,
                                        ConversaJson.corpoCriar(condominioId, cooperativaId));

                        if (resposta != null && resposta.sucesso()) {
                            conversa = ConversaJson.conversa(new JSONObject(resposta.corpo), souCondominio);
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível abrir a conversa", erro);
                    }

                    Conversa resultado = conversa;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resultado));
                });
    }

    // GET /api/conversas/{id}/mensagens: as mais recentes (até 100), da mais antiga para a mais nova.
    public void mensagens(
            @NonNull Context contexto, int conversaId, @NonNull String meuLado, @NonNull RetornoMensagens retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    List<MensagemConversa> mensagens = null;

                    try {
                        String corpo =
                                ApiAutenticada.obter(
                                        contextoApp,
                                        URL_CONVERSAS + "/" + conversaId + "/mensagens?size=" + TAMANHO_DA_PAGINA);

                        if (corpo != null) {
                            mensagens = ConversaJson.mensagens(new JSONObject(corpo), meuLado);
                        }
                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível carregar as mensagens", erro);
                    }

                    List<MensagemConversa> resultado = mensagens;

                    PRINCIPAL.post(() -> retorno.aoConcluir(resultado));
                });
    }

    public void enviar(
            @NonNull Context contexto,
            int conversaId,
            @NonNull String texto,
            @NonNull String meuLado,
            @NonNull RetornoEnvio retorno) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    MensagemConversa mensagem = null;
                    String erro = ConversaJson.mensagemDoErroDeEnvio(0, null);

                    try {
                        ApiAutenticada.Resposta resposta =
                                ApiAutenticada.enviarComCodigo(
                                        contextoApp,
                                        URL_CONVERSAS + "/" + conversaId + "/mensagens",
                                        ConversaJson.corpoEnviar(texto));

                        if (resposta == null) {
                            erro = ConversaJson.mensagemDoErroDeEnvio(401, null);

                        } else if (resposta.sucesso()) {
                            mensagem = ConversaJson.mensagem(new JSONObject(resposta.corpo), meuLado);

                        } else {
                            erro = ConversaJson.mensagemDoErroDeEnvio(resposta.codigo, resposta.corpo);
                        }
                    } catch (Exception falha) {
                        Log.w(TAG, "Não foi possível enviar a mensagem", falha);
                    }

                    MensagemConversa enviada = mensagem;
                    String erroFinal = erro;

                    PRINCIPAL.post(() -> retorno.aoConcluir(enviada, erroFinal));
                });
    }

    // PATCH /api/conversas/{id}/leitura: marca como lidas as mensagens do outro lado. Se falhar, as mensagens só continuam como não lidas.
    public void marcarComoLida(@NonNull Context contexto, int conversaId) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    try {
                        ApiAutenticada.alterarComCodigo(contextoApp, URL_CONVERSAS + "/" + conversaId + "/leitura", "{}");

                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível marcar a conversa como lida", erro);
                    }
                });
    }
}
