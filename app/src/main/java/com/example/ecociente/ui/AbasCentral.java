package com.example.ecociente.ui;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.example.ecociente.R;

public final class AbasCentral {

    private AbasCentral() {}

    public static void configurar(
            @NonNull View raiz,
            boolean notificacoesSelecionada,
            @NonNull Runnable aoNotificacoes,
            @NonNull Runnable aoAvisos) {

        TextView notificacoes = raiz.findViewById(R.id.abaCentralNotificacoes);
        TextView avisos = raiz.findViewById(R.id.abaCentralAvisos);

        marcar(notificacoes, notificacoesSelecionada);
        marcar(avisos, !notificacoesSelecionada);

        notificacoes.setOnClickListener(view -> {
            if (!notificacoesSelecionada) {
                aoNotificacoes.run();
            }
        });

        avisos.setOnClickListener(view -> {
            if (notificacoesSelecionada) {
                aoAvisos.run();
            }
        });
    }

    private static void marcar(@NonNull TextView aba, boolean selecionada) {

        aba.setBackgroundResource(selecionada ? R.drawable.fundo_aba_selecionada : 0);

        aba.setTextColor(
                ContextCompat.getColor(
                        aba.getContext(), selecionada ? R.color.branco : R.color.verde_escuro_principal));
    }
}
