package com.example.ecociente.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Catálogo LOCAL para pré-visualização do design, sem autenticação JWT ou chamadas HTTP.
 * Para voltar aos dados reais, altere ATIVO para false e recompile o aplicativo.
 * Não salva pontuações ou progresso na API, nem compartilha dados com produção.
 */
public final class DadosDemonstracaoEducacao {
    public static final boolean ATIVO = false;

    private DadosDemonstracaoEducacao() { }

    public static boolean ehQuizDemonstracao(int quizId) {
        return ATIVO && quizId <= -201 && quizId >= -206;
    }

    public static boolean ehDestaque(CursoEducativo curso) {
        return curso.id == -101 || curso.id == -102 || curso.id == -103;
    }

    public static String tipoConteudo(int id) {
        switch (id) {
            case -101: case -104: return "Guia";
            case -102: return "Artigo";
            case -103: case -105: case -106: case -107: return "Dica";
            default: return "Guia";
        }
    }

    /** IDs negativos impedem colisão com os IDs positivos usados no PostgreSQL. */
    public static List<CursoEducativo> cursos() {
        List<CursoEducativo> lista = new ArrayList<>();
        lista.add(curso(-101, "Guia completo de reciclagem",
                "Entenda o processo de reciclagem e seu impacto positivo no planeta.",
                aula(-1001, 1, "Por onde começar a reciclar?",
                        "Comece separando resíduos secos dos orgânicos. Papel, plástico, metal e vidro " +
                        "devem estar preferencialmente limpos e secos, seguindo as orientações de coleta da sua cidade."),
                aula(-1002, 2, "Preparando materiais para coleta seletiva",
                        "Esvazie embalagens, remova restos de comida e acondicione os resíduos com segurança. " +
                        "Evite misturar materiais contaminados aos recicláveis.")));
        lista.add(curso(-102, "Como fazer compostagem em casa",
                "Transforme resíduos orgânicos em adubo e reduza o lixo enviado para aterros.",
                aula(-1003, 1, "Montando sua composteira",
                        "Escolha um recipiente com ventilação adequada e uma mistura de materiais secos e úmidos. " +
                        "Adicione pequenas quantidades de resíduos orgânicos e mantenha a composteira arejada."),
                aula(-1004, 2, "Cuidados com a compostagem",
                        "Folhas secas e papelão sem tratamento ajudam a equilibrar a umidade. " +
                        "Evite excesso de líquidos e siga as orientações específicas do seu método de compostagem.")));
        lista.add(curso(-103, "Plástico: tipos e como descartar",
                "Conheça os principais tipos de plástico e a forma correta de descarte.",
                aula(-1005, 1, "Identificando embalagens plásticas",
                        "Verifique a identificação da embalagem e as regras locais de coleta seletiva. " +
                        "Nem todo material plástico é aceito em todos os programas de reciclagem."),
                aula(-1006, 2, "Reduzir antes de reciclar",
                        "Priorize itens reutilizáveis e evite descartáveis desnecessários sempre que possível.")));
        // Conteudos avulsos: nao possuem aulas, entao abrem diretamente a intro.
        lista.add(curso(-104, "Papel: como separar e descartar corretamente",
                "Aprenda a separar jornais, folhas e embalagens de papel."));
        lista.add(curso(-105, "Vidro: reciclagem e reutilização",
                "Veja como destinar recipientes de vidro com segurança."));
        lista.add(curso(-106, "Descarte de papelão: dicas e cuidados",
                "Dobre caixas, economize espaço e facilite a coleta."));
        lista.add(curso(-107, "Sustentabilidade no dia a dia",
                "Pequenas ações que fazem diferença na rotina."));
        return Collections.unmodifiableList(lista);
    }

    private static CursoEducativo curso(int id, String titulo, String descricao) {
        return new CursoEducativo(id, titulo, descricao, Collections.emptyList());
    }

