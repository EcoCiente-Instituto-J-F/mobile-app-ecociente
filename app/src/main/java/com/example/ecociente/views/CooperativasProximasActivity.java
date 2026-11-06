package com.example.ecociente.views;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ecociente.R;
import com.example.ecociente.model.CooperativaProxima;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.viewmodels.CooperativasProximasViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public final class CooperativasProximasActivity extends AppCompatActivity {
    private CooperativasProximasViewModel viewModel;
    private LinearLayout lista;
    private ProgressBar progresso;
    private TextView mensagem;
    private TextView contador;
    private EditText busca;
    private SwipeRefreshLayout atualizacao;
    private final List<MaterialButton> botoesSolicitar = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JanelaEdgeToEdge.aplicar(this);
        setContentView(R.layout.activity_cooperativas_proximas);
        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizCooperativasProximas));

        lista = findViewById(R.id.listaCooperativas);
        progresso = findViewById(R.id.progressoCooperativas);
        mensagem = findViewById(R.id.mensagemCooperativas);
        contador = findViewById(R.id.contadorCooperativas);
        busca = findViewById(R.id.buscaCooperativas);
        atualizacao = findViewById(R.id.atualizacaoCooperativas);
        viewModel = new ViewModelProvider(this).get(CooperativasProximasViewModel.class);

        findViewById(R.id.botaoVoltarCooperativas).setOnClickListener(v -> finish());
        findViewById(R.id.botaoTentarNovamenteCooperativas)
                .setOnClickListener(v -> viewModel.carregar());

        atualizacao.setColorSchemeResources(R.color.verde_escuro_principal);
        atualizacao.setOnRefreshListener(viewModel::carregar);

        busca.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.pesquisar(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) { }
        });

        viewModel.getCarregando().observe(this, carregando -> {
            boolean ativo = Boolean.TRUE.equals(carregando);
            if (!ativo) {
                atualizacao.setRefreshing(false);
            }
            progresso.setVisibility(
                    ativo && lista.getChildCount() == 0 && !atualizacao.isRefreshing()
                            ? View.VISIBLE : View.GONE);
        });

        viewModel.getEnviandoSolicitacao().observe(this, enviando -> {
            boolean ativo = Boolean.TRUE.equals(enviando);
            boolean possuiCondominio = viewModel.getCondominioId().getValue() != null;
            for (MaterialButton botao : botoesSolicitar) {
                boolean enviado = Boolean.TRUE.equals(botao.getTag());
                botao.setEnabled(!ativo && possuiCondominio && !enviado);
                botao.setText(ativo
                        ? R.string.enviando
                        : enviado
                                ? R.string.cooperativa_solicitacao_enviada
                                : R.string.cooperativa_solicitar_coleta);
            }
        });

        viewModel.getErro().observe(this, erro -> {
            if (erro != null) {
                mensagem.setText(erro);
                mensagem.setVisibility(View.VISIBLE);
                contador.setVisibility(View.GONE);
                findViewById(R.id.botaoTentarNovamenteCooperativas).setVisibility(View.VISIBLE);
                lista.removeAllViews();
                botoesSolicitar.clear();
            } else {
                mensagem.setVisibility(View.GONE);
                findViewById(R.id.botaoTentarNovamenteCooperativas).setVisibility(View.GONE);
            }
        });

        viewModel.getVazio().observe(this, vazio -> {
            if (Boolean.TRUE.equals(vazio) && viewModel.getErro().getValue() == null) {
                mensagem.setText(busca.getText().length() == 0
                        ? R.string.cooperativas_vazio
                        : R.string.cooperativas_busca_sem_resultado);
                mensagem.setVisibility(View.VISIBLE);
            } else if (viewModel.getErro().getValue() == null) {
                mensagem.setVisibility(View.GONE);
            }
        });

        viewModel.getLista().observe(this, this::exibirCooperativas);
        viewModel.carregar();
    }

    private void exibirCooperativas(List<CooperativaProxima> cooperativas) {
        lista.removeAllViews();
        botoesSolicitar.clear();
        contador.setVisibility(View.VISIBLE);
        contador.setText(getString(R.string.cooperativas_contagem, cooperativas.size()));

        for (CooperativaProxima cooperativa : cooperativas) {
            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, dp(6), 0, dp(8));
            card.setLayoutParams(cardParams);
            card.setRadius(dp(16));
            card.setCardElevation(dp(1));
            card.setStrokeWidth(dp(1));
            card.setStrokeColor(getColor(R.color.cinza_borda_clara_home));
            card.setCardBackgroundColor(getColor(R.color.branco));

            LinearLayout conteudo = new LinearLayout(this);
            conteudo.setOrientation(LinearLayout.VERTICAL);
            conteudo.setPadding(dp(16), dp(14), dp(16), dp(14));

            LinearLayout cabecalhoCard = new LinearLayout(this);
            cabecalhoCard.setOrientation(LinearLayout.HORIZONTAL);
            cabecalhoCard.setGravity(android.view.Gravity.CENTER_VERTICAL);

            ImageView iconeCaminhao = new ImageView(this);
            iconeCaminhao.setImageResource(R.drawable.ic_caminhao_coleta);
            iconeCaminhao.setContentDescription(getString(R.string.cooperativas_icone));
            cabecalhoCard.addView(iconeCaminhao, new LinearLayout.LayoutParams(dp(32), dp(32)));

            TextView nome = texto(cooperativa.getNome(), 18, true);
            nome.setTextColor(getColor(R.color.verde_escuro_principal));
            LinearLayout.LayoutParams nomeParams = new LinearLayout.LayoutParams(0, -2, 1f);
            nomeParams.leftMargin = dp(10);
            cabecalhoCard.addView(nome, nomeParams);
            conteudo.addView(cabecalhoCard);

            String local = cooperativa.getTextoLocalizacao();
            TextView endereco = texto(
                    local.isEmpty()
                            ? getString(R.string.cooperativa_endereco_indisponivel)
                            : local,
                    13,
                    false);
            endereco.setTextColor(getColor(R.color.cinza_texto_home));
            LinearLayout.LayoutParams enderecoParams = new LinearLayout.LayoutParams(-1, -2);
            enderecoParams.topMargin = dp(5);
            conteudo.addView(endereco, enderecoParams);

            String avaliacao;
            if (cooperativa.getMediaAvaliacoes() != null
                    && cooperativa.getTotalAvaliacoes() > 0) {
                avaliacao = String.format(
                        Locale.forLanguageTag("pt-BR"),
                        getString(R.string.cooperativa_avaliacao_formato),
                        cooperativa.getMediaAvaliacoes(),
                        cooperativa.getTotalAvaliacoes());
            } else {
                avaliacao = getString(R.string.cooperativa_sem_avaliacoes);
            }

            TextView nota = texto(avaliacao, 13, false);
            nota.setTextColor(getColor(R.color.verde_escuro_principal));
            LinearLayout.LayoutParams notaParams = new LinearLayout.LayoutParams(-1, -2);
            notaParams.topMargin = dp(7);
            conteudo.addView(nota, notaParams);

            TextView regiao = texto(getString(R.string.cooperativa_atende_regiao), 12, false);
            regiao.setTextColor(getColor(R.color.cinza_texto_auxiliar));
            LinearLayout.LayoutParams regiaoParams = new LinearLayout.LayoutParams(-1, -2);
            regiaoParams.topMargin = dp(3);
            conteudo.addView(regiao, regiaoParams);

            adicionarAcoesContato(conteudo, cooperativa, local);

            MaterialButton solicitar = new MaterialButton(this);
            solicitar.setText(R.string.cooperativa_solicitar_coleta);
            solicitar.setAllCaps(false);
            solicitar.setCornerRadius(dp(12));
            solicitar.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            getColor(R.color.verde_escuro_principal)));

            Integer condominioId = viewModel.getCondominioId().getValue();
            boolean possuiCondominio = condominioId != null && condominioId > 0;
            solicitar.setTag(Boolean.FALSE);
            solicitar.setEnabled(possuiCondominio);
            solicitar.setContentDescription(possuiCondominio
                    ? getString(R.string.cooperativa_solicitar_coleta)
                    : getString(R.string.cooperativa_condominio_indisponivel));
            solicitar.setOnClickListener(v -> abrirSelecaoDataHora(cooperativa, solicitar));

            LinearLayout.LayoutParams botaoParams = new LinearLayout.LayoutParams(-1, dp(48));
            botaoParams.topMargin = dp(14);
            conteudo.addView(solicitar, botaoParams);
            botoesSolicitar.add(solicitar);

            if (!possuiCondominio) {
                TextView aviso = texto(
                        getString(R.string.cooperativa_condominio_indisponivel), 12, false);
                aviso.setTextColor(getColor(R.color.cinza_texto_auxiliar));
                LinearLayout.LayoutParams avisoParams = new LinearLayout.LayoutParams(-1, -2);
                avisoParams.topMargin = dp(5);
                conteudo.addView(aviso, avisoParams);
            }

            card.addView(conteudo);
            lista.addView(card);
        }
    }

    private void adicionarAcoesContato(
            LinearLayout conteudo,
            CooperativaProxima cooperativa,
            String endereco) {

        LinearLayout acoes = new LinearLayout(this);
        acoes.setOrientation(LinearLayout.HORIZONTAL);
        acoes.setGravity(android.view.Gravity.CENTER_VERTICAL);
        boolean possuiAcao = false;

        if (!cooperativa.getTelefone().isEmpty()) {
            MaterialButton ligar = botaoSecundario(R.string.cooperativa_ligar);
            ligar.setOnClickListener(v -> abrirTelefone(cooperativa.getTelefone()));
            acoes.addView(ligar, acaoParams(possuiAcao));
            possuiAcao = true;
        }

        if (!cooperativa.getEmail().isEmpty()) {
            MaterialButton email = botaoSecundario(R.string.cooperativa_email);
            email.setOnClickListener(v -> abrirEmail(cooperativa.getEmail()));
            acoes.addView(email, acaoParams(possuiAcao));
            possuiAcao = true;
        }

        if (!endereco.isEmpty()) {
            MaterialButton mapa = botaoSecundario(R.string.cooperativa_ver_mapa);
            mapa.setOnClickListener(v -> abrirMapa(endereco));
            acoes.addView(mapa, acaoParams(possuiAcao));
            possuiAcao = true;
        }

        if (possuiAcao) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.topMargin = dp(8);
            conteudo.addView(acoes, params);
        } else {
            TextView semContato = texto(
                    getString(R.string.cooperativa_contato_indisponivel), 12, false);
            semContato.setTextColor(getColor(R.color.cinza_texto_auxiliar));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.topMargin = dp(8);
            conteudo.addView(semContato, params);
        }
    }

    private MaterialButton botaoSecundario(int textoId) {
        MaterialButton botao = new MaterialButton(this);
        botao.setText(textoId);
        botao.setAllCaps(false);
        botao.setTextSize(12);
        botao.setMinHeight(dp(40));
        botao.setInsetTop(0);
        botao.setInsetBottom(0);
        botao.setCornerRadius(dp(10));
        botao.setStrokeColorResource(R.color.verde_escuro_principal);
        botao.setStrokeWidth(dp(1));
        botao.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(getColor(R.color.branco)));
        botao.setTextColor(getColor(R.color.verde_escuro_principal));
        return botao;
    }

    private LinearLayout.LayoutParams acaoParams(boolean jaExisteAcao) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, dp(42), 1f);
        if (jaExisteAcao) {
            params.leftMargin = dp(6);
        }
        return params;
    }

    private void abrirTelefone(String telefone) {
        abrirIntent(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", telefone, null)));
    }

    private void abrirEmail(String email) {
        abrirIntent(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null)));
    }

    private void abrirMapa(String endereco) {
        Uri uri = Uri.parse(
                "https://www.google.com/maps/search/?api=1&query=" + Uri.encode(endereco));
        abrirIntent(new Intent(Intent.ACTION_VIEW, uri));
    }

    private void abrirIntent(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException erro) {
            Toast.makeText(this, R.string.cooperativa_acao_indisponivel, Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirSelecaoDataHora(
            CooperativaProxima cooperativa,
            MaterialButton botao) {

        Calendar hoje = Calendar.getInstance();
        DatePickerDialog seletorData = new DatePickerDialog(
                this,
                (view, ano, mes, dia) -> abrirSelecaoHora(
                        cooperativa, botao, ano, mes, dia),
                hoje.get(Calendar.YEAR),
                hoje.get(Calendar.MONTH),
                hoje.get(Calendar.DAY_OF_MONTH));
        seletorData.getDatePicker().setMinDate(hoje.getTimeInMillis());
        seletorData.show();
    }

    private void abrirSelecaoHora(
            CooperativaProxima cooperativa,
            MaterialButton botao,
            int ano,
            int mes,
            int dia) {

        Calendar agora = Calendar.getInstance();
        int horaInicial = agora.get(Calendar.HOUR_OF_DAY);
        int minutoInicial = agora.get(Calendar.MINUTE);

        new TimePickerDialog(
                this,
                (view, hora, minuto) -> confirmarSolicitacao(
                        cooperativa, botao, ano, mes, dia, hora, minuto),
                horaInicial,
                minutoInicial,
                true).show();
    }

    private void confirmarSolicitacao(
            CooperativaProxima cooperativa,
            MaterialButton botao,
            int ano,
            int mes,
            int dia,
            int hora,
            int minuto) {

        Calendar data = Calendar.getInstance();
        data.set(ano, mes, dia, hora, minuto, 0);
        data.set(Calendar.MILLISECOND, 0);

        if (!data.getTime().after(Calendar.getInstance().getTime())) {
            Toast.makeText(this, R.string.cooperativa_data_deve_ser_futura, Toast.LENGTH_LONG).show();
            return;
        }

        SimpleDateFormat formatoApi =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
        String dataInicio = formatoApi.format(data.getTime());

        SimpleDateFormat formatoTela =
                new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", new Locale("pt", "BR"));
        String dataLegivel = formatoTela.format(data.getTime());

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cooperativa_confirmar_solicitacao_titulo)
                .setMessage(getString(
                        R.string.cooperativa_confirmar_solicitacao_mensagem,
                        cooperativa.getNome(),
                        dataLegivel))
                .setNegativeButton(R.string.perfil_cancelar, null)
                .setPositiveButton(
                        R.string.cooperativa_confirmar_solicitacao,
                        (dialog, which) -> enviarSolicitacao(
                                cooperativa, botao, dataInicio))
                .show();
    }

    private void enviarSolicitacao(
            CooperativaProxima cooperativa,
            MaterialButton botao,
            String dataInicio) {

        botao.setEnabled(false);
        viewModel.solicitarColeta(
                cooperativa.getId(),
                dataInicio,
                resultado -> tratarResultadoSolicitacao(resultado, botao));
    }

    private void tratarResultadoSolicitacao(ResultadoApi resultado, MaterialButton botao) {
        if (resultado.isSucesso()) {
            botao.setTag(Boolean.TRUE);
            botao.setText(R.string.cooperativa_solicitacao_enviada);
            botao.setEnabled(false);
            Toast.makeText(
                    this,
                    R.string.cooperativa_solicitacao_enviada_mensagem,
                    Toast.LENGTH_LONG).show();
            return;
        }

        botao.setEnabled(viewModel.getCondominioId().getValue() != null);
        botao.setText(R.string.cooperativa_solicitar_coleta);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cooperativa_erro_solicitacao_titulo)
                .setMessage(resultado.getMensagemErro())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private TextView texto(String valor, int tamanho, boolean negrito) {
        TextView view = new TextView(this);
        view.setText(valor);
        view.setTextSize(tamanho);
        view.setTypeface(ResourcesCompat.getFont(
                this, negrito ? R.font.nunito_bold : R.font.nunito_regular));
        return view;
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}
