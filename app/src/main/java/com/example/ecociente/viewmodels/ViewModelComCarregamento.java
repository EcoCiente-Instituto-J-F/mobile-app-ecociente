package com.example.ecociente.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public abstract class ViewModelComCarregamento extends ViewModel {
    private final MutableLiveData<Boolean> carregando = new MutableLiveData<>(false);

    @NonNull
    public LiveData<Boolean> getCarregando() {
        return carregando;
    }

    protected void definirCarregando(boolean valor) {
        carregando.setValue(valor);
    }

    @NonNull
    protected <T> LiveData<T> executar(@NonNull LiveData<T> chamada) {
        carregando.setValue(true);

        MediatorLiveData<T> resultado = new MediatorLiveData<>();
        resultado.addSource(
                chamada,
                valor -> {
                    carregando.setValue(false);
                    resultado.setValue(valor);
                });

        return resultado;
    }
}
