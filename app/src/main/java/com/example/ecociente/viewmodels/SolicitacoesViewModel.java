package com.example.ecociente.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.ecociente.model.ResultadoSolicitacoes;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.model.StatusAgendamento;
import com.example.ecociente.repository.CalendarioExternoRepository;
import java.util.ArrayList;
import java.util.List;

public class SolicitacoesViewModel extends AndroidViewModel {

    private static final boolean USAR_EXEMPLOS = true;

    private final CalendarioExternoRepository repositorio = new CalendarioExternoRepository();

    private final MutableLiveData<List<Solicitacao>> itens =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<ResultadoSolicitacoes.Tipo> situacao = new MutableLiveData<>();
    private final MutableLiveData<Boolean> carregando = new MutableLiveData<>(false);

    private StatusAgendamento filtro = StatusAgendamento.AGENDADO;
    private int proximaPagina;
    private boolean ultimaPagina;

    public SolicitacoesViewModel(@NonNull Application aplicacao) {
        super(aplicacao);
    }

    @NonNull
    public LiveData<List<Solicitacao>> getItens() {
        return itens;
    }

    @NonNull
    public LiveData<ResultadoSolicitacoes.Tipo> getSituacao() {
        return situacao;
    }

    @NonNull
    public LiveData<Boolean> getCarregando() {
        return carregando;
    }

    @NonNull
    public StatusAgendamento getFiltro() {
        return filtro;
    }

    public boolean jaCarregou() {
        return situacao.getValue() != null;
    }

    public void definirFiltroInicial(@NonNull StatusAgendamento inicial) {

        if (!jaCarregou()) {
            filtro = inicial;
        }
    }

    public void filtrar(@NonNull StatusAgendamento novoFiltro) {

        if (novoFiltro == filtro && jaCarregou()) {
            return;
        }

        filtro = novoFiltro;

        recarregar();
    }

    public void recarregar() {

        proximaPagina = 0;
        ultimaPagina = false;

        itens.setValue(new ArrayList<>());

        carregarPagina();
    }

    public void carregarMais() {

        if (!ultimaPagina) {
            carregarPagina();
        }
    }

    private void exibirExemplos() {

        List<Solicitacao> filtradas = new ArrayList<>();

        for (Solicitacao solicitacao : Solicitacao.exemplos()) {
            if (solicitacao.getStatus() == filtro) {
                filtradas.add(solicitacao);
            }
        }

        itens.setValue(filtradas);

        ultimaPagina = true;

        situacao.setValue(ResultadoSolicitacoes.Tipo.SUCESSO);
    }

    private void carregarPagina() {

        if (Boolean.TRUE.equals(carregando.getValue())) {
            return;
        }

        if (USAR_EXEMPLOS) {
            exibirExemplos();
            return;
        }

        carregando.setValue(true);

        StatusAgendamento filtroDaChamada = filtro;

        repositorio.listar(
                getApplication(),
                filtroDaChamada,
                proximaPagina,
                resultado -> {
                    carregando.setValue(false);

                    if (filtroDaChamada != filtro) {
                        recarregar();
                        return;
                    }

                    if (resultado.getTipo() == ResultadoSolicitacoes.Tipo.SUCESSO) {

                        List<Solicitacao> acumulado = new ArrayList<>(itens.getValue());

                        acumulado.addAll(resultado.getItens());

                        itens.setValue(acumulado);

                        ultimaPagina = resultado.ehUltimaPagina();

                        proximaPagina++;
                    }

                    situacao.setValue(resultado.getTipo());
                });
    }
}
