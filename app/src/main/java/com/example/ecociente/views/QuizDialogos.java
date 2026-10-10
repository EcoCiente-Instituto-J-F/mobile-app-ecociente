package com.example.ecociente.views;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.ecociente.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * Pop-ups do Quiz com a identidade visual do EcoCiente.
 */
public final class QuizDialogos {

    private QuizDialogos() {
    }

    public static void mostrarConfirmacao(
            @NonNull AppCompatActivity activity,
            @NonNull String titulo,
            @NonNull String mensagem,
            @NonNull String textoConfirmar,
            @NonNull String textoCancelar,
            @NonNull Runnable aoConfirmar
    ) {
        Dialog dialog = criarDialogBase(activity);
        MaterialCardView card = criarCard(activity);

        LinearLayout conteudo = criarConteudo(activity);
        conteudo.addView(criarTitulo(activity, titulo));

        TextView descricao = criarDescricao(activity, mensagem, true);
        LinearLayout.LayoutParams descricaoParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        descricaoParams.topMargin = dp(activity, 10);
        descricao.setLayoutParams(descricaoParams);
        conteudo.addView(descricao);

        MaterialButton confirmar = criarBotaoPrimario(activity, textoConfirmar);
        LinearLayout.LayoutParams confirmarParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(activity, 50)
        );
        confirmarParams.topMargin = dp(activity, 22);
        confirmar.setLayoutParams(confirmarParams);
        conteudo.addView(confirmar);

