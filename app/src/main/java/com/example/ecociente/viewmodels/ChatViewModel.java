package com.example.ecociente.viewmodels;

import android.util.Log;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ecociente.model.MensagemChat;
import com.example.ecociente.model.RespostaChat;
import com.example.ecociente.repository.ChatRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatViewModel extends ViewModel {

    private static final String TAG = "EcoCienteChat";

    private final ChatRepository repositorio = new ChatRepository();
    private final ExecutorService executorRede = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<MensagemChat>> mensagens =
            new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> envioEmAndamento =
            new MutableLiveData<>(false);

    private String sessionId;

    public LiveData<List<MensagemChat>> getMensagens() {
        return mensagens;
    }

    public LiveData<Boolean> getEnvioEmAndamento() {
        return envioEmAndamento;
    }

    public void enviarMensagem(String mensagem) {
        if (mensagem == null || mensagem.trim().isEmpty()
                || Boolean.TRUE.equals(envioEmAndamento.getValue())) {
            return;
        }

        String mensagemNormalizada = mensagem.trim();
        adicionarMensagem(MensagemChat.doUsuario(mensagemNormalizada));
        envioEmAndamento.setValue(true);

        executorRede.execute(
                () -> {
                    try {
                        RespostaChat resposta =
                                repositorio.enviarMensagem(mensagemNormalizada, sessionId);

                        if (!resposta.getSessionId().isEmpty()) {
                            sessionId = resposta.getSessionId();
                        }

                        adicionarMensagem(MensagemChat.doEko(resposta.getResposta()));
                    } catch (Exception erro) {
                        Log.e(TAG, "Falha ao chamar a API", erro);
                        adicionarMensagem(MensagemChat.erroDeConexao());
                    } finally {
                        envioEmAndamento.postValue(false);
                    }
                });
    }

    private synchronized void adicionarMensagem(MensagemChat mensagem) {
        List<MensagemChat> atuais = mensagens.getValue();
        ArrayList<MensagemChat> atualizadas =
                atuais == null ? new ArrayList<>() : new ArrayList<>(atuais);
        atualizadas.add(mensagem);

        if (Looper.myLooper() == Looper.getMainLooper()) {
            mensagens.setValue(Collections.unmodifiableList(atualizadas));
        } else {
            mensagens.postValue(Collections.unmodifiableList(atualizadas));
        }
    }

    @Override
    protected void onCleared() {
        executorRede.shutdownNow();
        super.onCleared();
    }
}
