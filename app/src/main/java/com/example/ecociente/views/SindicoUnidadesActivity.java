package com.example.ecociente.views;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.example.ecociente.ui.DialogoEco;
import com.example.ecociente.model.SindicoDadosCondominio;
import com.example.ecociente.model.SindicoItemUnidade;
import com.example.ecociente.viewmodels.SindicoUnidadesViewModel;

import java.util.List;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.Navegacao;

/** Tela demonstrativa da aba Unidades; não consulta nem grava em banco. */
public final class SindicoUnidadesActivity extends AppCompatActivity {
    private static final String ESTADO_TIPO = "tipoResidencialSelecionado";
    private SindicoUnidadesViewModel viewModel;

    private final ActivityResultLauncher<String> escolherFoto = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && viewModel != null) {
                    viewModel.salvarFotoUri(uri.toString());
                    mostrarFoto();
                    Toast.makeText(getApplicationContext(), R.string.sindico_foto_alterada_somente_nesta_demons, Toast.LENGTH_LONG).show();
                }
            });

    private boolean residencial;
    private EditText nome;
    private EditText endereco;
    private EditText cidade;
    private EditText estado;
    private EditText cep;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        JanelaEdgeToEdge.aplicar(this);
        setContentView(R.layout.activity_sindico_unidades);

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
        nome = findViewById(R.id.nomeCondominioSindico);
        endereco = findViewById(R.id.enderecoCondominioSindico);
        cidade = findViewById(R.id.cidadeCondominioSindico);
        estado = findViewById(R.id.estadoCondominioSindico);
        cep = findViewById(R.id.cepCondominioSindico);
        preencherDados();
        residencial = estadoSalvo == null ? viewModel.getDados().isResidencial()
                : estadoSalvo.getBoolean(ESTADO_TIPO, viewModel.getDados().isResidencial());
        atualizarTipo();
        mostrarFoto();
        mostrarListas();

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
        findViewById(R.id.adicionarBlocoSindico).setOnClickListener(view -> {
            List<SindicoItemUnidade> blocos = viewModel.getBlocos();
            if (blocos.isEmpty()) abrirFormulario(true, null);
            else abrirBloco(blocos.get(0));
        });
        findViewById(R.id.adicionarAreaSindico).setOnClickListener(
                view -> abrirFormulario(false, null));

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
        cidade.setText(dados.getCidade());
        estado.setText(dados.getEstado());
        cep.setText(dados.getCep());
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

    private void mostrarFoto() {
        ImageView foto = findViewById(R.id.fotoSindicoUnidades);
        String uri = viewModel.getFotoUri();
        if (uri == null) {
            foto.setImageResource(R.drawable.sindico_nav_unidades);
            foto.setImageTintList(ColorStateList.valueOf(
                    getColor(R.color.verde_escuro_principal)));
            foto.setPadding(dp(17), dp(17), dp(17), dp(17));
            foto.setScaleType(ImageView.ScaleType.FIT_CENTER);
            foto.setContentDescription(getString(R.string.sindico_sem_foto_do_condominio_icone_ilust));
        } else {
            foto.setImageTintList(null);
            foto.setPadding(0, 0, 0, 0);
            foto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            foto.setImageURI(Uri.parse(uri));
            foto.setContentDescription(getString(R.string.sindico_foto_escolhida_para_o_condominio));
        }
    }

    private void salvarDados(View raiz) {
        if (!validar(nome, getString(R.string.sindico_informe_o_nome_do_condominio))
                || !validar(endereco, getString(R.string.sindico_informe_o_endereco))
                || !validar(cidade, getString(R.string.sindico_informe_a_cidade))
                || !validar(estado, getString(R.string.sindico_informe_a_sigla_do_estado))
                || !validar(cep, getString(R.string.sindico_informe_o_cep))) return;
        String uf = estado.getText().toString().trim().toUpperCase();
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
        viewModel.salvarDados(new SindicoDadosCondominio(residencial,
                nome.getText().toString().trim(), endereco.getText().toString().trim(),
                cidade.getText().toString().trim(), uf, codigoPostal));
        Toast.makeText(getApplicationContext(), R.string.sindico_dados_salvos_somente_nesta_demonst, Toast.LENGTH_LONG).show();
    }

    private boolean validar(EditText campo, String erro) {
        if (!campo.getText().toString().trim().isEmpty()) return true;
        campo.setError(erro);
        campo.requestFocus();
        return false;
    }

    private void mostrarListas() {
        mostrarItens(true, viewModel.getBlocos(), R.id.listaBlocosSindico,
                R.id.contagemBlocosSindico);
        mostrarItens(false, viewModel.getAreas(), R.id.listaAreasSindico,
                R.id.contagemAreasSindico);
    }

    private void mostrarItens(boolean bloco, List<SindicoItemUnidade> itens,
                              int idLista, int idContagem) {
        LinearLayout lista = findViewById(idLista);
        lista.removeAllViews();
        ((TextView) findViewById(idContagem)).setText(itens.size() + " itens cadastrados");
        if (itens.isEmpty()) {
            TextView vazio = new TextView(this);
            vazio.setText(bloco ? R.string.sindico_nenhum_bloco_adicione_o_primeiro_a : R.string.sindico_nenhuma_area_de_descarte_adicione);
            vazio.setTextColor(getColor(R.color.cinza_texto_home));
            vazio.setTextSize(12);
            vazio.setPadding(dp(4), dp(14), dp(4), dp(14));
            lista.addView(vazio);
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < itens.size(); i++) {
            SindicoItemUnidade item = itens.get(i);
            View linha = inflater.inflate(R.layout.item_sindico_unidades, lista, false);
            ImageView icone = linha.findViewById(R.id.iconeItemUnidades);
            icone.setImageResource(bloco ? R.drawable.sindico_building
                    : R.drawable.ic_localizacao_chat);
            icone.setImageTintList(ColorStateList.valueOf(
                    getColor(R.color.verde_escuro_principal)));
            ((TextView) linha.findViewById(R.id.nomeItemUnidades)).setText(item.getNome());
            ((TextView) linha.findViewById(R.id.detalheItemUnidades))
                    .setText(item.getDetalhe());
            if (bloco) {
                linha.setClickable(true);
                linha.setFocusable(true);
                linha.setContentDescription(getString(R.string.sindico_abrir_unidades_bloco, item.getNome()));
                linha.setOnClickListener(view -> abrirBloco(item));
            }
            ImageButton editar = linha.findViewById(R.id.editarItemUnidades);
            editar.setContentDescription(getString(R.string.sindico_editar_item, item.getNome()));
            editar.setOnClickListener(view -> abrirFormulario(bloco, item));
            ImageButton excluir = linha.findViewById(R.id.excluirItemUnidades);
            excluir.setContentDescription(getString(R.string.sindico_excluir_item, item.getNome()));
            excluir.setOnClickListener(view -> confirmarExclusao(bloco, item));
            lista.addView(linha);
            if (i < itens.size() - 1) {
                View divisor = new View(this);
                divisor.setBackgroundColor(getColor(R.color.cinza_borda_clara_home));
                lista.addView(divisor, new LinearLayout.LayoutParams(-1, dp(1)));
            }
        }
    }

    private void abrirFormulario(boolean bloco, SindicoItemUnidade item) {
        LinearLayout campos = new LinearLayout(this);
        campos.setOrientation(LinearLayout.VERTICAL);
        campos.setPadding(0, dp(4), 0, 0);
        EditText campoNome = new EditText(this);
        campoNome.setSingleLine(true);
        campoNome.setHint(bloco ? R.string.sindico_nome_do_bloco_ou_torre : R.string.sindico_nome_da_area_de_descarte);
        campoNome.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        campoNome.setText(item == null ? "" : item.getNome());
        campos.addView(campoNome, new LinearLayout.LayoutParams(-1, dp(54)));

        EditText campoDetalhe = new EditText(this);
        campoDetalhe.setSingleLine(true);
        campoDetalhe.setHint(bloco ? R.string.sindico_quantidade_de_unidades : R.string.localizacao);
        campoDetalhe.setInputType(bloco ? InputType.TYPE_CLASS_NUMBER
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        if (item != null) {
            campoDetalhe.setText(bloco
                    ? item.getDetalhe().replaceAll("\\D", "") : item.getDetalhe());
        }
        campos.addView(campoDetalhe, new LinearLayout.LayoutParams(-1, dp(54)));

        int titulo = item == null ? (bloco ? R.string.sindico_adicionar_bloco_torre : R.string.sindico_adicionar_area)
                : (bloco ? R.string.sindico_editar_bloco_torre : R.string.sindico_editar_area);
        new DialogoEco.Builder(this)
                .titulo(titulo)
                .conteudo(campos)
                .botaoValidado(R.string.perfil_salvar, DialogoEco.Estilo.PREENCHIDO, () -> {
                    String valorNome = campoNome.getText().toString().trim();
                    String valorDetalhe = campoDetalhe.getText().toString().trim();
                    if (valorNome.isEmpty()) {
                        campoNome.setError(getString(R.string.sindico_informe_o_nome));
                        campoNome.requestFocus();
                        return false;
                    }
                    if (valorDetalhe.isEmpty()) {
                        campoDetalhe.setError(getString(bloco ? R.string.sindico_informe_a_quantidade
                                : R.string.sindico_informe_a_localizacao));
                        campoDetalhe.requestFocus();
                        return false;
                    }
                    int id = item == null ? -1 : item.getId();
                    if (bloco) {
                        int quantidade;
                        try {
                            quantidade = Integer.parseInt(valorDetalhe);
                        } catch (NumberFormatException excecao) {
                            campoDetalhe.setError(getString(R.string.sindico_informe_uma_quantidade_valida));
                            return false;
                        }
                        if (quantidade < 1) {
                            campoDetalhe.setError(getString(R.string.sindico_a_quantidade_deve_ser_maior_que_ze));
                            return false;
                        }
                        viewModel.salvarBloco(id, valorNome, quantidade);
                    } else {
                        viewModel.salvarArea(id, valorNome, valorDetalhe);
                    }
                    mostrarListas();
                    Toast.makeText(getApplicationContext(), R.string.sindico_alteracao_salva_somente_nesta_demo, Toast.LENGTH_LONG).show();
                    return true;
                })
                .botao(R.string.perfil_cancelar, DialogoEco.Estilo.CONTORNO, null)
                .mostrar();
    }

    private void confirmarExclusao(boolean bloco, SindicoItemUnidade item) {
        new DialogoEco.Builder(this)
                .titulo(getString(R.string.sindico_excluir_titulo, item.getNome()))
                .mensagem(R.string.sindico_a_exclusao_afeta_apenas_os_dados_l)
                .botao(R.string.perfil_cancelar, DialogoEco.Estilo.PREENCHIDO, null)
                .botao(R.string.perfil_excluir_confirmar, DialogoEco.Estilo.PERIGO, () -> {
                    if (bloco) viewModel.excluirBloco(item.getId());
                    else viewModel.excluirArea(item.getId());
                    mostrarListas();
                    Toast.makeText(getApplicationContext(), R.string.sindico_item_removido_desta_demonstracao, Toast.LENGTH_LONG).show();
                })
                .mostrar();
    }

    private void navegar(Class<?> destino) {
        Intent intent = new Intent(this, destino);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void abrirBloco(SindicoItemUnidade bloco) {
        Intent intent = new Intent(this, SindicoBlocoActivity.class);
        intent.putExtra(SindicoBlocoActivity.EXTRA_BLOCO_ID, bloco.getId());
        startActivity(intent);
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