    public static List<QuizResumo> quizzes() {
        List<QuizResumo> lista = new ArrayList<>();
        lista.add(quiz(-201, "Você sabe reciclar?", "Reciclagem",
                pergunta(-2001, "Qual destes materiais costuma ser reciclável?", 0,
                        "Latas de alumínio podem ser recicladas quando encaminhadas corretamente.",
                        "Lata de alumínio", "Papel higiênico usado", "Guardanapo sujo", "Restos de comida"),
                pergunta(-2002, "Como deve estar uma embalagem para coleta seletiva?", 1,
                        "O ideal é retirar restos de comida e evitar contaminação.",
                        "Cheia de resíduos", "Vazia e sem restos de alimento", "Misturada a orgânicos", "Fechada com comida"),
                pergunta(-2003, "O que significa reutilizar um objeto?", 2,
                        "Reutilizar é dar novos usos antes do descarte.",
                        "Queimar o objeto", "Jogar no lixo", "Usá-lo novamente", "Enterrá-lo"),
                pergunta(-2004, "Qual é um benefício da coleta seletiva?", 3,
                        "A separação facilita o encaminhamento correto de materiais aproveitáveis.",
                        "Aumentar descarte irregular", "Misturar resíduos", "Eliminar toda geração de lixo", "Facilitar a reciclagem")));
        lista.add(quiz(-202, "Separação correta de resíduos", "Descarte consciente",
                pergunta(-2005, "Onde colocar uma casca de banana, quando há coleta de orgânicos?", 0,
                        "Cascas são resíduos orgânicos que podem ter destinação específica.",
                        "Na fração de orgânicos", "No recipiente para vidro", "No recipiente para metal", "Com eletrônicos"),
                pergunta(-2006, "O que fazer com pilhas usadas?", 2,
                        "Pilhas devem seguir para pontos de recebimento adequados.",
                        "Misturar com papel", "Jogar na rua", "Entregar em ponto de coleta apropriado", "Queimar"),
                pergunta(-2007, "Como descartar vidro quebrado?", 1,
                        "Proteger e identificar os cacos reduz riscos aos coletores.",
                        "Solto na sacola", "Protegido e identificado", "Misturado a alimentos", "Na pia"),
                pergunta(-2008, "Qual atitude ajuda na coleta de papelão?", 3,
                        "Caixas dobradas ocupam menos espaço no armazenamento.",
                        "Molhar as caixas", "Contaminar com óleo", "Rasgar e espalhar", "Dobrar e manter seco")));
        lista.add(quiz(-203, "Consumo consciente", "Sustentabilidade",
                pergunta(-2009, "Qual hábito ajuda a evitar desperdício de alimentos?", 1,
                        "Planejar as refeições ajuda a comprar e usar só o necessário.",
                        "Comprar sem lista", "Planejar as refeições", "Descartar sobras sempre", "Estocar sem controle"),
                pergunta(-2010, "O que caracteriza uma compra consciente?", 3,
                        "Avaliar necessidade e durabilidade reduz o consumo desnecessário.",
                        "Comprar por impulso", "Ignorar durabilidade", "Comprar itens repetidos", "Avaliar necessidade e vida útil"),
                pergunta(-2011, "Qual opção reduz descartáveis?", 0,
                        "Uma garrafa reutilizável pode evitar muitas embalagens de uso único.",
                        "Usar garrafa reutilizável", "Usar copos descartáveis todo dia", "Pedir mais embalagens", "Trocar garrafas sempre"),
                pergunta(-2012, "O que significa reduzir no contexto dos 3 Rs?", 2,
                        "Reduzir é evitar gerar resíduos desnecessariamente.",
                        "Reciclar tudo", "Guardar todo lixo", "Consumir e descartar menos", "Comprar mais produtos")));
        lista.add(quiz(-204, "O universo dos plásticos", "Tipos de resíduos",
                pergunta(-2013, "Qual ação é mais útil antes de descartar uma garrafa plástica?", 2,
                        "Esvazie a embalagem e siga a orientação de coleta local.",
                        "Encher de terra", "Quebrar em pedaços", "Esvaziá-la", "Misturar com óleo"),
                pergunta(-2014, "Todos os plásticos são aceitos em qualquer coleta?", 1,
                        "A aceitação depende do material e do serviço disponível na cidade.",
                        "Sim, sempre", "Não, depende da coleta local", "Só plástico colorido", "Só plástico transparente"),
                pergunta(-2015, "Qual é uma alternativa para reduzir sacolas descartáveis?", 0,
                        "Sacolas reutilizáveis reduzem o consumo de embalagens de uso único.",
                        "Levar uma ecobag", "Pedir sacola dupla", "Descartar sacolas na rua", "Comprar sacolas extras"),
                pergunta(-2016, "Em geral, o que é importante verificar no descarte?", 3,
                        "As orientações do município orientam a destinação mais adequada.",
                        "Apenas a cor", "O preço da embalagem", "A marca do produto", "As regras da coleta local")));
        lista.add(quiz(-205, "Água: cada gota conta", "Meio ambiente",
                pergunta(-2017, "Qual atitude ajuda a economizar água ao escovar os dentes?", 0,
                        "Fechar a torneira enquanto escova reduz o consumo de água.",
                        "Fechar a torneira", "Deixar água correr", "Abrir duas torneiras", "Usar mais água"),
                pergunta(-2018, "O que fazer ao identificar um vazamento?", 2,
                        "Consertar vazamentos evita desperdício contínuo.",
                        "Ignorar", "Aumentar a pressão", "Providenciar reparo", "Deixar por semanas"),
                pergunta(-2019, "Como usar a máquina de lavar de forma mais eficiente?", 1,
                        "Utilizar cargas adequadas às orientações do fabricante economiza recursos.",
                        "Lavar uma peça por vez", "Usar cargas adequadas", "Deixar ligada vazia", "Repetir ciclos sem necessidade"),
                pergunta(-2020, "Qual é uma boa prática no banho?", 3,
                        "Banhos mais curtos ajudam a reduzir o uso de água e energia.",
                        "Ficar o máximo possível", "Abrir o chuveiro antes por muito tempo", "Não reparar vazamentos", "Reduzir o tempo do banho")));
        lista.add(quiz(-206, "Pequenas atitudes, grande impacto", "Hábitos sustentáveis",
                pergunta(-2021, "Qual prática ajuda a economizar eletricidade?", 1,
                        "Desligar luzes quando não são necessárias evita consumo desnecessário.",
                        "Deixar tudo ligado", "Apagar luzes ao sair", "Ligar aparelhos vazios", "Ignorar o consumo"),
                pergunta(-2022, "O que fazer com roupas em bom estado que você não usa?", 0,
                        "Doar ou trocar prolonga a vida útil das roupas.",
                        "Doar ou trocar", "Jogar em terrenos", "Queimar", "Misturar a resíduos orgânicos"),
                pergunta(-2023, "Qual deslocamento pode reduzir emissões em trajetos curtos?", 3,
                        "Caminhar ou pedalar pode substituir deslocamentos motorizados.",
                        "Viajar de avião", "Usar carro sozinho sempre", "Dar voltas de carro", "Caminhar ou pedalar"),
                pergunta(-2024, "O que significa reparar antes de substituir?", 2,
                        "Consertar um item pode evitar uma compra e um descarte antecipado.",
                        "Descartar imediatamente", "Comprar dois novos", "Consertar o item utilizável", "Deixar parado")));
        return Collections.unmodifiableList(lista);
    }

    private static CursoEducativo curso(int id, String titulo, String descricao, CursoEducativo.Aula... aulas) {
        return new CursoEducativo(id, titulo, descricao, Arrays.asList(aulas));
    }

    private static CursoEducativo.Aula aula(int id, int ordem, String titulo, String conteudo) {
        return new CursoEducativo.Aula(id, titulo, conteudo, ordem);
    }

    private static QuizResumo quiz(int id, String titulo, String categoria, QuizConteudo.Pergunta... perguntas) {
        return new QuizResumo(id, titulo, categoria, 0, Arrays.asList(perguntas));
    }

    private static QuizConteudo.Pergunta pergunta(int id, String enunciado, int indiceCorreto,
                                                  String explicacao, String... alternativas) {
        int[] idsAlternativas = new int[alternativas.length];
        for (int i = 0; i < alternativas.length; i++) {
            idsAlternativas[i] = id * 10 - i - 1;
        }
        return new QuizConteudo.Pergunta(id, enunciado, alternativas, idsAlternativas,
                indiceCorreto, explicacao);
    }
}
