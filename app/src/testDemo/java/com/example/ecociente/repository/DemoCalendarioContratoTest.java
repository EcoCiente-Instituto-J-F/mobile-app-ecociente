package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.demo.ApiDemo;
import com.example.ecociente.demo.ArmazemEmMemoria;
import com.example.ecociente.demo.EstadoDemo;
import com.example.ecociente.demo.RespostaDemo;
import com.example.ecociente.demo.SementeDemo;
import com.example.ecociente.model.SemanaDeColetas;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.model.StatusAgendamento;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// Agendamentos da ds-mobile-api falsa, lidos pelo CalendarioJson real e consultados como o CalendarioExternoRepository.
public class DemoCalendarioContratoTest {

    private static final String MOBILE = "https://ds-mobile-api.onrender.com";
    private static final String AGENDAMENTOS = MOBILE + "/api/v1/agendamentos";
    private static final String FORMATO = "yyyy-MM-dd'T'HH:mm:ss";

    private TimeZone fusoOriginal;

    @Before
    public void fixarFuso() {
        fusoOriginal = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
    }

    @After
    public void restaurarFuso() {
        TimeZone.setDefault(fusoOriginal);
    }

    private static long em(int ano, int mes, int dia, int hora) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(ano, mes - 1, dia, hora, 0, 0);
        return c.getTimeInMillis();
    }

    private static String formatar(Calendar c) {
        return new SimpleDateFormat(FORMATO, Locale.US).format(c.getTime());
    }

    private static final class Cenario {
        final long agora;
        final ApiDemo api;
        final String token;

        Cenario(long agora) throws Exception {
            this.agora = agora;
            EstadoDemo estado = new EstadoDemo(new ArmazemEmMemoria(), () -> agora);
            api = new ApiDemo(estado, () -> agora);

            String corpo = new JSONObject().put("email", SementeDemo.EMAIL).put("senha", SementeDemo.SENHA).toString();
            RespostaDemo login = api.tratar("POST", MOBILE + "/auth/login", null, corpo.getBytes(StandardCharsets.UTF_8));

            token = "Bearer " + new JSONObject(login.texto()).getString("token");
        }

        RespostaDemo obter(String url) {
            return api.tratar("GET", url, token, new byte[0]);
        }
    }

    // Mesma consulta do doPeriodo do repositório.
    private static String consultaPeriodo(Calendar inicio, Calendar fim) {
        return AGENDAMENTOS + "?dataInicio=" + formatar(inicio) + "&dataFim=" + formatar(fim) + "&size=100";
    }

    private void verificarSemana(long agora) throws Exception {
        Cenario c = new Cenario(agora);

        Calendar hoje = Calendar.getInstance();
        hoje.setTimeInMillis(agora);

        RespostaDemo r = c.obter(consultaPeriodo(SemanaDeColetas.segundaDe(hoje), SemanaDeColetas.fimDe(hoje)));

        assertEquals(200, r.codigo);

        JSONObject json = new JSONObject(r.texto());
        JSONArray conteudo = json.getJSONArray("content");

        assertTrue("semana vazia em " + formatar(hoje), conteudo.length() >= 1);
        assertTrue(json.getBoolean("last"));

        for (int i = 0; i < conteudo.length(); i++) {
            JSONObject item = conteudo.getJSONObject(i);
            Solicitacao s = CalendarioJson.solicitacao(item);

            assertEquals(item.getString("statusAgendamento"), s.getStatus().name());
            assertNotNull(s.getDataInicio());
            assertTrue(s.getDataInicio().compareTo(formatar(SemanaDeColetas.segundaDe(hoje))) >= 0);
            assertTrue(s.getDataInicio().compareTo(formatar(SemanaDeColetas.fimDe(hoje))) <= 0);
        }
    }

    @Test
    public void semana_emDomingo_temItem() throws Exception {
        verificarSemana(em(2026, 11, 8, 10));
    }

    @Test
    public void semana_em31Dez_temItem() throws Exception {
        verificarSemana(em(2026, 12, 31, 23));
    }

    @Test
    public void semana_noUltimoDiaDeFevereiro_temItem() throws Exception {
        verificarSemana(em(2027, 2, 28, 9));
        verificarSemana(em(2028, 2, 29, 9));
    }

    @Test
    public void semana_emSegundaESexta_temItem() throws Exception {
        verificarSemana(em(2026, 11, 9, 8));
        verificarSemana(em(2026, 11, 13, 18));
    }

    @Test
    public void semana_diasFuturosDaSemanaTemUmCadaUm() throws Exception {
        // Quarta 11/11: quinta e sexta são futuras.
        Cenario c = new Cenario(em(2026, 11, 11, 10));
        Calendar hoje = Calendar.getInstance();
        hoje.setTimeInMillis(c.agora);

        JSONArray conteudo =
                new JSONObject(c.obter(consultaPeriodo(SemanaDeColetas.segundaDe(hoje), SemanaDeColetas.fimDe(hoje))).texto())
                        .getJSONArray("content");
        int futuras = 0;

        for (int i = 0; i < conteudo.length(); i++) {
            if (conteudo.getJSONObject(i).getString("dataInicio").compareTo("2026-11-11T10:00:00") > 0) {
                futuras++;
            }
        }

        assertEquals(2, futuras);
    }

    @Test
    public void proxima_eSempreFutura() throws Exception {
        long[] relogios = {em(2026, 11, 8, 10), em(2026, 12, 31, 23), em(2027, 2, 28, 9), em(2026, 11, 11, 10)};

        for (long agora : relogios) {
            Cenario c = new Cenario(agora);
            RespostaDemo r = c.obter(AGENDAMENTOS + "/proxima");

            assertEquals(200, r.codigo);

            Solicitacao s = CalendarioJson.solicitacao(new JSONObject(r.texto()));
            Calendar agoraCal = Calendar.getInstance();
            agoraCal.setTimeInMillis(agora);

            assertTrue(s.getDataInicio().compareTo(formatar(agoraCal)) > 0);
            assertTrue(
                    s.getStatus() == StatusAgendamento.AGENDADO || s.getStatus() == StatusAgendamento.CONFIRMADO);
        }
    }

    @Test
    public void proxima_semFuturo_devolve404() throws Exception {
        // Relógio muito à frente do calendário que a semente gerou: mesmo estado, tempo depois.
        long[] agora = {em(2026, 11, 11, 10)};
        EstadoDemo estado = new EstadoDemo(new ArmazemEmMemoria(), () -> agora[0]);
        ApiDemo api = new ApiDemo(estado, () -> agora[0]);
        String corpo = new JSONObject().put("email", SementeDemo.EMAIL).put("senha", SementeDemo.SENHA).toString();
        String token =
                "Bearer "
                        + new JSONObject(
                                        api.tratar("POST", MOBILE + "/auth/login", null, corpo.getBytes(StandardCharsets.UTF_8))
                                                .texto())
                                .getString("token");

        agora[0] = em(2030, 1, 1, 10);

        assertEquals(404, api.tratar("GET", AGENDAMENTOS + "/proxima", token, new byte[0]).codigo);
    }

    @Test
    public void listagem_semFiltro_temEnvelopeComTudoOrdenadoDoMaisRecente() throws Exception {
        Cenario c = new Cenario(em(2026, 11, 11, 10));
        JSONObject json = new JSONObject(c.obter(AGENDAMENTOS + "?page=0&size=10").texto());
        JSONArray conteudo = json.getJSONArray("content");

        assertTrue(conteudo.length() >= 7);

        for (int i = 1; i < conteudo.length(); i++) {
            assertTrue(
                    conteudo.getJSONObject(i - 1).getString("dataInicio")
                            .compareTo(conteudo.getJSONObject(i).getString("dataInicio")) >= 0);
        }
    }

    @Test
    public void listagem_paginaEUltima() throws Exception {
        Cenario c = new Cenario(em(2026, 11, 11, 10));

        JSONObject primeira = new JSONObject(c.obter(AGENDAMENTOS + "?page=0&size=2").texto());
        JSONObject ultima = new JSONObject(c.obter(AGENDAMENTOS + "?page=50&size=2").texto());

        assertEquals(2, primeira.getJSONArray("content").length());
        assertFalse(primeira.getBoolean("last"));
        assertEquals(0, ultima.getJSONArray("content").length());
        assertTrue(ultima.getBoolean("last"));
    }

    @Test
    public void listagem_filtraPorStatus() throws Exception {
        Cenario c = new Cenario(em(2026, 11, 11, 10));
        JSONArray conteudo =
                new JSONObject(c.obter(AGENDAMENTOS + "?status=REALIZADO&page=0&size=10").texto()).getJSONArray("content");

        assertEquals(4, conteudo.length());

        for (int i = 0; i < conteudo.length(); i++) {
            assertEquals("REALIZADO", conteudo.getJSONObject(i).getString("statusAgendamento"));
        }
    }

    @Test
    public void listagem_emSegunda_continuaComQuatroRealizadas() throws Exception {
        Cenario c = new Cenario(em(2026, 11, 9, 8));
        JSONArray conteudo =
                new JSONObject(c.obter(AGENDAMENTOS + "?status=REALIZADO&page=0&size=10").texto()).getJSONArray("content");

        assertEquals(4, conteudo.length());
    }

    @Test
    public void semente_temAgendamentosComIdNaRaiz() throws Exception {
        EstadoDemo estado = new EstadoDemo(new ArmazemEmMemoria(), () -> em(2026, 11, 11, 10));
        JSONArray lista = estado.raiz().getJSONArray("agendamentos");

        assertTrue(lista.length() >= 7);

        for (int i = 0; i < lista.length(); i++) {
            assertTrue(lista.getJSONObject(i).getInt("id") > 0);
        }
    }

    @Test
    public void semToken_devolve401() throws Exception {
        Cenario c = new Cenario(em(2026, 11, 11, 10));

        assertEquals(401, c.api.tratar("GET", AGENDAMENTOS, null, new byte[0]).codigo);
        assertEquals(401, c.api.tratar("GET", AGENDAMENTOS + "/proxima", null, new byte[0]).codigo);
    }
}
