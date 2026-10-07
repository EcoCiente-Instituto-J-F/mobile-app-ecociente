package com.example.ecociente.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.ecociente.R;
import com.example.ecociente.model.Conversa;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.Navegacao;

public class MensagensActivity extends AppCompatActivity {

    private static final int[] IDS_ABAS = {R.id.abaTodos, R.id.abaNaoLidos, R.id.abaArquivadas};

    private LinearLayout lista;
    private View textoSemConversas;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        JanelaEdgeToEdge.aplicar(this);

        setContentView(R.layout.activity_mensagens);

        lista = findViewById(R.id.listaConversas);
        textoSemConversas = findViewById(R.id.textoSemConversas);

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizMensagens));

        findViewById(R.id.botaoVoltarMensagens).setOnClickListener(view -> finish());

        for (int i = 0; i < IDS_ABAS.length; i++) {
            int aba = i;

            findViewById(IDS_ABAS[i]).setOnClickListener(view -> selecionarAba(aba));
        }

        selecionarAba(0);

        configurarNavegacao();
    }

    private void selecionarAba(int aba) {

        for (int i = 0; i < IDS_ABAS.length; i++) {

            TextView texto = findViewById(IDS_ABAS[i]);

            boolean selecionada = i == aba;

            texto.setBackgroundResource(selecionada ? R.drawable.fundo_aba_selecionada : 0);

            texto.setTextColor(
                    ContextCompat.getColor(
                            this, selecionada ? R.color.branco : R.color.verde_escuro_principal));
        }

        exibirConversas(aba);
    }

    private void exibirConversas(int aba) {

        lista.removeAllViews();

        LayoutInflater inflador = LayoutInflater.from(this);

        for (Conversa conversa : Conversa.exemplos()) {

            if (!pertence(conversa, aba)) {
                continue;
            }

            View item = inflador.inflate(R.layout.item_conversa, lista, false);

            ((TextView) item.findViewById(R.id.textoNomeConversa)).setText(conversa.nome);
            ((TextView) item.findViewById(R.id.textoUltimaMensagem)).setText(conversa.ultimaMensagem);
            ((TextView) item.findViewById(R.id.textoHoraConversa)).setText(conversa.hora);

            item.findViewById(R.id.pontoNaoLida)
                    .setVisibility(conversa.naoLida ? View.VISIBLE : View.INVISIBLE);

            lista.addView(item);
        }

        textoSemConversas.setVisibility(lista.getChildCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private boolean pertence(Conversa conversa, int aba) {

        if (aba == 2) {
            return conversa.arquivada;
        }

        return !conversa.arquivada && (aba == 0 || conversa.naoLida);
    }

    private void configurarNavegacao() {

        BarraNavegacaoView barra = findViewById(R.id.barraNavegacao);

        barra.configurar(ItensBarra.cooperativa());

        Navegacao.marcarNaBarra(this, barra, ItensBarra.TERCEIRO);

        barra.setOnAssistenteClickListener(view -> startActivity(new Intent(this, ChatActivity.class)));

        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDaCooperativa(this, indice, ItensBarra.TERCEIRO));
    }
}
