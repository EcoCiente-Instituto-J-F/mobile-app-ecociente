package com.example.ecociente.views;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecociente.R;

// Activity "casca": só hospeda o NavHostFragment com os 3 passos do fluxo
// (email -> código -> nova senha). Toda a lógica fica nos Fragments.
public class EsqueciSenha extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        setContentView(R.layout.activity_esqueci_senha);
    }
}
