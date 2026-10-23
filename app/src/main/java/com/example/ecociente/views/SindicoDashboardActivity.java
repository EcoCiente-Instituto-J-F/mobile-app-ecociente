package com.example.ecociente.views;

import android.widget.TextView;
import com.example.ecociente.ui.Motion;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecociente.R;
import com.example.ecociente.model.ResumoDashboard;
import com.example.ecociente.repository.DashboardSindicoRepository;
import com.example.ecociente.repository.PerfilApiRepository;
import com.example.ecociente.ui.AvisoFlutuante;
import com.example.ecociente.ui.JanelaEdgeToEdge;
import com.example.ecociente.ui.InsetsSistema;
import com.example.ecociente.ui.BarraNavegacaoView;
import com.example.ecociente.ui.ItensBarra;
import com.example.ecociente.ui.Navegacao;

/** Dashboard do síndico: números calculados das postagens e coletas do condomínio. */
public final class SindicoDashboardActivity extends AppCompatActivity {
    private static final int[] CORES = {R.color.verde_escuro_principal, R.color.rosa_ecociente,
            R.color.verde_claro_ecociente, R.color.amarelo_avaliacao, R.color.amarelo_botao_avaliar};

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        JanelaEdgeToEdge.aplicar(this);
        setContentView(R.layout.activity_sindico_dashboard);

        Motion.entrar(
                this,
                R.id.tituloSindicoDashboard,
                R.id.graficoQuizzesSindico,
                R.id.graficoCooperativasSindico);

        View raiz = findViewById(R.id.raizSindicoDashboard);
        InsetsSistema.aplicarComoPadding(raiz);

        findViewById(R.id.voltarSindicoDashboard).setOnClickListener(view -> finish());
        findViewById(R.id.botaoTentarDashboard).setOnClickListener(view -> carregar());
        exibirSemDados();

        BarraNavegacaoView barra = findViewById(R.id.barraInferiorSindico);
        barra.configurar(ItensBarra.sindico());
        Navegacao.marcarNaBarra(this, barra, ItensBarra.HOME);
        barra.setOnAssistenteClickListener(
                view -> startActivity(new Intent(this, ChatActivity.class)));
        barra.setOnItemClickListener(
                indice -> Navegacao.irParaItemDoSindico(this, indice, ItensBarra.NENHUM));
    }

    @Override
    protected void onStart() {
        super.onStart();
        carregar();
    }

    private void carregar() {
        int condominioId = PerfilApiRepository.condominioId(this);

        if (condominioId <= 0) {
            AvisoFlutuante.mostrar(this, R.string.sindico_coleta_sem_condominio);
            return;
        }

        findViewById(R.id.botaoTentarDashboard).setVisibility(View.GONE);

        for (int id : new int[]{R.id.valorAulasConcluidas, R.id.valorMoradoresNoApp, R.id.valorPontosDoMes,
                R.id.valorPontosNoMesDashboard}) {
            ((TextView) findViewById(id)).setText(R.string.sindico_carregando_dado);
        }

        new DashboardSindicoRepository().carregar(this, condominioId, resumo -> {
            if (isFinishing() || isDestroyed()) return;

            if (resumo == null) {
                exibirSemDados();
                findViewById(R.id.botaoTentarDashboard).setVisibility(View.VISIBLE);
                AvisoFlutuante.mostrar(this, R.string.sindico_dashboard_erro);
                return;
            }

            exibir(resumo);
        });
    }

    private void exibir(ResumoDashboard resumo) {
        ((TextView) findViewById(R.id.valorAulasConcluidas)).setText(String.valueOf(resumo.getReciclados()));
        ((TextView) findViewById(R.id.valorMoradoresNoApp)).setText(String.valueOf(resumo.getMoradores()));
        ((TextView) findViewById(R.id.valorPontosDoMes)).setText(String.valueOf(resumo.getColetasRealizadas()));
        ((TextView) findViewById(R.id.valorPontosNoMesDashboard)).setText(String.valueOf(resumo.getPontosDoMes()));

        String[] quizzes = resumo.getQuizzes();
        int[] notas = resumo.getNotasDosQuizzes();
        SindicoDashboardGraficoView graficoQuizzes = findViewById(R.id.graficoQuizzesSindico);
        graficoQuizzes.setDados(quizzes, notas, repetirCor(R.color.verde_escuro_principal, quizzes.length), 100);
        StringBuilder notasTexto = new StringBuilder();
        for (int i = 0; i < quizzes.length; i++) {
            if (i > 0) notasTexto.append(", ");
            notasTexto.append(quizzes[i]).append(' ').append(notas[i]);
        }
        graficoQuizzes.setContentDescription(quizzes.length == 0
                ? getString(R.string.sindico_sem_quizzes) : getString(R.string.sindico_notas_dos_quizzes, notasTexto));

        String[] materiais = resumo.getMateriais();
        int[] percentuais = resumo.getPercentuais();
        SindicoDashboardRoscaView rosca = findViewById(R.id.roscaMateriaisSindico);
        rosca.setPercentuais(percentuais);
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < materiais.length; i++) {
            if (i > 0) texto.append("  ·  ");
            texto.append(materiais[i]).append(' ').append(percentuais[i]).append('%');
        }
        String descricao = materiais.length == 0 ? getString(R.string.sindico_sem_descartes) : texto.toString();
        ((TextView) findViewById(R.id.textoMateriaisPercentuais)).setText(descricao);
        rosca.setContentDescription(descricao);

        String[] cooperativas = resumo.getCooperativas();
        int[] coletas = resumo.getColetasPorCooperativa();
        int[] cores = new int[cooperativas.length];
        for (int i = 0; i < cores.length; i++) cores[i] = CORES[i % CORES.length];
        SindicoDashboardGraficoView graficoCooperativas = findViewById(R.id.graficoCooperativasSindico);
        graficoCooperativas.setDados(cooperativas, coletas, cores, maximo(coletas));
        StringBuilder coletasTexto = new StringBuilder();
        for (int i = 0; i < cooperativas.length; i++) {
            if (i > 0) coletasTexto.append(", ");
            coletasTexto.append(cooperativas[i]).append(' ').append(coletas[i]);
        }
        graficoCooperativas.setContentDescription(cooperativas.length == 0
                ? getString(R.string.sindico_sem_coletas_realizadas) : coletasTexto.toString());
    }

    private static int[] repetirCor(int cor, int quantidade) {
        int[] cores = new int[quantidade];
        java.util.Arrays.fill(cores, cor);
        return cores;
    }

    // Maior valor com um mínimo de 3 para a escala do eixo não ficar toda repetida.
    private static int maximo(int[] valores) {
        int maior = 3;
        for (int valor : valores) maior = Math.max(maior, valor);
        return maior % 3 == 0 ? maior : maior + 3 - maior % 3;
    }

    private void exibirSemDados() {
        for (int id : new int[]{R.id.valorAulasConcluidas, R.id.valorMoradoresNoApp,
                R.id.valorPontosDoMes, R.id.textoMateriaisPercentuais}) {
            ((TextView) findViewById(id)).setText(R.string.sindico_sem_dado);
        }

        ((SindicoDashboardGraficoView) findViewById(R.id.graficoQuizzesSindico))
                .setDados(new String[0], new int[0], new int[0], 1);
        ((SindicoDashboardGraficoView) findViewById(R.id.graficoCooperativasSindico))
                .setDados(new String[0], new int[0], new int[0], 1);
        ((SindicoDashboardRoscaView) findViewById(R.id.roscaMateriaisSindico)).setPercentuais(new int[0]);
    }
}
