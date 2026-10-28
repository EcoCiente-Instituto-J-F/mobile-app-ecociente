package com.example.ecociente.notifications;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.model.PreferenciasNotificacao;
import com.example.ecociente.model.PreferenciasNotificacao.Grupo;
import org.junit.Test;

public class GrupoNotificacaoTest {

    @Test
    public void cadaTipoPertenceAoGrupoCombinado() {
        assertEquals(Grupo.LEMBRETES_AVISOS, TipoNotificacaoEcoCiente.COLETA_HOJE.getGrupo());
        assertEquals(Grupo.LEMBRETES_AVISOS, TipoNotificacaoEcoCiente.MENSAGEM_NOVA.getGrupo());
        assertEquals(Grupo.LEMBRETES_AVISOS, TipoNotificacaoEcoCiente.SOLICITACAO_RECUSADA.getGrupo());
        assertEquals(Grupo.NOVAS_ATIVIDADES, TipoNotificacaoEcoCiente.QUIZ.getGrupo());
        assertEquals(Grupo.NOVAS_ATIVIDADES, TipoNotificacaoEcoCiente.RANKING_SUBIU.getGrupo());
        assertEquals(Grupo.DICAS_SUSTENTAVEIS, TipoNotificacaoEcoCiente.DICA.getGrupo());
        assertEquals(Grupo.DICAS_SUSTENTAVEIS, TipoNotificacaoEcoCiente.RETORNO.getGrupo());
        assertEquals(Grupo.NENHUM, TipoNotificacaoEcoCiente.DESCONHECIDA.getGrupo());
    }

    @Test
    public void todoTipoConhecidoTemGrupo() {
        for (TipoNotificacaoEcoCiente tipo : TipoNotificacaoEcoCiente.values()) {
            if (tipo != TipoNotificacaoEcoCiente.DESCONHECIDA) {
                assertTrue(tipo.name(), tipo.getGrupo() != Grupo.NENHUM);
            }
        }
    }

    @Test
    public void geraisDesligadasSilenciamTudo() {
        PreferenciasNotificacao preferencias = new PreferenciasNotificacao();
        preferencias.setNotificacoesGerais(false);
        preferencias.setLembretesAvisos(true);

        for (Grupo grupo : Grupo.values()) {
            assertFalse(grupo.name(), preferencias.permite(grupo));
        }
    }

    @Test
    public void semPreferenciaSalvaTodosOsGruposVemLigados() {
        PreferenciasNotificacao preferencias = new PreferenciasNotificacao();

        for (Grupo grupo : Grupo.values()) {
            assertTrue(grupo.name(), preferencias.permite(grupo));
        }
    }

    @Test
    public void cadaGrupoSegueSeuInterruptor() {
        PreferenciasNotificacao preferencias = new PreferenciasNotificacao();
        preferencias.setLembretesAvisos(true);
        preferencias.setNovasAtividades(false);
        preferencias.setDicasSustentaveis(true);

        assertTrue(preferencias.permite(Grupo.LEMBRETES_AVISOS));
        assertFalse(preferencias.permite(Grupo.NOVAS_ATIVIDADES));
        assertTrue(preferencias.permite(Grupo.DICAS_SUSTENTAVEIS));
        assertTrue(preferencias.permite(Grupo.NENHUM));
    }
}
