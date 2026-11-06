package com.example.ecociente.demo;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class RespostasEko {
    private static volatile JSONArray faq = new JSONArray();
    private static volatile JSONArray educacao = new JSONArray();
    private static final Set<String> VAZIAS = new HashSet<>(Arrays.asList(
            "como", "qual", "quais", "quem", "onde", "quando", "porque", "por", "que", "sera", "oque",
            "isso", "este", "essa", "esse", "esta", "estou", "estao", "voce", "vocês", "minha",
            "meu", "seus", "suas", "tem", "tenho", "posso", "pode", "devo", "preciso", "quero",
            "saber", "sobre", "para", "com", "sem", "uma", "umas", "uns", "dos", "das", "por",
            "nao", "mais", "muito", "algum", "alguma", "fazer", "ser", "ter", "aqui", "la",
            "temos", "poderia", "favor", "explica", "explique", "sobre", "dentro", "fora"));

    private static final Map<String, String> SINONIMOS = new HashMap<>();
    static {
        SINONIMOS.put("lixo", "residuo");
        SINONIMOS.put("lixos", "residuo");
        SINONIMOS.put("residuos", "residuo");
        SINONIMOS.put("reciclar", "reciclagem");
        SINONIMOS.put("reciclaveis", "reciclagem");
        SINONIMOS.put("reciclavel", "reciclagem");
        SINONIMOS.put("coletar", "coleta");
        SINONIMOS.put("coletado", "coleta");
        SINONIMOS.put("compostar", "compostagem");
        SINONIMOS.put("composteira", "compostagem");
        SINONIMOS.put("pilhas", "pilha");
        SINONIMOS.put("baterias", "bateria");
        SINONIMOS.put("plasticos", "plastico");
        SINONIMOS.put("garrafas", "garrafa");
        SINONIMOS.put("oceanos", "oceano");
        SINONIMOS.put("metais", "metal");
        SINONIMOS.put("papeis", "papel");
        SINONIMOS.put("pontuacoes", "pontuacao");
        SINONIMOS.put("pontuacao", "ponto");
    }

    private RespostasEko() {}

    public static synchronized void inicializar(@NonNull Context contexto) {
        try {
            faq = new JSONArray(ler(contexto, "mock/eko/faq.json"));
            educacao = new JSONArray(ler(contexto, "mock/eko/indice_educacional.json"));
            if (faq.length() != 64 || educacao.length() != 531) {
                throw new IllegalStateException("Arquivos da Eko incompletos");
            }
        } catch (IOException | JSONException erro) {
            throw new IllegalStateException("Nao foi possivel carregar a Eko local", erro);
        }
    }

    @NonNull
    private static String ler(Context contexto, String caminho) throws IOException {
        try (InputStream entrada = contexto.getAssets().open(caminho);
             ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = entrada.read(buffer)) != -1) saida.write(buffer, 0, n);
            return new String(saida.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    @NonNull
    public static String responder(@Nullable String mensagem, @NonNull EstadoDemo estado) {
        String pergunta = normalizar(mensagem);
        if (pergunta.isEmpty()) {
            return "Olá! Sou a Eko em modo offline. Posso explicar sustentabilidade, reciclagem, cursos, quizzes, pontos e coletas. O que você gostaria de saber?";
        }
        JSONObject raiz = estado.raiz();
        String perfil = ContaDemo.me(raiz).optString("perfil", "USUARIO_COMUM");
        if (pergunta.contains("ranking") || pergunta.contains("posicao no ranking")) {
            if (UsuariosMultiperfilDemo.condominioId(raiz) <= 0) {
                return "Sua conta não está vinculada a um condomínio com ranking de moradores.";
            }
            int posicao = estado.posicaoDoUsuario();
            return posicao > 0 ? "Você está em " + posicao + "º lugar no ranking do condomínio. Continue participando!" :
                    "Você ainda não aparece no ranking. Participe das ações sustentáveis para pontuar.";
        }
        if (pergunta.contains("proxima coleta") || pergunta.contains("coleta hoje") ||
                pergunta.contains("quando") && pergunta.contains("coleta") ||
                pergunta.contains("calendario") && pergunta.contains("coleta")) {
            return proximaColeta(estado);
        }
        if ((pergunta.contains("ponto") || pergunta.contains("saldo")) &&
                (pergunta.contains("quantos") || pergunta.contains("tenho") || pergunta.contains("saldo") ||
                 pergunta.contains("meus ponto"))) {
            return "Você tem " + estado.saldo() + " pontos no modo de demonstração. O saldo muda conforme suas ações locais.";
        }
        if (pergunta.contains("ponto de coleta") || pergunta.contains("ecoponto") ||
                pergunta.contains("cooperativa proxima")) {
            return "No modo offline, o mapa usa pontos de coleta demonstrativos. Abra o mapa no EcoCiente para ver os pontos cadastrados. Não consigo confirmar distâncias ou funcionamento em tempo real.";
        }
        if (pergunta.contains("meu perfil") || pergunta.contains("tipo de usuario") ||
                pergunta.contains("que perfil")) {
            return "Seu perfil nesta demonstração é " + nomePerfil(perfil) + ". As informações são fictícias e ficam armazenadas no aparelho.";
        }
        if (pergunta.contains("pilha") || pergunta.contains("bateria")) {
            return "Pilhas e baterias precisam de coleta específica ou logística reversa. Armazene em local seco e protegido e entregue a um coletor apropriado, não no lixo comum.";
        }
        if (pergunta.contains("oleo") && (pergunta.contains("cozinha") || pergunta.contains("descart") || pergunta.contains("recicl"))) {
            return "Óleo de cozinha usado não deve ir para a pia. Armazene-o em recipiente bem fechado e leve a um ponto que receba óleo. Confira a aceitação antes da entrega.";
        }
        if (pergunta.contains("vidro") && (pergunta.contains("quebrado") || pergunta.contains("descart") || pergunta.contains("recicl"))) {
            return "Vidro reciclável deve ir limpo e de acordo com a coleta local. Se estiver quebrado, acondicione e identifique os cacos para proteger os coletores. Espelhos e cerâmica exigem orientação específica.";
        }

        Set<String> termos = termos(pergunta);
        double maior = 0;
        JSONObject escolhido = null;
        boolean respostaFaq = false;
        for (int i = 0; i < faq.length(); i++) {
            JSONObject item = faq.optJSONObject(i);
            if (item == null || !permite(item.optString("perfis", "Todos"), perfil)) continue;
            double valor = pontuar(termos, item.optString("pergunta"), 5) +
                    pontuar(termos, item.optString("palavrasChave"), 2);
            if (valor > maior) {
                maior = valor;
                escolhido = item;
                respostaFaq = true;
            }
        }
        for (int i = 0; i < educacao.length(); i++) {
            JSONObject item = educacao.optJSONObject(i);
            if (item == null) continue;
            double valor = pontuar(termos, item.optString("titulo"), 4) +
                    pontuar(termos, item.optString("texto"), 1);
            if (valor > maior) {
                maior = valor;
                escolhido = item;
                respostaFaq = false;
            }
        }
        if (escolhido != null && maior >= 4) {
            if (respostaFaq) {
                String resposta = escolhido.optString("resposta", "").trim();
                if (!resposta.isEmpty()) return resposta;
            } else {
                String assunto = escolhido.optString("titulo", "Sustentabilidade");
                String texto = escolhido.optString("texto", "").trim();
                if (!texto.isEmpty()) {
                    return "Nos conteúdos do EcoCiente, o tema \"" + assunto + "\" aborda: " + texto +
                            ". Você também pode abrir a Biblioteca para estudar o conteúdo completo.";
                }
            }
        }
        return "Não tenho uma resposta confiável sobre isso na minha base offline. Posso ajudar com reciclagem, sustentabilidade, compostagem, cursos, quizzes, coletas e informações da sua conta demonstrativa.";
    }

    private static boolean permite(String perfis, String perfil) {
        String f = normalizar(perfis);
        if (f.contains("todo") || f.isEmpty()) return true;
        if ("USUARIO_COMUM".equals(perfil)) return f.contains("comum") || f.contains("usuario");
        if ("MORADOR".equals(perfil)) return f.contains("morador") || f.contains("residencial");
        if ("SINDICO".equals(perfil)) return f.contains("sindico");
        return f.contains("cooperativa");
    }

    private static String nomePerfil(String perfil) {
        switch (perfil) {
            case "MORADOR": return "Morador";
            case "SINDICO": return "Síndico";
            case "COOPERATIVA": return "Cooperativa";
            default: return "Usuário Comum";
        }
    }

    private static double pontuar(Set<String> pergunta, String texto, int peso) {
        Set<String> tokens = termos(texto);
        double nota = 0;
        for (String item : pergunta) if (tokens.contains(item)) nota += peso;
        return nota;
    }

    private static Set<String> termos(String texto) {
        Set<String> saida = new HashSet<>();
        String[] tokens = normalizar(texto).split("[^a-z0-9]+");
        for (String t : tokens) {
            if (t.length() < 3 || VAZIAS.contains(t)) continue;
            saida.add(SINONIMOS.containsKey(t) ? SINONIMOS.get(t) : t);
        }
        return saida;
    }

    private static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).trim();
    }

    private static String proximaColeta(EstadoDemo estado) {
        String agora = SementeDemo.formatarData(estado.agora());
        String maisProxima = null;
        JSONArray lista = estado.raiz().optJSONArray("agendamentos");
        for (int i = 0; lista != null && i < lista.length(); i++) {
            JSONObject item = lista.optJSONObject(i);
            if (item == null) continue;
            String status = item.optString("statusAgendamento", "");
            String data = item.optString("dataInicio", "");
            if (("AGENDADO".equals(status) || "CONFIRMADO".equals(status))
                    && data.compareTo(agora) > 0 && (maisProxima == null || data.compareTo(maisProxima) < 0)) {
                maisProxima = data;
            }
        }
        if (maisProxima == null || maisProxima.length() < 16)
            return "Não há coleta futura cadastrada no calendário demonstrativo.";
        return "A próxima coleta é em " + maisProxima.substring(8, 10) + "/" +
                maisProxima.substring(5, 7) + ", a partir das " + maisProxima.substring(11, 16) + ".";
    }
}
