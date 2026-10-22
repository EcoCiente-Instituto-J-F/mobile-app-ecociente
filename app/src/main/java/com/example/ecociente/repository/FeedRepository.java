package com.example.ecociente.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import com.example.ecociente.model.PostagemFeed;
import java.util.Collections;
import java.util.List;

// Feed de fotos do condomínio, vindo de GET /api/postagens da ds-mobile-api.
public class FeedRepository {

    public interface Retorno {
        void aoConcluir(@NonNull List<PostagemFeed> postagens);

        // A API não respondeu ou o usuário não tem condomínio.
        default void aoFalhar() {
            aoConcluir(Collections.emptyList());
        }
    }

    public void listar(@NonNull Context contexto, @NonNull Retorno retorno) {

        Context contextoApp = contexto.getApplicationContext();

        int condominioId = PerfilApiRepository.condominioId(contextoApp);

        if (condominioId <= 0) {
            retorno.aoFalhar();
            return;
        }

        new PostagemRepository()
                .feed(
                        contextoApp,
                        condominioId,
                        postagens -> {
                            if (postagens != null) {
                                retorno.aoConcluir(postagens);
                            } else {
                                retorno.aoFalhar();
                            }
                        });
    }
}
