package com.example.ecociente.views;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.content.res.ResourcesCompat;

import com.example.ecociente.R;
import com.example.ecociente.model.CooperativaProxima;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.viewmodels.CooperativasProximasViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

public final class CooperativasProximasActivity extends AppCompatActivity {
    private CooperativasProximasViewModel viewModel;
    private LinearLayout lista;
    private ProgressBar progresso;
    private TextView mensagem;
    private EditText busca;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JanelaEdgeToEdge.aplicar(this);
        setContentView(R.layout.activity_cooperativas_proximas);
        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizCooperativasProximas));

        lista = findViewById(R.id.listaCooperativas);
        progresso = findViewById(R.id.progressoCooperativas);
        mensagem = findViewById(R.id.mensagemCooperativas);
        busca = findViewById(R.id.buscaCooperativas);
        viewModel = new ViewModelProvider(this).get(CooperativasProximasViewModel.class);

        findViewById(R.id.botaoVoltarCooperativas).setOnClickListener(v -> finish());
        findViewById(R.id.botaoTentarNovamenteCooperativas).setOnClickListener(v -> viewModel.carregar());

        busca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.pesquisar(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        viewModel.getCarregando().observe(this, carregando ->
                progresso.setVisibility(Boolean.TRUE.equals(carregando) ? View.VISIBLE : View.GONE));
        viewModel.getErro().observe(this, erro -> {
            if (erro != null) {
                mensagem.setText(erro);
                mensagem.setVisibility(View.VISIBLE);
                findViewById(R.id.botaoTentarNovamenteCooperativas).setVisibility(View.VISIBLE);
                lista.removeAllViews();
            } else {
                mensagem.setVisibility(View.GONE);
                findViewById(R.id.botaoTentarNovamenteCooperativas).setVisibility(View.GONE);
            }
        });
        viewModel.getVazio().observe(this, vazio -> {
            if (Boolean.TRUE.equals(vazio) && viewModel.getErro().getValue() == null) {
                mensagem.setText(busca.getText().length() == 0
                        ? "Ainda não há cooperativas disponíveis."
                        : "Nenhuma cooperativa encontrada. Tente outro nome ou cidade.");
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
        for (CooperativaProxima cooperativa : cooperativas) {
            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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

            TextView nome = texto(cooperativa.getNome(), 18, true);
            nome.setTextColor(getColor(R.color.verde_escuro_principal));
            conteudo.addView(nome);

            TextView local = texto(cooperativa.getTextoLocalizacao(), 13, false);
            local.setTextColor(getColor(R.color.cinza_texto_home));
            LinearLayout.LayoutParams localParams = new LinearLayout.LayoutParams(-1, -2);
            localParams.topMargin = dp(5);
            conteudo.addView(local, localParams);

            String avaliacao = cooperativa.getMediaAvaliacoes() == null
                    ? "Sem avaliações disponíveis"
                    : String.format(Locale.forLanguageTag("pt-BR"), "★ %.1f · %d avaliações",
                            cooperativa.getMediaAvaliacoes(), cooperativa.getTotalAvaliacoes());
            TextView nota = texto(avaliacao, 13, false);
            nota.setTextColor(getColor(R.color.verde_escuro_principal));
            LinearLayout.LayoutParams notaParams = new LinearLayout.LayoutParams(-1, -2);
            notaParams.topMargin = dp(5);
            conteudo.addView(nota, notaParams);

            TextView distancia = texto("Distância indisponível", 12, false);
            distancia.setTextColor(getColor(R.color.cinza_texto_auxiliar));
            LinearLayout.LayoutParams distanciaParams = new LinearLayout.LayoutParams(-1, -2);
            distanciaParams.topMargin = dp(3);
            conteudo.addView(distancia, distanciaParams);

            MaterialButton solicitar = new MaterialButton(this);
            solicitar.setText("Solicitar coleta");
            solicitar.setEnabled(false);
            solicitar.setContentDescription(
                    "Solicitação indisponível. O aplicativo ainda não identificou o condomínio vinculado.");
            LinearLayout.LayoutParams botaoParams = new LinearLayout.LayoutParams(-1, dp(48));
            botaoParams.topMargin = dp(12);
            conteudo.addView(solicitar, botaoParams);

            TextView indisponivel = texto(
                    "Não é possível enviar a solicitação até identificar o condomínio vinculado à sua conta.",
                    12, false);
            indisponivel.setTextColor(getColor(R.color.cinza_texto_auxiliar));
            LinearLayout.LayoutParams avisoParams = new LinearLayout.LayoutParams(-1, -2);
            avisoParams.topMargin = dp(5);
            conteudo.addView(indisponivel, avisoParams);

            card.addView(conteudo);
            lista.addView(card);
        }
    }

    private void mostrarLimiteSolicitacao() {
        new AlertDialog.Builder(this)
                .setTitle("Solicitação de coleta")
                .setMessage("A API exige o identificador do condomínio para criar a solicitação. Esse identificador ainda não está disponível de forma confiável no fluxo atual do aplicativo. Nenhuma solicitação foi enviada.")
                .setPositiveButton("Entendi", null)
                .show();
    }

    private TextView texto(String valor, int tamanho, boolean negrito) {
        TextView view = new TextView(this);
        view.setText(valor);
        view.setTextSize(tamanho);
        view.setFontFeatureSettings("kern");
        view.setTypeface(ResourcesCompat.getFont(this, negrito
                ? R.font.nunito_bold : R.font.nunito_regular));
        return view;
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}