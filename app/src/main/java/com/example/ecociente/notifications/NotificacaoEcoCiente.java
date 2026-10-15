package com.example.ecociente.notifications;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.ecociente.R;
import com.example.ecociente.views.MainActivity;
import com.example.ecociente.views.QuizActivity;

public final class NotificacaoEcoCiente {

    private static final String CANAL_ID =
            "ecociente_engajamento";

    private static final String CANAL_NOME =
            "Lembretes do EcoCiente";

    private static final String CANAL_DESCRICAO =
            "Quizzes, dicas e lembretes enviados pelo Eko";

    private NotificacaoEcoCiente() {
    }

    public static void exibir(
            @NonNull Context context,
            @NonNull String titulo,
            @NonNull String mensagem,
            @NonNull String destino,
            @NonNull String textoAcao
    ) {
        criarCanal(context);

        if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        && ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            return;
        }

        PendingIntent abrirHome =
                criarPendingIntentHome(context);

        PendingIntent abrirDestino =
                criarPendingIntentDestino(
                        context,
                        destino
                );

        RemoteViews compacta =
                new RemoteViews(
                        context.getPackageName(),
                        R.layout.notification_ecociente_compacta
                );

        RemoteViews expandida =
                new RemoteViews(
                        context.getPackageName(),
                        R.layout.notification_ecociente_expandida
                );

        preencherLayout(
                compacta,
                titulo,
                mensagem,
                null
        );

        preencherLayout(
                expandida,
                titulo,
                mensagem,
                textoAcao
        );

        expandida.setOnClickPendingIntent(
                R.id.acaoNotificacao,
                abrirDestino
        );

        NotificationCompat.Builder notificacao =
                new NotificationCompat.Builder(
                        context,
                        CANAL_ID
                )
                        .setSmallIcon(
                                R.drawable.ic_notification_ecociente
                        )
                        .setColor(
                                ContextCompat.getColor(
                                        context,
                                        R.color.verde_escuro_principal
                                )
                        )
                        .setCustomContentView(
                                compacta
                        )
                        .setCustomBigContentView(
                                expandida
                        )
                        .setStyle(
                                new NotificationCompat.DecoratedCustomViewStyle()
                        )
                        .setContentIntent(
                                abrirHome
                        )
                        .setAutoCancel(true)
                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )
                        .setCategory(
                                NotificationCompat.CATEGORY_REMINDER
                        )
                        .setVisibility(
                                NotificationCompat.VISIBILITY_PUBLIC
                        );

        int idNotificacao =
                (int) (
                        System.currentTimeMillis()
                                & 0x0FFFFFFF
                );

        NotificationManagerCompat
                .from(context)
                .notify(
                        idNotificacao,
                        notificacao.build()
                );
    }

    private static void preencherLayout(
            @NonNull RemoteViews layout,
            @NonNull String titulo,
            @NonNull String mensagem,
            String textoAcao
    ) {
        layout.setTextViewText(
                R.id.tituloNotificacao,
                titulo
        );

        layout.setTextViewText(
                R.id.mensagemNotificacao,
                mensagem
        );

        if (textoAcao != null) {
            layout.setTextViewText(
                    R.id.acaoNotificacao,
                    textoAcao
            );
        }
    }

    @NonNull
    private static PendingIntent criarPendingIntentHome(
            @NonNull Context context
    ) {
        Intent intent =
                new Intent(
                        context,
                        MainActivity.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        return PendingIntent.getActivity(
                context,
                2101,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );
    }

    @NonNull
    private static PendingIntent criarPendingIntentDestino(
            @NonNull Context context,
            @NonNull String destino
    ) {
        Intent intent;

        if ("quiz".equalsIgnoreCase(destino)) {

            intent =
                    new Intent(
                            context,
                            QuizActivity.class
                    );

        } else {

            intent =
                    new Intent(
                            context,
                            MainActivity.class
                    );
        }

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        return PendingIntent.getActivity(
                context,
                2102,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static void criarCanal(
            @NonNull Context context
    ) {
        if (
                Build.VERSION.SDK_INT
                        < Build.VERSION_CODES.O
        ) {
            return;
        }

        NotificationChannel canal =
                new NotificationChannel(
                        CANAL_ID,
                        CANAL_NOME,
                        NotificationManager.IMPORTANCE_HIGH
                );

        canal.setDescription(
                CANAL_DESCRICAO
        );

        canal.enableVibration(true);

        NotificationManager gerenciador =
                context.getSystemService(
                        NotificationManager.class
                );

        if (gerenciador != null) {
            gerenciador.createNotificationChannel(
                    canal
            );
        }
    }
}