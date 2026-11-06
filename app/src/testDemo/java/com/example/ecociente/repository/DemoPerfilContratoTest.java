package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.demo.ApiDemo;
import com.example.ecociente.demo.ArmazemEmMemoria;
import com.example.ecociente.demo.EstadoDemo;
import com.example.ecociente.demo.RespostaDemo;
import com.example.ecociente.demo.SementeDemo;
import com.example.ecociente.model.PerfilDaApi;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

// Eko, CEP, perfil, senha, conta e dispositivo contra a API falsa.
public class DemoPerfilContratoTest {

    private static final String MOBILE = "https://ds-mobile-api.onrender.com";
    private static final String CHAT_RENDER = "https://ia-eko.onrender.com/api/v1/chat";
    private static final String CHAT_PRINCIPAL = "https://api.52.71.117.143.sslip.io/api/v1/chat";
    private static final long AGORA = 1_700_000_000_000L;

    private ApiDemo api;
    private String token;

    @Before
    public void preparar() throws Exception {
        api = new ApiDemo(new EstadoDemo(new ArmazemEmMemoria(), () -> AGORA), () -> AGORA);

        String corpo = new JSONObject().put("email", SementeDemo.EMAIL).put("senha", SementeDemo.SENHA).toString();
        RespostaDemo login = api.tratar("POST", MOBILE + "/auth/login", null, corpo.getBytes(StandardCharsets.UTF_8));

        token = "Bearer " + new JSONObject(login.texto()).getString("token");
    }

    private RespostaDemo chamar(String metodo, String url, String autorizacao, String json) {
        return api.tratar(metodo, url, autorizacao, json == null ? new byte[0] : json.getBytes(StandardCharsets.UTF_8));
    }

    private PerfilDaApi perfil() throws Exception {
        return PerfilApiJson.perfilDaApi(new JSONObject(chamar("GET", MOBILE + "/api/usuarios/me", token, null).texto()));
    }

    @Test
    public void chat_nosDoisHosts_devolveAnswerESessionId() throws Exception {
        for (String url : new String[] {CHAT_RENDER, CHAT_PRINCIPAL}) {
            RespostaDemo r = chamar("POST", url, token, "{\"mensagem\":\"onde jogo pilha?\"}");
            JSONObject json = new JSONObject(r.texto());

            assertEquals(200, r.codigo);
            assertFalse(json.getString("answer").trim().isEmpty());
            assertFalse(json.getString("session_id").trim().isEmpty());
        }
    }

    @Test
    public void chat_mantemOSessionIdRecebido() throws Exception {
        RespostaDemo r = chamar("POST", CHAT_RENDER, token, "{\"mensagem\":\"oi\",\"session_id\":\"abc-123\"}");

        assertEquals("abc-123", new JSONObject(r.texto()).getString("session_id"));
    }

    @Test
    public void chat_semMensagem_devolve400() {
        assertEquals(400, chamar("POST", CHAT_RENDER, token, "{}").codigo);
        assertEquals(400, chamar("POST", CHAT_RENDER, token, "{\"mensagem\":\"  \"}").codigo);
    }

    @Test
    public void chat_semToken_devolve401() {
        assertEquals(401, chamar("POST", CHAT_RENDER, null, "{\"mensagem\":\"oi\"}").codigo);
    }

    @Test
    public void patchPerfil_refleteNoParserReal() throws Exception {
        RespostaDemo r =
                chamar(
                        "PATCH",
                        MOBILE + "/api/usuarios/me",
                        token,
                        "{\"nome\":\"Marina Souza\",\"urlAvatar\":\"https://res.cloudinary.com/demo/a.jpg\",\"telefone\":\"11911112222\"}");
        PerfilDaApi perfil = perfil();

        assertEquals(200, r.codigo);
        assertEquals("Marina Souza", perfil.getNome());
        assertEquals("https://res.cloudinary.com/demo/a.jpg", perfil.getUrlAvatar());
        assertEquals("11911112222", perfil.getTelefone());
    }

    @Test
    public void patchEndereco_refleteNoParserReal() throws Exception {
        chamar("PATCH", MOBILE + "/api/usuarios/me/endereco", token, "{\"cep\":\"01001000\",\"numero\":\"9\"}");

        assertEquals("01001000", perfil().getCep());
        assertEquals("9", perfil().getNumero());
    }

    @Test
    public void deleteConta_devolve2xx() {
        int codigo = chamar("DELETE", MOBILE + "/api/usuarios/me", token, null).codigo;

        assertTrue(codigo >= 200 && codigo < 300);
    }

    @Test
    public void dispositivo_devolve2xxSoComToken() {
        String corpo = "{\"instalacaoId\":\"i1\",\"token\":\"fcm\",\"preferencias\":{}}";
        int codigo = chamar("POST", MOBILE + "/api/notificacoes/dispositivos", token, corpo).codigo;

        assertTrue(codigo >= 200 && codigo < 300);
        assertEquals(401, chamar("POST", MOBILE + "/api/notificacoes/dispositivos", null, corpo).codigo);

        int remocao = chamar("DELETE", MOBILE + "/api/notificacoes/dispositivos/i1", token, null).codigo;

        assertTrue(remocao >= 200 && remocao < 300);
    }

    @Test
    public void viacep_cepConhecido_temOFormatoDoRepositorio() throws Exception {
        RespostaDemo r = chamar("GET", "https://viacep.com.br/ws/01001000/json/", null, null);
        JSONObject json = new JSONObject(r.texto());

        assertEquals(200, r.codigo);
        assertEquals("São Paulo", json.getString("localidade"));
        assertEquals("SP", json.getString("uf"));
        assertFalse(json.optBoolean("erro", false));
    }

    @Test
    public void viacep_cepInvalido_devolveErroTrue() throws Exception {
        for (String cep : new String[] {"00000000", "00000123", "9999999", "abcdefgh"}) {
            RespostaDemo r = chamar("GET", "https://viacep.com.br/ws/" + cep + "/json/", null, null);

            assertEquals(200, r.codigo);
            assertTrue(cep, new JSONObject(r.texto()).getBoolean("erro"));
        }
    }

    @Test
    public void senhas_alterar_validaASenhaAtualEAtualizaOLogin() throws Exception {
        String url = ConfiguracaoServidores.URL_SENHAS + "alterar";
        String errada = "{\"senhaAtual\":\"x\",\"novaSenha\":\"nova1234\",\"confirmacaoSenha\":\"nova1234\"}";
        String certa =
                "{\"senhaAtual\":\"" + SementeDemo.SENHA + "\",\"novaSenha\":\"nova1234\",\"confirmacaoSenha\":\"nova1234\"}";

        assertEquals(400, chamar("POST", url, token, errada).codigo);
        assertEquals(401, chamar("POST", url, null, certa).codigo);

        int codigo = chamar("POST", url, token, certa).codigo;

        assertTrue(codigo >= 200 && codigo < 300);

        String login = new JSONObject().put("email", SementeDemo.EMAIL).put("senha", "nova1234").toString();

        assertEquals(200, chamar("POST", MOBILE + "/auth/login", null, login).codigo);
    }
}
