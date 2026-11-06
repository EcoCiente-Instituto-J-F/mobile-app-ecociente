package com.example.ecociente.demo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class RespostasEkoTest {

    private static final long AGORA = 1_700_000_000_000L;

    private EstadoDemo estado;

    @Before
    public void preparar() {
        estado = new EstadoDemo(new ArmazemEmMemoria(), () -> AGORA);
    }

    private String ouvir(String mensagem) {
        return RespostasEko.responder(mensagem, estado).toLowerCase();
    }

    @Test
    public void pilha_mencionaPilha() {
        assertTrue(ouvir("onde jogo pilha?").contains("pilha"));
    }

    @Test
    public void oleo_ignoraAcentoECaixa() {
        assertTrue(ouvir("ÓLEO de cozinha").contains("óleo"));
        assertTrue(ouvir("oleo de cozinha").contains("óleo"));
    }

    @Test
    public void vidro_mencionaVidro() {
        assertTrue(ouvir("posso descartar vidro quebrado?").contains("vidro"));
    }

    @Test
    public void pontos_trazOSaldoAtualInclusiveDepoisDeCreditar() {
        assertTrue(ouvir("quantos pontos eu tenho").contains(String.valueOf(estado.saldo())));

        estado.creditar("POSTAGEM", 37);

        assertTrue(ouvir("quantos pontos eu tenho").contains(String.valueOf(estado.saldo())));
    }

    @Test
    public void ranking_trazAPosicaoAtual() {
        assertTrue(ouvir("posição no ranking").contains(estado.posicaoDoUsuario() + "º"));
    }

    @Test
    public void proximaColeta_trazADataDoProximoAgendamento() throws Exception {
        String esperado = null;
        String menor = null;
        String agora = SementeDemo.formatarData(AGORA);
        JSONArray lista = estado.raiz().getJSONArray("agendamentos");

        for (int i = 0; i < lista.length(); i++) {
            JSONObject item = lista.getJSONObject(i);
            String inicio = item.getString("dataInicio");
            String status = item.getString("statusAgendamento");

            if (("AGENDADO".equals(status) || "CONFIRMADO".equals(status))
                    && inicio.compareTo(agora) > 0
                    && (menor == null || inicio.compareTo(menor) < 0)) {
                menor = inicio;
            }
        }

        assertTrue("a semente deve ter coleta futura", menor != null);

        esperado = menor.substring(8, 10) + "/" + menor.substring(5, 7);

        assertTrue(ouvir("qual a próxima coleta").contains(esperado));
    }

    @Test
    public void semColetaFutura_respondeSemQuebrar() throws Exception {
        estado.raiz().put("agendamentos", new JSONArray());

        String resposta = ouvir("quando é a próxima coleta?");

        assertFalse(resposta.trim().isEmpty());
        assertFalse(resposta.contains("null"));
    }

    @Test
    public void semPalavraChave_devolveRespostaPadrao() {
        assertFalse(ouvir("blablabla").trim().isEmpty());
        assertFalse(ouvir("").trim().isEmpty());
    }
}
