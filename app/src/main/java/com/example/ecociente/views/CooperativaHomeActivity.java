package com.example.ecociente.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecociente.R;
import com.example.ecociente.model.Coletas;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.model.TipoLista;
import com.example.ecociente.model.ResultadoSolicitacoes;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.ui.AvatarPerfil;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.CalendarioMensalView;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.MenuPerfil;
import com.example.ecociente.ui.ProximaColetaView;
import com.example.ecociente.ui.Motion;
import com.example.ecociente.ui.Navegacao;
import com.example.ecociente.viewmodels.CalendarioViewModel;
import com.example.ecociente.viewmodels.PerfilViewModel;
import com.google.firebase.auth.FirebaseUser;
import java.util.Collections;
import java.util.List;

public class CooperativaHomeActivity extends AppCompatActivity {

    private PerfilViewModel viewModel;
    private CalendarioViewModel calendarioViewModel;

    private CalendarioMensalView calendarioMensal;

    private final Motion motion = new Motion();

    private TextView textoSaudacao;
    private TextView textoNomeCooperativa;
    private ImageView imagemPerfil;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        setContentView(R.layout.activity_home_cooperativa);

        viewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        textoSaudacao = findViewById(R.id.textoSaudacaoCooperativa);
        textoNomeCooperativa = findViewById(R.id.textoNomeCooperativa);
        imagemPerfil = findViewById(R.id.imagemPerfilCooperativa);

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizHomeCooperativa));

        configurarNavegacao();

        imagemPerfil.setOnClickListener(
                ancora ->
                        MenuPerfil.mostrar(
                                this, ancora, this::abrirGerenciarPerfil, this::confirmarSaida));

        motion.staggerIn(
                findViewById(R.id.cabecalhoHomeCooperativa),
                textoSaudacao,
                textoNomeCooperativa,
                findViewById(R.id.cardProximaColeta),
                findViewById(R.id.cardCalendarioColetas));

        configurarCalendario();

        configurarAtalhos();
    }

    private void configurarAtalhos() {

        findViewById(R.id.atalhoAvisos)
                .setOnClickListener(view -> startActivity(AvisosActivity.criarIntent(this, true)));

        findViewById(R.id.atalhoNovaColeta)
                .setOnClickListener(view -> startActivity(NovaColetaActivity.criarIntent(this, null)));

        findViewById(R.id.atalhoCondominios).setOnClickListener(view -> abrirLista(TipoLista.CONDOMINIOS));

        findViewById(R.id.atalhoAvaliacoes).setOnClickListener(view -> abrirLista(TipoLista.AVALIACOES));

        findViewById(R.id.atalhoNotificacoes)
                .setOnClickListener(view -> startActivity(new Intent(this, NotificacoesActivity.class)));
    }

    private void abrirLista(TipoLista tipo) {
        startActivity(TelaListaActivity.criarIntent(this, tipo, false));
    }

    private void configurarCalendario() {

        calendarioViewModel = new ViewModelProvider(this).get(CalendarioViewModel.class);

        calendarioMensal = findViewById(R.id.calendarioMensal);

        calendarioMensal.setOnNavegacaoListener(
                new CalendarioMensalView.OnNavegacaoListener() {
                    @Override
                    public void aoMesAnterior() {
                        calendarioViewModel.mesAnterior();
                    }

                    @Override
                    public void aoProximoMes() {
                        calendarioViewModel.proximoMes();
                    }
                });

        ProximaColetaView proximaColeta = findViewById(R.id.cardProximaColeta);

        calendarioViewModel
                .getProxima()
                .observe(
                        this,
                        resultado ->
                                proximaColeta.exibir(
                                        resultado,
                                        calendarioViewModel::carregar,
                                        this::sairParaLogin));

        calendarioViewModel.getColetasDoMes().observe(this, resultado -> atualizarCalendario());

        calendarioViewModel.getMesExibido().observe(this, mes -> atualizarCalendario());

        if (!calendarioViewModel.jaCarregou()) {
            calendarioViewModel.carregar();
        }
    }

    private void atualizarCalendario() {

        int[] mes = calendarioViewModel.getMesExibido().getValue();

        ResultadoSolicitacoes resultado = calendarioViewModel.getColetasDoMes().getValue();

        List<Solicitacao> coletas =
                resultado == null ? Collections.emptyList() : resultado.getItens();

        calendarioMensal.exibir(mes[0], mes[1], Coletas.diasDoMes(coletas, mes[0], mes[1]));
    }

    private void sairParaLogin() {

        viewModel.encerrarSessao();

        Navegacao.abrirLoginLimpandoPilha(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        carregarPerfil();

        if (calendarioViewModel != null && calendarioViewModel.jaCarregou()) {
            calendarioViewModel.carregar();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        Navegacao.marcarNaBarra(this, findViewById(R.id.barraNavegacao), ItensBarra.HOME);
    }

    @Override
    protected void onDestroy() {
        motion.cancelAll();

        super.onDestroy();
    }

    private void carregarPerfil() {

        FirebaseUser usuario = viewModel.usuarioAtual();

        if (usuario == null) {
            return;
        }

        viewModel
                .buscarPerfil(usuario.getUid())
                .observe(this, perfil -> preencherCabecalho(usuario, perfil));
    }

    private void preencherCabecalho(FirebaseUser usuario, PerfilUsuario perfil) {

        String nome = PerfilUsuario.nomeExibicao(perfil, usuario).trim();

        String primeiroNome = nome.isEmpty() ? "" : nome.split("\\s+")[0];

        textoSaudacao.setText(
                primeiroNome.isEmpty()
                        ? getString(R.string.home_usuario_padrao)
                        : getString(R.string.home_saudacao, primeiroNome));

        if (perfil == null) {
            return;
        }

        textoNomeCooperativa.setText(perfil.getNomeCooperativa());

        AvatarPerfil.exibir(imagemPerfil, perfil.getFotoUrl());
    }

    private void configurarNavegacao() {

        BarraNavegacaoView barra = findViewById(R.id.barraNavegacao);

        barra.configurar(ItensBarra.cooperativa());

        Navegacao.marcarNaBarra(this, barra, ItensBarra.HOME);

        barra.setOnAssistenteClickListener(view -> startActivity(new Intent(this, ChatActivity.class)));

        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDaCooperativa(this, indice, ItensBarra.HOME));
    }

    private void abrirGerenciarPerfil() {

        startActivity(intentPerfil());
    }

    private Intent intentPerfil() {

        return new Intent(this, GerenciarPerfilActivity.class)
                .putExtra(GerenciarPerfilActivity.EXTRA_COOPERATIVA, true);
    }

    private void confirmarSaida() {

        MenuPerfil.confirmarSaida(
                this,
                () -> {
                    viewModel.encerrarSessao();

                    Navegacao.abrirLoginLimpandoPilha(this);
                });
    }
}
