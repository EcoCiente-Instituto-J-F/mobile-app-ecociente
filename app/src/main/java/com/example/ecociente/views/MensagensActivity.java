package com.example.ecociente.views;

import com.example.ecociente.ui.EstadoTela;
import com.example.ecociente.ui.Motion;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.ecociente.R;
import com.example.ecociente.model.Conversa;
import com.example.ecociente.repository.ConversaRepository;
import com.example.ecociente.util.FormatoDataApi;
import com.example.ecociente.ui.IndicadorCarregamento;
import java.util.ArrayList;
import java.util.List;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.Navegacao;
import com.example.ecociente.ui.AoDigitar;
import com.example.ecociente.util.Iniciais;
import java.util.Locale;
import android.content.Context;
import androidx.annotation.NonNull;

public class MensagensActivity extends AppCompatActivity {

    private static final int[] IDS_ABAS = {R.id.abaTodos, R.id.abaNaoLidos, R.id.abaArquivadas};

    private static final String EXTRA_SINDICO = "mensagensDoSindico";

    private boolean sindico;

    private LinearLayout lista;
    private View textoSemConversas;
    private EditText campoBusca;
    private int abaAtual;
    private final List<Conversa> conversas = new ArrayList<>();

    @NonNull
    public static Intent criarIntentSindico(@NonNull Context contexto) {

        return new Intent(contexto, MensagensActivity.class).putExtra(EXTRA_SINDICO, true);
    }

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        sindico = getIntent().getBooleanExtra(EXTRA_SINDICO, false);

        JanelaEdgeToEdge.aplicar(this);

        setContentView(R.layout.activity_mensagens);

        Motion.entrar(
                this,
                R.id.tituloMensagens,
                R.id.campoBuscaConversa,
                R.id.cardAbasMensagens,
                R.id.listaConversas);

        lista = findViewById(R.id.listaConversas);
        textoSemConversas = findViewById(R.id.textoSemConversas);
        campoBusca = findViewById(R.id.campoBuscaConversa);

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizMensagens));

        for (int i = 0; i < IDS_ABAS.length; i++) {
            int aba = i;

            findViewById(IDS_ABAS[i]).setOnClickListener(view -> selecionarAba(aba));
        }

        AoDigitar.em(campoBusca, () -> exibirConversas(abaAtual));

        selecionarAba(0);

        configurarNavegacao();
    }

    // Recarrega ao voltar de uma conversa, para o contador de não lidas acompanhar o que foi lido.
    @Override
    protected void onStart() {
        super.onStart();
        carregar();
    }

    // As conversas ativas vêm primeiro; as arquivadas só complementam a aba "Arquivadas", então
    // falhar nelas não derruba a tela.
    private void carregar() {
        lista.removeAllViews();
        textoSemConversas.setVisibility(View.GONE);
        IndicadorCarregamento.mostrar(lista, R.string.conversas_carregando);

        ConversaRepository repositorio = new ConversaRepository();

        repositorio.listar(this, sindico, false, ativas -> {
            if (isFinishing() || isDestroyed()) return;

            if (ativas == null) {
                mostrarErro();
                return;
            }

            repositorio.listar(this, sindico, true, arquivadas -> {
                if (isFinishing() || isDestroyed()) return;

                conversas.clear();
                conversas.addAll(ativas);
                if (arquivadas != null) conversas.addAll(arquivadas);
                exibirConversas(abaAtual);
            });
        });
    }

    private void mostrarErro() {
        lista.removeAllViews();

        EstadoTela.erro(lista, R.string.conversas_erro, this::carregar);
    }

    private void selecionarAba(int aba) {

        abaAtual = aba;

        for (int i = 0; i < IDS_ABAS.length; i++) {

            TextView texto = findViewById(IDS_ABAS[i]);

            boolean selecionada = i == aba;

            texto.setBackgroundResource(selecionada ? R.drawable.fundo_aba_selecionada : 0);

            texto.setTextColor(
                    ContextCompat.getColor(
                            this, selecionada ? R.color.branco : R.color.verde_escuro_principal));
        }

        exibirConversas(aba);
    }

    private void exibirConversas(int aba) {

        lista.removeAllViews();

        LayoutInflater inflador = LayoutInflater.from(this);

        long agora = System.currentTimeMillis();

        for (Conversa conversa : conversas) {

            if (!pertence(conversa, aba)) {
                continue;
            }

            View item = inflador.inflate(R.layout.item_conversa, lista, false);

            ((TextView) item.findViewById(R.id.textoIniciaisConversa)).setText(Iniciais.de(conversa.nome));
            ((TextView) item.findViewById(R.id.textoNomeConversa)).setText(conversa.nome);
            // A API não devolve o texto da última mensagem: a linha de baixo resume o que há de novo.
            TextView previa = item.findViewById(R.id.textoPreviaConversa);
            previa.setText(conversa.temNaoLidas()
                    ? getResources().getQuantityString(R.plurals.conversa_mensagens_novas, conversa.naoLidas, conversa.naoLidas)
                    : "");
            previa.setVisibility(conversa.temNaoLidas() ? View.VISIBLE : View.GONE);
            ((TextView) item.findViewById(R.id.textoHoraConversa))
                    .setText(FormatoDataApi.relativa(conversa.ultimaMensagemEm, agora));

            TextView contador = item.findViewById(R.id.textoNaoLidasConversa);

            contador.setText(String.valueOf(conversa.naoLidas));
            contador.setVisibility(conversa.temNaoLidas() ? View.VISIBLE : View.INVISIBLE);

            item.setOnClickListener(view -> startActivity(ConversaActivity.criarIntent(this, conversa, sindico)));

            lista.addView(item);
        }

        textoSemConversas.setVisibility(lista.getChildCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private boolean pertence(Conversa conversa, int aba) {

        String busca = campoBusca.getText().toString().trim().toLowerCase(Locale.ROOT);

        if (!busca.isEmpty() && !conversa.nome.toLowerCase(Locale.ROOT).contains(busca)) {
            return false;
        }

        if (aba == 2) {
            return conversa.arquivada;
        }

        return !conversa.arquivada && (aba == 0 || conversa.temNaoLidas());
    }

    private void configurarNavegacao() {

        BarraNavegacaoView barra = findViewById(R.id.barraNavegacao);

        barra.configurar(sindico ? ItensBarra.sindico() : ItensBarra.cooperativa());

        Navegacao.marcarNaBarra(this, barra, sindico ? ItensBarra.NENHUM : ItensBarra.TERCEIRO);

        barra.setOnAssistenteClickListener(view -> startActivity(new Intent(this, ChatActivity.class)));

        barra.setOnItemClickListener(
                indice -> {
                    if (sindico) {
                        Navegacao.irParaItemDoSindico(this, indice, ItensBarra.NENHUM);

                    } else {
                        Navegacao.irParaItemDaCooperativa(this, indice, ItensBarra.TERCEIRO);
                    }
                });
    }
}
