package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.SindicoCooperativa;
import java.util.List;
import org.json.JSONObject;
import org.junit.Test;

public class CooperativaJsonTest {

    private static final String ENVELOPE =
            "{\"total\":2,\"pagina\":0,\"tamanho\":10,\"totalPaginas\":1,\"cooperativas\":["
                    + "{\"id\":4,\"nome\":\"Cooperativa A\",\"logradouro\":\"Avenida Coop A\",\"numero\":\"100\","
                    + "\"cidade\":\"São Paulo\",\"estado\":\"SP\",\"cep\":\"01310100\","
                    + "\"enderecoCompleto\":\"Avenida Coop A 100, São Paulo - SP, 01310-100\","
                    + "\"mediaAvaliacoes\":4.0,\"totalAvaliacoes\":2},"
                    + "{\"id\":5,\"nome\":\"Cooperativa B\",\"logradouro\":null,\"numero\":null,"
                    + "\"enderecoCompleto\":\"\",\"mediaAvaliacoes\":null,\"totalAvaliacoes\":0},"
                    + "{\"id\":0,\"nome\":\"Sem id\"}]}";

    @Test
    public void leAsCooperativasDoEnvelope() throws Exception {
        List<SindicoCooperativa> lista = CooperativaJson.cooperativas(new JSONObject(ENVELOPE));

        assertEquals(2, lista.size());
        assertEquals(4, lista.get(0).getId());
        assertEquals("Cooperativa A", lista.get(0).getNome());
        assertEquals("Avenida Coop A 100", lista.get(0).getEndereco());
        assertEquals("São Paulo", lista.get(0).getCidade());
        assertEquals("Avenida Coop A 100, São Paulo - SP, 01310-100", lista.get(0).getEnderecoCompleto());
        assertEquals(2, lista.get(0).getTotalAvaliacoes());
    }

    @Test
    public void mediaSaiNoFormatoBrasileiroEVazioQuandoNaoHaAvaliacao() throws Exception {
        List<SindicoCooperativa> lista = CooperativaJson.cooperativas(new JSONObject(ENVELOPE));

        assertEquals("4,0", lista.get(0).getAvaliacao());
        assertEquals("", lista.get(1).getAvaliacao());
    }

    @Test
    public void camposNulosViramTextoVazio() throws Exception {
        SindicoCooperativa semEndereco = CooperativaJson.cooperativas(new JSONObject(ENVELOPE)).get(1);

        assertEquals("", semEndereco.getEndereco());
        assertEquals("", semEndereco.getEnderecoCompleto());
    }

    @Test
    public void ignoraItemSemIdEEnvelopeSemLista() throws Exception {
        assertTrue(CooperativaJson.cooperativas(new JSONObject("{\"total\":0}")).isEmpty());
    }

    @Test
    public void buscaPeloNomeIgnoraAcentoEMaiuscula() throws Exception {
        SindicoCooperativa cooperativa = CooperativaJson.cooperativas(new JSONObject(ENVELOPE)).get(0);

        assertTrue(cooperativa.corresponde("cooperativa a"));
        assertTrue(cooperativa.corresponde(""));
    }
}
