package com.example.ecociente.views;

import com.example.ecociente.repository.PerfilApiRepository;
import com.example.ecociente.ui.Motion;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecociente.R;
import com.example.ecociente.model.SindicoCooperativa;
import com.example.ecociente.viewmodels.SindicoColetaViewModel;

import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.Navegacao;
import com.example.ecociente.model.SindicoSolicitacao;
import com.example.ecociente.model.StatusAgendamento;
import com.example.ecociente.repository.CooperativaRepository;
import com.example.ecociente.ui.AbrirMapa;
import com.example.ecociente.ui.IndicadorCarregamento;
import com.google.android.material.button.MaterialButton;

/** Cooperativas da cidade do condomínio (API), com a rota até elas no Google Maps. */
public final class SindicoCooperativasActivity extends AppCompatActivity {
    private SindicoColetaViewModel viewModel;
    private LinearLayout lista;
    private EditText busca;
    private boolean carregadas;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        JanelaEdgeToEdge.aplicar(this);
        setContentView(R.layout.activity_sindico_cooperativas);

        Motion.entrar(
                this,
                R.id.tituloSindicoCooperativas,
                R.id.buscaCooperativas,
                R.id.listaCooperativas);

        View raiz = findViewById(R.id.raizSindicoCooperativas);
        InsetsSistema.aplicarComoPadding(raiz);

        viewModel = new ViewModelProvider(this).get(SindicoColetaViewModel.class);
        lista = findViewById(R.id.listaCooperativas);
        busca = findViewById(R.id.buscaCooperativas);
        busca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                mostrarCooperativas(s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        BarraNavegacaoView barra = findViewById(R.id.barraInferiorSindico);
        barra.configurar(ItensBarra.sindico());
        Navegacao.marcarNaBarra(this, barra, ItensBarra.SEGUNDO);
        barra.setOnAssistenteClickListener(
                view -> startActivity(new Intent(this, ChatActivity.class)));
        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDoSindico(this, indice, ItensBarra.SEGUNDO));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!carregadas) {
            carregarCooperativas();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (carregadas) {
            mostrarCooperativas(busca.getText().toString());
        }
    }

    private void carregarCooperativas() {
        lista.removeAllViews();
        IndicadorCarregamento.mostrar(lista, R.string.cooperativas_carregando);

        new CooperativaRepository().listar(this, cooperativas -> {
            if (isFinishing() || isDestroyed()) return;

            if (cooperativas == null) {
                mostrarErroDeCooperativas();
                return;
            }

            viewModel.definirCooperativas(cooperativas);
            carregadas = true;
            mostrarCooperativas(busca.getText().toString());
        });
    }

    private void mostrarErroDeCooperativas() {
        lista.removeAllViews();

        TextView mensagem = new TextView(this);
        mensagem.setText(R.string.cooperativas_erro);
        mensagem.setTextColor(getColor(R.color.cinza_texto_home));
        mensagem.setTextSize(14);
        mensagem.setPadding(8, 28, 8, 12);
        lista.addView(mensagem);

        MaterialButton tentar = (MaterialButton) getLayoutInflater().inflate(R.layout.botao_dialogo, lista, false);
        tentar.setText(R.string.tentar_novamente);
        tentar.setOnClickListener(view -> carregarCooperativas());
        lista.addView(tentar);
    }

    private void mostrarCooperativas(String filtro) {
        lista.removeAllViews();
        int encontrados = 0;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (SindicoCooperativa cooperativa : viewModel.getCooperativas()) {
            if (!cooperativa.corresponde(filtro)) continue;
            encontrados++;
            View item = inflater.inflate(R.layout.item_sindico_cooperativa, lista, false);
            TextView nome = item.findViewById(R.id.nomeCooperativa);
            nome.setText(cooperativa.getNome());
            nome.setContentDescription(getString(R.string.sindico_ver_informacoes, cooperativa.getNome()));
            nome.setOnClickListener(view -> {
                Intent informacoes = new Intent(this, SindicoInformacoesCooperativaActivity.class);
                informacoes.putExtra(SindicoInformacoesCooperativaActivity.EXTRA_COOPERATIVA_ID,
                        cooperativa.getId());
                startActivity(informacoes);
            });
            ((TextView) item.findViewById(R.id.enderecoCooperativa))
                    .setText(cooperativa.getEnderecoCompleto());
            ((TextView) item.findViewById(R.id.avaliacaoCooperativa))
                    .setText(cooperativa.getAvaliacao().isEmpty()
                            ? getString(R.string.sindico_sem_avaliacoes)
                            : getString(R.string.sindico_avaliacao_cooperativa,
                                    cooperativa.getAvaliacao(), cooperativa.getTotalAvaliacoes()));
            TextView comoChegar = item.findViewById(R.id.comoChegarCooperativa);
            comoChegar.setContentDescription(getString(R.string.sindico_como_chegar_em, cooperativa.getNome()));
            comoChegar.setOnClickListener(view -> AbrirMapa.rota(this, cooperativa.getEnderecoCompleto()));
            Button acao = item.findViewById(R.id.acaoCooperativa);
            SindicoSolicitacao pendente = solicitacaoEmAndamento(cooperativa.getId());
            if (pendente != null) {
                acao.setText(R.string.sindico_solicitacao_enviada);
                acao.setTextColor(getColor(R.color.etiqueta_pendente_texto));
                acao.setBackgroundTintList(getColorStateList(R.color.etiqueta_pendente_fundo));
                acao.setOnClickListener(view -> startActivity(SolicitacaoDetalheActivity.criarIntentSindico(
                        this, pendente.paraSolicitacao(PerfilApiRepository.condominioNomeOuVazio(this)),
                        pendente.getCooperativa())));
                lista.addView(item, margemDoCartao());
                continue;
            }
            acao.setOnClickListener(view -> {
                Intent formulario = new Intent(this, SindicoSolicitacaoColetaActivity.class);
                formulario.putExtra(SindicoSolicitacaoColetaActivity.EXTRA_COOPERATIVA_ID,
                        cooperativa.getId());
                startActivity(formulario);
            });
            lista.addView(item, margemDoCartao());
        }
        if (encontrados == 0) {
            TextView vazio = new TextView(this);
            vazio.setText(R.string.sindico_nenhuma_cooperativa_de_exemplo_enc);
            vazio.setTextColor(getColor(R.color.cinza_texto_home));
            vazio.setTextSize(14);
            vazio.setPadding(8, 28, 8, 28);
            lista.addView(vazio);
        }
    }

    private LinearLayout.LayoutParams margemDoCartao() {
        LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(-1, -2);
        parametros.bottomMargin = Math.round(12 * getResources().getDisplayMetrics().density);
        return parametros;
    }

    private SindicoSolicitacao solicitacaoEmAndamento(int cooperativaId) {
        for (SindicoSolicitacao solicitacao : viewModel.getSolicitacoes()) {
            boolean ativa = solicitacao.getStatus() == StatusAgendamento.AGENDADO
                    || solicitacao.getStatus() == StatusAgendamento.CONFIRMADO;
            if (ativa && solicitacao.getCooperativaId() == cooperativaId) return solicitacao;
        }
        return null;
    }

    private void voltarHome() {
        Intent home = new Intent(this, SindicoHomeActivity.class);
        home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(home);
        finish();
    }
}
