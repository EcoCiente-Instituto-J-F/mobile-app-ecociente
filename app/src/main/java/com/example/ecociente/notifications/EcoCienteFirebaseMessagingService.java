package com.example.ecociente.notifications;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class EcoCienteFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_ECOCIENTE";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String titulo = obterValor(
                remoteMessage,
                "titulo",
                "EcoCiente"
        );

        String mensagem = obterValor(
                remoteMessage,
                "mensagem",
                "O Eko trouxe uma mensagem para você!"
        );

        String destino = obterValor(
                remoteMessage,
                "destino",
                "home"
        );

        String textoAcao = obterValor(
                remoteMessage,
                "acao",
                "Fazer quiz"
        );

        NotificacaoEcoCiente.exibir(
                this,
                titulo,
                mensagem,
                destino,
                textoAcao
        );
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        Log.d(
                TAG,
                "NOVO_TOKEN_FCM = " + token
        );
    }

    @NonNull
    private String obterValor(
            @NonNull RemoteMessage remoteMessage,
            @NonNull String chave,
            @NonNull String padrao
    ) {
        String valor =
                remoteMessage.getData().get(chave);

        if (valor == null || valor.trim().isEmpty()) {
            return padrao;
        }

        return valor.trim();
    }
}