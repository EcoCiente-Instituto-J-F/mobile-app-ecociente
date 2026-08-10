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
import com.google.firebase.functions.FirebaseFunctions;
import java.util.HashMap;
import java.util.Map;

// Passo 1 do "Esqueci a senha": pede o email e chama a Cloud Function
// que gera e envia o código de 4 dígitos por email.
public class EsqueciSenhaEmailFragment extends Fragment {
    private EditText campoEmail;
    private MaterialButton botaoEnviarCodigo;
    private FirebaseFunctions funcoes;
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

        funcoes = FirebaseFunctions.getInstance();

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

        Map<String, Object> dados = new HashMap<>();
        dados.put("email", email);

        funcoes.getHttpsCallable("enviarCodigoRecuperacao")
                .call(dados)
                .addOnCompleteListener(tarefa -> {
                    definirEnvioEmAndamento(false);

                    if (!tarefa.isSuccessful()) {
                        mostrarMensagem("Não foi possível enviar o código. Tente novamente");
                        return;
                    }

                    mostrarMensagem("Se o email existir, você receberá um código");

                    Bundle argumentos = new Bundle();
                    argumentos.putString("email", email);

                    Navigation.findNavController(requireView()).navigate(R.id.acaoParaCodigo, argumentos);
                });
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
