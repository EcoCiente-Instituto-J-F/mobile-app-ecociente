package com.example.ecociente.repository;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.example.ecociente.model.ResumoDashboard;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.model.StatusAgendamento;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.junit.Test;

public class DashboardSindicoJsonTest {

    private static final String RESPOSTA =
            "{\"condominioId\":1,\"nomeCondominio\":\"Condomínio Um\",\"nomeSindico\":\"Síndico Um\",\"totalMoradores\":35,"
                    + "\"materiais\":{\"reciclados\":120,\"emValidacao\":2,\"aguardandoSindico\":1,\"reprovadas\":3,"
                    + "\"porCategoria\":[{\"categoriaId\":1,\"categoria\":\"Plástico\",\"cor\":\"#E53935\",\"quantidade\":80},"
                    + "{\"categoriaId\":2,\"categoria\":\"Papel\",\"cor\":\"#1E88E5\",\"quantidade\":40}]},"
                    + "\"pontos\":{\"total\":900,\"postagens\":600,\"quizzes\":300,\"mesAtual\":150},"
                    + "\"quizzes\":{\"tentativasConcluidas\":10,\"tentativasAprovadas\":7,\"moradoresComQuiz\":6,"
                    + "\"notaMedia\":73.33,\"taxaAprovacao\":70.00,"
                    + "\"porQuiz\":[{\"quizId\":101,\"quiz\":\"Quiz Dois\",\"tentativas\":4,\"aprovadas\":3,\"notaMedia\":80.5},"
                    + "{\"quizId\":100,\"quiz\":\"Compostagem e adubo orgânico\",\"tentativas\":6,\"aprovadas\":4,\"notaMedia\":null}]}}";

    private static Solicitacao coleta(int cooperativa, StatusAgendamento status) {
        return new Solicitacao(1, 3, cooperativa, "2026-10-01T10:00:00", null, status, false);
    }

    private static ResumoDashboard resumir(List<Solicitacao> coletas, Map<Integer, String> nomes) throws Exception {
        return DashboardSindicoJson.resumir(new JSONObject(RESPOSTA), coletas, nomes);
    }

    @Test
    public void leOsNumerosDaApi() throws Exception {
        ResumoDashboard resumo = resumir(Collections.emptyList(), new HashMap<>());

        assertEquals(120, resumo.getReciclados());
        assertEquals(35, resumo.getMoradores());
        assertEquals(150, resumo.getPontosDoMes());
    }

    @Test
    public void percentuaisDosMateriaisFecham100() throws Exception {
        ResumoDashboard resumo = resumir(Collections.emptyList(), new HashMap<>());

        assertEquals(Arrays.asList("Plástico", "Papel"), Arrays.asList(resumo.getMateriais()));
        assertArrayEquals(new int[] {67, 33}, resumo.getPercentuais());
    }

    @Test
    public void agrupaOQueSobraEmOutros() throws Exception {
        JSONObject resposta = new JSONObject(RESPOSTA);
        resposta.getJSONObject("materiais").put("porCategoria", new org.json.JSONArray(
                "[{\"categoria\":\"A\",\"quantidade\":50},{\"categoria\":\"B\",\"quantidade\":20},"
                        + "{\"categoria\":\"C\",\"quantidade\":10},{\"categoria\":\"D\",\"quantidade\":10},"
                        + "{\"categoria\":\"E\",\"quantidade\":5},{\"categoria\":\"F\",\"quantidade\":5}]"));

        ResumoDashboard resumo = DashboardSindicoJson.resumir(resposta, Collections.emptyList(), new HashMap<>());

        assertEquals(Arrays.asList("A", "B", "C", "D", "Outros"), Arrays.asList(resumo.getMateriais()));
        assertArrayEquals(new int[] {50, 20, 10, 10, 10}, resumo.getPercentuais());
    }

    @Test
    public void semMateriaisNaoHaPercentuais() throws Exception {
        JSONObject resposta = new JSONObject(RESPOSTA);
        resposta.getJSONObject("materiais").put("porCategoria", new org.json.JSONArray());

        ResumoDashboard resumo = DashboardSindicoJson.resumir(resposta, Collections.emptyList(), new HashMap<>());

        assertEquals(0, resumo.getMateriais().length);
        assertEquals(0, resumo.getPercentuais().length);
    }

    @Test
    public void notasDosQuizzesViramInteirosENomesCurtos() throws Exception {
        ResumoDashboard resumo = resumir(Collections.emptyList(), new HashMap<>());

        assertEquals(Arrays.asList("Quiz Dois", "Compostagem…"), Arrays.asList(resumo.getQuizzes()));
        // 80,5 arredonda para 81; sem nota média fica 0
        assertArrayEquals(new int[] {81, 0}, resumo.getNotasDosQuizzes());
    }

    @Test
    public void respostaVaziaNaoQuebra() throws Exception {
        ResumoDashboard resumo = DashboardSindicoJson.resumir(new JSONObject("{}"), Collections.emptyList(), new HashMap<>());

        assertEquals(0, resumo.getReciclados());
        assertEquals(0, resumo.getMateriais().length);
        assertEquals(0, resumo.getQuizzes().length);
    }

    @Test
    public void soColetasRealizadasContamPorCooperativa() throws Exception {
        List<Solicitacao> coletas = new ArrayList<>();
        coletas.add(coleta(4, StatusAgendamento.REALIZADO));
        coletas.add(coleta(4, StatusAgendamento.REALIZADO));
        coletas.add(coleta(5, StatusAgendamento.REALIZADO));
        coletas.add(coleta(5, StatusAgendamento.CANCELADO));
        coletas.add(coleta(5, StatusAgendamento.AGENDADO));

        Map<Integer, String> nomes = new HashMap<>();
        nomes.put(4, "Cooperativa Estrela do Mar");

        ResumoDashboard resumo = resumir(coletas, nomes);

        assertEquals(3, resumo.getColetasRealizadas());
        assertEquals(Arrays.asList("Cooperativa…", "Coop. 5"), Arrays.asList(resumo.getCooperativas()));
        assertArrayEquals(new int[] {2, 1}, resumo.getColetasPorCooperativa());
    }

    @Test
    public void nomeDaColetaVenceAListaDeCooperativas() throws Exception {
        List<Solicitacao> coletas = Collections.singletonList(
                coleta(4, StatusAgendamento.REALIZADO).comDadosDoPedido("CONDOMINIO", "Coop Sol"));

        Map<Integer, String> nomes = new HashMap<>();
        nomes.put(4, "Nome antigo");

        assertEquals(Arrays.asList("Coop Sol"), Arrays.asList(resumir(coletas, nomes).getCooperativas()));
    }

    @Test
    public void abreviaNomesLongos() {
        assertEquals("Coop Sol", DashboardSindicoJson.abreviar("  Coop Sol "));
        assertEquals("Cooperativa…", DashboardSindicoJson.abreviar("Cooperativa Estrela"));
    }
}
