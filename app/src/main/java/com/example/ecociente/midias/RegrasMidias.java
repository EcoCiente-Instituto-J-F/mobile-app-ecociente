package com.example.ecociente.midias;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Motor puro Java: nenhum Android, rede, pontuacao ou gabarito. Testavel com javac. */
public final class RegrasMidias {
    public static final int TOPO_ESQUERDO = 0, TOPO_DIREITO = 1, ACIMA_BOTAO = 2;
    private final List<Midia> catalogo;
    private final Map<String, List<String>> sinonimos;

    public RegrasMidias(List<Midia> midias, Map<String, List<String>> sinonimos) {
        catalogo = new ArrayList<>(midias);
        catalogo.sort(Comparator.comparing(m -> m.id));
        this.sinonimos = new LinkedHashMap<>(sinonimos);
    }

    public static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ").trim().replaceAll("\\s+", " ");
    }

    private static boolean contemTermo(String texto, String termo) {
        return (" " + texto + " ").contains(" " + normalizar(termo) + " ");
    }

    /** O titulo pesa mais. Sinonimos sao palavras/expressoes inteiras, nao substrings. */
    public String tema(String titulo, String contexto) {
        String principal = normalizar(titulo), apoio = normalizar(contexto);
        // Desambiguacoes basicas; curadoria/associacao explicita continua sendo preferivel.
        principal = principal.replace("papel das", "funcao das").replace("papel dos", "funcao dos")
                .replace("papel da", "funcao da").replace("papel do", "funcao do");
        apoio = apoio.replace("papel das", "funcao das").replace("papel dos", "funcao dos")
                .replace("papel da", "funcao da").replace("papel do", "funcao do");
        String melhor = "geral";
        int maior = 0;
        for (Map.Entry<String, List<String>> grupo : sinonimos.entrySet()) {
            int nota = 0;
            List<String> termos = new ArrayList<>(grupo.getValue());
            termos.add(grupo.getKey());
            for (String termo : termos) {
                int especificidade = normalizar(termo).split(" ").length;
                if (contemTermo(principal, termo)) nota = Math.max(nota, 100 + especificidade * 5);
                if (contemTermo(apoio, termo)) nota = Math.max(nota, 10 + especificidade);
            }
            if (nota > maior) { maior = nota; melhor = grupo.getKey(); }
        }
        return melhor;
    }

    public Midia porId(String id) {
        for (Midia midia : catalogo) if (midia.id.equals(id)) return midia;
        return null;
    }

    private List<Midia> candidatos(String tipo, String tema, Set<String> excluirGrupos) {
        List<Midia> exatos = new ArrayList<>(), neutros = new ArrayList<>();
        for (Midia m : catalogo) {
            if (!tipo.equals(m.tipo) || excluirGrupos.contains(m.grupoImagem)) continue;
            if (tema.equals(m.tema)) exatos.add(m);
            else if ("geral".equals(m.tema)) neutros.add(m);
        }
        return exatos.isEmpty() ? neutros : exatos;
    }

    /** Distribui as artes disponiveis sem substituir o vinculo de entidades ja existentes. */
    public Midia escolherFixa(String tipo, String tema, String chave, Map<String, String> vinculos) {
        Midia salva = porId(vinculos.get(chave));
        if (salva != null && tipo.equals(salva.tipo)) return salva;
        List<Midia> candidatas = candidatos(tipo, tema, Collections.emptySet());
        if (candidatas.isEmpty()) return null;
        int menorUso = Integer.MAX_VALUE;
        List<Midia> empatadas = new ArrayList<>();
        for (Midia m : candidatas) {
            int uso = 0;
            for (Map.Entry<String, String> vinculo : vinculos.entrySet()) {
                if (vinculo.getKey().startsWith(tipo + ":") && m.id.equals(vinculo.getValue())) uso++;
            }
            if (uso < menorUso) { menorUso = uso; empatadas.clear(); }
            if (uso == menorUso) empatadas.add(m);
        }
        return empatadas.get(Math.floorMod(chave.hashCode(), empatadas.size()));
    }

    public List<Midia> fotosSemRepetir(List<String> temas, long semente) {
        List<Midia> resultado = new ArrayList<>();
        Set<String> usadas = new HashSet<>();
        Random sorteio = new Random(semente);
        for (String tema : temas) {
            List<Midia> candidatas = candidatos(Midia.FOTO_CONTEUDO, tema, usadas);
            Midia escolhida = candidatas.isEmpty() ? null : candidatas.get(sorteio.nextInt(candidatas.size()));
            resultado.add(escolhida);
            if (escolhida != null) usadas.add(escolhida.grupoImagem);
        }
        // Acervo insuficiente => null, NUNCA repete uma foto nem troca por um icone.
        return resultado;
    }

    public Midia decoracaoLeitura(String tema, long semente, double chance, Set<String> usadas) {
        Random sorteio = new Random(semente);
        if (sorteio.nextDouble() >= Math.max(0, Math.min(1, chance))) return null;
        List<Midia> candidatas = candidatos(Midia.DECORACAO_CONTEUDO, tema, usadas);
        return candidatas.isEmpty() ? null : candidatas.get(sorteio.nextInt(candidatas.size()));
    }

    public Midia ekoLeitura(String tema, long semente) {
        List<Midia> candidatas = candidatos(Midia.EKO_LEITURA, tema, Collections.emptySet());
        List<Midia> animados = new ArrayList<>();
        for (Midia m : candidatas) if (m.ehGifEko()) animados.add(m);
        if (!animados.isEmpty()) candidatas = animados;
        return candidatas.isEmpty() ? null : candidatas.get(new Random(semente).nextInt(candidatas.size()));
    }

    public static final class DecoracaoQuiz {
        public final int posicao;
        public final Midia midia;
        public DecoracaoQuiz(int posicao, Midia midia) { this.posicao = posicao; this.midia = midia; }
    }

    /** Uma decoracao por pergunta. Nao recebe enunciado, opcoes nem resposta correta. */
    public DecoracaoQuiz decoracaoQuiz(long semente, int indice) {
        List<Midia> todas = new ArrayList<>(), gifsEko = new ArrayList<>();
        for (Midia m : catalogo) {
            if (!Midia.DECORACAO_QUIZ.equals(m.tipo) || !m.neutra) continue;
            todas.add(m);
            if (m.ehGifEko()) gifsEko.add(m);
        }
        Random sorteio = new Random(semente ^ (0x9E3779B97F4A7C15L * (indice + 1L)));
        int posicao = sorteio.nextInt(gifsEko.isEmpty() ? 2 : 3);
        List<Midia> candidatas = posicao == ACIMA_BOTAO ? gifsEko : todas;
        Midia m = candidatas.isEmpty() ? null : candidatas.get(sorteio.nextInt(candidatas.size()));
        return new DecoracaoQuiz(posicao, m);
    }
}
