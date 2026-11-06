package com.example.ecociente.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import org.json.JSONObject;
import org.junit.Test;

public class EstadoDemoTest {

    private static final long AGORA = 1_700_000_000_000L;

    @Test
    public void armazemVazio_comecaNaSemente() throws Exception {
        ArmazemEmMemoria armazem = new ArmazemEmMemoria();

        EstadoDemo estado = new EstadoDemo(armazem, () -> AGORA);

        assertEquals(SementeDemo.NOME, estado.raiz().getJSONObject("me").getString("nome"));
        assertNotNull("a semente deve ser gravada na criação", armazem.json);
    }

    @Test
    public void jsonCorrompido_voltaParaSemente() throws Exception {
        ArmazemEmMemoria armazem = new ArmazemEmMemoria();
        armazem.json = "{isto não é json";

        EstadoDemo estado = new EstadoDemo(armazem, () -> AGORA);

        assertEquals(SementeDemo.NOME, estado.raiz().getJSONObject("me").getString("nome"));
        assertNotNull(new JSONObject(armazem.json).optJSONObject("me"));
    }

    @Test
    public void alteracao_sobreviveARecarregar() throws Exception {
        ArmazemEmMemoria armazem = new ArmazemEmMemoria();
        EstadoDemo estado = new EstadoDemo(armazem, () -> AGORA);

        estado.raiz().getJSONObject("me").put("nome", "Outro Nome");
        estado.salvar();

        EstadoDemo recarregado = new EstadoDemo(armazem, () -> AGORA);

        assertEquals("Outro Nome", recarregado.raiz().getJSONObject("me").getString("nome"));
    }

    @Test
    public void resetar_apagaArquivoEFotos_eRecriaSemente() throws Exception {
        File pasta = Files.createTempDirectory("demo-estado").toFile();
        ArmazemDemoArquivos armazem = new ArmazemDemoArquivos(pasta);
        EstadoDemo estado = new EstadoDemo(armazem, () -> AGORA);

        String urlFoto = armazem.gravarFoto(new byte[] {1, 2, 3});
        File foto = new File(java.net.URI.create(urlFoto));
        assertTrue(foto.exists());

        estado.raiz().getJSONObject("me").put("nome", "Mudou");
        estado.salvar();

        estado.resetar();

        assertFalse("a foto deve ser apagada", foto.exists());
        assertEquals(SementeDemo.NOME, estado.raiz().getJSONObject("me").getString("nome"));
        assertEquals(
                SementeDemo.NOME,
                new JSONObject(armazem.ler()).getJSONObject("me").getString("nome"));
    }

    @Test
    public void arquivoCorrompidoNoDisco_voltaParaSemente() throws Exception {
        File pasta = Files.createTempDirectory("demo-corrompido").toFile();
        ArmazemDemoArquivos armazem = new ArmazemDemoArquivos(pasta);
        armazem.salvar("{\"me\":");

        EstadoDemo estado = new EstadoDemo(armazem, () -> AGORA);

        assertEquals(SementeDemo.NOME, estado.raiz().getJSONObject("me").getString("nome"));
    }

    @Test
    public void armazemDeArquivos_semArquivo_lerDevolveNull() throws Exception {
        ArmazemDemoArquivos armazem =
                new ArmazemDemoArquivos(Files.createTempDirectory("demo-vazio").toFile());

        assertNull(armazem.ler());
    }
}
