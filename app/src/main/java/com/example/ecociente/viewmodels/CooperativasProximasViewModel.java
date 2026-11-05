package com.example.ecociente.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.ecociente.model.CooperativaProxima;
import com.example.ecociente.repository.CooperativasProximasRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CooperativasProximasViewModel extends AndroidViewModel {
    private final CooperativasProximasRepository repository = new CooperativasProximasRepository();
    private final MutableLiveData<List<CooperativaProxima>> lista =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> carregando = new MutableLiveData<>(false);
    private final MutableLiveData<String> erro = new MutableLiveData<>();
    private final MutableLiveData<Boolean> vazio = new MutableLiveData<>(false);
    private List<CooperativaProxima> original = new ArrayList<>();
    private String buscaAtual = "";

    public CooperativasProximasViewModel(@NonNull Application application) { super(application); }
    public LiveData<List<CooperativaProxima>> getLista() { return lista; }
    public LiveData<Boolean> getCarregando() { return carregando; }
    public LiveData<String> getErro() { return erro; }
    public LiveData<Boolean> getVazio() { return vazio; }

    public void carregar() {
        if (Boolean.TRUE.equals(carregando.getValue())) return;
        carregando.setValue(true);
        erro.setValue(null);
        repository.listar(getApplication(), resultado -> {
            carregando.setValue(false);
            if (!resultado.sucesso()) {
                erro.setValue(resultado.erro);
                vazio.setValue(false);
                return;
            }
            original = new ArrayList<>(resultado.cooperativas);
            pesquisar(buscaAtual);
            vazio.setValue(lista.getValue() == null || lista.getValue().isEmpty());
        });
    }

    public void pesquisar(@NonNull String consulta) {
        buscaAtual = consulta;
        String termo = consulta.trim().toLowerCase(Locale.ROOT);
        List<CooperativaProxima> filtradas = new ArrayList<>();
        for (CooperativaProxima item : original) {
            String indice = (item.getNome() + " " + item.getEndereco() + " "
                    + item.getCidade() + " " + item.getEstado()).toLowerCase(Locale.ROOT);
            if (indice.contains(termo)) filtradas.add(item);
        }
        lista.setValue(filtradas);
        vazio.setValue(filtradas.isEmpty() && erro.getValue() == null);
    }
}