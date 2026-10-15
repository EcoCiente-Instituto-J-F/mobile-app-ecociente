package com.example.ecociente.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecociente.R;
import com.example.ecociente.ui.DialogoEco;
import com.example.ecociente.model.SindicoHistoricoColeta;
import com.example.ecociente.viewmodels.SindicoColetaViewModel;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.InsetsSistema;
import androidx.core.view.ViewCompat;
import com.example.ecociente.model.SindicoSolicitacao;
import com.example.ecociente.model.StatusAgendamento;
import com.example.ecociente.ui.FormatoDataApi;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.Navegacao;

/** Histórico ilustrativo: avaliações ficam só na memória desta execução. */
public final class SindicoHistoricoActivity extends AppCompatActivity {
    private static final String ESTADO_FILTRO = "filtroHistorico";
    private SindicoColetaViewModel viewModel;
    private LinearLayout lista;
    private int filtro;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        JanelaEdgeToEdge.aplicar(this);
        setContentView(R.layout.activity_sindico_historico);

        View raiz = findViewById(R.id.raizSindicoHistorico);
        InsetsSistema.aplicarComoPadding(raiz);

        viewModel = new ViewModelProvider(this).get(SindicoColetaViewModel.class);
        lista = findViewById(R.id.listaSindicoHistorico);
        filtro = estadoSalvo == null ? 0 : estadoSalvo.getInt(ESTADO_FILTRO, 0);
        findViewById(R.id.filtrarHistorico).setOnClickListener(view -> abrirFiltro());

