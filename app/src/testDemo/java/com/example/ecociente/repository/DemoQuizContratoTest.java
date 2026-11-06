package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.demo.ApiDemo;
import com.example.ecociente.demo.ArmazemEmMemoria;
import com.example.ecociente.demo.EstadoDemo;
import com.example.ecociente.demo.RespostaDemo;
import com.example.ecociente.demo.SementeDemo;
import com.example.ecociente.model.DesempenhoQuiz;
import com.example.ecociente.model.MovimentacaoPontos;
import com.example.ecociente.model.QuizConteudo;
import com.example.ecociente.model.QuizResumo;
import com.example.ecociente.model.ResultadoQuiz;
import com.example.ecociente.util.FormatoDataApi;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

// Cursos, aulas, quiz, tentativas, revisão, histórico e quizzes concluídos, lidos pelos parsers reais.
public class DemoQuizContratoTest {

    private static final String MOBILE = "https://ds-mobile-api.onrender.com";
    private static final long AGORA = 1_700_000_000_000L;
    private static final int[] RECOMPENSAS = {40, 30, 50};

    private final long[] relogio = {AGORA};

    private ArmazemEmMemoria armazem;
    private EstadoDemo estado;
    private ApiDemo api;
    private String token;

    @Before
    public void preparar() throws Exception {
        armazem = new ArmazemEmMemoria();
        abrir();

        token = entrar();
    }

    private void abrir() {
        estado = new EstadoDemo(armazem, () -> relogio[0]);
        api = new ApiDemo(estado, () -> relogio[0]);
    }

    private String entrar() throws Exception {
        String corpo = new JSONObject().put("email", SementeDemo.EMAIL).put("senha", SementeDemo.SENHA).toString();
        RespostaDemo login = api.tratar("POST", MOBILE + "/auth/login", null, corpo.getBytes(StandardCharsets.UTF_8));

        return "Bearer " + new JSONObject(login.texto()).getString("token");
    }

    private RespostaDemo obter(String caminho) {
        return api.tratar("GET", MOBILE + caminho, token, new byte[0]);
    }

    private RespostaDemo enviar(String caminho, String json) {
        return api.tratar("POST", MOBILE + caminho, token, json.getBytes(StandardCharsets.UTF_8));
    }

    private JSONArray cursos() throws Exception {
        RespostaDemo r = obter("/api/cursos");

        assertEquals(200, r.codigo);

        return new JSONArray(r.texto());
    }

    // Ids das aulas, na ordem dos cursos, que têm quiz.
    private List<Integer> aulasComQuiz() throws Exception {
        List<Integer> ids = new ArrayList<>();
        JSONArray cursos = cursos();

        for (int i = 0; i < cursos.length(); i++) {
            JSONArray aulas =
                    new JSONArray(obter("/api/cursos/" + cursos.getJSONObject(i).getInt("id") + "/aulas").texto());

            for (int j = 0; j < aulas.length(); j++) {
                int id = aulas.getJSONObject(j).getInt("id");

                if (obter("/api/aulas/" + id + "/quiz").codigo == 200) {
                    ids.add(id);
                }
            }
        }

        return ids;
    }

    private QuizResumo quiz(int indice) throws Exception {
        int aula = aulasComQuiz().get(indice);

        return QuizJson.montar("Curso", "Aula", new JSONObject(obter("/api/aulas/" + aula + "/quiz").texto()));
    }

    private int abrirTentativa(int quizId) throws Exception {
        RespostaDemo r = enviar("/api/quizzes/" + quizId + "/tentativas", "{}");

        assertTrue(r.codigo >= 200 && r.codigo < 300);

        return TentativaQuizJson.tentativaId(new JSONObject(r.texto()));
    }

    // Revisão da tentativa já finalizada: dá a alternativa certa de cada pergunta.
    private List<QuizConteudo.Pergunta> gabarito(int tentativa, QuizResumo quiz) throws Exception {
        RespostaDemo r = obter("/api/tentativas-quiz/" + tentativa + "/revisao");

        assertEquals(200, r.codigo);

        return TentativaQuizJson.comGabarito(new JSONObject(r.texto()), quiz.getPerguntas());
    }

