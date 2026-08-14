package com.example.ecociente.controller;

import android.os.Bundle;
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

// Passo 1 do "Esqueci a senha": pede o email e chama o backend (Vercel)
// que gera e envia o código de 4 dígitos por email.
public class EsqueciSenhaEmailFragment extends Fragment {
    private EditText campoEmail;
    private MaterialButton botaoEnviarCodigo;
    private boolean envioEmAndamento = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle estadoSalvo) {
        return inflater.inflate(R.layout.fragment_esqueci_senha_email, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estadoSalvo) {
        super.onViewCreated(view, estadoSalvo);

        campoEmail = view.findViewById(R.id.campoEmail);
        botaoEnviarCodigo = view.findViewById(R.id.botaoEnviarCodigo);

        botaoEnviarCodigo.setOnClickListener(clique -> enviarCodigo());
    }

    private void enviarCodigo() {
        if (envioEmAndamento) {
            return;
        }

        String email = campoEmail.getText().toString().trim();

        if (email.isEmpty()) {
            mostrarMensagem("Informe seu email");
            return;
        }

        definirEnvioEmAndamento(true);

        try {
            JSONObject corpo = new JSONObject();
            corpo.put("email", email);

            ApiEsqueciSenha.chamar("enviarCodigoRecuperacao", corpo, (sucesso, mensagemErro) -> {
                definirEnvioEmAndamento(false);

                if (!sucesso) {
                    mostrarMensagem(mensagemErro);
                    return;
                }

                mostrarMensagem("Se o email existir, você receberá um código");

                Bundle argumentos = new Bundle();
                argumentos.putString("email", email);

                Navigation.findNavController(requireView()).navigate(R.id.acaoParaCodigo, argumentos);
            });
        } catch (JSONException erro) {
            definirEnvioEmAndamento(false);
            mostrarMensagem("Erro inesperado. Tente novamente");
        }
    }

    private void definirEnvioEmAndamento(boolean emAndamento) {
        envioEmAndamento = emAndamento;

        botaoEnviarCodigo.setEnabled(!emAndamento);
        botaoEnviarCodigo.setAlpha(emAndamento ? 0.55f : 1f);
    }

    private void mostrarMensagem(@NonNull String mensagem) {
        Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show();
    }
}
