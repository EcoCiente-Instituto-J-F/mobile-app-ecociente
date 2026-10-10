package com.example.ecociente.views;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecociente.R;
import com.example.ecociente.viewmodels.SindicoColetaViewModel;

/** Seletor de estrelas para a avaliação local de uma coleta de exemplo. */
public final class SindicoAvaliarColetaDialogFragment extends DialogFragment {
    public static final String RESULTADO_AVALIACAO = "avaliacaoSindicoSalva";
    private static final String ARG_ID = "coletaId";
    private static final String ARG_NOME = "cooperativa";
    private static final String ESTADO_NOTA = "notaSelecionada";

    private final ImageButton[] estrelas = new ImageButton[5];
    private int notaSelecionada;
    private TextView legenda;
    private Button salvar;

    public static SindicoAvaliarColetaDialogFragment criar(int coletaId, String cooperativa) {
        SindicoAvaliarColetaDialogFragment dialogo = new SindicoAvaliarColetaDialogFragment();
        Bundle argumentos = new Bundle();
        argumentos.putInt(ARG_ID, coletaId);
        argumentos.putString(ARG_NOME, cooperativa);
        dialogo.setArguments(argumentos);
        return dialogo;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle estadoSalvo) {
        Dialog dialogo = new Dialog(requireContext());
        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View conteudo = requireActivity().getLayoutInflater()
                .inflate(R.layout.dialog_sindico_avaliar_coleta, null);
        dialogo.setContentView(conteudo);
        Window janela = dialogo.getWindow();
        if (janela != null) janela.setBackgroundDrawableResource(android.R.color.transparent);

        Bundle argumentos = requireArguments();
        int coletaId = argumentos.getInt(ARG_ID);
        SindicoColetaViewModel viewModel = new ViewModelProvider(this)
                .get(SindicoColetaViewModel.class);
        notaSelecionada = estadoSalvo == null ? viewModel.getAvaliacao(coletaId)
                : estadoSalvo.getInt(ESTADO_NOTA, 0);
        ((TextView) conteudo.findViewById(R.id.cooperativaAvaliacao))
                .setText(argumentos.getString(ARG_NOME, ""));
        conteudo.findViewById(R.id.fecharAvaliacao).setOnClickListener(view -> dismiss());
        legenda = conteudo.findViewById(R.id.notaSelecionada);
        salvar = conteudo.findViewById(R.id.salvarAvaliacao);

        LinearLayout linha = conteudo.findViewById(R.id.estrelasAvaliacao);
        for (int i = 0; i < estrelas.length; i++) {
            int nota = i + 1;
            ImageButton estrela = new ImageButton(requireContext());
            estrela.setBackgroundResource(android.R.color.transparent);
            estrela.setScaleType(ImageButton.ScaleType.FIT_CENTER);
            estrela.setPadding(dp(6), dp(6), dp(6), dp(6));
            estrela.setContentDescription(nota + (nota == 1 ? " estrela" : " estrelas"));
            estrela.setOnClickListener(view -> selecionar(nota));
            linha.addView(estrela, new LinearLayout.LayoutParams(0, dp(52), 1));
            estrelas[i] = estrela;
        }
        atualizarEstrelas();
        salvar.setOnClickListener(view -> {
            viewModel.avaliar(coletaId, notaSelecionada);
            getParentFragmentManager().setFragmentResult(RESULTADO_AVALIACAO, new Bundle());
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
            dialogo.getWindow().setLayout(Math.min(larguraTela - dp(32), dp(480)),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void selecionar(int nota) {
        notaSelecionada = nota;
        atualizarEstrelas();
    }

    private void atualizarEstrelas() {
        for (int i = 0; i < estrelas.length; i++) {
            estrelas[i].setImageResource(i < notaSelecionada
                    ? R.drawable.sindico_estrela_cheia : R.drawable.sindico_estrela_vazia);
            estrelas[i].setSelected(i < notaSelecionada);
            estrelas[i].setContentDescription((i + 1) + (i == 0 ? " estrela" : " estrelas")
                    + (i + 1 == notaSelecionada ? ", nota selecionada" : ""));
        }
        legenda.setText(notaSelecionada == 0 ? "Escolha de 1 a 5 estrelas"
                : notaSelecionada + (notaSelecionada == 1 ? " estrela selecionada"
                : " estrelas selecionadas"));
        salvar.setVisibility(notaSelecionada == 0 ? View.GONE : View.VISIBLE);
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle estado) {
        estado.putInt(ESTADO_NOTA, notaSelecionada);
        super.onSaveInstanceState(estado);
    }
}
