package com.example.ecociente.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.example.ecociente.demo.ApiDemo;
import com.example.ecociente.demo.ArmazemEmMemoria;
import com.example.ecociente.demo.EstadoDemo;
import com.example.ecociente.demo.ProvedorContaDemo;
import com.example.ecociente.demo.RespostaDemo;
import com.example.ecociente.demo.SementeDemo;
import com.example.ecociente.model.PostagemFeed;
import com.example.ecociente.model.QuizConteudo;
import com.example.ecociente.model.QuizResumo;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

// O roteiro do stand de ponta a ponta, só na camada da API falsa, e a checagem de que nenhuma rota do morador devolve 404.
public class DemoFluxoCompletoTest {

    private static final String MOBILE = "https://ds-mobile-api.onrender.com";
    private static final String PONTOS = "https://ds-pontuacao-api.onrender.com";
    private static final String CHAT_RENDER = "https://ia-eko.onrender.com/api/v1/chat";
    private static final String CHAT_PRINCIPAL = "https://api.52.71.117.143.sslip.io/api/v1/chat";
    private static final String UPLOAD = "https://api.cloudinary.com/v1_1/ncwfxyfi/image/upload";
    private static final long AGORA = 1_700_000_000_000L;

    private final long[] relogio = {AGORA};

    private EstadoDemo estado;
    private ApiDemo api;
    private ProvedorContaDemo conta;
    private String token;

    @Before
    public void preparar() throws Exception {
        estado = new EstadoDemo(new ArmazemEmMemoria(), () -> relogio[0]);
        api = new ApiDemo(estado, () -> relogio[0]);
        conta = new ProvedorContaDemo(estado, () -> {});
        token = entrar();
    }

    private String entrar() throws Exception {
        assertTrue(conta.entrar(SementeDemo.EMAIL, SementeDemo.SENHA).isSucesso());

        String corpo = new JSONObject().put("email", SementeDemo.EMAIL).put("senha", SementeDemo.SENHA).toString();
        RespostaDemo login = api.tratar("POST", MOBILE + "/auth/login", null, corpo.getBytes(StandardCharsets.UTF_8));

        assertEquals(200, login.codigo);

        return "Bearer " + new JSONObject(login.texto()).getString("token");
    }

    private RespostaDemo chamar(String metodo, String url, String corpo) {
        return api.tratar(metodo, url, token, corpo == null ? new byte[0] : corpo.getBytes(StandardCharsets.UTF_8));
    }

    private RespostaDemo obter(String url) {
        return chamar("GET", url, null);
    }

    private long saldo() throws Exception {
        RespostaDemo r = obter(PONTOS + "/api/v1/pontuacoes/usuarios/42/saldo");

        assertEquals(200, r.codigo);

        return PontuacaoJson.saldo(new JSONObject(r.texto()));
    }

    private long posicao() throws Exception {
        RespostaDemo r = obter(PONTOS + "/api/v1/rankings/moradores/42/posicao?condominioId=7");

        assertEquals(200, r.codigo);

        return PontuacaoJson.posicao(new JSONObject(r.texto())).getPosicao();
    }

    private List<PostagemFeed> postagens(String caminho) throws Exception {
        RespostaDemo r = obter(MOBILE + caminho);

        assertEquals(200, r.codigo);

        return PostagemJson.postagens(new JSONObject(r.texto()), relogio[0]);
    }

    private int primeiroQuizId() throws Exception {
        int aula = primeiraAulaComQuiz();

        return new JSONObject(obter(MOBILE + "/api/aulas/" + aula + "/quiz").texto()).getInt("id");
    }

    private int primeiraAulaComQuiz() throws Exception {
        JSONArray cursos = new JSONArray(obter(MOBILE + "/api/cursos").texto());

        for (int i = 0; i < cursos.length(); i++) {
            JSONArray aulas =
                    new JSONArray(
                            obter(MOBILE + "/api/cursos/" + cursos.getJSONObject(i).getInt("id") + "/aulas").texto());

            for (int j = 0; j < aulas.length(); j++) {
                int id = aulas.getJSONObject(j).getInt("id");

                if (obter(MOBILE + "/api/aulas/" + id + "/quiz").codigo == 200) {
                    return id;
                }
            }
        }

        throw new AssertionError("Nenhuma aula com quiz");
    }

