package com.example.ecociente.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.example.ecociente.R;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.model.TipoLista;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;

// "Nova notificação" da cooperativa: escolhe o condomínio de destino, título e mensagem.
final class NovoAvisoCooperativaDialogo {

    private NovoAvisoCooperativaDialogo() {}

    static void mostrar(@NonNull Context contexto) {

        View conteudo = LayoutInflater.from(contexto).inflate(R.layout.dialog_novo_aviso, null);

        AutoCompleteTextView destino = conteudo.findViewById(R.id.campoDestinoAviso);
        EditText titulo = conteudo.findViewById(R.id.campoTituloAviso);
        EditText mensagem = conteudo.findViewById(R.id.campoMensagemAviso);

        List<String> destinos = new ArrayList<>();

        destinos.add(contexto.getString(R.string.todos_os_condominios));

        for (ItemLista condominio : TipoLista.CONDOMINIOS.itens()) {
            destinos.add(condominio.titulo);
        }

        destino.setAdapter(new ArrayAdapter<>(contexto, android.R.layout.simple_list_item_1, destinos));

        AlertDialog dialogo = new MaterialAlertDialogBuilder(contexto).setView(conteudo).create();

        conteudo.findViewById(R.id.botaoFecharNovoAviso).setOnClickListener(view -> dialogo.dismiss());
        conteudo.findViewById(R.id.botaoCancelarNovoAviso).setOnClickListener(view -> dialogo.dismiss());

        conteudo.findViewById(R.id.botaoEnviarNovoAviso)
                .setOnClickListener(
                        view -> {
                            if (destino.getText().length() == 0
                                    || titulo.getText().toString().trim().isEmpty()
                                    || mensagem.getText().toString().trim().isEmpty()) {
                                Toast.makeText(contexto, R.string.preencha_todos_os_campos, Toast.LENGTH_SHORT)
                                        .show();
                                return;
                            }

                            Toast.makeText(contexto, R.string.aviso_enviado_exemplo, Toast.LENGTH_SHORT).show();

                            dialogo.dismiss();
                        });

        dialogo.show();
    }
}