    private static int[] todasCertas(List<QuizConteudo.Pergunta> certas) {
        int[] respostas = new int[certas.size()];

        for (int i = 0; i < respostas.length; i++) {
            respostas[i] = certas.get(i).getIndiceCorreto();
        }

        return respostas;
    }

    private static int[] todasErradas(List<QuizConteudo.Pergunta> certas) {
        int[] respostas = new int[certas.size()];

        for (int i = 0; i < respostas.length; i++) {
            respostas[i] = (certas.get(i).getIndiceCorreto() + 1) % certas.get(i).getOpcoes().length;
        }

        return respostas;
    }

    private ResultadoQuiz finalizar(int tentativa, QuizResumo quiz, int[] respostas) throws Exception {
        RespostaDemo r =
                enviar(
                        "/api/tentativas-quiz/" + tentativa + "/finalizar",
                        TentativaQuizJson.corpoFinalizar(quiz.getPerguntas(), respostas));

        assertEquals(200, r.codigo);

        return TentativaQuizJson.resultado(new JSONObject(r.texto()));
    }

    // Descobre o gabarito fazendo uma tentativa errada de propósito (não pontua).
    private List<QuizConteudo.Pergunta> descobrirGabarito(QuizResumo quiz) throws Exception {
        int tentativa = abrirTentativa(quiz.getId());
        int[] erradas = new int[quiz.getPerguntas().size()];

        finalizar(tentativa, quiz, erradas);

        return gabarito(tentativa, quiz);
    }

    private List<MovimentacaoPontos> movimentacoes() throws Exception {
        RespostaDemo r =
                api.tratar(
                        "GET",
                        "https://ds-pontuacao-api.onrender.com/api/v1/pontuacoes/usuarios/42/movimentacoes?pagina=0&tamanho=50",
                        token,
                        new byte[0]);

        assertEquals(200, r.codigo);

        return PontuacaoJson.movimentacoes(new JSONObject(r.texto()));
    }

    private static int saldoDe(EstadoDemo estado) {
        return estado.saldo();
    }

    // ---- Cursos e aulas ----

    @Test
    public void cursos_temAoMenosCincoComIdsPositivos() throws Exception {
        JSONArray cursos = cursos();
        Set<Integer> ids = new HashSet<>();

        assertTrue(cursos.length() >= 5);

        for (int i = 0; i < cursos.length(); i++) {
            JSONObject curso = cursos.getJSONObject(i);

            assertTrue(curso.getInt("id") > 0);
            assertTrue(ids.add(curso.getInt("id")));
            assertFalse(curso.getString("titulo").isEmpty());
            assertFalse(curso.getString("descricao").isEmpty());
        }
    }

    @Test
    public void aulas_temIdsPositivosEOrdem() throws Exception {
        JSONArray cursos = cursos();
        int comAulas = 0;
        Set<Integer> idsAulas = new HashSet<>();

        for (int i = 0; i < cursos.length(); i++) {
            RespostaDemo r = obter("/api/cursos/" + cursos.getJSONObject(i).getInt("id") + "/aulas");

            assertEquals(200, r.codigo);

            JSONArray aulas = QuizJson.ordenarPorOrdem(new JSONArray(r.texto()));

            comAulas += aulas.length() > 0 ? 1 : 0;

            for (int j = 0; j < aulas.length(); j++) {
                JSONObject aula = aulas.getJSONObject(j);

                assertTrue(aula.getInt("id") > 0);
                assertTrue(idsAulas.add(aula.getInt("id")));
                assertEquals(j + 1, aula.getInt("ordem"));
                assertFalse(aula.getString("titulo").isEmpty());
                assertFalse(aula.getString("conteudo").isEmpty());
            }
        }

        assertTrue(comAulas >= 3);
    }

