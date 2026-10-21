package com.example.ecociente;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.SindicoCooperativa;
import org.junit.Test;

public class SindicoCooperativaTest {

    private static SindicoCooperativa cooperativa(String nome) {
        return new SindicoCooperativa(1, nome, "2 km", "4,5", "Rua A", "Recife", "PE", "50000-000");
    }

    @Test
    public void buscaIgnoraAcentoEMaiuscula() {
        assertTrue(cooperativa("Cooperativa Ecológica").corresponde("ECOLOGICA"));
        assertTrue(cooperativa("Cooperativa Ecológica").corresponde("  coop "));
    }

    @Test
    public void termoVazioCasaComTodas() {
        assertTrue(cooperativa("Qualquer").corresponde(""));
    }

    @Test
    public void nomeDiferenteNaoCasa() {
        assertFalse(cooperativa("Reciclar Mais").corresponde("verde"));
    }
}
