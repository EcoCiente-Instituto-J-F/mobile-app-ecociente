package com.example.ecociente.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.PerfilAcesso;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.model.PreferenciasNotificacao;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.model.ResultadoLogin;
import com.example.ecociente.model.UsuarioLogado;
import com.example.ecociente.repository.PerfilRepository;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class ProvedorContaDemoTest {

    private static final String MOBILE = "https://ds-mobile-api.onrender.com";
    private static final long AGORA = 1_700_000_000_000L;

    private ArmazemEmMemoria armazem;
    private EstadoDemo estado;
    private ApiDemo api;
    private AtomicInteger resets;
    private ProvedorContaDemo provedor;

    @Before
    public void preparar() {
        armazem = new ArmazemEmMemoria();
        estado = new EstadoDemo(armazem, () -> AGORA);
        api = new ApiDemo(estado, () -> AGORA);
        resets = new AtomicInteger();
        provedor = new ProvedorContaDemo(estado, resets::incrementAndGet);
    }

    private ResultadoLogin entrarCerto() {
        return provedor.entrar(SementeDemo.EMAIL, SementeDemo.SENHA);
    }

    private int codigoDoLoginNaApi(String senha) throws Exception {
        String corpo = new JSONObject().put("email", SementeDemo.EMAIL).put("senha", senha).toString();

        return api.tratar("POST", MOBILE + "/auth/login", null, corpo.getBytes(StandardCharsets.UTF_8)).codigo;
    }

    @Test
    public void entrarErrado_devolveErro_eNaoCriaSessao() {
        ResultadoLogin resultado = provedor.entrar(SementeDemo.EMAIL, "errada");

        assertFalse(resultado.isSucesso());
        assertFalse(resultado.getMensagemErro().isEmpty());
        assertNull(provedor.usuario());
    }

    @Test
    public void entrarComEmailDesconhecido_devolveErro() {
        assertFalse(provedor.entrar("outro@ecociente.demo", SementeDemo.SENHA).isSucesso());
        assertNull(provedor.usuario());
    }

    @Test
    public void entrarCerto_criaSessaoComUid() {
        assertTrue(entrarCerto().isSucesso());

        UsuarioLogado usuario = provedor.usuario();

        assertNotNull(usuario);
        assertEquals(SementeDemo.UID, usuario.getUid());
        assertEquals(SementeDemo.EMAIL, usuario.getEmail());
        assertEquals(SementeDemo.NOME, usuario.getDisplayName());
    }

    @Test
    public void entrar_aceitaEmailComMaiusculasEEspacos() {
        assertTrue(provedor.entrar("  " + SementeDemo.EMAIL.toUpperCase() + " ", SementeDemo.SENHA).isSucesso());
    }

    @Test
    public void perfil_levaOMoradorParaAHomeDoCondominio() {
        entrarCerto();

        PerfilUsuario perfil = provedor.perfil(SementeDemo.UID);

        assertNotNull(perfil);
        assertEquals("usuario", perfil.getTipoPerfil());
        assertTrue(perfil.isPossuiCodigoCondominio());
        assertEquals(PerfilAcesso.DestinoHome.CONDOMINIO, PerfilAcesso.destinoDaHome(perfil));
        assertEquals(SementeDemo.NOME, perfil.getNome());
        assertEquals(SementeDemo.EMAIL, perfil.getEmail());
        assertFalse(perfil.getTelefone().isEmpty());
        assertFalse(perfil.getCep().isEmpty());
    }

    @Test
    public void perfil_deOutroUid_ehNull() {
        entrarCerto();

        assertNull(provedor.perfil("outro-uid"));
    }

    @Test
    public void atualizarCampoNome_refleteNoPerfilENoMe() throws Exception {
        entrarCerto();

        ResultadoApi resultado = provedor.atualizarCampo(SementeDemo.UID, PerfilRepository.CAMPO_NOME, "Marina Souza");

        assertTrue(resultado.isSucesso());
        assertEquals("Marina Souza", provedor.perfil(SementeDemo.UID).getNome());
        assertEquals("Marina Souza", provedor.usuario().getDisplayName());

        String corpo = new JSONObject().put("email", SementeDemo.EMAIL).put("senha", SementeDemo.SENHA).toString();
        String token =
                new JSONObject(api.tratar("POST", MOBILE + "/auth/login", null, corpo.getBytes(StandardCharsets.UTF_8)).texto())
                        .getString("token");
        RespostaDemo me = api.tratar("GET", MOBILE + "/api/usuarios/me", "Bearer " + token, new byte[0]);

        assertEquals("Marina Souza", new JSONObject(me.texto()).getString("nome"));
    }

    @Test
    public void atualizarCampos_deContatoEEndereco() {
        entrarCerto();

        provedor.atualizarCampo(SementeDemo.UID, PerfilRepository.CAMPO_TELEFONE, "11999998888");
        provedor.atualizarCampo(SementeDemo.UID, PerfilRepository.CAMPO_CEP, "01001000");
        provedor.atualizarCampo(SementeDemo.UID, PerfilRepository.CAMPO_NUMERO, "99");
        provedor.atualizarCampo(SementeDemo.UID, PerfilRepository.CAMPO_FOTO_URL, "file:///foto.jpg");

        PerfilUsuario perfil = provedor.perfil(SementeDemo.UID);

        assertEquals("(11) 99999-8888", perfil.getTelefone());
        assertEquals("01001000", perfil.getCep());
        assertEquals("99", perfil.getNumero());
        assertEquals("file:///foto.jpg", perfil.getFotoUrl());
    }

    @Test
    public void atualizarCampo_queOAppSincronizaDaHome_eNaoMudaOPerfil() {
        entrarCerto();

        assertTrue(provedor.atualizarCampo(SementeDemo.UID, "tipoPerfil", "usuario").isSucesso());
        assertTrue(provedor.atualizarCampo(SementeDemo.UID, "tipoUsuario", "morador").isSucesso());
        assertTrue(provedor.atualizarCampo(SementeDemo.UID, "possuiCodigoCondominio", Boolean.TRUE).isSucesso());
        assertEquals(PerfilAcesso.DestinoHome.CONDOMINIO, PerfilAcesso.destinoDaHome(provedor.perfil(SementeDemo.UID)));
    }

    @Test
    public void atualizarCampo_semSessao_devolveErro() {
        assertFalse(provedor.atualizarCampo(SementeDemo.UID, PerfilRepository.CAMPO_NOME, "X").isSucesso());
    }

    @Test
    public void preferencias_comecamLigadas_eGuardamAMudanca() {
        entrarCerto();

        assertTrue(provedor.preferencias(SementeDemo.UID).isDicasSustentaveis());

        assertTrue(
                provedor
                        .atualizarPreferencia(
                                SementeDemo.UID, PreferenciasNotificacao.CHAVE_DICAS_SUSTENTAVEIS, false)
                        .isSucesso());

        PreferenciasNotificacao preferencias = provedor.preferencias(SementeDemo.UID);

        assertFalse(preferencias.isDicasSustentaveis());
        assertTrue(preferencias.isLembretesAvisos());
    }

    @Test
    public void alterarSenha_comAtualErrada_devolveErro() throws Exception {
        entrarCerto();

        assertFalse(provedor.alterarSenha("errada", "NovaSenha@1").isSucesso());
        assertEquals(200, codigoDoLoginNaApi(SementeDemo.SENHA));
    }

    @Test
    public void alterarSenha_comAtualCerta_novaSenhaPassaAValer() throws Exception {
        entrarCerto();

        assertTrue(provedor.alterarSenha(SementeDemo.SENHA, "NovaSenha@1").isSucesso());

        assertEquals(200, codigoDoLoginNaApi("NovaSenha@1"));
        assertEquals(401, codigoDoLoginNaApi(SementeDemo.SENHA));
        assertTrue(provedor.entrar(SementeDemo.EMAIL, "NovaSenha@1").isSucesso());
    }

    @Test
    public void sair_limpaASessao_eReseta() {
        entrarCerto();
        provedor.atualizarCampo(SementeDemo.UID, PerfilRepository.CAMPO_NOME, "Mudou");

        provedor.sair();

        assertNull(provedor.usuario());
        assertEquals(1, resets.get());
        assertEquals(SementeDemo.NOME, new EstadoDemo(armazem, () -> AGORA).raiz().optJSONObject("me").optString("nome"));
    }

    @Test
    public void excluirConta_comSenhaCerta_reseta() {
        entrarCerto();

        assertTrue(provedor.excluirConta(SementeDemo.SENHA).isSucesso());

        assertNull(provedor.usuario());
        assertEquals(1, resets.get());
    }

    @Test
    public void excluirConta_comSenhaErrada_naoFazNada() {
        entrarCerto();

        assertFalse(provedor.excluirConta("errada").isSucesso());

        assertNotNull(provedor.usuario());
        assertEquals(0, resets.get());
    }

    @Test
    public void sessao_sobreviveARecriarOEstadoNoMesmoArmazem() {
        entrarCerto();

        EstadoDemo recarregado = new EstadoDemo(armazem, () -> AGORA);
        ProvedorContaDemo outro = new ProvedorContaDemo(recarregado, () -> {});

        assertNotNull(outro.usuario());
        assertEquals(SementeDemo.UID, outro.usuario().getUid());
    }
}
