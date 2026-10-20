package com.example.ecociente.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import com.google.android.material.button.MaterialButton;
import com.example.ecociente.ui.DialogoEco;
import com.example.ecociente.ui.BotaoCarregando;
import com.example.ecociente.ui.AvisoFlutuante;
import com.example.ecociente.repository.FotoPerfilRepository;
import androidx.core.content.FileProvider;
import androidx.annotation.Nullable;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.ActivityResultLauncher;
import android.net.Uri;
import android.content.ActivityNotFoundException;
import com.bumptech.glide.Glide;
import com.example.ecociente.R;
import com.example.ecociente.model.PostagemFeed;
import com.example.ecociente.model.TipoLista;
import com.example.ecociente.repository.FeedRepository;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.Iniciais;
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
    private MaterialButton botaoEnviarFoto;
    private Uri uriFotoCamera;

    private final ActivityResultLauncher<PickVisualMediaRequest> seletorFoto =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::enviarFoto);

    private final ActivityResultLauncher<Uri> camera =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    tirou -> {
                        if (tirou) {
                            enviarFoto(uriFotoCamera);
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

        botaoEnviarFoto = findViewById(R.id.botaoEnviarFotoFeed);
        botaoEnviarFoto.setOnClickListener(view -> mostrarOpcoesFoto());
        Motion.pressFeedback(botaoEnviarFoto);

        View meusDescartes = findViewById(R.id.botaoMeusDescartes);
        meusDescartes.setOnClickListener(
                view -> startActivity(TelaListaActivity.criarIntent(this, TipoLista.MEUS_DESCARTES, true)));
        Motion.pressFeedback(meusDescartes);

        IndicadorCarregamento.mostrar(lista, R.string.feed_carregando);

        new FeedRepository().listar(this, this::exibir);
    }

    private void mostrarOpcoesFoto() {

        new DialogoEco.Builder(this)
                .titulo(R.string.feed_foto_titulo)
                .botao(R.string.perfil_foto_tirar, DialogoEco.Estilo.PREENCHIDO, this::abrirCamera)
                .botao(R.string.perfil_foto_galeria, DialogoEco.Estilo.CONTORNO, this::abrirGaleria)
                .botao(R.string.perfil_cancelar, DialogoEco.Estilo.CONTORNO, null)
                .mostrar();
    }

    private void abrirGaleria() {

        seletorFoto.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
    }

    private void abrirCamera() {

        File pasta = new File(getCacheDir(), "fotos");

        pasta.mkdirs();

        File[] antigas = pasta.listFiles();

        if (antigas != null) {
            for (File antiga : antigas) {
                antiga.delete();
            }
        }

        File arquivo = new File(pasta, "feed_" + System.currentTimeMillis() + ".jpg");

        uriFotoCamera = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", arquivo);

        try {
            camera.launch(uriFotoCamera);

        } catch (ActivityNotFoundException erro) {
            AvisoFlutuante.mostrar(this, R.string.perfil_foto_erro_camera);
        }
    }

    private void enviarFoto(@Nullable Uri imagem) {

        if (imagem == null) {
            return;
        }

        BotaoCarregando.definir(botaoEnviarFoto, true, R.string.feed_registrar, R.string.feed_enviando);

        new FotoPerfilRepository()
                .enviarParaFeed(getApplicationContext(), imagem)
                .observe(
                        this,
                        resultado -> {
                            BotaoCarregando.definir(
                                    botaoEnviarFoto, false, R.string.feed_registrar, R.string.feed_enviando);

                            if (!resultado.isSucesso()) {
                                AvisoFlutuante.mostrar(this, resultado.getMensagemErro());
                                return;
                            }

                            // A URL (resultado.getUrl()) já está no Cloudinary. Registrar a postagem
                            // na ds-pontuacao-api depende da rota de criação, que ainda não existe.
                            AvisoFlutuante.mostrar(this, R.string.feed_foto_enviada);
                        });
    }

    private void configurarBarra() {

        BarraNavegacaoView barra = findViewById(R.id.barraNavegacao);

        barra.configurar(ItensBarra.morador());

        Navegacao.marcarNaBarra(this, barra, ItensBarra.QUARTO);

        barra.setOnAssistenteClickListener(view -> startActivity(new Intent(this, ChatActivity.class)));

        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDoUsuario(this, indice, ItensBarra.QUARTO, true));
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

            lista.addView(cartao);
        }

        textoVazio.setVisibility(postagens.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
