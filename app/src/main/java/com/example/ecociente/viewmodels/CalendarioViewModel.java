package com.example.ecociente.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.ecociente.model.ResultadoSolicitacoes;
import com.example.ecociente.repository.CalendarioExternoRepository;
import com.example.ecociente.ui.SemanaColetasView;
import java.util.Calendar;

public class CalendarioViewModel extends AndroidViewModel {

    private final CalendarioExternoRepository repositorio = new CalendarioExternoRepository();

    private final MutableLiveData<ResultadoSolicitacoes> proxima = new MutableLiveData<>();
    private final MutableLiveData<ResultadoSolicitacoes> coletasDoMes = new MutableLiveData<>();
    private final MutableLiveData<ResultadoSolicitacoes> coletasDaSemana = new MutableLiveData<>();
    private final MutableLiveData<int[]> mesExibido = new MutableLiveData<>();

    public CalendarioViewModel(@NonNull Application aplicacao) {
        super(aplicacao);

        Calendar hoje = Calendar.getInstance();

        mesExibido.setValue(new int[] {hoje.get(Calendar.YEAR), hoje.get(Calendar.MONTH)});
    }

    @NonNull
    public LiveData<ResultadoSolicitacoes> getProxima() {
        return proxima;
    }

    @NonNull
    public LiveData<ResultadoSolicitacoes> getColetasDoMes() {
        return coletasDoMes;
    }

    @NonNull
    public LiveData<ResultadoSolicitacoes> getColetasDaSemana() {
        return coletasDaSemana;
    }

    @NonNull
    public LiveData<int[]> getMesExibido() {
        return mesExibido;
    }

    public boolean jaCarregou() {
        return proxima.getValue() != null;
    }

    public void carregar() {

        repositorio.proxima(getApplication(), proxima::setValue);

        carregarMes();
    }

    public void carregarSemana() {

        repositorio.doPeriodo(
                getApplication(),
                SemanaColetasView.segundaDaSemana(),
                SemanaColetasView.fimDaSemana(),
                coletasDaSemana::setValue);
    }

    public void mesAnterior() {
        mudarMes(-1);
    }

    public void proximoMes() {
        mudarMes(1);
    }

    private void mudarMes(int deslocamento) {

        int[] atual = mesExibido.getValue();

        Calendar calendario = Calendar.getInstance();

        calendario.clear();
        calendario.set(atual[0], atual[1], 1);
        calendario.add(Calendar.MONTH, deslocamento);

        mesExibido.setValue(
                new int[] {calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH)});

        carregarMes();
    }

    private void carregarMes() {

        int[] mes = mesExibido.getValue();

        repositorio.doMes(
                getApplication(),
                mes[0],
                mes[1],
                resultado -> {
                    int[] atual = mesExibido.getValue();

                    if (atual[0] == mes[0] && atual[1] == mes[1]) {
                        coletasDoMes.setValue(resultado);
                    }
                });
    }
}
