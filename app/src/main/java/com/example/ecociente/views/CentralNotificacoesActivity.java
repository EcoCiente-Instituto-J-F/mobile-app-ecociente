package com.example.ecociente.views;

import com.example.ecociente.ui.Motion;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecociente.R;
import com.example.ecociente.ui.AvisoFlutuante;
import com.example.ecociente.model.Aviso;
import com.example.ecociente.model.Notificacao;
import com.example.ecociente.model.SindicoAviso;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.DialogoEco;
import com.example.ecociente.ui.FormatoDataApi;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.Navegacao;
import com.example.ecociente.viewmodels.SindicoViewModel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// Notificações e avisos numa lista só, agrupada por dia. A cooperativa e o síndico também
// criam daqui; o condomínio só lê.
public class CentralNotificacoesActivity extends AppCompatActivity {

    public enum Destino {
        COOPERATIVA,
        CONDOMINIO,
        SINDICO
    }

    private static final String EXTRA_DESTINO = "destinoAvisos";

    private Destino destino;

    private LinearLayout lista;

    private List<SindicoAviso> avisosDoSindico = new ArrayList<>();

    @NonNull
    public static Intent criarIntent(@NonNull Context contexto, @NonNull Destino destino) {

        return new Intent(contexto, CentralNotificacoesActivity.class).putExtra(EXTRA_DESTINO, destino);
    }

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        JanelaEdgeToEdge.aplicar(this);

        setContentView(R.layout.activity_central_notificacoes);

        Motion.entrar(
                this,
                R.id.tituloCentralNotificacoes,
                R.id.listaCentralNotificacoes);

        View raiz = findViewById(R.id.raizCentralNotificacoes);

        InsetsSistema.aplicarComoPadding(raiz);

        destino = (Destino) getIntent().getSerializableExtra(EXTRA_DESTINO);

        if (destino == null) {
            destino = Destino.CONDOMINIO;
        }

        lista = findViewById(R.id.listaCentralNotificacoes);

        findViewById(R.id.botaoVoltarCentral).setOnClickListener(view -> finish());

