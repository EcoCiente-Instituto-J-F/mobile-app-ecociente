package com.example.ecociente.model;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Conteúdo local do primeiro quiz jogável do EcoCiente.
 *
 * Nesta etapa as perguntas ficam no aplicativo para que todo o fluxo
 * (introdução -> perguntas -> resultado -> revisão) funcione de ponta a ponta.
 * Depois esse conteúdo pode ser substituído por uma API sem alterar as telas.
 */
public final class QuizConteudo {

    private static final List<Pergunta> PERGUNTAS = Collections.unmodifiableList(Arrays.asList(
            new Pergunta(
                    "Qual nutriente é a principal contribuição dos materiais 'verdes' (restos de alimentos) na compostagem?",
                    new String[]{"Oxigênio", "Nitrogênio", "Carbono", "Cálcio"},
                    1,
                    "Na compostagem, os materiais chamados de 'verdes', como restos de frutas e vegetais, são ricos em nitrogênio. Esse nutriente ajuda na atividade dos microrganismos responsáveis pela decomposição da matéria orgânica."
            ),
            new Pergunta(
                    "Qual atitude ajuda mais a reduzir a quantidade de plástico descartável no dia a dia?",
                    new String[]{"Usar itens descartáveis", "Comprar porções menores", "Levar garrafa reutilizável", "Misturar plástico ao lixo orgânico"},
                    2,
                    "Usar uma garrafa reutilizável evita o consumo repetido de copos e garrafas descartáveis. Quanto mais vezes o mesmo recipiente é reutilizado, menor é a geração de resíduos plásticos."
            ),
            new Pergunta(
                    "Qual é a destinação mais adequada para celulares e eletrônicos que não serão mais usados?",
                    new String[]{"Lixo comum", "Ponto de coleta de eletrônicos", "Vaso sanitário", "Queima ao ar livre"},
                    1,
                    "Eletrônicos devem ser encaminhados a pontos de coleta específicos. Esses locais direcionam os componentes para tratamento e reciclagem adequados, evitando que substâncias prejudiciais contaminem o ambiente."
            ),
            new Pergunta(
                    "Qual hábito simples ajuda a economizar água durante a escovação dos dentes?",
                    new String[]{"Deixar a torneira aberta", "Fechar a torneira enquanto escova", "Usar água quente", "Escovar por mais tempo"},
                    1,
                    "Fechar a torneira enquanto escova os dentes evita que a água continue correndo sem necessidade. É uma ação simples que reduz o desperdício no dia a dia."
            ),
            new Pergunta(
                    "Qual escolha pode ajudar a diminuir a pegada de carbono em trajetos curtos?",
                    new String[]{"Ir de carro sozinho", "Usar bicicleta ou caminhar", "Manter o motor ligado parado", "Aumentar o uso de descartáveis"},
                    1,
                    "Caminhar ou usar bicicleta em trajetos curtos dispensa a queima de combustível durante o deslocamento. Isso reduz as emissões associadas ao transporte individual motorizado."
            ),
            new Pergunta(
                    "Na coleta seletiva brasileira, qual cor é normalmente associada ao descarte de papel?",
                    new String[]{"Azul", "Vermelho", "Verde", "Amarelo"},
                    0,
                    "Na identificação mais comum da coleta seletiva no Brasil, a cor azul é usada para papel e papelão. A separação por cores facilita o reconhecimento e a destinação dos resíduos."
            ),
            new Pergunta(
                    "Antes de enviar embalagens recicláveis para a coleta seletiva, o que é recomendado fazer?",
                    new String[]{"Encher de água", "Retirar o excesso de resíduos", "Quebrar todo o material", "Misturar com restos de comida"},
                    1,
                    "Retirar o excesso de resíduos ajuda a evitar mau cheiro, contaminação e perda de materiais recicláveis. Não é necessário desperdiçar muita água: o importante é que a embalagem não esteja cheia de restos."
            ),
            new Pergunta(
                    "Qual destes materiais normalmente NÃO deve ser colocado junto ao papel reciclável limpo?",
                    new String[]{"Folha de caderno", "Caixa de papelão", "Papel engordurado", "Jornal seco"},
                    2,
                    "Papel engordurado ou muito contaminado por alimentos normalmente perde a qualidade necessária para a reciclagem convencional. Já folhas, jornais secos e papelão limpo podem ser separados para reciclagem."
            ),
            new Pergunta(
                    "Qual item pode ser utilizado em uma composteira doméstica?",
                    new String[]{"Pilha usada", "Casca de frutas", "Vidro quebrado", "Embalagem plástica"},
                    1,
                    "Cascas de frutas são resíduos orgânicos e podem se decompor em uma composteira. Pilhas, vidros e plásticos precisam de outras formas de descarte e não devem ser colocados na compostagem."
            ),
            new Pergunta(
                    "Qual ação representa melhor o consumo consciente?",
                    new String[]{"Comprar sem necessidade", "Trocar produtos ainda úteis", "Planejar a compra e evitar desperdícios", "Escolher sempre itens descartáveis"},
                    2,
                    "Consumo consciente envolve avaliar a necessidade da compra, planejar o que será adquirido e evitar desperdícios. Isso reduz o uso desnecessário de recursos e a geração de resíduos."
            )
    ));

    private QuizConteudo() {
    }

    @NonNull
    public static List<Pergunta> obterPerguntas() {
        return PERGUNTAS;
    }

    public static final class Pergunta {
        private final String enunciado;
        private final String[] opcoes;
        private final int indiceCorreto;
        private final String explicacao;

        private Pergunta(
                @NonNull String enunciado,
                @NonNull String[] opcoes,
                int indiceCorreto,
                @NonNull String explicacao
        ) {
            this.enunciado = enunciado;
            this.opcoes = opcoes;
            this.indiceCorreto = indiceCorreto;
            this.explicacao = explicacao;
        }

        @NonNull
        public String getEnunciado() {
            return enunciado;
        }

        @NonNull
        public String[] getOpcoes() {
            return opcoes.clone();
        }

        public int getIndiceCorreto() {
            return indiceCorreto;
        }

        @NonNull
        public String getExplicacao() {
            return explicacao;
        }
    }
}
