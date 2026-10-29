package com.example.ecociente.views;

import com.example.ecociente.ui.Motion;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecociente.R;
import com.example.ecociente.ui.AvisoFlutuante;
import com.example.ecociente.model.SindicoDadosCondominio;
import com.example.ecociente.model.SindicoItemUnidade;
import com.example.ecociente.viewmodels.PerfilViewModel;
import com.example.ecociente.viewmodels.SindicoUnidadesViewModel;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.Navegacao;
import com.example.ecociente.repository.AtualizacaoExternaRepository;
import com.example.ecociente.repository.PerfilApiRepository;
import java.text.NumberFormat;
import java.util.Locale;
import com.example.ecociente.model.SindicoPosicaoRanking;
import com.example.ecociente.repository.RankingExternoRepository;

/** Aba do condomínio: dados e torres vêm da API (o endereço volta a ela); áreas ficam no aparelho. */
public final class SindicoUnidadesActivity extends AppCompatActivity {
    private static final String ESTADO_TIPO = "tipoResidencialSelecionado";
    private SindicoUnidadesViewModel viewModel;

    private final ActivityResultLauncher<String> escolherFoto = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && viewModel != null) {
                    viewModel.salvarFotoUri(uri.toString());
                    mostrarFoto();
                    enviarFotoDoCondominio(uri);
                }
            });

    private boolean residencial;
    private EditText nome;
    private EditText endereco;
    private EditText numero;
    private EditText complemento;
    private EditText cidade;
    private EditText estado;
    private EditText cep;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        JanelaEdgeToEdge.aplicar(this);
        setContentView(R.layout.activity_sindico_unidades);

        Motion.entrar(
                this,
                R.id.tituloSindicoUnidades,
                R.id.fotoSindicoUnidades,
                R.id.salvarDadosCondominioSindico,
                R.id.listaBlocosSindico);

        View raiz = findViewById(R.id.raizSindicoUnidades);
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (view, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(barras.left, barras.top, barras.right,
                    Math.max(barras.bottom, teclado.bottom));
            findViewById(R.id.barraInferiorSindico).setVisibility(
                    teclado.bottom > barras.bottom ? View.GONE : View.VISIBLE);
            return insets;
        });
        ViewCompat.requestApplyInsets(raiz);

        viewModel = new ViewModelProvider(this).get(SindicoUnidadesViewModel.class);
        carregarFotoSalva();
        nome = findViewById(R.id.nomeCondominioSindico);
        endereco = findViewById(R.id.enderecoCondominioSindico);
        numero = findViewById(R.id.numeroCondominioSindico);
        complemento = findViewById(R.id.complementoCondominioSindico);
        cidade = findViewById(R.id.cidadeCondominioSindico);
        estado = findViewById(R.id.estadoCondominioSindico);
        cep = findViewById(R.id.cepCondominioSindico);
        preencherDados();
        residencial = estadoSalvo == null ? viewModel.getDados().isResidencial()
                : estadoSalvo.getBoolean(ESTADO_TIPO, viewModel.getDados().isResidencial());
        atualizarTipo();
        carregarDadosDaApi(estadoSalvo == null);
        mostrarFoto();
        mostrarListas();
        carregarMoradores();

        findViewById(R.id.tipoResidencialSindico).setOnClickListener(view -> {
            residencial = true;
            atualizarTipo();
        });
        findViewById(R.id.tipoComercialSindico).setOnClickListener(view -> {
            residencial = false;
            atualizarTipo();
        });
        findViewById(R.id.alterarFotoSindicoUnidades).setOnClickListener(
                view -> escolherFoto.launch("image/*"));
        findViewById(R.id.cameraSindicoUnidades).setOnClickListener(
                view -> escolherFoto.launch("image/*"));
        findViewById(R.id.salvarDadosCondominioSindico).setOnClickListener(
                view -> salvarDados(raiz));

        BarraNavegacaoView barra = findViewById(R.id.barraInferiorSindico);
        barra.configurar(ItensBarra.sindico());
        Navegacao.marcarNaBarra(this, barra, ItensBarra.QUARTO);
        barra.setOnAssistenteClickListener(
                view -> startActivity(new Intent(this, ChatActivity.class)));
        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDoSindico(this, indice, ItensBarra.QUARTO));
    }

    private void preencherDados() {
        SindicoDadosCondominio dados = viewModel.getDados();
        nome.setText(dados.getNome());
        endereco.setText(dados.getEndereco());
        numero.setText(dados.getNumero());
        complemento.setText(dados.getComplemento());
        cidade.setText(dados.getCidade());
        estado.setText(dados.getEstado());
        cep.setText(dados.getCep());
    }

    // O condomínio vem de /api/usuarios/me. O que o síndico já digitou enquanto a API respondia
    // (ela pode levar minutos para acordar) não é apagado: só os campos vazios são preenchidos.
    private void carregarDadosDaApi(boolean primeiraAbertura) {
        new PerfilApiRepository().atualizarSeNecessario(this, () -> {
            SindicoDadosCondominio api = PerfilApiRepository.dadosDoCondominio(this);

            if (api == null || isFinishing() || isDestroyed()) {
                return;
            }

            mostrarListas();
            carregarMoradores();
            mostrarFotoDaApi();
            viewModel.aplicarDadosDaApi(api);
            preencherSeVazio(nome, api.getNome());
            preencherSeVazio(endereco, api.getEndereco());
            preencherSeVazio(numero, api.getNumero());
            preencherSeVazio(complemento, api.getComplemento());
            preencherSeVazio(cidade, api.getCidade());
            preencherSeVazio(estado, api.getEstado());
            preencherSeVazio(cep, api.getCep());

            if (primeiraAbertura) {
                residencial = api.isResidencial();
                atualizarTipo();
            }
        });
    }

    private static void preencherSeVazio(EditText campo, String valor) {
        if (campo.getText().toString().trim().isEmpty()) {
            campo.setText(valor);
        }
    }

    private void atualizarTipo() {
        TextView botaoResidencial = findViewById(R.id.tipoResidencialSindico);
        TextView botaoComercial = findViewById(R.id.tipoComercialSindico);
        botaoResidencial.setBackgroundResource(residencial
                ? R.drawable.sindico_unidades_segmento_ativo
                : android.R.color.transparent);
        botaoComercial.setBackgroundResource(residencial
                ? android.R.color.transparent
                : R.drawable.sindico_unidades_segmento_ativo);
        botaoResidencial.setTextColor(getColor(residencial
                ? R.color.branco : R.color.verde_escuro_principal));
        botaoComercial.setTextColor(getColor(residencial
                ? R.color.verde_escuro_principal : R.color.branco));
        botaoResidencial.setContentDescription(getString(residencial
                ? R.string.sindico_residencial_selecionado : R.string.sindico_selecionar_residencial));
        botaoComercial.setContentDescription(getString(residencial
                ? R.string.sindico_selecionar_comercial_ou_industrial : R.string.sindico_comercial_ou_industrial_selecionad));
    }

    // Envia ao Cloudinary e guarda a URL no usuário do síndico; enquanto isso a foto escolhida
    // já aparece, e se o envio falhar ela continua só neste aparelho.
    private void enviarFotoDoCondominio(Uri imagem) {
        PerfilViewModel perfil = new ViewModelProvider(this).get(PerfilViewModel.class);
        FirebaseUser usuario = perfil.usuarioAtual();

        if (usuario == null) {
            return;
        }

        perfil.atualizarFotoCondominio(getApplicationContext(), usuario.getUid(), imagem)
                .observe(this, resultado -> {
                    if (resultado == null) {
                        return;
                    }

                    if (resultado.isSucesso()) {
                        viewModel.salvarFotoUri(resultado.getUrl());
                        mostrarFoto();
                        AvisoFlutuante.mostrar(this, R.string.sindico_foto_condominio_salva);
                        new AtualizacaoExternaRepository().sincronizarCondominio(
                                getApplicationContext(), PerfilApiRepository.condominioId(this),
                                null, 0, resultado.getUrl(),
                                () -> AvisoFlutuante.mostrar(this, R.string.sindico_condominio_sincronizacao_falhou));
                    } else {
                        AvisoFlutuante.mostrar(this, resultado.getMensagemErro());
                    }
                });
    }

    // A foto que está no Postgres (via /me) vale mais do que a guardada só no aparelho.
    private void mostrarFotoDaApi() {
        String url = PerfilApiRepository.fotoDoCondominio(this);

        if (!url.isEmpty() && !url.equals(viewModel.getFotoUri())) {
            viewModel.salvarFotoUri(url);
            mostrarFoto();
        }
    }

    // Foto que o síndico já salvou: vem do documento dele quando a tela abre.
    private void carregarFotoSalva() {
        PerfilViewModel perfil = new ViewModelProvider(this).get(PerfilViewModel.class);
        FirebaseUser usuario = perfil.usuarioAtual();

        if (usuario == null) {
            return;
        }

        perfil.buscarPerfil(usuario.getUid()).observe(this, dados -> {
            if (dados != null && !dados.getFotoCondominioUrl().isEmpty()
                    && viewModel.getFotoUri() == null) {
                viewModel.salvarFotoUri(dados.getFotoCondominioUrl());
                mostrarFoto();
            }
        });
    }

    private void mostrarFoto() {
        ImageView foto = findViewById(R.id.fotoSindicoUnidades);
        String uri = viewModel.getFotoUri();
        if (uri == null) {
            foto.setImageResource(R.drawable.ic_predio);
            foto.setImageTintList(ColorStateList.valueOf(
                    getColor(R.color.verde_escuro_principal)));
            foto.setPadding(dp(17), dp(17), dp(17), dp(17));
            foto.setScaleType(ImageView.ScaleType.FIT_CENTER);
            foto.setContentDescription(getString(R.string.sindico_sem_foto_do_condominio_icone_ilust));
        } else {
            foto.setImageTintList(null);
            foto.setPadding(0, 0, 0, 0);
            foto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (uri.startsWith("http")) {
                com.bumptech.glide.Glide.with(foto).load(uri).into(foto);
            } else {
                foto.setImageURI(Uri.parse(uri));
            }
            foto.setContentDescription(getString(R.string.sindico_foto_escolhida_para_o_condominio));
        }
    }

    private void salvarDados(View raiz) {
        if (!validar(nome, getString(R.string.sindico_informe_o_nome_do_condominio))
                || !validar(endereco, getString(R.string.sindico_informe_o_endereco))
                || !validar(cidade, getString(R.string.sindico_informe_a_cidade))
                || !validar(estado, getString(R.string.sindico_informe_a_sigla_do_estado))
                || !validar(cep, getString(R.string.sindico_informe_o_cep))) return;
        String uf = estado.getText().toString().trim().toUpperCase(Locale.ROOT);
        if (!uf.matches("[A-Z]{2}")) {
            estado.setError(getString(R.string.sindico_use_duas_letras_como_sp));
            estado.requestFocus();
            return;
        }
        String codigoPostal = cep.getText().toString().trim();
        if (!codigoPostal.matches("\\d{5}-?\\d{3}")) {
            cep.setError(getString(R.string.sindico_use_o_formato_01234_567));
            cep.requestFocus();
            return;
        }
        String valorNumero = numero.getText().toString().trim();
        viewModel.salvarDados(new SindicoDadosCondominio(residencial,
                nome.getText().toString().trim(), endereco.getText().toString().trim(),
                valorNumero, complemento.getText().toString().trim(),
                cidade.getText().toString().trim(), uf, codigoPostal));
        AvisoFlutuante.mostrar(this, R.string.sindico_demo_dados_salvos);

        // Nome, tipo e endereço (CEP, número e complemento) vão para a API.
        new AtualizacaoExternaRepository().sincronizarCondominio(
                getApplicationContext(), PerfilApiRepository.condominioId(this),
                nome.getText().toString(), residencial ? 1 : 2, null,
                () -> AvisoFlutuante.mostrar(this, R.string.sindico_condominio_sincronizacao_falhou));
        new AtualizacaoExternaRepository().sincronizarEnderecoDoCondominio(
                getApplicationContext(), PerfilApiRepository.condominioId(this), codigoPostal,
                valorNumero, complemento.getText().toString(),
                () -> AvisoFlutuante.mostrar(this, R.string.sindico_condominio_sincronizacao_falhou));
    }

    private boolean validar(EditText campo, String erro) {
        if (!campo.getText().toString().trim().isEmpty()) return true;
        campo.setError(erro);
        campo.requestFocus();
        return false;
    }

    private void mostrarListas() {
        mostrarTorres(torresDoCondominio());
    }

    private List<SindicoItemUnidade> torresDoCondominio() {
        return PerfilApiRepository.torresDoCondominio(this);
    }

    // As torres vêm da API e só podem ser lidas aqui.
    private void mostrarTorres(List<SindicoItemUnidade> torres) {
        LinearLayout lista = findViewById(R.id.listaBlocosSindico);
        lista.removeAllViews();
        ((TextView) findViewById(R.id.contagemBlocosSindico)).setText(getResources().getQuantityString(R.plurals.sindico_itens_cadastrados, torres.size(), torres.size()));
        if (torres.isEmpty()) {
            TextView vazio = new TextView(this);
            vazio.setText(R.string.sindico_nenhuma_torre);
            vazio.setTextColor(getColor(R.color.cinza_texto_home));
            vazio.setTextSize(12);
            vazio.setPadding(dp(4), dp(14), dp(4), dp(14));
            lista.addView(vazio);
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < torres.size(); i++) {
            SindicoItemUnidade torre = torres.get(i);
            View linha = inflater.inflate(R.layout.item_sindico_unidades, lista, false);
            ImageView icone = linha.findViewById(R.id.iconeItemUnidades);
            icone.setImageResource(R.drawable.ic_predio);
            icone.setImageTintList(ColorStateList.valueOf(
                    getColor(R.color.verde_escuro_principal)));
            ((TextView) linha.findViewById(R.id.nomeItemUnidades)).setText(torre.getNome());
            ((TextView) linha.findViewById(R.id.detalheItemUnidades)).setText(torre.getDetalhe());
            linha.findViewById(R.id.editarItemUnidades).setVisibility(View.GONE);
            linha.findViewById(R.id.excluirItemUnidades).setVisibility(View.GONE);
            lista.addView(linha);
            if (i < torres.size() - 1) {
                View divisor = new View(this);
                divisor.setBackgroundColor(getColor(R.color.cinza_borda_clara_home));
                lista.addView(divisor, new LinearLayout.LayoutParams(-1, dp(1)));
            }
        }
    }

    // A API não lista todos os moradores: vem o ranking, então só aparece quem pontuou no ciclo.
    private void carregarMoradores() {
        new RankingExternoRepository().listarMoradoresNoRanking(
                this, PerfilApiRepository.condominioId(this), 100, moradores -> {
                    if (isFinishing() || isDestroyed()) return;
                    mostrarMoradores(moradores);
                });
    }

    private void mostrarMoradores(List<SindicoPosicaoRanking> moradores) {
        LinearLayout lista = findViewById(R.id.listaMoradoresSindico);
        lista.removeAllViews();

        if (moradores == null || moradores.isEmpty()) {
            ((TextView) findViewById(R.id.contagemMoradoresSindico)).setText("");
            TextView vazio = new TextView(this);
            vazio.setText(moradores == null ? R.string.ranking_erro : R.string.sindico_moradores_vazio);
            vazio.setTextColor(getColor(R.color.cinza_texto_home));
            vazio.setTextSize(12);
            vazio.setPadding(dp(4), dp(14), dp(4), dp(14));
            lista.addView(vazio);
            return;
        }

        ((TextView) findViewById(R.id.contagemMoradoresSindico)).setText(
                getResources().getQuantityString(R.plurals.sindico_itens_cadastrados, moradores.size(), moradores.size()));

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < moradores.size(); i++) {
            SindicoPosicaoRanking morador = moradores.get(i);
            View linha = inflater.inflate(R.layout.item_sindico_unidades, lista, false);
            ImageView icone = linha.findViewById(R.id.iconeItemUnidades);
            icone.setImageResource(R.drawable.ic_usuario);
            icone.setImageTintList(ColorStateList.valueOf(getColor(R.color.verde_escuro_principal)));
            ((TextView) linha.findViewById(R.id.nomeItemUnidades)).setText(morador.getNome());
            ((TextView) linha.findViewById(R.id.detalheItemUnidades)).setText(
                    getString(R.string.sindico_morador_pontos,
                            NumberFormat.getIntegerInstance(new Locale("pt", "BR")).format(morador.getPontos())));
            linha.findViewById(R.id.editarItemUnidades).setVisibility(View.GONE);
            linha.findViewById(R.id.excluirItemUnidades).setVisibility(View.GONE);
            lista.addView(linha);
            if (i < moradores.size() - 1) {
                View divisor = new View(this);
                divisor.setBackgroundColor(getColor(R.color.cinza_borda_clara_home));
                lista.addView(divisor, new LinearLayout.LayoutParams(-1, dp(1)));
            }
        }
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        estado.putBoolean(ESTADO_TIPO, residencial);
        super.onSaveInstanceState(estado);
    }
}
