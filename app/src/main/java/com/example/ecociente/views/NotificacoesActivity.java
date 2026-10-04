package com.example.ecociente.views;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecociente.R;
import com.example.ecociente.model.PreferenciasNotificacao;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.ui.Motion;
import com.example.ecociente.viewmodels.PerfilViewModel;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseUser;

public class NotificacoesActivity extends AppCompatActivity {

    private PerfilViewModel viewModel;

    private final Motion motion = new Motion();

    private SwitchMaterial switchNotificacoesGerais;
    private SwitchMaterial switchLembretesAvisos;
    private SwitchMaterial switchNovasAtividades;
    private SwitchMaterial switchDicasSustentaveis;

    private String uid;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        setContentView(R.layout.activity_notificacoes);

        viewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        inicializarComponentes();

        findViewById(R.id.botaoVoltarNotificacoes).setOnClickListener(view -> finish());

        carregarPreferencias();

        motion.staggerIn(
                findViewById(R.id.cabecalhoNotificacoes),
                findViewById(R.id.containerIntroNotificacoes),
                findViewById(R.id.tituloSecaoApp),
                findViewById(R.id.containerSecaoApp),
                findViewById(R.id.tituloSecaoAtividades),
                findViewById(R.id.containerSecaoAtividades),
                findViewById(R.id.tituloSecaoSustentabilidade),
                findViewById(R.id.containerSecaoSustentabilidade));
    }

    @Override
    protected void onDestroy() {
        motion.cancelAll();

        super.onDestroy();
    }

    private void inicializarComponentes() {

        View linhaGerais = findViewById(R.id.toggleNotificacoesGerais);

        View linhaLembretes = findViewById(R.id.toggleLembretesAvisos);

        View linhaAtividades = findViewById(R.id.toggleNovasAtividades);

        View linhaSustentaveis = findViewById(R.id.toggleDicasSustentaveis);

        configurarRotulo(linhaGerais, R.string.perfil_notificacao_gerais, R.drawable.icon_notificacoes);

        configurarRotulo(linhaLembretes, R.string.perfil_notificacao_lembretes, R.drawable.icon_notificacoes);

        configurarRotulo(linhaAtividades, R.string.perfil_notificacao_atividades, R.drawable.icon_notificacoes);

        configurarRotulo(
                linhaSustentaveis,
                R.string.perfil_notificacao_sustentaveis,
                R.drawable.ic_folha_sustentabilidade);

        switchNotificacoesGerais = linhaGerais.findViewById(R.id.switchToggleNotificacao);

        switchLembretesAvisos = linhaLembretes.findViewById(R.id.switchToggleNotificacao);

        switchNovasAtividades = linhaAtividades.findViewById(R.id.switchToggleNotificacao);

        switchDicasSustentaveis = linhaSustentaveis.findViewById(R.id.switchToggleNotificacao);
    }

    private void configurarRotulo(View linha, int rotulo, int icone) {

        ((TextView) linha.findViewById(R.id.rotuloToggleNotificacao)).setText(rotulo);

        ((ImageView) linha.findViewById(R.id.iconeToggleNotificacao)).setImageResource(icone);
    }

    private void carregarPreferencias() {

        FirebaseUser usuario = viewModel.usuarioAtual();

        if (usuario == null) {
            return;
        }

        uid = usuario.getUid();

        viewModel.buscarPreferencias(uid).observe(this, this::preencherSwitches);
    }

    private void preencherSwitches(PreferenciasNotificacao preferencias) {

        if (preferencias == null) {
            preferencias = new PreferenciasNotificacao();
        }

        definirSemDispararListener(switchNotificacoesGerais, preferencias.isNotificacoesGerais());

        definirSemDispararListener(switchLembretesAvisos, preferencias.isLembretesAvisos());

        definirSemDispararListener(switchNovasAtividades, preferencias.isNovasAtividades());

        definirSemDispararListener(switchDicasSustentaveis, preferencias.isDicasSustentaveis());

        configurarListenerPreferencia(
                switchNotificacoesGerais, PreferenciasNotificacao.CHAVE_NOTIFICACOES_GERAIS);

        configurarListenerPreferencia(
                switchLembretesAvisos, PreferenciasNotificacao.CHAVE_LEMBRETES_AVISOS);

        configurarListenerPreferencia(
                switchNovasAtividades, PreferenciasNotificacao.CHAVE_NOVAS_ATIVIDADES);

        configurarListenerPreferencia(
                switchDicasSustentaveis, PreferenciasNotificacao.CHAVE_DICAS_SUSTENTAVEIS);
    }

    private void definirSemDispararListener(SwitchMaterial botao, boolean marcado) {

        botao.setOnCheckedChangeListener(null);

        botao.setChecked(marcado);
    }

    private void configurarListenerPreferencia(SwitchMaterial botao, String chave) {

        botao.setOnCheckedChangeListener(
                (buttonView, marcado) -> {
                    if (uid == null) {
                        return;
                    }

                    viewModel
                            .atualizarPreferencia(uid, chave, marcado)
                            .observe(this, resultado -> tratarResultadoPreferencia(botao, marcado, resultado));
                });
    }

    private void tratarResultadoPreferencia(
            SwitchMaterial botao, boolean valorTentado, ResultadoApi resultado) {

        if (resultado == null || resultado.isSucesso()) {
            return;
        }

        // Falhou salvar no Firestore: desfaz o toggle na tela pra não mentir
        // pro usuário sobre uma preferência que não foi realmente gravada.
        Toast.makeText(this, R.string.perfil_notificacao_erro_salvar, Toast.LENGTH_SHORT).show();

        definirSemDispararListener(botao, !valorTentado);

        String chave = chavePor(botao);

        if (chave != null) {
            configurarListenerPreferencia(botao, chave);
        }
    }

    private String chavePor(SwitchMaterial botao) {

        if (botao == switchNotificacoesGerais) {
            return PreferenciasNotificacao.CHAVE_NOTIFICACOES_GERAIS;
        }

        if (botao == switchLembretesAvisos) {
            return PreferenciasNotificacao.CHAVE_LEMBRETES_AVISOS;
        }

        if (botao == switchNovasAtividades) {
            return PreferenciasNotificacao.CHAVE_NOVAS_ATIVIDADES;
        }

        if (botao == switchDicasSustentaveis) {
            return PreferenciasNotificacao.CHAVE_DICAS_SUSTENTAVEIS;
        }

        return null;
    }
}
