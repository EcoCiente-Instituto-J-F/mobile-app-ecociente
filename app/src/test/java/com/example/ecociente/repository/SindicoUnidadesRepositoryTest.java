package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.SindicoDadosCondominio;
import org.junit.Test;

public class SindicoUnidadesRepositoryTest {

    private static SindicoUnidadesRepository preenchido() {
        SindicoUnidadesRepository repositorio = new SindicoUnidadesRepository();

        repositorio.salvarDados(new SindicoDadosCondominio(false, "Raio de Luz", "Rua A", "10", "Bloco 2", "Recife", "PE", "50000-000"));
        repositorio.salvarFotoUri("content://foto/1");

        return repositorio;
    }

    @Test
    public void dadosDaApiSubstituemOQueTrouxeEMantemOResto() {
        SindicoUnidadesRepository repositorio = preenchido();

        repositorio.aplicarDadosDaApi(new SindicoDadosCondominio(true, "Residencial Eco", "Avenida Nova", "200", "", "", "", "01310-100"));

        assertEquals("Residencial Eco", repositorio.getDados().getNome());
        assertEquals("Avenida Nova", repositorio.getDados().getEndereco());
        assertEquals("200", repositorio.getDados().getNumero());
        assertEquals("Bloco 2", repositorio.getDados().getComplemento());
        assertEquals("Recife", repositorio.getDados().getCidade());
        assertEquals("01310-100", repositorio.getDados().getCep());
        assertTrue(repositorio.getDados().isResidencial());
    }

    @Test
    public void tudoQueFoiCadastradoVoltaDoJson() {
        SindicoUnidadesRepository novo = new SindicoUnidadesRepository();

        novo.deJson(preenchido().paraJson());

        assertEquals("Raio de Luz", novo.getDados().getNome());
        assertEquals("10", novo.getDados().getNumero());
        assertEquals("Bloco 2", novo.getDados().getComplemento());
        assertEquals("content://foto/1", novo.getFotoUri());
    }

    @Test
    public void dadosAntigosDeBlocosAreasEApartamentosSaoIgnorados() {
        SindicoUnidadesRepository repositorio = new SindicoUnidadesRepository();

        repositorio.deJson("{\"proximoId\":3,\"blocos\":[{\"id\":1,\"nome\":\"Torre A\",\"detalhe\":\"12 unidades\"}],"
                + "\"areas\":[{\"id\":2,\"nome\":\"Lixeira\",\"detalhe\":\"Térreo\"}],"
                + "\"apartamentos\":[{\"id\":1,\"blocoId\":1,\"numero\":\"101\",\"morador\":\"Ana\",\"telefone\":\"1\"}]}");

        assertEquals("", repositorio.getDados().getNome());
    }

    @Test
    public void semDadosGuardadosFicaVazio() {
        SindicoUnidadesRepository repositorio = preenchido();

        repositorio.deJson(null);

        assertNull(repositorio.getFotoUri());
        assertEquals("", repositorio.getDados().getNome());
    }

    @Test
    public void jsonInvalidoNaoQuebraEFicaVazio() {
        SindicoUnidadesRepository repositorio = preenchido();

        repositorio.deJson("{isso nao e json");

        assertNull(repositorio.getFotoUri());
        assertEquals("", repositorio.getDados().getNome());
    }
}
