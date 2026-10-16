package com.example.ecociente.views;

import com.example.ecociente.ui.Motion;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecociente.R;
import com.example.ecociente.ui.AvisoFlutuante;
import com.example.ecociente.ui.DialogoEco;
import com.example.ecociente.model.SindicoApartamento;
import com.example.ecociente.model.SindicoItemUnidade;
import com.example.ecociente.viewmodels.SindicoUnidadesViewModel;

import java.util.Locale;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.Navegacao;

/** Lista demonstrativa das unidades de um bloco, sem acesso a banco de dados. */
public final class SindicoBlocoActivity extends AppCompatActivity {
    public static final String EXTRA_BLOCO_ID = "blocoId";

    private SindicoUnidadesViewModel viewModel;
    private LinearLayout lista;
    private EditText busca;
    private int blocoId;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        JanelaEdgeToEdge.aplicar(this);
        setContentView(R.layout.activity_sindico_bloco);

        Motion.entrar(
                this,
                R.id.tituloSindicoBloco,
                R.id.buscaSindicoBloco,
                R.id.listaSindicoApartamentos);

        View raiz = findViewById(R.id.raizSindicoBloco);
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
        blocoId = getIntent().getIntExtra(EXTRA_BLOCO_ID, -1);
        SindicoItemUnidade bloco = viewModel.getBloco(blocoId);
        if (bloco == null) {
            finish();
            return;
        }
        ((TextView) findViewById(R.id.tituloSindicoBloco)).setText(bloco.getNome());
        lista = findViewById(R.id.listaSindicoApartamentos);
        busca = findViewById(R.id.buscaSindicoBloco);
        busca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence texto, int inicio, int tamanho,
                                                    int depois) { }
            @Override public void onTextChanged(CharSequence texto, int inicio, int antes,
                                                int quantidade) { mostrarApartamentos(); }
            @Override public void afterTextChanged(Editable texto) { }
        });
        findViewById(R.id.voltarSindicoBloco).setOnClickListener(view -> finish());
        findViewById(R.id.adicionarApartamentoSindico).setOnClickListener(
                view -> abrirFormulario(-1));
        getSupportFragmentManager().setFragmentResultListener(
                SindicoAdicionarUnidadeDialogFragment.RESULTADO_UNIDADE, this,
                (chave, resultado) -> {
                    mostrarApartamentos();
                    AvisoFlutuante.mostrar(this, R.string.sindico_unidade_salva_somente_nesta_demons);
                });

        BarraNavegacaoView barra = findViewById(R.id.barraInferiorSindico);
        barra.configurar(ItensBarra.sindico());
        Navegacao.marcarNaBarra(this, barra, ItensBarra.QUARTO);
        barra.setOnAssistenteClickListener(
                view -> startActivity(new Intent(this, ChatActivity.class)));
        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDoSindico(this, indice, ItensBarra.NENHUM));
        mostrarApartamentos();
    }

    private void mostrarApartamentos() {
        lista.removeAllViews();
        String filtro = busca.getText().toString().trim().toLowerCase(Locale.ROOT);
        LayoutInflater inflater = LayoutInflater.from(this);
        int encontrados = 0;
        for (SindicoApartamento apartamento : viewModel.getApartamentos(blocoId)) {
            if (!apartamento.getNumero().toLowerCase(Locale.ROOT).contains(filtro)
                    && !apartamento.getMorador().toLowerCase(Locale.ROOT).contains(filtro)) {
                continue;
            }
            encontrados++;
            View linha = inflater.inflate(R.layout.item_sindico_apartamento, lista, false);
            ((TextView) linha.findViewById(R.id.numeroApartamentoSindico))
                    .setText(apartamento.getNumero());
            ((TextView) linha.findViewById(R.id.moradorApartamentoSindico))
                    .setText(apartamento.isVinculado()
                            ? getString(R.string.sindico_morador_nome, apartamento.getMorador())
                            : getString(R.string.sindico_sem_responsavel_vinculado));
            TextView status = linha.findViewById(R.id.statusApartamentoSindico);
            status.setText(apartamento.isVinculado() ? R.string.sindico_vinculado : R.string.sindico_sem_vinculo);
            status.setBackgroundResource(apartamento.isVinculado()
                    ? R.drawable.sindico_unidade_vinculada
                    : R.drawable.sindico_unidade_sem_vinculo);
            status.setTextColor(getColor(apartamento.isVinculado()
                    ? R.color.verde_escuro_principal : R.color.cinza_texto_home));
            ImageButton editar = linha.findViewById(R.id.editarApartamentoSindico);
            editar.setContentDescription(getString(R.string.sindico_editar_item, apartamento.getNumero()));
            editar.setOnClickListener(view -> abrirFormulario(apartamento.getId()));
            ImageButton excluir = linha.findViewById(R.id.excluirApartamentoSindico);
            excluir.setContentDescription(getString(R.string.sindico_excluir_item, apartamento.getNumero()));
            excluir.setOnClickListener(view -> confirmarExclusao(apartamento));
            LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(-1, dp(64));
            parametros.bottomMargin = dp(10);
            lista.addView(linha, parametros);
        }
        if (encontrados == 0) {
            TextView vazio = new TextView(this);
            vazio.setText(filtro.isEmpty() ? R.string.sindico_nenhuma_unidade_cadastrada_neste_b : R.string.sindico_nenhuma_unidade_corresponde_a_busc);
            vazio.setTextColor(getColor(R.color.cinza_texto_home));
            vazio.setTextSize(14);
            vazio.setPadding(dp(8), dp(24), dp(8), dp(24));
            lista.addView(vazio);
        }
    }

    private void abrirFormulario(int apartamentoId) {
        if (getSupportFragmentManager().findFragmentByTag("formularioUnidadeSindico") == null) {
            SindicoAdicionarUnidadeDialogFragment.criar(blocoId, apartamentoId)
                    .show(getSupportFragmentManager(), "formularioUnidadeSindico");
        }
    }

    private void confirmarExclusao(SindicoApartamento apartamento) {
        new DialogoEco.Builder(this)
                .titulo(getString(R.string.sindico_excluir_titulo, apartamento.getNumero()))
                .mensagem(R.string.sindico_a_exclusao_afeta_apenas_os_dados_l)
                .botao(R.string.perfil_cancelar, DialogoEco.Estilo.PREENCHIDO, null)
                .botao(R.string.perfil_excluir_confirmar, DialogoEco.Estilo.PERIGO, () -> {
                    viewModel.excluirApartamento(apartamento.getId());
                    mostrarApartamentos();
                    AvisoFlutuante.mostrar(this, R.string.sindico_unidade_removida_desta_demonstraca);
                })
                .mostrar();
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
}