    private int abrirTentativa(int quizId) throws Exception {
        RespostaDemo r = chamar("POST", MOBILE + "/api/quizzes/" + quizId + "/tentativas", "{}");

        assertTrue(r.codigo >= 200 && r.codigo < 300);

        return TentativaQuizJson.tentativaId(new JSONObject(r.texto()));
    }

    private QuizResumo quizDaAula(int aula) throws Exception {
        return QuizJson.montar("Curso", "Aula", new JSONObject(obter(MOBILE + "/api/aulas/" + aula + "/quiz").texto()));
    }

    private void finalizar(int tentativa, QuizResumo quiz, int[] respostas) throws Exception {
        RespostaDemo r =
                chamar(
                        "POST",
                        MOBILE + "/api/tentativas-quiz/" + tentativa + "/finalizar",
                        TentativaQuizJson.corpoFinalizar(quiz.getPerguntas(), respostas));

        assertEquals(200, r.codigo);
    }

    // Faz uma tentativa errada (não pontua) só para ler o gabarito na revisão.
    private int[] gabarito(QuizResumo quiz) throws Exception {
        int tentativa = abrirTentativa(quiz.getId());

        finalizar(tentativa, quiz, new int[quiz.getPerguntas().size()]);

        RespostaDemo revisao = obter(MOBILE + "/api/tentativas-quiz/" + tentativa + "/revisao");

        assertEquals(200, revisao.codigo);

        List<QuizConteudo.Pergunta> certas =
                TentativaQuizJson.comGabarito(new JSONObject(revisao.texto()), quiz.getPerguntas());
        int[] respostas = new int[certas.size()];

        for (int i = 0; i < respostas.length; i++) {
            respostas[i] = certas.get(i).getIndiceCorreto();
        }

        return respostas;
    }

    private Set<Integer> quizzesConcluidos() throws Exception {
        RespostaDemo r = obter(MOBILE + "/api/tentativas-quiz/quizzes-concluidos");

        assertEquals(200, r.codigo);

        return QuizzesConcluidosJson.ids(new JSONArray(r.texto()));
    }

    @Test
    public void roteiroDoStand() throws Exception {
        assertEquals(320, saldo());
        assertEquals(4, posicao());

        // Registrar descarte.
        String corpo = PostagemJson.corpoCriar(7, 1, "file:///memoria/foto.jpg", "hash-stand", PostagemJson.iso(relogio[0]));

        assertEquals(201, chamar("POST", MOBILE + "/api/postagens", corpo).codigo);
        assertEquals(1, postagens("/api/postagens/minhas?size=20").size());
        assertEquals(320, saldo());

        // 20 s depois, a aprovação cai sozinha.
        relogio[0] += 20_000;

        assertEquals(370, saldo());
        assertEquals(3, posicao());
        assertEquals("Aprovada", postagens("/api/postagens/minhas?size=20").get(0).getStatus());

        // Quiz aprovado, recompensa de 40.
        int aula = primeiraAulaComQuiz();
        QuizResumo quiz = quizDaAula(aula);
        int[] certas = gabarito(quiz);

        assertEquals(370, saldo());

        finalizar(abrirTentativa(quiz.getId()), quiz, certas);

        assertEquals(40, quiz.getPontos());
        assertEquals(410, saldo());
        assertTrue(quizzesConcluidos().contains(quiz.getId()));

        // Sair e entrar de novo: tudo volta ao começo.
        conta.sair();

        assertEquals(null, conta.usuario());

        token = entrar();

        assertEquals(320, saldo());
        assertEquals(4, posicao());
        assertTrue(postagens("/api/postagens/minhas?size=20").isEmpty());
        assertTrue(quizzesConcluidos().isEmpty());
    }

