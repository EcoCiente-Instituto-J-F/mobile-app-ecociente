package com.example.ecociente.controller;


import android.content.Intent;
import android.os.Build;
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
import com.example.ecociente.R;
import com.google.android.material.button.MaterialButton;
import org.json.JSONException;
import org.json.JSONObject;

// Passo 3 do "Esqueci a senha": define a nova senha e chama o backend
// que revalida o código e efetivamente troca a senha no Firebase Auth.
public class EsqueciSenhaNovaSenhaFragment extends Fragment {
    private EditText campoSenha;
    private EditText campoConfirmarSenha;
    private MaterialButton botaoContinuar;
    private ImageView iconeOlhoSenha;
    private ImageView iconeOlhoConfirmarSenha;
    private String email;
    private String codigo;
    private boolean senhaVisivel = false;
    private boolean confirmarSenhaVisivel = false;
    private boolean redefinicaoEmAndamento = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle estadoSalvo) {
        return inflater.inflate(R.layout.fragment_esqueci_senha_nova_senha, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estadoSalvo) {
        super.onViewCreated(view, estadoSalvo);

        email = requireArguments().getString("email");
        codigo = requireArguments().getString("codigo");

        campoSenha = view.findViewById(R.id.campoSenha);
        campoConfirmarSenha = view.findViewById(R.id.campoConfirmarSenha);
        botaoContinuar = view.findViewById(R.id.botaoContinuar);
        iconeOlhoSenha = view.findViewById(R.id.iconeOlhoSenha);
        iconeOlhoConfirmarSenha = view.findViewById(R.id.iconeOlhoConfirmarSenha);

        botaoContinuar.setOnClickListener(clique -> redefinirSenha());

        iconeOlhoSenha.setOnClickListener(clique -> {
            senhaVisivel = alternarVisibilidade(campoSenha, iconeOlhoSenha, senhaVisivel);
        });

        iconeOlhoConfirmarSenha.setOnClickListener(clique -> {
            confirmarSenhaVisivel = alternarVisibilidade(campoConfirmarSenha, iconeOlhoConfirmarSenha, confirmarSenhaVisivel);
        });
    }

    private void redefinirSenha() {
        if (redefinicaoEmAndamento) {
            return;
        }

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

        definirRedefinicaoEmAndamento(true);

        try {
            JSONObject corpo = new JSONObject();
            corpo.put("email", email);
            corpo.put("codigo", codigo);
            corpo.put("novaSenha", senha);

            ApiEsqueciSenha.chamar("redefinirSenhaComCodigo", corpo, (sucesso, mensagemErro) -> {
                definirRedefinicaoEmAndamento(false);

                if (!sucesso) {
                    mostrarMensagem(mensagemErro);
                    return;
                }

                mostrarMensagem("Senha redefinida com sucesso");
                voltarParaLogin();
            });
        } catch (JSONException erro) {
            definirRedefinicaoEmAndamento(false);
            mostrarMensagem("Erro inesperado. Tente novamente");
        }
    }

    private void voltarParaLogin() {
        Intent rota = new Intent(requireContext(), Login.class);

        rota.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(rota);
        requireActivity().finish();
    }

    private void definirRedefinicaoEmAndamento(boolean emAndamento) {
        redefinicaoEmAndamento = emAndamento;

        botaoContinuar.setEnabled(!emAndamento);
        botaoContinuar.setAlpha(emAndamento ? 0.55f : 1f);
    }

    // Mesmo padrão de toggle de olho usado em Login/Cadastro; retorna o novo estado de visibilidade.
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
