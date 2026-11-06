package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.LongSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

// A API de mentira: recebe o pedido que o app faria à rede e responde com o estado da demo. Java puro, sem Android, para os testes rodarem na JVM. Cada área de rotas é uma classe; área nova entra na lista `rotas`.
public final class ApiDemo implements TratadorDemo {

    private static final Logger LOG = Logger.getLogger("EcoCienteDemo");

    private final EstadoDemo estado;
    private final LongSupplier relogio;
    private final List<RotasDemo> rotas;

    // Chamado fora da trava; pode mudar a qualquer momento (o Application o define depois de criar a API).
    @Nullable private volatile OuvinteDemo ouvinte;

    public ApiDemo(@NonNull EstadoDemo estado, @NonNull LongSupplier relogio) {
        this.estado = estado;
        this.relogio = relogio;
        this.rotas =
                Arrays.asList(
                        new RotasConta(estado, relogio),
                        new RotasPontuacao(estado),
                        new RotasCalendario(estado, relogio),
                        new RotasFeed(estado, relogio),
                        new RotasEducacao(estado),
                        new RotasQuiz(estado, relogio),
                        new RotasUpload(estado),
                        new RotasDiversas(estado));
    }

    // Quem é avisado quando um descarte do morador é aprovado.
    public void definirOuvinte(@Nullable OuvinteDemo ouvinte) {
        this.ouvinte = ouvinte;
    }

    // Uma trava só (o EstadoDemo) para pedidos da rede, do ProvedorContaDemo e do temporizador.
    @NonNull
    @Override
    public RespostaDemo tratar(
            @NonNull String metodo, @NonNull String url, @Nullable String autorizacao, @NonNull byte[] corpo) {

        List<Boolean> aprovacoes;
        RespostaDemo resposta;

        synchronized (estado) {
            // Aprovações vencidas valem antes da resposta: quem consulta logo depois do prazo já vê o resultado.
            aprovacoes = aprovarVencidas();
            resposta = responder(metodo, url, autorizacao, corpo);
        }

        avisar(aprovacoes);

        return resposta;
    }

    // Sob a trava do estado.
    @NonNull
    private RespostaDemo responder(
            @NonNull String metodo, @NonNull String url, @Nullable String autorizacao, @NonNull byte[] corpo) {

        try {
            RequisicaoDemo requisicao = RequisicaoDemo.de(metodo, url, autorizacao, corpo);

            // O AquecimentoDasApis só quer ver o serviço de pé, sem token.
            if (requisicao.e("GET", "/")) {
                return new RespostaDemo(200, "{\"status\":\"UP\"}");
            }

            for (RotasDemo grupo : rotas) {
                RespostaDemo resposta = grupo.tratar(requisicao);

                if (resposta != null) {
                    return resposta;
                }
            }

            // Sem handler: o log aponta a lacuna ao testar no aparelho. O código de negócio é outro que o do "usuário não encontrado", para o login não tratar como recusa.
            LOG.warning("Demo sem rota: " + requisicao.metodo + " " + requisicao.host + requisicao.caminho);

            return RespostaDemo.erro(404, "ROTA_NAO_ENCONTRADA");

        } catch (URISyntaxException erro) {
            LOG.warning("Demo recebeu URL inválida: " + url);

            return RespostaDemo.erro(400, "REQUISICAO_INVALIDA");

        } catch (RuntimeException erro) {
            LOG.log(Level.SEVERE, "Erro na rota da demo " + metodo + " " + url, erro);

            return RespostaDemo.erro(500, "ERRO_INTERNO");
        }
    }

    // Aprovações vencidas viram resultado. Chamado a cada poucos segundos por um temporizador.
    public void processarPendentes() {
        List<Boolean> aprovacoes;

        synchronized (estado) {
            aprovacoes = aprovarVencidas();
        }

        avisar(aprovacoes);
    }

    // Sob a trava do estado.
    @NonNull
    private List<Boolean> aprovarVencidas() {
        try {
            return FeedDemo.aprovarVencidas(estado, relogio.getAsLong());

        } catch (RuntimeException erro) {
            // Uma falha aqui não pode derrubar o pedido que passou por perto.
            LOG.log(Level.SEVERE, "Erro ao aprovar descartes da demo", erro);

            return Collections.emptyList();
        }
    }

    // Fora da trava: o ouvinte pode demorar ou tocar no estado. Uma falha dele não desfaz a aprovação.
    private void avisar(@NonNull List<Boolean> aprovacoes) {
        OuvinteDemo atual = ouvinte;

        for (int i = 0; atual != null && i < aprovacoes.size(); i++) {
            try {
                atual.aoAprovarDescarte(aprovacoes.get(i));

            } catch (RuntimeException erro) {
                LOG.log(Level.WARNING, "Ouvinte da demo falhou ao receber a aprovação", erro);
            }
        }
    }
}
