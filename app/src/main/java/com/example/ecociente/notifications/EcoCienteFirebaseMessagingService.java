package com.example.ecociente.notifications;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.ecociente.repository.PerfilRepository;
import com.example.ecociente.model.PreferenciasNotificacao;
import com.example.ecociente.repository.AutenticacaoExternaRepository;
import com.example.ecociente.repository.PushRegistroRepository;
import com.example.ecociente.model.UsuarioLogado;
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
        UsuarioLogado usuario = perfil.usuarioAtual();

        if (usuario == null || tipo == TipoNotificacaoEcoCiente.DESCONHECIDA) return;
        String destinatario = dados.get("usuarioId");
        int usuarioId = AutenticacaoExternaRepository.usuarioIdSalvo(this);
        if (destinatario == null || usuarioId <= 0
                || !destinatario.equals(String.valueOf(usuarioId))) return;
        String grupo;
        switch (tipo.getGrupo()) {
            case LEMBRETES_AVISOS: grupo = "lembretesAvisos"; break;
            case NOVAS_ATIVIDADES: grupo = "novasAtividades"; break;
            case DICAS_SUSTENTAVEIS: grupo = "dicasSustentaveis"; break;
            default: return;
        }
        if (!PushRegistroRepository.notificacaoPermitida(this, usuario.getUid(), grupo)) return;

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
