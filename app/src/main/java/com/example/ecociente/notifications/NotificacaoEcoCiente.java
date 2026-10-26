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
import com.example.ecociente.views.CalendarioColetasActivity;
import com.example.ecociente.views.MensagensActivity;
import com.example.ecociente.views.QuizActivity;
import com.example.ecociente.views.SolicitacoesActivity;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class NotificacaoEcoCiente {

    private static final String CANAL_ID =
            "ecociente_engajamento";

    private static final String CANAL_NOME =
            "Lembretes do EcoCiente";

    private static final String CANAL_DESCRICAO =
            "Quizzes, dicas e lembretes enviados pelo Eko";

    private static final AtomicInteger PROXIMO_CODIGO =
            new AtomicInteger((int) (System.currentTimeMillis() & 0x0FFFFFFF));

    private NotificacaoEcoCiente() {
    }

    public static void exibir(
            @NonNull Context context,
            @NonNull TipoNotificacaoEcoCiente tipo,
            @NonNull Map<String, String> dados
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

        String titulo = obterValor(dados, "titulo", tipo.getTituloPadrao());
        String mensagem = obterValor(dados, "mensagem", tipo.getMensagemPadrao());
        String destino = obterValor(dados, "destino", tipo.getDestinoPadrao());
        String textoAcao = tipo.possuiCta()
                ? obterValor(dados, "acao", tipo.getAcaoPadrao())
                : null;

        int idNotificacao = proximoCodigo();

        PendingIntent abrirCorpo = criarPendingIntent(
                context,
                destino,
                dados,
                proximoCodigo()
        );

        PendingIntent abrirDestino = textoAcao == null
                ? null
                : criarPendingIntent(
                        context,
                        destino,
                        dados,
                        proximoCodigo()
        );

        RemoteViews compacta =
                new RemoteViews(
                        context.getPackageName(),
                        R.layout.notification_ecociente_compacta
                );

        RemoteViews expandida =
                new RemoteViews(
                        context.getPackageName(),
                        tipo.possuiCta()
                                ? R.layout.notification_ecociente_expandida
                                : R.layout.notification_ecociente_expandida_sem_acao
                );

        preencherLayout(
                compacta,
                titulo,
                mensagem,
                tipo.getImagemEko(),
                null
        );

        preencherLayout(
                expandida,
                titulo,
                mensagem,
                tipo.getImagemEko(),
                textoAcao
        );

        if (abrirDestino != null) {
            expandida.setOnClickPendingIntent(R.id.acaoNotificacao, abrirDestino);
        }

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
                                abrirCorpo
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
            int imagemEko,
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

        layout.setImageViewResource(
                R.id.imagemEkoNotificacao,
                imagemEko
        );

        if (textoAcao != null) {
            layout.setTextViewText(
                    R.id.acaoNotificacao,
                    textoAcao
            );
        }
    }

    @NonNull
    private static PendingIntent criarPendingIntent(
            @NonNull Context context,
            @NonNull String destino,
            @NonNull Map<String, String> dados,
            int codigoSolicitacao
    ) {
        Intent intent;

        if ("quiz".equalsIgnoreCase(destino)) {

            intent =
                    new Intent(
                            context,
                            QuizActivity.class
                    );

        } else if ("calendario".equalsIgnoreCase(destino)) {

            intent = new Intent(context, CalendarioColetasActivity.class);

        } else if ("mensagens".equalsIgnoreCase(destino)) {

            intent = new Intent(context, MensagensActivity.class);

        } else if ("solicitacoes".equalsIgnoreCase(destino)) {

            intent = new Intent(context, SolicitacoesActivity.class);

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

        for (Map.Entry<String, String> entrada : dados.entrySet()) {
            intent.putExtra(entrada.getKey(), entrada.getValue());
        }

        return PendingIntent.getActivity(
                context,
                codigoSolicitacao,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );
    }

    @NonNull
    private static String obterValor(
            @NonNull Map<String, String> dados,
            @NonNull String chave,
            @NonNull String padrao
    ) {
        String valor = dados.get(chave);
        return valor == null || valor.trim().isEmpty() ? padrao : valor.trim();
    }

    private static int proximoCodigo() {
        return PROXIMO_CODIGO.getAndUpdate(
                codigo -> codigo == Integer.MAX_VALUE ? 1 : codigo + 1
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
