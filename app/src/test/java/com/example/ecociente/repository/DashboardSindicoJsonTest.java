package com.example.ecociente.repository;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.example.ecociente.model.ResumoDashboard;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.model.StatusAgendamento;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONObject;
import org.junit.Test;

public class DashboardSindicoJsonTest {

    private static final long AGORA = 1_800_000_000_000L;
    private static final long DIA = 24L * 60 * 60 * 1000;

    private static String postagem(int usuario, String categoria, String status, int diasAtras) {
        String data = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(new Date(AGORA - diasAtras * DIA));

        return "{\"usuarioId\":" + usuario + ",\"categoria\":\"" + categoria + "\",\"statusValidacao\":\"" + status
                + "\",\"dataPostagem\":\"" + data + "\"}";
    }

    private static JSONObject envelope(String... postagens) throws Exception {
        return new JSONObject("{\"postagens\":[" + String.join(",", postagens) + "]}");
    }

    private static Solicitacao coleta(int cooperativa, StatusAgendamento status) {
        return new Solicitacao(1, 3, cooperativa, "2026-10-01T10:00:00", null, status, false);
    }

    @Test
    public void contaDescartesEMoradoresIgnorandoReprovadosEAntigos() throws Exception {
        ResumoDashboard resumo = DashboardSindicoJson.resumir(
                envelope(
                        postagem(1, "Papel", "aprovada", 1),
                        postagem(1, "Plástico", "em_analise", 2),
                        postagem(2, "Papel", "aprovada", 10),
                        postagem(3, "Vidro", "reprovada", 3),
                        postagem(4, "Papel", "aprovada", 45)),
                Collections.emptyList(), new HashMap<>(), AGORA);

        assertEquals(3, resumo.getDescartes());
        assertEquals(2, resumo.getMoradores());
    }

    @Test
    public void separaOsDescartesEmQuatroSemanas() throws Exception {
        ResumoDashboard resumo = DashboardSindicoJson.resumir(
                envelope(
                        postagem(1, "Papel", "aprovada", 1),
                        postagem(1, "Papel", "aprovada", 6),
                        postagem(1, "Papel", "aprovada", 8),
                        postagem(1, "Papel", "aprovada", 20),
                        postagem(1, "Papel", "aprovada", 27)),
                Collections.emptyList(), new HashMap<>(), AGORA);

        assertArrayEquals(new int[] {1, 1, 1, 2}, resumo.getDescartesPorSemana());
    }

    @Test
    public void percentuaisDosMateriaisFecham100() throws Exception {
        ResumoDashboard resumo = DashboardSindicoJson.resumir(
                envelope(
                        postagem(1, "Papel", "aprovada", 1),
                        postagem(1, "Papel", "aprovada", 2),
                        postagem(1, "Vidro", "aprovada", 3)),
                Collections.emptyList(), new HashMap<>(), AGORA);

        assertEquals(Arrays.asList("Papel", "Vidro"), Arrays.asList(resumo.getMateriais()));
        assertArrayEquals(new int[] {67, 33}, resumo.getPercentuais());
    }

    @Test
    public void semDescartesNaoHaMateriais() throws Exception {
        ResumoDashboard resumo = DashboardSindicoJson.resumir(
                envelope(), Collections.emptyList(), new HashMap<>(), AGORA);

        assertEquals(0, resumo.getMateriais().length);
        assertEquals(0, resumo.getPercentuais().length);
    }

    @Test
    public void soColetasRealizadasContamPorCooperativa() {
        List<Solicitacao> coletas = new ArrayList<>();
        coletas.add(coleta(4, StatusAgendamento.REALIZADO));
        coletas.add(coleta(4, StatusAgendamento.REALIZADO));
        coletas.add(coleta(5, StatusAgendamento.REALIZADO));
        coletas.add(coleta(5, StatusAgendamento.CANCELADO));
        coletas.add(coleta(5, StatusAgendamento.AGENDADO));

        Map<Integer, String> nomes = new HashMap<>();
        nomes.put(4, "Cooperativa Estrela do Mar");

        try {
            ResumoDashboard resumo = DashboardSindicoJson.resumir(envelope(), coletas, nomes, AGORA);

            assertEquals(3, resumo.getColetasRealizadas());
            assertEquals(Arrays.asList("Cooperativa…", "Coop. 5"), Arrays.asList(resumo.getCooperativas()));
            assertArrayEquals(new int[] {2, 1}, resumo.getColetasPorCooperativa());

        } catch (Exception erro) {
            throw new AssertionError(erro);
        }
    }

    @Test
    public void abreviaNomesLongos() {
        assertEquals("Coop Sol", DashboardSindicoJson.abreviar("  Coop Sol "));
        assertEquals("Cooperativa…", DashboardSindicoJson.abreviar("Cooperativa Estrela"));
    }
}
