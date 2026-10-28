package com.example.ecociente.views;

import com.example.ecociente.model.MovimentacaoPontos;
import com.example.ecociente.repository.PerfilApiRepository;
import com.example.ecociente.repository.PontuacaoRepository;
import com.example.ecociente.util.FormatoDataApi;
import com.example.ecociente.ui.Motion;
import android.content.Context;
import android.widget.RadioGroup;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.ecociente.repository.FavoritosRepository;
import com.example.ecociente.model.FiltroGuia;
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
import com.example.ecociente.model.PostagemFeed;
import com.example.ecociente.repository.EducacaoRepository;
import com.example.ecociente.repository.PostagemRepository;
import com.example.ecociente.repository.AvaliacaoRepository;
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
import com.example.ecociente.ui.ChipEco;
import com.example.ecociente.ui.IndicadorCarregamento;
import android.widget.FrameLayout;

public class TelaListaActivity extends AppCompatActivity {

    private static final String EXTRA_TIPO = "tipoLista";
    private static final String EXTRA_MORADOR = "listaMorador";

    private TipoLista tipo;
    private boolean morador;
    private java.util.List<ItemLista> guiaCompleto;
    private final FiltroGuia filtro = new FiltroGuia();

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

        if (tipo == TipoLista.GUIA) {
            startActivity(new Intent(this, BibliotecaActivity.class)
                    .putExtra(BibliotecaActivity.EXTRA_MORADOR, morador));
            finish();
            return;
        }

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

        if (tipo == TipoLista.GUIA) {
            configurarAcoesDoGuia();
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

        if (tipo == TipoLista.MEUS_DESCARTES) {
            carregarMeusDescartes(lista);
            return;
        }

        if (tipo == TipoLista.SALVOS) {
            carregarSalvos(lista);
            return;
        }

        if (tipo == TipoLista.AVALIACOES) {
            carregarAvaliacoes(lista);
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

        indicadorDeCarregamento(lista, R.string.guia_carregando);

        new EducacaoRepository()
                .listarGuia(
                        this,
                        (sucesso, itens) -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            lista.removeAllViews();

                            if (!sucesso) {
                                erroComTentar(lista, R.string.guia_erro);
                                return;
                            }

                            guiaCompleto = itens;

                            mostrarGuia();
                        });
    }

    // O guia com o filtro e a ordem escolhidos.
    private void mostrarGuia() {

        LinearLayout lista = findViewById(R.id.listaItens);

        lista.removeAllViews();

        if (guiaCompleto.isEmpty()) {
            textoDeEstado(lista, R.string.guia_vazio);
            return;
        }

        java.util.List<ItemLista> itens = filtro.aplicar(guiaCompleto);

        if (itens.isEmpty()) {
            textoDeEstado(lista, R.string.guia_filtro_vazio);
            return;
        }

        exibirItens(lista, itens);
    }

