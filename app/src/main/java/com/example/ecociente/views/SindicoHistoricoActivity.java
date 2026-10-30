package com.example.ecociente.views;

import com.example.ecociente.repository.PerfilApiRepository;
import com.example.ecociente.ui.Motion;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecociente.R;
import com.example.ecociente.ui.DialogoEco;
import com.example.ecociente.model.ResultadoSolicitacoes;
import com.example.ecociente.model.SindicoCooperativa;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.repository.CalendarioExternoRepository;
import com.example.ecociente.repository.CooperativaRepository;
import com.example.ecociente.viewmodels.SindicoColetaViewModel;
import com.example.ecociente.ui.IndicadorCarregamento;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.InsetsSistema;
import androidx.core.view.ViewCompat;
import com.example.ecociente.model.StatusAgendamento;
import com.example.ecociente.util.FormatoDataApi;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.Navegacao;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.widget.EditText;
import android.widget.RatingBar;
import com.example.ecociente.repository.AvaliacaoColetaRepository;
import com.example.ecociente.ui.AvisoFlutuante;

/** Coletas do condomínio vindas da ds-calendario-api, da mais recente para a mais antiga. */
public final class SindicoHistoricoActivity extends AppCompatActivity {
    private static final String ESTADO_FILTRO = "filtroHistorico";
    private static final int TODAS = 0;
    private static final int EM_ANDAMENTO = 1;
    private static final int CONCLUIDAS = 2;
    private static final int ENCERRADAS = 3;

    private SindicoColetaViewModel viewModel;
    private LinearLayout lista;
    private int filtro;
    private boolean carregadas;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        JanelaEdgeToEdge.aplicar(this);
        setContentView(R.layout.activity_sindico_historico);

        Motion.entrar(
                this,
                R.id.tituloSindicoHistorico,
                R.id.listaSindicoHistorico);

        View raiz = findViewById(R.id.raizSindicoHistorico);
        InsetsSistema.aplicarComoPadding(raiz);

        viewModel = new ViewModelProvider(this).get(SindicoColetaViewModel.class);
        lista = findViewById(R.id.listaSindicoHistorico);
        filtro = estadoSalvo == null ? TODAS : estadoSalvo.getInt(ESTADO_FILTRO, TODAS);
        findViewById(R.id.filtrarHistorico).setOnClickListener(view -> abrirFiltro());