    @Test
    public void aulas_cursoInexistente_404() {
        assertEquals(404, obter("/api/cursos/9999/aulas").codigo);
    }

    @Test
    public void educacao_semToken_401() {
        assertEquals(401, api.tratar("GET", MOBILE + "/api/cursos", null, new byte[0]).codigo);
        assertEquals(401, api.tratar("GET", MOBILE + "/api/tentativas-quiz", "Bearer errado", new byte[0]).codigo);
        assertEquals(
                401, api.tratar("POST", MOBILE + "/api/quizzes/101/tentativas", null, "{}".getBytes(StandardCharsets.UTF_8)).codigo);
    }

    // ---- Quiz ----

    @Test
    public void quiz_tresAulasComQuiz_montamPeloParserReal() throws Exception {
        assertEquals(3, aulasComQuiz().size());

        Set<Integer> quizIds = new HashSet<>();

        for (int i = 0; i < 3; i++) {
            QuizResumo quiz = quiz(i);

            assertTrue(quiz.getId() > 0);
            assertTrue(quizIds.add(quiz.getId()));
            assertEquals(RECOMPENSAS[i], quiz.getPontos());
            assertEquals(70.0, quiz.getNotaMinimaAprovacao(), 0.0);
            assertFalse(quiz.getTitulo().isEmpty());
            assertTrue(quiz.getPerguntas().size() >= 3);

            Set<Integer> perguntaIds = new HashSet<>();

            for (QuizConteudo.Pergunta pergunta : quiz.getPerguntas()) {
                assertTrue(pergunta.getId() > 0);
                assertTrue(perguntaIds.add(pergunta.getId()));
                assertTrue(pergunta.getOpcoes().length >= 2);
                assertEquals(-1, pergunta.getIndiceCorreto());

                Set<Integer> alternativas = new HashSet<>();

                for (int j = 0; j < pergunta.getOpcoes().length; j++) {
                    assertTrue(pergunta.getIdAlternativa(j) > 0);
                    assertTrue(alternativas.add(pergunta.getIdAlternativa(j)));
                }
            }
        }
    }

    @Test
    public void quiz_naoExpoeCorretaNemExplicacao() throws Exception {
        String corpo = obter("/api/aulas/" + aulasComQuiz().get(0) + "/quiz").texto();

        assertFalse(corpo.contains("correta"));
        assertFalse(corpo.contains("explicacao"));
    }

    @Test
    public void quiz_outraAula_404() {
        assertEquals(404, obter("/api/aulas/2/quiz").codigo);
        assertEquals(404, obter("/api/aulas/9999/quiz").codigo);
    }

    // ---- Tentativas ----

    @Test
    public void tentativa_devolveIdPositivo_eIdsDistintos() throws Exception {
        int quizId = quiz(0).getId();
        int a = abrirTentativa(quizId);
        int b = abrirTentativa(quizId);

        assertTrue(a > 0);
        assertTrue(b > 0);
        assertNotEquals(a, b);
    }

    @Test
    public void tentativa_quizInexistente_404() {
        assertEquals(404, enviar("/api/quizzes/9999/tentativas", "{}").codigo);
    }

    @Test
    public void finalizar_tudoCerto_notaCem_creditaARecompensa() throws Exception {
        for (int i = 0; i < 3; i++) {
            QuizResumo quiz = quiz(i);
            List<QuizConteudo.Pergunta> certas = descobrirGabarito(quiz);
            int saldoAntes = saldoDe(estado);

            int tentativa = abrirTentativa(quiz.getId());
            ResultadoQuiz resultado = finalizar(tentativa, quiz, todasCertas(certas));

            assertEquals(tentativa, resultado.getTentativaId());
            assertEquals(100.0, resultado.getNota(), 0.0);
            assertTrue(resultado.isAprovado());
            assertEquals(quiz.getPerguntas().size(), resultado.getAcertos());
            assertEquals(quiz.getPerguntas().size(), resultado.getTotalPerguntas());
            assertEquals(RECOMPENSAS[i], resultado.getPontosConcedidos());
            assertEquals(saldoAntes + RECOMPENSAS[i], saldoDe(estado));
        }
    }

