package com.example.ecociente.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.ecociente.repository.PostagemRepository;
import com.google.android.material.button.MaterialButton;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecociente.ui.AvisoFlutuante;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;
import com.bumptech.glide.Glide;
import com.example.ecociente.R;
import com.example.ecociente.model.PostagemFeed;
import com.example.ecociente.model.TipoLista;
import com.example.ecociente.repository.FeedRepository;
import com.example.ecociente.repository.PerfilApiRepository;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.util.Iniciais;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.Motion;
import com.example.ecociente.ui.Navegacao;
import java.util.List;
import com.example.ecociente.ui.IndicadorCarregamento;

// Aba Feed do morador: fotos de descarte do condomínio.
public class FeedActivity extends AppCompatActivity {

    private LinearLayout lista;
    private View textoVazio;

    private final ActivityResultLauncher<Intent> registro =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    resultado -> {
                        if (resultado.getResultCode() == RESULT_OK) {
                            AvisoFlutuante.mostrar(this, R.string.registro_enviado);
                            carregarFeed();
                        }
                    });

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        JanelaEdgeToEdge.aplicar(this);

        setContentView(R.layout.activity_feed);

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizFeed));

        lista = findViewById(R.id.listaFeed);
        textoVazio = findViewById(R.id.textoFeedVazio);

        Motion.entrar(this, R.id.tituloFeed, R.id.subtituloFeed, R.id.listaFeed);

        configurarBarra();

        View registrar = findViewById(R.id.botaoRegistrarDescarte);
        registrar.setOnClickListener(view -> registro.launch(new Intent(this, RegistroDescarteActivity.class)));
        Motion.pressFeedback(registrar);

        View meusDescartes = findViewById(R.id.textoMeusDescartes);
        meusDescartes.setOnClickListener(
                view -> startActivity(TelaListaActivity.criarIntent(this, TipoLista.MEUS_DESCARTES, true)));
        Motion.pressFeedback(meusDescartes);

        carregarFeed();
    }

    // O condomínio do morador vem do perfil da API: atualiza antes de pedir o feed.
    private void carregarFeed() {

        lista.removeAllViews();
        textoVazio.setVisibility(View.GONE);

        IndicadorCarregamento.mostrar(lista, R.string.feed_carregando);

        new PerfilApiRepository()
                .atualizarSeNecessario(
                        this,
                        () -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            new FeedRepository()
                                    .listar(
                                            this,
                                            new FeedRepository.Retorno() {
                                                @Override
                                                public void aoConcluir(List<PostagemFeed> postagens) {
                                                    exibir(postagens);
                                                }

                                                @Override
                                                public void aoFalhar() {
                                                    exibirErro(R.string.feed_erro);
                                                }

                                                @Override
                                                public void aoNegar() {
                                                    exibirErro(R.string.feed_sem_aprovacao);
                                                }
                                            });
                        });
    }

    private void exibirErro(@StringRes int mensagem) {

        lista.removeAllViews();

        ((TextView) textoVazio).setText(mensagem);
        textoVazio.setVisibility(View.VISIBLE);

        com.google.android.material.button.MaterialButton tentar =
                (com.google.android.material.button.MaterialButton)
                        getLayoutInflater().inflate(R.layout.botao_dialogo, lista, false);

        tentar.setText(R.string.tentar_novamente);
        tentar.setOnClickListener(view -> carregarFeed());

        lista.addView(tentar);
    }

    private void configurarBarra() {

        BarraNavegacaoView barra = findViewById(R.id.barraNavegacao);

        barra.configurar(ItensBarra.morador());

        Navegacao.marcarNaBarra(this, barra, ItensBarra.QUARTO);

        barra.setOnAssistenteClickListener(view -> startActivity(new Intent(this, ChatActivity.class)));

        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDoUsuario(this, indice, ItensBarra.QUARTO, true));
    }

    // Reaproveita a linha de dois botões do cartão (a mesma que o síndico usa): Aprovar e Denunciar.
    private void configurarVoto(View cartao, PostagemFeed postagem) {

        cartao.findViewById(R.id.acoesValidacaoFeed).setVisibility(View.VISIBLE);

        MaterialButton aprovar = cartao.findViewById(R.id.botaoAprovarFeed);
        MaterialButton denunciar = cartao.findViewById(R.id.botaoReprovarFeed);

        denunciar.setText(R.string.feed_denunciar);

        aprovar.setOnClickListener(view -> votar(cartao, postagem, true, 0, aprovar, denunciar));
        denunciar.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle(R.string.feed_denunciar_titulo)
                .setItems(R.array.motivos_denuncia, (dialogo, posicao) -> votar(
                        cartao, postagem, false, getResources().getIntArray(R.array.motivos_denuncia_ids)[posicao],
                        aprovar, denunciar))
                .setNegativeButton(R.string.perfil_cancelar, null)
                .show());
    }

    private void votar(View cartao, PostagemFeed postagem, boolean aprovar, int motivoDenunciaId,
                       MaterialButton botaoAprovar, MaterialButton botaoDenunciar) {

        botaoAprovar.setEnabled(false);
        botaoDenunciar.setEnabled(false);

        new PostagemRepository().votar(this, postagem.getId(), aprovar, motivoDenunciaId, resultado -> {
            if (isFinishing() || isDestroyed()) return;

            if (!resultado.isSucesso()) {
                botaoAprovar.setEnabled(true);
                botaoDenunciar.setEnabled(true);
                AvisoFlutuante.mostrar(this, resultado.getMensagemErro());
                return;
            }

            cartao.findViewById(R.id.acoesValidacaoFeed).setVisibility(View.GONE);
            AvisoFlutuante.mostrar(this, aprovar ? R.string.feed_voto_aprovado : R.string.feed_voto_denunciado);
        });
    }

    private void exibir(List<PostagemFeed> postagens) {

        lista.removeAllViews();

        LayoutInflater inflador = LayoutInflater.from(this);

        for (PostagemFeed postagem : postagens) {

            View cartao = inflador.inflate(R.layout.item_feed_postagem, lista, false);

            ((TextView) cartao.findViewById(R.id.iniciaisFeed)).setText(Iniciais.de(postagem.getAutor()));
            ((TextView) cartao.findViewById(R.id.autorFeed)).setText(postagem.getAutor());
            ((TextView) cartao.findViewById(R.id.quandoFeed)).setText(postagem.getQuando());
            ((TextView) cartao.findViewById(R.id.categoriaFeed)).setText(postagem.getCategoria());
            ((TextView) cartao.findViewById(R.id.statusFeed)).setText(postagem.getStatus());

            cartao.findViewById(R.id.categoriaFeed)
                    .setVisibility(postagem.getCategoria().isEmpty() ? View.GONE : View.VISIBLE);

            Glide.with(this).load(postagem.getFotoUrl()).into((ImageView) cartao.findViewById(R.id.fotoFeed));

            // Só dá para votar enquanto o descarte está em análise (janela de 24 h).
            if (PostagemRepository.estaEmAnalise(postagem)) {
                configurarVoto(cartao, postagem);
            }

            lista.addView(cartao);
        }

        ((TextView) textoVazio).setText(R.string.feed_vazio);
        textoVazio.setVisibility(postagens.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
