package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import com.example.ecociente.model.ResumoDashboard;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.model.StatusAgendamento;
import com.example.ecociente.ui.FormatoDataApi;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

// Calcula o resumo do dashboard a partir de GET /api/postagens e das coletas do calendário.
final class DashboardSindicoJson {

    private static final long DIA_MS = 24L * 60 * 60 * 1000;
    private static final int SEMANAS = 4;
    private static final int MAXIMO_DE_ITENS = 4;

    private DashboardSindicoJson() {}

    @NonNull
    static ResumoDashboard resumir(
            @NonNull JSONObject envelope,
            @NonNull List<Solicitacao> coletas,
            @NonNull Map<Integer, String> nomesDasCooperativas,
            long agoraMs) {

        int descartes = 0;
        Set<Integer> moradores = new HashSet<>();
        int[] porSemana = new int[SEMANAS];
        Map<String, Integer> porMaterial = new LinkedHashMap<>();

        JSONArray itens = envelope.optJSONArray("postagens");

        for (int i = 0; itens != null && i < itens.length(); i++) {
            JSONObject item = itens.optJSONObject(i);

            if (item == null || "reprovada".equals(item.optString("statusValidacao", ""))) {
                continue;
            }

            Calendar data = FormatoDataApi.calendario(item.optString("dataPostagem", null));

            if (data == null) {
                continue;
            }

            long idade = agoraMs - data.getTimeInMillis();

            if (idade < 0 || idade >= SEMANAS * 7 * DIA_MS) {
                continue;
            }

            porSemana[SEMANAS - 1 - (int) (idade / (7 * DIA_MS))]++;

            if (idade >= 30 * DIA_MS) {
                continue;
            }

            descartes++;
            moradores.add(item.optInt("usuarioId", 0));

            String categoria = item.optString("categoria", "").trim();
            porMaterial.merge(categoria.isEmpty() ? "Outros" : categoria, 1, Integer::sum);
        }

        List<Map.Entry<String, Integer>> materiais = new ArrayList<>(porMaterial.entrySet());
        materiais.sort((a, b) -> b.getValue() - a.getValue());

        int quantidade = Math.min(materiais.size(), MAXIMO_DE_ITENS);
        int resto = 0;

        for (int i = quantidade; i < materiais.size(); i++) {
            resto += materiais.get(i).getValue();
        }

        boolean temOutros = resto > 0;
        String[] nomesMateriais = new String[quantidade + (temOutros ? 1 : 0)];
        int[] percentuais = new int[nomesMateriais.length];
        int somados = 0;

        for (int i = 0; i < nomesMateriais.length; i++) {
            boolean ultimo = i == nomesMateriais.length - 1;
            nomesMateriais[i] = i < quantidade ? materiais.get(i).getKey() : "Outros";
            int total = i < quantidade ? materiais.get(i).getValue() : resto;
            // O último ocupa o que sobrou para a soma fechar em 100.
            percentuais[i] = ultimo ? 100 - somados : Math.round(total * 100f / descartes);
            somados += percentuais[i];
        }

        int realizadas = 0;
        Map<Integer, Integer> porCooperativa = new LinkedHashMap<>();

        // O nome que a própria coleta traz vale mais que a lista de cooperativas.
        Map<Integer, String> nomes = new java.util.HashMap<>(nomesDasCooperativas);

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

        List<Map.Entry<Integer, Integer>> cooperativas = new ArrayList<>(porCooperativa.entrySet());
        cooperativas.sort((a, b) -> b.getValue() - a.getValue());

        int mostradas = Math.min(cooperativas.size(), MAXIMO_DE_ITENS);
        String[] nomesCooperativas = new String[mostradas];
        int[] coletasPorCooperativa = new int[mostradas];

        for (int i = 0; i < mostradas; i++) {
            Map.Entry<Integer, Integer> entrada = cooperativas.get(i);
            String nome = nomes.get(entrada.getKey());
            nomesCooperativas[i] = abreviar(nome == null ? "Coop. " + entrada.getKey() : nome);
            coletasPorCooperativa[i] = entrada.getValue();
        }

        return new ResumoDashboard(
                descartes, moradores.size(), realizadas, porSemana, nomesMateriais, percentuais,
                nomesCooperativas, coletasPorCooperativa);
    }

    // Cabe sob a barra do gráfico: até 12 caracteres.
    @NonNull
    static String abreviar(@NonNull String nome) {
        String limpo = nome.trim();
        return limpo.length() <= 12 ? limpo : limpo.substring(0, 11).trim() + "…";
    }
}