        MaterialButton cancelar = criarBotaoSecundario(activity, textoCancelar);
        LinearLayout.LayoutParams cancelarParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(activity, 50)
        );
        cancelarParams.topMargin = dp(activity, 10);
        cancelar.setLayoutParams(cancelarParams);
        conteudo.addView(cancelar);

        confirmar.setOnClickListener(view -> {
            dialog.dismiss();
            aoConfirmar.run();
        });
        cancelar.setOnClickListener(view -> dialog.dismiss());

        card.addView(conteudo);
        exibir(activity, dialog, card);
    }

    public static void mostrarDetalheQuestao(
            @NonNull AppCompatActivity activity,
            @NonNull String titulo,
            @NonNull String enunciado,
            @NonNull String respostaUsuario,
            @NonNull String respostaCorreta,
            boolean acertou,
            @NonNull String textoFechar
    ) {
        Dialog dialog = criarDialogBase(activity);
        MaterialCardView card = criarCard(activity);
        LinearLayout conteudo = criarConteudo(activity);

        conteudo.addView(criarTitulo(activity, titulo));

        TextView pergunta = criarDescricao(activity, enunciado, false);
        LinearLayout.LayoutParams perguntaParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        perguntaParams.topMargin = dp(activity, 14);
        pergunta.setLayoutParams(perguntaParams);
        pergunta.setTextColor(Color.parseColor("#484848"));
        pergunta.setTextSize(15f);
        conteudo.addView(pergunta);

        TextView suaResposta = criarDescricao(activity, respostaUsuario, false);
        LinearLayout.LayoutParams respostaParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        respostaParams.topMargin = dp(activity, 18);
        suaResposta.setLayoutParams(respostaParams);
        suaResposta.setTextColor(ContextCompat.getColor(
                activity,
                acertou ? R.color.verde_escuro_principal : R.color.rosa_ecociente
        ));
        suaResposta.setTextSize(14f);
        conteudo.addView(suaResposta);

        TextView correta = criarDescricao(activity, respostaCorreta, false);
        LinearLayout.LayoutParams corretaParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        corretaParams.topMargin = dp(activity, 6);
        correta.setLayoutParams(corretaParams);
        correta.setTextColor(ContextCompat.getColor(activity, R.color.verde_escuro_principal));
        correta.setTextSize(14f);
        conteudo.addView(correta);

        MaterialButton fechar = criarBotaoPrimario(activity, textoFechar);
        LinearLayout.LayoutParams fecharParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(activity, 50)
        );
        fecharParams.topMargin = dp(activity, 22);
        fechar.setLayoutParams(fecharParams);
        fechar.setOnClickListener(view -> dialog.dismiss());
        conteudo.addView(fechar);

        card.addView(conteudo);
        exibir(activity, dialog, card);
    }

    /**
     * Para respostas certas, mantemos a interacao leve: o usuario recebe somente
     * uma explicacao do motivo da resposta estar correta, sem sair da tela de resultado.
     */
    public static void mostrarExplicacaoQuestao(
            @NonNull AppCompatActivity activity,
            @NonNull String titulo,
            @NonNull String respostaCorreta,
            @NonNull String explicacao,
            @NonNull String textoFechar
    ) {
        Dialog dialog = criarDialogBase(activity);
        MaterialCardView card = criarCard(activity);
        LinearLayout conteudo = criarConteudo(activity);

        conteudo.addView(criarTitulo(activity, titulo));

        TextView correta = criarDescricao(activity, respostaCorreta, false);
        LinearLayout.LayoutParams corretaParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        corretaParams.topMargin = dp(activity, 16);
        correta.setLayoutParams(corretaParams);
        correta.setTextColor(ContextCompat.getColor(activity, R.color.verde_escuro_principal));
        correta.setTextSize(14.5f);
        correta.setTypeface(ResourcesCompat.getFont(activity, R.font.nunito_semibold));
        conteudo.addView(correta);

        TextView rotulo = criarDescricao(activity, activity.getString(R.string.quiz_revisao_explicacao), false);
        LinearLayout.LayoutParams rotuloParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rotuloParams.topMargin = dp(activity, 18);
        rotulo.setLayoutParams(rotuloParams);
        rotulo.setTextColor(ContextCompat.getColor(activity, R.color.verde_escuro_principal));
        rotulo.setTextSize(15f);
        rotulo.setTypeface(ResourcesCompat.getFont(activity, R.font.nunito_semibold));
        conteudo.addView(rotulo);

        TextView descricao = criarDescricao(activity, explicacao, false);
        LinearLayout.LayoutParams descricaoParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        descricaoParams.topMargin = dp(activity, 8);
        descricao.setLayoutParams(descricaoParams);
        descricao.setTextColor(Color.parseColor("#565656"));
        descricao.setTextSize(14f);
        conteudo.addView(descricao);

        MaterialButton fechar = criarBotaoPrimario(activity, textoFechar);
        LinearLayout.LayoutParams fecharParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(activity, 50)
        );
        fecharParams.topMargin = dp(activity, 22);
        fechar.setLayoutParams(fecharParams);
        fechar.setOnClickListener(view -> dialog.dismiss());
        conteudo.addView(fechar);

        card.addView(conteudo);
        exibir(activity, dialog, card);
    }

    private static Dialog criarDialogBase(@NonNull AppCompatActivity activity) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @NonNull
    private static MaterialCardView criarCard(@NonNull AppCompatActivity activity) {
        MaterialCardView card = new MaterialCardView(activity);
        card.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.branco));
        card.setCardElevation(dp(activity, 10));
        card.setRadius(dp(activity, 20));
        card.setStrokeWidth(0);
        return card;
    }

    @NonNull
    private static LinearLayout criarConteudo(@NonNull AppCompatActivity activity) {
        LinearLayout conteudo = new LinearLayout(activity);
        conteudo.setOrientation(LinearLayout.VERTICAL);
        conteudo.setGravity(Gravity.CENTER_HORIZONTAL);
        conteudo.setPadding(
                dp(activity, 26),
                dp(activity, 24),
                dp(activity, 26),
                dp(activity, 24)
        );
        return conteudo;
    }

    @NonNull
    private static TextView criarTitulo(@NonNull AppCompatActivity activity, @NonNull String texto) {
        TextView titulo = new TextView(activity);
        titulo.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        titulo.setGravity(Gravity.CENTER);
        titulo.setIncludeFontPadding(false);
        titulo.setText(texto);
        titulo.setTextColor(ContextCompat.getColor(activity, R.color.verde_escuro_principal));
        titulo.setTextSize(22f);
        titulo.setTypeface(ResourcesCompat.getFont(activity, R.font.nunito_semibold));
        return titulo;
    }

    @NonNull
    private static TextView criarDescricao(
            @NonNull AppCompatActivity activity,
            @NonNull String texto,
            boolean centralizado
    ) {
        TextView descricao = new TextView(activity);
        descricao.setGravity(centralizado ? Gravity.CENTER : Gravity.START);
        descricao.setIncludeFontPadding(false);
        descricao.setLineSpacing(dp(activity, 1), 1f);
        descricao.setText(texto);
        descricao.setTextColor(ContextCompat.getColor(activity, R.color.cinza_texto_auxiliar));
        descricao.setTextSize(14f);
        descricao.setTypeface(ResourcesCompat.getFont(activity, R.font.nunito_regular));
        return descricao;
    }

    @NonNull
    private static MaterialButton criarBotaoPrimario(
            @NonNull AppCompatActivity activity,
            @NonNull String texto
    ) {
        MaterialButton botao = new MaterialButton(activity);
        botao.setAllCaps(false);
        botao.setInsetTop(0);
        botao.setInsetBottom(0);
        botao.setMinHeight(0);
        botao.setText(texto);
        botao.setTextColor(ContextCompat.getColor(activity, R.color.branco));
        botao.setTextSize(15.5f);
        botao.setTypeface(ResourcesCompat.getFont(activity, R.font.nunito_semibold));
        botao.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(activity, R.color.verde_escuro_principal)
        ));
        botao.setCornerRadius(dp(activity, 12));
        return botao;
    }

    @NonNull
    private static MaterialButton criarBotaoSecundario(
            @NonNull AppCompatActivity activity,
            @NonNull String texto
    ) {
        MaterialButton botao = new MaterialButton(activity);
        int rosa = ContextCompat.getColor(activity, R.color.rosa_ecociente);
        botao.setAllCaps(false);
        botao.setInsetTop(0);
        botao.setInsetBottom(0);
        botao.setMinHeight(0);
        botao.setText(texto);
        botao.setTextColor(rosa);
        botao.setTextSize(15.5f);
        botao.setTypeface(ResourcesCompat.getFont(activity, R.font.nunito_semibold));
        botao.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(activity, R.color.branco)
        ));
        botao.setStrokeColor(ColorStateList.valueOf(rosa));
        botao.setStrokeWidth(dp(activity, 1));
        botao.setCornerRadius(dp(activity, 12));
        return botao;
    }

    private static void exibir(
            @NonNull AppCompatActivity activity,
            @NonNull Dialog dialog,
            @NonNull MaterialCardView card
    ) {
        dialog.setContentView(card);
        dialog.show();

        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }

        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = (int) (activity.getResources().getDisplayMetrics().widthPixels * .90f);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.dimAmount = .48f;
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }

    private static int dp(@NonNull AppCompatActivity activity, float valor) {
        return Math.round(valor * activity.getResources().getDisplayMetrics().density);
    }
}
