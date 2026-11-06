package com.example.ecociente.demo;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class ConexaoDemoTest {

    // Tratador falso: conta as chamadas e guarda o que recebeu.
    private static final class TratadorFalso implements TratadorDemo {
        final AtomicInteger chamadas = new AtomicInteger();
        final List<String> metodos = Collections.synchronizedList(new ArrayList<>());
        volatile String autorizacao;
        volatile byte[] corpo;
        volatile RespostaDemo resposta = new RespostaDemo(200, "{\"ok\":true}");

        @Override
        public RespostaDemo tratar(String metodo, String url, @Nullable String autorizacao, byte[] corpo) {
            chamadas.incrementAndGet();
            metodos.add(metodo + " " + url);
            this.autorizacao = autorizacao;
            this.corpo = corpo;
            return resposta;
        }
    }

    private static HttpURLConnection abrir(TratadorFalso tratador, String url) throws IOException {
        FabricaConexoesDemo fabrica = new FabricaConexoesDemo(tratador, 0, endereco -> null);
        URLStreamHandler handler = fabrica.createURLStreamHandler("https");

        return (HttpURLConnection) new URL(null, url, handler).openConnection();
    }

    private static String texto(InputStream fluxo) throws IOException {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        byte[] buffer = new byte[256];
        int lidos;

        while ((lidos = fluxo.read(buffer)) != -1) {
            saida.write(buffer, 0, lidos);
        }

        return saida.toString("UTF-8");
    }

    @Test
    public void post_processaUmaVezSo() throws Exception {
        TratadorFalso tratador = new TratadorFalso();
        HttpURLConnection conexao = abrir(tratador, "https://ds-mobile-api.onrender.com/auth/login");

        conexao.setRequestMethod("POST");
        conexao.setDoOutput(true);
        try (OutputStream saida = conexao.getOutputStream()) {
            saida.write("{}".getBytes(StandardCharsets.UTF_8));
        }

        assertEquals(200, conexao.getResponseCode());
        conexao.getInputStream();
        assertEquals(200, conexao.getResponseCode());

        assertEquals(1, tratador.chamadas.get());
    }

    @Test
    public void getInputStream_primeiro_funciona() throws Exception {
        TratadorFalso tratador = new TratadorFalso();
        HttpURLConnection conexao = abrir(tratador, "https://ds-mobile-api.onrender.com/api/x");

        assertEquals("{\"ok\":true}", texto(conexao.getInputStream()));
        assertEquals(200, conexao.getResponseCode());
        assertEquals(1, tratador.chamadas.get());
    }

    @Test
    public void erro400_getInputStreamLanca_getErrorStreamTemCorpo() throws Exception {
        TratadorFalso tratador = new TratadorFalso();
        tratador.resposta = RespostaDemo.erro(400, "REQUISICAO_INVALIDA");
        HttpURLConnection conexao = abrir(tratador, "https://ds-mobile-api.onrender.com/api/x");

        assertEquals(400, conexao.getResponseCode());

        try {
            conexao.getInputStream();
            fail("getInputStream deveria lançar em status >= 400");
        } catch (IOException esperado) {
            // ok
        }

        assertTrue(texto(conexao.getErrorStream()).contains("REQUISICAO_INVALIDA"));
    }

    @Test
    public void erro_comGetInputStreamPrimeiro_tambemLancaEGuardaCorpo() throws Exception {
        TratadorFalso tratador = new TratadorFalso();
        tratador.resposta = RespostaDemo.erro(401, "NAO_AUTORIZADO");
        HttpURLConnection conexao = abrir(tratador, "https://ds-mobile-api.onrender.com/api/x");

        try {
            conexao.getInputStream();
            fail();
        } catch (IOException esperado) {
            // ok
        }

        assertTrue(texto(conexao.getErrorStream()).contains("NAO_AUTORIZADO"));
        assertEquals(1, tratador.chamadas.get());
    }

    @Test
    public void sucesso_getErrorStreamEhNull() throws Exception {
        TratadorFalso tratador = new TratadorFalso();
        HttpURLConnection conexao = abrir(tratador, "https://ds-mobile-api.onrender.com/api/x");

        assertEquals(200, conexao.getResponseCode());
        assertNull(conexao.getErrorStream());
    }

    @Test
    public void corpoEnviado_chegaIgual() throws Exception {
        TratadorFalso tratador = new TratadorFalso();
        HttpURLConnection conexao = abrir(tratador, "https://ds-mobile-api.onrender.com/api/usuarios/me");
        byte[] dados = "{\"nome\":\"Ana\"}".getBytes(StandardCharsets.UTF_8);

        conexao.setRequestMethod("PATCH");
        conexao.setRequestProperty("Authorization", "Bearer abc");
        conexao.setDoOutput(true);
        conexao.setFixedLengthStreamingMode(dados.length);
        try (OutputStream saida = conexao.getOutputStream()) {
            saida.write(dados);
        }

        assertEquals(200, conexao.getResponseCode());
        assertArrayEquals(dados, tratador.corpo);
        assertEquals("Bearer abc", tratador.autorizacao);
        assertEquals("PATCH https://ds-mobile-api.onrender.com/api/usuarios/me", tratador.metodos.get(0));
        assertEquals("Bearer abc", conexao.getRequestProperty("authorization"));
    }

    @Test
    public void cabecalhosDaResposta() throws Exception {
        TratadorFalso tratador = new TratadorFalso();
        HttpURLConnection conexao = abrir(tratador, "https://ds-mobile-api.onrender.com/api/x");

        assertTrue(conexao.getHeaderField("Content-Type").startsWith("application/json"));
        assertEquals("11", conexao.getHeaderField("content-length"));
        assertEquals(11, conexao.getContentLength());
        assertNull(conexao.getHeaderField("X-Qualquer"));
    }

    @Test
    public void hostDesconhecido_lancaIOException() throws Exception {
        TratadorFalso tratador = new TratadorFalso();
        HttpURLConnection conexao = abrir(tratador, "https://identitytoolkit.googleapis.com/v1/accounts");

        try {
            conexao.getResponseCode();
            fail("host fora da lista deve lançar IOException");
        } catch (IOException esperado) {
            // ok
        }

        try {
            conexao.getInputStream();
            fail();
        } catch (IOException esperado) {
            // ok
        }

        assertEquals(0, tratador.chamadas.get());
    }

    @Test
    public void todosOsHostsDaApi_chegamAoTratador() throws Exception {
        String[] hosts = {
            "ds-mobile-api.onrender.com",
            "ds-pontuacao-api.onrender.com",
            "ia-eko.onrender.com",
            "api.52.71.117.143.sslip.io",
            "api.cloudinary.com",
            "viacep.com.br"
        };
        TratadorFalso tratador = new TratadorFalso();

        for (String host : hosts) {
            assertEquals(200, abrir(tratador, "https://" + host + "/").getResponseCode());
        }

        assertEquals(hosts.length, tratador.chamadas.get());
    }

    @Test
    public void protocoloQueNaoEhHttp_naoTemHandler() {
        FabricaConexoesDemo fabrica = new FabricaConexoesDemo(new TratadorFalso(), 0, endereco -> null);

        assertNull(fabrica.createURLStreamHandler("file"));
        assertNull(fabrica.createURLStreamHandler("jar"));
        assertNull(fabrica.createURLStreamHandler("ftp"));
        assertTrue(fabrica.createURLStreamHandler("http") != null);
        assertTrue(fabrica.createURLStreamHandler("https") != null);
    }

    @Test
    public void duasThreads_umaChamadaSo() throws Exception {
        TratadorFalso tratador = new TratadorFalso();
        HttpURLConnection conexao = abrir(tratador, "https://ds-mobile-api.onrender.com/api/x");
        CountDownLatch largada = new CountDownLatch(1);
        List<Integer> codigos = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> falhas = Collections.synchronizedList(new ArrayList<>());

        Runnable tarefa = () -> {
            try {
                largada.await();
                codigos.add(conexao.getResponseCode());
            } catch (Throwable erro) {
                falhas.add(erro);
            }
        };

        Thread a = new Thread(tarefa);
        Thread b = new Thread(tarefa);
        a.start();
        b.start();
        largada.countDown();
        a.join(5000);
        b.join(5000);

        assertTrue(falhas.toString(), falhas.isEmpty());
        assertEquals(2, codigos.size());
        assertEquals(1, tratador.chamadas.get());
    }

    // Conexão de imagem falsa: só prova que a função injetada foi usada.
    private static final class ImagemFalsa extends HttpURLConnection {
        ImagemFalsa(URL url) {
            super(url);
        }

        @Override public void disconnect() {}
        @Override public boolean usingProxy() { return false; }
        @Override public void connect() {}
        @Override public int getResponseCode() { return 200; }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(new byte[] {7}); }
    }

    @Test
    public void leituraCloudinary_naoPassaPelaApi() throws Exception {
        TratadorFalso tratador = new TratadorFalso();
        String endereco = "https://res.cloudinary.com/ncwfxyfi/image/upload/v1/guia/foto.jpg";
        List<URL> abertas = new ArrayList<>();
        ImagemFalsa[] criada = new ImagemFalsa[1];
        FabricaConexoesDemo fabrica =
                new FabricaConexoesDemo(
                        tratador,
                        0,
                        url -> {
                            abertas.add(url);
                            criada[0] = new ImagemFalsa(url);
                            return criada[0];
                        });
        URLStreamHandler handler = fabrica.createURLStreamHandler("https");

        HttpURLConnection conexao = (HttpURLConnection) new URL(null, endereco, handler).openConnection();

        assertSame(criada[0], conexao);
        assertEquals("GET", conexao.getRequestMethod());
        assertEquals(1, abertas.size());
        assertEquals(endereco, abertas.get(0).toString());
        assertEquals(200, conexao.getResponseCode());
        assertEquals(0, tratador.chamadas.get());
    }

    @Test
    public void uploadCloudinary_passaPelaApi() throws Exception {
        TratadorFalso tratador = new TratadorFalso();
        HttpURLConnection conexao = abrir(tratador, "https://api.cloudinary.com/v1_1/x/image/upload");

        conexao.setRequestMethod("POST");
        conexao.setDoOutput(true);
        try (OutputStream saida = conexao.getOutputStream()) {
            saida.write(new byte[] {1, 2, 3});
        }

        assertEquals(200, conexao.getResponseCode());
        assertEquals(1, tratador.chamadas.get());
        assertEquals("POST https://api.cloudinary.com/v1_1/x/image/upload", tratador.metodos.get(0));
    }
}
