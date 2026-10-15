package com.example.ecociente.notifications;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ecociente.R;

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

    @DrawableRes
    public int getImagemEko() {
        // Fallback temporário para todos os tipos até cada ilustração existir.
        return R.drawable.eko_notificacao;
    }
}
