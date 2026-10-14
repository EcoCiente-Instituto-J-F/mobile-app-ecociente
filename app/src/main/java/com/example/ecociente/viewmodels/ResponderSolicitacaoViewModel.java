package com.example.ecociente.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.model.StatusAgendamento;
import com.example.ecociente.repository.CalendarioExternoRepository;

public class ResponderSolicitacaoViewModel extends ViewModelComCarregamento {

    private final CalendarioExternoRepository repositorio = new CalendarioExternoRepository();

    @NonNull
    public LiveData<ResultadoApi> responder(
            @NonNull Context contexto, int id, @NonNull StatusAgendamento resposta) {

        MutableLiveData<ResultadoApi> resultado = new MutableLiveData<>();

        repositorio.alterarStatus(contexto, id, resposta, resultado::setValue);

        return executar(resultado);
    }
}