    @Test
    public void finalizar_pontoVaiParaMovimentacoesComoQuiz() throws Exception {
        QuizResumo quiz = quiz(0);
        List<QuizConteudo.Pergunta> certas = descobrirGabarito(quiz);

        finalizar(abrirTentativa(quiz.getId()), quiz, todasCertas(certas));

        MovimentacaoPontos ultima = movimentacoes().get(0);

        assertEquals(MovimentacaoPontos.Origem.QUIZ, ultima.getOrigem());
        assertEquals(MovimentacaoPontos.Tipo.CREDITO, ultima.getTipo());
        assertEquals(40, ultima.getPontos());
        assertEquals(320 + 40, saldoDe(estado));
    }

    @Test
    public void finalizar_reprovado_zeroPontos_saldoIgual() throws Exception {
        QuizResumo quiz = quiz(1);
        List<QuizConteudo.Pergunta> certas = descobrirGabarito(quiz);
        int tentativa = abrirTentativa(quiz.getId());

        ResultadoQuiz resultado = finalizar(tentativa, quiz, todasErradas(certas));

        assertEquals(0.0, resultado.getNota(), 0.0);
        assertFalse(resultado.isAprovado());
        assertEquals(0, resultado.getAcertos());
        assertEquals(0, resultado.getPontosConcedidos());
        assertEquals(320, saldoDe(estado));
    }

    @Test
    public void finalizar_notaNoLimite_aprova() throws Exception {
        // 4 perguntas: 3 certas = 75 >= 70.
        QuizResumo quiz = quiz(0);
        List<QuizConteudo.Pergunta> certas = descobrirGabarito(quiz);
        int[] respostas = todasCertas(certas);
        respostas[0] = (respostas[0] + 1) % certas.get(0).getOpcoes().length;

        ResultadoQuiz resultado = finalizar(abrirTentativa(quiz.getId()), quiz, respostas);

        assertEquals(75.0, resultado.getNota(), 0.0);
        assertTrue(resultado.isAprovado());
        assertEquals(40, resultado.getPontosConcedidos());
    }

    @Test
    public void repetir_aprovado_naoPontuaDeNovo() throws Exception {
        QuizResumo quiz = quiz(2);
        List<QuizConteudo.Pergunta> certas = descobrirGabarito(quiz);

        ResultadoQuiz primeira = finalizar(abrirTentativa(quiz.getId()), quiz, todasCertas(certas));
        int saldo = saldoDe(estado);
        int movimentos = movimentacoes().size();

        ResultadoQuiz segunda = finalizar(abrirTentativa(quiz.getId()), quiz, todasCertas(certas));

        assertEquals(50, primeira.getPontosConcedidos());
        assertTrue(segunda.isAprovado());
        assertEquals(100.0, segunda.getNota(), 0.0);
        assertEquals(0, segunda.getPontosConcedidos());
        assertEquals(saldo, saldoDe(estado));
        assertEquals(movimentos, movimentacoes().size());
    }

    @Test
    public void reprovadoDepoisAprovado_pontuaNaPrimeiraAprovacao() throws Exception {
        QuizResumo quiz = quiz(0);
        List<QuizConteudo.Pergunta> certas = descobrirGabarito(quiz);

        finalizar(abrirTentativa(quiz.getId()), quiz, todasErradas(certas));

        ResultadoQuiz aprovada = finalizar(abrirTentativa(quiz.getId()), quiz, todasCertas(certas));

        assertEquals(40, aprovada.getPontosConcedidos());
        assertEquals(360, saldoDe(estado));
    }