    @Test
    public void todaRotaDoMorador_naoDevolve404() throws Exception {
        int quizId = primeiroQuizId();
        int tentativa = abrirTentativa(quizId);
        int emAnalise = -1;

        for (PostagemFeed p : postagens("/api/postagens?condominioId=7&size=20")) {
            if (PostagemJson.statusLegivel("em_analise").equals(p.getStatus())) {
                emAnalise = p.getId();
                break;
            }
        }

        assertNotEquals(-1, emAnalise);

        String periodo = "?dataInicio=2020-01-01T00:00:00&dataFim=2035-12-31T23:59:59&size=100";
        String registro =
                PostagemJson.corpoCriar(7, 1, "file:///memoria/foto.jpg", "hash-rotas", PostagemJson.iso(relogio[0]));
        String dispositivo = "{\"instalacaoId\":\"i1\",\"token\":\"fcm\",\"preferencias\":{}}";
        String upload =
                "--L\r\nContent-Disposition: form-data; name=\"upload_preset\"\r\n\r\necociente_feed\r\n"
                        + "--L\r\nContent-Disposition: form-data; name=\"file\"; filename=\"f.jpg\"\r\n"
                        + "Content-Type: image/jpeg\r\n\r\nJPEG\r\n--L--\r\n";
        String chat = "{\"mensagem\":\"onde jogo pilha?\"}";
        String[][] rotas = {
            // Conta e perfil
            {"GET", MOBILE + "/api/usuarios/me", null},
            {"PATCH", MOBILE + "/api/usuarios/me", "{\"nome\":\"Marina Souza\"}"},
            {"PATCH", MOBILE + "/api/usuarios/me/endereco", "{\"cep\":\"01001000\",\"numero\":\"10\"}"},
            {"GET", "https://viacep.com.br/ws/01001000/json/", null},
            {"POST", MOBILE + "/api/notificacoes/dispositivos", dispositivo},
            {"DELETE", MOBILE + "/api/notificacoes/dispositivos/i1", null},
            // Pontuação e ranking
            {"GET", PONTOS + "/api/v1/pontuacoes/usuarios/42/saldo", null},
            {"GET", PONTOS + "/api/v1/pontuacoes/usuarios/42/movimentacoes?pagina=0&tamanho=10", null},
            {"GET", PONTOS + "/api/v1/rankings/moradores?condominioId=7&limit=10", null},
            {"GET", PONTOS + "/api/v1/rankings/moradores/42/posicao?condominioId=7", null},
            // Feed
            {"GET", MOBILE + "/api/postagens?condominioId=7&size=20", null},
            {"GET", MOBILE + "/api/postagens/minhas?size=20", null},
            {"GET", MOBILE + "/api/categorias-residuos", null},
            {"POST", MOBILE + "/api/postagens", registro},
            {"POST", MOBILE + "/api/postagens/" + emAnalise + "/votos", PostagemJson.corpoVoto(true, 0)},
            // Educação e quiz
            {"GET", MOBILE + "/api/cursos", null},
            {"GET", MOBILE + "/api/cursos/1/aulas", null},
            {"GET", MOBILE + "/api/aulas/" + primeiraAulaComQuiz() + "/quiz", null},
            {"POST", MOBILE + "/api/quizzes/" + quizId + "/tentativas", "{}"},
            {"POST", MOBILE + "/api/tentativas-quiz/" + tentativa + "/finalizar",
                "{\"respostas\":[{\"perguntaId\":1,\"alternativaId\":1}]}"},
            {"GET", MOBILE + "/api/tentativas-quiz/" + tentativa + "/revisao", null},
            {"GET", MOBILE + "/api/tentativas-quiz?limite=20", null},
            {"GET", MOBILE + "/api/tentativas-quiz/quizzes-concluidos", null},
            // Calendário
            {"GET", MOBILE + "/api/v1/agendamentos" + periodo, null},
            {"GET", MOBILE + "/api/v1/agendamentos?page=0&size=100", null},
            {"GET", MOBILE + "/api/v1/agendamentos/proxima", null},
            // Eko
            {"POST", CHAT_RENDER, chat},
            {"POST", CHAT_PRINCIPAL, chat},
            // Health checks do aquecimento
            {"GET", MOBILE + "/", null},
            {"GET", PONTOS + "/", null},
            // Upload da foto do descarte
            {"POST", UPLOAD, upload},
            // Senha (por último: muda a senha da conta)
            {"POST", MOBILE + "/senhas/alterar",
                "{\"senhaAtual\":\"" + SementeDemo.SENHA + "\",\"novaSenha\":\"nova12345\",\"confirmacaoSenha\":\"nova12345\"}"},
            {"DELETE", MOBILE + "/api/usuarios/me", null},
        };

        for (String[] rota : rotas) {
            RespostaDemo r = chamar(rota[0], rota[1], rota[2]);

            assertNotEquals(rota[0] + " " + rota[1] + " devolveu 404", 404, r.codigo);
            assertFalse(rota[0] + " " + rota[1] + " devolveu " + r.codigo, r.codigo >= 500);
        }
    }
}