    // "Meus descartes": as postagens do próprio usuário na ds-mobile-api.
    private void carregarMeusDescartes(LinearLayout lista) {

        indicadorDeCarregamento(lista, R.string.meus_descartes_carregando);

        new PostagemRepository()
                .minhas(
                        this,
                        postagens -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            lista.removeAllViews();

                            if (postagens == null) {
                                erroComTentar(lista, R.string.meus_descartes_erro);
                                return;
                            }

                            if (postagens.isEmpty()) {
                                textoDeEstado(lista, R.string.meus_descartes_vazio);
                                return;
                            }

                            java.util.List<ItemLista> itens = new java.util.ArrayList<>();

                            for (PostagemFeed postagem : postagens) {
                                itens.add(
                                        new ItemLista(
                                                postagem.getCategoria().isEmpty()
                                                        ? getString(R.string.descarte)
                                                        : postagem.getCategoria(),
                                                postagem.getQuando(),
                                                postagem.getStatus()));
                            }

                            exibirItens(lista, itens);
                        });
    }

    private void carregarSalvos(LinearLayout lista) {

        indicadorDeCarregamento(lista, R.string.guia_carregando);

        new EducacaoRepository()
                .listarGuia(
                        this,
                        (sucesso, itens) -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            if (!sucesso) {
                                lista.removeAllViews();
                                erroComTentar(lista, R.string.guia_erro);
                                return;
                            }

                            guiaCompleto = itens;

                            mostrarSalvos();
                        });
    }

    // Avaliações da cooperativa na ds-mobile-api; o id dela vem do perfil (/usuarios/me).
    private void carregarAvaliacoes(LinearLayout lista) {

        indicadorDeCarregamento(lista, R.string.avaliacoes_carregando);

        if (PerfilApiRepository.cooperativaId(this) > 0) {
            buscarAvaliacoes(lista);
            return;
        }

        new PerfilApiRepository()
                .atualizar(
                        this,
                        () -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            if (PerfilApiRepository.cooperativaId(this) > 0) {
                                buscarAvaliacoes(lista);
                                return;
                            }

                            lista.removeAllViews();
                            erroComTentar(lista, R.string.avaliacoes_erro);
                        });
    }

    private void buscarAvaliacoes(LinearLayout lista) {

        new AvaliacaoRepository()
                .listar(
                        this,
                        PerfilApiRepository.cooperativaId(this),
                        avaliacoes -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            lista.removeAllViews();

                            if (avaliacoes == null) {
                                erroComTentar(lista, R.string.avaliacoes_erro);
                                return;
                            }

                            if (avaliacoes.getItens().isEmpty()) {
                                textoDeEstado(lista, R.string.avaliacoes_vazio);
                                return;
                            }

                            java.util.List<ItemLista> itens = new java.util.ArrayList<>();

                            itens.add(
                                    new ItemLista(
                                            getString(R.string.avaliacoes_media_titulo),
                                            getResources()
                                                    .getQuantityString(
                                                            R.plurals.avaliacoes_total,
                                                            (int) avaliacoes.getTotal(),
                                                            (int) avaliacoes.getTotal()),
                                            String.format(
                                                    new Locale("pt", "BR"), "%.1f", avaliacoes.getMedia())));
                            itens.addAll(avaliacoes.getItens());

                            exibirItens(lista, itens);
                        });
    }

    private void mostrarSalvos() {

        LinearLayout lista = findViewById(R.id.listaItens);

        lista.removeAllViews();

        java.util.List<ItemLista> salvos = new java.util.ArrayList<>();

        for (ItemLista item : guiaCompleto) {
            if (item.chave != null && FavoritosRepository.ehSalvo(this, item.chave)) {
                salvos.add(item);
            }
        }

        if (salvos.isEmpty()) {
            textoDeEstado(lista, R.string.salvos_vazio);
            return;
        }

        exibirItens(lista, salvos);
    }

    private void configurarAcoesDoGuia() {

        View salvos = findViewById(R.id.botaoSalvosTelaLista);
        View filtrar = findViewById(R.id.botaoFiltrarTelaLista);

        salvos.setVisibility(View.VISIBLE);
        filtrar.setVisibility(View.VISIBLE);

        salvos.setOnClickListener(view -> startActivity(criarIntent(this, TipoLista.SALVOS, morador)));

        filtrar.setOnClickListener(
                view -> {
                    if (guiaCompleto != null && !guiaCompleto.isEmpty()) {
                        abrirFiltro();
                    }
                });
    }

    private void abrirFiltro() {

        BottomSheetDialog folha = new BottomSheetDialog(this);

        View conteudo = getLayoutInflater().inflate(R.layout.dialog_filtro_guia, new FrameLayout(this), false);

        ChipGroup categorias = conteudo.findViewById(R.id.chipsCategoria);
        ChipGroup tempos = conteudo.findViewById(R.id.chipsTempo);
        RadioGroup ordem = conteudo.findViewById(R.id.grupoOrdem);

        for (String categoria : FiltroGuia.categoriasDe(guiaCompleto)) {
            Chip chip = ChipEco.criar(this, categoria, filtro.getCategorias().contains(categoria));
            chip.setTag(categoria);
            categorias.addView(chip);
        }

        int[] rotulos = {R.string.filtro_tempo_ate3, R.string.filtro_tempo_3a6, R.string.filtro_tempo_mais6};

        for (FiltroGuia.Tempo tempo : FiltroGuia.Tempo.values()) {
            Chip chip = ChipEco.criar(this, getString(rotulos[tempo.ordinal()]), filtro.getTempos().contains(tempo));
            chip.setTag(tempo);
            tempos.addView(chip);
        }

        ordem.check(
                filtro.getOrdem() == FiltroGuia.Ordem.A_Z ? R.id.radioOrdemAz : R.id.radioOrdemCurso);

        conteudo.findViewById(R.id.botaoLimparFiltro)
                .setOnClickListener(
                        view -> {
                            filtro.limpar();
                            folha.dismiss();
                            mostrarGuia();
                        });

        conteudo.findViewById(R.id.botaoAplicarFiltro)
                .setOnClickListener(
                        view -> {
                            filtro.getCategorias().clear();
                            filtro.getTempos().clear();

                            for (int i = 0; i < categorias.getChildCount(); i++) {
                                Chip chip = (Chip) categorias.getChildAt(i);

                                if (chip.isChecked()) {
                                    filtro.getCategorias().add((String) chip.getTag());
                                }
                            }

                            for (int i = 0; i < tempos.getChildCount(); i++) {
                                Chip chip = (Chip) tempos.getChildAt(i);

                                if (chip.isChecked()) {
                                    filtro.getTempos().add((FiltroGuia.Tempo) chip.getTag());
                                }
                            }

                            filtro.setOrdem(
                                    ordem.getCheckedRadioButtonId() == R.id.radioOrdemAz
                                            ? FiltroGuia.Ordem.A_Z
                                            : FiltroGuia.Ordem.ORDEM_DO_CURSO);

                            folha.dismiss();
                            mostrarGuia();
                        });

        folha.setContentView(conteudo);
        folha.show();
    }

    // Tempo de leitura e marcador de salvo nas linhas do guia.
    private void configurarSalvar(View linha, ItemLista item) {

        if (item.chave == null || (tipo != TipoLista.GUIA && tipo != TipoLista.SALVOS)) {
            return;
        }

        ((TextView) linha.findViewById(R.id.textoDestaqueLinha))
                .setText(
                        getString(
                                R.string.guia_minutos,
                                FiltroGuia.minutosDeLeitura(FiltroGuia.conteudoDe(item))));

        ImageView marcador = linha.findViewById(R.id.botaoSalvarLinha);

        marcador.setVisibility(View.VISIBLE);

        atualizarMarcador(marcador, FavoritosRepository.ehSalvo(this, item.chave));

        marcador.setOnClickListener(
                view -> {
                    boolean salvo = FavoritosRepository.alternar(this, item.chave);

                    atualizarMarcador(marcador, salvo);

                    if (tipo == TipoLista.SALVOS && !salvo) {
                        mostrarSalvos();
                    }
                });
    }

    private void atualizarMarcador(ImageView marcador, boolean salvo) {

        marcador.setImageResource(salvo ? R.drawable.ic_marcador_cheio : R.drawable.ic_marcador);
        marcador.setContentDescription(getString(salvo ? R.string.remover_salvo : R.string.salvar_conteudo));
    }

    // Ranking dos moradores da ds-pontuacao-api; sem resposta (403, fora do ar), mostra o exemplo.
    private void carregarRanking(LinearLayout lista) {

        indicadorDeCarregamento(lista, R.string.ranking_carregando);

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

                            if (moradores == null) {
                                erroComTentar(lista, R.string.ranking_erro);
                                return;
                            }

                            java.util.List<ItemLista> itens = moradores;

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

        indicadorDeCarregamento(lista, R.string.pontos_carregando_historico);

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

                                    lista.removeAllViews();

                                    if (saldo == null && pagina == null) {
                                        erroComTentar(lista, R.string.pontos_erro);
                                        return;
                                    }

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

    private View indicadorDeCarregamento(LinearLayout lista, int mensagem) {
        return IndicadorCarregamento.mostrar(lista, mensagem);
    }

    // Falha ao carregar: a mensagem e um botão que refaz a consulta da lista inteira.
    private void erroComTentar(LinearLayout lista, int mensagem) {

        textoDeEstado(lista, mensagem);

        com.google.android.material.button.MaterialButton tentar =
                (com.google.android.material.button.MaterialButton)
                        getLayoutInflater().inflate(R.layout.botao_dialogo, lista, false);

        tentar.setText(R.string.tentar_novamente);
        tentar.setOnClickListener(
                view -> {
                    lista.removeAllViews();
                    preencherLista();
                });

        lista.addView(tentar);
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

            configurarSalvar(linha, item);

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
            case SALVOS:
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
            case MEUS_DESCARTES:
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
