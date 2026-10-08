package com.example.ecociente.repository;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.example.ecociente.R;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.ui.FormatoDataApi;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public final class LembretesColeta {

    private static final String PREFERENCIAS = "lembretes_coleta";
    private static final String CHAVE_IDS = "ids";
    private static final String CANAL = "lembretes_coleta";
    private static final String EXTRA_ID = "idColeta";
    private static final String EXTRA_TEXTO = "textoLembrete";
    private static final int HORA_DA_VESPERA = 18;
    private static final long UMA_HORA_MS = 60L * 60 * 1000;

    private LembretesColeta() {}

    public static boolean ativado(@NonNull Context contexto, int idColeta) {
        return ids(contexto).contains(String.valueOf(idColeta));
    }

    public static boolean ativar(@NonNull Context contexto, @NonNull Solicitacao coleta) {

        Calendar inicio = FormatoDataApi.calendario(coleta.getDataInicio());

        if (inicio == null) {
            return false;
        }

        Calendar vespera = (Calendar) inicio.clone();

        vespera.add(Calendar.DAY_OF_MONTH, -1);
        vespera.set(Calendar.HOUR_OF_DAY, HORA_DA_VESPERA);
        vespera.set(Calendar.MINUTE, 0);
        vespera.set(Calendar.SECOND, 0);

        long agora = System.currentTimeMillis();

        long momento;
        int textoRes;

        if (vespera.getTimeInMillis() > agora) {
            momento = vespera.getTimeInMillis();
            textoRes = R.string.lembrete_amanha;

        } else if (inicio.getTimeInMillis() - UMA_HORA_MS > agora) {
            momento = inicio.getTimeInMillis() - UMA_HORA_MS;
            textoRes = R.string.lembrete_hoje;

        } else {
            return false;
        }

        String texto =
                contexto.getString(
                        textoRes,
                        FormatoDataApi.data(coleta.getDataInicio()),
                        FormatoDataApi.horaCurta(coleta.getDataInicio()));

        alarmes(contexto).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, momento, intencao(contexto, coleta.getId(), texto));

        Set<String> ids = new HashSet<>(ids(contexto));

        ids.add(String.valueOf(coleta.getId()));

        preferencias(contexto).edit().putStringSet(CHAVE_IDS, ids).apply();

        return true;
    }

    public static void desativar(@NonNull Context contexto, int idColeta) {

        alarmes(contexto).cancel(intencao(contexto, idColeta, ""));

        Set<String> ids = new HashSet<>(ids(contexto));

        ids.remove(String.valueOf(idColeta));

        preferencias(contexto).edit().putStringSet(CHAVE_IDS, ids).apply();
    }

    @NonNull
    private static PendingIntent intencao(Context contexto, int idColeta, String texto) {

        Intent rota =
                new Intent(contexto, Receptor.class).putExtra(EXTRA_ID, idColeta).putExtra(EXTRA_TEXTO, texto);

        return PendingIntent.getBroadcast(
                contexto, idColeta, rota, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @NonNull
    private static AlarmManager alarmes(Context contexto) {
        return (AlarmManager) contexto.getSystemService(Context.ALARM_SERVICE);
    }

    @NonNull
    private static Set<String> ids(Context contexto) {
        return preferencias(contexto).getStringSet(CHAVE_IDS, new HashSet<>());
    }

    @NonNull
    private static SharedPreferences preferencias(Context contexto) {
        return contexto.getApplicationContext().getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
    }

    public static final class Receptor extends BroadcastReceiver {

        @Override
        public void onReceive(Context contexto, Intent intent) {

            int idColeta = intent.getIntExtra(EXTRA_ID, 0);

            if (!ativado(contexto, idColeta)) {
                return;
            }

            desativar(contexto, idColeta);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                contexto.getSystemService(NotificationManager.class)
                        .createNotificationChannel(
                                new NotificationChannel(
                                        CANAL,
                                        contexto.getString(R.string.canal_lembretes_coleta),
                                        NotificationManager.IMPORTANCE_DEFAULT));
            }

            NotificationManagerCompat.from(contexto)
                    .notify(
                            idColeta,
                            new NotificationCompat.Builder(contexto, CANAL)
                                    .setSmallIcon(R.drawable.ic_reciclar_chat)
                                    .setContentTitle(contexto.getString(R.string.lembrete_de_coleta))
                                    .setContentText(intent.getStringExtra(EXTRA_TEXTO))
                                    .setAutoCancel(true)
                                    .build());
        }
    }
}
