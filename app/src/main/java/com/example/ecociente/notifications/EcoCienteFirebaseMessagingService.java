package com.example.ecociente.notifications;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class EcoCienteFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_ECOCIENTE";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> dados = remoteMessage.getData();

        TipoNotificacaoEcoCiente tipo =
                TipoNotificacaoEcoCiente.porId(dados.get("tipo"));

        NotificacaoEcoCiente.exibir(
                this,
                tipo,
                dados
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

}
