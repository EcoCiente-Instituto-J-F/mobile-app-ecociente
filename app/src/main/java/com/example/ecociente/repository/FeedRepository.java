package com.example.ecociente.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import com.example.ecociente.model.DadosDemonstracao;
import com.example.ecociente.model.PostagemFeed;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Feed de fotos do condomínio, vindo de GET /api/postagens da ds-mobile-api.
// Sem resposta da API, mostra o exemplo só enquanto a demonstração estiver ligada.
public class FeedRepository {

    public interface Retorno {
        void aoConcluir(@NonNull List<PostagemFeed> postagens);

        // A API não respondeu e não há exemplo para mostrar.
        default void aoFalhar() {
            aoConcluir(Collections.emptyList());
        }
    }

    public void listar(@NonNull Context contexto, @NonNull Retorno retorno) {

        Context contextoApp = contexto.getApplicationContext();

        new PostagemRepository()
                .feed(
                        contextoApp,
                        PerfilApiRepository.condominioId(contextoApp),
                        postagens -> {
                            if (postagens != null) {
                                retorno.aoConcluir(postagens);
                            } else if (DadosDemonstracao.ATIVO) {
                                retorno.aoConcluir(exemplos(contextoApp));
                            } else {
                                retorno.aoFalhar();
                            }
                        });
    }

    @NonNull
    private List<PostagemFeed> exemplos(@NonNull Context contexto) {

        List<PostagemFeed> postagens = new ArrayList<>();

        String raiz = "android.resource://" + contexto.getPackageName() + "/drawable/";

        postagens.add(new PostagemFeed(1, "Rafael Mendes", raiz + "sindico_reciclagem", "Plástico", "Aprovada · +20 pts", "Hoje, 08:40"));
        postagens.add(new PostagemFeed(2, "Ana Paula Souza", raiz + "ilustracao_coleta_reciclavel", "Papel", "Em análise", "Hoje, 07:15"));
        postagens.add(new PostagemFeed(3, "Marina Alves", raiz + "sindico_caminhao_coleta", "Vidro", "Aprovada · +15 pts", "Ontem, 18:30"));
        postagens.add(new PostagemFeed(4, "Carlos Eduardo Lima", raiz + "sindico_reciclagem", "Metal", "Aprovada · +25 pts", "Seg, 10:05"));

        return postagens;
    }
}
