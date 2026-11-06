package com.example.ecociente.demo;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.ecociente.EcoCienteApplication;
import com.example.ecociente.repository.ContaAtual;
import com.example.ecociente.repository.LembretesColeta;
import java.io.File;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import org.json.JSONArray;
import org.json.JSONObject;

// Application do build `demo`. Troca a rede pela ApiDemo e o Firebase pelo ProvedorContaDemo antes de qualquer tela existir.
public class EcoCienteDemoApplication extends EcoCienteApplication {

    private static final String TAG = "EcoCienteDemo";

    private static final long INTERVALO_PENDENTES_S = 5;

    private static boolean transporteInstalado;

    @Override
    public void onCreate() {
        // Antes do super: nenhuma tela nem repositório pode abrir conexão antes da fábrica estar no lugar.
        LongSupplier relogio = System::currentTimeMillis;

        EstadoDemo estado = new EstadoDemo(new ArmazemDemoArquivos(getFilesDir()), relogio);
        ApiDemo api = new ApiDemo(estado, relogio);

        instalarTransporte(api);

        ContaAtual.usar(new ProvedorContaDemo(estado, () -> limparTudoMenosOEstado(estado)));

        super.onCreate();

        agendarPendentes(api);
    }

    // URL.setURLStreamHandlerFactory só aceita uma chamada por processo.
    private static synchronized void instalarTransporte(@NonNull ApiDemo api) {
        if (transporteInstalado) {
            return;
        }

        URL.setURLStreamHandlerFactory(
                new FabricaConexoesDemo(api, ConexaoDemo.ATRASO_MS, ConexaoImagemCloudinary::new));

        transporteInstalado = true;
    }

    // Aprovações vencidas viram resultado mesmo sem ninguém consultando a API.
    private static void agendarPendentes(@NonNull ApiDemo api) {
        ScheduledExecutorService agenda =
                Executors.newSingleThreadScheduledExecutor(
                        tarefa -> {
                            Thread fio = new Thread(tarefa, "demo-pendentes");
                            fio.setDaemon(true);
                            return fio;
                        });

        agenda.scheduleWithFixedDelay(
                () -> {
                    try {
                        api.processarPendentes();

                    } catch (RuntimeException erro) {
                        // Uma falha não pode cancelar as execuções seguintes.
                        Log.w(TAG, "Falha ao processar pendências da demo", erro);
                    }
                },
                INTERVALO_PENDENTES_S,
                INTERVALO_PENDENTES_S,
                TimeUnit.SECONDS);
    }

    // Chamado por "Sair" e "Excluir conta", antes de o estado voltar à semente. Primeiro cancela os lembretes, porque o cancelamento lê as preferências; depois zera todas. Não usa deleteSharedPreferences: o Android mantém a instância em cache e ela reapareceria com os valores antigos.
    private void limparTudoMenosOEstado(@NonNull EstadoDemo estado) {
        cancelarLembretes(estado);

        File pasta = new File(getApplicationInfo().dataDir, "shared_prefs");
        String[] arquivos = pasta.list((dir, nome) -> nome.endsWith(".xml"));

        for (int i = 0; arquivos != null && i < arquivos.length; i++) {
            String nome = arquivos[i].substring(0, arquivos[i].length() - ".xml".length());

            getSharedPreferences(nome, Context.MODE_PRIVATE).edit().clear().commit();
        }
    }

    // Agendamentos do estado, com o `id` que a LembretesColeta usa (a semente ainda não tem nenhum).
    private void cancelarLembretes(@NonNull EstadoDemo estado) {
        JSONArray agendamentos;

        synchronized (estado) {
            agendamentos = estado.raiz().optJSONArray("agendamentos");
        }

        for (int i = 0; agendamentos != null && i < agendamentos.length(); i++) {
            JSONObject agendamento = agendamentos.optJSONObject(i);
            int id = agendamento == null ? 0 : agendamento.optInt("id", 0);

            if (id > 0 && LembretesColeta.ativado(this, id)) {
                LembretesColeta.desativar(this, id);
            }
        }
    }
}
