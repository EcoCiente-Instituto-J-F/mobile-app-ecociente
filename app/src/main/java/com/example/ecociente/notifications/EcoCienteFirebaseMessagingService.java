package com.example.ecociente.notifications;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.ecociente.repository.PerfilRepository;
import com.example.ecociente.repository.AutenticacaoExternaRepository;
import com.example.ecociente.repository.PushRegistroRepository;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class EcoCienteFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_ECOCIENTE";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> dados = new java.util.HashMap<>(remoteMessage.getData());
        if (remoteMessage.getNotification() != null) {
            if (!dados.containsKey("titulo") && remoteMessage.getNotification().getTitle() != null) {
                dados.put("titulo", remoteMessage.getNotification().getTitle());
            }
            if (!dados.containsKey("mensagem") && remoteMessage.getNotification().getBody() != null) {
                dados.put("mensagem", remoteMessage.getNotification().getBody());
            }
        }
        if (dados.isEmpty()) return;

        TipoNotificacaoEcoCiente tipo =
                TipoNotificacaoEcoCiente.porId(dados.containsKey("tipo") ? dados.get("tipo") : dados.get("type"));

        // Aparelho sem sessão não mostra aviso (o toque abriria uma tela restrita), e o aviso respeita
        // os interruptores do perfil (gerais e o grupo do tipo).
        PerfilRepository perfil = new PerfilRepository();
        FirebaseUser usuario = perfil.usuarioAtual();

        if (usuario == null || !perfil.notificacaoPermitida(usuario.getUid(), tipo.getGrupo())) return;
        String destinatario = dados.get("usuarioId");
        if (destinatario != null && !destinatario.equals(
                String.valueOf(AutenticacaoExternaRepository.usuarioIdSalvo(this)))) return;

        NotificacaoEcoCiente.exibir(
                this,
                tipo,
                dados
        );
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        Log.d(TAG, "Token FCM atualizado");
        PushRegistroRepository.registrarToken(getApplicationContext(), token);
    }

}
