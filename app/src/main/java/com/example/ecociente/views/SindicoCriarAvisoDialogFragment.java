package com.example.ecociente.views;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
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
import com.example.ecociente.viewmodels.SindicoViewModel;

public final class SindicoCriarAvisoDialogFragment extends DialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle estadoSalvo) {
        return inflater.inflate(R.layout.dialog_sindico_criar_aviso, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estadoSalvo) {
        super.onViewCreated(view, estadoSalvo);
        EditText titulo = view.findViewById(R.id.campoTituloAviso);
        EditText mensagem = view.findViewById(R.id.campoMensagemAviso);
        TextView destinatario = view.findViewById(R.id.campoDestinatarioAviso);
        view.findViewById(R.id.botaoFecharCriarAviso).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.botaoCancelarAviso).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.botaoSalvarAviso).setOnClickListener(v -> {
            String tituloTexto = titulo.getText().toString().trim();
            String mensagemTexto = mensagem.getText().toString().trim();
            if (tituloTexto.isEmpty()) {
                titulo.setError("Informe um título.");
                titulo.requestFocus();
                return;
            }
            if (mensagemTexto.isEmpty()) {
                mensagem.setError("Escreva a mensagem do aviso.");
                mensagem.requestFocus();
                return;
            }
            new ViewModelProvider(requireActivity()).get(SindicoViewModel.class)
                    .salvarRascunho(tituloTexto, mensagemTexto, destinatario.getText().toString());
            getParentFragmentManager().setFragmentResult("rascunhoSindico", new Bundle());
            dismiss();
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle estadoSalvo) {
        Dialog dialog = super.onCreateDialog(estadoSalvo);
        Window window = dialog.getWindow();
        if (window != null) window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null || dialog.getWindow() == null) return;
        DisplayMetrics tela = getResources().getDisplayMetrics();
        dialog.getWindow().setLayout(Math.min((int) (tela.widthPixels * 0.94f),
                (int) (420 * tela.density)), WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }
}
