package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import com.example.ecociente.model.Aviso;
import com.example.ecociente.model.CategoriaAviso;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Avisos do síndico (fictício). Uma lista só, criada na primeira leitura: marcarComoLida vale enquanto o processo viver e não passa de uma reinicialização nem do "Sair" da demo. As datas são relativas ao momento em que a lista nasce.
final class AvisosDemo {

    private static final String SINDICO = "Carlos Menezes (Síndico)";

    private static List<Aviso> lista;

    private AvisosDemo() {}

    @NonNull
    static synchronized List<Aviso> lista() {
        if (lista == null) {
            lista =
                    Collections.unmodifiableList(
                            Arrays.asList(
                                    Aviso.de(
                                            1,
                                            "Coleta seletiva nesta semana",
                                            "A cooperativa passa para recolher os recicláveis. Deixe tudo separado e limpo na área de descarte até as 8h.",
                                            CategoriaAviso.COLETA,
                                            false,
                                            SINDICO,
                                            3,
                                            false),
                                    Aviso.de(
                                            2,
                                            "Manutenção dos coletores",
                                            "Os coletores do bloco B ficam fechados para manutenção. Use os do bloco A até a liberação.",
                                            CategoriaAviso.MANUTENCAO,
                                            false,
                                            SINDICO,
                                            30,
                                            false),
                                    Aviso.de(
                                            3,
                                            "Campanha do óleo de cozinha",
                                            "Entregue seu óleo usado em garrafa PET na portaria e ganhe pontos extras no EcoCiente.",
                                            CategoriaAviso.CAMPANHA,
                                            false,
                                            SINDICO,
                                            120,
                                            true)));
        }

        return lista;
    }
}
