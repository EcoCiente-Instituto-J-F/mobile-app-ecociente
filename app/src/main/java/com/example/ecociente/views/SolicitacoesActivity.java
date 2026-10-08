package com.example.ecociente.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import android.app.Activity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.ecociente.R;
import com.example.ecociente.model.ResultadoSolicitacoes;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.model.StatusAgendamento;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.Dimensoes;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.Navegacao;
import com.example.ecociente.viewmodels.PerfilViewModel;
import com.example.ecociente.viewmodels.SolicitacoesViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.List;

public class SolicitacoesActivity extends AppCompatActivity {

    public static final String EXTRA_HISTORICO = "historico";

    private static final int FALTAM_PARA_CARREGAR_MAIS = 3;

    private SolicitacoesViewModel viewModel;

    private SolicitacaoAdapter adaptador;

    private final ActivityResultLauncher<Intent> detalhe =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    resultado -> {
                        if (resultado.getResultCode() == Activity.RESULT_OK) {
                            viewModel.recarregar();
                        }
                    });

    private boolean historico;

    private SwipeRefreshLayout atualizacao;
    private View indicador;
    private TextView textoEstado;
    private MaterialButton botaoEstado;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        JanelaEdgeToEdge.aplicar(this);

        setContentView(R.layout.activity_solicitacoes);

        viewModel = new ViewModelProvider(this).get(SolicitacoesViewModel.class);

        historico = getIntent().getBooleanExtra(EXTRA_HISTORICO, false);

        if (historico) {
            viewModel.definirFiltroInicial(StatusAgendamento.REALIZADO);

            ((TextView) findViewById(R.id.tituloSolicitacoes)).setText(R.string.historico);
        }

        atualizacao = findViewById(R.id.atualizacaoSolicitacoes);
        indicador = findViewById(R.id.indicadorSolicitacoes);
        textoEstado = findViewById(R.id.textoEstadoSolicitacoes);
        botaoEstado = findViewById(R.id.botaoAcaoEstadoSolicitacoes);

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizSolicitacoes));

        configurarFiltros();

        configurarLista();

        configurarNavegacao();

        viewModel.getItens().observe(this, this::exibirItens);

        viewModel.getSituacao().observe(this, situacao -> atualizarEstado());

        viewModel.getCarregando().observe(this, carregando -> atualizarEstado());

        viewModel.filtrar(viewModel.getFiltro());
    }

    private void configurarFiltros() {

        ChipGroup grupo = findViewById(R.id.filtrosSolicitacoes);

        for (StatusAgendamento status : StatusAgendamento.values()) {

            Chip chip = new Chip(this, null, com.google.android.material.R.attr.chipStyle);

            chip.setId(View.generateViewId());
            chip.setText(status.getRotulo());
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setTag(status);
            chip.setChipBackgroundColor(ContextCompat.getColorStateList(this, R.color.fundo_chip_filtro));
            chip.setTextColor(ContextCompat.getColorStateList(this, R.color.texto_chip_filtro));
            chip.setChipStrokeColorResource(R.color.verde_escuro_principal);
            chip.setChipStrokeWidth(Dimensoes.dpParaPx(this, 1));

            grupo.addView(chip);

            chip.setChecked(status == viewModel.getFiltro());
        }

        grupo.setOnCheckedStateChangeListener(
                (ignorado, ids) -> {
                    if (ids.isEmpty()) {
                        return;
                    }

                    Chip marcado = findViewById(ids.get(0));

                    viewModel.filtrar((StatusAgendamento) marcado.getTag());
                });
    }

    private void configurarLista() {

        adaptador =
                new SolicitacaoAdapter(
                        solicitacao ->
                                detalhe.launch(
                                        SolicitacaoDetalheActivity.criarIntent(this, solicitacao)));

        RecyclerView lista = findViewById(R.id.listaSolicitacoes);

        LinearLayoutManager gerenciador = new LinearLayoutManager(this);

        lista.setLayoutManager(gerenciador);

        lista.setAdapter(adaptador);

        lista.addOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recycler, int dx, int dy) {

                        int restantes =
                                adaptador.getItemCount()
                                        - gerenciador.findLastVisibleItemPosition();

                        if (dy > 0 && restantes <= FALTAM_PARA_CARREGAR_MAIS) {
                            viewModel.carregarMais();
                        }
                    }
                });

        atualizacao.setColorSchemeColors(ContextCompat.getColor(this, R.color.verde_escuro_principal));

        atualizacao.setOnRefreshListener(viewModel::recarregar);
    }

    private void exibirItens(@NonNull List<Solicitacao> itens) {

        adaptador.submitList(itens);

        atualizarEstado();
    }

    private void atualizarEstado() {

        boolean carregando = Boolean.TRUE.equals(viewModel.getCarregando().getValue());

        boolean semItens = adaptador.getCurrentList().isEmpty();

        ResultadoSolicitacoes.Tipo situacao = viewModel.getSituacao().getValue();

        if (!carregando) {
            atualizacao.setRefreshing(false);
        }

        indicador.setVisibility(carregando && semItens && !atualizacao.isRefreshing() ? View.VISIBLE : View.GONE);

        if (carregando || situacao == null || !semItens && situacao == ResultadoSolicitacoes.Tipo.SUCESSO) {
            ocultarEstado();
            return;
        }

        if (situacao == ResultadoSolicitacoes.Tipo.SESSAO_EXPIRADA) {
            mostrarEstado(R.string.solicitacoes_sessao_expirada, R.string.entrar_novamente, view -> sair());

        } else if (situacao == ResultadoSolicitacoes.Tipo.SEM_ACESSO) {
            mostrarEstado(R.string.solicitacoes_sem_acesso, 0, null);

        } else if (situacao == ResultadoSolicitacoes.Tipo.ERRO) {
            mostrarEstado(R.string.solicitacoes_erro, R.string.tentar_novamente, view -> viewModel.recarregar());

        } else if (semItens) {
            mostrarEstado(R.string.solicitacoes_vazio, 0, null);

        } else {
            ocultarEstado();
        }
    }

    private void mostrarEstado(int mensagem, int rotuloBotao, View.OnClickListener acao) {

        textoEstado.setText(mensagem);
        textoEstado.setVisibility(View.VISIBLE);

        if (rotuloBotao == 0) {
            botaoEstado.setVisibility(View.GONE);
            return;
        }

        botaoEstado.setText(rotuloBotao);
        botaoEstado.setOnClickListener(acao);
        botaoEstado.setVisibility(View.VISIBLE);
    }

    private void ocultarEstado() {

        textoEstado.setVisibility(View.GONE);
        botaoEstado.setVisibility(View.GONE);
    }

    private void sair() {

        new ViewModelProvider(this).get(PerfilViewModel.class).encerrarSessao();

        Navegacao.abrirLoginLimpandoPilha(this);
    }

    private void configurarNavegacao() {

        BarraNavegacaoView barra = findViewById(R.id.barraNavegacao);

        barra.configurar(ItensBarra.cooperativa());

        Navegacao.marcarNaBarra(this, barra, indiceAtual());

        barra.setOnAssistenteClickListener(view -> startActivity(new Intent(this, ChatActivity.class)));

        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDaCooperativa(this, indice, indiceAtual()));
    }

    private int indiceAtual() {
        return historico ? ItensBarra.QUARTO : ItensBarra.SEGUNDO;
    }
}
