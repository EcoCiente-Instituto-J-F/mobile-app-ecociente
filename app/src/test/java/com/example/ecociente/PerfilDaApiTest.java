package com.example.ecociente;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.PerfilDaApi;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.model.TipoPerfil;
import java.util.Map;
import org.junit.Test;

public class PerfilDaApiTest {

    private static PerfilDaApi api() {
        return new PerfilDaApi(
                "Ana Souza", "https://res.cloudinary.com/x/nova.jpg", "81988887777", "12345678901",
                "50000000", "PE", "Recife", "Rua Nova", "42", "Apto 3", "", "");
    }

    @Test
    public void aApiPassaPorCimaDoFirestoreEDevolveOQueMudou() {
        PerfilUsuario perfil = new PerfilUsuario();
        perfil.setNome("Ana");
        perfil.setFotoUrl("https://res.cloudinary.com/x/antiga.jpg");

        Map<String, String> mudancas = api().aplicarEm(perfil);

        assertEquals("Ana Souza", perfil.getNome());
        assertEquals("https://res.cloudinary.com/x/nova.jpg", perfil.getFotoUrl());
        assertEquals("(81) 98888-7777", perfil.getTelefone());
        assertEquals("123.456.789-01", perfil.getCpf());
        assertEquals("50000-000", perfil.getCep());
        assertEquals("Rua Nova", perfil.getEndereco());
        assertEquals("https://res.cloudinary.com/x/nova.jpg", mudancas.get("fotoUrl"));
        assertEquals("Ana Souza", mudancas.get("nome"));
    }

    @Test
    public void valorIgualNaoGeraMudanca() {
        PerfilUsuario perfil = new PerfilUsuario();
        perfil.setNome("Ana Souza");
        perfil.setFotoUrl("https://res.cloudinary.com/x/nova.jpg");
        perfil.setTelefone("(81) 98888-7777");
        perfil.setCpf("123.456.789-01");
        perfil.setCep("50000-000");
        perfil.setEndereco("Rua Nova");
        perfil.setNumero("42");
        perfil.setComplemento("Apto 3");
        perfil.setCidade("Recife");
        perfil.setEstado("PE");

        assertTrue(api().aplicarEm(perfil).isEmpty());
    }

    @Test
    public void campoVazioNaApiNaoApagaOQueTemNoFirestore() {
        PerfilUsuario perfil = new PerfilUsuario();
        perfil.setNome("Ana");
        perfil.setTelefone("(81) 91111-2222");
        perfil.setFotoUrl("https://res.cloudinary.com/x/antiga.jpg");

        Map<String, String> mudancas = PerfilDaApi.vazio().aplicarEm(perfil);

        assertTrue(mudancas.isEmpty());
        assertEquals("Ana", perfil.getNome());
        assertEquals("(81) 91111-2222", perfil.getTelefone());
        assertEquals("https://res.cloudinary.com/x/antiga.jpg", perfil.getFotoUrl());
    }

    @Test
    public void cooperativaRecebeNomeEEmailDaCooperativaENaoDoResponsavel() {
        PerfilUsuario perfil = new PerfilUsuario();
        perfil.setTipoPerfil(TipoPerfil.COOPERATIVA);
        perfil.setNome("Nome antigo");
        perfil.setNomeCooperativa("Coop Antiga");

        PerfilDaApi cooperativa =
                new PerfilDaApi("Responsavel", "", "", "", "", "", "", "", "", "", "Coop Verde", "c@verde.com");

        Map<String, String> mudancas = cooperativa.aplicarEm(perfil);

        assertEquals("Coop Verde", perfil.getNomeCooperativa());
        assertEquals("c@verde.com", perfil.getEmailCooperativa());
        assertEquals("Nome antigo", perfil.getNome());
        assertEquals("Coop Verde", mudancas.get("nomeCooperativa"));
    }

    @Test
    public void telefoneForaDoPadraoFicaComoVeio() {
        PerfilUsuario perfil = new PerfilUsuario();

        new PerfilDaApi("", "", "123", "", "", "", "", "", "", "", "", "").aplicarEm(perfil);

        assertEquals("123", perfil.getTelefone());
    }
}
