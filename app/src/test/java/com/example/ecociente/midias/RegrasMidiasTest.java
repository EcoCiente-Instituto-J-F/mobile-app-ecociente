package com.example.ecociente.midias;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class RegrasMidiasTest {

    private static Midia icone(String id, String tema) {
        return new Midia(id, Midia.ICONE_CONTEUDO, tema, "cloudinary", "cursos/" + id, "png", "",
                "", "ncwfxyfi", 1, false, false, false, new ArrayList<>());
    }

    private static RegrasMidias regrasComIcones(int quantidade, String tema) {
        List<Midia> midias = new ArrayList<>();
        for (int i = 1; i <= quantidade; i++) midias.add(icone("icone_" + i, tema));
        return new RegrasMidias(midias, new LinkedHashMap<>());
    }

    // Mesmo laço do MidiasEco.prepararCursos: cursos em ordem de id, cada um evitando a imagem do anterior.
    private static List<String> distribuir(RegrasMidias regras, int cursos, Map<String, String> vinculos) {
        List<String> escolhidas = new ArrayList<>();
        String anterior = null;
        for (int id = 1; id <= cursos; id++) {
            String chave = Midia.ICONE_CONTEUDO + ":curso:" + id;
            Midia m = regras.escolherFixa(Midia.ICONE_CONTEUDO, "geral", chave, vinculos, anterior);
            assertNotNull(m);
            vinculos.put(chave, m.id);
            escolhidas.add(m.id);
            anterior = m.id;
        }
        return escolhidas;
    }

    @Test
    public void quarentaCursosEQuinzeImagensUsamCadaImagemDuasOuTresVezes() {
        List<String> escolhidas = distribuir(regrasComIcones(15, "geral"), 40, new HashMap<>());

        Map<String, Integer> usos = new HashMap<>();
        for (String id : escolhidas) usos.merge(id, 1, Integer::sum);

        assertEquals(15, usos.size());
        for (int uso : usos.values()) assertTrue("uso " + uso, uso >= 2 && uso <= 3);
    }

    @Test
    public void cursosVizinhosNuncaRepetemAImagem() {
        List<String> escolhidas = distribuir(regrasComIcones(15, "geral"), 40, new HashMap<>());

        for (int i = 1; i < escolhidas.size(); i++) {
            assertNotEquals("cursos " + i + " e " + (i + 1), escolhidas.get(i - 1), escolhidas.get(i));
        }
    }

    @Test
    public void umaImagemSoRepeteQuandoNaoHaOutra() {
        RegrasMidias regras = regrasComIcones(1, "geral");
        Map<String, String> vinculos = new HashMap<>();

        Midia primeira = regras.escolherFixa(Midia.ICONE_CONTEUDO, "geral", "k:1", vinculos, null);
        Midia segunda = regras.escolherFixa(Midia.ICONE_CONTEUDO, "geral", "k:2", vinculos, primeira.id);

        assertEquals(primeira.id, segunda.id);
    }

    @Test
    public void cursoJaVinculadoMantemASuaImagem() {
        RegrasMidias regras = regrasComIcones(15, "geral");
        Map<String, String> vinculos = new HashMap<>();
        vinculos.put("k:7", "icone_3");

        Midia m = regras.escolherFixa(Midia.ICONE_CONTEUDO, "geral", "k:7", vinculos, "icone_3");

        assertEquals("icone_3", m.id);
    }

    @Test
    public void temaSemImagemEspecificaUsaOutraImagemDisponivel() {
        List<Midia> midias = new ArrayList<>();
        midias.add(icone("icone_agua", "agua"));
        midias.add(icone("icone_energia", "energia"));
        midias.add(icone("icone_reciclagem", "reciclagem"));
        RegrasMidias regras = new RegrasMidias(midias, new LinkedHashMap<>());

        Midia escolhida = regras.escolherFixa(
                Midia.ICONE_CONTEUDO, "tema_sem_cadastro", "curso:novo", new HashMap<>());

        assertNotNull(escolhida);
        assertTrue("icone_agua".equals(escolhida.id)
                || "icone_energia".equals(escolhida.id)
                || "icone_reciclagem".equals(escolhida.id));
    }

    @Test
    public void falhaDeImagemRemotaPriorizaUmaAlternativaLocal() {
        List<Midia> midias = new ArrayList<>();
        midias.add(icone("remota_1", "geral"));
        midias.add(new Midia("local_1", Midia.ICONE_CONTEUDO, "geral", "drawable",
                "img_greenpapers", "png", "local_1", "", "", 1,
                false, false, false, new ArrayList<>()));
        RegrasMidias regras = new RegrasMidias(midias, new LinkedHashMap<>());
        java.util.Set<String> tentadas = new java.util.HashSet<>();
        tentadas.add("remota_1");

        Midia alternativa = regras.escolherAlternativaLocal(
                Midia.ICONE_CONTEUDO, tentadas, 123L);

        assertNotNull(alternativa);
        assertEquals("local_1", alternativa.id);
    }

    @Test
    public void aEscolhaEhRepetivel() {
        List<String> a = distribuir(regrasComIcones(15, "geral"), 40, new HashMap<>());
        List<String> b = distribuir(regrasComIcones(15, "geral"), 40, new HashMap<>());

        assertEquals(a, b);
    }
}