    @Test
    public void finalizar_tentativaInexistente_erroSemCredito() throws Exception {
        QuizResumo quiz = quiz(0);
        String corpo = TentativaQuizJson.corpoFinalizar(quiz.getPerguntas(), new int[] {0, 0, 0, 0});
        RespostaDemo r = enviar("/api/tentativas-quiz/9999/finalizar", corpo);

        assertEquals(404, r.codigo);
        assertEquals(320, saldoDe(estado));
    }

    @Test
    public void finalizar_duasVezesMesmaTentativa_naoCreditaDeNovo() throws Exception {
        QuizResumo quiz = quiz(0);
        List<QuizConteudo.Pergunta> certas = descobrirGabarito(quiz);
        int tentativa = abrirTentativa(quiz.getId());
        String corpo = TentativaQuizJson.corpoFinalizar(quiz.getPerguntas(), todasCertas(certas));

        assertEquals(200, enviar("/api/tentativas-quiz/" + tentativa + "/finalizar", corpo).codigo);

        int saldo = saldoDe(estado);
        RespostaDemo segunda = enviar("/api/tentativas-quiz/" + tentativa + "/finalizar", corpo);

        assertEquals(409, segunda.codigo);
        assertEquals(saldo, saldoDe(estado));
    }

    @Test
    public void finalizar_semNenhumaResposta_400() throws Exception {
        QuizResumo quiz = quiz(0);
        int tentativa = abrirTentativa(quiz.getId());

        assertEquals(400, enviar("/api/tentativas-quiz/" + tentativa + "/finalizar", "{\"respostas\":[]}").codigo);
    }

    @Test
    public void naoRespondidas_contamComoErro() throws Exception {
        QuizResumo quiz = quiz(0);
        List<QuizConteudo.Pergunta> certas = descobrirGabarito(quiz);
        int[] respostas = todasCertas(certas);

        // Só a primeira respondida, certa: 1 de 4 = 25.
        for (int i = 1; i < respostas.length; i++) {
            respostas[i] = -1;
        }

        ResultadoQuiz resultado = finalizar(abrirTentativa(quiz.getId()), quiz, respostas);

        assertEquals(25.0, resultado.getNota(), 0.0);
        assertEquals(1, resultado.getAcertos());
        assertEquals(4, resultado.getTotalPerguntas());
        assertFalse(resultado.isAprovado());
        assertEquals(0, resultado.getPontosConcedidos());
    }

    // ---- Revisão ----

    @Test
    public void revisao_marcaACorretaETrazExplicacao() throws Exception {
        QuizResumo quiz = quiz(0);
        int tentativa = abrirTentativa(quiz.getId());

        finalizar(tentativa, quiz, new int[quiz.getPerguntas().size()]);

        for (QuizConteudo.Pergunta pergunta : gabarito(tentativa, quiz)) {
            assertTrue(pergunta.getIndiceCorreto() >= 0);
            assertTrue(pergunta.getIndiceCorreto() < pergunta.getOpcoes().length);
            assertFalse(pergunta.getExplicacao().isEmpty());
        }

        assertTrue(QuizConteudo.temGabarito(gabarito(tentativa, quiz)));
    }

    @Test
    public void revisao_antesDeFinalizar_naoVazaGabarito() throws Exception {
        int tentativa = abrirTentativa(quiz(0).getId());

        assertEquals(409, obter("/api/tentativas-quiz/" + tentativa + "/revisao").codigo);
        assertEquals(404, obter("/api/tentativas-quiz/9999/revisao").codigo);
    }

    // ---- Histórico e concluídos ----

    @Test
    public void historico_vazioNoInicio() throws Exception {
        RespostaDemo r = obter("/api/tentativas-quiz?limite=20");

        assertEquals(200, r.codigo);
        assertTrue(HistoricoQuizJson.desempenhos(new JSONArray(r.texto())).isEmpty());
    }

