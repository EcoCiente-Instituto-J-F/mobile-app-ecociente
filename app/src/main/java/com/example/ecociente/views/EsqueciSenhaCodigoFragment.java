package com.example.ecociente.views;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.ecociente.R;
import com.example.ecociente.viewmodels.EsqueciSenhaViewModel;
import com.google.android.material.button.MaterialButton;

// tela 2: código de 4 dígitos
public class EsqueciSenhaCodigoFragment extends Fragment {
    private EditText digito1;
    private EditText digito2;
    private EditText digito3;
    private EditText digito4;
    private MaterialButton botaoVerificar;
    private EsqueciSenhaViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle estadoSalvo) {
        return inflater.inflate(R.layout.fragment_esqueci_senha_codigo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estadoSalvo) {
        super.onViewCreated(view, estadoSalvo);

        digito1 = view.findViewById(R.id.digito1);
        digito2 = view.findViewById(R.id.digito2);
        digito3 = view.findViewById(R.id.digito3);
        digito4 = view.findViewById(R.id.digito4);
        botaoVerificar = view.findViewById(R.id.botaoVerificar);

        viewModel = new ViewModelProvider(requireActivity()).get(EsqueciSenhaViewModel.class);
        viewModel.getCarregando().observe(getViewLifecycleOwner(), this::definirCarregando);

        configurarAvancoAutomatico(digito1, null, digito2);
        configurarAvancoAutomatico(digito2, digito1, digito3);
        configurarAvancoAutomatico(digito3, digito2, digito4);
        configurarAvancoAutomatico(digito4, digito3, null);

        botaoVerificar.setOnClickListener(clique -> verificarCodigo());
    }

    // pula pro próximo campo ao digitar, volta pro anterior se apagar vazio
    private void configurarAvancoAutomatico(EditText campoAtual, @Nullable EditText campoAnterior, @Nullable EditText proximoCampo) {
        campoAtual.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable texto) {
                if (texto.length() == 1 && proximoCampo != null) {
                    proximoCampo.requestFocus();
                }
            }
        });

        campoAtual.setOnKeyListener((v, keyCode, evento) -> {
            boolean apagouComCampoVazio = keyCode == KeyEvent.KEYCODE_DEL
                    && evento.getAction() == KeyEvent.ACTION_DOWN
                    && campoAtual.getText().length() == 0;

            if (apagouComCampoVazio && campoAnterior != null) {
                campoAnterior.setText("");
                campoAnterior.requestFocus();
                return true;
            }

            return false;
        });
    }

    private void verificarCodigo() {
        String codigo = digito1.getText().toString()
                + digito2.getText().toString()
                + digito3.getText().toString()
                + digito4.getText().toString();

        if (codigo.length() != 4) {
            mostrarMensagem("Digite os 4 dígitos do código");
            return;
        }

        viewModel.verificarCodigo(codigo).observe(getViewLifecycleOwner(), resultado -> {
            if (!resultado.isSucesso()) {
                mostrarMensagem(resultado.getMensagemErro());
                return;
            }

            Navigation.findNavController(requireView()).navigate(R.id.acaoParaNovaSenha);
        });
    }

    private void definirCarregando(boolean carregando) {
        botaoVerificar.setEnabled(!carregando);
        botaoVerificar.setAlpha(carregando ? 0.55f : 1f);
    }

    private void mostrarMensagem(@NonNull String mensagem) {
        Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show();
    }
}
