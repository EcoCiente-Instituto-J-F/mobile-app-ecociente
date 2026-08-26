package com.example.ecociente.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecociente.R;
import com.example.ecociente.viewmodels.EsqueciSenhaViewModel;
import com.google.android.material.button.MaterialButton;

// Passo 3 do "Esqueci a senha" (View do MVVM): define a nova senha; quem
// revalida o código e troca a senha de fato é o EsqueciSenhaViewModel.
public class EsqueciSenhaNovaSenhaFragment extends Fragment {
    private EditText campoSenha;
    private EditText campoConfirmarSenha;
    private MaterialButton botaoContinuar;
    private ImageView iconeOlhoSenha;
    private ImageView iconeOlhoConfirmarSenha;
    private EsqueciSenhaViewModel viewModel;
    private boolean senhaVisivel = false;
    private boolean confirmarSenhaVisivel = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle estadoSalvo) {
        return inflater.inflate(R.layout.fragment_esqueci_senha_nova_senha, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estadoSalvo) {
        super.onViewCreated(view, estadoSalvo);

        campoSenha = view.findViewById(R.id.campoSenha);
        campoConfirmarSenha = view.findViewById(R.id.campoConfirmarSenha);
        botaoContinuar = view.findViewById(R.id.botaoContinuar);
        iconeOlhoSenha = view.findViewById(R.id.iconeOlhoSenha);
        iconeOlhoConfirmarSenha = view.findViewById(R.id.iconeOlhoConfirmarSenha);

        viewModel = new ViewModelProvider(requireActivity()).get(EsqueciSenhaViewModel.class);
        viewModel.getCarregando().observe(getViewLifecycleOwner(), this::definirCarregando);

        botaoContinuar.setOnClickListener(clique -> redefinirSenha());

        iconeOlhoSenha.setOnClickListener(
                clique -> {
                    senhaVisivel = alternarVisibilidade(campoSenha, iconeOlhoSenha, senhaVisivel);
                });

        iconeOlhoConfirmarSenha.setOnClickListener(
                clique -> {
                    confirmarSenhaVisivel =
                            alternarVisibilidade(
                                    campoConfirmarSenha,
                                    iconeOlhoConfirmarSenha,
                                    confirmarSenhaVisivel);
                });
    }

    private void redefinirSenha() {
        String senha = campoSenha.getText().toString();

        String confirmarSenha = campoConfirmarSenha.getText().toString();

        if (senha.isEmpty() || confirmarSenha.isEmpty()) {
            mostrarMensagem("Preencha os dois campos");
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            mostrarMensagem("As senhas não coincidem");
            return;
        }

        if (senha.length() < 6) {
            mostrarMensagem("A senha deve ter no mínimo 6 caracteres");
            return;
        }

        viewModel
                .redefinirSenha(senha)
                .observe(
                        getViewLifecycleOwner(),
                        resultado -> {
                            if (!resultado.isSucesso()) {
                                mostrarMensagem(resultado.getMensagemErro());
                                return;
                            }

                            mostrarMensagem("Senha redefinida com sucesso");
                            voltarParaLogin();
                        });
    }

    private void voltarParaLogin() {
        Intent rota = new Intent(requireContext(), Login.class);

        rota.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(rota);
        requireActivity().finish();
    }

    private void definirCarregando(boolean carregando) {
        botaoContinuar.setEnabled(!carregando);
        botaoContinuar.setAlpha(carregando ? 0.55f : 1f);
    }

    // Mesmo padrão de toggle de olho usado em Login/Cadastro; retorna o novo estado de
    // visibilidade.
    private boolean alternarVisibilidade(EditText campo, ImageView icone, boolean visivelAtual) {
        if (visivelAtual) {
            campo.setTransformationMethod(PasswordTransformationMethod.getInstance());
            icone.setImageResource(R.drawable.icon_olho_fechado);
            icone.setContentDescription("Mostrar senha");
        } else {
            campo.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            icone.setImageResource(R.drawable.icon_olho_aberto);
            icone.setContentDescription("Ocultar senha");
        }

        campo.setSelection(campo.getText().length());

        return !visivelAtual;
    }

    private void mostrarMensagem(@NonNull String mensagem) {
        Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show();
    }
}
