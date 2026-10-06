package com.example.ecociente.ui;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

public final class FieldFeedback {

    private static final int ERROR = Color.parseColor("#D64573");
    private static final int ERROR_BACKGROUND = Color.parseColor("#66D64573");
    private static final int ERROR_TEXT = Color.parseColor("#70213E");

    private FieldFeedback() {
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
        message.setTextColor(ERROR_TEXT);
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
        fundo.setColor(ERROR_BACKGROUND);
        fundo.setCornerRadius(dp(snackbarView, 14));
        fundo.setStroke(dp(snackbarView, 1), ERROR);

        snackbarView.setBackground(fundo);
        snackbarView.setElevation(dp(snackbarView, 6));
        snackbar.setTextColor(ERROR_TEXT);

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

        card.setStrokeColor(ERROR);
        card.setStrokeWidth(dp(card, 2));
        card.setCardElevation(dp(card, 4));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            card.setOutlineAmbientShadowColor(ERROR);
            card.setOutlineSpotShadowColor(ERROR);
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
        fundo.setColor(ERROR_BACKGROUND);
        fundo.setCornerRadius(dp(view, 9));
        fundo.setStroke(dp(view, 1), ERROR);
        return fundo;
    }

    private static int dp(View view, int dp) {
        return Math.round(dp * view.getResources().getDisplayMetrics().density);
    }
}