    @Test
    public void historico_maisRecentePrimeiro_semAsNaoFinalizadas() throws Exception {
        QuizResumo primeiro = quiz(0);
        QuizResumo segundo = quiz(1);

        finalizar(abrirTentativa(primeiro.getId()), primeiro, new int[primeiro.getPerguntas().size()]);

        relogio[0] += 3_600_000L;

        int recente = abrirTentativa(segundo.getId());
        finalizar(recente, segundo, new int[segundo.getPerguntas().size()]);

        // Aberta e nunca finalizada: não entra.
        relogio[0] += 3_600_000L;
        abrirTentativa(segundo.getId());

        RespostaDemo r = obter("/api/tentativas-quiz?limite=20");
        List<DesempenhoQuiz> lista = HistoricoQuizJson.desempenhos(new JSONArray(r.texto()));

        assertEquals(2, lista.size());
        assertEquals(recente, lista.get(0).tentativaId);
        assertEquals(segundo.getTitulo(), lista.get(0).titulo);
        assertEquals(primeiro.getTitulo(), lista.get(1).titulo);
        assertTrue(lista.get(0).concluidoEm.compareTo(lista.get(1).concluidoEm) > 0);
        assertFalse(FormatoDataApi.data(lista.get(0).concluidoEm).isEmpty());
        assertFalse(FormatoDataApi.hora(lista.get(0).concluidoEm).isEmpty());
    }

    @Test
    public void historico_respeitaOLimite() throws Exception {
        QuizResumo quiz = quiz(0);

        for (int i = 0; i < 3; i++) {
            relogio[0] += 1000L;
            finalizar(abrirTentativa(quiz.getId()), quiz, new int[quiz.getPerguntas().size()]);
        }

        assertEquals(
                2, HistoricoQuizJson.desempenhos(new JSONArray(obter("/api/tentativas-quiz?limite=2").texto())).size());
    }

    @Test
    public void concluidos_soTemAprovados() throws Exception {
        QuizResumo reprovado = quiz(0);
        QuizResumo aprovado = quiz(1);
        List<QuizConteudo.Pergunta> certas = descobrirGabarito(aprovado);

        descobrirGabarito(reprovado);

        RespostaDemo antes = obter("/api/tentativas-quiz/quizzes-concluidos");

        assertEquals(200, antes.codigo);
        assertTrue(QuizzesConcluidosJson.ids(new JSONArray(antes.texto())).isEmpty());

        finalizar(abrirTentativa(aprovado.getId()), aprovado, todasCertas(certas));

        Set<Integer> ids =
                QuizzesConcluidosJson.ids(new JSONArray(obter("/api/tentativas-quiz/quizzes-concluidos").texto()));

        assertEquals(1, ids.size());
        assertTrue(ids.contains(aprovado.getId()));
        assertFalse(ids.contains(reprovado.getId()));
    }

    // ---- Persistência ----

    @Test
    public void tentativas_sobrevivemAReaberOApp() throws Exception {
        QuizResumo quiz = quiz(0);
        List<QuizConteudo.Pergunta> certas = descobrirGabarito(quiz);

        int tentativa = abrirTentativa(quiz.getId());
        finalizar(tentativa, quiz, todasCertas(certas));

        abrir();

        Set<Integer> ids =
                QuizzesConcluidosJson.ids(new JSONArray(obter("/api/tentativas-quiz/quizzes-concluidos").texto()));

        assertTrue(ids.contains(quiz.getId()));
        assertEquals(200, obter("/api/tentativas-quiz/" + tentativa + "/revisao").codigo);

        // O id da próxima não repete.
        assertTrue(abrirTentativa(quiz.getId()) > tentativa);
    }

    @Test
    public void resetar_apagaAsTentativas() throws Exception {
        QuizResumo quiz = quiz(0);

        finalizar(abrirTentativa(quiz.getId()), quiz, new int[quiz.getPerguntas().size()]);
        estado.resetar();
        token = entrar();

        assertTrue(
                HistoricoQuizJson.desempenhos(new JSONArray(obter("/api/tentativas-quiz?limite=20").texto())).isEmpty());
    }
}
