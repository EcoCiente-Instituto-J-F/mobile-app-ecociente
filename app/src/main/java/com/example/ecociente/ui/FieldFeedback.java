package com.example.ecociente.ui;

import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.example.ecociente.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

public final class FieldFeedback {

    private FieldFeedback() {
    }

    private static int cor(View view, @ColorRes int cor) {
        return ContextCompat.getColor(view.getContext(), cor);
    }

    public static void error(
            MaterialCardView card,
            TextView message,
            String text,
            Motion motion
    ) {
        aplicarEstadoErro(card, text, motion);

        if (message == null) {
            return;
        }

        message.setText(text);
        message.setTextColor(cor(message, R.color.erro_texto));
        message.setBackground(criarFundoErro(message));
        message.setPadding(dp(message, 10), dp(message, 6), dp(message, 10), dp(message, 6));
        message.setVisibility(View.VISIBLE);
        message.setAlpha(0f);
        message.setTranslationY(-dp(message, 3));
        message.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(Motion.STATE_MS)
                .start();
    }

    public static void error(
            MaterialCardView card,
            String accessibilityText,
            Motion motion
    ) {
        aplicarEstadoErro(card, accessibilityText, motion);
    }

    public static void clear(
            MaterialCardView card,
            TextView message,
            int normalColor
    ) {
        limparEstadoCard(card, normalColor);

        if (message == null) {
            return;
        }

        if (message.getVisibility() == View.VISIBLE) {
            message.animate()
                    .alpha(0f)
                    .translationY(-dp(message, 2))
                    .setDuration(Motion.MICRO_MS)
                    .withEndAction(() -> {
                        message.setVisibility(View.GONE);
                        message.setTranslationY(0f);
                    })
                    .start();
        }
    }

    public static void clear(
            MaterialCardView card,
            int normalColor
    ) {
        limparEstadoCard(card, normalColor);
    }

    public static void showErrorSnackbar(
            View anchor,
            String text
    ) {
        if (anchor == null || text == null || text.trim().isEmpty()) {
            return;
        }

        Snackbar snackbar = Snackbar.make(anchor, text, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(cor(snackbarView, R.color.erro_fundo));
        fundo.setCornerRadius(dp(snackbarView, 14));
        fundo.setStroke(dp(snackbarView, 1), cor(snackbarView, R.color.rosa_ecociente));

        snackbarView.setBackground(fundo);
        snackbarView.setElevation(dp(snackbarView, 6));
        snackbar.setTextColor(cor(snackbarView, R.color.erro_texto));

        snackbar.show();
    }

    private static void aplicarEstadoErro(
            MaterialCardView card,
            String accessibilityText,
            Motion motion
    ) {
        if (card == null) {
            return;
        }

        int erro = cor(card, R.color.rosa_ecociente);

        card.setStrokeColor(erro);
        card.setStrokeWidth(dp(card, 2));
        card.setCardElevation(dp(card, 4));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            card.setOutlineAmbientShadowColor(erro);
            card.setOutlineSpotShadowColor(erro);
        }

        card.setContentDescription(accessibilityText);

        if (motion != null) {
            motion.shake(card);
        }
    }

    private static void limparEstadoCard(
            MaterialCardView card,
            int normalColor
    ) {
        if (card == null) {
            return;
        }

        card.setStrokeColor(normalColor);
        card.setStrokeWidth(dp(card, 1));
        card.setCardElevation(0f);
        card.setContentDescription(null);
    }

    private static GradientDrawable criarFundoErro(View view) {
        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(cor(view, R.color.erro_fundo));
        fundo.setCornerRadius(dp(view, 9));
        fundo.setStroke(dp(view, 1), cor(view, R.color.rosa_ecociente));
        return fundo;
    }

    private static int dp(View view, int dp) {
        return Math.round(dp * view.getResources().getDisplayMetrics().density);
    }
}
