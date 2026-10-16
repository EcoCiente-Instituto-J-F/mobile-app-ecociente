package com.example.ecociente.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.ecociente.model.DadosDemonstracao;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.model.StatusAgendamento;
import com.example.ecociente.repository.CalendarioExternoRepository;
import com.example.ecociente.repository.SolicitacoesExemplosRepository;

public class ResponderSolicitacaoViewModel extends ViewModelComCarregamento {

    private final CalendarioExternoRepository repositorio = new CalendarioExternoRepository();

    @NonNull
    public LiveData<ResultadoApi> responder(
            @NonNull Context contexto, int id, @NonNull StatusAgendamento resposta) {

        MutableLiveData<ResultadoApi> resultado = new MutableLiveData<>();

        if (DadosDemonstracao.ATIVO) {
            SolicitacoesExemplosRepository.getInstance().alterarStatus(id, resposta);

            resultado.setValue(ResultadoApi.sucesso());

            return executar(resultado);
        }

        repositorio.alterarStatus(contexto.getApplicationContext(), id, resposta, resultado::setValue);

        return executar(resultado);
    }
}
