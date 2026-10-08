package com.example.ecociente.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecociente.R;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.model.StatusAgendamento;
import com.example.ecociente.ui.BotaoCarregando;
import com.example.ecociente.ui.FormatoDataApi;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.viewmodels.ResponderSolicitacaoViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SolicitacaoDetalheActivity extends AppCompatActivity {

    private static final String EXTRA_SOLICITACAO = "solicitacao";

    private ResponderSolicitacaoViewModel viewModel;

    private Solicitacao solicitacao;

    private MaterialButton botaoAceitar;
    private MaterialButton botaoRecusar;

    @NonNull
    public static Intent criarIntent(@NonNull Context contexto, @NonNull Solicitacao solicitacao) {

        return new Intent(contexto, SolicitacaoDetalheActivity.class)
                .putExtra(EXTRA_SOLICITACAO, solicitacao);
    }

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        JanelaEdgeToEdge.aplicar(this);

        solicitacao = (Solicitacao) getIntent().getSerializableExtra(EXTRA_SOLICITACAO);

        if (solicitacao == null) {
            finish();
            return;
        }

        setContentView(R.layout.activity_solicitacao_detalhe);

        viewModel = new ViewModelProvider(this).get(ResponderSolicitacaoViewModel.class);

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizSolicitacaoDetalhe));

        findViewById(R.id.botaoVoltarDetalhe).setOnClickListener(view -> finish());

        preencher();

        findViewById(R.id.botaoEditarColeta)
                .setOnClickListener(view -> startActivity(NovaColetaActivity.criarIntent(this, solicitacao)));

        configurarAcoes();
    }

    private void preencher() {

        ((TextView) findViewById(R.id.tituloDetalheSolicitacao))
                .setText(getString(R.string.solicitacao_condominio, solicitacao.getCondominioId()));

        ((TextView) findViewById(R.id.valorStatusDetalhe))
                .setText(solicitacao.getStatus().getRotulo());

        ((TextView) findViewById(R.id.valorDataDetalhe))
                .setText(FormatoDataApi.data(solicitacao.getDataInicio()));

        String inicio = FormatoDataApi.hora(solicitacao.getDataInicio());
        String fim = FormatoDataApi.hora(solicitacao.getDataFim());

        ((TextView) findViewById(R.id.valorHorarioDetalhe))
                .setText(fim.isEmpty() ? inicio : inicio + " – " + fim);

        ((TextView) findViewById(R.id.valorRecorrenciaDetalhe))
                .setText(
                        solicitacao.possuiRecorrencia()
                                ? R.string.solicitacao_recorrencia_sim
                                : R.string.solicitacao_recorrencia_nao);
    }

    private void configurarAcoes() {

        View acoes = findViewById(R.id.acoesDetalhe);

        if (solicitacao.getStatus() != StatusAgendamento.AGENDADO) {
            acoes.setVisibility(View.GONE);
            return;
        }

        botaoAceitar = findViewById(R.id.botaoAceitar);
        botaoRecusar = findViewById(R.id.botaoRecusar);

        botaoAceitar.setOnClickListener(
                view ->
                        confirmar(
                                R.string.aceitar_titulo,
                                getString(
                                        R.string.aceitar_mensagem,
                                        FormatoDataApi.data(solicitacao.getDataInicio()),
                                        FormatoDataApi.hora(solicitacao.getDataInicio())),
                                R.string.aceitar,
                                StatusAgendamento.CONFIRMADO));

        botaoRecusar.setOnClickListener(
                view ->
                        confirmar(
                                R.string.recusar_titulo,
                                getString(R.string.recusar_mensagem),
                                R.string.recusar,
                                StatusAgendamento.RECUSADO));

        viewModel.getCarregando().observe(this, this::definirCarregando);
    }

    private void confirmar(
            @StringRes int titulo,
            @NonNull String mensagem,
            @StringRes int rotuloConfirmar,
            @NonNull StatusAgendamento resposta) {

        new MaterialAlertDialogBuilder(this)
                .setTitle(titulo)
                .setMessage(mensagem)
                .setNegativeButton(R.string.perfil_cancelar, null)
                .setPositiveButton(rotuloConfirmar, (dialogo, botao) -> responder(resposta))
                .show();
    }

    private void responder(@NonNull StatusAgendamento resposta) {

        viewModel
                .responder(this, solicitacao.getId(), resposta)
                .observe(
                        this,
                        resultado -> {
                            if (!resultado.isSucesso()) {
                                Toast.makeText(this, resultado.getMensagemErro(), Toast.LENGTH_LONG).show();
                                return;
                            }

                            Toast.makeText(
                                            this,
                                            resposta == StatusAgendamento.CONFIRMADO
                                                    ? R.string.solicitacao_aceita
                                                    : R.string.solicitacao_recusada,
                                            Toast.LENGTH_SHORT)
                                    .show();

                            setResult(RESULT_OK);

                            finish();
                        });
    }

    private void definirCarregando(boolean carregando) {

        BotaoCarregando.definir(botaoAceitar, carregando, R.string.aceitar, R.string.enviando);

        botaoRecusar.setEnabled(!carregando);
    }
}
