package com.example.ecociente.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.TextUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecociente.R;
import com.example.ecociente.model.Aviso;
import com.example.ecociente.model.Coletas;
import com.example.ecociente.model.CondominioAtendido;
import com.example.ecociente.repository.CondominiosAtendidosRepository;
import com.google.android.material.card.MaterialCardView;
import android.widget.LinearLayout;
import android.view.ViewGroup;
import android.graphics.Typeface;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.model.ResultadoSolicitacoes;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.repository.AquecimentoDasApis;
import com.example.ecociente.repository.PerfilApiRepository;
import com.example.ecociente.ui.AvatarPerfil;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.CalendarioMensalView;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.MenuPerfil;
import com.example.ecociente.ui.ProximaColetaView;
import com.example.ecociente.notifications.PermissaoNotificacoes;
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
    private LinearLayout listaCondominios;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        setContentView(R.layout.activity_home_cooperativa);

        AquecimentoDasApis.aquecer(this);
        new PerfilApiRepository().atualizarSeNecessario(this);

        new PermissaoNotificacoes(this).pedirSeNecessario();

        viewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        textoSaudacao = findViewById(R.id.textoSaudacaoCooperativa);
        textoNomeCooperativa = findViewById(R.id.textoNomeCooperativa);
        imagemPerfil = findViewById(R.id.imagemPerfilCooperativa);
        listaCondominios = findViewById(R.id.listaCondominiosAtendidos);

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

        // A raiz não recorta os filhos (por causa da barra); sem isto o conteúdo rolado
        // desenha por cima do cabeçalho.
        findViewById(R.id.rolagemHomeCooperativa)
                .addOnLayoutChangeListener(
                        (rolagem, esquerda, topo, direita, base, e, t, d, b) ->
                                rolagem.setClipBounds(
                                        new android.graphics.Rect(0, 0, direita - esquerda, base - topo)));

        configurarCalendario();

        findViewById(R.id.botaoAvisosCooperativa)
                .setOnClickListener(
                        view ->
                                startActivity(
                                        CentralNotificacoesActivity.criarIntent(
                                                this, CentralNotificacoesActivity.Destino.COOPERATIVA)));

        findViewById(R.id.cardAvisosCooperativa)
                .setOnClickListener(view -> startActivity(CentralNotificacoesActivity.criarIntent(this, CentralNotificacoesActivity.Destino.COOPERATIVA)));

        findViewById(R.id.cardChatbotCooperativa)
                .setOnClickListener(view -> startActivity(new Intent(this, ChatActivity.class)));
    }


    private void carregarCondominiosAtendidos() {
        if (isFinishing() || isDestroyed() || listaCondominios == null) return;
        listaCondominios.removeAllViews();
        listaCondominios.addView(textoLista("Carregando condomínios atendidos…", 14, false));
        int cooperativaId = PerfilApiRepository.cooperativaId(this);
        new CondominiosAtendidosRepository().listar(this, cooperativaId, resultado -> {
            if (isFinishing() || isDestroyed()) return;
            listaCondominios.removeAllViews();
            if (resultado == null) {
                listaCondominios.addView(textoLista("Não foi possível carregar os condomínios. Verifique a conexão e tente novamente.", 14, false));
                return;
            }
            if (resultado.isEmpty()) {
                listaCondominios.addView(textoLista("Ainda não há condomínios com coletas confirmadas ou realizadas para esta cooperativa.", 14, false));
                return;
            }
            for (CondominioAtendido condominio : resultado) {
                MaterialCardView card = new MaterialCardView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.bottomMargin = Math.round(10 * getResources().getDisplayMetrics().density);
                card.setLayoutParams(params);
                card.setRadius(14 * getResources().getDisplayMetrics().density);
                card.setCardElevation(getResources().getDisplayMetrics().density);
                card.setStrokeWidth(Math.round(getResources().getDisplayMetrics().density));
                card.setStrokeColor(getColor(R.color.cinza_borda_clara_home));
                LinearLayout conteudo = new LinearLayout(this);
                conteudo.setOrientation(LinearLayout.VERTICAL);
                int padding = Math.round(14 * getResources().getDisplayMetrics().density);
                conteudo.setPadding(padding, padding, padding, padding);
                conteudo.addView(textoLista(condominio.getNome(), 16, true));
                if (!condominio.getEndereco().isEmpty()) conteudo.addView(textoLista(condominio.getEndereco(), 12, false));
                conteudo.addView(textoLista(condominio.getTotalTorres() + " torres cadastradas", 12, false));
                conteudo.addView(textoLista(condominio.getDiasColeta().isEmpty()
                        ? "Sem recorrência de coleta confirmada."
                        : "Dias de coleta: " + TextUtils.join(", ", condominio.getDiasColeta()), 12, false));
                card.addView(conteudo);
                listaCondominios.addView(card);
            }
        });
    }

    private TextView textoLista(String valor, int tamanhoSp, boolean destaque) {
        TextView texto = new TextView(this);
        texto.setText(valor);
        texto.setTextSize(tamanhoSp);
        texto.setTextColor(getColor(destaque ? R.color.verde_escuro_principal : R.color.cinza_texto_home));
        texto.setTypeface(null, destaque ? Typeface.BOLD : Typeface.NORMAL);
        if (!destaque) texto.setPadding(0, Math.round(4 * getResources().getDisplayMetrics().density), 0, 0);
        return texto;
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
        new PerfilApiRepository().atualizarSeNecessario(this, this::carregarCondominiosAtendidos);

        TextView avisoDestaque = findViewById(R.id.textoAvisoDestaqueCooperativa);
        TextView avisoOrigem = findViewById(R.id.textoAvisoOrigemCooperativa);

        if (Aviso.lista().isEmpty()) {
            avisoDestaque.setText(R.string.sem_avisos);
            avisoOrigem.setText("");
        } else {
            Aviso recente = Aviso.lista().get(0);

            avisoDestaque.setText(recente.titulo);
            avisoOrigem.setText(recente.remetente);
        }

        findViewById(R.id.pontoAvisosCooperativa)
                .setVisibility(Aviso.quantidadeNaoLidos() > 0 ? View.VISIBLE : View.GONE);

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
