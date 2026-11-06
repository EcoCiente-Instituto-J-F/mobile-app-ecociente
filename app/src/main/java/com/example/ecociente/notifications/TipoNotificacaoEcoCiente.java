package com.example.ecociente.notifications;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ecociente.R;
import com.example.ecociente.model.PreferenciasNotificacao;

import java.util.Locale;

/**
 * Catálogo local da apresentação das notificações do EcoCiente.
 *
 * As imagens especializadas podem substituir eko_notificacao quando os novos
 * arquivos forem adicionados a drawable-nodpi, sem mudar o fluxo de FCM.
 */
public enum TipoNotificacaoEcoCiente {

    DICA("dica", "Você sabia? 🌱", "Pilhas e baterias precisam de descarte específico. Não coloque no lixo comum.", null, "home"),
    RETORNO("retorno", "Eko sentiu sua falta 👀", "Faz um tempinho que você não aparece por aqui... que tal dar uma passadinha?", null, "home"),
    QUIZ("quiz", "Bora de desafio? 👀", "Tem um quiz rapidinho esperando por você!", "Fazer quiz", "quiz"),
    REVISAO("revisao", "Que tal uma revanche? 🌱", "Você pode melhorar sua pontuação no quiz de Plástico. Bora tentar de novo?", "Tentar novamente", "quiz"),
    MOTIVACIONAL("motivacional", "Pequenas atitudes importam 💚", "Uma escolha sustentável hoje já ajuda a transformar o amanhã.", null, "home"),
    CONTEUDOS("conteudos", "Tem conteúdo fresquinho te esperando 📚", "Separei algumas coisas para você aprender de um jeito rapidinho.", "Explorar", "home"),
    AULAS("aulas", "Bora aprender algo novo hoje? 📖", "Uns minutinhos agora já podem mudar a forma como você enxerga o meio ambiente.", "Ver aulas", "home"),
    COLETA_AMANHA("coleta_amanha", "A coleta é amanhã ♻️", "Já deixa os recicláveis separados para não esquecer.", "Ver coleta", "calendario"),
    COLETA_HOJE("coleta_hoje", "Hoje é dia de coleta! 🚛", "Seus recicláveis já estão separados?", null, "calendario"),
    COLETA_ALTERADA("coleta_alterada", "Atenção à sua coleta ⚠️", "O horário da próxima coleta foi atualizado.", "Ver detalhes", "calendario"),
    ECOREGISTRO_APROVADO("ecoregistro_aprovado", "Seu registro foi validado! 🌿", "Boa! Seu EcoRegistro acabou de ser aprovado.", "Ver registro", "home"),
    RANKING_SUBIU("ranking_subiu", "Você subiu no ranking! 🏆", "Suas atitudes sustentáveis estão fazendo diferença. Continue assim!", "Ver ranking", "home"),
    AVISO_CONDOMINIO("aviso_condominio", "Tem recado novo por aqui 📢", "O condomínio publicou um novo aviso para os moradores.", "Ver aviso", "home"),
    MENSAGEM_NOVA("mensagem_nova", "Chegou mensagem nova 💬", "Tem uma nova mensagem esperando por você.", "Abrir conversa", "mensagens"),
    SOLICITACAO_NOVA("solicitacao_nova", "Nova solicitação de coleta 🚛", "Um condomínio acabou de solicitar uma coleta.", "Ver solicitação", "solicitacoes"),
    SOLICITACAO_ACEITA("solicitacao_aceita", "Coleta confirmada ✅", "A cooperativa confirmou sua solicitação de coleta.", "Ver detalhes", "home"),
    SOLICITACAO_RECUSADA("solicitacao_recusada", "Atualização na sua coleta", "A cooperativa não conseguiu atender essa solicitação. Confira os detalhes.", "Ver detalhes", "home"),
    CALENDARIO("calendario", "Olha a semana chegando 👀", "Você tem coletas programadas nos próximos dias.", "Ver calendário", "calendario"),
    SAZONAL("sazonal", "Hoje é dia de cuidar ainda mais do planeta 🌎", "Que tal descobrir uma atitude simples para colocar em prática hoje?", "Descobrir", "home"),
    DESCONHECIDA("desconhecida", "EcoCiente", "O Eko trouxe uma mensagem para você!", null, "home");

