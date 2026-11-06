package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.text.Normalizer;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

// A Eko da demo: respostas curtas por palavra-chave, com o saldo, o ranking e a próxima coleta do estado atual. Texto puro, sem markdown: o chat do app mostra o que vier.
public final class RespostasEko {

    private static final String PADRAO =
            "Sou a Eko, sua assistente de reciclagem! Posso ajudar com o descarte de pilha, óleo e vidro, "
                    + "com a próxima coleta, seus pontos e sua posição no ranking. O que você quer saber?";

    private RespostasEko() {}

    @NonNull
    public static String responder(@Nullable String mensagem, @NonNull EstadoDemo estado) {
        String texto = normalizar(mensagem);

        if (texto.contains("ranking") || texto.contains("posicao")) {
            int posicao = estado.posicaoDoUsuario();

            return posicao > 0
                    ? "Você está em " + posicao + "º lugar no ranking do condomínio. Descartando mais, você sobe!"
                    : "Você ainda não aparece no ranking. Faça um descarte para entrar na disputa!";
        }

        if (texto.contains("ponto")) {
            return "Você tem " + estado.saldo() + " pontos. Descartes aprovados e quizzes rendem mais pontos!";
        }

        if (texto.contains("pilha") || texto.contains("bateria")) {
            return "Pilhas e baterias não vão no lixo comum. Leve a um ponto de coleta específico, "
                    + "como os coletores de mercados e lojas de eletrônicos, ou separe para a coleta do condomínio.";
        }

        if (texto.contains("oleo")) {
            return "Guarde o óleo de cozinha usado numa garrafa PET bem fechada e entregue na coleta do condomínio. "
                    + "Nunca jogue na pia: ele entope o encanamento e polui a água.";
        }

        if (texto.contains("vidro")) {
            return "Vidro limpo vai na reciclagem. Se estiver quebrado, embrulhe em jornal e avise com uma etiqueta "
                    + "para proteger quem manuseia. Espelhos e cerâmica não são recicláveis.";
        }

        if (texto.contains("coleta")) {
            return proximaColeta(estado);
        }

        return PADRAO;
    }

    // Sem acento, em minúsculas.
    @NonNull
    private static String normalizar(@Nullable String texto) {
        if (texto == null) {
            return "";
        }

        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }

    @NonNull
    private static String proximaColeta(@NonNull EstadoDemo estado) {
        String agora = SementeDemo.formatarData(estado.agora());
        String proxima = null;
        JSONArray lista;

        synchronized (estado) {
            lista = estado.raiz().optJSONArray("agendamentos");

            for (int i = 0; lista != null && i < lista.length(); i++) {
                JSONObject item = lista.optJSONObject(i);

                if (item == null) {
                    continue;
                }

                String status = item.optString("statusAgendamento", "");
                String inicio = item.optString("dataInicio", "");

                if (("AGENDADO".equals(status) || "CONFIRMADO".equals(status))
                        && inicio.length() >= 16
                        && inicio.compareTo(agora) > 0
                        && (proxima == null || inicio.compareTo(proxima) < 0)) {
                    proxima = inicio;
                }
            }
        }

        if (proxima == null) {
            return "Não há coleta agendada por enquanto. Assim que a cooperativa marcar a próxima, ela aparece no calendário.";
        }

        // yyyy-MM-dd'T'HH:mm:ss
        return "A próxima coleta é em " + proxima.substring(8, 10) + "/" + proxima.substring(5, 7)
                + ", a partir das " + proxima.substring(11, 16) + ". Deixe seus recicláveis separados e limpos!";
    }
}
