package com.example.ecociente.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.Aviso;
import com.example.ecociente.util.FormatoDataApi;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class AvisosDemoTest {

    private static long dias(Aviso aviso) {
        Calendar enviado = FormatoDataApi.calendario(aviso.enviadoEm);

        return TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - enviado.getTimeInMillis());
    }

    @Test
    public void tresAvisosDoSindico_emHojeOntemEAnteriores() {
        assertEquals(3, AvisosDemo.lista().size());
        assertEquals(0, dias(AvisosDemo.lista().get(0)));
        assertEquals(1, dias(AvisosDemo.lista().get(1)));
        assertTrue(dias(AvisosDemo.lista().get(2)) >= 2);
    }

    @Test
    public void marcarComoLida_valeNaMesmaLista() {
        Aviso.definirFonte(AvisosDemo::lista);

        assertSame(AvisosDemo.lista(), AvisosDemo.lista());

        int antes = Aviso.quantidadeNaoLidos();

        Aviso.lista().get(0).marcarComoLida();

        assertTrue(Aviso.quantidadeNaoLidos() <= antes);
        assertTrue(Aviso.porId(Aviso.lista().get(0).id).isLida());
    }
}
