package com.example.ecociente.demo;

// Avisa o app quando um descarte do morador é aprovado. Chamado fora da trava da ApiDemo, na thread que processou a aprovação (a da rede ou a do temporizador): o ouvinte pode mostrar notificação, mas não deve demorar.
public interface OuvinteDemo {

    // `subiuNoRanking`: o morador ficou numa posição melhor do que tinha antes de receber os pontos.
    void aoAprovarDescarte(boolean subiuNoRanking);
}
