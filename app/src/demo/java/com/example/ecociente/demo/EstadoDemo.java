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
