package com.example.ecociente.controller;

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
import androidx.navigation.Navigation;
import com.example.ecociente.R;
import com.google.android.material.button.MaterialButton;
import org.json.JSONException;
import org.json.JSONObject;

// Passo 2 do "Esqueci a senha": 4 caixas de dígito que avançam o foco sozinhas,
// e confirma o código com o backend antes de deixar seguir pra próxima tela.
public class EsqueciSenhaCodigoFragment extends Fragment {
    private EditText digito1;
    private EditText digito2;
    private EditText digito3;
    private EditText digito4;
    private MaterialButton botaoVerificar;
    private String email;
    private boolean verificacaoEmAndamento = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle estadoSalvo) {
        return inflater.inflate(R.layout.fragment_esqueci_senha_codigo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estadoSalvo) {
        super.onViewCreated(view, estadoSalvo);

        email = requireArguments().getString("email");

        digito1 = view.findViewById(R.id.digito1);
        digito2 = view.findViewById(R.id.digito2);
        digito3 = view.findViewById(R.id.digito3);
        digito4 = view.findViewById(R.id.digito4);
        botaoVerificar = view.findViewById(R.id.botaoVerificar);

        configurarAvancoAutomatico(digito1, null, digito2);
        configurarAvancoAutomatico(digito2, digito1, digito3);
        configurarAvancoAutomatico(digito3, digito2, digito4);
        configurarAvancoAutomatico(digito4, digito3, null);

        botaoVerificar.setOnClickListener(clique -> verificarCodigo());
    }

    // Ao digitar um dígito, pula pro próximo campo; ao apagar num campo vazio, volta pro anterior.
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
        if (verificacaoEmAndamento) {
            return;
        }

        String codigo = digito1.getText().toString()
                + digito2.getText().toString()
                + digito3.getText().toString()
                + digito4.getText().toString();

        if (codigo.length() != 4) {
            mostrarMensagem("Digite os 4 dígitos do código");
            return;
        }

        definirVerificacaoEmAndamento(true);

        try {
            JSONObject corpo = new JSONObject();
            corpo.put("email", email);
            corpo.put("codigo", codigo);

            ApiEsqueciSenha.chamar("verificarCodigoRecuperacao", corpo, (sucesso, mensagemErro) -> {
                definirVerificacaoEmAndamento(false);

                if (!sucesso) {
                    mostrarMensagem(mensagemErro);
                    return;
                }

                Bundle argumentos = new Bundle();
                argumentos.putString("email", email);
                argumentos.putString("codigo", codigo);

                Navigation.findNavController(requireView()).navigate(R.id.acaoParaNovaSenha, argumentos);
            });
        } catch (JSONException erro) {
            definirVerificacaoEmAndamento(false);
            mostrarMensagem("Erro inesperado. Tente novamente");
        }
    }

    private void definirVerificacaoEmAndamento(boolean emAndamento) {
        verificacaoEmAndamento = emAndamento;

        botaoVerificar.setEnabled(!emAndamento);
        botaoVerificar.setAlpha(emAndamento ? 0.55f : 1f);
    }

    private void mostrarMensagem(@NonNull String mensagem) {
        Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show();
    }
}
