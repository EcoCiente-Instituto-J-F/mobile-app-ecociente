package com.example.ecociente.demo;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class ConteudoEducativoDemo {

    static final int NOTA_MINIMA = 70;

    private static volatile List<Curso> cursos = criar();
    private static volatile boolean carregadoNeon;

    private ConteudoEducativoDemo() {}

    static synchronized void inicializar(@NonNull Context contexto) {
        if (carregadoNeon) return;
        try {
            JSONArray originais = new JSONArray(ler(contexto, "mock/educacao/cursos.json"));
            JSONArray provas = new JSONArray(ler(contexto, "mock/educacao/quizzes.json"));
            java.util.Map<Integer, Quiz> porAula = new java.util.HashMap<>();
            for (int i = 0; i < provas.length(); i++) {
                JSONObject item = provas.getJSONObject(i);
                JSONArray questoes = item.getJSONArray("perguntas");
                List<Pergunta> perguntas = new ArrayList<>();
                for (int p = 0; p < questoes.length(); p++) {
                    JSONObject q = questoes.getJSONObject(p);
                    JSONArray respostas = q.getJSONArray("alternativas");
                    String[] opcoes = new String[respostas.length()];
                    int[] ids = new int[respostas.length()];
                    int correta = -1;
                    for (int a = 0; a < respostas.length(); a++) {
                        JSONObject alternativa = respostas.getJSONObject(a);
                        ids[a] = alternativa.getInt("id");
                        opcoes[a] = alternativa.getString("texto");
                        if (alternativa.optBoolean("correta", false)) {
                            if (correta >= 0) throw new IllegalStateException("Mais de uma alternativa correta na pergunta " + q.getInt("id"));
                            correta = a;
                        }
                    }
                    if (correta < 0) throw new IllegalStateException("Pergunta sem gabarito: " + q.getInt("id"));
                    perguntas.add(new Pergunta(q.getInt("id"), q.optInt("ordem", p + 1), q.getString("enunciado"), correta,
                            q.optString("explicacao", ""), ids, opcoes));
                }
                Quiz quiz = new Quiz(item.getInt("id"), item.getString("titulo"),
                        item.optInt("pontosRecompensa", 0), perguntas);
                if (porAula.put(item.getInt("aulaId"), quiz) != null) {
                    throw new IllegalStateException("Dois quizzes na mesma aula " + item.getInt("aulaId"));
                }
            }
            List<Curso> resultado = new ArrayList<>();
            for (int i = 0; i < originais.length(); i++) {
                JSONObject item = originais.getJSONObject(i);
                JSONArray itens = item.getJSONArray("aulas");
                List<Aula> aulas = new ArrayList<>();
                for (int j = 0; j < itens.length(); j++) {
                    JSONObject a = itens.getJSONObject(j);
                    aulas.add(new Aula(a.getInt("id"), a.optInt("ordem", j + 1), a.getString("titulo"),
                            a.optString("conteudo", ""), porAula.remove(a.getInt("id"))));
                }
                aulas.sort((a, b) -> Integer.compare(a.ordem, b.ordem));
                resultado.add(new Curso(item.getInt("id"), item.getString("titulo"),
                        item.optString("descricao", ""), aulas));
            }
            if (resultado.size() != 42 || !porAula.isEmpty()) {
                throw new IllegalStateException("Catalogo Neon incompleto ou quiz sem aula: " + resultado.size());
            }
            cursos = Collections.unmodifiableList(resultado);
            carregadoNeon = true;
        } catch (IOException | JSONException e) {
            throw new IllegalStateException("Nao foi possivel carregar catalogo offline EcoCiente", e);
        }
    }

    private static String ler(Context contexto, String caminho) throws IOException {
        try (InputStream entrada = contexto.getAssets().open(caminho);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] bloco = new byte[8192];
            int lidos;
            while ((lidos = entrada.read(bloco)) != -1) buffer.write(bloco, 0, lidos);
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    static final class Curso {
        final int id;
        final String titulo;
        final String descricao;
        final List<Aula> aulas;
        Curso(int id, String titulo, String descricao, Aula... aulas) {
            this(id, titulo, descricao, java.util.Arrays.asList(aulas));
        }
        Curso(int id, String titulo, String descricao, List<Aula> aulas) {
            this.id = id; this.titulo = titulo; this.descricao = descricao;
            this.aulas = Collections.unmodifiableList(new ArrayList<>(aulas));
        }
    }

    static final class Aula {
        final int id;
        final int ordem;
        final String titulo;
        final String conteudo;
        @Nullable final Quiz quiz;
        Aula(int id, int ordem, String titulo, String conteudo, @Nullable Quiz quiz) {
            this.id = id; this.ordem = ordem; this.titulo = titulo;
            this.conteudo = conteudo; this.quiz = quiz;
        }
    }

    static final class Quiz {
        final int id;
        final String titulo;
        final int pontosRecompensa;
        final List<Pergunta> perguntas;
        Quiz(int id, String titulo, int pontosRecompensa, Pergunta... perguntas) {
            this(id, titulo, pontosRecompensa, java.util.Arrays.asList(perguntas));
        }
        Quiz(int id, String titulo, int pontosRecompensa, List<Pergunta> perguntas) {
            this.id = id; this.titulo = titulo; this.pontosRecompensa = pontosRecompensa;
            this.perguntas = Collections.unmodifiableList(new ArrayList<>(perguntas));
        }
        @Nullable Pergunta pergunta(int perguntaId) {
            for (Pergunta p : perguntas) if (p.id == perguntaId) return p;
            return null;
        }
    }

    static final class Pergunta {
        final int id;
        final int ordem;
        final String enunciado;
        final String explicacao;
        final int indiceCorreto;
        final List<String> alternativas;
        private final int[] idsAlternativas;
        Pergunta(int id, int ordem, String enunciado, int indiceCorreto, String explicacao,
                String... alternativas) {
            this(id, ordem, enunciado, indiceCorreto, explicacao, idsSinteticos(id, alternativas.length), alternativas);
        }
        private static int[] idsSinteticos(int perguntaId, int quantidade) {
            int[] ids = new int[quantidade];
            for (int i = 0; i < ids.length; i++) ids[i] = perguntaId * 10 + i + 1;
            return ids;
        }
        Pergunta(int id, int ordem, String enunciado, int indiceCorreto, String explicacao,
                int[] idsAlternativas, String[] alternativas) {
            this.id = id; this.ordem = ordem; this.enunciado = enunciado;
            this.indiceCorreto = indiceCorreto; this.explicacao = explicacao;
            this.idsAlternativas = idsAlternativas.clone();
            this.alternativas = Collections.unmodifiableList(java.util.Arrays.asList(alternativas.clone()));
        }
        int idAlternativa(int indice) { return idsAlternativas[indice]; }
        int idCorreta() { return idAlternativa(indiceCorreto); }
        boolean temAlternativa(int id) {
            for (int item : idsAlternativas) if (item == id) return true;
            return false;
        }
    }

    @NonNull static List<Curso> cursos() { return cursos; }
    @Nullable static Curso curso(int id) {
        for (Curso c : cursos) if (c.id == id) return c;
        return null;
    }
    @Nullable static Aula aula(int id) {
        for (Curso c : cursos) for (Aula a : c.aulas) if (a.id == id) return a;
        return null;
    }
    @Nullable static Quiz quiz(int id) {
        for (Curso c : cursos) for (Aula a : c.aulas)
            if (a.quiz != null && a.quiz.id == id) return a.quiz;
        return null;
    }
    @NonNull
    private static List<Curso> criar() {
        List<Curso> lista = new ArrayList<>();

        lista.add(
                new Curso(
                        1,
                        "Guia completo de reciclagem",
                        "Entenda o processo de reciclagem e seu impacto positivo no planeta.",
                        new Aula(
                                1,
                                1,
                                "Por onde começar a reciclar?",
                                "Comece separando resíduos secos dos orgânicos. Papel, plástico, metal e vidro devem estar preferencialmente limpos e secos, seguindo as orientações de coleta da sua cidade.",
                                new Quiz(
                                        101,
                                        "Você sabe reciclar?",
                                        40,
                                        new Pergunta(
                                                1001, 1,
                                                "Qual destes materiais costuma ser reciclável?", 0,
                                                "Latas de alumínio podem ser recicladas quando encaminhadas corretamente.",
                                                "Lata de alumínio", "Papel higiênico usado", "Guardanapo sujo", "Restos de comida"),
                                        new Pergunta(
                                                1002, 2,
                                                "Como deve estar uma embalagem para coleta seletiva?", 1,
                                                "O ideal é retirar restos de comida e evitar contaminação.",
                                                "Cheia de resíduos", "Vazia e sem restos de alimento", "Misturada a orgânicos", "Fechada com comida"),
                                        new Pergunta(
                                                1003, 3,
                                                "O que significa reutilizar um objeto?", 2,
                                                "Reutilizar é dar novos usos antes do descarte.",
                                                "Queimar o objeto", "Jogar no lixo", "Usá-lo novamente", "Enterrá-lo"),
                                        new Pergunta(
                                                1004, 4,
                                                "Qual é um benefício da coleta seletiva?", 3,
                                                "A separação facilita o encaminhamento correto de materiais aproveitáveis.",
                                                "Aumentar descarte irregular", "Misturar resíduos", "Eliminar toda geração de lixo", "Facilitar a reciclagem"))),
                        new Aula(
                                2,
                                2,
                                "Preparando materiais para coleta seletiva",
                                "Esvazie embalagens, remova restos de comida e acondicione os resíduos com segurança. Evite misturar materiais contaminados aos recicláveis.",
                                null)));

        lista.add(
                new Curso(
                        2,
                        "Como fazer compostagem em casa",
                        "Transforme resíduos orgânicos em adubo e reduza o lixo enviado para aterros.",
                        new Aula(
                                3,
                                1,
                                "Montando sua composteira",
                                "Escolha um recipiente com ventilação adequada e uma mistura de materiais secos e úmidos. Adicione pequenas quantidades de resíduos orgânicos e mantenha a composteira arejada.",
                                new Quiz(
                                        102,
                                        "Separação correta de resíduos",
                                        30,
                                        new Pergunta(
                                                1005, 1,
                                                "Onde colocar uma casca de banana, quando há coleta de orgânicos?", 0,
                                                "Cascas são resíduos orgânicos que podem ter destinação específica.",
                                                "Na fração de orgânicos", "No recipiente para vidro", "No recipiente para metal", "Com eletrônicos"),
                                        new Pergunta(
                                                1006, 2,
                                                "O que fazer com pilhas usadas?", 2,
                                                "Pilhas devem seguir para pontos de recebimento adequados.",
                                                "Misturar com papel", "Jogar na rua", "Entregar em ponto de coleta apropriado", "Queimar"),
                                        new Pergunta(
                                                1007, 3,
                                                "Como descartar vidro quebrado?", 1,
                                                "Proteger e identificar os cacos reduz riscos aos coletores.",
                                                "Solto na sacola", "Protegido e identificado", "Misturado a alimentos", "Na pia"),
                                        new Pergunta(
                                                1008, 4,
                                                "Qual atitude ajuda na coleta de papelão?", 3,
                                                "Caixas dobradas ocupam menos espaço no armazenamento.",
                                                "Molhar as caixas", "Contaminar com óleo", "Rasgar e espalhar", "Dobrar e manter seco"))),
                        new Aula(
                                4,
                                2,
                                "Cuidados com a compostagem",
                                "Folhas secas e papelão sem tratamento ajudam a equilibrar a umidade. Evite excesso de líquidos e siga as orientações específicas do seu método de compostagem.",
                                null)));

        lista.add(
                new Curso(
                        3,
                        "Plástico: tipos e como descartar",
                        "Conheça os principais tipos de plástico e a forma correta de descarte.",
                        new Aula(
                                5,
                                1,
                                "Identificando embalagens plásticas",
                                "Verifique a identificação da embalagem e as regras locais de coleta seletiva. Nem todo material plástico é aceito em todos os programas de reciclagem.",
                                new Quiz(
                                        103,
                                        "O universo dos plásticos",
                                        50,
                                        new Pergunta(
                                                1009, 1,
                                                "Qual ação é mais útil antes de descartar uma garrafa plástica?", 2,
                                                "Esvazie a embalagem e siga a orientação de coleta local.",
                                                "Encher de terra", "Quebrar em pedaços", "Esvaziá-la", "Misturar com óleo"),
                                        new Pergunta(
                                                1010, 2,
                                                "Todos os plásticos são aceitos em qualquer coleta?", 1,
                                                "A aceitação depende do material e do serviço disponível na cidade.",
                                                "Sim, sempre", "Não, depende da coleta local", "Só plástico colorido", "Só plástico transparente"),
                                        new Pergunta(
                                                1011, 3,
                                                "Qual é uma alternativa para reduzir sacolas descartáveis?", 0,
                                                "Sacolas reutilizáveis reduzem o consumo de embalagens de uso único.",
                                                "Levar uma ecobag", "Pedir sacola dupla", "Descartar sacolas na rua", "Comprar sacolas extras"),
                                        new Pergunta(
                                                1012, 4,
                                                "Em geral, o que é importante verificar no descarte?", 3,
                                                "As orientações do município orientam a destinação mais adequada.",
                                                "Apenas a cor", "O preço da embalagem", "A marca do produto", "As regras da coleta local"))),
                        new Aula(
                                6,
                                2,
                                "Reduzir antes de reciclar",
                                "Priorize itens reutilizáveis e evite descartáveis desnecessários sempre que possível.",
                                null)));

        // Conteúdos avulsos, sem aulas.
        lista.add(
                new Curso(
                        4,
                        "Papel: como separar e descartar corretamente",
                        "Aprenda a separar jornais, folhas e embalagens de papel."));
        lista.add(
                new Curso(
                        5,
                        "Vidro: reciclagem e reutilização",
                        "Veja como destinar recipientes de vidro com segurança."));
        lista.add(
                new Curso(
                        6,
                        "Descarte de papelão: dicas e cuidados",
                        "Dobre caixas, economize espaço e facilite a coleta."));
        lista.add(
                new Curso(
                        7,
                        "Sustentabilidade no dia a dia",
                        "Pequenas ações que fazem diferença na rotina."));

        return Collections.unmodifiableList(lista);
    }
}
