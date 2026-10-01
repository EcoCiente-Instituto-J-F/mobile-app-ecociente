package com.example.ecociente.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.MotionEvent;
import android.view.View;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import java.util.ArrayList;
import java.util.List;

/** Pequena linguagem de movimento compartilhada pelo EcoCiente. */
public final class Motion {
    public static final long MICRO_MS = 120;
    public static final long STATE_MS = 220;
    public static final long ENTER_MS = 320;
    private final List<Animator> running = new ArrayList<>();

    public void fadeUp(View view, long delay) {
        if (view == null) return;
        float distance = 14 * view.getResources().getDisplayMetrics().density;
        view.setAlpha(0f);
        view.setTranslationY(distance);
        AnimatorSet animation = new AnimatorSet();
        animation.playTogether(ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, distance, 0f));
        animation.setStartDelay(delay);
        animation.setDuration(ENTER_MS);
        animation.setInterpolator(new FastOutSlowInInterpolator());
        track(animation).start();
    }

    public void staggerIn(View... views) {
        int index = 0;
        for (View view : views) {
            if (view != null && view.getVisibility() == View.VISIBLE) fadeUp(view, Math.min(index++, 7) * 48L);
        }
    }

    public void shake(View view) {
        if (view == null) return;
        float d = 7 * view.getResources().getDisplayMetrics().density;
        ObjectAnimator animation = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0f, -d, d, -d * .6f, d * .35f, 0f);
        animation.setDuration(300);
        track(animation).start();
    }

    public static void pressFeedback(View view) {
        view.setOnTouchListener((v, event) -> {
            if (!v.isEnabled()) return false;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(.98f).scaleY(.98f).setDuration(90).setInterpolator(new FastOutSlowInInterpolator()).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1f).scaleY(1f).setDuration(160).setInterpolator(new FastOutSlowInInterpolator()).start();
            }
            return false;
        });
    }

    public void cancelAll() {
        for (Animator animator : new ArrayList<>(running)) animator.cancel();
        running.clear();
    }

    private <T extends Animator> T track(T animator) {
        running.add(animator);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) { running.remove(animation); }
            @Override public void onAnimationCancel(Animator animation) { running.remove(animation); }
        });
        return animator;
    }
}
