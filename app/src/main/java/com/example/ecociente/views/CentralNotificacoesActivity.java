package com.example.ecociente.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import com.example.ecociente.R;
import com.example.ecociente.model.Notificacao;
import com.example.ecociente.ui.AbasCentral;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.JanelaEdgeToEdge;

public class CentralNotificacoesActivity extends AppCompatActivity {

    public enum Destino {
        COOPERATIVA,
        CONDOMINIO,
        SINDICO
    }

    private static final String EXTRA_DESTINO = "destinoAvisos";

    private Destino destino;

    @NonNull
    public static Intent criarIntent(@NonNull Context contexto, @NonNull Destino destino) {

        return new Intent(contexto, CentralNotificacoesActivity.class).putExtra(EXTRA_DESTINO, destino);
    }

    @NonNull
    public static Intent intentDosAvisos(@NonNull Context contexto, @NonNull Destino destino) {

        if (destino == Destino.SINDICO) {
            return new Intent(contexto, SindicoAvisosActivity.class);
        }

        return AvisosActivity.criarIntent(contexto, destino == Destino.COOPERATIVA);
    }

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        JanelaEdgeToEdge.aplicar(this);

        setContentView(R.layout.activity_central_notificacoes);

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizCentralNotificacoes));

        destino = (Destino) getIntent().getSerializableExtra(EXTRA_DESTINO);

        if (destino == null) {
            destino = Destino.CONDOMINIO;
        }

        findViewById(R.id.botaoVoltarCentral).setOnClickListener(view -> finish());

        AbasCentral.configurar(
                findViewById(R.id.abasCentral),
                true,
                () -> {},
                () -> {
                    startActivity(intentDosAvisos(this, destino));

                    overridePendingTransition(0, 0);

                    finish();
                });

        exibirNotificacoes();
    }

    private void exibirNotificacoes() {

        LinearLayout lista = findViewById(R.id.listaCentralNotificacoes);

        LayoutInflater inflador = LayoutInflater.from(this);

        String grupoAtual = null;

        for (Notificacao notificacao : Notificacao.exemplos()) {

            if (!notificacao.grupo.equals(grupoAtual)) {
                grupoAtual = notificacao.grupo;

                TextView rotulo = new TextView(this);
                rotulo.setText(grupoAtual);
                rotulo.setTextColor(getColor(R.color.verde_escuro_principal));
                rotulo.setTextSize(15);
                rotulo.setTypeface(ResourcesCompat.getFont(this, R.font.nunito_bold));
                rotulo.setPadding(dp(22), dp(16), dp(22), dp(4));
                lista.addView(rotulo);
            }

            View cartao = inflador.inflate(R.layout.item_central_notificacao, lista, false);

            ((TextView) cartao.findViewById(R.id.tituloCentralNotificacao)).setText(notificacao.titulo);
            ((TextView) cartao.findViewById(R.id.mensagemCentralNotificacao)).setText(notificacao.mensagem);
            ((TextView) cartao.findViewById(R.id.horaCentralNotificacao)).setText(notificacao.hora);

            lista.addView(cartao, new ViewGroup.MarginLayoutParams(-1, -2));
        }
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}
