package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import com.example.ecociente.model.Aviso;
import com.example.ecociente.model.CategoriaAviso;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;

// Avisos do síndico (fictício). O Aviso só guarda "lida" em memória; aqui a lista em cache é a que a tela marca, e a cada leitura os lidos vão para o estado (`avisosLidos`), que Sair e Excluir conta recomeçam e que sobrevive ao processo. A lista é refeita quando o estado é resetado ou o dia muda, para as datas relativas ("Hoje", "Ontem") não envelhecerem.
final class AvisosDemo {

    private static final String SINDICO = "Carlos Menezes (Síndico)";

    private final EstadoDemo estado;

    private List<Aviso> cache;
    private int geracao;
    private String dia = "";

    AvisosDemo(@NonNull EstadoDemo estado) {
        this.estado = estado;
    }

    @NonNull
    List<Aviso> lista() {
        synchronized (estado) {
            String hoje = SementeDemo.formatarData(estado.agora()).substring(0, 10);
            boolean resetado = cache != null && estado.geracao() != geracao;

            // Depois de um reset os flags do cache são de antes dele: não podem voltar para o estado novo.
            if (cache != null && !resetado) {
                gravarLidos();
            }

            if (cache == null || resetado || !hoje.equals(dia)) {
                cache = criar(lidos());
                geracao = estado.geracao();
                dia = hoje;
            }

            return cache;
        }
    }

    private void gravarLidos() {
        Set<Integer> ids = new HashSet<>(lidos());
        boolean mudou = false;

        for (Aviso aviso : cache) {
            if (aviso.isLida() && ids.add(aviso.id)) {
                mudou = true;
            }
        }

        if (!mudou) {
            return;
        }

        JSONArray novo = new JSONArray();

        for (int id : ids) {
            novo.put(id);
        }

        try {
            estado.raiz().put("avisosLidos", novo);

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }

        estado.salvar();
    }

    @NonNull
    private Set<Integer> lidos() {
        Set<Integer> ids = new HashSet<>();
        JSONArray gravados = estado.raiz().optJSONArray("avisosLidos");

        for (int i = 0; gravados != null && i < gravados.length(); i++) {
            ids.add(gravados.optInt(i));
        }

        return ids;
    }

    @NonNull
    private static List<Aviso> criar(@NonNull Set<Integer> lidos) {
        List<Aviso> avisos = new ArrayList<>();

        avisos.add(
                Aviso.de(
                        1,
                        "Coleta seletiva nesta semana",
                        "A cooperativa passa para recolher os recicláveis. Deixe tudo separado e limpo na área de descarte até as 8h.",
                        CategoriaAviso.COLETA,
                        false,
                        SINDICO,
                        3,
                        lidos.contains(1)));
        avisos.add(
                Aviso.de(
                        2,
                        "Manutenção dos coletores",
                        "Os coletores do bloco B ficam fechados para manutenção. Use os do bloco A até a liberação.",
                        CategoriaAviso.MANUTENCAO,
                        false,
                        SINDICO,
                        30,
                        lidos.contains(2)));
        avisos.add(
                Aviso.de(
                        3,
                        "Campanha do óleo de cozinha",
                        "Entregue seu óleo usado em garrafa PET na portaria e ganhe pontos extras no EcoCiente.",
                        CategoriaAviso.CAMPANHA,
                        false,
                        SINDICO,
                        120,
                        lidos.contains(3)));

        return Collections.unmodifiableList(avisos);
    }
}
