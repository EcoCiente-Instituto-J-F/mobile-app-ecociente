package com.example.ecociente.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecociente.model.CooperativaProxima;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.repository.CalendarioExternoRepository;
import com.example.ecociente.repository.CooperativasProximasRepository;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CooperativasProximasViewModel extends AndroidViewModel {
    private final CooperativasProximasRepository repository = new CooperativasProximasRepository();
    private final CalendarioExternoRepository calendarioRepository = new CalendarioExternoRepository();

    private final MutableLiveData<List<CooperativaProxima>> lista =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> carregando = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> enviandoSolicitacao = new MutableLiveData<>(false);
    private final MutableLiveData<String> erro = new MutableLiveData<>();
    private final MutableLiveData<Boolean> vazio = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> condominioId = new MutableLiveData<>();

    private List<CooperativaProxima> original = new ArrayList<>();
    private String buscaAtual = "";

    public CooperativasProximasViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<CooperativaProxima>> getLista() { return lista; }
    public LiveData<Boolean> getCarregando() { return carregando; }
    public LiveData<Boolean> getEnviandoSolicitacao() { return enviandoSolicitacao; }
    public LiveData<String> getErro() { return erro; }
    public LiveData<Boolean> getVazio() { return vazio; }
    public LiveData<Integer> getCondominioId() { return condominioId; }

    public void carregar() {
        if (Boolean.TRUE.equals(carregando.getValue())) {
            return;
        }

        carregando.setValue(true);
        erro.setValue(null);

        repository.listar(getApplication(), resultado -> {
            carregando.setValue(false);

            if (!resultado.sucesso()) {
                erro.setValue(resultado.erro);
                vazio.setValue(false);
                return;
            }

            condominioId.setValue(resultado.condominioId);
            original = new ArrayList<>(resultado.cooperativas);
            pesquisar(buscaAtual);
            vazio.setValue(lista.getValue() == null || lista.getValue().isEmpty());
        });
    }

    public void pesquisar(@NonNull String consulta) {
        buscaAtual = consulta;
        String termo = normalizar(consulta);
        List<CooperativaProxima> filtradas = new ArrayList<>();

        for (CooperativaProxima item : original) {
            String indice = normalizar(
                    item.getNome() + " "
                            + item.getEndereco() + " "
                            + item.getCidade() + " "
                            + item.getEstado());
            if (indice.contains(termo)) {
                filtradas.add(item);
            }
        }

        lista.setValue(filtradas);
        vazio.setValue(filtradas.isEmpty() && erro.getValue() == null);
    }

    public void solicitarColeta(
            int cooperativaId,
            @NonNull String dataInicio,
            @NonNull CalendarioExternoRepository.RetornoStatus retorno) {

        Integer idCondominio = condominioId.getValue();
        if (idCondominio == null || idCondominio <= 0) {
            retorno.aoConcluir(ResultadoApi.erro(
                    "Não foi possível identificar o condomínio vinculado a esta conta."));
            return;
        }

        if (Boolean.TRUE.equals(enviandoSolicitacao.getValue())) {
            return;
        }

        enviandoSolicitacao.setValue(true);
        calendarioRepository.criar(
                getApplication(),
                idCondominio,
                cooperativaId,
                dataInicio,
                resultado -> {
                    enviandoSolicitacao.setValue(false);
                    retorno.aoConcluir(resultado);
                });
    }

    @NonNull
    private String normalizar(@NonNull String valor) {
        String semAcentos = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return semAcentos.trim().toLowerCase(Locale.ROOT);
    }
}