        BarraNavegacaoView barra = findViewById(R.id.barraInferiorSindico);
        barra.configurar(ItensBarra.sindico());
        Navegacao.marcarNaBarra(this, barra, ItensBarra.TERCEIRO);
        barra.setOnAssistenteClickListener(
                view -> startActivity(new Intent(this, ChatActivity.class)));
        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDoSindico(this, indice, ItensBarra.TERCEIRO));

        getSupportFragmentManager().setFragmentResultListener(
                SindicoAvaliarColetaDialogFragment.RESULTADO_AVALIACAO, this,
                (chave, resultado) -> {
                    mostrarHistorico();
                    Toast.makeText(getApplicationContext(), R.string.sindico_avaliacao_salva_somente_nesta_demo, Toast.LENGTH_LONG).show();
                });
        mostrarHistorico();
    }

    private void mostrarHistorico() {
        lista.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        int encontrados = 0;
        if (filtro == 0) {
            for (SindicoSolicitacao solicitacao : viewModel.getSolicitacoes()) {
                encontrados++;
                lista.addView(criarCartaoSolicitacao(inflater, solicitacao), margemEntreCartoes());
            }
        }
        for (SindicoHistoricoColeta coleta : viewModel.getHistorico()) {
            int nota = viewModel.getAvaliacao(coleta.getId());
            if ((filtro == 1 && nota != 0) || (filtro == 2 && nota == 0)) continue;
            encontrados++;
            View cartao = inflater.inflate(R.layout.item_sindico_historico, lista, false);
            ((TextView) cartao.findViewById(R.id.nomeHistorico)).setText(coleta.getCooperativa());
            ((TextView) cartao.findViewById(R.id.dataHistorico)).setText(coleta.getData());
            ((TextView) cartao.findViewById(R.id.avaliacaoPublicaHistorico))
                    .setText(coleta.getAvaliacaoPublica());

            FrameLayout acao = cartao.findViewById(R.id.acaoAvaliarHistorico);
            TextView textoAcao = cartao.findViewById(R.id.textoAcaoAvaliarHistorico);
            if (nota > 0) {
                textoAcao.setText(getString(R.string.sindico_nota_texto, nota));
                textoAcao.setBackgroundResource(R.drawable.sindico_status_concluida);
                textoAcao.setTextColor(getColor(R.color.branco));
                acao.setContentDescription(getString(R.string.sindico_avaliacao_local, nota));
            } else {
                acao.setContentDescription(getString(R.string.sindico_avaliar_coleta_de, coleta.getCooperativa()));
            }
            acao.setOnClickListener(view -> abrirAvaliacao(coleta));
            LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(-1, -2);
            parametros.bottomMargin = dp(12);
            lista.addView(cartao, parametros);
        }
        if (encontrados == 0) {
            TextView vazio = new TextView(this);
            vazio.setText(filtro == 1 ? R.string.sindico_todas_as_coletas_de_exemplo_ja_for : R.string.sindico_nenhuma_coleta_avaliada_neste_filt);
            vazio.setTextColor(getColor(R.color.cinza_texto_home));
            vazio.setTextSize(14);
            vazio.setPadding(dp(8), dp(28), dp(8), dp(28));
            lista.addView(vazio);
        }
    }

    private LinearLayout.LayoutParams margemEntreCartoes() {
        LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(-1, -2);
        parametros.bottomMargin = dp(12);
        return parametros;
    }

    private View criarCartaoSolicitacao(LayoutInflater inflater, SindicoSolicitacao solicitacao) {
        View cartao = inflater.inflate(R.layout.item_sindico_historico, lista, false);
        ((TextView) cartao.findViewById(R.id.nomeHistorico)).setText(solicitacao.getCooperativa());
        ((TextView) cartao.findViewById(R.id.dataHistorico)).setText(getString(
                R.string.sindico_solicitacao_andamento,
                FormatoDataApi.data(solicitacao.getInicio()), FormatoDataApi.hora(solicitacao.getInicio())));
        cartao.findViewById(R.id.linhaAvaliacaoHistorico).setVisibility(View.GONE);
        cartao.findViewById(R.id.acaoAvaliarHistorico).setVisibility(View.GONE);
        TextView etiqueta = cartao.findViewById(R.id.etiquetaHistorico);
        StatusAgendamento status = solicitacao.getStatus();
        etiqueta.setText(status.getEtiqueta());
        etiqueta.setTextColor(getColor(status.getTextoEtiqueta()));
        ViewCompat.setBackgroundTintList(etiqueta, getColorStateList(status.getFundoEtiqueta()));
        cartao.setOnClickListener(view -> startActivity(SolicitacaoDetalheActivity.criarIntentSindico(
                this, solicitacao.paraSolicitacao(getString(R.string.sindico_condominio_demo)),
                solicitacao.getCooperativa())));
        return cartao;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mostrarHistorico();
    }

    private void abrirFiltro() {
        new DialogoEco.Builder(this)
                .titulo(R.string.sindico_filtrar_historico)
                .botao(R.string.sindico_todas, filtro == 0 ? DialogoEco.Estilo.PREENCHIDO : DialogoEco.Estilo.CONTORNO,
                        () -> aplicarFiltro(0))
                .botao(R.string.sindico_para_avaliar, filtro == 1 ? DialogoEco.Estilo.PREENCHIDO : DialogoEco.Estilo.CONTORNO,
                        () -> aplicarFiltro(1))
                .botao(R.string.sindico_avaliadas, filtro == 2 ? DialogoEco.Estilo.PREENCHIDO : DialogoEco.Estilo.CONTORNO,
                        () -> aplicarFiltro(2))
                .botao(R.string.perfil_cancelar, DialogoEco.Estilo.CONTORNO, null)
                .mostrar();
    }

    private void aplicarFiltro(int novoFiltro) {
        filtro = novoFiltro;
        mostrarHistorico();
    }

    private void abrirAvaliacao(SindicoHistoricoColeta coleta) {
        if (getSupportFragmentManager().findFragmentByTag("avaliarColetaSindico") == null) {
            SindicoAvaliarColetaDialogFragment.criar(coleta.getId(), coleta.getCooperativa())
                    .show(getSupportFragmentManager(), "avaliarColetaSindico");
        }
    }

    private void navegar(Class<?> destino) {
        Intent intent = new Intent(this, destino);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        estado.putInt(ESTADO_FILTRO, filtro);
        super.onSaveInstanceState(estado);
    }
}
