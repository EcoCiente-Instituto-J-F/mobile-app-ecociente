package com.example.ecociente.views;

import com.example.ecociente.repository.PerfilApiRepository;
import com.example.ecociente.ui.Motion;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import com.example.ecociente.repository.RotasGoogle;
import com.example.ecociente.ui.AvisoFlutuante;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecociente.R;
import com.example.ecociente.model.SindicoCooperativa;
import com.example.ecociente.viewmodels.SindicoColetaViewModel;

import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.Navegacao;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.model.StatusAgendamento;
import com.example.ecociente.repository.CooperativaRepository;
import com.example.ecociente.ui.AbrirMapa;
import com.example.ecociente.ui.IndicadorCarregamento;
import com.google.android.material.button.MaterialButton;
import com.example.ecociente.model.ResultadoSolicitacoes;
import com.example.ecociente.repository.CalendarioExternoRepository;

/** Cooperativas da cidade do condomínio (API), com a rota até elas no Google Maps. */
public final class SindicoCooperativasActivity extends AppCompatActivity {
    private SindicoColetaViewModel viewModel;
    private LinearLayout lista;
    private EditText busca;
    private boolean carregadas;
    private MaterialButton botaoOrdenar;
    private final Map<Integer, Float> distanciasEmMetros = new HashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ActivityResultLauncher<String[]> pedirLocalizacao;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        JanelaEdgeToEdge.aplicar(this);
        setContentView(R.layout.activity_sindico_cooperativas);

        Motion.entrar(
                this,
                R.id.tituloSindicoCooperativas,
                R.id.buscaCooperativas,
                R.id.listaCooperativas);

        View raiz = findViewById(R.id.raizSindicoCooperativas);
        InsetsSistema.aplicarComoPadding(raiz);

