package com.example.ecociente.views;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecociente.R;

// Só hospeda o NavHostFragment dos 3 passos (email -> código -> nova senha).
public class EsqueciSenha extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        setContentView(R.layout.activity_esqueci_senha);
    }
}
