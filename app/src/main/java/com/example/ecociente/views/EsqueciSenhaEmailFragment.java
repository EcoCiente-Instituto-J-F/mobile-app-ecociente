package com.example.ecociente.views;

import android.os.Bundle;
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

// Passo 1: pede o email e pede pro ViewModel enviar o código.
public class EsqueciSenhaEmailFragment extends Fragment {
    private EditText campoEmail;
    private MaterialButton botaoEnviarCodigo;
    private EsqueciSenhaViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle estadoSalvo) {
        return inflater.inflate(R.layout.fragment_esqueci_senha_email, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estadoSalvo) {
        super.onViewCreated(view, estadoSalvo);

        campoEmail = view.findViewById(R.id.campoEmail);
        botaoEnviarCodigo = view.findViewById(R.id.botaoEnviarCodigo);

        viewModel = new ViewModelProvider(requireActivity()).get(EsqueciSenhaViewModel.class);
        viewModel.getCarregando().observe(getViewLifecycleOwner(), this::definirCarregando);

        botaoEnviarCodigo.setOnClickListener(clique -> enviarCodigo());
    }

    private void enviarCodigo() {
        String email = campoEmail.getText().toString().trim();

        if (email.isEmpty()) {
            mostrarMensagem("Informe seu email");
            return;
        }

        viewModel
                .enviarCodigo(email)
                .observe(
                        getViewLifecycleOwner(),
                        resultado -> {
                            if (!resultado.isSucesso()) {
                                mostrarMensagem(resultado.getMensagemErro());
                                return;
                            }

                            mostrarMensagem("Se o email existir, você receberá um código");
                            Navigation.findNavController(requireView())
                                    .navigate(R.id.acaoParaCodigo);
                        });
    }

    private void definirCarregando(boolean carregando) {
        botaoEnviarCodigo.setEnabled(!carregando);
        botaoEnviarCodigo.setAlpha(carregando ? 0.55f : 1f);
    }

    private void mostrarMensagem(@NonNull String mensagem) {
        Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show();
    }
}
