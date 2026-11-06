package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class RotasSociaisDemo implements RotasDemo {
    private static final String MOBILE = "ds-mobile-api.onrender.com";
    private static final String PONTUACAO = "ds-pontuacao-api.onrender.com";
    private static final Pattern CONDOMINIO_DASH = Pattern.compile("^/api/condominios/(\\d+)/dashboard$");
    private static final Pattern CONDOMINIO_FEED = Pattern.compile("^/api/condominios/(\\d+)/postagens/em-analise$");
    private static final Pattern COOP_CONDOMINIOS = Pattern.compile("^/api/cooperativas/(\\d+)/condominios$");
    private static final Pattern COOP_AVALIACOES = Pattern.compile("^/api/cooperativas/(\\d+)/avaliacoes$");
    private static final Pattern AVALIAR = Pattern.compile("^/api/agendamentos/(\\d+)/avaliacao$");
    private static final Pattern CONVERSA_MENSAGENS = Pattern.compile("^/api/conversas/(\\d+)/mensagens$");
    private static final Pattern CONVERSA_LEITURA = Pattern.compile("^/api/conversas/(\\d+)/leitura$");
    private static final Pattern DECIDIR = Pattern.compile("^/api/v1/postagens/(\\d+)/decisao$");
    private static final Pattern TORRE_POSICAO = Pattern.compile("^/api/v1/rankings/torres/(\\d+)/posicao$");
    private final EstadoDemo estado;

    RotasSociaisDemo(@NonNull EstadoDemo estado) { this.estado = estado; }

    @Nullable
    @Override
    public RespostaDemo tratar(@NonNull RequisicaoDemo r) {
        if (PONTUACAO.equals(r.host)) {
            if (!ContaDemo.tokenConfere(estado.raiz(), r.autorizacao))
                return RespostaDemo.erro(401, "NAO_AUTORIZADO");
            try {
                Matcher decisao = DECIDIR.matcher(r.caminho);
                Matcher torre = TORRE_POSICAO.matcher(r.caminho);
                if (decisao.matches() && "PATCH".equals(r.metodo))
                    return decidir(Integer.parseInt(decisao.group(1)), r);
                if (r.e("GET", "/api/v1/rankings/torres"))
                    return rankingTorres(r);
                if (torre.matches() && "GET".equals(r.metodo))
                    return posicaoTorre(Integer.parseInt(torre.group(1)));
            } catch (JSONException e) { throw new IllegalStateException(e); }
            return null;
        }
        if (!MOBILE.equals(r.host)) return null;
        Matcher dash = CONDOMINIO_DASH.matcher(r.caminho);
        Matcher feed = CONDOMINIO_FEED.matcher(r.caminho);
        Matcher condominios = COOP_CONDOMINIOS.matcher(r.caminho);
        Matcher avaliacoes = COOP_AVALIACOES.matcher(r.caminho);
        Matcher avaliar = AVALIAR.matcher(r.caminho);
        Matcher mensagens = CONVERSA_MENSAGENS.matcher(r.caminho);
        Matcher leitura = CONVERSA_LEITURA.matcher(r.caminho);
        boolean identificado = r.caminho.equals("/api/cooperativas") || r.caminho.equals("/api/conversas")
                || dash.matches() || feed.matches() || condominios.matches() || avaliacoes.matches()
                || avaliar.matches() || mensagens.matches() || leitura.matches();
        if (!identificado) return null;
        if (!ContaDemo.tokenConfere(estado.raiz(), r.autorizacao))
            return RespostaDemo.erro(401, "NAO_AUTORIZADO");
        try {
            if (r.e("GET", "/api/cooperativas")) return cooperativas();
            if (dash.matches() && "GET".equals(r.metodo)) return dashboard(Integer.parseInt(dash.group(1)));
            if (feed.matches() && "GET".equals(r.metodo)) return feedEmAnalise(Integer.parseInt(feed.group(1)));
            if (condominios.matches() && "GET".equals(r.metodo)) return condominiosAtendidos();
            if (avaliacoes.matches() && "GET".equals(r.metodo)) return avaliacoes();
            if (avaliar.matches() && "POST".equals(r.metodo)) return avaliar(Integer.parseInt(avaliar.group(1)), r);
            if (r.e("GET", "/api/conversas")) return conversas(r);
            if (r.e("POST", "/api/conversas")) return abrirConversa(r);
            if (mensagens.matches() && "GET".equals(r.metodo)) return mensagens(Integer.parseInt(mensagens.group(1)));
            if (mensagens.matches() && "POST".equals(r.metodo)) return enviar(Integer.parseInt(mensagens.group(1)), r);
            if (leitura.matches() && "PATCH".equals(r.metodo)) return new RespostaDemo(204, "");
            return RespostaDemo.erro(405, "METODO_NAO_PERMITIDO");
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    private RespostaDemo cooperativas() throws JSONException {
        JSONArray dados = new JSONArray();
        dados.put(cooperativa(3, "Cooperativa EcoCiente", 4.8, 24));
        dados.put(cooperativa(4, "Cooperativa Verde Futuro", 4.7, 18));
        dados.put(cooperativa(5, "Recicla Juntos", 4.9, 12));
        return json(200, new JSONObject().put("total", dados.length()).put("pagina", 0)
                .put("tamanho", 100).put("totalPaginas", 1).put("cooperativas", dados));
    }

    private JSONObject cooperativa(int id, String nome, double media, int avaliacoes) throws JSONException {
        return new JSONObject().put("id", id).put("nome", nome).put("mediaAvaliacoes", media)
                .put("totalAvaliacoes", avaliacoes).put("logradouro", "Rua das Flores")
                .put("numero", String.valueOf(id * 100)).put("cidade", "Barueri")
                .put("estado", "SP").put("cep", "06454000")
                .put("enderecoCompleto", "Rua das Flores, " + (id * 100) + ", Barueri - SP");
    }

    private RespostaDemo condominiosAtendidos() throws JSONException {
        JSONObject endereco = new JSONObject().put("logradouro", "Rua das Acacias").put("numero", "200")
                .put("cidade", "Barueri").put("estado", "SP").put("cep", "06454000");
        JSONArray dias = new JSONArray().put("SEGUNDA").put("QUARTA").put("SEXTA");
        JSONObject item = new JSONObject().put("id", 7).put("nome", "Condominio Jardim das Palmeiras")
                .put("endereco", endereco).put("diasColeta", dias).put("totalTorres", 3);
        return json(200, new JSONObject().put("condominios", new JSONArray().put(item))
                .put("totalPaginas", 1).put("total", 1).put("pagina", 0));
    }

    private RespostaDemo dashboard(int condominioId) throws JSONException {
        if (condominioId != 7) return RespostaDemo.erro(404, "CONDOMINIO_NAO_ENCONTRADO");
        JSONArray categorias = new JSONArray()
                .put(new JSONObject().put("categoria", "Plastico").put("quantidade", 58))
                .put(new JSONObject().put("categoria", "Papel").put("quantidade", 44))
                .put(new JSONObject().put("categoria", "Metal").put("quantidade", 29))
                .put(new JSONObject().put("categoria", "Vidro").put("quantidade", 19));
        JSONArray quizzes = new JSONArray()
                .put(new JSONObject().put("quiz", "Sustentabilidade").put("notaMedia", 86))
                .put(new JSONObject().put("quiz", "Reciclagem").put("notaMedia", 92))
                .put(new JSONObject().put("quiz", "Consumo consciente").put("notaMedia", 78));
        long pontos = 1720;
        return json(200, new JSONObject().put("totalMoradores", 128)
                .put("materiais", new JSONObject().put("reciclados", 150).put("porCategoria", categorias))
                .put("pontos", new JSONObject().put("mesAtual", pontos))
                .put("quizzes", new JSONObject().put("porQuiz", quizzes)));
    }

    private RespostaDemo feedEmAnalise(int condominioId) throws JSONException {
        if (condominioId != 7) return RespostaDemo.erro(403, "SEM_ACESSO");
        JSONArray lista = new JSONArray();
        for (JSONObject p : FeedDemo.postagens(estado.raiz())) {
            if (!FeedDemo.EM_ANALISE.equals(p.optString("statusValidacao"))) continue;
            lista.put(new JSONObject().put("id", p.optInt("id")).put("autor", p.optString("autor"))
                    .put("categoria", p.optString("categoria")).put("urlFoto", p.optString("urlFoto"))
                    .put("statusValidacao", p.optString("statusValidacao")).put("dataPostagem", p.optString("dataPostagem")));
        }
        return json(200, new JSONObject().put("postagens", lista).put("total", lista.length())
                .put("pagina", 0).put("tamanho", 100).put("totalPaginas", 1));
    }

    private JSONObject blocoAvaliacoes() throws JSONException {
        JSONObject raiz = estado.raiz();
        JSONObject bloco = raiz.optJSONObject("avaliacoesMock");
        if (bloco == null) {
            bloco = new JSONObject().put("proximoId", 3);
            JSONArray lista = new JSONArray().put(avaliacao(1, "Condominio Jardim das Palmeiras", 5,
                    "Coleta organizada e pontual.", "2026-11-02T10:00:00"))
                    .put(avaliacao(2, "Residencial Verde Vida", 4, "Atendimento muito bom.", "2026-11-01T09:20:00"));
            bloco.put("itens", lista);
            raiz.put("avaliacoesMock", bloco);
            estado.salvar();
        }
        return bloco;
    }

    private JSONObject avaliacao(int id, String nome, int nota, String comentario, String data) throws JSONException {
        return new JSONObject().put("id", id).put("nomeCondominio", nome).put("nota", nota)
                .put("comentario", comentario).put("avaliadoEm", data);
    }

    private RespostaDemo avaliacoes() throws JSONException {
        JSONArray lista = blocoAvaliacoes().getJSONArray("itens");
        double soma = 0;
        for (int i = 0; i < lista.length(); i++) soma += lista.getJSONObject(i).optInt("nota", 0);
        return json(200, new JSONObject().put("total", lista.length())
                .put("media", lista.length() == 0 ? 0 : soma / lista.length()).put("avaliacoes", lista));
    }

    private RespostaDemo avaliar(int agendamentoId, RequisicaoDemo r) throws JSONException {
        JSONObject request = r.corpoJson();
        if (request == null || request.optInt("nota", 0) < 1 || request.optInt("nota", 0) > 5)
            return RespostaDemo.erro(400, "NOTA_INVALIDA");
        JSONArray agenda = estado.raiz().optJSONArray("agendamentos");
        boolean encontrado = false;
        for (int i = 0; agenda != null && i < agenda.length(); i++) {
            JSONObject ag = agenda.optJSONObject(i);
            if (ag != null && ag.optInt("id") == agendamentoId) {
                if (ag.optBoolean("avaliado", false)) return RespostaDemo.erro(409, "AVALIACAO_JA_REGISTRADA");
                ag.put("avaliado", true); encontrado = true; break;
            }
        }
        if (!encontrado) return RespostaDemo.erro(404, "AGENDAMENTO_NAO_ENCONTRADO");
        JSONObject bloco = blocoAvaliacoes();
        int id = bloco.optInt("proximoId", 3);
        bloco.put("proximoId", id + 1);
        bloco.getJSONArray("itens").put(avaliacao(id, "Condominio Jardim das Palmeiras",
                request.optInt("nota", 5), request.optString("comentario", ""),
                SementeDemo.formatarData(estado.agora())));
        estado.salvar();
        return json(201, new JSONObject().put("id", id));
    }

    private JSONObject blocoConversas() throws JSONException {
        JSONObject raiz = estado.raiz();
        JSONObject bloco = raiz.optJSONObject("conversasMock");
        if (bloco != null) return bloco;
        JSONObject conversa = conversa(1, 7, 3, "Condominio Jardim das Palmeiras", "Cooperativa EcoCiente");
        JSONArray mensagens = new JSONArray()
                .put(mensagem(1, "Olá! A próxima coleta está confirmada. Podemos ajudar com mais alguma coisa?",
                        "COOPERATIVA", "2026-11-05T09:00:00"));
        conversa.put("mensagens", mensagens);
        bloco = new JSONObject().put("proximoId", 2).put("itens", new JSONArray().put(conversa));
        raiz.put("conversasMock", bloco);
        estado.salvar();
        return bloco;
    }

    private JSONObject conversa(int id, int condominio, int cooperativa, String nomeCond, String nomeCoop)
            throws JSONException {
        return new JSONObject().put("id", id).put("condominioId", condominio)
                .put("cooperativaId", cooperativa).put("nomeCondominio", nomeCond)
                .put("nomeCooperativa", nomeCoop).put("ativa", true)
                .put("ultimaMensagemEm", "2026-11-05T09:00:00").put("naoLidas", 1);
    }

    private JSONObject mensagem(int id, String conteudo, String remetente, String data) throws JSONException {
        return new JSONObject().put("id", id).put("conteudo", conteudo)
                .put("remetenteTipo", remetente).put("enviadaEm", data);
    }

    private JSONObject encontrarConversa(int id) throws JSONException {
        JSONArray lista = blocoConversas().getJSONArray("itens");
        for (int i = 0; i < lista.length(); i++) {
            JSONObject c = lista.getJSONObject(i);
            if (c.optInt("id") == id) return c;
        }
        return null;
    }

    private RespostaDemo conversas(RequisicaoDemo r) throws JSONException {
        boolean arquivadas = "true".equalsIgnoreCase(r.query.get("arquivadas"));
        JSONArray lista = new JSONArray();
        JSONArray todos = blocoConversas().getJSONArray("itens");
        for (int i = 0; i < todos.length(); i++) {
            JSONObject c = todos.getJSONObject(i);
            if (c.optBoolean("ativa", true) == !arquivadas) lista.put(c);
        }
        return json(200, new JSONObject().put("itens", lista).put("total", lista.length())
                .put("last", true).put("totalPages", 1));
    }

    private RespostaDemo abrirConversa(RequisicaoDemo r) throws JSONException {
        JSONObject dados = r.corpoJson();
        if (dados == null || dados.optInt("condominioId", 0) <= 0 || dados.optInt("cooperativaId", 0) <= 0)
            return RespostaDemo.erro(400, "REQUISICAO_INVALIDA");
        JSONArray lista = blocoConversas().getJSONArray("itens");
        for (int i = 0; i < lista.length(); i++) {
            JSONObject c = lista.getJSONObject(i);
            if (c.optInt("condominioId") == dados.optInt("condominioId") &&
                    c.optInt("cooperativaId") == dados.optInt("cooperativaId")) return json(200, c);
        }
        JSONObject bloco = blocoConversas();
        int id = bloco.optInt("proximoId", 2);
        bloco.put("proximoId", id + 1);
        JSONObject c = conversa(id, dados.optInt("condominioId"), dados.optInt("cooperativaId"),
                "Condominio Jardim das Palmeiras", "Cooperativa EcoCiente");
        c.put("mensagens", new JSONArray());
        lista.put(c); estado.salvar();
        return json(201, c);
    }

    private RespostaDemo mensagens(int id) throws JSONException {
        JSONObject c = encontrarConversa(id);
        if (c == null) return RespostaDemo.erro(404, "CONVERSA_NAO_ENCONTRADA");
        JSONArray originais = c.optJSONArray("mensagens");
        JSONArray novos = new JSONArray();
        for (int i = originais.length() - 1; i >= 0; i--) novos.put(originais.getJSONObject(i));
        return json(200, new JSONObject().put("itens", novos).put("total", novos.length()).put("last", true));
    }

    private RespostaDemo enviar(int id, RequisicaoDemo r) throws JSONException {
        JSONObject c = encontrarConversa(id);
        if (c == null) return RespostaDemo.erro(404, "CONVERSA_NAO_ENCONTRADA");
        if (!c.optBoolean("ativa", true)) return RespostaDemo.erro(409, "CONVERSA_ARQUIVADA");
        JSONObject req = r.corpoJson();
        String msg = req == null ? "" : req.optString("conteudo", "").trim();
        if (msg.isEmpty() || msg.length() > 2000) return RespostaDemo.erro(400, "MENSAGEM_INVALIDA");
        String perfil = ContaDemo.me(estado.raiz()).optString("perfil", "");
        String lado = "COOPERATIVA".equals(perfil) ? "COOPERATIVA" : "CONDOMINIO";
        JSONArray lista = c.optJSONArray("mensagens");
        int proximoId = lista.length() + 1;
        String data = SementeDemo.formatarData(estado.agora());
        JSONObject mensagem = mensagem(proximoId, msg, lado, data);
        lista.put(mensagem);
        c.put("ultimaMensagemEm", data).put("naoLidas", 0);
        estado.salvar();
        return json(201, mensagem);
    }


    private RespostaDemo decidir(int id, RequisicaoDemo r) throws JSONException {
        if (!"SINDICO".equals(ContaDemo.me(estado.raiz()).optString("perfil")))
            return RespostaDemo.erro(403, "PERFIL_NAO_PERMITIDO");
        JSONObject req = r.corpoJson();
        if (req == null) return RespostaDemo.erro(400, "REQUISICAO_INVALIDA");
        boolean aprovar = req.optBoolean("aprovar", req.optBoolean("aprovado", true));
        for (JSONObject p : FeedDemo.postagens(estado.raiz())) {
            if (p.optInt("id") != id) continue;
            if (!FeedDemo.EM_ANALISE.equals(p.optString("statusValidacao")))
                return RespostaDemo.erro(409, "DECISAO_JA_REGISTRADA");
            p.put("statusValidacao", aprovar ? FeedDemo.APROVADA : "reprovada");
            if (aprovar && FeedDemo.doUsuario(estado.raiz(), p)) estado.creditar("POSTAGEM", 50);
            estado.salvar();
            return new RespostaDemo(204, "");
        }
        return RespostaDemo.erro(404, "POSTAGEM_NAO_ENCONTRADA");
    }

    private RespostaDemo rankingTorres(RequisicaoDemo r) throws JSONException {
        int condominio = UsuariosMultiperfilDemo.condominioId(estado.raiz());
        if (condominio <= 0 || !String.valueOf(condominio).equals(r.query.get("condominioId")))
            return RespostaDemo.erro(404, "CONDOMINIO_NAO_ENCONTRADO");
        JSONArray lista = new JSONArray()
                .put(new JSONObject().put("id", 2).put("nome", "Torre B").put("pontos", 1320).put("posicao", 1))
                .put(new JSONObject().put("id", 1).put("nome", "Torre A").put("pontos", 1150).put("posicao", 2))
                .put(new JSONObject().put("id", 3).put("nome", "Torre C").put("pontos", 980).put("posicao", 3));
        return json(200, new JSONObject().put("ranking", lista));
    }

    private RespostaDemo posicaoTorre(int id) throws JSONException {
        int pontos = id == 2 ? 1320 : id == 1 ? 1150 : id == 3 ? 980 : 0;
        if (pontos == 0) return RespostaDemo.erro(404, "TORRE_NAO_ENCONTRADA");
        int pos = id == 2 ? 1 : id == 1 ? 2 : 3;
        return json(200, new JSONObject().put("posicao", pos).put("pontos", pontos)
                .put("totalParticipantes", 3));
    }

    private static RespostaDemo json(int status, JSONObject obj) { return new RespostaDemo(status, obj.toString()); }
}
