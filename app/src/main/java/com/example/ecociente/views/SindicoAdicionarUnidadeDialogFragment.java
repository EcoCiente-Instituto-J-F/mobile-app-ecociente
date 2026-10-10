package com.example.ecociente.views;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecociente.R;
import com.example.ecociente.model.SindicoApartamento;
import com.example.ecociente.viewmodels.SindicoUnidadesViewModel;

/** Formulário local para cadastrar ou editar uma unidade de um bloco. */
public final class SindicoAdicionarUnidadeDialogFragment extends DialogFragment {
    public static final String RESULTADO_UNIDADE = "unidadeSindicoSalva";
    private static final String ARG_BLOCO = "blocoId";
    private static final String ARG_APARTAMENTO = "apartamentoId";

    public static SindicoAdicionarUnidadeDialogFragment criar(int blocoId, int apartamentoId) {
        SindicoAdicionarUnidadeDialogFragment dialogo =
                new SindicoAdicionarUnidadeDialogFragment();
        Bundle argumentos = new Bundle();
        argumentos.putInt(ARG_BLOCO, blocoId);
        argumentos.putInt(ARG_APARTAMENTO, apartamentoId);
        dialogo.setArguments(argumentos);
        return dialogo;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle estadoSalvo) {
        Dialog dialogo = new Dialog(requireContext());
        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View conteudo = requireActivity().getLayoutInflater()
                .inflate(R.layout.dialog_sindico_adicionar_unidade, null);
        dialogo.setContentView(conteudo);
        Window janela = dialogo.getWindow();
        if (janela != null) {
            janela.setBackgroundDrawableResource(android.R.color.transparent);
            janela.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        int blocoId = requireArguments().getInt(ARG_BLOCO);
        int apartamentoId = requireArguments().getInt(ARG_APARTAMENTO, -1);
        SindicoUnidadesViewModel viewModel = new ViewModelProvider(this)
                .get(SindicoUnidadesViewModel.class);
        EditText numero = conteudo.findViewById(R.id.numeroFormularioUnidade);
        EditText morador = conteudo.findViewById(R.id.moradorFormularioUnidade);
        EditText telefone = conteudo.findViewById(R.id.telefoneFormularioUnidade);
        if (apartamentoId >= 0) {
            SindicoApartamento apartamento = viewModel.getApartamento(apartamentoId);
            if (apartamento != null) {
                numero.setText(apartamento.getNumero());
                morador.setText(apartamento.getMorador());
                telefone.setText(apartamento.getTelefone());
                ((TextView) conteudo.findViewById(R.id.tituloFormularioUnidade))
                        .setText("Editar unidade");
            }
        }
        conteudo.findViewById(R.id.voltarFormularioUnidade).setOnClickListener(view -> dismiss());
        conteudo.findViewById(R.id.enviarFormularioUnidade).setOnClickListener(view -> {
            String valorNumero = numero.getText().toString().trim();
            String valorMorador = morador.getText().toString().trim();
            String valorTelefone = telefone.getText().toString().trim();
            if (valorNumero.isEmpty()) {
                numero.setError("Informe o número da unidade");
                numero.requestFocus();
                return;
            }
            if (viewModel.existeNumero(blocoId, apartamentoId, valorNumero)) {
                numero.setError("Esta unidade já está cadastrada neste bloco");
                numero.requestFocus();
                return;
            }
            if (!valorTelefone.isEmpty() && valorMorador.isEmpty()) {
                morador.setError("Informe o morador ou remova o telefone");
                morador.requestFocus();
                return;
            }
            viewModel.salvarApartamento(apartamentoId, blocoId, valorNumero,
                    valorMorador, valorTelefone);
            getParentFragmentManager().setFragmentResult(RESULTADO_UNIDADE, new Bundle());
            dismiss();
        });
        return dialogo;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialogo = getDialog();
        if (dialogo != null && dialogo.getWindow() != null) {
            int larguraTela = getResources().getDisplayMetrics().widthPixels;
            dialogo.getWindow().setLayout(Math.min(larguraTela - dp(16), dp(440)),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}
