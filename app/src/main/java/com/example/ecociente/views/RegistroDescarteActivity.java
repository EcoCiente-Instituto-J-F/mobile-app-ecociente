package com.example.ecociente.views;

import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.example.ecociente.R;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.repository.PerfilApiRepository;
import com.example.ecociente.repository.PostagemRepository;
import com.example.ecociente.ui.AvisoFlutuante;
import com.example.ecociente.ui.BotaoCarregando;
import com.example.ecociente.ui.ChipEco;
import com.example.ecociente.ui.IndicadorCarregamento;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.Motion;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.io.File;
import java.util.List;

// Registro de descarte do morador: foto tirada na hora, tipo de resíduo e envio para a API.
public class RegistroDescarteActivity extends AppCompatActivity {

    // A câmera devolve a foto já pronta; o instante real da captura fica poucos segundos antes.
    private static final long FOLGA_DA_CAPTURA_MS = 3_000L;

    private ImageView previaFoto;
    private LinearLayout areaCategorias;
    private MaterialButton botaoEnviar;

    private Uri uriFoto;
    private long capturadaEmMs;
    private boolean fotoTirada;
    private boolean enviando;

    private final ActivityResultLauncher<Uri> camera =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    tirou -> {
                        if (!tirou) {
                            return;
                        }

                        fotoTirada = true;
                        capturadaEmMs = System.currentTimeMillis() - FOLGA_DA_CAPTURA_MS;

                        previaFoto.setPadding(0, 0, 0, 0);
                        previaFoto.setScaleType(ImageView.ScaleType.CENTER_CROP);

                        Glide.with(this).load(uriFoto).into(previaFoto);
                    });

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        JanelaEdgeToEdge.aplicar(this);

        setContentView(R.layout.activity_registro_descarte);

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizRegistroDescarte));

        Motion.entrar(
                this,
                R.id.tituloRegistroDescarte,
                R.id.subtituloRegistroDescarte,
                R.id.cartaoFotoRegistro,
                R.id.areaCategoriasRegistro);

        findViewById(R.id.botaoVoltarRegistro).setOnClickListener(view -> finish());

        previaFoto = findViewById(R.id.previaFotoRegistro);
        areaCategorias = findViewById(R.id.areaCategoriasRegistro);
        botaoEnviar = findViewById(R.id.botaoEnviarRegistro);

        View tirarFoto = findViewById(R.id.botaoTirarFotoRegistro);
        tirarFoto.setOnClickListener(view -> abrirCamera());
        Motion.pressFeedback(tirarFoto);

        botaoEnviar.setOnClickListener(view -> enviar());

        // O condomínio vem do perfil da API; se ainda não chegou, busca já para o envio.
        new PerfilApiRepository().atualizarSeNecessario(this);

        carregarCategorias();
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

        File arquivo = new File(pasta, "descarte_" + System.currentTimeMillis() + ".jpg");

        uriFoto = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", arquivo);

        try {
            camera.launch(uriFoto);

        } catch (ActivityNotFoundException erro) {
            AvisoFlutuante.mostrar(this, R.string.perfil_foto_erro_camera);
        }
    }

    private void carregarCategorias() {

        areaCategorias.removeAllViews();

        IndicadorCarregamento.mostrar(areaCategorias, R.string.registro_categorias_carregando);

        new PostagemRepository()
                .categorias(
                        this,
                        categorias -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            areaCategorias.removeAllViews();

                            if (categorias == null || categorias.isEmpty()) {
                                mostrarErroDeCategorias();
                                return;
                            }

                            mostrarCategorias(categorias);
                        });
    }

    private void mostrarErroDeCategorias() {

        TextView mensagem = new TextView(this);

        mensagem.setText(R.string.registro_categorias_erro);
        mensagem.setTextColor(getColor(R.color.cinza_texto_auxiliar));
        mensagem.setTextSize(14);
        mensagem.setPadding(0, 16, 0, 16);

        areaCategorias.addView(mensagem);

        MaterialButton tentar = (MaterialButton) getLayoutInflater().inflate(R.layout.botao_dialogo, areaCategorias, false);

        tentar.setText(R.string.tentar_novamente);
        tentar.setOnClickListener(view -> carregarCategorias());

        areaCategorias.addView(tentar);
    }

    private void mostrarCategorias(List<ItemLista> categorias) {

        ChipGroup grupo = new ChipGroup(this);

        grupo.setSingleSelection(true);
        grupo.setSelectionRequired(false);
        grupo.setChipSpacingHorizontal(Math.round(8 * getResources().getDisplayMetrics().density));

        for (ItemLista categoria : categorias) {
            Chip chip = ChipEco.criar(this, categoria.titulo, false);

            chip.setTag(categoria.chave);
            chip.setId(View.generateViewId());

            grupo.addView(chip);
        }

        grupo.setId(R.id.grupoCategoriasRegistro);

        areaCategorias.addView(grupo);
    }

    // Id da categoria marcada; 0 se nenhuma.
    private int categoriaEscolhida() {

        ChipGroup grupo = findViewById(R.id.grupoCategoriasRegistro);

        if (grupo == null) {
            return 0;
        }

        for (int i = 0; i < grupo.getChildCount(); i++) {
            Chip chip = (Chip) grupo.getChildAt(i);

            if (chip.isChecked() && chip.getTag() != null) {
                return Integer.parseInt((String) chip.getTag());
            }
        }

        return 0;
    }

    private void enviar() {

        if (enviando) {
            return;
        }

        if (!fotoTirada || uriFoto == null) {
            AvisoFlutuante.mostrar(this, R.string.registro_sem_foto);
            return;
        }

        int categoriaId = categoriaEscolhida();

        if (categoriaId <= 0) {
            AvisoFlutuante.mostrar(this, R.string.registro_sem_categoria);
            return;
        }

        int condominioId = PerfilApiRepository.condominioId(this);

        if (condominioId <= 0) {
            AvisoFlutuante.mostrar(this, R.string.registro_sem_condominio);
            return;
        }

        enviando = true;

        BotaoCarregando.definir(botaoEnviar, true, R.string.registro_enviar, R.string.registro_enviando);

        new PostagemRepository()
                .registrar(
                        this,
                        uriFoto,
                        condominioId,
                        categoriaId,
                        capturadaEmMs,
                        resultado -> {
                            enviando = false;

                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            BotaoCarregando.definir(botaoEnviar, false, R.string.registro_enviar, R.string.registro_enviando);

                            if (!resultado.isSucesso()) {
                                AvisoFlutuante.mostrar(this, resultado.getMensagemErro());
                                return;
                            }

                            setResult(RESULT_OK);
                            finish();
                        });
    }
}
