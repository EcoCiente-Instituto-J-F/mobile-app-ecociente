package com.example.ecociente.midias;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Identidade de uma arte, nao de uma View ou de um ID numerico de R.drawable. */
public final class Midia {
    public static final String ICONE_CONTEUDO = "icones_conteudo";
    public static final String FOTO_CONTEUDO = "fotos_conteudo";
    public static final String DECORACAO_CONTEUDO = "decoracoes_conteudo";
    public static final String EKO_LEITURA = "eko_leitura";
    public static final String ICONE_QUIZ = "icones_quiz";
    public static final String CAPA_QUIZ = "capa_quiz";
    public static final String DECORACAO_QUIZ = "decoracoes_quiz";

    public final String id, tipo, tema, origem, caminho, formato, grupoImagem, descricao, cloudName;
    public final boolean animada, eko, neutra;
    public final long versao;
    public final List<String> tags;

    public Midia(String id, String tipo, String tema, String origem, String caminho,
                 String formato, String grupoImagem, String descricao, String cloudName,
                 long versao, boolean animada, boolean eko, boolean neutra, List<String> tags) {
        this.id = id;
        this.tipo = tipo;
        this.tema = tema;
        this.origem = origem;
        this.caminho = caminho;
        this.formato = formato;
        this.grupoImagem = grupoImagem == null || grupoImagem.isEmpty() ? origem + ":" + caminho : grupoImagem;
        this.descricao = descricao == null ? "" : descricao;
        this.cloudName = cloudName == null ? "" : cloudName;
        this.versao = versao;
        this.animada = animada;
        this.eko = eko;
        this.neutra = neutra;
        this.tags = Collections.unmodifiableList(new ArrayList<>(tags));
    }

    public boolean ehGifEko() { return eko && animada && "gif".equalsIgnoreCase(formato); }
}
