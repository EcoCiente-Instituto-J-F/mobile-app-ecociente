package com.example.ecociente.model;

import androidx.annotation.NonNull;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// Filtro e ordem do guia, aplicados sobre as aulas que a API já devolveu: a API não manda data
// nem leituras, então a ordem é a do curso ou alfabética, e o tempo de leitura é estimado.
public final class FiltroGuia {

    public static final String ROTULO_CONTEUDO = "Conteúdo";

    private static final int PALAVRAS_POR_MINUTO = 200;

    public enum Ordem {
        ORDEM_DO_CURSO,
        A_Z
    }

    public enum Tempo {
        ATE_3,
        DE_3_A_6,
        MAIS_DE_6;

        @NonNull
        static Tempo de(int minutos) {

            if (minutos <= 3) {
                return ATE_3;
            }

            return minutos <= 6 ? DE_3_A_6 : MAIS_DE_6;
        }
    }

    private final Set<String> categorias = new LinkedHashSet<>();
    private final Set<Tempo> tempos = EnumSet.noneOf(Tempo.class);
    private Ordem ordem = Ordem.ORDEM_DO_CURSO;

    @NonNull
    public Set<String> getCategorias() {
        return categorias;
    }

    @NonNull
    public Set<Tempo> getTempos() {
        return tempos;
    }

    @NonNull
    public Ordem getOrdem() {
        return ordem;
    }

    public void setOrdem(@NonNull Ordem ordem) {
        this.ordem = ordem;
    }

    public boolean ativo() {
        return !categorias.isEmpty() || !tempos.isEmpty() || ordem != Ordem.ORDEM_DO_CURSO;
    }

    public void limpar() {
        categorias.clear();
        tempos.clear();
        ordem = Ordem.ORDEM_DO_CURSO;
    }

    // Minutos para ler o texto (200 palavras por minuto), no mínimo 1.
    public static int minutosDeLeitura(@NonNull String conteudo) {

        String texto = conteudo.trim();

        if (texto.isEmpty()) {
            return 1;
        }

        int palavras = texto.split("\\s+").length;

        return Math.max(1, (palavras + PALAVRAS_POR_MINUTO - 1) / PALAVRAS_POR_MINUTO);
    }

    // Texto da aula guardado nos detalhes do item; vazio se não houver.
    @NonNull
    public static String conteudoDe(@NonNull ItemLista item) {

        if (item.detalhes != null) {
            for (String[] campo : item.detalhes) {
                if (campo.length > 1 && ROTULO_CONTEUDO.equals(campo[0])) {
                    return campo[1];
                }
            }
        }

        return "";
    }

    // Os cursos (categorias) presentes na lista, na ordem em que aparecem.
    @NonNull
    public static List<String> categoriasDe(@NonNull List<ItemLista> itens) {

        Set<String> categorias = new LinkedHashSet<>();

        for (ItemLista item : itens) {
            if (!item.subtitulo.isEmpty()) {
                categorias.add(item.subtitulo);
            }
        }

        return new ArrayList<>(categorias);
    }

    @NonNull
    public List<ItemLista> aplicar(@NonNull List<ItemLista> itens) {

        List<ItemLista> resultado = new ArrayList<>();

        for (ItemLista item : itens) {

            if (!categorias.isEmpty() && !categorias.contains(item.subtitulo)) {
                continue;
            }

            if (!tempos.isEmpty() && !tempos.contains(Tempo.de(minutosDeLeitura(conteudoDe(item))))) {
                continue;
            }

            resultado.add(item);
        }

        if (ordem == Ordem.A_Z) {
            Collator collator = Collator.getInstance(new Locale("pt", "BR"));

            Collections.sort(resultado, (a, b) -> collator.compare(a.titulo, b.titulo));
        }

        return resultado;
    }
}
