package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Pontos do morador e ranking do condomínio dentro do JSON do estado (`pontuacao`). Quem chama já está sob `synchronized (estado)` e chama `estado.salvar()` depois.
final class PontuacaoDemo {

    static final String FORMATO_DATA = "yyyy-MM-dd'T'HH:mm:ss";

    private PontuacaoDemo() {}

    // Participante do ranking; o morador logado é o de `usuarioId` igual ao da conta e usa o nome do perfil (que ele pode editar).
    static final class Participante {
        final int usuarioId;
        final String nome;
        final int pontos;

        Participante(int usuarioId, String nome, int pontos) {
            this.usuarioId = usuarioId;
            this.nome = nome;
            this.pontos = pontos;
        }
    }

    @NonNull
    static JSONObject semente(long agora) throws JSONException {
        JSONArray participantes =
                new JSONArray()
                        .put(participante(0, "Rafael Mendes", 480))
                        .put(participante(0, "Camila Duarte", 410))
                        .put(participante(0, "Thiago Barros", 350))
                        .put(participante(SementeDemo.USUARIO_ID, "", 320))
                        .put(participante(0, "Letícia Prado", 180));

        // Do mais antigo ao mais novo; a API devolve o inverso. Somam os 320 do morador.
        JSONArray movimentacoes =
                new JSONArray()
                        .put(movimentacao("QUIZ", 150, agora, -6))
                        .put(movimentacao("POSTAGEM", 120, agora, -3))
                        .put(movimentacao("POSTAGEM", 50, agora, -1));

        return new JSONObject().put("participantes", participantes).put("movimentacoes", movimentacoes);
    }

    private static JSONObject participante(int usuarioId, String nome, int pontos) throws JSONException {
        return new JSONObject().put("usuarioId", usuarioId).put("nome", nome).put("pontos", pontos);
    }

    private static JSONObject movimentacao(String origem, int pontos, long agora, int diasAntes) throws JSONException {
        Calendar quando = Calendar.getInstance();
        quando.setTimeInMillis(agora);
        quando.add(Calendar.DAY_OF_MONTH, diasAntes);

        return movimentacao(origem, pontos, quando.getTimeInMillis());
    }

    private static JSONObject movimentacao(String origem, int pontos, long quando) throws JSONException {
        return new JSONObject()
                .put("origemTipo", origem)
                .put("tipoMovimentacao", "CREDITO")
                .put("pontos", pontos)
                .put("ocorridoEm", new SimpleDateFormat(FORMATO_DATA, Locale.US).format(new Date(quando)));
    }

    @NonNull
    private static JSONObject bloco(@NonNull JSONObject raiz) {
        JSONObject bloco = raiz.optJSONObject("pontuacao");

        if (bloco == null) {
            throw new IllegalStateException("Estado da demo sem pontuação");
        }

        return bloco;
    }

    // Ranking já ordenado: mais pontos primeiro; empate decidido pelo nome, para a ordem não mudar entre consultas.
    @NonNull
    static List<Participante> ranking(@NonNull JSONObject raiz) {
        String nomeDoUsuario = ContaDemo.me(raiz).optString("nome", "");
        JSONArray lista = bloco(raiz).optJSONArray("participantes");
        List<Participante> ranking = new ArrayList<>();

        for (int i = 0; lista != null && i < lista.length(); i++) {
            JSONObject item = lista.optJSONObject(i);

            if (item == null) {
                continue;
            }

            int id = item.optInt("usuarioId", 0);

            ranking.add(
                    new Participante(
                            id,
                            id == UsuariosMultiperfilDemo.usuarioId(raiz) ? nomeDoUsuario : item.optString("nome", ""),
                            item.optInt("pontos", 0)));
        }

        Collections.sort(
                ranking,
                new Comparator<Participante>() {
                    @Override
                    public int compare(Participante a, Participante b) {
                        if (a.pontos != b.pontos) {
                            return b.pontos > a.pontos ? 1 : -1;
                        }

                        int porNome = a.nome.compareToIgnoreCase(b.nome);

                        return porNome != 0 ? porNome : a.nome.compareTo(b.nome);
                    }
                });

        return ranking;
    }

    static int saldo(@NonNull JSONObject raiz) {
        for (Participante p : ranking(raiz)) {
            if (p.usuarioId == UsuariosMultiperfilDemo.usuarioId(raiz)) {
                return p.pontos;
            }
        }

        return 0;
    }

    // 1 para o primeiro; 0 se o usuário não está no ranking.
    static int posicaoDoUsuario(@NonNull JSONObject raiz) {
        List<Participante> ranking = ranking(raiz);

        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).usuarioId == UsuariosMultiperfilDemo.usuarioId(raiz)) {
                return i + 1;
            }
        }

        return 0;
    }

    static void creditar(@NonNull JSONObject raiz, @NonNull String origemTipo, int pontos, long agora) {
        if (!"POSTAGEM".equals(origemTipo) && !"QUIZ".equals(origemTipo)) {
            throw new IllegalArgumentException("Origem de pontos desconhecida: " + origemTipo);
        }

        if (pontos <= 0) {
            throw new IllegalArgumentException("Pontos devem ser positivos: " + pontos);
        }

        try {
            JSONObject bloco = bloco(raiz);
            JSONArray participantes = bloco.getJSONArray("participantes");

            for (int i = 0; i < participantes.length(); i++) {
                JSONObject item = participantes.getJSONObject(i);

                if (item.optInt("usuarioId", 0) == UsuariosMultiperfilDemo.usuarioId(raiz)) {
                    item.put("pontos", item.optInt("pontos", 0) + pontos);
                }
            }

            bloco.getJSONArray("movimentacoes").put(movimentacao(origemTipo, pontos, agora));

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    // Movimentações do mais recente ao mais antigo.
    @NonNull
    static List<JSONObject> movimentacoes(@NonNull JSONObject raiz) {
        JSONArray lista = bloco(raiz).optJSONArray("movimentacoes");
        List<JSONObject> saida = new ArrayList<>();

        for (int i = lista == null ? -1 : lista.length() - 1; i >= 0; i--) {
            JSONObject item = lista.optJSONObject(i);

            if (item != null) {
                saida.add(item);
            }
        }

        return saida;
    }
}
