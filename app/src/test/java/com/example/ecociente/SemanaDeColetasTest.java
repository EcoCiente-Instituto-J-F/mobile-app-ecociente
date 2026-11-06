package com.example.ecociente;

import static org.junit.Assert.assertEquals;

import com.example.ecociente.model.SemanaDeColetas;
import java.util.Calendar;
import org.junit.Test;

public class SemanaDeColetasTest {

    private static Calendar dia(int ano, int mes, int diaDoMes, int hora) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(ano, mes - 1, diaDoMes, hora, 30, 15);
        return c;
    }

    private static void assertData(Calendar c, int ano, int mes, int diaDoMes, int hora, int minuto, int segundo) {
        assertEquals(ano, c.get(Calendar.YEAR));
        assertEquals(mes - 1, c.get(Calendar.MONTH));
        assertEquals(diaDoMes, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(hora, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(minuto, c.get(Calendar.MINUTE));
        assertEquals(segundo, c.get(Calendar.SECOND));
    }

    @Test
    public void quartaFeiraVoltaParaASegundaAsZeroHoras() {
        // 14/10/2026 é quarta-feira.
        assertData(SemanaDeColetas.segundaDe(dia(2026, 10, 14, 15)), 2026, 10, 12, 0, 0, 0);
    }

    @Test
    public void segundaFeiraContinuaNoProprioDia() {
        assertData(SemanaDeColetas.segundaDe(dia(2026, 10, 12, 8)), 2026, 10, 12, 0, 0, 0);
    }

    @Test
    public void fimDeSemanaPertenceASemanaQueTermina() {
        // 17/10/2026 é sábado e 18/10/2026 é domingo.
        assertData(SemanaDeColetas.segundaDe(dia(2026, 10, 17, 10)), 2026, 10, 12, 0, 0, 0);
        assertData(SemanaDeColetas.segundaDe(dia(2026, 10, 18, 10)), 2026, 10, 12, 0, 0, 0);
    }

    @Test
    public void semanaCruzaOMesEOAno() {
        // 31/12/2026 é quinta-feira: a semana começa em 28/12.
        assertData(SemanaDeColetas.segundaDe(dia(2026, 12, 31, 9)), 2026, 12, 28, 0, 0, 0);
        assertData(SemanaDeColetas.fimDe(dia(2026, 12, 31, 9)), 2027, 1, 1, 23, 59, 59);
    }

    @Test
    public void fimEASextaAs23h59() {
        assertData(SemanaDeColetas.fimDe(dia(2026, 10, 14, 15)), 2026, 10, 16, 23, 59, 59);
    }

    @Test
    public void naoAlteraOCalendarioRecebido() {
        Calendar original = dia(2026, 10, 14, 15);

        SemanaDeColetas.segundaDe(original);
        SemanaDeColetas.fimDe(original);

        assertData(original, 2026, 10, 14, 15, 30, 15);
    }
}
