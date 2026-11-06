package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import java.util.function.LongSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

// Todo o estado da demo num único JSON, gravado no armazém a cada alteração. Quem muda `raiz()` chama `salvar()` em seguida e, para a mudança ser atômica, faz as duas coisas dentro de `synchronized (estado)`: a ApiDemo e o ProvedorContaDemo usam este mesmo objeto como trava.
public final class EstadoDemo {

    private static final Logger LOG = Logger.getLogger("EcoCienteDemo");

    private final ArmazemDemo armazem;
    private final LongSupplier relogio;

    private JSONObject raiz;

    // Carrega do armazém; ausente, corrompido ou de outra versão, começa da semente.
    public EstadoDemo(@NonNull ArmazemDemo armazem, @NonNull LongSupplier relogio) {
        this.armazem = armazem;
        this.relogio = relogio;
        this.raiz = carregar();
    }

    @NonNull
    public synchronized JSONObject raiz() {
        return raiz;
    }

    public synchronized void salvar() {
        armazem.salvar(raiz.toString());
    }

    // Pontos do morador. Quem chama de fora da ApiDemo não precisa travar: os métodos já são synchronized.
    public synchronized int saldo() {
        return PontuacaoDemo.saldo(raiz);
    }

    // 1 para o primeiro do ranking; 0 se o morador não está nele.
    public synchronized int posicaoDoUsuario() {
        return PontuacaoDemo.posicaoDoUsuario(raiz);
    }

    // Soma pontos ao morador (origem POSTAGEM ou QUIZ), grava a movimentação e persiste.
    public synchronized void creditar(@NonNull String origemTipo, int pontos) {
        PontuacaoDemo.creditar(raiz, origemTipo, pontos, relogio.getAsLong());
        salvar();
    }

    // Grava o JPEG enviado (upload do avatar ou do descarte) e devolve a URL file:// dele.
    @NonNull
    public synchronized String gravarFoto(@NonNull byte[] jpeg) {
        return armazem.gravarFoto(jpeg);
    }

    // Apaga o arquivo e as fotos e recomeça da semente.
    public synchronized void resetar() {
        armazem.apagarTudo();
        raiz = SementeDemo.criar(relogio.getAsLong());
        salvar();
    }

    @NonNull
    private JSONObject carregar() {
        String json = armazem.ler();

        if (json != null) {
            try {
                JSONObject lido = new JSONObject(json);

                if (lido.optInt("versao", 0) == SementeDemo.VERSAO) {
                    // A semente de agendamentos é relativa à semana em que nasceu: abrindo o app em outra semana, só ela é refeita.
                    long agora = relogio.getAsLong();

                    if (!SementeDemo.semanaDe(agora).equals(lido.optString("agendamentosSemana", ""))) {
                        SementeDemo.renovarAgendamentos(lido, agora);
                        armazem.salvar(lido.toString());
                    }

                    return lido;
                }

                LOG.warning("Estado da demo de outra versão; recomeçando da semente");

            } catch (JSONException erro) {
                LOG.log(Level.WARNING, "Estado da demo corrompido; recomeçando da semente", erro);
            }
        }

        JSONObject semente = SementeDemo.criar(relogio.getAsLong());

        armazem.salvar(semente.toString());

        return semente;
    }
}
