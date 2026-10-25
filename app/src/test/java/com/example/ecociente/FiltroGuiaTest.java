package com.example.ecociente;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.FiltroGuia;
import com.example.ecociente.model.ItemLista;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class FiltroGuiaTest {

    private static String texto(int palavras) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < palavras; i++) {
            sb.append("palavra ");
        }
        return sb.toString();
    }

    private static ItemLista aula(int id, String titulo, String curso, int palavras) {
        return new ItemLista(
                "aula:" + id,
                titulo,
                curso,
                "",
                new String[][] {{"Curso", curso}, {FiltroGuia.ROTULO_CONTEUDO, texto(palavras)}});
    }

    private static List<ItemLista> guia() {
        return Arrays.asList(
                aula(1, "Vidro", "Reciclagem", 100),
                aula(2, "Papel", "Reciclagem", 500),
                aula(3, "Compostagem", "Casa", 1500),
                aula(4, "Abelha", "Casa", 900));
    }

    @Test
    public void minutosDeLeituraUsa200PalavrasPorMinutoComMinimoDe1() {
        assertEquals(1, FiltroGuia.minutosDeLeitura(""));
        assertEquals(1, FiltroGuia.minutosDeLeitura("   "));
        assertEquals(1, FiltroGuia.minutosDeLeitura(texto(200)));
        assertEquals(2, FiltroGuia.minutosDeLeitura(texto(201)));
        assertEquals(8, FiltroGuia.minutosDeLeitura(texto(1500)));
    }

    @Test
    public void semFiltroDevolveTudoNaOrdemDoCurso() {
        FiltroGuia filtro = new FiltroGuia();

        assertFalse(filtro.ativo());
        assertEquals(4, filtro.aplicar(guia()).size());
        assertEquals("Vidro", filtro.aplicar(guia()).get(0).titulo);
    }

    @Test
    public void filtraPorCategoria() {
        FiltroGuia filtro = new FiltroGuia();
        filtro.getCategorias().add("Casa");

        List<ItemLista> resultado = filtro.aplicar(guia());

        assertTrue(filtro.ativo());
        assertEquals(2, resultado.size());
        assertEquals("Compostagem", resultado.get(0).titulo);
    }

    @Test
    public void filtraPorTempoDeLeitura() {
        FiltroGuia filtro = new FiltroGuia();
        filtro.getTempos().add(FiltroGuia.Tempo.ATE_3);

        List<ItemLista> resultado = filtro.aplicar(guia());

        assertEquals(2, resultado.size());
        assertEquals("Vidro", resultado.get(0).titulo);
        assertEquals("Papel", resultado.get(1).titulo);

        filtro.getTempos().clear();
        filtro.getTempos().add(FiltroGuia.Tempo.MAIS_DE_6);

        assertEquals("Compostagem", filtro.aplicar(guia()).get(0).titulo);
    }

    @Test
    public void ordemAlfabeticaIgnoraAcentoEMaiuscula() {
        FiltroGuia filtro = new FiltroGuia();
        filtro.setOrdem(FiltroGuia.Ordem.A_Z);

        List<ItemLista> resultado = filtro.aplicar(guia());

        assertEquals("Abelha", resultado.get(0).titulo);
        assertEquals("Vidro", resultado.get(3).titulo);
    }

    @Test
    public void filtrosSeCombinamELimparVoltaAoPadrao() {
        FiltroGuia filtro = new FiltroGuia();
        filtro.getCategorias().add("Reciclagem");
        filtro.getTempos().add(FiltroGuia.Tempo.ATE_3);
        filtro.setOrdem(FiltroGuia.Ordem.A_Z);

        List<ItemLista> resultado = filtro.aplicar(guia());

        assertEquals("Papel", resultado.get(0).titulo);

        filtro.limpar();

        assertFalse(filtro.ativo());
        assertEquals(4, filtro.aplicar(guia()).size());
    }

    @Test
    public void categoriasSaemUmaVezNaOrdemQueAparecem() {
        assertEquals(Arrays.asList("Reciclagem", "Casa"), FiltroGuia.categoriasDe(guia()));
        assertTrue(FiltroGuia.categoriasDe(new ArrayList<>()).isEmpty());
    }

    @Test
    public void itemSemConteudoContaComoLeituraDe1Minuto() {
        ItemLista semDetalhes = new ItemLista("x", "Curso", "");

        assertEquals("", FiltroGuia.conteudoDe(semDetalhes));

        FiltroGuia filtro = new FiltroGuia();
        filtro.getTempos().add(FiltroGuia.Tempo.ATE_3);

        assertEquals(1, filtro.aplicar(Arrays.asList(semDetalhes)).size());
    }
}
