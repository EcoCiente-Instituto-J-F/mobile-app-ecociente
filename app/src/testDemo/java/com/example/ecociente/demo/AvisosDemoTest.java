package com.example.ecociente.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.Aviso;
import com.example.ecociente.util.FormatoDataApi;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class AvisosDemoTest {

    private static final long AGORA = 1_700_000_000_000L;
    private static final long DIA = TimeUnit.DAYS.toMillis(1);

    private final long[] relogio = {AGORA};

    private ArmazemEmMemoria armazem;
    private EstadoDemo estado;
    private AvisosDemo avisos;

    @Before
    public void preparar() {
        armazem = new ArmazemEmMemoria();
        abrir();
    }

    private void abrir() {
        estado = new EstadoDemo(armazem, () -> relogio[0]);
        avisos = new AvisosDemo(estado);
        Aviso.definirFonte(avisos::lista);
    }

    private static long dias(Aviso aviso) {
        Calendar enviado = FormatoDataApi.calendario(aviso.enviadoEm);

        return TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - enviado.getTimeInMillis());
    }

    @Test
    public void comecaTudoNaoLido_comUmHojeUmOntemEUmAnterior() {
        List<Aviso> lista = avisos.lista();

        assertEquals(3, lista.size());
        assertEquals(3, Aviso.quantidadeNaoLidos());
        assertEquals(0, dias(lista.get(0)));
        assertEquals(1, dias(lista.get(1)));
        assertTrue(dias(lista.get(2)) >= 2);

        int hoje = 0;

        for (Aviso aviso : lista) {
            if (dias(aviso) < 1) {
                hoje++;
            }
        }

        assertEquals(1, hoje);
    }

    @Test
    public void marcarComoLida_diminuiOContadorExatamenteUm() {
        Aviso.porId(2).marcarComoLida();

        assertEquals(2, Aviso.quantidadeNaoLidos());
        assertTrue(Aviso.porId(2).isLida());
        assertFalse(Aviso.porId(1).isLida());
    }

    @Test
    public void lidaSobrevive_aRecriarEstadoEAvisosNoMesmoArmazem() {
        Aviso.porId(1).marcarComoLida();
        avisos.lista();

        abrir();

        assertEquals(2, Aviso.quantidadeNaoLidos());
        assertTrue(Aviso.porId(1).isLida());
    }

    @Test
    public void resetar_voltaTudoParaNaoLido_eAInstanciaVelhaNaoRessuscita() {
        Aviso.porId(1).marcarComoLida();
        Aviso.porId(3).marcarComoLida();
        avisos.lista();

        estado.resetar();

        assertEquals(3, Aviso.quantidadeNaoLidos());

        // Mesmo lendo de novo, o flag antigo não volta para o estado novo.
        assertEquals(3, Aviso.quantidadeNaoLidos());
        assertEquals(0, estado.raiz().optJSONArray("avisosLidos").length());
    }

    @Test
    public void sairEExcluirConta_voltamTudoParaNaoLido() {
        ProvedorContaDemo provedor = new ProvedorContaDemo(estado, () -> {});

        provedor.entrar(SementeDemo.EMAIL, SementeDemo.SENHA);
        Aviso.porId(1).marcarComoLida();
        avisos.lista();
        provedor.sair();

        assertEquals(3, Aviso.quantidadeNaoLidos());

        provedor.entrar(SementeDemo.EMAIL, SementeDemo.SENHA);
        Aviso.porId(2).marcarComoLida();
        avisos.lista();

        assertTrue(provedor.excluirConta(SementeDemo.SENHA).isSucesso());
        assertEquals(3, Aviso.quantidadeNaoLidos());
    }

    @Test
    public void mudancaDeDia_refazAListaEMantemOsLidos() {
        List<Aviso> antes = avisos.lista();

        Aviso.porId(2).marcarComoLida();
        assertSame(antes, avisos.lista());

        relogio[0] += DIA;

        List<Aviso> depois = avisos.lista();

        assertNotSame(antes, depois);
        assertTrue(Aviso.porId(2).isLida());
        assertEquals(2, Aviso.quantidadeNaoLidos());
    }
}
