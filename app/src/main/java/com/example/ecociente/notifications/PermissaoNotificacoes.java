package com.example.ecociente.notifications;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

// Pede a permissão de notificação (Android 13+) para as notificações push aparecerem.
// Crie no `onCreate`: o pedido precisa ser registrado antes de a tela iniciar.
public final class PermissaoNotificacoes {

    private final AppCompatActivity tela;
    private final ActivityResultLauncher<String> pedido;

    public PermissaoNotificacoes(@NonNull AppCompatActivity tela) {
        this.tela = tela;
        this.pedido = tela.registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedida -> {});
    }

    public void pedirSeNecessario() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (ContextCompat.checkSelfPermission(tela, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            pedido.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }
}
