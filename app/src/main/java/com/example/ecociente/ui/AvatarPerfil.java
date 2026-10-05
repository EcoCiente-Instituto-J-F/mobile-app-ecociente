package com.example.ecociente.ui;

import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;

public final class AvatarPerfil {

    private AvatarPerfil() {}

    public static void exibir(@NonNull ImageView imagem, @Nullable String url) {

        if (url == null || url.isEmpty()) {
            return;
        }

        imagem.setPadding(0, 0, 0, 0);

        imagem.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(imagem).load(url).into(imagem);
    }
}
