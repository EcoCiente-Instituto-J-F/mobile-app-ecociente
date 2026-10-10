package com.example.ecociente;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.example.ecociente.ui.FormatoDataApi;
import java.util.Calendar;
import org.junit.Test;

public class FormatoDataApiTest {

    @Test
    public void formataDataEHora() {
        assertEquals("17/05/2026", FormatoDataApi.data("2026-05-17T08:00:00"));
        assertEquals("08:00", FormatoDataApi.hora("2026-05-17T08:00:00"));
    }

    @Test
    public void ignoraFracaoDeSegundos() {
        assertEquals("17/05/2026", FormatoDataApi.data("2026-05-17T08:00:00.123456"));
    }

    @Test
    public void horaCurtaOmiteMinutosZerados() {
        assertEquals("8h", FormatoDataApi.horaCurta("2026-05-17T08:00:00"));
        assertEquals("12h30", FormatoDataApi.horaCurta("2026-05-17T12:30:00"));
    }

    @Test
    public void dataExtensaComecaMaiuscula() {
        String texto = FormatoDataApi.dataExtensa("2026-05-17T08:00:00");

        assertEquals("2026", texto.substring(texto.length() - 4));
        assertEquals(Character.toUpperCase(texto.charAt(0)), texto.charAt(0));
    }

    @Test
    public void valoresInvalidosViramVazio() {
        assertEquals("", FormatoDataApi.data(null));
        assertEquals("", FormatoDataApi.hora("texto"));
        assertEquals("", FormatoDataApi.horaCurta("2026-13"));
        assertNull(FormatoDataApi.calendario("curto"));
    }

    @Test
    public void calendarioPreservaOsCampos() {
        Calendar calendario = FormatoDataApi.calendario("2026-05-17T08:30:00");

        assertNotNull(calendario);
        assertEquals(2026, calendario.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, calendario.get(Calendar.MONTH));
        assertEquals(17, calendario.get(Calendar.DAY_OF_MONTH));
        assertEquals(8, calendario.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, calendario.get(Calendar.MINUTE));
    }

    @Test
    public void tempoRelativo() {
        long agora = FormatoDataApi.calendario("2026-05-10T12:00:00").getTimeInMillis();

        assertEquals("Agora", FormatoDataApi.relativa("2026-05-10T12:00:00", agora));
        assertEquals("Há 30 min", FormatoDataApi.relativa("2026-05-10T11:30:00", agora));
        assertEquals("Há 5 h", FormatoDataApi.relativa("2026-05-10T07:00:00", agora));
        assertEquals("Ontem", FormatoDataApi.relativa("2026-05-09T11:00:00", agora));
        assertEquals("Há 3 dias", FormatoDataApi.relativa("2026-05-07T11:00:00", agora));
        assertEquals("", FormatoDataApi.relativa(null, agora));
        assertEquals("1", FormatoDataApi.relativa("2026-04-01T12:00:00", agora).substring(0, 1));
    }
}
