package com.example.ecociente.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.example.ecociente.R;
import com.google.android.material.button.MaterialButton;

// Lista vazia e falha ao carregar, iguais em todas as telas (o carregando é o IndicadorCarregamento).
// Só acrescenta ao fim de `area`; quem chama limpa a área antes, se for o caso.
public final class EstadoTela {

    private static final int HORIZONTAL_DP = 24;
    private static final int TOPO_DP = 48;
    private static final int ENTRE_TEXTO_E_BOTAO_DP = 16;

    private EstadoTela() {}

    public static void vazio(@NonNull ViewGroup area, @StringRes int mensagem) {
        texto(area, mensagem);
    }

    public static void erro(@NonNull ViewGroup area, @StringRes int mensagem, @NonNull Runnable tentarNovamente) {
        comAcao(area, mensagem, R.string.tentar_novamente, tentarNovamente);
    }

    // Mensagem com um botão (por exemplo "Entrar novamente"); sem ação, só a mensagem.
    public static void comAcao(
            @NonNull ViewGroup area,
            @StringRes int mensagem,
            @StringRes int rotulo,
            @Nullable Runnable acao) {

        texto(area, mensagem);

        if (acao == null) {
            return;
        }

        Context contexto = area.getContext();

        MaterialButton botao =
                (MaterialButton) LayoutInflater.from(contexto).inflate(R.layout.botao_dialogo, area, false);

        botao.setText(rotulo);
        botao.setOnClickListener(view -> acao.run());

        area.addView(botao);
    }

    private static void texto(ViewGroup area, @StringRes int mensagem) {

        Context contexto = area.getContext();

        TextView texto = new TextView(contexto);

        texto.setText(mensagem);
        texto.setTextColor(contexto.getColor(R.color.cinza_texto_auxiliar));
        texto.setTextSize(14);
        texto.setGravity(Gravity.CENTER);
        texto.setPadding(dp(contexto, HORIZONTAL_DP), dp(contexto, TOPO_DP), dp(contexto, HORIZONTAL_DP),
                dp(contexto, ENTRE_TEXTO_E_BOTAO_DP));

        area.addView(texto);
    }

    private static int dp(Context contexto, int valor) {
        return Math.round(valor * contexto.getResources().getDisplayMetrics().density);
    }
}
