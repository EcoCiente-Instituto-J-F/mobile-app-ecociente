package com.example.ecociente.viewmodels;

import android.content.Context;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ecociente.model.MensagemChat;
import com.example.ecociente.model.RespostaChat;
import com.example.ecociente.repository.ChatRepository;
import com.example.ecociente.repository.EkoRespostaLocal;
import com.example.ecociente.ui.FormatadorRespostaEko;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.io.IOException;

public class ChatViewModel extends ViewModel {

    private static final String TAG = "EcoCienteChat";
    private static final int TEMPO_ESPERA_EKO_SEGUNDOS = 10;
    private final Handler principal = new Handler(Looper.getMainLooper());

    private final ChatRepository repositorio = new ChatRepository();
    private final ExecutorService executorRede = Executors.newSingleThreadExecutor();
    private final ExecutorService executorChamadas = Executors.newCachedThreadPool();
    private final MutableLiveData<List<MensagemChat>> mensagens =
            new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> envioEmAndamento =
            new MutableLiveData<>(false);

    // Escrito pela thread de rede e lido pela principal ao enviar a próxima mensagem.
    private volatile String sessionId;

    public LiveData<List<MensagemChat>> getMensagens() {
        return mensagens;
    }

    public LiveData<Boolean> getEnvioEmAndamento() {
        return envioEmAndamento;
    }

    public void enviarMensagem(Context contexto, String mensagem) {
        if (mensagem == null || mensagem.trim().isEmpty()
                || Boolean.TRUE.equals(envioEmAndamento.getValue())) {
            return;
        }

        String mensagemNormalizada = mensagem.trim();
        Context contextoApp = contexto.getApplicationContext();
        adicionarMensagem(MensagemChat.doUsuario(mensagemNormalizada));
        envioEmAndamento.setValue(true);

        executorRede.execute(() -> {
            Future<RespostaChat> chamada = executorChamadas.submit(
                    () -> repositorio.enviarMensagem(contextoApp, mensagemNormalizada, sessionId));
            try {
                RespostaChat resposta = chamada.get(TEMPO_ESPERA_EKO_SEGUNDOS, TimeUnit.SECONDS);
                if (!resposta.getSessionId().isEmpty()) {
                    sessionId = resposta.getSessionId();
                }
                adicionarMensagem(MensagemChat.doEko(
                        FormatadorRespostaEko.paraExibicao(resposta.getResposta())));
            } catch (TimeoutException erro) {
                chamada.cancel(true);
                Log.w(TAG, "Eko excedeu 10 segundos; mostrando orientacao offline");
                adicionarMensagem(MensagemChat.doEko(EkoRespostaLocal.responder(mensagemNormalizada)));
            } catch (InterruptedException erro) {
                chamada.cancel(true);
                Thread.currentThread().interrupt();
                adicionarMensagem(MensagemChat.erroDeConexao());
            } catch (ExecutionException erro) {
                Throwable causa = erro.getCause();
                Log.w(TAG, "Falha no Eko", causa);
                if (causa instanceof IOException && causa.getMessage() != null
                        && causa.getMessage().contains("HTTP 429")) {
                    String motivo = causa.getMessage();
                    String aviso = motivo.contains("limite_rate")
                            ? "Muitas mensagens em sequência. Aguarde um minuto para tentar novamente."
                            : motivo.contains("limite_session")
                                    ? "Esta conversa atingiu o limite. Inicie uma nova conversa para continuar."
                                    : motivo.contains("limite_daily")
                                            ? "Você atingiu sua cota diária do Eko. O limite renova à meia-noite, horário de Brasília."
                                            : "O Eko limitou temporariamente as mensagens. Tente novamente mais tarde.";
                    adicionarMensagem(MensagemChat.doEko(aviso));
                } else if (causa instanceof IOException && causa.getMessage() != null
                        && (causa.getMessage().contains("HTTP 401")
                        || causa.getMessage().contains("HTTP 403"))) {
                    adicionarMensagem(MensagemChat.doEko(
                            "Sua sessao precisa ser renovada. Entre novamente na conta para conversar com o Eko."));
                } else {
                    adicionarMensagem(MensagemChat.doEko(EkoRespostaLocal.responder(mensagemNormalizada)));
                }
            } finally {
                envioEmAndamento.postValue(false);
            }
        });
    }

    private void adicionarMensagem(MensagemChat mensagem) {
        Runnable aplicar = () -> {
            List<MensagemChat> atuais = mensagens.getValue();
            ArrayList<MensagemChat> atualizadas =
                    atuais == null ? new ArrayList<>() : new ArrayList<>(atuais);
            atualizadas.add(mensagem);
            mensagens.setValue(Collections.unmodifiableList(atualizadas));
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            aplicar.run();
        } else {
            principal.post(aplicar);
        }
    }

    @Override
    protected void onCleared() {
        executorRede.shutdownNow();
        executorChamadas.shutdownNow();
    }
}
