package com.example.ecociente.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecociente.R;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.model.TipoLista;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.ItemBarra;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.Navegacao;

public class TelaListaActivity extends AppCompatActivity {

    private static final String EXTRA_TIPO = "tipoLista";
    private static final String EXTRA_MORADOR = "listaMorador";

    private TipoLista tipo;
    private boolean morador;

    @NonNull
    public static Intent criarIntent(@NonNull Context contexto, @NonNull TipoLista tipo, boolean morador) {

        return new Intent(contexto, TelaListaActivity.class)
                .putExtra(EXTRA_TIPO, tipo)
                .putExtra(EXTRA_MORADOR, morador);
    }

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        JanelaEdgeToEdge.aplicar(this);

        tipo = (TipoLista) getIntent().getSerializableExtra(EXTRA_TIPO);

        if (tipo == null) {
            finish();
            return;
        }

        morador = getIntent().getBooleanExtra(EXTRA_MORADOR, false);

        setContentView(R.layout.activity_tela_lista);

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizTelaLista));

        ((TextView) findViewById(R.id.tituloTelaLista)).setText(tipo.titulo);

        findViewById(R.id.botaoVoltarTelaLista).setOnClickListener(view -> finish());

        preencherLista();

        configurarNavegacao();
    }

    private void preencherLista() {

        LinearLayout lista = findViewById(R.id.listaItens);

        LayoutInflater inflador = LayoutInflater.from(this);

        for (ItemLista item : tipo.itens()) {

            View linha = inflador.inflate(R.layout.item_linha_lista, lista, false);

            ((ImageView) linha.findViewById(R.id.iconeLinha)).setImageResource(tipo.icone);
            ((TextView) linha.findViewById(R.id.textoTituloLinha)).setText(item.titulo);
            ((TextView) linha.findViewById(R.id.textoSubtituloLinha)).setText(item.subtitulo);
            ((TextView) linha.findViewById(R.id.textoDestaqueLinha)).setText(item.destaque);

            linha.findViewById(R.id.pontoNaoLida).setVisibility(View.GONE);

            if (item.detalhes != null) {
                linha.setOnClickListener(view -> startActivity(DetalheItemActivity.criarIntent(this, item)));
            }

            lista.addView(linha);
        }
    }

    private void configurarNavegacao() {

        BarraNavegacaoView barra = findViewById(R.id.barraNavegacao);

        ItemBarra[] itens;
        int atual = ItensBarra.NENHUM;

        switch (tipo) {
            case GUIA:
                itens = morador ? ItensBarra.morador() : ItensBarra.usuario();
                atual = ItensBarra.SEGUNDO;
                break;

            case FEED:
                itens = ItensBarra.morador();
                atual = ItensBarra.QUARTO;
                break;

            case UNIDADES:
                itens = ItensBarra.sindico();
                atual = ItensBarra.QUARTO;
                break;

            case RANKING:
                itens = ItensBarra.morador();
                break;

            default:
                itens = ItensBarra.cooperativa();
                break;
        }

        int indiceAtual = atual;

        barra.configurar(itens);

        Navegacao.marcarNaBarra(this, barra, indiceAtual);

        barra.setOnAssistenteClickListener(view -> startActivity(new Intent(this, ChatActivity.class)));

        barra.setOnItemClickListener(
                indice -> {
                    if (tipo == TipoLista.CONDOMINIOS || tipo == TipoLista.AVALIACOES) {
                        Navegacao.irParaItemDaCooperativa(this, indice, indiceAtual);

                    } else if (tipo != TipoLista.UNIDADES) {
                        Navegacao.irParaItemDoUsuario(this, indice, indiceAtual, morador);
                    }
                });
    }
}
