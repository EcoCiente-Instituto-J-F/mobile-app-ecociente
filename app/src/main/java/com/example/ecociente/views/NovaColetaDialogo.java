package com.example.ecociente.views;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecociente.R;
import com.example.ecociente.model.ItemLista;
import com.example.ecociente.model.TipoLista;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public final class NovaColetaDialogo {

    private final AppCompatActivity tela;
    private final Runnable aoSalvar;

    private AutoCompleteTextView campoCondominio;
    private EditText campoEndereco;
    private EditText campoData;
    private EditText campoInicio;
    private EditText campoFim;
    private MaterialSwitch campoRecorrencia;
    private AlertDialog dialogo;

    private NovaColetaDialogo(@NonNull AppCompatActivity tela, @NonNull Runnable aoSalvar) {
        this.tela = tela;
        this.aoSalvar = aoSalvar;
    }

    public static void mostrar(@NonNull AppCompatActivity tela, @NonNull Runnable aoSalvar) {
        new NovaColetaDialogo(tela, aoSalvar).abrir();
    }

    private void abrir() {

        View conteudo = LayoutInflater.from(tela).inflate(R.layout.dialog_nova_coleta, null);

        campoCondominio = conteudo.findViewById(R.id.campoCondominioColeta);
        campoEndereco = conteudo.findViewById(R.id.campoEnderecoColeta);
        campoData = conteudo.findViewById(R.id.campoDataColeta);
        campoInicio = conteudo.findViewById(R.id.campoInicioColeta);
        campoFim = conteudo.findViewById(R.id.campoFimColeta);
        campoRecorrencia = conteudo.findViewById(R.id.campoRecorrenciaColeta);

        campoCondominio.setAdapter(
                new ArrayAdapter<>(tela, android.R.layout.simple_list_item_1, nomesDosCondominios()));

        campoCondominio.setOnItemClickListener(
                (pai, view, posicao, id) -> campoEndereco.setText(enderecoDoCondominio(posicao)));

        campoData.setOnClickListener(view -> escolherData());
        campoInicio.setOnClickListener(view -> escolherHora(campoInicio));
        campoFim.setOnClickListener(view -> escolherHora(campoFim));

        dialogo = new MaterialAlertDialogBuilder(tela).setView(conteudo).create();

        conteudo.findViewById(R.id.botaoFecharNovaColeta).setOnClickListener(view -> dialogo.dismiss());
        conteudo.findViewById(R.id.botaoCancelarColeta).setOnClickListener(view -> dialogo.dismiss());
        conteudo.findViewById(R.id.botaoSalvarColeta).setOnClickListener(view -> salvar());

        dialogo.show();
    }

    @NonNull
    private String[] nomesDosCondominios() {

        List<ItemLista> itens = TipoLista.CONDOMINIOS.itens();

        String[] nomes = new String[itens.size()];

        for (int i = 0; i < nomes.length; i++) {
            nomes[i] = itens.get(i).titulo;
        }

        return nomes;
    }

    @NonNull
    private String enderecoDoCondominio(int posicao) {

        String subtitulo = TipoLista.CONDOMINIOS.itens().get(posicao).subtitulo;

        int separador = subtitulo.indexOf(" · ");

        return separador < 0 ? subtitulo : subtitulo.substring(0, separador);
    }

    private void escolherData() {

        MaterialDatePicker<Long> seletor =
                MaterialDatePicker.Builder.datePicker()
                        .setTheme(R.style.ThemeOverlay_EcoCiente_MaterialDatePicker)
                        .setTitleText(R.string.data)
                        .build();

        seletor.addOnPositiveButtonClickListener(
                selecao -> {
                    SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                    formato.setTimeZone(TimeZone.getTimeZone("UTC"));

                    campoData.setText(formato.format(new Date(selecao)));
                });

        seletor.show(tela.getSupportFragmentManager(), "data_coleta");
    }

    private void escolherHora(@NonNull EditText campo) {

        MaterialTimePicker seletor =
                new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).setHour(8).setMinute(0).build();

        seletor.addOnPositiveButtonClickListener(
                view ->
                        campo.setText(
                                String.format(Locale.getDefault(), "%02d:%02d", seletor.getHour(), seletor.getMinute())));

        seletor.show(tela.getSupportFragmentManager(), "hora_coleta");
    }

    private void salvar() {

        String inicio = campoInicio.getText().toString();
        String fim = campoFim.getText().toString();

        if (campoCondominio.getText().length() == 0
                || campoData.getText().length() == 0
                || inicio.isEmpty()
                || fim.isEmpty()) {
            Toast.makeText(tela, R.string.preencha_todos_os_campos, Toast.LENGTH_SHORT).show();
            return;
        }

        if (fim.compareTo(inicio) <= 0) {
            Toast.makeText(tela, R.string.fim_antes_do_inicio, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(tela, R.string.coleta_salva_exemplo, Toast.LENGTH_SHORT).show();

        dialogo.dismiss();

        aoSalvar.run();
    }
}