        viewModel = new ViewModelProvider(this).get(SindicoColetaViewModel.class);
        lista = findViewById(R.id.listaCooperativas);
        busca = findViewById(R.id.buscaCooperativas);
        busca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                mostrarCooperativas(s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        pedirLocalizacao = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                concedidas -> {
                    if (temPermissaoDeLocalizacao()) {
                        ordenarPorDistancia();
                    } else {
                        AvisoFlutuante.mostrar(this, R.string.localizacao_negada);
                    }
                });
        botaoOrdenar = findViewById(R.id.botaoOrdenarPorDistancia);
        botaoOrdenar.setOnClickListener(view -> ordenarPorDistancia());
        BarraNavegacaoView barra = findViewById(R.id.barraInferiorSindico);
        barra.configurar(ItensBarra.sindico());
        Navegacao.marcarNaBarra(this, barra, ItensBarra.SEGUNDO);
        barra.setOnAssistenteClickListener(
                view -> startActivity(new Intent(this, ChatActivity.class)));
        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDoSindico(this, indice, ItensBarra.SEGUNDO));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!carregadas) {
            carregarCooperativas();
        } else {
            carregarSolicitacoes();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (carregadas) {
            mostrarCooperativas(busca.getText().toString());
        }
    }

    private void carregarCooperativas() {
        lista.removeAllViews();
        IndicadorCarregamento.mostrar(lista, R.string.cooperativas_carregando);

        new CooperativaRepository().listar(this, cooperativas -> {
            if (isFinishing() || isDestroyed()) return;

            if (cooperativas == null) {
                mostrarErroDeCooperativas();
                return;
            }

            viewModel.definirCooperativas(cooperativas);
            carregadas = true;
            mostrarCooperativas(busca.getText().toString());
            carregarSolicitacoes();
        });
    }

    // Coletas já pedidas: a cooperativa com pedido em andamento mostra "Solicitação enviada".
    private void carregarSolicitacoes() {
        new CalendarioExternoRepository().listarTodos(this, resultado -> {
            if (isFinishing() || isDestroyed()
                    || resultado.getTipo() != ResultadoSolicitacoes.Tipo.SUCESSO) return;

            viewModel.definirSolicitacoes(resultado.getItens());
            mostrarCooperativas(busca.getText().toString());
        });
    }

    private boolean temPermissaoDeLocalizacao() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    // GPS quando a pessoa deu a localização precisa e ele está ligado; senão, a rede (aproximada).
    private String provedorDeLocalizacao(LocationManager gerenciador) {
        boolean precisa = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (precisa && gerenciador.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        }
        if (gerenciador.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        }
        return null;
    }

    private void ordenarPorDistancia() {
        if (!temPermissaoDeLocalizacao()) {
            pedirLocalizacao.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
            return;
        }

        LocationManager gerenciador = getSystemService(LocationManager.class);
        String provedor = gerenciador == null ? null : provedorDeLocalizacao(gerenciador);
        if (provedor == null) {
            AvisoFlutuante.mostrar(this, R.string.localizacao_desligada);
            return;
        }

        botaoOrdenar.setEnabled(false);
        botaoOrdenar.setText(R.string.sindico_buscando_localizacao);

        try {
            LocationManagerCompat.getCurrentLocation(
                    gerenciador, provedor, (android.os.CancellationSignal) null, ContextCompat.getMainExecutor(this), local -> {
                        if (isFinishing() || isDestroyed()) return;
                        if (local == null) {
                            restaurarBotaoOrdenar();
                            AvisoFlutuante.mostrar(this, R.string.localizacao_indisponivel);
                            return;
                        }
                        calcularDistancias(local);
                    });
        } catch (SecurityException erro) {
            // a permissão pode ser revogada entre a checagem e o pedido
            restaurarBotaoOrdenar();
            AvisoFlutuante.mostrar(this, R.string.localizacao_negada);
        }
    }

    // O banco não guarda coordenadas. A distância vem da Routes API; sem chave ou sem resposta, cada
    // endereço vira latitude/longitude pelo Geocoder do aparelho.
    @SuppressWarnings("deprecation") // a versão assíncrona só existe a partir do Android 13
    private void calcularDistancias(Location local) {
        List<SindicoCooperativa> cooperativas = new ArrayList<>(viewModel.getCooperativas());
        executor.execute(() -> {
            Map<Integer, Float> calculadas = RotasGoogle.distanciasEmMetros(this, local, cooperativas);
            if (calculadas.isEmpty() && Geocoder.isPresent()) {
                Geocoder geocoder = new Geocoder(this, new Locale("pt", "BR"));
                for (SindicoCooperativa cooperativa : cooperativas) {
                    try {
                        List<Address> achados =
                                geocoder.getFromLocationName(cooperativa.getEnderecoCompleto(), 1);
                        if (achados == null || achados.isEmpty()) continue;
                        float[] resultado = new float[1];
                        Location.distanceBetween(local.getLatitude(), local.getLongitude(),
                                achados.get(0).getLatitude(), achados.get(0).getLongitude(), resultado);
                        calculadas.put(cooperativa.getId(), resultado[0]);
                    } catch (IOException erro) {
                        // sem rede para o serviço de endereços: essa cooperativa fica sem distância
                    }
                }
            }
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                restaurarBotaoOrdenar();
                if (calculadas.isEmpty()) {
                    AvisoFlutuante.mostrar(this, R.string.localizacao_sem_enderecos);
                    return;
                }
                distanciasEmMetros.clear();
                distanciasEmMetros.putAll(calculadas);
                mostrarCooperativas(busca.getText().toString());
            });
        });
    }

    private void restaurarBotaoOrdenar() {
        botaoOrdenar.setEnabled(true);
        botaoOrdenar.setText(R.string.sindico_ordenar_por_distancia);
    }

    private String distanciaFormatada(float metros) {
        return metros < 1000
                ? String.format(Locale.getDefault(), "%d m", Math.round(metros))
                : String.format(Locale.getDefault(), "%.1f km", metros / 1000);
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    private void mostrarErroDeCooperativas() {
        lista.removeAllViews();

        TextView mensagem = new TextView(this);
        mensagem.setText(R.string.cooperativas_erro);
        mensagem.setTextColor(getColor(R.color.cinza_texto_home));
        mensagem.setTextSize(14);
        mensagem.setPadding(8, 28, 8, 12);
        lista.addView(mensagem);

        MaterialButton tentar = (MaterialButton) getLayoutInflater().inflate(R.layout.botao_dialogo, lista, false);
        tentar.setText(R.string.tentar_novamente);
        tentar.setOnClickListener(view -> carregarCooperativas());
        lista.addView(tentar);
    }

    private void mostrarCooperativas(String filtro) {
        lista.removeAllViews();
        int encontrados = 0;
        LayoutInflater inflater = LayoutInflater.from(this);
        List<SindicoCooperativa> ordenadas = new ArrayList<>(viewModel.getCooperativas());
        if (!distanciasEmMetros.isEmpty()) {
            ordenadas.sort(Comparator.comparing(
                    cooperativa -> distanciasEmMetros.getOrDefault(cooperativa.getId(), Float.MAX_VALUE)));
        }
        for (SindicoCooperativa cooperativa : ordenadas) {
            if (!cooperativa.corresponde(filtro)) continue;
            encontrados++;
            View item = inflater.inflate(R.layout.item_sindico_cooperativa, lista, false);
            TextView nome = item.findViewById(R.id.nomeCooperativa);
            nome.setText(cooperativa.getNome());
            nome.setContentDescription(getString(R.string.sindico_ver_informacoes, cooperativa.getNome()));
            nome.setOnClickListener(view -> {
                Intent informacoes = new Intent(this, SindicoInformacoesCooperativaActivity.class);
                informacoes.putExtra(SindicoInformacoesCooperativaActivity.EXTRA_COOPERATIVA_ID,
                        cooperativa.getId());
                startActivity(informacoes);
            });
            Float distancia = distanciasEmMetros.get(cooperativa.getId());
            ((TextView) item.findViewById(R.id.enderecoCooperativa))
                    .setText(distancia == null
                            ? cooperativa.getEnderecoCompleto()
                            : getString(R.string.sindico_endereco_com_distancia,
                                    cooperativa.getEnderecoCompleto(), distanciaFormatada(distancia)));
            ((TextView) item.findViewById(R.id.avaliacaoCooperativa))
                    .setText(cooperativa.getAvaliacao().isEmpty()
                            ? getString(R.string.sindico_sem_avaliacoes)
                            : getString(R.string.sindico_avaliacao_cooperativa,
                                    cooperativa.getAvaliacao(), cooperativa.getTotalAvaliacoes()));
            TextView comoChegar = item.findViewById(R.id.comoChegarCooperativa);
            comoChegar.setContentDescription(getString(R.string.sindico_como_chegar_em, cooperativa.getNome()));
            comoChegar.setOnClickListener(view -> AbrirMapa.rota(this, cooperativa.getEnderecoCompleto()));
            Button acao = item.findViewById(R.id.acaoCooperativa);
            Solicitacao pendente = solicitacaoEmAndamento(cooperativa.getId());
            if (pendente != null) {
                acao.setText(R.string.sindico_solicitacao_enviada);
                acao.setTextColor(getColor(R.color.etiqueta_pendente_texto));
                acao.setBackgroundTintList(getColorStateList(R.color.etiqueta_pendente_fundo));
                acao.setOnClickListener(view -> startActivity(SolicitacaoDetalheActivity.criarIntentSindico(
                        this, pendente.comNomeCondominio(PerfilApiRepository.condominioNomeOuVazio(this)),
                        cooperativa.getNome())));
                lista.addView(item, margemDoCartao());
                continue;
            }
            acao.setOnClickListener(view -> {
                Intent formulario = new Intent(this, SindicoSolicitacaoColetaActivity.class);
                formulario.putExtra(SindicoSolicitacaoColetaActivity.EXTRA_COOPERATIVA_ID,
                        cooperativa.getId());
                startActivity(formulario);
            });
            lista.addView(item, margemDoCartao());
        }
        if (encontrados == 0) {
            TextView vazio = new TextView(this);
            vazio.setText(R.string.sindico_nenhuma_cooperativa_de_exemplo_enc);
            vazio.setTextColor(getColor(R.color.cinza_texto_home));
            vazio.setTextSize(14);
            vazio.setPadding(8, 28, 8, 28);
            lista.addView(vazio);
        }
    }

    private LinearLayout.LayoutParams margemDoCartao() {
        LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(-1, -2);
        parametros.bottomMargin = Math.round(12 * getResources().getDisplayMetrics().density);
        return parametros;
    }

    private Solicitacao solicitacaoEmAndamento(int cooperativaId) {
        for (Solicitacao solicitacao : viewModel.getSolicitacoes()) {
            boolean ativa = solicitacao.getStatus() == StatusAgendamento.AGENDADO
                    || solicitacao.getStatus() == StatusAgendamento.CONFIRMADO;
            if (ativa && solicitacao.getCooperativaId() == cooperativaId) return solicitacao;
        }
        return null;
    }
}
