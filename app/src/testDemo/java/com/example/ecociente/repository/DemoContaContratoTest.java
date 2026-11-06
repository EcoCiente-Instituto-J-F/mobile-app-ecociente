package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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

// Os parsers reais (package-private) lendo o que a API falsa devolve: se a forma do JSON divergir, o teste quebra.
public class DemoContaContratoTest {

    private static final String MOBILE = "https://ds-mobile-api.onrender.com";

    private ApiDemo api;

    @Before
    public void preparar() {
        api = new ApiDemo(new EstadoDemo(new ArmazemEmMemoria(), () -> 1_700_000_000_000L), () -> 1_700_000_000_000L);
    }

    private RespostaDemo entrar(String senha) throws Exception {
        String corpo =
                new JSONObject().put("email", SementeDemo.EMAIL).put("senha", senha).toString();

        return api.tratar("POST", MOBILE + "/auth/login", null, corpo.getBytes(StandardCharsets.UTF_8));
    }

    private String tokenValido() throws Exception {
        return new JSONObject(entrar(SementeDemo.SENHA).texto()).getString("token");
    }

    @Test
    public void loginCorreto_devolveTokenUsuarioEPerfil() throws Exception {
        RespostaDemo resposta = entrar(SementeDemo.SENHA);

        assertEquals(200, resposta.codigo);

        JSONObject json = new JSONObject(resposta.texto());

        assertFalse(json.getString("token").isEmpty());
        assertEquals(42, json.getInt("usuarioId"));
        assertEquals("MORADOR", json.getString("perfil"));
    }

    @Test
    public void loginComSenhaErrada_contaComoRecusado() throws Exception {
        RespostaDemo resposta = entrar("senha-errada");

        assertTrue(PoliticaErroLogin.credenciaisRecusadas(resposta.codigo, resposta.texto()));
    }

    @Test
    public void me_semToken_devolve401() {
        RespostaDemo resposta = api.tratar("GET", MOBILE + "/api/usuarios/me", null, new byte[0]);

        assertEquals(401, resposta.codigo);
    }

    @Test
    public void me_comTokenErrado_devolve401() {
        RespostaDemo resposta = api.tratar("GET", MOBILE + "/api/usuarios/me", "Bearer qualquer", new byte[0]);

        assertEquals(401, resposta.codigo);
    }

    @Test
    public void me_comToken_temCondominioTelefoneEEndereco() throws Exception {
        String token = tokenValido();

        RespostaDemo resposta = api.tratar("GET", MOBILE + "/api/usuarios/me", "Bearer " + token, new byte[0]);

        assertEquals(200, resposta.codigo);

        JSONObject me = new JSONObject(resposta.texto());
        PerfilApiJson.Condominio condominio = PerfilApiJson.condominio(me);

        assertNotNull("o morador precisa de um vínculo aprovado", condominio);
        assertEquals(7, condominio.id);
        assertEquals(SementeDemo.CONDOMINIO_NOME, condominio.nome);

        PerfilDaApi perfil = PerfilApiJson.perfilDaApi(me);

        assertEquals(SementeDemo.NOME, perfil.getNome());
        assertFalse(perfil.getTelefone().isEmpty());
        assertFalse(perfil.getLogradouro().isEmpty());
        assertFalse(perfil.getCep().isEmpty());
        assertFalse(perfil.getNumero().isEmpty());
    }

    @Test
    public void patchMe_mudaONome() throws Exception {
        String token = "Bearer " + tokenValido();

        RespostaDemo patch =
                api.tratar("PATCH", MOBILE + "/api/usuarios/me", token, "{\"nome\":\"Marina A. Souza\"}".getBytes(StandardCharsets.UTF_8));
        RespostaDemo me = api.tratar("GET", MOBILE + "/api/usuarios/me", token, new byte[0]);

        assertEquals(200, patch.codigo);
        assertEquals("Marina A. Souza", PerfilApiJson.perfilDaApi(new JSONObject(me.texto())).getNome());
    }

    @Test
    public void patchEndereco_mudaCepENumero() throws Exception {
        String token = "Bearer " + tokenValido();

        RespostaDemo patch =
                api.tratar(
                        "PATCH",
                        MOBILE + "/api/usuarios/me/endereco",
                        token,
                        "{\"cep\":\"01001000\",\"numero\":\"55\",\"complemento\":\"Bloco C\"}".getBytes(StandardCharsets.UTF_8));
        PerfilDaApi perfil =
                PerfilApiJson.perfilDaApi(
                        new JSONObject(api.tratar("GET", MOBILE + "/api/usuarios/me", token, new byte[0]).texto()));

        assertEquals(200, patch.codigo);
        assertEquals("01001000", perfil.getCep());
        assertEquals("55", perfil.getNumero());
        assertEquals("Bloco C", perfil.getComplemento());
    }

    @Test
    public void healthCheck_semToken_devolve200EmCadaHost() {
        assertEquals(200, api.tratar("GET", "https://ds-pontuacao-api.onrender.com/", null, new byte[0]).codigo);
        assertEquals(200, api.tratar("GET", MOBILE + "/", null, new byte[0]).codigo);
    }

    @Test
    public void rotaSemHandler_devolve404QueNaoContaComoRecusaDeLogin() {
        RespostaDemo resposta = api.tratar("GET", MOBILE + "/api/rota-que-nao-existe", "Bearer x", new byte[0]);

        assertEquals(404, resposta.codigo);
        assertFalse(PoliticaErroLogin.credenciaisRecusadas(resposta.codigo, resposta.texto()));
    }
}
