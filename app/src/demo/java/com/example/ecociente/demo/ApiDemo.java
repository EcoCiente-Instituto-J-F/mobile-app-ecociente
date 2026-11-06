package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.function.LongSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

// A API de mentira: recebe o pedido que o app faria à rede e responde com o estado da demo. Java puro, sem Android, para os testes rodarem na JVM. Cada área de rotas é uma classe; área nova entra na lista `rotas`.
public final class ApiDemo implements TratadorDemo {

    private static final Logger LOG = Logger.getLogger("EcoCienteDemo");

    private final EstadoDemo estado;
    private final List<RotasDemo> rotas;

    public ApiDemo(@NonNull EstadoDemo estado, @NonNull LongSupplier relogio) {
        this.estado = estado;
        this.rotas = Arrays.asList(new RotasConta(estado, relogio));
    }

    // Uma trava só (o EstadoDemo) para pedidos da rede, do ProvedorContaDemo e do temporizador.
    @NonNull
    @Override
    public RespostaDemo tratar(
            @NonNull String metodo, @NonNull String url, @Nullable String autorizacao, @NonNull byte[] corpo) {

        synchronized (estado) {
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
    }

    // Aprovações vencidas viram resultado (entra na Task 5). Chamado a cada poucos segundos por um temporizador.
    public void processarPendentes() {
        synchronized (estado) {
            // Sem pendências ainda.
        }
    }
}