        configurarPorDestino(raiz);
    }

    @Override
    protected void onStart() {
        super.onStart();

        exibir();
    }

    private void configurarPorDestino(View raiz) {

        View botaoNovo = findViewById(R.id.botaoNovaNotificacaoCentral);

        if (destino == Destino.COOPERATIVA) {
            botaoNovo.setVisibility(View.VISIBLE);
            botaoNovo.setOnClickListener(view -> NovoAvisoCooperativaDialogo.mostrar(this));
        }

        if (destino != Destino.SINDICO) {
            return;
        }

        botaoNovo.setVisibility(View.VISIBLE);
        botaoNovo.setOnClickListener(view -> abrirFormularioDoSindico());

        SindicoViewModel viewModel = new ViewModelProvider(this).get(SindicoViewModel.class);

        viewModel
                .getAvisos()
                .observe(
                        this,
                        avisos -> {
                            avisosDoSindico = avisos == null ? new ArrayList<>() : avisos;

                            exibir();
                        });

        BarraNavegacaoView barra = findViewById(R.id.barraInferiorCentral);

        barra.setVisibility(View.VISIBLE);
        barra.configurar(ItensBarra.sindico());
        Navegacao.marcarNaBarra(this, barra, ItensBarra.HOME);
        barra.setOnAssistenteClickListener(view -> startActivity(new Intent(this, ChatActivity.class)));
        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDoSindico(this, indice, ItensBarra.NENHUM));

        getSupportFragmentManager()
                .setFragmentResultListener(
                        "rascunhoSindico",
                        this,
                        (chave, resultado) ->
                                AvisoFlutuante.mostrar(this, R.string.sindico_rascunho_salvo));
    }

    private void abrirFormularioDoSindico() {

        if (getSupportFragmentManager().findFragmentByTag("criarAvisoSindico") == null) {
            new SindicoCriarAvisoDialogFragment()
                    .show(getSupportFragmentManager(), "criarAvisoSindico");
        }
    }

    private void exibir() {

        lista.removeAllViews();

        LayoutInflater inflador = LayoutInflater.from(this);

        String hoje = getString(R.string.grupo_hoje);
        String ontem = getString(R.string.grupo_ontem);
        String anteriores = getString(R.string.grupo_anteriores);

        // A ordem das chaves é a ordem da tela; dentro do dia, notificações vêm antes dos avisos.
        Map<String, List<View>> grupos = new LinkedHashMap<>();

        grupos.put(hoje, new ArrayList<>());
        grupos.put(ontem, new ArrayList<>());
        grupos.put(anteriores, new ArrayList<>());

        // As notificações de exemplo não são do síndico: ele só vê os avisos que existem de verdade.
        for (Notificacao notificacao : destino == Destino.SINDICO ? Collections.<Notificacao>emptyList() : Notificacao.lista()) {
            List<View> grupo = grupos.get(notificacao.grupo);

            if (grupo != null) {
                grupo.add(CartoesCentral.notificacao(inflador, lista, notificacao));
            }
        }

        if (destino == Destino.SINDICO) {
            for (SindicoAviso aviso : avisosDoSindico) {
                View cartao = CartoesCentral.avisoDoSindico(inflador, lista, aviso);

                cartao.setOnClickListener(view -> abrirAvisoDoSindico(aviso));
                Motion.pressFeedback(cartao);

                // O síndico guarda o momento como texto ("Agora", "Há 2 dias").
                grupos.get("Agora".equals(aviso.getMomento()) ? hoje : anteriores).add(cartao);
            }
        } else {
            for (Aviso aviso : Aviso.lista()) {
                View cartao = CartoesCentral.aviso(inflador, lista, aviso);

                cartao.setOnClickListener(
                        view -> startActivity(AvisoDetalheActivity.criarIntent(this, aviso.id)));
                Motion.pressFeedback(cartao);

                grupos.get(grupoDoAviso(aviso, hoje, ontem, anteriores)).add(cartao);
            }
        }

        boolean vazia = true;

        for (Map.Entry<String, List<View>> grupo : grupos.entrySet()) {

            if (grupo.getValue().isEmpty()) {
                continue;
            }

            vazia = false;

            lista.addView(rotuloDoGrupo(grupo.getKey()));

            for (View cartao : grupo.getValue()) {
                lista.addView(cartao);
            }
        }

        if (vazia) {
            TextView aviso = new TextView(this);

            aviso.setText(R.string.central_sem_itens);
            aviso.setTextColor(getColor(R.color.cinza_texto_auxiliar));
            aviso.setTextSize(14);
            aviso.setGravity(android.view.Gravity.CENTER);
            aviso.setPadding(dp(22), dp(48), dp(22), dp(0));
            lista.addView(aviso);
        }
    }

    @NonNull
    private String grupoDoAviso(Aviso aviso, String hoje, String ontem, String anteriores) {

        Calendar enviado = FormatoDataApi.calendario(aviso.enviadoEm);

        if (enviado == null) {
            return anteriores;
        }

        long dias = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - enviado.getTimeInMillis());

        return dias < 1 ? hoje : dias < 2 ? ontem : anteriores;
    }

    private TextView rotuloDoGrupo(String texto) {

        TextView rotulo = new TextView(this);

        rotulo.setText(texto);
        rotulo.setTextColor(getColor(R.color.verde_escuro_principal));
        rotulo.setTextSize(15);
        rotulo.setTypeface(ResourcesCompat.getFont(this, R.font.nunito_bold));
        rotulo.setPadding(dp(22), dp(16), dp(22), dp(4));

        return rotulo;
    }

    private void abrirAvisoDoSindico(SindicoAviso aviso) {

        String detalhe =
                getString(
                        R.string.sindico_aviso_detalhe,
                        aviso.getMensagem(),
                        aviso.getDestinatario(),
                        aviso.getAutor(),
                        aviso.getMomento());

        if (aviso.isRascunho()) {
            detalhe += "\n\n" + getString(R.string.sindico_rascunho_local_ainda_nao_enviado);
        }

        new DialogoEco.Builder(this)
                .titulo(aviso.getTitulo())
                .mensagem(detalhe)
                .botao(R.string.fechar, DialogoEco.Estilo.PREENCHIDO, null)
                .mostrar();
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}
