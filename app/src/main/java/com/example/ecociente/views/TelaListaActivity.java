package com.example.ecociente.views;

import com.example.ecociente.model.MovimentacaoPontos;
import com.example.ecociente.repository.PerfilApiRepository;
import com.example.ecociente.repository.PontuacaoRepository;
import com.example.ecociente.ui.FormatoDataApi;
import com.example.ecociente.ui.Motion;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecociente.R;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.repository.EducacaoRepository;
import com.example.ecociente.repository.RankingExternoRepository;
import com.example.ecociente.model.TipoLista;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.ItemBarra;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.Navegacao;
import java.text.NumberFormat;
import java.util.Locale;

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

        Motion.entrar(
                this,
                R.id.tituloTelaLista,
                R.id.listaItens);

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizTelaLista));

        ((TextView) findViewById(R.id.tituloTelaLista)).setText(tipo.titulo);

        findViewById(R.id.botaoVoltarTelaLista).setOnClickListener(view -> finish());

        // Guia e Feed são abas da barra inferior: sem seta, com o título na altura das outras abas.
        if (tipo == TipoLista.GUIA || tipo == TipoLista.FEED) {
            findViewById(R.id.botaoVoltarTelaLista).setVisibility(View.GONE);

            TextView titulo = findViewById(R.id.tituloTelaLista);

            ViewGroup.MarginLayoutParams parametros = (ViewGroup.MarginLayoutParams) titulo.getLayoutParams();

            parametros.topMargin = Math.round(22 * getResources().getDisplayMetrics().density);

            titulo.setLayoutParams(parametros);
        }

        preencherLista();

        configurarNavegacao();
    }

    private void preencherLista() {

        LinearLayout lista = findViewById(R.id.listaItens);

        if (tipo == TipoLista.GUIA) {
            carregarGuia(lista);
            return;
        }

        if (tipo == TipoLista.RANKING) {
            carregarRanking(lista);
            return;
        }

        if (tipo == TipoLista.PONTOS) {
            carregarPontos(lista);
            return;
        }

        java.util.List<ItemLista> itens = tipo.itens();

        if (itens.isEmpty()) {
            textoDeEstado(lista, R.string.lista_vazia);
            return;
        }

        exibirItens(lista, itens);
    }

    private void carregarGuia(LinearLayout lista) {

        TextView estado = textoDeEstado(lista, R.string.guia_carregando);

        new EducacaoRepository()
                .listarGuia(
                        this,
                        (sucesso, itens) -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            if (!sucesso) {
                                estado.setText(R.string.guia_erro);
                                return;
                            }

                            lista.removeAllViews();

                            if (itens.isEmpty()) {
                                textoDeEstado(lista, R.string.guia_vazio);
                                return;
                            }

                            exibirItens(lista, itens);
                        });
    }

    // Ranking dos moradores da ds-pontuacao-api; sem resposta (403, fora do ar), mostra o exemplo.
    private void carregarRanking(LinearLayout lista) {

        TextView estado = textoDeEstado(lista, R.string.ranking_carregando);

        new RankingExternoRepository()
                .listarMoradores(
                        this,
                        PerfilApiRepository.condominioId(this),
                        20,
                        moradores -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            lista.removeAllViews();

                            java.util.List<ItemLista> itens = moradores != null ? moradores : tipo.itens();

                            if (itens.isEmpty()) {
                                textoDeEstado(lista, R.string.ranking_sem_pontuacao);
                                return;
                            }

                            exibirItens(lista, itens);

                            mostrarSuaPosicao(lista);
                        });
    }

    // "Você: 3º de 12" no topo do ranking, quando a API sabe onde o morador está.
    private void mostrarSuaPosicao(LinearLayout lista) {

        new PontuacaoRepository()
                .posicaoNoRanking(
                        this,
                        posicao -> {
                            if (posicao == null || isFinishing() || isDestroyed()) {
                                return;
                            }

                            ItemLista voce =
                                    posicao.estaNoRanking()
                                            ? new ItemLista(
                                                    getString(R.string.sua_posicao_titulo),
                                                    getString(
                                                            R.string.sua_posicao_valor,
                                                            posicao.getPosicao(),
                                                            posicao.getTotalParticipantes()),
                                                    formatarPontos(posicao.getPontos()))
                                            : new ItemLista(
                                                    getString(R.string.sua_posicao_titulo),
                                                    getString(R.string.sua_posicao_fora),
                                                    "");

                            LinearLayout area = new LinearLayout(this);

                            area.setOrientation(LinearLayout.VERTICAL);

                            exibirItens(area, java.util.Collections.singletonList(voce));

                            if (area.getChildCount() > 0) {
                                View linha = area.getChildAt(0);

                                area.removeView(linha);
                                lista.addView(linha, 0);
                            }
                        });
    }

    // Saldo e histórico de pontos do usuário (ds-pontuacao-api).
    private void carregarPontos(LinearLayout lista) {

        TextView estado = textoDeEstado(lista, R.string.pontos_carregando_historico);

        PontuacaoRepository repositorio = new PontuacaoRepository();

        repositorio.saldo(
                this,
                saldo ->
                        repositorio.movimentacoes(
                                this,
                                0,
                                20,
                                pagina -> {
                                    if (isFinishing() || isDestroyed()) {
                                        return;
                                    }

                                    if (saldo == null && pagina == null) {
                                        estado.setText(R.string.pontos_erro);
                                        return;
                                    }

                                    lista.removeAllViews();

                                    java.util.List<ItemLista> itens = new java.util.ArrayList<>();

                                    itens.add(
                                            new ItemLista(
                                                    getString(R.string.pontos_saldo_atual),
                                                    "",
                                                    formatarPontos(saldo == null ? 0 : saldo)));

                                    if (pagina != null) {
                                        for (MovimentacaoPontos movimentacao : pagina.itens) {
                                            itens.add(itemDaMovimentacao(movimentacao));
                                        }
                                    }

                                    exibirItens(lista, itens);

                                    if (pagina != null && pagina.itens.isEmpty()) {
                                        textoDeEstado(lista, R.string.pontos_sem_movimentacao);
                                    }
                                }));
    }

    private ItemLista itemDaMovimentacao(MovimentacaoPontos movimentacao) {

        int titulo;

        if (movimentacao.getTipo() == MovimentacaoPontos.Tipo.ESTORNO) {
            titulo = R.string.pontos_estorno;
        } else if (movimentacao.getTipo() == MovimentacaoPontos.Tipo.RESTAURACAO) {
            titulo = R.string.pontos_restauracao;
        } else if (movimentacao.getOrigem() == MovimentacaoPontos.Origem.POSTAGEM) {
            titulo = R.string.pontos_origem_descarte;
        } else if (movimentacao.getOrigem() == MovimentacaoPontos.Origem.QUIZ) {
            titulo = R.string.pontos_origem_quiz;
        } else {
            titulo = R.string.pontos_origem_outra;
        }

        int sinal = movimentacao.getPontosComSinal();

        return new ItemLista(
                getString(titulo),
                FormatoDataApi.dataCompacta(movimentacao.getOcorridoEm()),
                getString(
                        sinal < 0 ? R.string.pontos_valor_negativo : R.string.pontos_valor_positivo,
                        NumberFormat.getIntegerInstance(new Locale("pt", "BR")).format(Math.abs(sinal))));
    }

    private String formatarPontos(long pontos) {
        return getString(
                R.string.pontos_saldo, NumberFormat.getIntegerInstance(new Locale("pt", "BR")).format(pontos));
    }

    private TextView textoDeEstado(LinearLayout lista, int mensagem) {

        TextView texto = new TextView(this);

        texto.setText(mensagem);
        texto.setTextColor(getColor(R.color.cinza_texto_auxiliar));
        texto.setTextSize(14);
        texto.setGravity(android.view.Gravity.CENTER);
        texto.setPadding(48, 96, 48, 0);

        lista.addView(texto);

        return texto;
    }

    private void exibirItens(LinearLayout lista, java.util.List<ItemLista> itens) {

        LayoutInflater inflador = LayoutInflater.from(this);

        for (ItemLista item : itens) {

            View linha = inflador.inflate(R.layout.item_linha_lista, lista, false);

            ((ImageView) linha.findViewById(R.id.iconeLinha)).setImageResource(tipo.icone);
            ((TextView) linha.findViewById(R.id.textoTituloLinha)).setText(item.titulo);
            TextView subtitulo = linha.findViewById(R.id.textoSubtituloLinha);
            subtitulo.setText(item.subtitulo);
            subtitulo.setVisibility(item.subtitulo.isEmpty() ? View.GONE : View.VISIBLE);
            ((TextView) linha.findViewById(R.id.textoDestaqueLinha)).setText(item.destaque);

            linha.findViewById(R.id.pontoNaoLida).setVisibility(View.GONE);

            if (item.detalhes != null) {
                linha.setOnClickListener(view -> startActivity(DetalheItemActivity.criarIntent(this, item)));
                Motion.pressFeedback(linha);
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

            case RANKING:
            case PONTOS:
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

                    } else {
                        Navegacao.irParaItemDoUsuario(this, indice, indiceAtual, morador);
                    }
                });
    }
}
