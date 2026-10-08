package com.example.ecociente.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.example.ecociente.R;
import java.util.Arrays;
import java.util.List;

public enum TipoLista {
    GUIA(R.string.guia, R.drawable.icon_guia_verde),
    FEED(R.string.feed, R.drawable.ic_nav_feed_verde),
    UNIDADES(R.string.unidades, R.drawable.ic_nav_unidades_verde),
    CONDOMINIOS(R.string.condominios_atendidos, R.drawable.ic_perfil_cooperativa),
    AVALIACOES(R.string.avaliacoes_recebidas, R.drawable.ic_estrela),
    RANKING(R.string.ranking_do_morador, R.drawable.ic_trofeu);

    @StringRes public final int titulo;
    @DrawableRes public final int icone;

    TipoLista(@StringRes int titulo, @DrawableRes int icone) {
        this.titulo = titulo;
        this.icone = icone;
    }

    @NonNull
    public List<ItemLista> itens() {

        switch (this) {
            case GUIA:
                return Arrays.asList(
                        new ItemLista("Como separar o lixo reciclável", "Guia básico da coleta seletiva", "5 min"),
                        new ItemLista("Compostagem caseira", "Transforme restos de comida em adubo", "7 min"),
                        new ItemLista("Descarte correto de eletrônicos", "Onde levar pilhas, baterias e celulares", "4 min"),
                        new ItemLista("Reduzindo o uso de plástico", "Pequenas trocas que fazem diferença", "6 min"),
                        new ItemLista("O que não vai na coleta seletiva", "Evite contaminar o material reciclável", "3 min"),
                        new ItemLista("Lavar embalagens sem desperdiçar água", "Dicas para economizar na limpeza", "4 min"));

            case FEED:
                return Arrays.asList(
                        new ItemLista("Coleta antecipada nesta semana", "Aviso da cooperativa EcoCoop", "Hoje"),
                        new ItemLista("Novo ponto de descarte de vidro", "Disponível na portaria do bloco B", "Ontem"),
                        new ItemLista("Meta de reciclagem do mês atingida", "O condomínio reciclou 320 kg em setembro", "Seg"),
                        new ItemLista("Lembrete: separe o óleo de cozinha", "Entregue em garrafas PET fechadas", "05/10"),
                        new ItemLista("Reunião de moradores sobre coleta", "Quinta-feira, 19h, no salão de festas", "02/10"));

            case UNIDADES:
                return Arrays.asList(
                        new ItemLista("Bloco A · Apto 101", "Ana Paula Souza", "Ativo"),
                        new ItemLista("Bloco A · Apto 102", "Carlos Eduardo Lima", "Ativo"),
                        new ItemLista("Bloco A · Apto 201", "Marina Alves", "Pendente"),
                        new ItemLista("Bloco B · Apto 101", "Rafael Mendes", "Ativo"),
                        new ItemLista("Bloco B · Apto 302", "Juliana Costa", "Ativo"),
                        new ItemLista("Bloco B · Apto 402", "Pedro Henrique Dias", "Pendente"));

            case CONDOMINIOS:
                return Arrays.asList(
                        condominio("Condomínio Raio de Luz", "Rua das Flores, 120", "84 unidades", "Segunda e quinta"),
                        condominio("Residencial Jardim das Flores", "Av. Brasil, 560", "120 unidades", "Terça e sexta"),
                        condominio("Condomínio Vila Verde", "Rua das Palmeiras, 33", "56 unidades", "Quarta"),
                        condominio("Edifício Solar", "Rua do Sol, 410", "72 unidades", "Segunda"),
                        condominio("Residencial Aurora", "Rua da Aurora, 88", "40 unidades", "Quinta"));

            case AVALIACOES:
                return Arrays.asList(
                        new ItemLista("Condomínio Raio de Luz", "“Equipe pontual e muito educada.”", "5,0"),
                        new ItemLista("Residencial Jardim das Flores", "“A coleta atrasou um pouco, mas avisaram.”", "4,0"),
                        new ItemLista("Condomínio Vila Verde", "“Ótimo atendimento, recomendo.”", "5,0"),
                        new ItemLista("Edifício Solar", "“Poderiam passar mais cedo.”", "3,5"),
                        new ItemLista("Residencial Aurora", "“Tudo certo, sem reclamações.”", "4,5"));

            default:
                return Arrays.asList(
                        new ItemLista("1º  Rafael Mendes", "Bloco B · Apto 402", "1.980 pts"),
                        new ItemLista("2º  Ana Paula Souza", "Bloco A · Apto 101", "1.840 pts"),
                        new ItemLista("3º  Marina Alves", "Bloco A · Apto 201", "1.720 pts"),
                        new ItemLista("4º  Carlos Eduardo Lima", "Bloco A · Apto 102", "1.560 pts"),
                        new ItemLista("5º  Juliana Costa", "Bloco B · Apto 302", "1.410 pts"),
                        new ItemLista("6º  Pedro Henrique Dias", "Bloco B · Apto 101", "1.250 pts"));
        }
    }

    @NonNull
    private static ItemLista condominio(
            String nome, String endereco, String unidades, String diasDeColeta) {

        return new ItemLista(
                nome,
                endereco + " · " + unidades,
                diasDeColeta,
                new String[][] {
                    {"Endereço", endereco},
                    {"Unidades", unidades},
                    {"Dias de coleta", diasDeColeta},
                    {"Atendido desde", "Março de 2026"}
                });
    }
}