        BarraNavegacaoView barra = findViewById(R.id.barraInferiorSindico);
        barra.configurar(ItensBarra.sindico());
        Navegacao.marcarNaBarra(this, barra, ItensBarra.TERCEIRO);
        barra.setOnAssistenteClickListener(
                view -> startActivity(new Intent(this, ChatActivity.class)));
        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDoSindico(this, indice, ItensBarra.TERCEIRO));
    }

    @Override
    protected void onStart() {
        super.onStart();
        carregar();
    }

    private void carregar() {
        if (!carregadas) {
            lista.removeAllViews();
            IndicadorCarregamento.mostrar(lista, R.string.historico_carregando);
        }

        // Os nomes das cooperativas vêm de outra API; chegando depois, a lista se redesenha.
        if (viewModel.getCooperativas().isEmpty()) {
            new CooperativaRepository().listar(this, cooperativas -> {
                if (cooperativas == null || isFinishing() || isDestroyed()) return;
                viewModel.definirCooperativas(cooperativas);
                if (carregadas) mostrarHistorico();
            });
        }

        new CalendarioExternoRepository().listarTodos(this, resultado -> {
            if (isFinishing() || isDestroyed()) return;

            if (resultado.getTipo() != ResultadoSolicitacoes.Tipo.SUCESSO) {
                if (!carregadas) mostrarErro();
                return;
            }

            List<Solicitacao> recentes = new ArrayList<>(resultado.getItens());
            Collections.sort(recentes, (a, b) -> texto(b.getDataInicio()).compareTo(texto(a.getDataInicio())));
            viewModel.definirSolicitacoes(recentes);
            carregadas = true;
            mostrarHistorico();
        });
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor;
    }

    private void mostrarErro() {
        lista.removeAllViews();

        TextView mensagem = new TextView(this);
        mensagem.setText(R.string.historico_erro);
        mensagem.setTextColor(getColor(R.color.cinza_texto_home));
        mensagem.setTextSize(14);
        mensagem.setPadding(dp(8), dp(28), dp(8), dp(12));
        lista.addView(mensagem);

        MaterialButton tentar = (MaterialButton) getLayoutInflater().inflate(R.layout.botao_dialogo, lista, false);
        tentar.setText(R.string.tentar_novamente);
        tentar.setOnClickListener(view -> carregar());
        lista.addView(tentar);
    }

    private boolean passaNoFiltro(StatusAgendamento status) {
        switch (filtro) {
            case EM_ANDAMENTO:
                return status == StatusAgendamento.AGENDADO || status == StatusAgendamento.CONFIRMADO;
            case CONCLUIDAS:
                return status == StatusAgendamento.REALIZADO;
            case ENCERRADAS:
                return status == StatusAgendamento.CANCELADO || status == StatusAgendamento.RECUSADO;
            default:
                return true;
        }
    }

    private void mostrarHistorico() {
        lista.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        int encontrados = 0;
        for (Solicitacao solicitacao : viewModel.getSolicitacoes()) {
            if (!passaNoFiltro(solicitacao.getStatus())) continue;
            encontrados++;
            lista.addView(criarCartao(inflater, solicitacao), margemEntreCartoes());
        }
        if (encontrados == 0) {
            TextView vazio = new TextView(this);
            vazio.setText(R.string.sindico_historico_vazio);
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

    private View criarCartao(LayoutInflater inflater, Solicitacao solicitacao) {
        SindicoCooperativa cooperativa = viewModel.buscar(solicitacao.getCooperativaId());
        String nome = solicitacao.getCooperativaNome() != null && !solicitacao.getCooperativaNome().isEmpty()
                ? solicitacao.getCooperativaNome()
                : cooperativa != null ? cooperativa.getNome()
                : getString(R.string.sindico_cooperativa_numero, solicitacao.getCooperativaId());

        View cartao = inflater.inflate(R.layout.item_sindico_historico, lista, false);
        ((TextView) cartao.findViewById(R.id.nomeHistorico)).setText(nome);
        ((TextView) cartao.findViewById(R.id.dataHistorico)).setText(getString(
                R.string.sindico_solicitacao_andamento,
                FormatoDataApi.data(solicitacao.getDataInicio()), FormatoDataApi.hora(solicitacao.getDataInicio())));
        TextView etiqueta = cartao.findViewById(R.id.etiquetaHistorico);
        StatusAgendamento status = solicitacao.getStatus();
        etiqueta.setText(status.getEtiqueta());
        etiqueta.setTextColor(getColor(status.getTextoEtiqueta()));
        ViewCompat.setBackgroundTintList(etiqueta, getColorStateList(status.getFundoEtiqueta()));
        cartao.setOnClickListener(view -> startActivity(SolicitacaoDetalheActivity.criarIntentSindico(
                this, solicitacao.comNomeCondominio(PerfilApiRepository.condominioNomeOuVazio(this)), nome)));
        Motion.pressFeedback(cartao);

        // Só a coleta realizada se avalia, e uma vez só (a API recusa a repetida).
        TextView avaliar = cartao.findViewById(R.id.acaoAvaliarHistorico);
        if (status == StatusAgendamento.REALIZADO) {
            avaliar.setVisibility(View.VISIBLE);
            if (viewModel.foiAvaliada(solicitacao.getId())) {
                avaliar.setText(R.string.sindico_avaliada);
                avaliar.setEnabled(false);
            } else {
                avaliar.setContentDescription(getString(R.string.sindico_avaliar_coleta_de, nome));
                avaliar.setOnClickListener(view -> abrirAvaliacao(solicitacao, nome));
            }
        }
        return cartao;
    }

    private void abrirAvaliacao(Solicitacao solicitacao, String nomeDaCooperativa) {
        LinearLayout campos = new LinearLayout(this);
        campos.setOrientation(LinearLayout.VERTICAL);
        campos.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

        RatingBar estrelas = new RatingBar(this);
        estrelas.setNumStars(5);
        estrelas.setStepSize(1f);
        estrelas.setRating(0f);
        estrelas.setContentDescription(getString(R.string.sindico_avaliar_escolha_a_nota));
        campos.addView(estrelas, new LinearLayout.LayoutParams(-2, -2));

        EditText comentario = new EditText(this);
        comentario.setHint(R.string.sindico_avaliar_comentario);
        comentario.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        comentario.setMinLines(2);
        comentario.setFilters(new android.text.InputFilter[] {new android.text.InputFilter.LengthFilter(500)});
        campos.addView(comentario, new LinearLayout.LayoutParams(-1, -2));

        new DialogoEco.Builder(this)
                .titulo(getString(R.string.sindico_avaliar_titulo, nomeDaCooperativa))
                .conteudo(campos)
                .botaoValidado(R.string.sindico_avaliar_enviar, DialogoEco.Estilo.PREENCHIDO, () -> {
                    int nota = Math.round(estrelas.getRating());
                    if (nota < 1) {
                        AvisoFlutuante.mostrar(this, R.string.sindico_avaliar_escolha_a_nota);
                        return false;
                    }
                    enviarAvaliacao(solicitacao, nota, comentario.getText().toString());
                    return true;
                })
                .botao(R.string.perfil_cancelar, DialogoEco.Estilo.CONTORNO, null)
                .mostrar();
    }

    private void enviarAvaliacao(Solicitacao solicitacao, int nota, String comentario) {
        new AvaliacaoColetaRepository().avaliar(this, solicitacao.getId(), nota, comentario, resultado -> {
            if (isFinishing() || isDestroyed()) return;

            if (resultado.isSucesso()) {
                viewModel.marcarAvaliada(solicitacao.getId());
                AvisoFlutuante.mostrar(this, R.string.sindico_avaliacao_enviada);
                mostrarHistorico();
                return;
            }

            AvisoFlutuante.mostrar(this, resultado.getMensagemErro());
        });
    }

    private void abrirFiltro() {
        new DialogoEco.Builder(this)
                .titulo(R.string.sindico_filtrar_historico)
                .botao(R.string.sindico_todas, estilo(TODAS), () -> aplicarFiltro(TODAS))
                .botao(R.string.sindico_filtro_andamento, estilo(EM_ANDAMENTO), () -> aplicarFiltro(EM_ANDAMENTO))
                .botao(R.string.sindico_filtro_concluidas, estilo(CONCLUIDAS), () -> aplicarFiltro(CONCLUIDAS))
                .botao(R.string.sindico_filtro_encerradas, estilo(ENCERRADAS), () -> aplicarFiltro(ENCERRADAS))
                .botao(R.string.perfil_cancelar, DialogoEco.Estilo.CONTORNO, null)
                .mostrar();
    }

    private DialogoEco.Estilo estilo(int opcao) {
        return filtro == opcao ? DialogoEco.Estilo.PREENCHIDO : DialogoEco.Estilo.CONTORNO;
    }

    private void aplicarFiltro(int novoFiltro) {
        filtro = novoFiltro;
        if (carregadas) mostrarHistorico();
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
