package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import com.example.ecociente.model.ResumoDashboard;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.model.StatusAgendamento;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

// Monta o resumo do dashboard: a resposta de GET /api/condominios/{id}/dashboard (ds-mobile-api) mais as coletas realizadas do calendário, que a view não traz.
final class DashboardSindicoJson {

    private static final int MAXIMO_DE_ITENS = 4;

    private DashboardSindicoJson() {}

    @NonNull
    static ResumoDashboard resumir(
            @NonNull JSONObject dashboard,
            @NonNull List<Solicitacao> coletas,
            @NonNull Map<Integer, String> nomesDasCooperativas) {

        JSONObject materiais = dashboard.optJSONObject("materiais");
        JSONObject pontos = dashboard.optJSONObject("pontos");
        JSONObject quizzes = dashboard.optJSONObject("quizzes");

        JSONArray categorias = materiais == null ? null : materiais.optJSONArray("porCategoria");
        JSONArray porQuiz = quizzes == null ? null : quizzes.optJSONArray("porQuiz");

        // materiais por categoria: os maiores e, somados, "Outros"
        int totalDeCategorias = categorias == null ? 0 : categorias.length();
        int mostradas = Math.min(totalDeCategorias, MAXIMO_DE_ITENS);
        long resto = 0;
        long soma = 0;
        long[] quantidades = new long[totalDeCategorias];

        for (int i = 0; i < totalDeCategorias; i++) {
            quantidades[i] = categorias.optJSONObject(i).optLong("quantidade", 0);
            soma += quantidades[i];

            if (i >= mostradas) {
                resto += quantidades[i];
            }
        }

        boolean temOutros = resto > 0;
        String[] nomesMateriais = new String[mostradas + (temOutros ? 1 : 0)];
        int[] percentuais = new int[nomesMateriais.length];
        int somados = 0;

        for (int i = 0; i < nomesMateriais.length; i++) {
            boolean ultimo = i == nomesMateriais.length - 1;
            nomesMateriais[i] = i < mostradas ? categorias.optJSONObject(i).optString("categoria", "") : "Outros";
            long quantidade = i < mostradas ? quantidades[i] : resto;
            // O último ocupa o que sobrou para a soma fechar em 100.
            percentuais[i] = ultimo ? 100 - somados : Math.round(quantidade * 100f / soma);
            somados += percentuais[i];
        }

        // notas médias dos quizzes
        int quantidadeDeQuizzes = porQuiz == null ? 0 : Math.min(porQuiz.length(), MAXIMO_DE_ITENS);
        String[] nomesQuizzes = new String[quantidadeDeQuizzes];
        int[] notas = new int[quantidadeDeQuizzes];

        for (int i = 0; i < quantidadeDeQuizzes; i++) {
            JSONObject quiz = porQuiz.optJSONObject(i);
            nomesQuizzes[i] = abreviar(quiz.optString("quiz", ""));
            notas[i] = (int) Math.round(quiz.optDouble("notaMedia", 0));
        }

        // coletas realizadas por cooperativa; o nome que a própria coleta traz vale mais que a lista
        Map<Integer, String> nomes = new HashMap<>(nomesDasCooperativas);
        int realizadas = 0;
        Map<Integer, Integer> porCooperativa = new LinkedHashMap<>();

        for (Solicitacao coleta : coletas) {
            String nomeDaColeta = coleta.getCooperativaNome();

            if (nomeDaColeta != null && !nomeDaColeta.isEmpty()) {
                nomes.put(coleta.getCooperativaId(), nomeDaColeta);
            }

            if (coleta.getStatus() == StatusAgendamento.REALIZADO) {
                realizadas++;
                porCooperativa.merge(coleta.getCooperativaId(), 1, Integer::sum);
            }
        }

        List<Map.Entry<Integer, Integer>> ordenadas = new ArrayList<>(porCooperativa.entrySet());
        ordenadas.sort((a, b) -> b.getValue() - a.getValue());

        int cooperativasMostradas = Math.min(ordenadas.size(), MAXIMO_DE_ITENS);
        String[] nomesCooperativas = new String[cooperativasMostradas];
        int[] coletasPorCooperativa = new int[cooperativasMostradas];

        for (int i = 0; i < cooperativasMostradas; i++) {
            Map.Entry<Integer, Integer> entrada = ordenadas.get(i);
            String nome = nomes.get(entrada.getKey());
            nomesCooperativas[i] = abreviar(nome == null ? "Coop. " + entrada.getKey() : nome);
            coletasPorCooperativa[i] = entrada.getValue();
        }

        return new ResumoDashboard(
                materiais == null ? 0 : materiais.optLong("reciclados", 0),
                dashboard.optLong("totalMoradores", 0),
                pontos == null ? 0 : pontos.optLong("mesAtual", 0),
                realizadas,
                nomesQuizzes,
                notas,
                nomesMateriais,
                percentuais,
                nomesCooperativas,
                coletasPorCooperativa);
    }

    // Cabe sob a barra do gráfico: até 12 caracteres.
    @NonNull
    static String abreviar(@NonNull String nome) {
        String limpo = nome.trim();
        return limpo.length() <= 12 ? limpo : limpo.substring(0, 11).trim() + "…";
    }
}
