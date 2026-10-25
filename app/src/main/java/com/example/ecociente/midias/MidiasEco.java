package com.example.ecociente.midias;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.ecociente.model.CursoEducativo;
import com.example.ecociente.model.FluxoEducativo;
import com.example.ecociente.model.QuizResumo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Fachada compartilhada: Biblioteca e detalhe consultam o MESMO vinculo por ID de curso. */
public final class MidiasEco {
    private static volatile MidiasEco instancia;
    private final CatalogoMidias catalogo;
    private final RegrasMidias regras;
    private final SharedPreferences preferencias;
    private final Map<String, String> vinculos = new LinkedHashMap<>();

    public static MidiasEco obter(Context contexto) {
        if (instancia == null) synchronized (MidiasEco.class) {
            if (instancia == null) instancia = new MidiasEco(contexto.getApplicationContext());
        }
        return instancia;
    }

    private MidiasEco(Context contexto) {
        catalogo = CatalogoMidias.carregar(contexto);
        regras = new RegrasMidias(catalogo.itens, catalogo.sinonimos);
        preferencias = contexto.getSharedPreferences("eco_midias_v1_" + ConfiguracaoMidias.AMBIENTE, Context.MODE_PRIVATE);
        for (Map.Entry<String, ?> entrada : preferencias.getAll().entrySet()) {
            if (entrada.getValue() instanceof String) vinculos.put(entrada.getKey(), (String) entrada.getValue());
        }
    }

    private synchronized Midia fixa(String tipo, String entidade, int id, String titulo, String contexto) {
        String chave = tipo + ":" + entidade + ":" + id;
        Midia explicita = regras.porId(catalogo.associacoes.get(chave));
        if (explicita != null && tipo.equals(explicita.tipo)) {
            guardar(chave, explicita);
            return explicita;
        }
        Midia m = regras.escolherFixa(tipo, regras.tema(titulo, contexto), chave, vinculos);
        if (m != null && !m.id.equals(vinculos.get(chave))) guardar(chave, m);
        return m;
    }

    private void guardar(String chave, Midia m) {
        vinculos.put(chave, m.id);
        preferencias.edit().putString(chave, m.id).apply();
    }

    public Midia iconeCurso(CursoEducativo curso) {
        return fixa(Midia.ICONE_CONTEUDO, "curso", curso.id, curso.titulo, curso.descricao);
    }

    public Midia iconeQuiz(int id, String titulo, String categoria) {
        return fixa(Midia.ICONE_QUIZ, "quiz", id, titulo, categoria);
    }

    public Midia capaQuiz(int id, String titulo, String categoria) {
        return fixa(Midia.CAPA_QUIZ, "quiz", id, titulo, categoria);
    }

    public void prepararCursos(List<CursoEducativo> cursos) {
        List<CursoEducativo> ordenados = new ArrayList<>(cursos);
        ordenados.sort(Comparator.comparingInt(c -> c.id));
        for (CursoEducativo curso : ordenados) iconeCurso(curso);
    }

    public void prepararQuizzes(List<QuizResumo> quizzes) {
        List<QuizResumo> ordenados = new ArrayList<>(quizzes);
        ordenados.sort(Comparator.comparingInt(QuizResumo::getQuizId));
        for (QuizResumo q : ordenados) {
            iconeQuiz(q.getQuizId(), q.getTitulo(), q.getCategoria());
            capaQuiz(q.getQuizId(), q.getTitulo(), q.getCategoria());
        }
    }

    public static final class PlanoLeitura {
        public final List<Midia> fotos, iconesTitulo, iconesTexto;
        public final Midia eko;
        private PlanoLeitura(List<Midia> fotos, List<Midia> titulos, List<Midia> textos, Midia eko) {
            this.fotos = Collections.unmodifiableList(fotos);
            this.iconesTitulo = Collections.unmodifiableList(titulos);
            this.iconesTexto = Collections.unmodifiableList(textos);
            this.eko = eko;
        }
    }

    public PlanoLeitura planejarLeitura(CursoEducativo curso, CursoEducativo.Aula aula,
                                        List<FluxoEducativo.Pagina> paginas, long semente) {
        String tema = regras.tema(aula == null ? curso.titulo : aula.titulo,
                curso.titulo + " " + curso.descricao);
        List<String> temas = new ArrayList<>();
        List<Midia> titulos = new ArrayList<>(), textos = new ArrayList<>();
        for (int i = 0; i < paginas.size(); i++) {
            FluxoEducativo.Pagina p = paginas.get(i);
            String temaPagina = regras.tema(p.titulo, p.texto);
            if ("geral".equals(temaPagina)) temaPagina = tema;
            temas.add(temaPagina);
            Set<String> usadas = new HashSet<>();
            Midia titulo = regras.decoracaoLeitura(temaPagina, semente ^ (i * 727L + 91),
                    ConfiguracaoMidias.CHANCE_ICONE_TITULO, usadas);
            if (titulo != null) usadas.add(titulo.grupoImagem);
            Midia texto = regras.decoracaoLeitura(temaPagina, semente ^ (i * 8191L + 6151),
                    ConfiguracaoMidias.CHANCE_ICONE_TEXTO, usadas);
            titulos.add(titulo);
            textos.add(texto);
        }
        return new PlanoLeitura(regras.fotosSemRepetir(temas, semente), titulos, textos,
                regras.ekoLeitura(tema, semente));
    }

    public RegrasMidias.DecoracaoQuiz decoracaoQuiz(long semente, int indice) {
        return regras.decoracaoQuiz(semente, indice);
    }

    public Midia planetaPadrao() { return regras.porId("interno_quiz"); }
}
