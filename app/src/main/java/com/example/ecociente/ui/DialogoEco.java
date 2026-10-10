package com.example.ecociente.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import com.example.ecociente.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class DialogoEco {

    public enum Estilo {
        PREENCHIDO(R.color.verde_escuro_principal, R.color.branco, R.color.verde_escuro_principal),
        CONTORNO(R.color.branco, R.color.verde_escuro_principal, R.color.verde_escuro_principal),
        PERIGO(R.color.branco, R.color.rosa_ecociente, R.color.rosa_ecociente);

        @ColorRes final int fundo;
        @ColorRes final int texto;
        @ColorRes final int borda;

        Estilo(@ColorRes int fundo, @ColorRes int texto, @ColorRes int borda) {
            this.fundo = fundo;
            this.texto = texto;
            this.borda = borda;
        }
    }

    private static final int MARGEM_LATERAL_DP = 24;

    private DialogoEco() {}

    public static final class Builder {

        private final Context contexto;
        private final View raiz;
        private final LinearLayout botoes;
        private AlertDialog dialogo;

        public Builder(@NonNull Context contexto) {
            this.contexto = contexto;
            this.raiz = LayoutInflater.from(contexto).inflate(R.layout.dialog_eco, null);
            this.botoes = raiz.findViewById(R.id.botoesDialogoEco);
        }

        @NonNull
        public Builder titulo(@StringRes int titulo) {
            ((TextView) raiz.findViewById(R.id.tituloDialogoEco)).setText(titulo);
            return this;
        }

        @NonNull
        public Builder mensagem(@StringRes int mensagem) {
            return mensagem(contexto.getString(mensagem));
        }

        @NonNull
        public Builder mensagem(@NonNull CharSequence mensagem) {
            TextView texto = raiz.findViewById(R.id.mensagemDialogoEco);
            texto.setText(mensagem);
            texto.setVisibility(View.VISIBLE);
            return this;
        }

        @NonNull
        public Builder conteudo(@NonNull View conteudo) {
            FrameLayout area = raiz.findViewById(R.id.conteudoDialogoEco);
            area.addView(conteudo);
            area.setVisibility(View.VISIBLE);
            return this;
        }

        @NonNull
        public Builder botao(@StringRes int rotulo, @NonNull Estilo estilo, @Nullable Runnable acao) {

            MaterialButton botao =
                    (MaterialButton) LayoutInflater.from(contexto).inflate(R.layout.botao_dialogo, botoes, false);

            botao.setText(rotulo);
            botao.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(contexto, estilo.fundo)));
            botao.setTextColor(ContextCompat.getColor(contexto, estilo.texto));
            botao.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(contexto, estilo.borda)));

            botao.setOnClickListener(
                    view -> {
                        dialogo.dismiss();

                        if (acao != null) {
                            acao.run();
                        }
                    });

            botoes.addView(botao);

            return this;
        }

        public void mostrar() {

            dialogo = new MaterialAlertDialogBuilder(contexto).setView(raiz).create();

            dialogo.show();

            if (dialogo.getWindow() != null) {
                int margem = Dimensoes.dpParaPx(contexto, MARGEM_LATERAL_DP);

                dialogo.getWindow()
                        .setBackgroundDrawable(new InsetDrawable(new ColorDrawable(Color.TRANSPARENT), margem, 0, margem, 0));
            }
        }
    }
}
