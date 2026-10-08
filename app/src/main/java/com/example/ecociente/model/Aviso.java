package com.example.ecociente.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class Aviso {

    private static List<Aviso> exemplos;

    public final int id;
    public final String titulo;
    public final String mensagem;
    public final CategoriaAviso categoria;
    public final boolean deCooperativa;
    public final String remetente;
    public final String enviadoEm;

    private boolean lida;

    private Aviso(
            int id,
            String titulo,
            String mensagem,
            CategoriaAviso categoria,
            boolean deCooperativa,
            String remetente,
            long horasAtras,
            boolean lida) {
        this.id = id;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.categoria = categoria;
        this.deCooperativa = deCooperativa;
        this.remetente = remetente;
        this.enviadoEm =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                        .format(new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(horasAtras)));
        this.lida = lida;
    }

    public boolean isLida() {
        return lida;
    }

    public void marcarComoLida() {
        lida = true;
    }

    @NonNull
    public static synchronized List<Aviso> exemplos() {

        if (exemplos == null) {
            exemplos = new ArrayList<>();

            exemplos.add(new Aviso(1, "Mudança no horário da coleta de recicláveis",
                    "A partir da próxima semana, a coleta de recicláveis no condomínio será realizada às terças e quintas-feiras, às 9h. Pedimos que os materiais sejam colocados na área de coleta até as 8h30.",
                    CategoriaAviso.COLETA, false, "Síndico/Gestão", 48, false));
            exemplos.add(new Aviso(2, "Dicas de separação de resíduos eletrônicos",
                    "A cooperativa parceira compartilha algumas dicas importantes para o descarte correto de eletrônicos: retire as pilhas e baterias, não quebre os aparelhos e leve-os ao ponto de coleta indicado.",
                    CategoriaAviso.ORIENTACAO, true, "Cooperativa VerdeMais", 72, true));
            exemplos.add(new Aviso(3, "Manutenção na área de coleta",
                    "Informamos que, na próxima segunda-feira, a área de coleta passará por manutenção. Pedimos que os resíduos sejam mantidos nos apartamentos até a liberação do local.",
                    CategoriaAviso.MANUTENCAO, false, "Síndico/Gestão", 24 * 5, false));
            exemplos.add(new Aviso(4, "Campanha de reciclagem de garrafas PET",
                    "A cooperativa inicia uma nova campanha de reciclagem de garrafas PET. Separe e entregue as garrafas limpas e secas na portaria e ganhe pontos no ranking do condomínio.",
                    CategoriaAviso.CAMPANHA, true, "Cooperativa VerdeMais", 24 * 7, true));
            exemplos.add(new Aviso(5, "Reunião do conselho do condomínio",
                    "Informamos que a reunião do conselho será realizada no dia 12 de maio, às 19h, no salão de festas. A presença de todos é muito importante.",
                    CategoriaAviso.AVISO_GERAL, false, "Síndico/Gestão", 24 * 9, true));
        }

        return exemplos;
    }

    @Nullable
    public static Aviso porId(int id) {

        for (Aviso aviso : exemplos()) {
            if (aviso.id == id) {
                return aviso;
            }
        }

        return null;
    }
}
