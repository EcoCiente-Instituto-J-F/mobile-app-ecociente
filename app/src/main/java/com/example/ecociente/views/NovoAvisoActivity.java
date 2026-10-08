package com.example.ecociente.views;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecociente.R;
import com.example.ecociente.model.CategoriaAviso;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.model.TipoLista;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import java.util.ArrayList;
import java.util.List;

public class NovoAvisoActivity extends AppCompatActivity {

    private static final CategoriaAviso[] CATEGORIAS_DO_ENVIO = {
        CategoriaAviso.COLETA,
        CategoriaAviso.ORIENTACAO,
        CategoriaAviso.MANUTENCAO,
        CategoriaAviso.CAMPANHA,
        CategoriaAviso.AVISO_GERAL
    };

    private AutoCompleteTextView campoCategoria;
    private AutoCompleteTextView campoDestino;
    private EditText campoTitulo;
    private EditText campoMensagem;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);

        JanelaEdgeToEdge.aplicar(this);

        setContentView(R.layout.activity_novo_aviso);

        InsetsSistema.aplicarComoPadding(findViewById(R.id.raizNovoAviso));

        findViewById(R.id.botaoVoltarNovoAviso).setOnClickListener(view -> finish());

        campoCategoria = findViewById(R.id.campoCategoriaAviso);
        campoDestino = findViewById(R.id.campoDestinoAviso);
        campoTitulo = findViewById(R.id.campoTituloAviso);
        campoMensagem = findViewById(R.id.campoMensagemAviso);

        campoCategoria.setAdapter(adaptador(rotulosDasCategorias()));
        campoDestino.setAdapter(adaptador(destinos()));
        campoDestino.setText(getString(R.string.todos_os_condominios), false);

        findViewById(R.id.botaoEnviarAviso).setOnClickListener(view -> enviar());
    }

    private ArrayAdapter<String> adaptador(List<String> itens) {
        return new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itens);
    }

    private List<String> rotulosDasCategorias() {

        List<String> rotulos = new ArrayList<>();

        for (CategoriaAviso categoria : CATEGORIAS_DO_ENVIO) {
            rotulos.add(getString(categoria.rotulo));
        }

        return rotulos;
    }

    private List<String> destinos() {

        List<String> nomes = new ArrayList<>();

        nomes.add(getString(R.string.todos_os_condominios));

        for (ItemLista condominio : TipoLista.CONDOMINIOS.itens()) {
            nomes.add(condominio.titulo);
        }

        return nomes;
    }

    private void enviar() {

        if (campoCategoria.getText().length() == 0
                || campoTitulo.getText().toString().trim().isEmpty()
                || campoMensagem.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.preencha_todos_os_campos, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, R.string.aviso_enviado_exemplo, Toast.LENGTH_SHORT).show();

        finish();
    }
}
