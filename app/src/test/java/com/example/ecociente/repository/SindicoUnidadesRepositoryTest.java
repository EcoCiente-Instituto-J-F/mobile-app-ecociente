package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.SindicoApartamento;
import com.example.ecociente.model.SindicoDadosCondominio;
import org.junit.Test;
import com.example.ecociente.model.SindicoItemUnidade;
import java.util.Arrays;

public class SindicoUnidadesRepositoryTest {

    private static SindicoUnidadesRepository preenchido() {
        SindicoUnidadesRepository repositorio = new SindicoUnidadesRepository();

        repositorio.salvarDados(new SindicoDadosCondominio(false, "Raio de Luz", "Rua A", "10", "Bloco 2", "Recife", "PE", "50000-000"));
        repositorio.salvarFotoUri("content://foto/1");
        repositorio.salvarBloco(-1, "Torre A", 12);
        repositorio.salvarBloco(-1, "Torre B", 8);
        repositorio.salvarArea(-1, "Lixeira central", "Térreo");
        repositorio.salvarApartamento(-1, 1, "101", "Ana", "81 99999-0000");

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
        assertEquals(2, novo.getBlocos().size());
        assertEquals("Torre B", novo.getBlocos().get(1).getNome());
        assertEquals("12 unidades", novo.getBlocos().get(0).getDetalhe());
        assertEquals("Lixeira central", novo.getAreas().get(0).getNome());

        SindicoApartamento apartamento = novo.getApartamentos(1).get(0);

        assertEquals("101", apartamento.getNumero());
        assertEquals("Ana", apartamento.getMorador());
    }

    @Test
    public void osIdsContinuamDepoisDeRecarregar() {
        SindicoUnidadesRepository novo = new SindicoUnidadesRepository();

        novo.deJson(preenchido().paraJson());
        novo.salvarBloco(-1, "Torre C", 5);

        assertEquals(3, novo.getBlocos().size());
        assertEquals(4, novo.getBlocos().get(2).getId());
    }

    @Test
    public void semDadosGuardadosFicaVazio() {
        SindicoUnidadesRepository repositorio = preenchido();

        repositorio.deJson(null);

        assertTrue(repositorio.getBlocos().isEmpty());
        assertTrue(repositorio.getAreas().isEmpty());
        assertNull(repositorio.getFotoUri());
        assertEquals("", repositorio.getDados().getNome());
    }

    @Test
    public void jsonInvalidoNaoQuebraEFicaVazio() {
        SindicoUnidadesRepository repositorio = preenchido();

        repositorio.deJson("{isso nao e json");

        assertTrue(repositorio.getBlocos().isEmpty());
        assertTrue(repositorio.getAreas().isEmpty());
    }

    @Test
    public void excluirBlocoTiraAsUnidadesDele() {
        SindicoUnidadesRepository repositorio = preenchido();

        repositorio.excluirBloco(1);

        assertEquals(1, repositorio.getBlocos().size());
        assertTrue(repositorio.getApartamentos(1).isEmpty());
    }

    @Test
    public void naoPermiteNumeroRepetidoNoMesmoBloco() {
        SindicoUnidadesRepository repositorio = preenchido();

        assertTrue(repositorio.existeNumero(1, -1, "101"));
        assertTrue(!repositorio.existeNumero(2, -1, "101"));
    }

    @Test
    public void torresDaApiViramBlocosSemDuplicar() {
        SindicoUnidadesRepository repositorio = preenchido();

        boolean acrescentou = repositorio.aplicarTorresDaApi(Arrays.asList(
                new SindicoItemUnidade(10, "torre a", ""),
                new SindicoItemUnidade(11, "Torre C", "")), "Torre do condomínio");

        assertTrue(acrescentou);
        assertEquals(3, repositorio.getBlocos().size());
        assertEquals("Torre C", repositorio.getBlocos().get(2).getNome());
        assertEquals("Torre do condomínio", repositorio.getBlocos().get(2).getDetalhe());
        assertEquals(false, repositorio.aplicarTorresDaApi(Arrays.asList(new SindicoItemUnidade(11, "Torre C", "")), "x"));
    }
}
