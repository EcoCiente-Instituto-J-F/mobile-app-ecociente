package com.example.ecociente.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import com.example.ecociente.model.DadosDemonstracao;
import com.example.ecociente.model.PostagemFeed;
import java.util.ArrayList;
import java.util.List;

// Feed de fotos do condomínio.
// API: GET /api/v1/postagens?condominioId=&pagina= (ds-pontuacao-api tem a tabela e a entidade
// de postagens, mas ainda não expõe a listagem; precisa devolver url_foto, autor, categoria e status).
public class FeedRepository {

    public interface Retorno {
        void aoConcluir(@NonNull List<PostagemFeed> postagens);
    }

    public void listar(@NonNull Context contexto, @NonNull Retorno retorno) {

        List<PostagemFeed> postagens = new ArrayList<>();

        if (DadosDemonstracao.ATIVO) {
            String raiz = "android.resource://" + contexto.getPackageName() + "/drawable/";

            postagens.add(new PostagemFeed(1, "Rafael Mendes", raiz + "sindico_reciclagem", "Plástico", "Aprovada · +20 pts", "Hoje, 08:40"));
            postagens.add(new PostagemFeed(2, "Ana Paula Souza", raiz + "ilustracao_coleta_reciclavel", "Papel", "Em análise", "Hoje, 07:15"));
            postagens.add(new PostagemFeed(3, "Marina Alves", raiz + "sindico_caminhao_coleta", "Vidro", "Aprovada · +15 pts", "Ontem, 18:30"));
            postagens.add(new PostagemFeed(4, "Carlos Eduardo Lima", raiz + "sindico_reciclagem", "Metal", "Aprovada · +25 pts", "Seg, 10:05"));
        }

        retorno.aoConcluir(postagens);
    }
}
