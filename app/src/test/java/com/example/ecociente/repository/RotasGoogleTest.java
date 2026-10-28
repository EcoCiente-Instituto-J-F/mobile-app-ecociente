package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.SindicoCooperativa;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class RotasGoogleTest {

    private static SindicoCooperativa cooperativa(int id, String enderecoCompleto) {
        return new SindicoCooperativa(id, "Coop " + id, "", 0, "", "", "", "", enderecoCompleto);
    }

    private static List<SindicoCooperativa> lote() {
        return Arrays.asList(cooperativa(10, "Rua A, 1, Recife - PE"), cooperativa(20, "Rua B, 2, Recife - PE"));
    }

    @Test
    public void corpoTemAOrigemPorCoordenadaEOsDestinosPorEndereco() throws JSONException {
        JSONObject corpo = RotasGoogle.montarCorpo(-8.05, -34.9, lote());

        assertEquals("DRIVE", corpo.getString("travelMode"));

        JSONObject origem = corpo.getJSONArray("origins").getJSONObject(0)
                .getJSONObject("waypoint").getJSONObject("location").getJSONObject("latLng");
        assertEquals(-8.05, origem.getDouble("latitude"), 0.0);
        assertEquals(-34.9, origem.getDouble("longitude"), 0.0);

        JSONArray destinos = corpo.getJSONArray("destinations");
        assertEquals(2, destinos.length());
        assertEquals("Rua A, 1, Recife - PE", destinos.getJSONObject(0).getJSONObject("waypoint").getString("address"));
        assertEquals("Rua B, 2, Recife - PE", destinos.getJSONObject(1).getJSONObject("waypoint").getString("address"));
    }

    @Test
    public void respostaLigaOIndiceDoDestinoAoIdDaCooperativa() throws JSONException {
        String resposta = "[{\"originIndex\":0,\"destinationIndex\":1,\"distanceMeters\":3200,\"condition\":\"ROUTE_EXISTS\"},"
                + "{\"originIndex\":0,\"destinationIndex\":0,\"distanceMeters\":850,\"condition\":\"ROUTE_EXISTS\"}]";

        Map<Integer, Float> distancias = RotasGoogle.lerResposta(resposta, lote());

        assertEquals(2, distancias.size());
        assertEquals(850f, distancias.get(10), 0f);
        assertEquals(3200f, distancias.get(20), 0f);
    }

    @Test
    public void rotaInexistenteNaoEntraNoResultado() throws JSONException {
        String resposta = "[{\"originIndex\":0,\"destinationIndex\":0,\"condition\":\"ROUTE_NOT_FOUND\"},"
                + "{\"originIndex\":0,\"destinationIndex\":1,\"distanceMeters\":500,\"condition\":\"ROUTE_EXISTS\"}]";

        Map<Integer, Float> distancias = RotasGoogle.lerResposta(resposta, lote());

        assertEquals(1, distancias.size());
        assertEquals(500f, distancias.get(20), 0f);
    }

    @Test
    public void indiceForaDoLoteEIgnorado() throws JSONException {
        String resposta = "[{\"originIndex\":0,\"destinationIndex\":7,\"distanceMeters\":500,\"condition\":\"ROUTE_EXISTS\"}]";

        assertTrue(RotasGoogle.lerResposta(resposta, lote()).isEmpty());
    }

    @Test(expected = JSONException.class)
    public void respostaQueNaoEJsonLancaExcecao() throws JSONException {
        RotasGoogle.lerResposta("<html>erro</html>", lote());
    }
}
