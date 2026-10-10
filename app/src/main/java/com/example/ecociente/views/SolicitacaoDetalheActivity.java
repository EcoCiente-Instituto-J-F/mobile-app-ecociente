package com.example.ecociente.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecociente.R;
import com.example.ecociente.model.DetalheSolicitacao;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.model.StatusAgendamento;
import com.example.ecociente.ui.BotaoCarregando;
import com.example.ecociente.ui.Dimensoes;
import com.example.ecociente.ui.FormatoDataApi;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.viewmodels.ResponderSolicitacaoViewModel;
import com.google.android.material.button.MaterialButton;
import java.util.Locale;
import com.example.ecociente.ui.DialogoEco;

public class SolicitacaoDetalheActivity extends AppCompatActivity {

    private static final String EXTRA_SOLICITACAO = "solicitacao";

    private static final int ETAPA_PENDENTE = 0;
    private static final int ETAPA_ATUAL = 1;
    private static final int ETAPA_CONCLUIDA = 2;

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

        configurarAcoes();
    }

    private void preencher() {

        DetalheSolicitacao detalhe = solicitacao.getDetalhe();

        String nome = SolicitacaoAdapter.nomeExibido(this, solicitacao);

        TextView chipCategoria = findViewById(R.id.chipCategoriaDetalhe);

        TextView atualizacao = findViewById(R.id.textoAtualizacaoDetalhe);

        if (detalhe == null || detalhe.getAtualizadaEm() == null) {
            chipCategoria.setVisibility(View.GONE);
            atualizacao.setVisibility(View.GONE);

        } else {
            atualizacao.setText(
                    getString(
                            R.string.ultima_atualizacao,
                            FormatoDataApi.dataCompacta(detalhe.getAtualizadaEm()),
                            FormatoDataApi.hora(detalhe.getAtualizadaEm())));
        }

        ((TextView) findViewById(R.id.nomeCondominioDetalhe)).setText(nome);

        preencherTexto(R.id.tipoCondominioDetalhe, detalhe == null ? null : detalhe.getTipoCondominio());
        preencherTexto(R.id.cidadeCondominioDetalhe, detalhe == null ? null : detalhe.getCidade());

        findViewById(R.id.botaoChatDetalhe)
                .setOnClickListener(view -> startActivity(ConversaActivity.criarIntent(this, nome)));

        montarLinhaDoTempo(nome, detalhe);

        montarLinhasDoDetalhe(detalhe);
    }

    private void preencherTexto(int id, String texto) {

        TextView campo = findViewById(id);

        campo.setText(texto);
        campo.setVisibility(texto == null || texto.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void montarLinhaDoTempo(@NonNull String nome, DetalheSolicitacao detalhe) {

        LinearLayout linha = findViewById(R.id.linhaDoTempoDetalhe);

        StatusAgendamento status = solicitacao.getStatus();

        String recebida = detalhe == null ? solicitacao.getDataInicio() : detalhe.getRecebidaEm();

        String dataRecebida =
                recebida == null
                        ? ""
                        : FormatoDataApi.dataCompacta(recebida) + " • " + FormatoDataApi.hora(recebida);

        adicionarPasso(
                linha,
                ETAPA_CONCLUIDA,
                R.string.linha_recebida_titulo,
                dataRecebida,
                getString(R.string.linha_recebida_texto, nome),
                false);

        int respostaTexto;
        int respostaEtapa = ETAPA_CONCLUIDA;

        if (status == StatusAgendamento.AGENDADO) {
            respostaTexto = R.string.linha_resposta_aguardando;
            respostaEtapa = ETAPA_ATUAL;

        } else if (status == StatusAgendamento.RECUSADO) {
            respostaTexto = R.string.linha_resposta_recusada;

        } else {
            respostaTexto = R.string.linha_resposta_aceita;
        }

        adicionarPasso(
                linha, respostaEtapa, R.string.linha_resposta_titulo, "", getString(respostaTexto), false);

        int coletaEtapa = ETAPA_PENDENTE;
        String coletaTexto;

        if (status == StatusAgendamento.CONFIRMADO) {
            coletaEtapa = ETAPA_ATUAL;
            coletaTexto =
                    getString(
                            R.string.linha_coleta_agendada,
                            FormatoDataApi.data(solicitacao.getDataInicio()),
                            FormatoDataApi.hora(solicitacao.getDataInicio()));

        } else if (status == StatusAgendamento.REALIZADO) {
            coletaEtapa = ETAPA_CONCLUIDA;
            coletaTexto = getString(R.string.linha_coleta_concluida);

        } else if (status == StatusAgendamento.RECUSADO) {
            coletaTexto = getString(R.string.linha_coleta_sem_coleta);

        } else if (status == StatusAgendamento.CANCELADO) {
            coletaTexto = getString(R.string.linha_coleta_cancelada);

        } else {
            coletaTexto = getString(R.string.linha_coleta_aguardando);
        }

        adicionarPasso(linha, coletaEtapa, R.string.linha_coleta_titulo, "", coletaTexto, true);
    }

    private void adicionarPasso(
            @NonNull LinearLayout linha,
            int etapa,
            @StringRes int titulo,
            @NonNull String data,
            @NonNull String texto,
            boolean ultimo) {

        View passo = LayoutInflater.from(this).inflate(R.layout.item_linha_tempo, linha, false);

        ((ImageView) passo.findViewById(R.id.iconeLinhaTempo))
                .setImageResource(
                        etapa == ETAPA_CONCLUIDA
                                ? R.drawable.ic_linha_concluida
                                : R.drawable.ic_linha_pendente);

        TextView tituloPasso = passo.findViewById(R.id.tituloLinhaTempo);

        tituloPasso.setText(titulo);
        tituloPasso.setTextColor(
                ContextCompat.getColor(
                        this,
                        etapa == ETAPA_PENDENTE ? R.color.cinza_texto_home : R.color.verde_escuro_principal));

        TextView dataPasso = passo.findViewById(R.id.dataLinhaTempo);

        dataPasso.setText(data);
        dataPasso.setVisibility(data.isEmpty() ? View.GONE : View.VISIBLE);

        ((TextView) passo.findViewById(R.id.textoLinhaTempo)).setText(texto);

        View conector = passo.findViewById(R.id.conectorLinhaTempo);

        conector.setVisibility(ultimo ? View.GONE : View.VISIBLE);

        conector.setBackgroundColor(
                ContextCompat.getColor(
                        this,
                        etapa == ETAPA_CONCLUIDA ? R.color.verde_escuro_principal : R.color.cinza_icone));

        linha.addView(passo);
    }

    private void montarLinhasDoDetalhe(DetalheSolicitacao detalhe) {

        LinearLayout linhas = findViewById(R.id.linhasDetalhe);

        if (detalhe != null && detalhe.getMateriais().length > 0) {

            View linha = novaLinha(linhas, R.string.rotulo_materiais, null);

            linha.findViewById(R.id.valorLinhaDetalhe).setVisibility(View.GONE);

            LinearLayout chips = linha.findViewById(R.id.chipsLinhaDetalhe);

            chips.setVisibility(View.VISIBLE);

            for (String material : detalhe.getMateriais()) {
                chips.addView(criarChipMaterial(material));
            }
        }

        if (detalhe != null && detalhe.getVolume() != null) {
            novaLinha(linhas, R.string.rotulo_volume, detalhe.getVolume());
        }

        novaLinha(
                linhas,
                R.string.rotulo_data_desejada,
                FormatoDataApi.dataLonga(solicitacao.getDataInicio()));

        String inicio = FormatoDataApi.hora(solicitacao.getDataInicio());
        String fim = FormatoDataApi.hora(solicitacao.getDataFim());

        novaLinha(linhas, R.string.rotulo_horario, fim.isEmpty() ? inicio : inicio + " – " + fim);

        novaLinha(
                linhas,
                R.string.rotulo_recorrencia,
                getString(
                        solicitacao.possuiRecorrencia()
                                ? R.string.solicitacao_recorrencia_sim
                                : R.string.solicitacao_recorrencia_nao));

        if (detalhe != null && detalhe.getObservacoes() != null) {
            novaLinha(linhas, R.string.rotulo_observacoes, detalhe.getObservacoes());
        }
    }

    @NonNull
    private View novaLinha(@NonNull LinearLayout pai, @StringRes int rotulo, String valor) {

        View linha = LayoutInflater.from(this).inflate(R.layout.item_linha_detalhe, pai, false);

        ((TextView) linha.findViewById(R.id.rotuloLinhaDetalhe)).setText(rotulo);
        ((TextView) linha.findViewById(R.id.valorLinhaDetalhe)).setText(valor);

        pai.addView(linha);

        return linha;
    }

    @NonNull
    private TextView criarChipMaterial(@NonNull String material) {

        TextView chip = new TextView(this, null, 0, R.style.ChipCategoriaAviso);

        chip.setText(material);

        String chave = material.toLowerCase(Locale.ROOT);

        int fundo = R.color.chip_geral_fundo;
        int texto = R.color.chip_geral_texto;

        if (chave.contains("papel")) {
            fundo = R.color.material_papelao_fundo;
            texto = R.color.material_papelao_texto;

        } else if (chave.contains("plást")) {
            fundo = R.color.chip_orientacao_fundo;
            texto = R.color.chip_orientacao_texto;

        } else if (chave.contains("vidro")) {
            fundo = R.color.material_vidro_fundo;
            texto = R.color.material_vidro_texto;
        }

        chip.setTextColor(ContextCompat.getColor(this, texto));

        ViewCompat.setBackgroundTintList(chip, ContextCompat.getColorStateList(this, fundo));

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        parametros.setMarginEnd(Dimensoes.dpParaPx(this, 6));

        chip.setLayoutParams(parametros);

        return chip;
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

        new DialogoEco.Builder(this)
                .titulo(titulo)
                .mensagem(mensagem)
                .botao(
                        rotuloConfirmar,
                        resposta == StatusAgendamento.RECUSADO
                                ? DialogoEco.Estilo.PERIGO
                                : DialogoEco.Estilo.PREENCHIDO,
                        () -> responder(resposta))
                .botao(R.string.perfil_cancelar, DialogoEco.Estilo.CONTORNO, null)
                .mostrar();
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
