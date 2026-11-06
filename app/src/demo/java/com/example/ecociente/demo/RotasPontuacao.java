package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// ds-pontuacao-api: saldo, movimentações e posição do usuário, e o ranking de moradores do condomínio.
final class RotasPontuacao implements RotasDemo {

    private static final String HOST = "ds-pontuacao-api.onrender.com";
    private static final String RANKING = "/api/v1/rankings/moradores";

    private final EstadoDemo estado;

    RotasPontuacao(@NonNull EstadoDemo estado) {
        this.estado = estado;
    }

    @Nullable
    @Override
    public RespostaDemo tratar(@NonNull RequisicaoDemo r) {
        if (!HOST.equals(r.host) || !"GET".equals(r.metodo)) {
            return null;
        }

        String USUARIO = "/api/v1/pontuacoes/usuarios/" + UsuariosMultiperfilDemo.usuarioId(estado.raiz());
        boolean outroUsuario = r.caminho.startsWith("/api/v1/pontuacoes/usuarios/") && !r.caminho.startsWith(USUARIO + "/");
        boolean outraPosicao = r.caminho.startsWith(RANKING + "/") && !r.caminho.equals(RANKING + "/" + UsuariosMultiperfilDemo.usuarioId(estado.raiz()) + "/posicao");

        if (!(r.caminho.equals(USUARIO + "/saldo")
                || r.caminho.equals(USUARIO + "/movimentacoes")
                || r.caminho.equals(RANKING)
                || r.caminho.equals(RANKING + "/" + UsuariosMultiperfilDemo.usuarioId(estado.raiz()) + "/posicao")
                || outroUsuario
                || outraPosicao)) {
            return null;
        }

        if (!ContaDemo.tokenConfere(estado.raiz(), r.autorizacao)) {
            return RespostaDemo.erro(401, "NAO_AUTORIZADO");
        }

        // A API só deixa o usuário consultar os próprios pontos.
        if (outroUsuario || outraPosicao) {
            return RespostaDemo.erro(403, "ACESSO_NEGADO");
        }

        if (r.caminho.endsWith("/saldo")) {
            return resposta(new JSONObject(), "saldo", estado.saldo());
        }

        if (r.caminho.endsWith("/movimentacoes")) {
            return movimentacoes(r);
        }

        if (!String.valueOf(UsuariosMultiperfilDemo.condominioId(estado.raiz())).equals(r.query.get("condominioId"))) {
            return RespostaDemo.erro(404, "CONDOMINIO_NAO_ENCONTRADO");
        }

        return r.caminho.equals(RANKING) ? ranking(r) : posicao();
    }

    @NonNull
    private static RespostaDemo resposta(@NonNull JSONObject json, @NonNull String campo, int valor) {
        try {
            return new RespostaDemo(200, json.put(campo, valor).toString());

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    // `pagina` começa em 0, como o PontuacaoJson.ultimaPagina espera.
    @NonNull
    private RespostaDemo movimentacoes(@NonNull RequisicaoDemo r) {
        List<JSONObject> todas = PontuacaoDemo.movimentacoes(estado.raiz());

        int pagina = Math.max(0, inteiro(r.query.get("pagina"), 0));
        int tamanho = inteiro(r.query.get("tamanho"), 10);

        if (tamanho <= 0) {
            tamanho = 10;
        }

        int totalPaginas = Math.max(1, (todas.size() + tamanho - 1) / tamanho);
        JSONArray conteudo = new JSONArray();

        for (int i = pagina * tamanho; i < Math.min(todas.size(), (pagina + 1) * tamanho); i++) {
            conteudo.put(todas.get(i));
        }

        try {
            return new RespostaDemo(
                    200,
                    new JSONObject()
                            .put("conteudo", conteudo)
                            .put("pagina", pagina)
                            .put("totalPaginas", totalPaginas)
                            .toString());

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    // Sem `limit`, todos os participantes.
    @NonNull
    private RespostaDemo ranking(@NonNull RequisicaoDemo r) {
        List<PontuacaoDemo.Participante> ranking = PontuacaoDemo.ranking(estado.raiz());
        int limite = inteiro(r.query.get("limit"), ranking.size());

        if (limite <= 0) {
            limite = ranking.size();
        }

        JSONArray itens = new JSONArray();

        try {
            for (int i = 0; i < Math.min(limite, ranking.size()); i++) {
                PontuacaoDemo.Participante p = ranking.get(i);

                itens.put(new JSONObject().put("posicao", i + 1).put("nome", p.nome).put("pontos", p.pontos));
            }

            return new RespostaDemo(200, new JSONObject().put("ranking", itens).toString());

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    @NonNull
    private RespostaDemo posicao() {
        try {
            return new RespostaDemo(
                    200,
                    new JSONObject()
                            .put("posicao", estado.posicaoDoUsuario())
                            .put("pontos", estado.saldo())
                            .put("totalParticipantes", PontuacaoDemo.ranking(estado.raiz()).size())
                            .toString());

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    private static int inteiro(@Nullable String texto, int padrao) {
        try {
            return texto == null ? padrao : Integer.parseInt(texto.trim());

        } catch (NumberFormatException erro) {
            return padrao;
        }
    }
}
