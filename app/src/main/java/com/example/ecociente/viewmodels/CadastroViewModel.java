package com.example.ecociente.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.model.ResultadoCadastro;
import com.example.ecociente.repository.CadastroExternoRepository;
import com.example.ecociente.repository.CadastroRepository;
import java.util.Map;

public class CadastroViewModel extends ViewModel {
    private final CadastroRepository repositorio = new CadastroRepository();
    private final CadastroExternoRepository repositorioExterno = new CadastroExternoRepository();
    private final MutableLiveData<Boolean> carregando = new MutableLiveData<>(false);

    @NonNull
    public LiveData<Boolean> getCarregando() {
        return carregando;
    }

    // Cadastro precisa existir nos dois lugares (Firebase e no Postgres da
    // API externa). A API externa vai primeiro porque ela não tem endpoint
    // de cancelamento - se falhar ali, nada foi criado ainda em lugar
    // nenhum. Só cria a conta no Firebase depois dela confirmar.
    @NonNull
    public LiveData<ResultadoCadastro> cadastrar(
            @NonNull String nome,
            @NonNull String email,
            @NonNull String senha,
            @NonNull Map<String, Object> dadosUsuario) {
        carregando.setValue(true);

        MediatorLiveData<ResultadoCadastro> resultado = new MediatorLiveData<>();

        LiveData<ResultadoApi> cadastroExterno = repositorioExterno.cadastrar(dadosUsuario, senha);

        resultado.addSource(
                cadastroExterno,
                respostaExterna -> {
                    resultado.removeSource(cadastroExterno);

                    if (!respostaExterna.isSucesso()) {
                        carregando.setValue(false);
                        resultado.setValue(ResultadoCadastro.erro(respostaExterna.getMensagemErro()));
                        return;
                    }

                    LiveData<ResultadoCadastro> cadastroFirebase =
                            repositorio.cadastrar(nome, email, senha, dadosUsuario);

                    resultado.addSource(
                            cadastroFirebase,
                            valor -> {
                                carregando.setValue(false);
                                resultado.setValue(valor);
                            });
                });

        return resultado;
    }
}
