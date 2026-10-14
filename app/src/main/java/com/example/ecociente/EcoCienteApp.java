package com.example.ecociente;

import android.app.Application;
import com.example.ecociente.ui.OrientacaoPorTamanho;

public class EcoCienteApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new OrientacaoPorTamanho());
    }
}