    private final String id;
    private final String tituloPadrao;
    private final String mensagemPadrao;
    @Nullable
    private final String acaoPadrao;
    private final String destinoPadrao;

    TipoNotificacaoEcoCiente(
            @NonNull String id,
            @NonNull String tituloPadrao,
            @NonNull String mensagemPadrao,
            @Nullable String acaoPadrao,
            @NonNull String destinoPadrao
    ) {
        this.id = id;
        this.tituloPadrao = tituloPadrao;
        this.mensagemPadrao = mensagemPadrao;
        this.acaoPadrao = acaoPadrao;
        this.destinoPadrao = destinoPadrao;
    }

    @NonNull
    public static TipoNotificacaoEcoCiente porId(@Nullable String id) {
        if (id == null) {
            return DESCONHECIDA;
        }

        String idNormalizado = id.trim().toLowerCase(Locale.ROOT);

        for (TipoNotificacaoEcoCiente tipo : values()) {
            if (tipo.id.equals(idNormalizado)) {
                return tipo;
            }
        }

        return DESCONHECIDA;
    }

    @NonNull
    public String getId() { return id; }

    @NonNull
    public String getTituloPadrao() { return tituloPadrao; }

    @NonNull
    public String getMensagemPadrao() { return mensagemPadrao; }

    @Nullable
    public String getAcaoPadrao() { return acaoPadrao; }

    @NonNull
    public String getDestinoPadrao() { return destinoPadrao; }

    public boolean possuiCta() { return acaoPadrao != null; }

    @NonNull
    public PreferenciasNotificacao.Grupo getGrupo() {
        switch (this) {
            case COLETA_AMANHA:
            case COLETA_HOJE:
            case COLETA_ALTERADA:
            case CALENDARIO:
            case AVISO_CONDOMINIO:
            case MENSAGEM_NOVA:
            case SOLICITACAO_NOVA:
            case SOLICITACAO_ACEITA:
            case SOLICITACAO_RECUSADA:
                return PreferenciasNotificacao.Grupo.LEMBRETES_AVISOS;
            case QUIZ:
            case REVISAO:
            case CONTEUDOS:
            case AULAS:
            case ECOREGISTRO_APROVADO:
            case RANKING_SUBIU:
                return PreferenciasNotificacao.Grupo.NOVAS_ATIVIDADES;
            case DICA:
            case MOTIVACIONAL:
            case SAZONAL:
            case RETORNO:
                return PreferenciasNotificacao.Grupo.DICAS_SUSTENTAVEIS;
            default:
                return PreferenciasNotificacao.Grupo.NENHUM;
        }
    }

    @DrawableRes
    public int getImagemEko() {
        switch (this) {
            case DICA: return R.drawable.eko_notificacao_dica;
            case RETORNO: return R.drawable.eko_notificacao_retorno;
            case QUIZ: return R.drawable.eko_notificacao_quiz;
            case REVISAO: return R.drawable.eko_notificacao_revisao;
            case MOTIVACIONAL: return R.drawable.eko_notificacao_motivacional;
            case CONTEUDOS: return R.drawable.eko_notificacao_conteudos;
            case AULAS: return R.drawable.eko_notificacao_aulas;
            case COLETA_AMANHA: return R.drawable.eko_notificacao_coleta_amanha;
            case COLETA_HOJE: return R.drawable.eko_notificacao_coleta_hoje;
            case COLETA_ALTERADA: return R.drawable.eko_notificacao_coleta_alterada;
            case ECOREGISTRO_APROVADO: return R.drawable.eko_notificacao_ecoregistro_aprovado;
            case RANKING_SUBIU: return R.drawable.eko_notificacao_ranking;
            case AVISO_CONDOMINIO: return R.drawable.eko_notificacao_aviso;
            case MENSAGEM_NOVA: return R.drawable.eko_notificacao_mensagem;
            case SOLICITACAO_NOVA: return R.drawable.eko_notificacao_solicitacao_nova;
            case SOLICITACAO_ACEITA: return R.drawable.eko_notificacao_solicitacao_aceita;
            case SOLICITACAO_RECUSADA: return R.drawable.eko_notificacao_solicitacao_recusada;
            case CALENDARIO: return R.drawable.eko_notificacao_calendario;
            case SAZONAL: return R.drawable.eko_notificacao_sazonal;
            case DESCONHECIDA:
            default: return R.drawable.eko_notificacao;
        }
    }
}
