package com.example.ecociente.views;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecociente.R;
import com.example.ecociente.model.SindicoDadosCondominio;
import com.example.ecociente.model.SindicoItemUnidade;
import com.example.ecociente.viewmodels.SindicoUnidadesViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/** Tela demonstrativa da aba Unidades; não consulta nem grava em banco. */
public final class SindicoUnidadesActivity extends AppCompatActivity {
    private static final String ESTADO_TIPO = "tipoResidencialSelecionado";

    private final ActivityResultLauncher<String> escolherFoto = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && viewModel != null) {
                    viewModel.salvarFotoUri(uri.toString());
                    mostrarFoto();
                    Snackbar.make(findViewById(R.id.raizSindicoUnidades),
                            "Foto alterada somente nesta demonstração.",
                            Snackbar.LENGTH_LONG).show();
                }
            });

    private SindicoUnidadesViewModel viewModel;
    private boolean residencial;
    private EditText nome;
    private EditText endereco;
    private EditText cidade;
    private EditText estado;
    private EditText cep;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
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
        findViewById(R.id.adicionarBlocoSindico).setOnClickListener(
                view -> abrirFormulario(true, null));
        findViewById(R.id.adicionarAreaSindico).setOnClickListener(
                view -> abrirFormulario(false, null));
        findViewById(R.id.voltarSindicoUnidades).setOnClickListener(view -> finish());

        SindicoBottomBar barra = findViewById(R.id.barraInferiorSindico);
        barra.selecionar(SindicoBottomBar.Aba.UNIDADES);
        barra.aoTocarUnidades(null);
        barra.aoTocarHome(view -> navegar(SindicoHomeActivity.class));
        barra.aoTocarSolicitacao(view -> navegar(SindicoCooperativasActivity.class));
        barra.aoTocarHistorico(view -> navegar(SindicoHistoricoActivity.class));
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
        botaoResidencial.setContentDescription(residencial
                ? "Residencial, selecionado" : "Selecionar residencial");
        botaoComercial.setContentDescription(residencial
                ? "Selecionar comercial ou industrial" : "Comercial ou industrial, selecionado");
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
            foto.setContentDescription("Sem foto do condomínio; ícone ilustrativo");
        } else {
            foto.setImageTintList(null);
            foto.setPadding(0, 0, 0, 0);
            foto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            foto.setImageURI(Uri.parse(uri));
            foto.setContentDescription("Foto escolhida para o condomínio");
        }
    }

    private void salvarDados(View raiz) {
        if (!validar(nome, "Informe o nome do condomínio")
                || !validar(endereco, "Informe o endereço")
                || !validar(cidade, "Informe a cidade")
                || !validar(estado, "Informe a sigla do estado")
                || !validar(cep, "Informe o CEP")) return;
        String uf = estado.getText().toString().trim().toUpperCase();
        if (!uf.matches("[A-Z]{2}")) {
            estado.setError("Use duas letras, como SP");
            estado.requestFocus();
            return;
        }
        String codigoPostal = cep.getText().toString().trim();
        if (!codigoPostal.matches("\\d{5}-?\\d{3}")) {
            cep.setError("Use o formato 01234-567");
            cep.requestFocus();
            return;
        }
        viewModel.salvarDados(new SindicoDadosCondominio(residencial,
                nome.getText().toString().trim(), endereco.getText().toString().trim(),
                cidade.getText().toString().trim(), uf, codigoPostal));
        Snackbar.make(raiz, "Dados salvos somente nesta demonstração.",
                Snackbar.LENGTH_LONG).show();
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
            vazio.setText(bloco ? "Nenhum bloco. Adicione o primeiro abaixo."
                    : "Nenhuma área de descarte. Adicione a primeira abaixo.");
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
            ImageButton editar = linha.findViewById(R.id.editarItemUnidades);
            editar.setContentDescription("Editar " + item.getNome());
            editar.setOnClickListener(view -> abrirFormulario(bloco, item));
            ImageButton excluir = linha.findViewById(R.id.excluirItemUnidades);
            excluir.setContentDescription("Excluir " + item.getNome());
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
        campos.setPadding(dp(20), dp(4), dp(20), 0);
        EditText campoNome = new EditText(this);
        campoNome.setSingleLine(true);
        campoNome.setHint(bloco ? "Nome do bloco ou torre" : "Nome da área de descarte");
        campoNome.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        campoNome.setText(item == null ? "" : item.getNome());
        campos.addView(campoNome, new LinearLayout.LayoutParams(-1, dp(54)));

        EditText campoDetalhe = new EditText(this);
        campoDetalhe.setSingleLine(true);
        campoDetalhe.setHint(bloco ? "Quantidade de unidades" : "Localização");
        campoDetalhe.setInputType(bloco ? InputType.TYPE_CLASS_NUMBER
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        if (item != null) {
            campoDetalhe.setText(bloco
                    ? item.getDetalhe().replaceAll("\\D", "") : item.getDetalhe());
        }
        campos.addView(campoDetalhe, new LinearLayout.LayoutParams(-1, dp(54)));

        String titulo = item == null ? (bloco ? "Adicionar bloco/torre" : "Adicionar área")
                : (bloco ? "Editar bloco/torre" : "Editar área");
        AlertDialog dialogo = new MaterialAlertDialogBuilder(this)
                .setTitle(titulo)
                .setView(campos)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", null)
                .create();
        dialogo.setOnShowListener(ignorado -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> {
                    String valorNome = campoNome.getText().toString().trim();
                    String valorDetalhe = campoDetalhe.getText().toString().trim();
                    if (valorNome.isEmpty()) {
                        campoNome.setError("Informe o nome");
                        campoNome.requestFocus();
                        return;
                    }
                    if (valorDetalhe.isEmpty()) {
                        campoDetalhe.setError(bloco ? "Informe a quantidade"
                                : "Informe a localização");
                        campoDetalhe.requestFocus();
                        return;
                    }
                    int id = item == null ? -1 : item.getId();
                    if (bloco) {
                        int quantidade;
                        try {
                            quantidade = Integer.parseInt(valorDetalhe);
                        } catch (NumberFormatException excecao) {
                            campoDetalhe.setError("Informe uma quantidade válida");
                            return;
                        }
                        if (quantidade < 1) {
                            campoDetalhe.setError("A quantidade deve ser maior que zero");
                            return;
                        }
                        viewModel.salvarBloco(id, valorNome, quantidade);
                    } else {
                        viewModel.salvarArea(id, valorNome, valorDetalhe);
                    }
                    mostrarListas();
                    Snackbar.make(findViewById(R.id.raizSindicoUnidades),
                            "Alteração salva somente nesta demonstração.",
                            Snackbar.LENGTH_LONG).show();
                    dialogo.dismiss();
                }));
        dialogo.show();
    }

    private void confirmarExclusao(boolean bloco, SindicoItemUnidade item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Excluir " + item.getNome() + "?")
                .setMessage("A exclusão afeta apenas os dados locais desta demonstração.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialogo, botao) -> {
                    if (bloco) viewModel.excluirBloco(item.getId());
                    else viewModel.excluirArea(item.getId());
                    mostrarListas();
                    Snackbar.make(findViewById(R.id.raizSindicoUnidades),
                            "Item removido desta demonstração.",
                            Snackbar.LENGTH_LONG).show();
                })
                .show();
    }

    private void navegar(Class<?> destino) {
        Intent intent = new Intent(this, destino);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
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
