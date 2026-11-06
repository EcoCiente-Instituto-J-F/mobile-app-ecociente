package com.example.ecociente.repository;

import java.util.Objects;
import java.util.Random;

final class SorteadorMensagensMotivacionais {

    private static final String[] FRASES = {
            "Faça sua parte: separe, recicle e inspire outras pessoas!",
            "Vamos manter os materiais circulando e o desperdício diminuindo?",
            "Seu reciclável pode ganhar uma nova vida. Separe corretamente!",
            "Que tal transformar seus resíduos em novas oportunidades? Recicle!",
            "Cada atitude conta! Separe seus recicláveis e faça a diferença. ♻️",
            "O planeta agradece cada material que você encaminha para a reciclagem.",
            "Hoje é um ótimo dia para fazer uma escolha mais sustentável.",
            "Seu hábito pode inspirar uma comunidade inteira. Comece reciclando!",
            "Menos desperdício, mais consciência. Recicle!",
            "Mais reciclagem, menos desperdício. Juntos podemos fazer a diferença!",
            "Uma embalagem separada hoje pode fazer parte de algo novo amanhã.",
            "Dê uma segunda chance aos materiais: recicle! ♻️",
            "Cada reciclável no lugar certo é um passo a mais para um futuro melhor.",
            "Reciclar é simples e o impacto pode ser enorme. Vamos nessa?",
            "Uma pequena ação sua pode contribuir para uma grande mudança. Recicle!",
            "Não deixe a oportunidade passar: separe seus recicláveis hoje!",
            "Reciclar hoje é cuidar do planeta amanhã. 🌱",
            "Sua atitude faz parte da mudança. Separe seus recicláveis!",
            "Vamos juntos deixar o mundo mais sustentável? Comece reciclando!",
            "Pequenas ações geram grandes mudanças. Que tal reciclar?"
    };

    private final Random aleatorio;
    private int ultimoIndice = -1;

    SorteadorMensagensMotivacionais() {
        this(new Random());
    }

    SorteadorMensagensMotivacionais(Random aleatorio) {
        this.aleatorio = Objects.requireNonNull(aleatorio);
    }

    synchronized String sortear() {
        int indice;

        if (ultimoIndice < 0) {
            indice = aleatorio.nextInt(FRASES.length);
        } else {
            indice = aleatorio.nextInt(FRASES.length - 1);

            if (indice >= ultimoIndice) {
                indice++;
            }
        }

        ultimoIndice = indice;
        return FRASES[indice];
    }
}
